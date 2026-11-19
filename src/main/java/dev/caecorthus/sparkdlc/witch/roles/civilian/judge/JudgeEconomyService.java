package dev.caecorthus.sparkdlc.witch.roles.civilian.judge;

import dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/** Judge income has one owner; its police-network registration (which earns it a free SparkStrength tablet) must
 * never grant a second task reward.
 * 法官收入只有一个归属；其警察网络登记（因此获得 SparkStrength 免费平板）不得产生第二份任务收益。 */
public final class JudgeEconomyService {
    private static final Identifier IMPOSTOR = Identifier.of("sparktraits", "impostor");

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

    /** Pays only a non-Impostor (SparkTraits pays that trait's task income itself). SparkTraits is fused into
     * SparkDLC, so the facade call is direct.
     * 仅向非内鬼发放（内鬼词条的任务收入由 SparkTraits 自行支付）。SparkTraits 已融合进 SparkDLC，门面为直接调用。 */
    private static boolean receivesOrdinaryTaskMoney(PlayerEntity player) {
        return !SparkTraitsApi.hasActiveTrait(player, IMPOSTOR);
    }
}
