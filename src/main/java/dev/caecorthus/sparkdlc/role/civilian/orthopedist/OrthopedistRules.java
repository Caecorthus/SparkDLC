package dev.caecorthus.sparkdlc.role.civilian.orthopedist;

import net.minecraft.util.Identifier;

/**
 * Pure Orthopedist tuning and server-validation decisions.
 * 骨科大夫的纯数值与服务端校验决策集中在这里。
 */
public final class OrthopedistRules {
    public static final Identifier ROLE_ID = Identifier.of("sparkdlc", "orthopedist");
    public static final int COLOR = 0x90B358;
    public static final int INITIAL_COOLDOWN_TICKS = 20 * 30;
    public static final int POST_USE_COOLDOWN_TICKS = 20 * 60;
    public static final double TARGET_RANGE = 3.0D;
    public static final double TARGET_RANGE_SQUARED = TARGET_RANGE * TARGET_RANGE;
    public static final int BONE_SETTING_TICKS = 20 * 20;
    public static final int SPEED_TICKS = 20 * 5;
    public static final double STAMINA_CONSUMPTION_MULTIPLIER = 0.75D;
    // Economy (owner 2026-11-28): no opening coins, the mod's standard task reward. The shop is in ExoskeletonRules.
    // 经济（所有者 2026-11-28）：开局没有金币，任务奖励为本模组的标准数额。商店数值见 ExoskeletonRules。
    public static final int INITIAL_MONEY = 0;
    public static final int TASK_REWARD = 50;

    private OrthopedistRules() {
    }

    /**
     * Pays only a non-Impostor: the Impostor trait pays its holder's task income itself, so the two never stack.
     * 只向非内鬼发放：内鬼词条自行支付持有者的任务收入，两者绝不叠加。
     */
    public static boolean receivesTaskMoney(boolean impostor) {
        return !impostor;
    }

    public static TargetAction targetAction(int fractureLayers, boolean boneSettingActive) {
        if (boneSettingActive) {
            return TargetAction.REJECT_ALREADY_ACTIVE;
        }
        return fractureLayers > 0 ? TargetAction.HEAL_FRACTURE : TargetAction.APPLY_BONE_SETTING;
    }

    public static boolean canAttempt(
            boolean isOrthopedist,
            boolean casterAlive,
            boolean feared,
            int cooldownTicks,
            boolean targetAlive,
            boolean hasLineOfSight,
            double squaredDistance
    ) {
        return isOrthopedist
                && casterAlive
                && !feared
                && cooldownTicks <= 0
                && targetAlive
                && hasLineOfSight
                && squaredDistance <= TARGET_RANGE_SQUARED;
    }

    public enum TargetAction {
        REJECT_ALREADY_ACTIVE,
        HEAL_FRACTURE,
        APPLY_BONE_SETTING
    }
}
