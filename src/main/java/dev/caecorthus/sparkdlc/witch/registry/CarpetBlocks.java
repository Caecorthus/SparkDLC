package dev.caecorthus.sparkdlc.witch.registry;

import dev.caecorthus.sparkdlc.witch.SparkWitch;
import dev.caecorthus.sparkdlc.witch.block.props.DampCarpetBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registers the two damp carpets: a thin carpet that lies flush on the floor and a full block version for builds that
 * need the carpet stacked or used as a wall covering.
 * <p>
 * 注册两种潮湿地毯：贴地铺的薄地毯，以及供堆叠或当墙面用的完整方块版本。
 */
public final class CarpetBlocks {
    public static final Identifier DAMP_CARPET_ID = SparkWitch.id("damp_carpet");
    public static final Identifier DAMP_CARPET_BLOCK_ID = SparkWitch.id("damp_carpet_block");

    private static DampCarpetBlock dampCarpet;
    private static Block dampCarpetBlock;
    private static boolean registered;

    private CarpetBlocks() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        dampCarpet = new DampCarpetBlock(AbstractBlock.Settings.copy(Blocks.MOSS_CARPET));
        dampCarpetBlock = new Block(AbstractBlock.Settings.copy(Blocks.BROWN_WOOL));
        register(DAMP_CARPET_ID, dampCarpet);
        register(DAMP_CARPET_BLOCK_ID, dampCarpetBlock);
    }

    private static void register(Identifier id, Block block) {
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
    }

    public static DampCarpetBlock dampCarpet() {
        return dampCarpet;
    }

    public static Block dampCarpetBlock() {
        return dampCarpetBlock;
    }
}
