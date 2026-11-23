package dev.caecorthus.sparkdlc.role.civilian.judge;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.trait.api.TraitIds;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

/** Judge income has one owner; its police-network registration (which earns it a free tablet) must
 * never grant a second task reward.
 * 法官收入只有一个归属；其警察网络登记（因此获得免费平板）不得产生第二份任务收益。 */
public final class JudgeEconomyService {
    private JudgeEconomyService() {
    }

    public static void initialize(ServerPlayerEntity player) {
        PlayerShopComponent.KEY.get(player).setBalance(JudgeRules.INITIAL_MONEY);
    }

    public static void onTaskComplete(ServerPlayerEntity player) {
        if (JudgeRuntime.canJudge(player) && receivesOrdinaryTaskMoney(player)) {
            PlayerShopComponent.KEY.get(player).addToBalance(JudgeRules.TASK_MONEY_REWARD);
        }
    }

    /** Pays only a non-Impostor (the Impostor trait pays its own task income), asked directly through
     * {@code SparkTraitsApi}.
     * 仅向非内鬼发放（内鬼词条的任务收入由该词条自行支付），经 {@code SparkTraitsApi} 直接查询。 */
    private static boolean receivesOrdinaryTaskMoney(PlayerEntity player) {
        return !SparkTraitsApi.hasActiveTrait(player, TraitIds.IMPOSTOR);
    }
}
