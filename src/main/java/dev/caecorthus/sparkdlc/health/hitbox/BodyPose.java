package dev.caecorthus.sparkdlc.health.hitbox;

import net.minecraft.util.math.Vec3d;

/**
 * Everything the part hitboxes of one player at one moment are built from: a snapshot, never a live view. Pure data,
 * shared by both sides; {@link BodyHitboxRules} turns it into a frame and seven boxes.
 *
 * <p>{@code position} is the entity position (bottom centre of its vanilla box). {@code bodyYaw} is the yaw of the
 * BODY in degrees, not of the head; for {@link BodyStance#SUPINE} it is the yaw the bed's head points to.
 * {@code pitch} only matters to {@link BodyStance#PRONE} in water and {@link BodyStance#GLIDING}. {@code scale} is
 * the entity scale (1 for a normal player). {@code child} selects the big-headed proportions a Childish player is
 * drawn with.</p>
 *
 * 构建某一时刻某名玩家部位命中箱所需的全部数据：是快照，而非实时视图。纯数据，两端共用；{@link BodyHitboxRules} 把它转换为
 * 坐标系与七个箱体。
 *
 * <p>{@code position} 为实体位置（原版碰撞箱底面中心）。{@code bodyYaw} 是身体（而非头部）的偏航角，单位为度；
 * {@link BodyStance#SUPINE} 时为床头所指方向的偏航角。{@code pitch} 只对水中的 {@link BodyStance#PRONE} 与
 * {@link BodyStance#GLIDING} 有意义。{@code scale} 为实体缩放（普通玩家为 1）。{@code child} 表示采用幼稚玩家绘制时的
 * 大头比例。</p>
 */
public record BodyPose(BodyStance stance, boolean child, Vec3d position, float bodyYaw, float pitch, double scale) {
}
