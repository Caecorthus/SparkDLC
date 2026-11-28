package dev.caecorthus.sparkdlc.client.mixin.item.m16a4;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.caecorthus.sparkdlc.client.item.m16a4.M16A4ReloadInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * macOS Ctrl + left-click with the M16A4 (client intent only). Vanilla's Ctrl-click branch would turn a sprinting
 * (Left Ctrl) left click into a right click, which pulls the trigger instead of reloading; this answers false for its
 * one {@code IS_SYSTEM_MAC} read only when {@code M16A4ReloadInput.keepsMacLeftPress} says so, chaining with the
 * launcher, AXMC, M32A1, Gohei and Death Ray answers for the same read. Releases are never touched.
 * macOS 上持 M16A4 的 Ctrl + 左键（仅为客户端意图）。原版 Ctrl 点击分支会把疾跑（左 Ctrl）时的左键变成右键，于是扣动扳机而
 * 不是换弹；仅当 {@code M16A4ReloadInput.keepsMacLeftPress} 判定时，本钩子让那一次 {@code IS_SYSTEM_MAC} 读取返回 false，
 * 并与同一读取上的炮筒、AXMC、M32A1、御币与死亡射线应答串联。从不改动松开。
 */
@Mixin(Mouse.class)
public abstract class M16A4MacControlClickMixin {
    @Shadow
    private int controlLeftClicks;

    @ModifyExpressionValue(method = "onMouseButton",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;IS_SYSTEM_MAC:Z",
                    opcode = Opcodes.GETSTATIC))
    private boolean sparkdlc$keepM16a4LeftPressOnMac(boolean systemMac,
                                                     @Local(argsOnly = true, ordinal = 0) int button,
                                                     @Local(argsOnly = true, ordinal = 1) int action) {
        return systemMac && !M16A4ReloadInput.keepsMacLeftPress(MinecraftClient.getInstance(), button, action,
                controlLeftClicks);
    }
}
