package dev.caecorthus.sparkdlc.client.mixin.item.m16a4;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.caecorthus.sparkdlc.client.item.m16a4.M16A4ReloadInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * M16A4 left-click (client intent only; the server decides every reload), the anti-tank launcher's
 * {@code PotionLauncherInputMixin} scheme with a reload in place of a shot. While the local player's main hand holds
 * the rifle: every {@code wasPressed()} read in {@code handleInputEvents} is wrapped (always called through, never
 * redirected), and an attack press drained by either branch (the item-in-use drain during a trigger pull, or the
 * {@code doAttack} loop) is an edge for {@code M16A4ReloadInput.onPressEdge}, the only reload path;
 * {@code doAttack} HEAD swallows the attack (answer false, no swing); {@code handleBlockBreaking} HEAD is cancelled,
 * so a held key never mines. The Seeker remote view, the Control Expert stun and the Kidnapper control make the attack
 * key read as released at {@code KeyBinding}, and a drone pilot drains it before vanilla does, so no edge reaches this
 * path during them. Each hook answers only for the M16A4 and chains with the launcher's, the AXMC's, the M32A1's, the
 * Gohei's and the Death Ray's hooks on the same calls.
 * M16A4 左键（仅为客户端意图；每次换弹都由服务端决定），即反坦克炮筒 {@code PotionLauncherInputMixin} 的方案，只是把射击换成
 * 换弹。本地玩家主手持步枪时：包装 {@code handleInputEvents} 中每次 {@code wasPressed()} 读取（始终继续调用，从不重定向），
 * 任一分支（扣扳机期间的使用中丢弃循环，或 {@code doAttack} 循环）取出的攻击按键都是 {@code M16A4ReloadInput.onPressEdge}
 * 的按下沿，也是唯一的换弹路径；{@code doAttack} HEAD 吞掉攻击（返回 false，不挥动）；{@code handleBlockBreaking} HEAD
 * 被取消，因此按住不放永远不会挖掘。搜寻者遥控视角、控场专家眩晕与绑匪控制会在 {@code KeyBinding} 层让攻击键读作未按下，
 * 无人机驾驶则先于原版取走按键，因此这些期间不会有按下沿到达本路径。每个钩子只对 M16A4 作答，并与同一调用上的炮筒、AXMC、
 * M32A1、御币与死亡射线钩子串联。
 */
@Mixin(MinecraftClient.class)
public abstract class M16A4InputMixin {
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void sparkdlc$swallowM16a4Attack(CallbackInfoReturnable<Boolean> cir) {
        if (M16A4ReloadInput.onAttack((MinecraftClient) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(method = "handleInputEvents",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z"))
    private boolean sparkdlc$reloadM16a4OnPressEdge(KeyBinding key, Operation<Boolean> original) {
        boolean pressed = original.call(key);
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (pressed && key == client.options.attackKey) {
            M16A4ReloadInput.onPressEdge(client);
        }
        return pressed;
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void sparkdlc$cancelM16a4BlockBreaking(CallbackInfo ci) {
        if (M16A4ReloadInput.onBlockBreaking((MinecraftClient) (Object) this)) {
            ci.cancel();
        }
    }
}
