package dev.caecorthus.sparkdlc.role.killer.blackraven.crow;

import dev.caecorthus.sparkdlc.role.witch.riftwalker.session.RiftExitSafety;
import dev.caecorthus.sparkdlc.role.witch.riftwalker.session.RiftExitSearch;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-only landing-spot search at the Homing Feather (plan §2.8), driven by the public {@link RiftExitSearch} ring
 * and judged by the Riftwalker's own exit check {@link RiftExitSafety#isSafe} (it kept a copy of that check while it
 * was package-private; the copy was identical with entity overlap never relaxed): the feather's own spot first, then
 * the Rift ring around it in the player's facing frame, each reachable from the feather by a block-free sweep. A spot
 * is safe on the player's STANDING box: finite, inside the world height and not below the Wathe play area's fall line,
 * inside the world border, every touched chunk already loaded (never loads one), no block collision (probed with no
 * entity context, so door exemptions cannot hide solids), a 1/16-block floor under the whole footprint, no fluid, and
 * no other collidable entity or living player in the box. No safe spot means the player stays at the body.
 * 仅服务端的归羽落点搜索（计划 §2.8），由公开的 {@link RiftExitSearch} 环形候选驱动，并由隙行者自己的出门检查
 * {@link RiftExitSafety#isSafe} 判定（该检查仍为包内可见时，这里保留过一份副本；副本与其完全相同，且从不放宽实体重叠）：
 * 先是归羽本身的位置，再按玩家朝向坐标系取其周围的 Rift 环形格，且每格都必须能从归羽处无碰撞扫掠到达。
 * 按玩家站立碰撞箱判断安全：坐标有限、位于世界高度内且不低于 Wathe 游戏区域的坠落线、位于世界边界内、涉及的区块均已加载（从不加载区块）、
 * 无方块碰撞（无实体上下文探测，门的豁免无法隐藏实体方块）、整个脚底下方 1/16 格有地面、没有流体、盒内没有其他可碰撞实体或存活玩家。
 * 找不到安全点时玩家留在本体处。
 */
final class BlackRavenCrowLandingSafety {
    private BlackRavenCrowLandingSafety() {
    }

    /** First safe feet position at the feather, or null. / 归羽处第一个安全的脚底位置，没有时为 null。 */
    static @Nullable Vec3d findSpot(ServerPlayerEntity player, ServerWorld world, Vec3d feather) {
        if (!BlackRavenCrowUseRules.isFinite(feather)) {
            return null;
        }
        List<Vec3d> candidates = new ArrayList<>();
        candidates.add(feather);
        candidates.addAll(RiftExitSearch.candidates(feather, player.getHorizontalFacing(), null));
        return RiftExitSearch.select(candidates,
                candidate -> RiftExitSafety.isSafe(player, world, candidate, candidate == feather ? null : feather,
                        false));
    }
}
