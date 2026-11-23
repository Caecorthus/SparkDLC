package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.block.CircuitBreakerBlock;
import dev.caecorthus.sparkdlc.block.EngineUnitBlock;
import dev.caecorthus.sparkdlc.block.JunctionBoxBlock;
import dev.caecorthus.sparkdlc.block.ShiningNeonPillarBlock;
import dev.caecorthus.sparkdlc.block.SparkBedBlock;
import dev.caecorthus.sparkdlc.block.SparkBedService;
import dev.doctor4t.wathe.index.WatheBlockEntities;
import dev.doctor4t.wathe.index.WatheBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * The blocks of the mod and their block items: the sabotage fixtures, the map-maker beds and the map-maker props
 * (yellowed wallpaper, shining neon pillar). Each part registers through its own method, at the moment it always did.
 * 本模组的方块及其方块物品：破坏系统的地图设施、供地图作者使用的床，以及地图道具（泛黄墙纸、闪烁霓虹灯柱）。
 * 每个部分通过自己的方法、在一贯的时刻注册。
 */
public final class SparkDlcBlocks {
    private SparkDlcBlocks() {
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Sabotage fixtures / 破坏系统的地图设施
    // ---------------------------------------------------------------------------------------------------------------
    /*
     * The sabotage blocks and their block items. Both are map fixtures: unbreakable outside creative, no drops.
     * 破坏系统的方块及其方块物品。两者都是地图设施：创造模式以外无法破坏，不掉落。
     */

    public static final Identifier JUNCTION_BOX_ID = SparkDLC.id("junction_box");
    public static final Identifier CIRCUIT_BREAKER_ID = SparkDLC.id("circuit_breaker");
    public static final Identifier ENGINE_UNIT_ID = SparkDLC.id("engine_unit");

    private static boolean sabotageFixturesRegistered;
    private static Block junctionBox;
    private static Block circuitBreaker;
    private static Block engineUnit;

    public static synchronized void registerSabotageFixtures() {
        if (sabotageFixturesRegistered) {
            return;
        }
        junctionBox = registerWithItem(JUNCTION_BOX_ID, new JunctionBoxBlock(fixtureSettings()));
        circuitBreaker = registerWithItem(CIRCUIT_BREAKER_ID, new CircuitBreakerBlock(fixtureSettings()));
        engineUnit = registerWithItem(ENGINE_UNIT_ID, new EngineUnitBlock(fixtureSettings()));
        sabotageFixturesRegistered = true;
    }

    public static Block junctionBox() {
        if (junctionBox == null) {
            throw new IllegalStateException("SparkDLC sabotage fixture blocks are not registered yet");
        }
        return junctionBox;
    }

    public static Block circuitBreaker() {
        if (circuitBreaker == null) {
            throw new IllegalStateException("SparkDLC sabotage fixture blocks are not registered yet");
        }
        return circuitBreaker;
    }

    public static Block engineUnit() {
        if (engineUnit == null) {
            throw new IllegalStateException("SparkDLC sabotage fixture blocks are not registered yet");
        }
        return engineUnit;
    }

    /** Any block the sabotage system scans for. / 破坏系统会扫描的任意方块。 */
    public static boolean isSabotageFixture(net.minecraft.block.BlockState state) {
        return state.isOf(junctionBox()) || state.isOf(circuitBreaker()) || state.isOf(engineUnit());
    }

    private static AbstractBlock.Settings fixtureSettings() {
        return AbstractBlock.Settings.create()
                .mapColor(MapColor.IRON_GRAY)
                .strength(-1.0F, 3_600_000.0F)
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque()
                .dropsNothing()
                .pistonBehavior(PistonBehavior.BLOCK);
    }

    private static Block registerWithItem(Identifier id, Block block) {
        Block registeredBlock = Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(registeredBlock, new Item.Settings()));
        return registeredBlock;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Map-maker beds / 供地图作者使用的床
    // ---------------------------------------------------------------------------------------------------------------
    /*
     * The two map-maker beds (PR #239). They copy Wathe's trimmed bed settings and are added to
     * Wathe's own {@code wathe:trimmed_bed} block entity type through Fabric's {@code addSupportedBlock}, so the block
     * entity they create is Wathe's {@code TrimmedBedBlockEntity} with Wathe's type: scorpion poison, its client
     * particles and every add-on hook that checks that type or class see them as ordinary trimmed beds. Registered from
     * {@code SparkDlcItems.registerRoleItems()}, after every existing item, so earlier raw ids keep their order.
     * <p>
     * 两张供地图作者使用的床（PR #239）。沿用 Wathe 裁剪床的方块设置，并通过 Fabric 的
     * {@code addSupportedBlock} 加入 Wathe 自己的 {@code wathe:trimmed_bed} 方块实体类型，因此它们创建的方块实体就是
     * 带 Wathe 类型的 {@code TrimmedBedBlockEntity}：蝎子毒、其客户端粒子以及所有按该类型或类判断的附属钩子都把它们视为
     * 普通裁剪床。由 {@code SparkDlcItems.registerRoleItems()} 在所有既有物品之后注册，保持既有注册顺序。
     */

    public static final Identifier GREY_DIRTY_BED_ID = SparkDLC.id("grey_dirty_bed");
    public static final Identifier GREEN_STRANGE_BED_ID = SparkDLC.id("green_strange_bed");

    private static Block greyDirtyBed;
    private static Block greenStrangeBed;

    public static synchronized void registerBeds() {
        if (greyDirtyBed != null) {
            return;
        }
        greyDirtyBed = registerBed(GREY_DIRTY_BED_ID, SparkBedBlock.Variant.GREY_DIRTY);
        greenStrangeBed = registerBed(GREEN_STRANGE_BED_ID, SparkBedBlock.Variant.GREEN_STRANGE);
        // The beds own their one listener, so RoleEvents' pinned registration order stays untouched.
        // 床自带唯一的监听器，因此 RoleEvents 中被固定的注册顺序保持不变。
        SparkBedService.register();
    }

    private static Block registerBed(Identifier id, SparkBedBlock.Variant variant) {
        Block bed = Registry.register(
                Registries.BLOCK,
                id,
                new SparkBedBlock(variant, AbstractBlock.Settings.copy(WatheBlocks.WHITE_TRIMMED_BED))
        );
        WatheBlockEntities.TRIMMED_BED.addSupportedBlock(bed);
        Registry.register(Registries.ITEM, id, new BlockItem(bed, new Item.Settings()));
        return bed;
    }

    public static Block greyDirtyBed() {
        return greyDirtyBed;
    }

    public static Block greenStrangeBed() {
        return greenStrangeBed;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Map-maker wallpaper / 供地图作者使用的墙纸
    // ---------------------------------------------------------------------------------------------------------------
    /*
     * The yellowed wallpaper family (PR #1): plain full cubes for train interiors, one texture on every face. One
     * sheet with a dark skirting board along its bottom edge, one plain sheet, and eight sheets with a painted arrow
     * that points the way through the carriages. Wool settings (soft, wool sounds, no tool needed) with a pale
     * yellow map colour; each block drops itself ({@code data/sparkdlc/loot_table/blocks}), like Wathe's decorative
     * blocks. Registered from {@code SparkDlcItems.registerRoleItems()} after every existing item, so earlier raw ids
     * keep their order; the block items join the creative tab through its namespace sweep, in this order.
     * <p>
     * 泛黄墙纸系列（PR #1）：用于车厢内饰的普通完整方块，六个面使用同一张贴图。一款底边带深色踢脚线，一款素面，另有八款
     * 画有指引车厢方向的箭头。方块设置沿用羊毛（质软、羊毛音效、无需工具），地图颜色为淡黄色；每个方块掉落自身
     * （{@code data/sparkdlc/loot_table/blocks}），与 Wathe 的装饰方块一致。
     * 由 {@code SparkDlcItems.registerRoleItems()} 在所有既有物品之后注册，保持既有注册顺序；方块物品按此顺序经命名空间
     * 扫描加入创造模式物品栏。
     */

    public static final Identifier YELLOWED_WALLPAPER_SKIRTING_ID = SparkDLC.id("yellowed_wallpaper_skirting");
    public static final Identifier YELLOWED_WALLPAPER_PLAIN_ID = SparkDLC.id("yellowed_wallpaper_plain");

    /**
     * Arrow directions, in registration order; the arrow block of direction {@code d} is
     * {@code sparkdlc:yellowed_wallpaper_arrow_<d>}. The direction is the one the arrow points on the block's top face
     * (north is the texture's up); on the side faces the arrow keeps its picture direction (north up, east right).
     * 箭头方向，按注册顺序；方向 {@code d} 的箭头方块为 {@code sparkdlc:yellowed_wallpaper_arrow_<d>}。方向指箭头在方块
     * 顶面上所指的方向（北即贴图的上方）；在侧面上箭头保持图上的方向（北朝上，东朝右）。
     */
    public static final List<String> WALLPAPER_ARROW_DIRECTIONS = List.of("n", "ne", "e", "se", "s", "sw", "w", "nw");

    private static boolean wallpaperRegistered;

    public static synchronized void registerWallpaper() {
        if (wallpaperRegistered) {
            return;
        }
        registerWithItem(YELLOWED_WALLPAPER_SKIRTING_ID, new Block(wallpaperSettings()));
        registerWithItem(YELLOWED_WALLPAPER_PLAIN_ID, new Block(wallpaperSettings()));
        for (String direction : WALLPAPER_ARROW_DIRECTIONS) {
            registerWithItem(SparkDLC.id("yellowed_wallpaper_arrow_" + direction), new Block(wallpaperSettings()));
        }
        wallpaperRegistered = true;
    }

    private static AbstractBlock.Settings wallpaperSettings() {
        return AbstractBlock.Settings.copy(Blocks.WHITE_WOOL).mapColor(MapColor.PALE_YELLOW);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Shining neon pillar / 闪烁霓虹灯柱
    // ---------------------------------------------------------------------------------------------------------------
    /*
     * A map-maker neon pillar that blinks (PR #2). It copies Wathe's neon pillar settings, so its light follows
     * {@code lit} and {@code active} exactly like Wathe's pillar, and has its own block entity type for the blink
     * settings. Registered from {@code SparkDlcItems.registerRoleItems()}, after every existing item, so earlier raw
     * ids keep their order.
     * 供地图作者使用、会闪烁的霓虹灯柱（PR #2）。沿用 Wathe 霓虹灯柱的方块设置，因此亮度与 Wathe 灯柱完全一样跟随
     * {@code lit} 与 {@code active}；闪烁设置存放在它自己的方块实体类型中。由 {@code SparkDlcItems.registerRoleItems()}
     * 在所有既有物品之后注册，保持既有原始 id 的顺序。
     */

    public static final Identifier SHINING_NEON_PILLAR_ID = SparkDLC.id("shining_neon_pillar");

    private static Block shiningNeonPillar;

    public static synchronized void registerShiningNeonPillar() {
        if (shiningNeonPillar != null) {
            return;
        }
        Block pillar = Registry.register(
                Registries.BLOCK,
                SHINING_NEON_PILLAR_ID,
                new ShiningNeonPillarBlock(AbstractBlock.Settings.copy(WatheBlocks.NEON_PILLAR))
        );
        SparkDlcBlockEntities.registerShiningNeonPillar(pillar);
        Registry.register(Registries.ITEM, SHINING_NEON_PILLAR_ID, new BlockItem(pillar, new Item.Settings()));
        shiningNeonPillar = pillar;
    }

    public static Block shiningNeonPillar() {
        if (shiningNeonPillar == null) {
            throw new IllegalStateException("SparkDLC shining neon pillar is not registered yet");
        }
        return shiningNeonPillar;
    }
}
