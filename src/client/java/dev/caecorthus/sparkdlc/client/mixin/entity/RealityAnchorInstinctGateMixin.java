package dev.caecorthus.sparkdlc.client.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.caecorthus.sparkdlc.client.hooks.RealityAnchorClientHooks;
import dev.doctor4t.wathe.client.WatheClient;
import org.spongepowered.asm.mixin.Mixin;

/**
 * A placed reality anchor denies the Alt instinct look within 15 blocks (owner 2026-11-27). Both keyed Wathe gates are
 * wrapped (like the Control Expert stun gate), so instinct light and every key-gated highlight turn off together;
 * outlines that need no key stay untouched.
 * 已放置的现实稳定锚在 15 格内禁用 Alt 本能透视（所有者 2026-11-27）。与控场专家眩晕拦截一样包装 Wathe 的两个按键入口，
 * 因此本能增亮与所有需按键的高亮一起关闭；无需按键的描边不受影响。
 */
@Mixin(value = WatheClient.class, remap = false)
public abstract class RealityAnchorInstinctGateMixin {
    @WrapMethod(method = "isInstinctEnabled()Z")
    private static boolean sparkdlc$gateAnchorInstinct(Operation<Boolean> original) {
        return !RealityAnchorClientHooks.blocksLocalInstinct() && original.call();
    }

    @WrapMethod(method = "isInstinctEnabledAndIsKiller()Z")
    private static boolean sparkdlc$gateAnchorKillerInstinct(Operation<Boolean> original) {
        return !RealityAnchorClientHooks.blocksLocalInstinct() && original.call();
    }
}
