package dev.caecorthus.sparkdlc.gametest;

import dev.caecorthus.sparkdlc.SparkDlcEntities;
import dev.caecorthus.sparkdlc.entity.RealityAnchorEntity;
import dev.caecorthus.sparkdlc.entity.RealityAnchorHits;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * The placed reality stabilization anchor on a real server: its light block is its own and never costs the map a
 * block, a shot or a blast breaks only what it really reaches, a punch needs a clear line, and an anchor of a match
 * that is not running leaves by itself.
 * 在真实服务器上检查已放置的现实稳定锚：光源方块归它自己所有且绝不让地图损失方块；射击与爆炸只打掉真正够得到的锚；一拳需要
 * 畅通的视线；所属对局不在进行中的锚会自行消失。
 */
public final class RealityAnchorGameTest implements FabricGameTest {
    private static final BlockState LIGHT = Blocks.LIGHT.getDefaultState();

    /**
     * The light goes into air only (one block up when the anchor stands in a carpet or in a map's own light block),
     * appears and disappears without disturbing an unsupported block next to it, and is taken away by every kind of
     * removal, {@code /kill} included, while the map's own light block stays.
     * 光源只放进空气（锚立在地毯或地图自己的光源方块里时放在上方一格），出现与消失都不会惊动旁边无支撑的方块；任何方式的移除
     * （包括 {@code /kill}）都会收回它，而地图自己的光源方块保持不变。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void theLightBlockBelongsToTheAnchor(TestContext context) {
        ServerWorld world = context.getWorld();
        BlockPos inAir = new BlockPos(1, 1, 1);
        BlockPos floatingTorch = inAir.up();
        // A torch whose support is taken away without updates, as a map maker's floating decoration.
        // 在不触发更新的情况下抽走支撑的火把，相当于地图作者的悬空装饰。
        context.setBlockState(inAir, Blocks.STONE);
        context.setBlockState(floatingTorch, Blocks.TORCH);
        world.setBlockState(context.getAbsolutePos(inAir), Blocks.AIR.getDefaultState(),
                Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
        BlockPos mapLight = new BlockPos(3, 1, 3);
        context.setBlockState(mapLight, LIGHT);
        BlockPos carpet = new BlockPos(5, 1, 5);
        context.setBlockState(carpet.down(), Blocks.STONE);
        context.setBlockState(carpet, Blocks.RED_CARPET);

        RealityAnchorEntity plain = spawn(context, new Vec3d(1.5, 1.0, 1.5), null);
        RealityAnchorEntity onMapLight = spawn(context, new Vec3d(3.5, 1.0, 3.5), null);
        RealityAnchorEntity onCarpet = spawn(context, new Vec3d(5.5, 1.0625, 5.5), null);

        context.runAtTick(3, () -> {
            require(context, inAir, Blocks.LIGHT, "an anchor in air lights its own block");
            require(context, floatingTorch, Blocks.TORCH, "placing the light must not update its neighbours");
            require(context, mapLight.up(), Blocks.LIGHT, "an anchor in a map light block lights the block above");
            require(context, carpet.up(), Blocks.LIGHT, "an anchor in a carpet lights the block above");
            if (!plain.destroy()) {
                throw new GameTestException("destroy() did not remove a live anchor");
            }
            onMapLight.kill();
            onCarpet.discard();
            require(context, inAir, Blocks.AIR, "a destroyed anchor takes its light along");
            require(context, floatingTorch, Blocks.TORCH, "removing the light must not update its neighbours");
            require(context, mapLight.up(), Blocks.AIR, "a killed anchor takes its light along");
            require(context, mapLight, Blocks.LIGHT, "the map's own light block is never removed");
            require(context, carpet.up(), Blocks.AIR, "a discarded anchor takes its light along");
            require(context, carpet, Blocks.RED_CARPET, "the block the anchor stood in is untouched");
        });
        context.runAtTick(30, () -> {
            require(context, inAir, Blocks.AIR, "no light comes back after the anchor is gone");
            context.complete();
        });
    }

    /**
     * A ray breaks the nearest anchor in front of the first block and nothing behind it; a blast breaks an anchor it
     * can see and spares one behind a wall; a punch needs a clear line to the anchor.
     * 射线打掉第一个方块之前最近的锚，不影响其后的任何东西；爆炸打掉它看得见的锚，放过墙后的锚；一拳需要到锚的畅通视线。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void aHitBreaksOnlyWhatItReaches(TestContext context) {
        ServerWorld world = context.getWorld();
        for (int y = 1; y <= 3; y++) {
            for (int z = 0; z <= 3; z++) {
                context.setBlockState(new BlockPos(4, y, z), Blocks.STONE);
            }
        }
        RealityAnchorEntity near = spawn(context, new Vec3d(2.5, 1.0, 1.5), null);
        RealityAnchorEntity far = spawn(context, new Vec3d(3.5, 1.0, 1.5), null);
        RealityAnchorEntity behindWall = spawn(context, new Vec3d(5.5, 1.0, 1.5), null);
        context.runAtTick(3, () -> {
            Vec3d from = context.getAbsolute(new Vec3d(0.5, 1.25, 1.5));
            Vec3d to = context.getAbsolute(new Vec3d(7.5, 1.25, 1.5));
            if (!RealityAnchorHits.destroyAlongRay(world, null, from, to) || !near.isRemoved() || far.isRemoved()) {
                throw new GameTestException("the first shot must break the nearest anchor and only that one");
            }
            if (!RealityAnchorHits.destroyAlongRay(world, null, from, to) || !far.isRemoved()) {
                throw new GameTestException("the second shot must break the next anchor in front of the wall");
            }
            if (RealityAnchorHits.destroyAlongRay(world, null, from, to) || behindWall.isRemoved()) {
                throw new GameTestException("a shot must not break an anchor behind a wall");
            }
            FakePlayer puncher = FakePlayer.get(world);
            Vec3d outside = context.getAbsolute(new Vec3d(2.5, 1.0, 1.5));
            puncher.setPosition(outside.x, outside.y, outside.z);
            if (RealityAnchorHits.onMelee(puncher, behindWall) || behindWall.isRemoved()) {
                throw new GameTestException("a punch through a wall must not break an anchor");
            }
            if (RealityAnchorHits.onBlast(world, context.getAbsolute(new Vec3d(3.5, 1.5, 1.5)), 3.0, null) != 0
                    || behindWall.isRemoved()) {
                throw new GameTestException("a blast must not break an anchor behind a wall");
            }
            Vec3d inside = context.getAbsolute(new Vec3d(6.5, 1.0, 1.5));
            puncher.setPosition(inside.x, inside.y, inside.z);
            RealityAnchorEntity second = spawn(context, new Vec3d(6.5, 1.0, 2.5), null);
            if (!RealityAnchorHits.onMelee(puncher, behindWall) || !behindWall.isRemoved()) {
                throw new GameTestException("a punch with a clear line must break the anchor");
            }
            if (RealityAnchorHits.onBlast(world, context.getAbsolute(new Vec3d(6.5, 1.5, 1.5)), 3.0, null) != 1
                    || !second.isRemoved()) {
                throw new GameTestException("a blast must break an anchor it can see");
            }
            context.complete();
        });
    }

    /**
     * An anchor that carries the id of a match that is not the running one (here: no match runs) removes itself on
     * its first tick and leaves no light; one placed outside a match stays.
     * 带有非当前对局 id 的锚（这里没有对局在进行）会在第一刻自行移除且不留下光源；对局外放下的锚保留。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void anAnchorLeavesWithItsMatch(TestContext context) {
        if (RealityAnchorEntity.currentMatchId() != null) {
            throw new GameTestException("the game test server must not be running a match");
        }
        RealityAnchorEntity ofAnEndedMatch = spawn(context, new Vec3d(1.5, 1.0, 1.5), UUID.randomUUID());
        RealityAnchorEntity decoration = spawn(context, new Vec3d(4.5, 1.0, 4.5), null);
        context.runAtTick(25, () -> {
            if (!ofAnEndedMatch.isRemoved()) {
                throw new GameTestException("an anchor of a match that is over must remove itself");
            }
            require(context, new BlockPos(1, 1, 1), Blocks.AIR, "an anchor of an ended match leaves no light");
            if (decoration.isRemoved()) {
                throw new GameTestException("an anchor placed outside a match must stay");
            }
            require(context, new BlockPos(4, 1, 4), Blocks.LIGHT, "an anchor placed outside a match keeps its light");
            decoration.discard();
            context.complete();
        });
    }

    private static RealityAnchorEntity spawn(TestContext context, Vec3d relative, @Nullable UUID matchId) {
        ServerWorld world = context.getWorld();
        RealityAnchorEntity anchor = SparkDlcEntities.realityAnchor().create(world);
        if (anchor == null) {
            throw new GameTestException("the reality anchor entity type could not be created");
        }
        Vec3d pos = context.getAbsolute(relative);
        anchor.setPosition(pos.x, pos.y, pos.z);
        anchor.bindToMatch(matchId);
        world.spawnEntity(anchor);
        return anchor;
    }

    private static void require(TestContext context, BlockPos pos, Block expected, String rule) {
        BlockState state = context.getBlockState(pos);
        if (!state.isOf(expected)) {
            throw new GameTestException(rule + ": expected " + expected + " at " + pos + ", found " + state);
        }
    }
}
