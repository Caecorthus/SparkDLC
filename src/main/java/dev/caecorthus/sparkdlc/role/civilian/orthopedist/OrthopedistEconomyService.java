package dev.caecorthus.sparkdlc.role.civilian.orthopedist;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.trait.api.TraitIds;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.event.CanSeeMoney;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * The Orthopedist's task-funded economy (owner 2026-11-28), on the Perfumer's pattern: the role sees its coins,
 * starts at {@link OrthopedistRules#INITIAL_MONEY} and earns {@link OrthopedistRules#TASK_REWARD} per finished task.
 * Everything here reads the raw role ({@code getRole}); a Black Raven acting as the Orthopedist is paid and shown
 * money by its disguise adapter instead.
 * 骨科大夫依靠任务获得金币的经济（所有者 2026-11-28），沿用调香师的写法：该职业能看到金币，开局为
 * {@link OrthopedistRules#INITIAL_MONEY}，每完成一个任务获得 {@link OrthopedistRules#TASK_REWARD}。这里全部读取真实身份
 * （{@code getRole}）；扮演骨科大夫的黑羽鸦改由其伪装适配器发钱并显示金币。
 */
public final class OrthopedistEconomyService {
    private static boolean registered;

    private OrthopedistEconomyService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        CanSeeMoney.EVENT.register(OrthopedistEconomyService::canSeeMoney);
    }

    public static void assignForRole(ServerPlayerEntity player, Role role) {
        if (isOrthopedist(role)) {
            PlayerShopComponent.KEY.get(player).setBalance(OrthopedistRules.INITIAL_MONEY);
        }
    }

    /**
     * Called from the {@code TaskComplete} listener in {@code RoleEvents}, next to the Perfumer's. {@code addToBalance},
     * so income observers (the income record, the Snowball trait) see ordinary income. Never paid to a holder of the
     * Impostor trait, whose task income that trait pays itself (the rule of the mod's newer role economies).
     * 由 {@code RoleEvents} 中紧邻调香师的 {@code TaskComplete} 监听器调用。使用 {@code addToBalance}，使收入观察者（收入
     * 记录、滚雪球词条）视其为普通收入。从不发给内鬼词条持有者，其任务收入由该词条自行支付（本模组较新职业经济的规则）。
     */
    public static void onTaskComplete(ServerPlayerEntity player) {
        Role role = GameWorldComponent.KEY.get(player.getServerWorld()).getRole(player);
        if (isOrthopedist(role)
                && OrthopedistRules.receivesTaskMoney(SparkTraitsApi.hasActiveTrait(player, TraitIds.IMPOSTOR))) {
            PlayerShopComponent.KEY.get(player).addToBalance(OrthopedistRules.TASK_REWARD);
        }
    }

    static CanSeeMoney.Result moneyVisibilityResult(@Nullable Role role) {
        return isOrthopedist(role) ? CanSeeMoney.Result.ALLOW : null;
    }

    private static CanSeeMoney.Result canSeeMoney(PlayerEntity player) {
        if (player == null || !GameFunctions.isPlayerPlayingAndAlive(player)) {
            return null;
        }
        return moneyVisibilityResult(GameWorldComponent.KEY.get(player.getWorld()).getRole(player));
    }

    private static boolean isOrthopedist(@Nullable Role role) {
        return role != null && OrthopedistRules.ROLE_ID.equals(role.identifier());
    }
}
