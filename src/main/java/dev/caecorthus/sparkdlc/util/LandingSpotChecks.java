package dev.caecorthus.sparkdlc.util;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

/**
 * The two checks every server-side landing search shares word for word: the Glimmerfish door exit, the Witches'
 * Sabbath landing, the Rift gate exit (also used by the Black Raven's Homing Feather) and the crushed Swapper's body
 * cell. Each search composes its own safety rule from them, in its own order; the rules differ on purpose (which box,
 * which play-area bound, fluid, doors, sweep), so only these identical parts are shared.
 * 每个服务端落点搜索逐字相同的两项检查：灵光鱼的出门、魔女集会的落点、裂隙门的出门（黑羽鸦的归羽也使用）以及被夹死的交换者
 * 的尸体格。各搜索按自己的顺序用它们组合出自己的安全规则；这些规则有意不同（用哪个碰撞箱、游戏区域取哪条边界、流体、门、
 * 扫掠），因此只共享这些完全相同的部分。
 */
public final class LandingSpotChecks {
    private LandingSpotChecks() {
    }

    /**
     * Pure bounds rule: finite, inside the world height and, when known, wholly inside Wathe's play area (below its
     * {@code minY} Wathe kills the player as fallen out of the train). The Rift gate exit uses a looser rule that only
     * keeps the fall line ({@code RiftExitSafety.insideBounds}).
     * 纯边界规则：坐标有限、位于世界高度内，并在已知时完全位于 Wathe 游戏区域内（低于其 {@code minY} 会被 Wathe 判为掉出列车
     * 而死亡）。裂隙门出门使用只保留坠落线的较宽规则（{@code RiftExitSafety.insideBounds}）。
     */
    public static boolean insideWorldAndPlayArea(Box body, int bottom, int top, @Nullable Box playArea) {
        return Double.isFinite(body.minX) && Double.isFinite(body.maxX)
                && Double.isFinite(body.minY) && Double.isFinite(body.maxY)
                && Double.isFinite(body.minZ) && Double.isFinite(body.maxZ)
                && body.minY >= bottom && body.maxY <= top
                && (playArea == null || body.minX >= playArea.minX && body.maxX <= playArea.maxX
                && body.minY >= playArea.minY && body.maxY <= playArea.maxY
                && body.minZ >= playArea.minZ && body.maxZ <= playArea.maxZ);
    }

    /**
     * Every chunk the box touches is already loaded; never loads one (unloaded chunks read as empty to collision
     * queries, so a spot over them would look free).
     * 碰撞箱涉及的每个区块都已加载；从不加载区块（未加载区块在碰撞查询中视为空，其上的落点会被误判为空闲）。
     */
    public static boolean chunksLoaded(ServerWorld world, Box box) {
        for (int x = MathHelper.floor(box.minX) >> 4; x <= MathHelper.floor(box.maxX) >> 4; x++) {
            for (int z = MathHelper.floor(box.minZ) >> 4; z <= MathHelper.floor(box.maxZ) >> 4; z++) {
                if (!world.getChunkManager().isChunkLoaded(x, z)) {
                    return false;
                }
            }
        }
        return true;
    }
}
