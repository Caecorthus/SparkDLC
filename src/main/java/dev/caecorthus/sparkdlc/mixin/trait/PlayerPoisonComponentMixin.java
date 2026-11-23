package dev.caecorthus.sparkdlc.mixin.trait;

import dev.caecorthus.sparkdlc.trait.effective.EffectiveConsequenceService;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = PlayerPoisonComponent.class, remap = false)
public abstract class PlayerPoisonComponentMixin {
    @Shadow
    @Final
    private PlayerEntity player;

    @Inject(method = "setPoisonTicks(ILjava/util/UUID;Lnet/minecraft/util/Identifier;Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    private void sparkdlc$rememberPoisonSource(int ticks, UUID poisoner, Identifier source, NbtCompound recordExtra, CallbackInfo ci) {
        EffectiveConsequenceService.rememberPoisonSource(player, source);
    }
}
