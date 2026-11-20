package dev.caecorthus.sparkdlc.mixin.role.killer.blackraven;

import dev.caecorthus.sparkdlc.role.killer.blackraven.BlackRavenInventoryRules;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Prevents the Black Raven ledger from becoming an item entity. */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityBlackRavenLedgerMixin {
    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void sparkdlc$keepBlackRavenLedgerBound(
            ItemStack stack,
            boolean throwRandomly,
            boolean retainOwnership,
            CallbackInfoReturnable<ItemEntity> cir
    ) {
        if (BlackRavenInventoryRules.blocksDrop(stack)) {
            cir.setReturnValue(null);
        }
    }
}
