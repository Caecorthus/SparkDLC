package dev.caecorthus.sparkdlc.witch.mixin;

import dev.caecorthus.sparkdlc.bridge.witch.SparkTraitsKillerBridge;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaTerminalService;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaDisconnectService;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Intercepts only the exact Vendetta pair before Wathe rejects a second death. / 在 Wathe 拒绝二次死亡前仅拦截仇杀客绑定双方。 */
@Mixin(GameFunctions.class)
public abstract class VendettaTerminalKillMixin {
    @Inject(
            method = "killPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;ZLnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/Identifier;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void sparkwitch$resolveVendettaTerminalDeath(
            ServerPlayerEntity victim,
            boolean spawnBody,
            ServerPlayerEntity killer,
            Identifier deathReason,
            boolean force,
            CallbackInfo ci
    ) {
        if (SparkTraitsKillerBridge.isLastEscapeActive(victim)) {
            return;
        }
        if (VendettaDisconnectService.shouldPauseOfflineBoundKillerEscape(
                victim, killer, deathReason, force)) {
            ci.cancel();
            return;
        }
        if (VendettaTerminalService.tryResolveBoundKillerVictory(victim, killer, deathReason)) {
            ci.cancel();
        }
    }
}
