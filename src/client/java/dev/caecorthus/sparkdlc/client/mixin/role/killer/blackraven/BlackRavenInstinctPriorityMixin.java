package dev.caecorthus.sparkdlc.client.mixin.role.killer.blackraven;

import dev.caecorthus.sparkdlc.client.role.killer.blackraven.BlackRavenInstinctClientHooks;
import dev.doctor4t.wathe.client.WatheClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * HEAD answer for the Black Raven held-instinct x-ray on perceived players. The trait {@code WatheClientMixin} calls
 * the same resolver directly before its Conscience branch, so Conscience cannot recolour or hide the x-ray.
 * 黑羽鸦按住本能键时对已感知玩家透视的 HEAD 结果。词条的 {@code WatheClientMixin} 在善良分支之前直接调用同一裁决器，因此善良不会改色或隐藏透视。
 */
@Mixin(value = WatheClient.class, remap = false, priority = 2000)
public abstract class BlackRavenInstinctPriorityMixin {
    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void sparkdlc$prioritizeBlackRavenXray(
            Entity target,
            CallbackInfoReturnable<Integer> cir
    ) {
        Integer color = BlackRavenInstinctClientHooks.resolvePrioritySensedHighlight(target);
        if (color != null) {
            cir.setReturnValue(color);
        }
    }
}
