package dev.caecorthus.sparkdlc.bridge.strength;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Bridge to SparkTraits' public facade for the Bomber drones' interaction locks. The modules are fused in SparkDLC,
 * so both queries call {@link SparkTraitsApi} directly.
 * 炸弹客无人机交互锁使用的 SparkTraits 公开门面桥接。各模块已融合进 SparkDLC，因此两个查询都直接调用
 * {@link SparkTraitsApi}。
 */
public final class SparkTraitsDroneCompat {
    private SparkTraitsDroneCompat() {
    }

    /** True while Last Stand owns a pending death transition for {@code player} (all interactions are locked).
     *  背水一战正持有该玩家的待决死亡转换时为 true（此时锁定一切交互）。 */
    public static boolean isLastStandPending(PlayerEntity player) {
        return player != null && SparkTraitsApi.isLastStandPending(player);
    }

    /** True while SparkTraits blocks {@code player}'s role skills (Last Escape, silenced killer ...).
     *  SparkTraits 封锁该玩家职业技能时为 true（脱险、被沉默的杀手等）。 */
    public static boolean isRoleSkillBlocked(PlayerEntity player) {
        return player != null && SparkTraitsApi.isRoleSkillBlocked(player);
    }
}
