package dev.caecorthus.sparkdlc.block;

import com.mojang.serialization.MapCodec;
import dev.caecorthus.sparkdlc.SparkDlcBlockEntities;
import dev.doctor4t.wathe.block.NeonPillarBlock;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A neon pillar that can blink. It IS Wathe's {@link NeonPillarBlock} with Wathe's pillar settings, so a right-click
 * toggles the whole column, its light follows {@code lit} and {@code active} like Wathe's, and Wathe's blackout
 * (which darkens every block with both properties in the play area) takes it too. A sneak-right-click with an empty
 * hand by a creative-mode operator opens the blink settings (see {@link ShiningNeonPillarService}); for anyone else
 * a sneak-right-click passes, as on Wathe's pillar. Unlike Wathe's pillar it is placed lit.
 * <p>
 * 可以闪烁的霓虹灯柱。它就是 Wathe 的 {@link NeonPillarBlock}，沿用 Wathe 灯柱的方块设置：右键切换整列，亮度与 Wathe 一样
 * 跟随 {@code lit} 和 {@code active}，Wathe 的停电（使游玩区域内同时具有这两个属性的方块变暗）也会作用于它。创造模式的管理员
 * 空手潜行右键会打开闪烁设置（见 {@link ShiningNeonPillarService}）；其他人潜行右键则与 Wathe 灯柱一样放行。与 Wathe 灯柱不同，
 * 它放置时是亮的。
 */
public class ShiningNeonPillarBlock extends NeonPillarBlock implements BlockEntityProvider {
    public static final MapCodec<ShiningNeonPillarBlock> CODEC = createCodec(ShiningNeonPillarBlock::new);

    public ShiningNeonPillarBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(LIT, true));
    }

    @Override
    public MapCodec<ShiningNeonPillarBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player.shouldCancelInteraction() && ShiningNeonPillarService.mayConfigure(player)) {
            if (player instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
                ShiningNeonPillarService.openSettings(serverPlayer, serverWorld, pos);
            }
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShiningNeonPillarBlockEntity(pos, state);
    }

    /** Server side only: the client never flips a lamp, it receives the block updates. / 仅服务端：客户端从不翻转灯，只接收方块更新。 */
    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient || type != SparkDlcBlockEntities.shiningNeonPillar()) {
            return null;
        }
        return (tickWorld, pos, tickState, blockEntity) ->
                ShiningNeonPillarBlockEntity.serverTick(tickWorld, pos, tickState, (ShiningNeonPillarBlockEntity) blockEntity);
    }
}
