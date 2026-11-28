package dev.caecorthus.sparkdlc.client.health;

import dev.caecorthus.sparkdlc.health.BodyPart;
import dev.caecorthus.sparkdlc.health.HealthModeComponent;
import dev.caecorthus.sparkdlc.health.hitbox.BodyHitboxRules;
import dev.caecorthus.sparkdlc.health.hitbox.BodyHitboxes;
import dev.caecorthus.sparkdlc.health.hitbox.BodyPose;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Debug view of the body-part hitboxes ({@link BodyHitboxRules}): wherever vanilla draws an entity's hitbox (F3+B),
 * the seven part boxes of a player are drawn over it as lines, one fixed colour per {@link BodyPart}; the part under
 * the camera's crosshair is white. Called only by {@code BodyHitboxDebugMixin} at the TAIL of vanilla
 * {@code EntityRenderDispatcher.renderHitbox}, so every vanilla gate (hitboxes on, entity not invisible, no reduced
 * debug info) and the mod's own suppression during an {@code EntityRedraw} already hold; the gates here only add a
 * confirmed SparkDLC server with the health mode on. Display only: it keeps no state, sends nothing and predicts
 * nothing; the boxes are those the server resolves a hit on, built from what this client draws (interpolated body yaw
 * and pitch), not from the server's pose history. No entity is drawn, only lines into vanilla's own buffer.
 * 部位命中箱（{@link BodyHitboxRules}）的调试视图：凡是原版绘制实体碰撞箱之处（F3+B），都会在其上以线框叠画玩家的七个部位
 * 箱体，每个 {@link BodyPart} 一种固定颜色；摄像机准星所指的部位为白色。只由 {@code BodyHitboxDebugMixin} 在原版
 * {@code EntityRenderDispatcher.renderHitbox} 的 TAIL 调用，因此原版的全部门槛（已开启碰撞箱显示、实体未隐身、未开启
 * 简化调试信息）以及本模组在 {@code EntityRedraw} 期间的屏蔽均已成立；此处的门槛只额外要求已确认的 SparkDLC 服务器且健康
 * 模式开启。仅用于展示：不保存状态、不发包、不做任何预测；箱体与服务端判定命中时所用的相同，但依据本客户端的绘制数据
 * （插值后的身体偏航角与俯仰角）构建，而非服务端的姿态历史。不绘制任何实体，只向原版自身的缓冲写入线段。
 */
public final class BodyHitboxDebugRenderer {
    /** Colour of the part under the crosshair. / 准星所指部位的颜色。 */
    private static final int HIGHLIGHT_COLOR = 0xFFFFFFFF;
    /** Length of the crosshair ray, in blocks. / 准星射线的长度，单位为方块。 */
    private static final double CROSSHAIR_RANGE = 64.0;

    private BodyHitboxDebugRenderer() {
    }

    /**
     * Draws the part boxes of {@code entity} when it is a player. {@code matrices} must be at the entity's
     * interpolated position, as it is inside vanilla {@code renderHitbox}; {@code vertices} is the
     * {@code RenderLayer.getLines()} buffer vanilla draws the hitbox into.
     * 当 {@code entity} 为玩家时绘制其部位箱体。{@code matrices} 的原点必须位于实体的插值位置，即原版 {@code renderHitbox}
     * 内部的状态；{@code vertices} 为原版绘制碰撞箱所用的 {@code RenderLayer.getLines()} 缓冲。
     */
    public static void render(MatrixStack matrices, VertexConsumer vertices, Entity entity, float tickDelta) {
        // Cheapest exits first. / 先做开销最小的判断。
        if (!(entity instanceof AbstractClientPlayerEntity player) || player.isSpectator()
                || !SparkDlcServerConnection.isConfirmedServer()
                || !HealthModeComponent.isEnabled(player.getWorld())) {
            return;
        }
        // The matrix origin is the entity's interpolated position, so the pose is built at the origin and the frame
        // is relative to the entity. / 矩阵原点即实体的插值位置，因此在原点构建姿态，坐标系相对于实体。
        BodyPose pose = BodyHitboxes.poseOf(player, Vec3d.ZERO,
                MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw),
                MathHelper.lerp(tickDelta, player.prevPitch, player.getPitch()));
        BodyHitboxRules.Frame frame = BodyHitboxRules.frame(pose);
        List<BodyHitboxRules.PartBox> boxes = BodyHitboxRules.boxes(pose);
        BodyPart aimed = aimedPart(player, tickDelta, frame, boxes);

