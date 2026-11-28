package dev.caecorthus.sparkdlc.role.civilian.saint;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.record.AchievementRecords;
import dev.caecorthus.sparkdlc.role.civilian.saint.flash.HolyFlashFeatureService;
import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorForcedShotScope;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.event.KillPlayer;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Owns the Saint's Voodoo immunity at Wathe's kill entry point and her passive Hellfire through Wathe's public
 * post-kill event, and wires the Saint shop and the Holy Flash lifecycle. Her gun rules are in
 * {@link SaintGunShotService} and {@link SaintGunDropService}.
 * 在 Wathe 击杀入口执行圣徒的巫毒免疫，并通过 Wathe 公开的击杀后事件处理她的被动业火；同时注册圣徒商店与圣光弹生命周期。
 * 她的枪械规则位于 {@link SaintGunShotService} 与 {@link SaintGunDropService}。
 */
public final class SaintFeatureService {
    /**
     * Runs before the default phase of {@code KillPlayer.AFTER}, only to read the coins the Saint died with: the
     * Plunderer trait's listener, registered before this feature, takes part of a victim's coins in the default phase.
     * 先于 {@code KillPlayer.AFTER} 的默认阶段运行，只为读取圣徒死亡时的金币：掠夺者词条的监听器先于本功能注册，会在默认阶段
     * 拿走受害者的一部分金币。
     */
    private static final Identifier DEATH_BALANCE_PHASE = SparkDLC.id("saint_death_balance");
    /** Server thread only; an entry lives from the capture phase to the default phase of one death. / 仅服务端线程；条目只存活于一次死亡的捕获阶段到默认阶段之间。 */
    private static final Map<UUID, Integer> DEATH_BALANCES = new HashMap<>();
    private static boolean registered;

    private SaintFeatureService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        SaintEconomyService.register();
        SaintShopService.register();
        HolyFlashFeatureService.register();
        KillPlayer.AFTER.addPhaseOrdering(DEATH_BALANCE_PHASE, Event.DEFAULT_PHASE);
        KillPlayer.AFTER.register(DEATH_BALANCE_PHASE, SaintFeatureService::captureDeathBalance);
        KillPlayer.AFTER.register(SaintFeatureService::afterKill);
    }

    public static void assignForRole(ServerPlayerEntity player, Role role) {
        SaintEconomyService.assignForRole(player, role);
        if (SaintRules.isKarmaImmune(role)) {
            SaintKarmaService.clear(player);
        }
    }

    /**
     * Called by the GameFunctions mixin before Wathe's short-circuiting BEFORE listeners. Only Voodoo is refused; the
     * killer does not matter any more (owner 2026-11-28: good players can kill the Saint).
     * 由 GameFunctions mixin 在 Wathe 会短路的 BEFORE 监听器之前调用。只拒绝巫毒；凶手是谁已不再重要
     * （所有者 2026-11-28：好人可以击杀圣徒）。
     */
    public static boolean blocksKill(ServerPlayerEntity victim, Identifier deathReason) {
        GameWorldComponent gameComponent = GameWorldComponent.KEY.get(victim.getServerWorld());
        return SaintRules.blocksKill(SaintRules.isSaint(gameComponent.getRole(victim)), deathReason);
    }

    private static void captureDeathBalance(
            ServerPlayerEntity victim,
            @Nullable ServerPlayerEntity killer,
            Identifier deathReason
    ) {
        if (isSaint(victim)) {
            DEATH_BALANCES.put(victim.getUuid(), PlayerShopComponent.KEY.get(victim).getBalance());
        }
    }

    /**
     * Passive Hellfire: any other player who kills the Saint burns for the seconds her coins buy
     * ({@link SaintRules#hellfireTicks}), whatever their faction; the Grand Witch is immune. No killer (environment,
     * herself) burns nobody. Like the Karma mark before it, this fires on every {@code KillPlayer.AFTER} of a Saint,
     * a death the Last Stand trait then intercepts included.
     * 被动业火：击杀圣徒的任何其他玩家，无论阵营，都承受由她的金币换算出的业火秒数（{@link SaintRules#hellfireTicks}）；
     * 大魔女免疫。没有凶手（环境、自杀）时无人受罚。与此前的业障标记一样，圣徒的每次 {@code KillPlayer.AFTER} 都会触发，
     * 包括随后被背水一战词条拦截的死亡。
     */
    private static void afterKill(
            ServerPlayerEntity victim,
            @Nullable ServerPlayerEntity killer,
            Identifier deathReason
    ) {
        Integer deathBalance = DEATH_BALANCES.remove(victim.getUuid());
        if (!isSaint(victim) || killer == null || killer.getUuid().equals(victim.getUuid())) {
            return;
        }
        // A shot the Dominator forces is the Dominator's kill: the pawn who was made to fire is shielded from its
        // other consequences (GameFunctionsForcedShotProtectionMixin) and does not burn either. KillPlayer.AFTER fires
        // synchronously inside the forced-shot scope, so the scope is still open here.
        // 支配者强迫开出的那一枪是支配者的击杀：被迫开枪者已被免除这一枪的其他后果（GameFunctionsForcedShotProtectionMixin），
        // 同样不承受业火。KillPlayer.AFTER 在强制射击作用域内同步触发，因此此处作用域仍然打开。
        if (DominatorForcedShotScope.isFiring(killer)) {
            return;
        }
        int balance = deathBalance != null ? deathBalance : PlayerShopComponent.KEY.get(victim).getBalance();
        if (SaintKarmaService.inflict(killer, SaintRules.hellfireTicks(balance))) {
            // Achievement record W12, only past the Hellfire immunity check.
            // 成就记录 W12，仅在通过业火免疫检查后写入。
            AchievementRecords.saintKarma(victim, killer);
        }
    }

    private static boolean isSaint(ServerPlayerEntity player) {
        return SaintRules.isSaint(GameWorldComponent.KEY.get(player.getServerWorld()).getRole(player));
    }
}
