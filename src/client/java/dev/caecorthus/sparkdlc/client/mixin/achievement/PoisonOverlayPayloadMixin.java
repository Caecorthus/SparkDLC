package dev.caecorthus.sparkdlc.client.mixin.achievement;

import dev.caecorthus.sparkdlc.client.achievement.AchievementClientState;
import dev.doctor4t.wathe.util.PoisonUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Notes which poison overlay Wathe showed the player (e.g. a bed scorpion). 记录 Wathe 给玩家显示的中毒画面（例如床底蝎子）。 */
@Mixin(value = PoisonUtils.PoisonOverlayPayload.Receiver.class, remap = false)
public abstract class PoisonOverlayPayloadMixin {
    @Inject(
            method = "receive(Ldev/doctor4t/wathe/util/PoisonUtils$PoisonOverlayPayload;Lnet/fabricmc/fabric/api/client/networking/v1/ClientPlayNetworking$Context;)V",
            at = @At("HEAD")
    )
    private void sparkdlc$poisonOverlay(PoisonUtils.PoisonOverlayPayload payload,
                                           ClientPlayNetworking.Context context, CallbackInfo ci) {
        AchievementClientState.onPoisonOverlay(payload.translationKey());
    }
}
