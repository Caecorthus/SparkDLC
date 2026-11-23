package dev.caecorthus.sparkdlc.mixin.round.dualselect;

import dev.caecorthus.sparkdlc.round.dualselect.DualRoleSelectionComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Freezes the round clock while the dual-role choice is still open.
 * <p>
 * 二选一还没结束时不走这一 tick 的计时/状态推进，让整局停在原地。
 */
@Mixin(dev.doctor4t.wathe.cca.GameTimeComponent.class)
public abstract class GameTimeComponentFreezeMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void sparkdlc$freeze(CallbackInfo ci) {
        if (DualRoleSelectionComponent.isFreezing()) {
            ci.cancel();
        }
    }
}
