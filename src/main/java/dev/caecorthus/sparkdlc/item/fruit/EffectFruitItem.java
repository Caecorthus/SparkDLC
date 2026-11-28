package dev.caecorthus.sparkdlc.item.fruit;

import dev.caecorthus.sparkdlc.role.civilian.fruitvendor.FruitVendorRules;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Orange and Enlightenment Berry: ordinary food that also applies one effect, whether eaten from the hand or taken off
 * a platter — the effect lives here, so both routes get it.
 * 橙子与启明浆果：普通食物，额外附带一个效果；无论是手上吃还是从托盘取走，效果都在这里，两条路都会生效。
 */
public class EffectFruitItem extends Item {

    public enum Kind { ORANGE, ENLIGHTENMENT_BERRY }

    private final Kind kind;

    public EffectFruitItem(Settings settings, Kind kind) {
        super(settings);
        this.kind = kind;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack result = super.finishUsing(stack, world, user);
        if (!world.isClient) {
            switch (kind) {
                case ORANGE -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,
                        FruitVendorRules.ORANGE_SPEED_TICKS, FruitVendorRules.ORANGE_SPEED_AMPLIFIER,
                        true, false, true));
                case ENLIGHTENMENT_BERRY -> user.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING,
                        FruitVendorRules.BERRY_GLOW_TICKS, 0, true, false, true));
            }
        }
        return result;
    }
}
