package dev.caecorthus.sparkdlc.item.drink;

import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * A served drink in the same family as Wathe's martini and mojito: held and drunk over a second and completes the
 * drinking task. It deliberately grants no status effect (owner 2026-11-28). 与 Wathe 的马提尼、莫吉托同族的成品饮料：
 * 手持饮用一秒并完成饮酒任务。按所有者 2026-11-28 的要求，它刻意不附加任何状态效果。
 */
public class SparkDrinkItem extends Item {

    /** Same drink time as the vanilla drinks and Wathe's cocktails. / 与原版饮品、Wathe 鸡尾酒相同的饮用时长。 */
    public static final int USE_TICKS = 32;


    public SparkDrinkItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack result = super.finishUsing(stack, world, user);
        if (user instanceof ServerPlayerEntity player) {
            world.emitGameEvent(player, net.minecraft.world.event.GameEvent.DRINK, player.getPos());
            // Completes Wathe's drink task, exactly like CocktailItem does.
            // 完成 Wathe 的饮酒任务，与 CocktailItem 行为一致。
            PlayerMoodComponent.KEY.get(player).drinkCocktail();
            GameRecordManager.recordItemUse(player, Registries.ITEM.getId(this), null, null);
        }
        return result;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return USE_TICKS;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public SoundEvent getDrinkSound() {
        return SoundEvents.ENTITY_GENERIC_DRINK;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.ENTITY_GENERIC_DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }
}
