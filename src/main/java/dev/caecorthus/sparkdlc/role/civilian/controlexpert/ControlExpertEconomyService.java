package dev.caecorthus.sparkdlc.role.civilian.controlexpert;

import dev.caecorthus.sparkdlc.skill.api.SparkRoleApi;
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
 * Control Expert task economy on the Judge model: 0 at round start, +50 per completed task, nothing for an
 * Impostor (the Impostor trait pays its holder's task income itself, so paying here would double it).
 * 控场专家沿用法官的任务经济：开局 0 金币，每完成一个任务 +50；内鬼不在此发放
 * （内鬼词条自行支付其持有者的任务收入，此处再发会重复）。
 */
public final class ControlExpertEconomyService {
    private static boolean registered;

    private ControlExpertEconomyService() {
    }

    static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        TaskComplete.EVENT.register((player, task) -> onTaskComplete(player));
        CanSeeMoney.EVENT.register(ControlExpertEconomyService::canSeeMoney);
    }

    /** Round-start balance, written once the final role is known. / 开局余额，在最终身份确定后写入。 */
    static void initialize(ServerPlayerEntity player) {
        PlayerShopComponent.KEY.get(player).setBalance(ControlExpertRules.INITIAL_MONEY);
    }

    static void onTaskComplete(ServerPlayerEntity player) {
        // Traits are part of this mod: always loaded. / 词条属于本模组：恒为已加载。
        if (canEarn(player) && receivesTaskMoney(true, SparkTraitsApi.hasActiveTrait(player, TraitIds.IMPOSTOR))) {
            PlayerShopComponent.KEY.get(player).addToBalance(ControlExpertRules.TASK_MONEY_REWARD);
        }
    }

    static boolean canEarn(@Nullable PlayerEntity player) {
        if (player == null) {
            return false;
        }
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
        return canEarn(game.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE,
                GameFunctions.isPlayerPlayingAndAlive(player), game.getRole(player), player.isSpectator(),
                player.isCreative(), SparkRoleApi.isWraithRestricted(player));
    }

    /** Same eligibility as the Judge's task income. / 与法官任务收入相同的资格判定。 */
    static boolean canEarn(boolean active, boolean playingAndAlive, @Nullable Role role, boolean spectator,
                           boolean creative, boolean wraithRestricted) {
        return active && playingAndAlive && ControlExpertRules.isControlExpert(role)
                && !spectator && !creative && !wraithRestricted;
    }

    /**
     * {@code traitsLoaded} false → pay, the branch for a jar without traits, unreachable since the fusion (the caller
     * always passes true); true → pay only on a confirmed non-Impostor (unknown fails closed, Judge semantics).
     * {@code traitsLoaded} 为 false → 发放，这是为没有词条的 jar 准备的分支，融合后不可达（调用方恒传入 true）；
     * 为 true → 仅在确认不是内鬼时发放（无法确认时不发放，与法官一致）。
     */
    static boolean receivesTaskMoney(boolean traitsLoaded, @Nullable Boolean impostor) {
        return !traitsLoaded || Boolean.FALSE.equals(impostor);
    }

    static @Nullable CanSeeMoney.Result moneyVisibilityResult(@Nullable Role role) {
        return ControlExpertRules.isControlExpert(role) ? CanSeeMoney.Result.ALLOW : null;
    }

    private static @Nullable CanSeeMoney.Result canSeeMoney(PlayerEntity player) {
        if (player == null || !GameFunctions.isPlayerPlayingAndAlive(player)) {
            return null;
        }
        return moneyVisibilityResult(GameWorldComponent.KEY.get(player.getWorld()).getRole(player));
    }
}
