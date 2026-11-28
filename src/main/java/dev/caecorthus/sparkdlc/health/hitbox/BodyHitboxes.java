package dev.caecorthus.sparkdlc.health.hitbox;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * The body-part hitboxes of a player: reads a player into a {@link BodyPose} (both sides) and, on the server, tells
 * which {@link dev.caecorthus.sparkdlc.health.BodyPart} a hit landed on.
 *
 * <p>Contract of {@link #resolve}: call it AFTER a weapon's own targeting has established that {@code target} is
 * hit, with the ray of that hit. It never says "missed" and changes nothing: no health is touched, no kill is
 * prevented, and whether {@link dev.caecorthus.sparkdlc.health.HealthModeComponent} is on is the caller's question.
 * It names no weapon and no role; a weapon calls it.</p>
 *
 * 玩家的部位命中箱：把玩家读取为 {@link BodyPose}（两端通用），并在服务端给出命中落在哪个
 * {@link dev.caecorthus.sparkdlc.health.BodyPart}。
 *
 * <p>{@link #resolve} 的契约：在武器自身的瞄准逻辑已确认命中 {@code target} 之后调用，并传入该次命中的射线。它从不回答
 * “未命中”，也不改变任何状态：不动血量，不阻止击杀；{@link dev.caecorthus.sparkdlc.health.HealthModeComponent} 是否开启由
 * 调用方自行判断。它不涉及任何武器或职业；由武器来调用它。</p>
 */
public final class BodyHitboxes {
    private BodyHitboxes() {
    }

    /**
     * Server only. The part of {@code target} struck by the ray {@code start → end} (world space; a point attack
     * passes the same point twice and gets the nearest part). The ray is tested against the target's current pose and
     * the poses inside the rewind window of {@code shooter}, the window its hitscan targeting uses; without a shooter
     * (a projectile, a trap) only the ping-independent view delay is rewound. A part the ray really enters beats a
     * near miss, then the earliest entry wins.
     * 仅服务端。射线 {@code start → end}（世界坐标；点状攻击可把同一点传两次，得到最近的部位）击中 {@code target} 的部位。
     * 射线会与目标的当前姿态以及 {@code shooter} 回溯窗口内的姿态逐一检测，该窗口即其即时射线瞄准所用的窗口；没有射手
     * （投射物、陷阱）时只回溯与延迟无关的视图滞后。真正进入的部位优先于擦边，其次取最早进入者。
     */
    public static BodyPartHit resolve(@Nullable ServerPlayerEntity shooter, PlayerEntity target, Vec3d start,
                                      Vec3d end) {
        BodyHitboxRules.Hit best = null;
        for (BodyPose pose : BodyPoseHistory.recentPoses(shooter, target)) {
            BodyHitboxRules.Hit hit = BodyHitboxRules.resolve(pose, start, end);
            if (best == null || hit.beats(best)) {
                best = hit;
            }
        }
        // recentPoses always starts with the current pose. / recentPoses 总以当前姿态开头。
        return new BodyPartHit(best.part(), best.point(), best.direct());
    }

    /**
     * Both sides: the pose of {@code player} at its current position with the given body yaw. The server passes the
     * yaw it tracks ({@link BodyPoseHistory}); a client passes the yaw it draws.
     * 两端通用：{@code player} 在当前位置、采用给定身体偏航角时的姿态。服务端传入其推算的偏航角
     * （{@link BodyPoseHistory}）；客户端传入其绘制所用的偏航角。
     */
    public static BodyPose poseOf(PlayerEntity player, float bodyYaw) {
        return poseOf(player, player.getPos(), bodyYaw, player.getPitch());
    }

    /**
     * Both sides: the pose of {@code player} as if it stood at {@code position} with that body yaw and pitch, for a
     * caller that interpolates them itself (a renderer). Stance, proportions and scale are read from the player.
     * 两端通用：假设 {@code player} 位于 {@code position}、采用该身体偏航角与俯仰角时的姿态，供自行插值的调用方（渲染器）
     * 使用。姿势、比例与缩放读自玩家。
     */
    public static BodyPose poseOf(PlayerEntity player, Vec3d position, float bodyYaw, float pitch) {
        BodyStance stance = stanceOf(player);
        float yaw = bodyYaw;
        float bodyPitch = 0.0F;
        switch (stance) {
            case SUPINE -> {
                Direction bed = player.getSleepingDirection();
                if (bed != null) {
                    yaw = bed.asRotation();
                }
            }
            // On land a crawling body lies level; in water it follows the look. / 陆地上爬行的身体保持水平；水中则跟随视线。
            case PRONE -> bodyPitch = player.isTouchingWater() ? pitch : 0.0F;
            case GLIDING -> bodyPitch = pitch;
            default -> {
            }
        }
        return new BodyPose(stance, SparkTraitsApi.hasChildishBody(player), position, yaw, bodyPitch,
                player.getScale());
    }

    /**
     * How the body of {@code player} is drawn right now. A pig is a pig in every pose. A rider sits only on a seat:
     * a living carrier holds its passenger upright.
     * {@code player} 的身体此刻的绘制方式。猪在任何姿态下都是猪。只有坐在座位上的乘客才是坐姿：被生物携带的乘客保持直立。
     */
    static BodyStance stanceOf(PlayerEntity player) {
        if (SparkTraitsApi.hasPigBody(player)) {
            return BodyStance.ALL_FOURS;
        }
        EntityPose pose = player.getPose();
        if (pose == EntityPose.SLEEPING) {
            return BodyStance.SUPINE;
        }
        if (pose == EntityPose.SWIMMING) {
            return BodyStance.PRONE;
        }
        if (pose == EntityPose.FALL_FLYING || pose == EntityPose.SPIN_ATTACK) {
            return BodyStance.GLIDING;
        }
        if (player.hasVehicle() && !(player.getVehicle() instanceof LivingEntity)) {
            return BodyStance.SEATED;
        }
        return pose == EntityPose.CROUCHING ? BodyStance.CROUCHING : BodyStance.STANDING;
    }
}
