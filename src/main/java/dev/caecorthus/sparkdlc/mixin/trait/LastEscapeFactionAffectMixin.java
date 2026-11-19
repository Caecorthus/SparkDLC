package dev.caecorthus.sparkdlc.mixin.trait;

import dev.caecorthus.sparkdlc.faction.target.PlayerAffectMixinGuard;
import dev.caecorthus.sparkdlc.trait.killer.escape.LastEscapeService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The train exception retains attribution, but cannot be vetoed as friendly fire. */
@Mixin(value = PlayerAffectMixinGuard.class, remap = false)
public abstract class LastEscapeFactionAffectMixin {
    @Inject(method = "allows", at = @At("HEAD"), cancellable = true)
    private static void sparktraits$trainException(PlayerEntity actor, PlayerEntity target, Identifier reason,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (LastEscapeService.isActive(target) && LastEscapeService.isTrainDeath(reason)) cir.setReturnValue(true);
    }
}
