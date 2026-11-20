package dev.caecorthus.sparkdlc.client.mixin.role.civilian.timekeeper;

import dev.caecorthus.sparkdlc.client.role.civilian.timekeeper.TimekeeperWatchClientHooks;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 怀表左键只用于切换模式，不能继续触发原版攻击或破坏方块。 */
@Mixin(MinecraftClient.class)
public abstract class TimekeeperWatchAttackMixin {
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void sparkdlc$toggleWatchMode(CallbackInfoReturnable<Boolean> cir) {
        if (TimekeeperWatchClientHooks.handleAttack(MinecraftClient.getInstance())) {
            cir.setReturnValue(false);
        }
    }
}
