package dev.caecorthus.sparkdlc.item.fruit;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

/**
 * Eats like any food, and hands back one banana peel — the owner's "食用后留下香蕉皮".
 * 像普通食物一样吃，并返还一张香蕉皮——即所有者的「食用后留下香蕉皮」。
 */
public class BananaItem extends Item {

    public BananaItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack result = super.finishUsing(stack, world, user);
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            ItemStack peel = new ItemStack(FruitItems.bananaPeel());
            if (!player.getInventory().insertStack(peel)) {
                player.dropItem(peel, false);
            }
        }
        return result;
    }
}
