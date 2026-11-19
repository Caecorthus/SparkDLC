package dev.caecorthus.sparkdlc.bridge.strength;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Bridge to SparkTraits' public facade for the Pathogen's T-Virus: SparkTraits' fake corpses (Depression,
 * Last Stand pending) and deaths Last Stand still owns must never be revived. The modules are fused in SparkDLC, so
 * both queries call {@link SparkTraitsApi} directly; the revive also keeps its own checks: the body owner must be in
 * Wathe's dead set and spectating.
 * The Coroner also asks {@link #isFakeDeathBody} so such corpses pay no proximity coins and cannot be bagged.
 * 病原体 T病毒使用的 SparkTraits 公开门面桥接：SparkTraits 的假尸体（抑郁、背水一战待决）以及仍由背水一战接管的
 * 死亡绝不能被复活。各模块已融合进 SparkDLC，因此两个查询都直接调用 {@link SparkTraitsApi}；复活仍保留自身检查：
 * 尸体主人必须在 Wathe 死亡名单中且处于旁观。验尸官同样通过 {@link #isFakeDeathBody} 判定，这类尸体不给靠近金币、
 * 也不能装袋。
 */
public final class SparkTraitsReviveCompat {
    private SparkTraitsReviveCompat() {
    }

    public static boolean isFakeDeathBody(Entity body) {
        return body != null && SparkTraitsApi.isFakeDeathBody(body);
    }

    public static boolean isLastStandDeathIntercepted(PlayerEntity player) {
        return player != null && SparkTraitsApi.isLastStandDeathIntercepted(player);
    }
}
