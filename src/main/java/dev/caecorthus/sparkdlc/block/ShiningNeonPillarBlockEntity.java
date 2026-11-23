package dev.caecorthus.sparkdlc.block;

import dev.caecorthus.sparkdlc.SparkDlcBlockEntities;
import dev.doctor4t.wathe.cca.WorldBlackoutComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Holds the blink settings of a shining neon pillar column. Only one block of a column carries settings, the column's
 * driver (its lowest block along the axis when the settings were applied); the others keep an empty block entity. The settings are saved with the
 * chunk, so a column keeps blinking after a restart, in every dimension, and in a copy made by the lobby map reset
 * (which copies block entity data). When the next flip is due is not saved: after a load the driver starts a fresh
 * phase.
 * <p>
 * 保存一根闪烁霓虹灯柱的闪烁设置。一根灯柱只有一个方块带设置，即灯柱的驱动方块（应用设置时沿轴向最低的那个方块）；其余方块的方块实体
 * 为空。设置随区块保存，因此重启后、在任何维度中、以及在大厅地图重置复制出的副本里（它会复制方块实体数据），灯柱都会继续闪烁。
 * 下一次翻转的时刻不保存：载入后驱动方块从一个新的相位开始。
 */
public class ShiningNeonPillarBlockEntity extends BlockEntity {
    private static final String ON_TICKS_KEY = "blink_on_ticks";
    private static final String OFF_TICKS_KEY = "blink_off_ticks";
    private static final String RANDOM_KEY = "blink_random";

    @Nullable
    private NeonBlinkSettings blink;
    /** World time of the next flip, or -1 when a fresh phase has to be drawn. / 下一次翻转的世界时间；为 -1 时需重新取一段相位。 */
    private long nextFlipTime = -1L;

    public ShiningNeonPillarBlockEntity(BlockPos pos, BlockState state) {
        super(SparkDlcBlockEntities.shiningNeonPillar(), pos, state);
    }

    /** The blink settings when this block drives its column, else {@code null}. / 本方块驱动其灯柱时的闪烁设置，否则为 {@code null}。 */
    @Nullable
    public NeonBlinkSettings blink() {
        return blink;
    }

    void setBlink(@Nullable NeonBlinkSettings blink) {
        nextFlipTime = -1L;
        if (!Objects.equals(this.blink, blink)) {
            this.blink = blink;
            markDirty();
        }
    }

    /**
     * Server tick. A block without settings returns at once, so a map full of pillars costs one field read per
     * block. While the column is inactive or Wathe's blackout runs, the driver only forgets its phase: a blackout, a
     * lights sabotage or a Saboteur outage records every lamp and restores each one on its own timer, and a flip made
     * before the last restore would be overwritten and leave the column mixed. A fresh phase first makes the whole
     * column show the driver's state again (after a load, a map reset copy or such a restore), then waits its length.
     * <p>
     * 服务端 tick。没有设置的方块立即返回，因此满地图的灯柱每个方块只多读一个字段。灯柱处于非激活状态或 Wathe 停电期间，驱动方块
     * 只丢弃当前相位：停电、灯光破坏或破坏者的熄灯都会记录每盏灯并按各自的计时恢复，在最后一盏恢复之前做出的翻转会被覆盖，使灯柱
     * 亮灭不一。新相位开始时先让整根灯柱重新显示驱动方块的状态（载入后、地图重置复制后或上述恢复之后），再等待该相位的时长。
     */
    static void serverTick(World world, BlockPos pos, BlockState state, ShiningNeonPillarBlockEntity blockEntity) {
        NeonBlinkSettings settings = blockEntity.blink;
        if (settings == null) {
            return;
        }
        if (!state.get(ShiningNeonPillarBlock.ACTIVE) || WorldBlackoutComponent.KEY.get(world).isBlackoutActive()) {
            blockEntity.nextFlipTime = -1L;
            return;
        }
        long now = world.getTime();
        boolean lit = state.get(ShiningNeonPillarBlock.LIT);
        if (blockEntity.nextFlipTime < 0L) {
            ShiningNeonPillarService.showOnColumn(world, pos, state, blockEntity, lit);
            blockEntity.nextFlipTime = now + settings.phaseTicks(lit, world.getRandom());
            return;
        }
        if (now < blockEntity.nextFlipTime) {
            return;
        }
        ShiningNeonPillarService.flipColumn(world, pos, state, blockEntity);
        blockEntity.nextFlipTime = now + settings.phaseTicks(!lit, world.getRandom());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        nextFlipTime = -1L;
        blink = nbt.contains(ON_TICKS_KEY)
                ? new NeonBlinkSettings(nbt.getInt(ON_TICKS_KEY), nbt.getInt(OFF_TICKS_KEY), nbt.getBoolean(RANDOM_KEY))
                : null;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        if (blink != null) {
            nbt.putInt(ON_TICKS_KEY, blink.onTicks());
            nbt.putInt(OFF_TICKS_KEY, blink.offTicks());
            nbt.putBoolean(RANDOM_KEY, blink.random());
        }
    }
}
