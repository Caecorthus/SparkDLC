package dev.caecorthus.sparkdlc.witch.item.props;

import dev.caecorthus.sparkdlc.witch.prop.PropText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * A pocket watch that reads the same timetable as the station clock: right-click to hear how long the round still has,
 * with the same per-player cooldown so it cannot be spammed.
 * <p>
 * 怀表，读的是与车站时钟同一份时刻表：右键查看本局剩余时间，与时钟共用每名玩家的查看冷却，不能刷。
 */
public class ScheduleWatchItem extends Item {

    public ScheduleWatchItem(Settings settings) {
        super(settings);
    }

    public static Settings createSettings() {
        return new Settings().maxCount(1);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            PropText.showSchedule(serverPlayer, world);
        }
        return TypedActionResult.success(stack, world.isClient);
    }
}
