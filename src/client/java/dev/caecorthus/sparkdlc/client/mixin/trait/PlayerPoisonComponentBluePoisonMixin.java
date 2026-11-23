package dev.caecorthus.sparkdlc.client.mixin.trait;

import dev.caecorthus.sparkdlc.trait.component.TraitPlayerComponent;
import dev.caecorthus.sparkdlc.trait.killer.conscience.BluePoisonPresentationRules;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerPoisonComponent.class, remap = false)
public abstract class PlayerPoisonComponentBluePoisonMixin {
    @Shadow
    @Final
    private PlayerEntity player;

    @Shadow
    public int poisonTicks;

    @Shadow
    public boolean pulsing;

    @Shadow
    public float pulseProgress;

    @Unique
    private int sparkdlc$bluePulseCooldown;

    @Unique
    private boolean sparkdlc$presentingBluePoison;

    @Inject(method = "clientTick", at = @At("HEAD"))
    private void sparkdlc$releaseBluePulseBeforeNativeTick(CallbackInfo ci) {
        if (sparkdlc$presentingBluePoison && poisonTicks > 0) {
            pulsing = false;
            pulseProgress = 0.0F;
            sparkdlc$presentingBluePoison = false;
            sparkdlc$bluePulseCooldown = 0;
        }
    }

    @Inject(method = "clientTick", at = @At("TAIL"))
    private void sparkdlc$bluePoisonHeartbeat(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != player) {
            return;
        }

        TraitPlayerComponent traits = TraitPlayerComponent.KEY.get(player);
        traits.tickConsciencePoisonClient();
        int nativePoisonTicks = poisonTicks;
        boolean bluePoisoned = traits.hasConsciencePoison();
        if (!SparkDlcServerConnection.isConfirmedServer() || !player.isAlive() || player.isSpectator()) {
            sparkdlc$clearBluePulse(nativePoisonTicks);
            return;
        }
        if (BluePoisonPresentationRules.owner(nativePoisonTicks > 0, bluePoisoned)
                != BluePoisonPresentationRules.Owner.BLUE_POISON) {
            sparkdlc$clearBluePulse(nativePoisonTicks);
            return;
        }

        sparkdlc$presentingBluePoison = true;
        int remainingTicks = traits.getConsciencePoisonTicks();
        int initialTicks = traits.getConsciencePoisonInitialTicks();
        if (!BluePoisonPresentationRules.canPulse(initialTicks, remainingTicks)) {
            return;
        }
        if (sparkdlc$bluePulseCooldown > 0) {
            sparkdlc$bluePulseCooldown--;
            return;
        }

        int maximumPoisonTicks = PlayerPoisonComponent.clampTime.getRight();
        sparkdlc$bluePulseCooldown = BluePoisonPresentationRules.cooldownAfterPulse(remainingTicks, maximumPoisonTicks);
        pulsing = true;
        player.playSound(
                SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                BluePoisonPresentationRules.heartbeatVolume(remainingTicks, maximumPoisonTicks),
                1.0F
        );
    }

    @Unique
    private void sparkdlc$clearBluePulse(int nativePoisonTicks) {
        sparkdlc$bluePulseCooldown = 0;
        if (sparkdlc$presentingBluePoison && nativePoisonTicks <= 0) {
            pulsing = false;
            pulseProgress = 0.0F;
        }
        sparkdlc$presentingBluePoison = false;
    }
}
