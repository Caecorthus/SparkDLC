package dev.caecorthus.sparkdlc.client.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.caecorthus.sparkdlc.client.hooks.RealityAnchorClientHooks;
import dev.doctor4t.wathe.client.WatheClient;
import org.spongepowered.asm.mixin.Mixin;

/**
 * A placed reality anchor denies the Alt instinct look within 15 blocks (owner 2026-11-27). Both keyed Wathe gates are
 * wrapped (like the Control Expert stun gate), so instinct light and every key-gated highlight turn off together;
 * outlines that need no key stay untouched, and a spectating viewer is never gated
 * ({@code RealityAnchorClientHooks.blocksLocalInstinct}). The wrapper only ever answers false or delegates, so its
 * place among the other {@code @WrapMethod}s on these two methods cannot let an answer past another veto. Decided by
 * the client, like all of Wathe's instinct: a modified client is not bound by it. Wathe member: {@code remap = false}.
 * 已放置的现实稳定锚在 15 格内禁用 Alt 本能透视（所有者 2026-11-27）。与控场专家眩晕拦截一样包装 Wathe 的两个按键入口，
 * 因此本能增亮与所有需按键的高亮一起关闭；无需按键的描边不受影响，旁观视角永不受拦截
 * （{@code RealityAnchorClientHooks.blocksLocalInstinct}）。该包装器只会返回 false 或委托原方法，因此它在这两个方法的其他
 * {@code @WrapMethod} 之间处于什么位置，都不会让某个结果越过别的否决。与 Wathe 的全部本能一样由客户端判定：修改过的客户端
 * 不受其约束。Wathe 成员：{@code remap = false}。
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