        MatrixStack.Entry entry = matrices.peek();
        BodyHitboxRules.PartBox aimedBox = null;
        for (BodyHitboxRules.PartBox box : boxes) {
            if (box.part() == aimed) {
                aimedBox = box;
                continue;
            }
            drawOrientedBox(entry, vertices, frame, box.box(), color(box.part()));
        }
        // Neighbouring boxes share edges: the highlighted one is drawn last so its white wins on them.
        // 相邻箱体共用棱边：高亮的箱体最后绘制，使共用棱边显示为白色。
        if (aimedBox != null) {
            drawOrientedBox(entry, vertices, frame, aimedBox.box(), HIGHLIGHT_COLOR);
        }
    }

    /**
     * The part the crosshair of the camera drawing this pass is on, or null: the camera's forward ray, made relative
     * to the entity like the frame, has to really enter a box (a near miss highlights nothing). The camera's own
     * entity is never highlighted: in third person the ray starts behind it and would always cross it.
     * 绘制本次画面的摄像机准星所指的部位，没有则为 null：摄像机的前向射线与坐标系一样换算为相对于实体，且必须真正进入某个
     * 箱体（擦边不高亮）。摄像机自身的实体从不高亮：第三人称下射线从其身后出发，必然穿过它。
     */
    private static @Nullable BodyPart aimedPart(AbstractClientPlayerEntity player, float tickDelta,
                                                BodyHitboxRules.Frame frame, List<BodyHitboxRules.PartBox> boxes) {
        Camera camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera;
        if (camera == null || !camera.isReady() || camera.getFocusedEntity() == player) {
            return null;
        }
        Vec3d start = camera.getPos().subtract(player.getLerpedPos(tickDelta));
        Vec3d end = start.add(Vec3d.fromPolar(camera.getPitch(), camera.getYaw()).multiply(CROSSHAIR_RANGE));
        BodyHitboxRules.Hit hit = BodyHitboxRules.resolve(frame, boxes, start, end);
        return hit.direct() ? hit.part() : null;
    }

    /**
     * The twelve edges of a box that lives in {@code frame}. The box is oriented (a turned or lying body), so its
     * eight corners are moved out of the frame one by one instead of handing it to {@code WorldRenderer.drawBox},
     * which only draws axis-aligned boxes. Corner {@code i} takes the maximum on x, y, z where bit 0, 1, 2 of
     * {@code i} is set.
     * 位于 {@code frame} 中的箱体的十二条棱。箱体带有朝向（转身或躺下的身体），因此逐个把八个顶点变换出坐标系，而不是交给
     * 只能绘制轴对齐箱体的 {@code WorldRenderer.drawBox}。顶点 {@code i} 在其第 0、1、2 位为 1 时分别取 x、y、z 的最大值。
     */
    private static void drawOrientedBox(MatrixStack.Entry entry, VertexConsumer vertices, BodyHitboxRules.Frame frame,
                                        Box box, int argb) {
        Vec3d[] corners = new Vec3d[8];
        for (int i = 0; i < corners.length; i++) {
            corners[i] = frame.toWorld(
                    (i & 1) == 0 ? box.minX : box.maxX,
                    (i & 2) == 0 ? box.minY : box.maxY,
                    (i & 4) == 0 ? box.minZ : box.maxZ);
        }
        for (int i = 0; i < corners.length; i++) {
            // From each corner, one edge along every frame axis it is the minimum of: 4 + 4 + 4 edges.
            // 自每个顶点出发，沿它取最小值的每条坐标轴各画一条棱：4 + 4 + 4 条。
            if ((i & 1) == 0) {
                line(entry, vertices, corners[i], corners[i | 1], frame.left(), argb);
            }
            if ((i & 2) == 0) {
                line(entry, vertices, corners[i], corners[i | 2], frame.axis(), argb);
            }
            if ((i & 4) == 0) {
                line(entry, vertices, corners[i], corners[i | 4], frame.front(), argb);
            }
        }
    }

    /**
     * One segment in the lines layer: both vertices carry the unit direction of the segment as their normal, which
     * is what the line shader widens the segment along. / 线段图层中的一条线段：两个顶点都以线段的单位方向作为法线，线段
     * 着色器据此把线段展宽。
     */
    private static void line(MatrixStack.Entry entry, VertexConsumer vertices, Vec3d from, Vec3d to, Vec3d direction,
                             int argb) {
        float nx = (float) direction.x;
        float ny = (float) direction.y;
        float nz = (float) direction.z;
        vertices.vertex(entry, (float) from.x, (float) from.y, (float) from.z).color(argb).normal(entry, nx, ny, nz);
        vertices.vertex(entry, (float) to.x, (float) to.y, (float) to.z).color(argb).normal(entry, nx, ny, nz);
    }

    /**
     * Opaque ARGB of a part's box. The switch is exhaustive on purpose: a new body part must get a colour here before
     * the mod compiles. None of them is white, which is the highlight and vanilla's own box.
     * 部位箱体的不透明 ARGB。此 switch 有意写成穷尽形式：新增身体部位时必须先在此处给出颜色，模组才能编译。其中没有白色：
     * 白色留给高亮，也是原版自身碰撞箱的颜色。
     */
    private static int color(BodyPart part) {
        return switch (part) {
            case HEAD -> 0xFFFF4040;
            case CHEST -> 0xFFFF9A1F;
            case ABDOMEN -> 0xFFFFE640;
            case LEFT_ARM -> 0xFF40E060;
            case RIGHT_ARM -> 0xFF40D8FF;
            case LEFT_LEG -> 0xFF4A6CFF;
            case RIGHT_LEG -> 0xFFE050FF;
        };
    }
}
