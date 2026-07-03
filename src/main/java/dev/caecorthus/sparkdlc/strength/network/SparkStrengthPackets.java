package dev.caecorthus.sparkdlc.strength.network;

import dev.caecorthus.sparkdlc.strength.network.criminologist.OpenCriminologistScreenS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.criminologist.SelectCriminologistTargetC2SPacket;
import dev.caecorthus.sparkdlc.strength.network.noisemaker.NoisemakerGlowC2SPacket;
import dev.caecorthus.sparkdlc.strength.noisemaker.NoisemakerGlowService;
import dev.caecorthus.sparkdlc.strength.role.NoellesRoleEnhancementService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * SparkStrength 网络包注册。
 */
public final class SparkStrengthPackets {
    private SparkStrengthPackets() {
    }

    public static void registerServer() {
        PayloadTypeRegistry.playC2S().register(NoisemakerGlowC2SPacket.ID, NoisemakerGlowC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SelectCriminologistTargetC2SPacket.ID, SelectCriminologistTargetC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenCriminologistScreenS2CPacket.ID, OpenCriminologistScreenS2CPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(NoisemakerGlowC2SPacket.ID, (payload, context) ->
                NoisemakerGlowService.tryUseBackpackGlow(context.player(), payload.targetPlayer())
        );
        ServerPlayNetworking.registerGlobalReceiver(SelectCriminologistTargetC2SPacket.ID,
                (payload, context) -> NoellesRoleEnhancementService.handleCriminologistSelection(
                        context.player(),
                        payload.victimUuid(),
                        payload.suspectUuid()
                ));
    }
}
