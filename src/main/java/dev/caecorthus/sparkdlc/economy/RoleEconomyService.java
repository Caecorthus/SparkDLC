package dev.caecorthus.sparkdlc.economy;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.trait.api.TraitIds;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.event.CanSeeMoney;
import dev.doctor4t.wathe.api.event.TaskComplete;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Server-side economy hooks for the role kits.
 * 职业套件的服务端金币逻辑。
 */
public final class RoleEconomyService {
    private static boolean registered;

    private RoleEconomyService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        CanSeeMoney.EVENT.register(RoleEconomyService::canSeeMoney);
        TaskComplete.EVENT.register((player, taskType) -> onTaskComplete(player));
    }

    public static void assignForRole(ServerPlayerEntity player, Role role) {
        if (RoleEconomyRules.shouldInitializeGoodMoney(role)) {
            PlayerShopComponent.KEY.get(player).setBalance(RoleEconomyRules.INITIAL_GOOD_ROLE_MONEY);
        }
    }

    /**
     * Pays the role's own +50 even to an Impostor trait holder; the Impostor trait's +50 stacks on top (owner rule).
     * 即使是内鬼词条持有者也发放职业自带的 +50；内鬼词条的 +50 另行叠加（所有者规则）。
     */
    public static void onTaskComplete(ServerPlayerEntity player) {
        Role role = GameWorldComponent.KEY.get(player.getServerWorld()).getRole(player);
        if (RoleEconomyRules.earnsTaskMoney(role)) {
            PlayerShopComponent.KEY.get(player).addToBalance(RoleEconomyRules.TASK_MONEY_REWARD);
        }
    }

    public static boolean isMoneyVisible(PlayerEntity player) {
        Role role = GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
        return RoleEconomyRules.isGoodMoneyRole(role) || SparkTraitsApi.hasActiveTrait(player, TraitIds.IMPOSTOR);
    }

    static @Nullable CanSeeMoney.Result moneyVisibilityResult(@Nullable Role role) {
        return RoleEconomyRules.isGoodMoneyRole(role) ? CanSeeMoney.Result.ALLOW : null;
    }

    private static CanSeeMoney.Result canSeeMoney(PlayerEntity player) {
        if (player == null || !GameFunctions.isPlayerPlayingAndAlive(player)) {
            return null;
        }
        return isMoneyVisible(player) ? CanSeeMoney.Result.ALLOW : null;
    }
}
