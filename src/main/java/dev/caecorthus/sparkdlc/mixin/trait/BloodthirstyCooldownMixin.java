package dev.caecorthus.sparkdlc.mixin.trait;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.trait.killer.KillerTraitService;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerItemCooldownManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Applies Bloodthirsty only to tagged weapon cooldowns owned by the player.
 * 只把嗜血作用到玩家自己的已标记武器冷却上。
 */
@Mixin(value = ItemCooldownManager.class, priority = 950)
public abstract class BloodthirstyCooldownMixin {
    private static final TagKey<Item> BLOODTHIRSTY_WEAPONS = TagKey.of(
            RegistryKeys.ITEM,
            SparkDLC.id("bloodthirsty_weapons")
    );

    @ModifyVariable(method = "set", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int sparkdlc$reduceBloodthirstyWeaponCooldown(int duration, Item item) {
        if (Registries.ITEM.getEntry(item).isIn(BLOODTHIRSTY_WEAPONS)
                && (Object) this instanceof ServerItemCooldownManager serverCooldownManager) {
            ServerPlayerEntity player = ((ServerItemCooldownManagerAccessor) (Object) serverCooldownManager).sparkdlc$getPlayerForTraits();
            return KillerTraitService.bloodthirstyCooldown(player, duration);
        }
        return duration;
    }
}
