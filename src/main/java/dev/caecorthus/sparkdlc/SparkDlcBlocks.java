package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.block.CircuitBreakerBlock;
import dev.caecorthus.sparkdlc.block.EngineUnitBlock;
import dev.caecorthus.sparkdlc.block.JunctionBoxBlock;
import dev.caecorthus.sparkdlc.block.ScheduleBlock;
import dev.caecorthus.sparkdlc.block.SparkBedBlock;
import dev.caecorthus.sparkdlc.block.SparkBedService;
import dev.caecorthus.sparkdlc.item.ScheduleWatchItem;
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

/**
 * The blocks of the mod and their block items: the sabotage fixtures, the map-maker beds and the station timetable
 * (with the pocket watch that reads it). Each part registers through its own method, at the moment it always did.
 * 本模组的方块及其方块物品：破坏系统的地图设施、供地图作者使用的床，以及车站时刻表（连同读它的怀表）。每个部分通过
 * 自己的方法、在一贯的时刻注册。
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
    // Station timetable / 车站时刻表
    // ---------------------------------------------------------------------------------------------------------------
    /*
     * The map-maker timetable block, its block item and the pocket watch that gives the same reading (see
     * ScheduleBlock). Registered from SparkDlcItems.registerRoleItems(), after every existing item, so earlier raw ids
     * keep their order. No listener: the reading cooldown is the watch's item cooldown.
     * 供地图作者使用的时刻表方块、其方块物品，以及给出同样读数的怀表（见 ScheduleBlock）。由
     * SparkDlcItems.registerRoleItems() 在所有既有物品之后注册，保持既有注册顺序。没有监听器：读数冷却就是怀表的物品冷却。
     */

    public static final Identifier SCHEDULE_ID = SparkDLC.id("schedule");
    public static final Identifier SCHEDULE_WATCH_ID = SparkDLC.id("schedule_watch");

    private static Block schedule;
    private static Item scheduleWatch;

    public static synchronized void registerSchedule() {
        if (schedule != null) {
            return;
        }
        schedule = registerWithItem(SCHEDULE_ID, new ScheduleBlock(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS)));
        scheduleWatch = Registry.register(
                Registries.ITEM,
                SCHEDULE_WATCH_ID,
                new ScheduleWatchItem(ScheduleWatchItem.createSettings())
        );
    }

    public static Block schedule() {
        if (schedule == null) {
            throw new IllegalStateException("SparkDLC timetable is not registered yet");
        }
        return schedule;
    }

    public static Item scheduleWatch() {
        if (scheduleWatch == null) {
            throw new IllegalStateException("SparkDLC timetable is not registered yet");
        }
        return scheduleWatch;
    }
}
