package dev.caecorthus.sparkdlc.economy;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.trait.api.TraitIds;
import dev.caecorthus.sparkdlc.economy.KillerTeamEconomyWorldComponent;
import dev.caecorthus.sparkdlc.economy.SyncKillerTeamEconomyS2CPacket;
import dev.caecorthus.sparkdlc.record.AchievementRecords;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.event.GameEvents;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * All earning, spending and visibility decisions are made on the server from current membership.
 * 收入、支付及可见性均由服务端按当前真实阵营决定。
 */
public final class KillerTeamEconomyService {
    /**
     * A feature's say on whether a member's wallet is, right now, the wallet the purse works with. The purse names no
     * role: a role that gives a member a second wallet registers a rule from its own registration code, before a round
     * can run. Rules are order-independent: income raises the purse only when every rule counts it, and a purchase
     * draws on the purse only when no rule keeps it personal. A rule is asked before the purse's own access checks,
     * for every caller of the three entries, with the player exactly as the caller passed it.
     * 某个功能对“成员的钱包此刻是否就是团队资金所对应的钱包”的意见。团队资金不点名任何职业：给成员第二个钱包的职业在自己的注册
     * 代码中、任何对局运行之前注册一条规则。规则与顺序无关：只有所有规则都认可时收入才计入团队资金；只要有一条规则要求仅用
     * 个人余额，购买就不动用团队资金。规则先于团队资金自身的访问检查询问，对三个入口的所有调用方生效，玩家参数原样传入。
     */
    public interface WalletRule {
        /**
         * False when income landing in this player's wallet now is not killer income and must not raise the purse.
         * 此刻进入该玩家钱包的收入不是杀手收入、不得计入团队资金时返回 false。
         */
        boolean countsIncome(PlayerEntity player);

        /**
         * True when this player's purchases now neither draw on the purse nor are debited from it.
         * 该玩家此刻的购买既不动用团队资金、也不从中扣款时返回 true。
         */
        boolean keepsPurchasesPersonal(PlayerEntity player);
    }

    private static final Map<ServerPlayNetworkHandler, Snapshot> VISIBLE_SNAPSHOTS = new HashMap<>();
    // Copy-on-write: a check iterates a snapshot. / 写时复制：检查遍历快照。
    private static final List<WalletRule> WALLET_RULES = new CopyOnWriteArrayList<>();
    private static boolean registered;

    private KillerTeamEconomyService() {
    }

    public static void registerWalletRule(WalletRule rule) {
        WALLET_RULES.add(Objects.requireNonNull(rule, "rule"));
    }

    /** True when every rule counts income of {@code player}. / 所有规则都认可 {@code player} 的收入时为 true。 */
    static boolean countsIncome(Iterable<? extends WalletRule> rules, PlayerEntity player) {
        for (WalletRule rule : rules) {
            if (!rule.countsIncome(player)) {
                return false;
            }
        }
        return true;
    }

