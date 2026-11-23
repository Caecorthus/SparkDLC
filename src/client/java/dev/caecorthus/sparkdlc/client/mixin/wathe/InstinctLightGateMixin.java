package dev.caecorthus.sparkdlc.client.mixin.wathe;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.caecorthus.sparkdlc.client.hooks.WitchInstinctClientHooks;
import dev.caecorthus.sparkdlc.client.role.jiahao.corruptcop.CorruptCopClientHooks;
import dev.doctor4t.wathe.client.WatheClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Extends Wathe's version-pinned instinct lightmap gate for the mod's chosen roles, without granting killer
 * authority: the Corrupt Cop, then the roles of {@link WitchInstinctClientHooks}. Was two wrappers of this call (the
 * Corrupt Cop's, applied first and so innermost, and the witch roles' outside it); one handler asks in the same
 * order, and no other entry of the client config wraps this call, so the slot of the first one keeps the chain.
 * 为本模组指定角色扩展 Wathe 版本锁定的本能亮度入口，但不授予杀手权限：先黑警，再 {@link WitchInstinctClientHooks} 的角色。
 * 原为对此调用的两个包装（黑警的先应用、位于内层，魔女角色的在其外层）；一个处理器按同样顺序询问，客户端配置中没有其他条目包装
 * 此调用，因此占用第一个的位置即可保持调用链。
 */
@Mixin(value = WatheClient.class, remap = false)
public abstract class InstinctLightGateMixin {
    @WrapOperation(
            method = "lambda$onInitializeClient$15",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/client/WatheClient;isInstinctEnabledAndIsKiller()Z")
    )
    private static boolean sparkdlc$allowKillerStyleInstinctLight(Operation<Boolean> original) {
        // Preserve the wrapped chain so other mods can extend the same Wathe light gate.
        // 保留包装调用链，让其他模组也能扩展同一个 Wathe 亮度入口。
        boolean originalAllowed = original.call();
        return originalAllowed
                || CorruptCopClientHooks.usesKillerStyleInstinctLight()
                || WitchInstinctClientHooks.usesKillerStyleInstinctLight();
    }
}
