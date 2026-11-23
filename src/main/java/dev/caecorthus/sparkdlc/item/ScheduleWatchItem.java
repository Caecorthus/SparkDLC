package dev.caecorthus.sparkdlc.item;

import dev.caecorthus.sparkdlc.block.ScheduleBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * A pocket watch that reads the station timetable: right-click to see how long the round still has. It answers through
 * {@link ScheduleBlock#showRemainingTime}, so it shares the timetable's per-player cooldown, which is this item's own
 * cooldown.
 * <p>
 * 读车站时刻表的怀表：右键查看本局剩余时间。它经由 {@link ScheduleBlock#showRemainingTime} 作答，因此与时刻表共用每名
 * 玩家的冷却，而这份冷却就是本物品自身的冷却。
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
            ScheduleBlock.showRemainingTime(serverPlayer);
        }
        return TypedActionResult.success(stack, world.isClient);
    }
}
