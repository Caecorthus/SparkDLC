package dev.caecorthus.sparkdlc.factionapi.mixin;

import dev.caecorthus.sparkdlc.factionapi.impl.FactionRegistryImpl;
import dev.doctor4t.wathe.api.Faction;
import dev.doctor4t.wathe.api.Role;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents SparkFactionAPI roles from leaking into Wathe's native buckets.
 * 防止 SparkFactionAPI 角色被 wathe 原生阵营桶误判。
 */
@Mixin(Faction.class)
public abstract class FactionMixin {
    @Inject(method = "fromRole", at = @At("HEAD"), cancellable = true)
    private static void sparkfactionapi$overrideSparkFactionRole(
            Role role,
            CallbackInfoReturnable<Faction> cir
    ) {
        FactionRegistryImpl.nativeFactionOverride(role).ifPresent(cir::setReturnValue);
    }
}
