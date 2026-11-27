package dev.caecorthus.sparkdlc.entity;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;


/**
 * Destruction seam for placed reality stabilization anchors. Every firearm that fires a server-side ray (Wathe's
 * revolver, the Bandit revolver, the serial killer's psycho pistols, the Savior pistol, the double barrel and the
 * M1903A4) calls {@link #destroyAlongRay} with the shot's line, so a shot aimed at an anchor removes it exactly like a
 * melee hit does ({@link RealityAnchorEntity#damage}); grenades call {@link #destroyInRadius}. The anchor never blocks
 * the shot itself: the weapon's own player resolution still runs unchanged.
 * 现实稳定锚被摧毁的接缝。所有服务端射线的枪械（Wathe 左轮、强盗左轮、连环杀手疯魔双枪、救世主手枪、双管猎枪与
 * M1903A4）都用弹道调用 {@link #destroyAlongRay}，瞄准锚开一枪就与近战一击（{@link RealityAnchorEntity#damage}）同样移除它；
 * 手雷调用 {@link #destroyInRadius}。锚不会拦截子弹：武器自身的玩家判定照常执行。
 */
public final class RealityAnchorHits {
    /** Extra margin around the anchor's own box, so a shot that grazes the machine still counts. / 锚包围盒的额外余量。 */
    private static final double HIT_MARGIN = 0.15D;

    private RealityAnchorHits() {
    }

    /**
     * Removes the nearest anchor on the segment {@code from..to} in front of the first colliding block, if any.
     * {@code from} is normally the shooter's eye.
     * 移除 {@code from..to} 线段上、第一个阻挡方块之前最近的锚；{@code from} 通常是射手眼睛。
     */
    public static boolean destroyAlongRay(ServerWorld world, net.minecraft.entity.Entity source, Vec3d from, Vec3d to) {
        Vec3d direction = to.subtract(from);
        double length = direction.length();
        if (length < 1.0E-4D) {
            return false;
        }
        BlockHitResult block = world.raycast(new net.minecraft.world.RaycastContext(
                from, to, net.minecraft.world.RaycastContext.ShapeType.COLLIDER, net.minecraft.world.RaycastContext.FluidHandling.NONE, source));
        double limit = block.getType() == HitResult.Type.MISS ? length : from.distanceTo(block.getPos());

        RealityAnchorEntity nearest = null;
        double nearestDistance = limit;
        Box bounds = new Box(from, to).expand(1.0D);
        for (RealityAnchorEntity anchor : world.getEntitiesByClass(RealityAnchorEntity.class, bounds, entity -> true)) {
            double distance = anchor.getBoundingBox().expand(HIT_MARGIN)
                    .raycast(from, to)
                    .map(from::distanceTo)
                    .orElse(Double.POSITIVE_INFINITY);
            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearest = anchor;
            }
        }
        if (nearest == null) {
            return false;
        }
        nearest.discard();
        return true;
    }

    /**
     * Removes every anchor whose centre lies in {@code blast}, the Wathe grenade's own hit box: the grenade's bounding
     * box expanded by 3 blocks, exactly the region in which it kills players.
     * 移除中心落在 {@code blast} 内的所有锚。{@code blast} 就是 Wathe 手雷自身的命中盒：手雷包围盒向外扩 3 格，与它击杀玩家的
     * 范围完全一致。
     */
    public static int destroyInBox(ServerWorld world, Box blast) {
        int removed = 0;
        for (RealityAnchorEntity anchor : world.getEntitiesByClass(RealityAnchorEntity.class, blast, entity -> true)) {
            if (blast.contains(anchor.getBoundingBox().getCenter())) {
                anchor.discard();
                removed++;
            }
        }
        return removed;
    }
}
