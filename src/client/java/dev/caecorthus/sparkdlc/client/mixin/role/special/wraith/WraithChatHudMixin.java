package dev.caecorthus.sparkdlc.client.mixin.role.special.wraith;

import dev.caecorthus.sparkdlc.client.render.WraithClientState;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelRules;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithParticipationRules;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WatheClient.class, remap = false)
public final class WraithChatHudMixin {
    private WraithChatHudMixin() {
    }

    @Inject(method = "shouldDisableChat()Z", at = @At("HEAD"), cancellable = true, require = 1)
    private static void sparkwitch$enforceWraithChatPolicy(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        var player = client.player;
        if (!SparkDlcServerConnection.isConfirmedServer()
                || player == null
                || !WraithClientState.isActive(player)) {
            return;
        }

        var role = GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
        cir.setReturnValue(!WraithParticipationRules.mayUseTextChat(
                true,
                GuardianAngelRules.isGuardianAngel(role),
                player.isCreative()
        ));
    }
}
