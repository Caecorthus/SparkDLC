package dev.caecorthus.sparkdlc.witch.registry;

import dev.caecorthus.sparkdlc.witch.SparkWitch;
import dev.caecorthus.sparkdlc.witch.block.props.ScheduleBlock;
import dev.caecorthus.sparkdlc.witch.item.props.ScheduleWatchItem;
import dev.caecorthus.sparkdlc.witch.prop.PropText;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registers the station timetable block and the pocket watch that reads the same schedule.
 * <p>
 * 注册车站时刻表方块，以及与它读同一份时刻表的怀表。
 */
public final class ScheduleBlocks {
    public static final Identifier SCHEDULE_ID = SparkWitch.id("schedule");
    public static final Identifier SCHEDULE_WATCH_ID = SparkWitch.id("schedule_watch");

    private static ScheduleBlock schedule;
    private static ScheduleWatchItem scheduleWatch;
    private static boolean registered;

    private ScheduleBlocks() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        schedule = new ScheduleBlock(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS));
        scheduleWatch = new ScheduleWatchItem(ScheduleWatchItem.createSettings());
        Registry.register(Registries.BLOCK, SCHEDULE_ID, schedule);
        Registry.register(Registries.ITEM, SCHEDULE_ID, new BlockItem(schedule, new Item.Settings()));
        Registry.register(Registries.ITEM, SCHEDULE_WATCH_ID, scheduleWatch);
        PropText.register();
    }

    public static ScheduleBlock schedule() {
        return schedule;
    }

    public static ScheduleWatchItem scheduleWatch() {
        return scheduleWatch;
    }
}
