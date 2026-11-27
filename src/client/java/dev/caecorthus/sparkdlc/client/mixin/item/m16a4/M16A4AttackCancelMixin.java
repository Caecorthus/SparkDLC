package dev.caecorthus.sparkdlc.client.mixin.item.m16a4;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Left click is the M16A4's reload key, so it must not also punch entities or mine blocks: both vanilla attack entry
 * points are cancelled while the rifle is in the main hand. The reload intent itself is sent once per key press by
 * {@code M16A4Client}, not from here.
 * 左键是 M16A4 的换弹键，因此不能同时打实体或挖方块：主手持步枪时取消原版的两个攻击入口。换弹意图由 {@code M16A4Client}
 * 在每次按键时发送一次，不在这里发送。
 */
@Mixin(MinecraftClient.class)
public abstract class M16A4AttackCancelMixin {

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void sparkdlc$noMeleeWithM16a4(CallbackInfoReturnable<Boolean> cir) {
        if (sparkdlc$holdsM16a4()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void sparkdlc$noMiningWithM16a4(boolean breaking, CallbackInfo ci) {
        if (sparkdlc$holdsM16a4()) {
            ci.cancel();
        }
    }

    private static boolean sparkdlc$holdsM16a4() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player != null && player.getMainHandStack().isOf(SparkDlcItems.m16a4());
    }
}
