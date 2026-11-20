package dev.caecorthus.sparkdlc.item;

import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * The player-affect gate shared by the custom melee attack services (fire poker, ceremonial sword, Mighty Force): each
 * asks it before its attack path runs, and before it looks at what the attacker holds. A refused player-on-player
 * attack is therefore answered {@code ActionResult.SUCCESS} by whichever of those attack listeners runs first, whatever
 * is in the attacker's hand, which ends that attack.
 * 自定义近战攻击服务（火钳、仪礼剑、强力）共用的玩家影响门槛：各服务在自身攻击路径运行之前、且在查看攻击者手持物之前询问它。
 * 因此被拒绝的玩家对玩家攻击，无论攻击者手持什么，都由这些攻击监听器中最先运行的一个回答 {@code ActionResult.SUCCESS}，
 * 这次攻击就此结束。
 */
public final class CustomMeleeAffect {
    /** Action id the player-affect policies are asked with. / 询问玩家影响策略所用的行为 id。 */
    public static final Identifier ACTION = Identifier.of("sparkdlc", "custom_melee");

    private CustomMeleeAffect() {
    }

    /**
     * True when both sides are server players and the policies refuse the attack; anything else is not refused here.
     * 双方都是服务端玩家且策略拒绝这次攻击时为 true；其余情况不在此处拒绝。
     */
    public static boolean refuses(Entity attacker, Entity target) {
        return attacker instanceof ServerPlayerEntity actor
                && target instanceof ServerPlayerEntity playerTarget
                && !SparkFactionApi.allowsPlayerAction(actor, playerTarget, ACTION);
    }
}
