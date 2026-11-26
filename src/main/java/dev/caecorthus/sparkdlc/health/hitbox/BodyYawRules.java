package dev.caecorthus.sparkdlc.health.hitbox;

import net.minecraft.util.math.MathHelper;

/**
 * The body yaw a CLIENT draws for another player, recomputed on the server. Vanilla derives it in
 * {@code LivingEntity#tick} from the movement of the last tick, and the server's own {@code bodyYaw} of a player is
 * useless for that: a player's tick runs right after the network handler has set the previous position to the
 * current one, so the server always sees a standing player and its body only trails the head. This restates the
 * vanilla step (same constants) so that it can be fed the real per-tick movement. Pure arithmetic.
 * 客户端为其他玩家绘制的身体偏航角，在服务端重新计算。原版在 {@code LivingEntity#tick} 中依据上一刻的位移推导它，而服务端
 * 自身的玩家 {@code bodyYaw} 对此无用：玩家的 tick 紧跟在网络处理器把“上一位置”设为当前位置之后执行，服务端看到的永远是
 * 静止的玩家，其身体只会跟随头部。这里复述原版的这一步（常量相同），以便代入真实的每刻位移。纯算术。
 */
public final class BodyYawRules {
    /** Squared horizontal movement per tick above which the body turns towards the movement. / 每刻水平位移的平方超过该值时，身体转向移动方向。 */
    static final float MOVING_SQUARED = 0.0025000002F;
    /** Share of the remaining turn made each tick. / 每刻完成剩余转动的比例。 */
    static final float TURN_SHARE = 0.3F;
    /** The head never turns farther than this from the body. / 头部相对身体的最大偏转。 */
    static final float MAX_HEAD_TURN = 50.0F;

    private BodyYawRules() {
    }

    /**
     * The body yaw after one tick. {@code dx}, {@code dz}: the movement of that tick; {@code yaw}: where the player
     * looks; {@code swinging}: an arm swing in progress, which turns the body to the look direction. Walking
     * backwards keeps the body facing the other way. Degrees, not wrapped.
     * 一刻之后的身体偏航角。{@code dx}、{@code dz}：这一刻的位移；{@code yaw}：玩家的视线方向；{@code swinging}：正在挥臂，
     * 此时身体转向视线方向。后退时身体保持朝向相反方向。单位为度，不做回绕。
     */
    public static float next(float bodyYaw, float yaw, double dx, double dz, boolean swinging) {
        float target = bodyYaw;
        if ((float) (dx * dx + dz * dz) > MOVING_SQUARED) {
            float moving = (float) MathHelper.atan2(dz, dx) * MathHelper.DEGREES_PER_RADIAN - 90.0F;
            float apart = MathHelper.abs(MathHelper.wrapDegrees(yaw) - moving);
            target = 95.0F < apart && apart < 265.0F ? moving - 180.0F : moving;
        }
        if (swinging) {
            target = yaw;
        }
        float body = bodyYaw + MathHelper.wrapDegrees(target - bodyYaw) * TURN_SHARE;
        float head = MathHelper.wrapDegrees(yaw - body);
        if (Math.abs(head) > MAX_HEAD_TURN) {
            body += head - MathHelper.sign(head) * MAX_HEAD_TURN;
        }
        return body;
    }
}
