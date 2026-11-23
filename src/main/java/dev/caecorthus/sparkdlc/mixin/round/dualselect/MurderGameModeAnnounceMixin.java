package dev.caecorthus.sparkdlc.mixin.round.dualselect;

import dev.caecorthus.sparkdlc.round.dualselect.DualRoleSelectionComponent;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Swallows Wathe's opening role announcement while a dual-role choice is still open, because that screen would replace
 * the choice sheet and start the round early. The announcement is sent again once the choice resolves.
 * <p>
 * 双选还没结束时吞掉 Wathe 开局的身份报幕：那个屏幕会顶掉选择界面并让对局提前开始，等选择落地后我们再补发一次。
 */
@Mixin(MurderGameMode.class)
public abstract class MurderGameModeAnnounceMixin {

    @Redirect(
            method = "initializeGame",
            at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking;send(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/packet/CustomPayload;)V")
    )
    private void sparkdlc$holdAnnouncement(ServerPlayerEntity player, CustomPayload payload) {
        if (payload instanceof AnnounceWelcomePayload && DualRoleSelectionComponent.isFreezing()) {
            return;
        }
        ServerPlayNetworking.send(player, payload);
    }
}
