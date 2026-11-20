package dev.caecorthus.sparkdlc.witch.block.props;

import net.minecraft.block.BlockState;
import net.minecraft.block.CarpetBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Damp, stained carpet. Footsteps on it are muffled: the block reports a wool-like sound group at a low pitch instead
 * of the wooden floor it covers, and a sneaking player scuffs the fibres as they move.
 * <p>
 * 潮湿、带污渍的地毯。踩上去的脚步声是闷的：方块报出低音高的羊毛质感音组，而不是它盖住的木地板；玩家潜行移动时
 * 还会蹭出纤维摩擦声。
 */
public class DampCarpetBlock extends CarpetBlock {
    /** Muffled footstep group: wool steps, quieter and lower than the planks underneath. / 闷住的脚步音组：羊毛脚步，比下面的木板更轻更低。 */
    public static final BlockSoundGroup DAMP_SOUNDS = new BlockSoundGroup(
            0.6F, 0.55F,
            SoundEvents.BLOCK_WOOL_BREAK,
            SoundEvents.BLOCK_WOOL_STEP,
            SoundEvents.BLOCK_WOOL_PLACE,
            SoundEvents.BLOCK_WOOL_HIT,
            SoundEvents.BLOCK_WOOL_FALL
    );

    public DampCarpetBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockSoundGroup getSoundGroup(BlockState state) {
        return DAMP_SOUNDS;
    }

    /**
     * Sneaking across the carpet makes a soft scuffing noise. Wathe has no step callback to hook, so this rides
     * {@code onSteppedOn}, which fires for every entity that walks over the block.
     * <p>
     * 潜行走过地毯会发出轻微的摩擦声。Wathe 没有可挂的踩踏回调，所以这里用 {@code onSteppedOn}——任何踩过该方块的
     * 实体都会触发它。
     */
    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (world.isClient || !(entity instanceof PlayerEntity player) || !player.isSneaking()) {
            return;
        }
        Random random = world.getRandom();
        if (random.nextInt(6) != 0) {
            return;
        }
        world.playSound(
                null, pos, SoundEvents.BLOCK_GRAVEL_STEP, SoundCategory.BLOCKS,
                0.18F + random.nextFloat() * 0.08F, 0.55F + random.nextFloat() * 0.1F
        );
    }
}
