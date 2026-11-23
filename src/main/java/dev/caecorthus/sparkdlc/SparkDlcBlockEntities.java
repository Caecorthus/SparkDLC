package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.block.ShiningNeonPillarBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * The block entity types of the mod. The map-maker beds use Wathe's own trimmed bed type (see {@code SparkDlcBlocks});
 * the shining neon pillar has its own, which keeps a column's blink settings in the saved chunk.
 * 本模组的方块实体类型。供地图作者使用的床沿用 Wathe 自己的裁剪床类型（见 {@code SparkDlcBlocks}）；闪烁霓虹灯柱有自己的
 * 类型，用来把灯柱的闪烁设置保存在区块里。
 */
public final class SparkDlcBlockEntities {
    private static BlockEntityType<ShiningNeonPillarBlockEntity> shiningNeonPillar;

    private SparkDlcBlockEntities() {
    }

    /** Called by {@code SparkDlcBlocks} right after the pillar block registers. / 由 {@code SparkDlcBlocks} 在灯柱方块注册后立即调用。 */
    static synchronized void registerShiningNeonPillar(Block block) {
        if (shiningNeonPillar != null) {
            return;
        }
        shiningNeonPillar = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                SparkDlcBlocks.SHINING_NEON_PILLAR_ID,
                BlockEntityType.Builder.create(ShiningNeonPillarBlockEntity::new, block).build(null)
        );
    }

    public static BlockEntityType<ShiningNeonPillarBlockEntity> shiningNeonPillar() {
        if (shiningNeonPillar == null) {
            throw new IllegalStateException("SparkDLC block entity types are not registered yet");
        }
        return shiningNeonPillar;
    }
}