    /** True when a rule keeps purchases of {@code player} personal. / 有规则要求 {@code player} 的购买仅用个人余额时为 true。 */
    static boolean keepsPurchasesPersonal(Iterable<? extends WalletRule> rules, PlayerEntity player) {
        for (WalletRule rule : rules) {
            if (rule.keepsPurchasesPersonal(player)) {
                return true;
            }
        }
        return false;
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        GameEvents.ON_FINISH_INITIALIZE.register((world, game) -> {
            if (world instanceof ServerWorld serverWorld) {
                initializeRound(serverWorld, game);
            }
        });
        GameEvents.ON_FINISH_FINALIZE.register((world, game) -> {
            if (world instanceof ServerWorld serverWorld) {
                KillerTeamEconomyWorldComponent.KEY.get(serverWorld).clearRoundState();
                syncWorld(serverWorld);
            }
        });
        ServerTickEvents.END_WORLD_TICK.register(KillerTeamEconomyService::syncWorld);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            VISIBLE_SNAPSHOTS.remove(handler);
            syncPlayer(handler.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> VISIBLE_SNAPSHOTS.remove(handler));
        ServerWorldEvents.UNLOAD.register((server, world) -> VISIBLE_SNAPSHOTS.entrySet().removeIf(entry -> {
            if (entry.getValue().world() != world) {
                return false;
            }
            ServerPlayNetworking.send(entry.getKey().player, new SyncKillerTeamEconomyS2CPacket(false, 0));
            return true;
        }));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> VISIBLE_SNAPSHOTS.clear());
    }

    private static void initializeRound(ServerWorld world, GameWorldComponent game) {
        KillerTeamEconomyWorldComponent purse = KillerTeamEconomyWorldComponent.KEY.get(world);
        purse.clearRoundState();
        int openingMembers = 0;
        int openingKillers = 0;
        // Traits are assigned synchronously before this event, but Wathe has not set ACTIVE yet.
        // 天赋在此事件前已同步分配，但 Wathe 尚未进入 ACTIVE，开局计数不能套用存活运行门禁。
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (game.hasAnyRole(player) && isGenuineMember(player, game)) {
                openingMembers++;
                if (hasKillerRole(player, game)) {
                    openingKillers++;
                }
            }
        }
        // A lone killer with only an Impostor stays uninitialized; N still counts every genuine member.
        // 仅 1 名真杀手加内鬼时钱包保持未初始化；开启后 N 仍按全部真实成员计数。
        if (KillerTeamEconomyRules.formsTeam(openingKillers)) {
            purse.initializeRound(openingMembers);
        }
        syncWorld(world);
    }

    public static void recordIncome(PlayerEntity player, long actualIncome) {
        if (!countsIncome(WALLET_RULES, player)) {
            return;
        }
        if (actualIncome <= 0 || !(player instanceof ServerPlayerEntity serverPlayer) || !canAccess(serverPlayer)) {
            return;
        }
        ServerWorld world = serverPlayer.getServerWorld();
        KillerTeamEconomyWorldComponent purse = KillerTeamEconomyWorldComponent.KEY.get(world);
        int purseBefore = purse.getBalance();
        if (purse.recordIncome(actualIncome, SparkTraitsApi.hasActiveTrait(serverPlayer, TraitIds.TEAM_FIRST))) {
            syncWorld(world);
            // Achievement record S7: this killer's income raised the purse (amount = after - before).
            // 成就记录 S7：该杀手的收入使团队钱包增加（amount = 入账后 - 入账前）。
            AchievementRecords.teamContribution(serverPlayer, purseBefore, purse.getBalance());
        }
    }

    public static int availableForPurchase(PlayerEntity player, int personalBalance) {
        if (keepsPurchasesPersonal(WALLET_RULES, player)) {
            return personalBalance;
        }
        if (!(player instanceof ServerPlayerEntity serverPlayer) || !canAccess(serverPlayer)) {
            return personalBalance;
        }
        return KillerTeamEconomyRules.availableBalance(
                personalBalance, KillerTeamEconomyWorldComponent.KEY.get(serverPlayer.getServerWorld()).getBalance()
        );
    }

    // Called only at the existing successful purchase debit; the personal wallet never contains team funds.
    // 仅在原有购买成功扣款点调用，个人钱包从不临时写入团队余额。
    public static int personalBalanceAfterPurchase(PlayerEntity player, int personalBalance, int price) {
        if (keepsPurchasesPersonal(WALLET_RULES, player)) {
            return personalBalance - price;
        }
        int shortfall = KillerTeamEconomyRules.sharedShortfall(personalBalance, price);
        if (shortfall == 0 || !(player instanceof ServerPlayerEntity serverPlayer) || !canAccess(serverPlayer)) {
            return personalBalance - price;
        }
        ServerWorld world = serverPlayer.getServerWorld();
        if (!KillerTeamEconomyWorldComponent.KEY.get(world).spend(shortfall)) {
            return personalBalance - price;
        }
        syncWorld(world);
        return 0;
    }

    // Creative stays allowed: Wathe still shows and spends the personal wallet there, so the team row must match it.
    // 创造模式不排除：Wathe 在创造模式下仍显示并可使用个人钱包，团队钱包需与之一致。
    private static boolean canAccess(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        return KillerTeamEconomyWorldComponent.KEY.get(world).isEnabled()
                && game.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE
                && game.hasAnyRole(player) && !game.isPlayerDead(player.getUuid())
                && player.isAlive() && !player.isSpectator()
                && isGenuineMember(player, game);
    }

    private static boolean isGenuineMember(ServerPlayerEntity player, GameWorldComponent game) {
        return KillerTeamEconomyRules.isGenuineKiller(hasKillerRole(player, game),
                SparkTraitsApi.hasActiveTrait(player, TraitIds.IMPOSTOR),
                SparkTraitsApi.hasActiveTrait(player, TraitIds.CONSCIENCE));
    }

    private static boolean hasKillerRole(ServerPlayerEntity player, GameWorldComponent game) {
        Role role = game.getRole(player);
        return role != null && role.canUseKiller();
    }

    private static void syncWorld(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            syncPlayer(player);
        }
    }

    private static void syncPlayer(ServerPlayerEntity player) {
        ServerPlayNetworkHandler connection = player.networkHandler;
        Snapshot previous = VISIBLE_SNAPSHOTS.get(connection);
        if (!canAccess(player)) {
            if (previous != null) {
                ServerPlayNetworking.send(player, new SyncKillerTeamEconomyS2CPacket(false, 0));
                VISIBLE_SNAPSHOTS.remove(connection);
            }
            return;
        }
        ServerWorld world = player.getServerWorld();
        Snapshot current = new Snapshot(world, KillerTeamEconomyWorldComponent.KEY.get(world).getBalance());
        if (!current.equals(previous)) {
            // Connection-scoped snapshots reveal neither opening N nor any member roster.
            // 按连接定向同步，仅公开可见性与余额，不发送开局 N 或成员名单。
            ServerPlayNetworking.send(player, new SyncKillerTeamEconomyS2CPacket(true, current.balance()));
            VISIBLE_SNAPSHOTS.put(connection, current);
        }
    }

    private record Snapshot(ServerWorld world, int balance) {
    }
}
