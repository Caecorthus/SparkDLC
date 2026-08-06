package dev.caecorthus.sparkdlc.strength.network;

import dev.caecorthus.sparkdlc.strength.network.criminologist.OpenCriminologistScreenS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.criminologist.SelectCriminologistTargetC2SPacket;
import dev.caecorthus.sparkdlc.strength.network.coroner.CoronerMorphC2SPacket;
import dev.caecorthus.sparkdlc.strength.network.demonhunter.DemonHunterSniffC2SPacket;
import dev.caecorthus.sparkdlc.strength.network.noisemaker.NoisemakerGlowC2SPacket;
import dev.caecorthus.sparkdlc.strength.network.professor.ProfessorRemoteFeedC2SPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.ApproveSuspectRemovalC2SPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.CallTabletMeetingC2SPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.CastTabletVoteC2SPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.ConfirmTabletVoteC2SPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.OpenTabletScreenS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.RequestTabletSnapshotC2SPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.SendTabletChatC2SPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.SyncTabletSnapshotS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.veteran.SyncVeteranBlackoutS2CPacket;
import dev.caecorthus.sparkdlc.strength.role.noisemaker.NoisemakerGlowService;
import dev.caecorthus.sparkdlc.strength.role.coroner.CoronerService;
import dev.caecorthus.sparkdlc.strength.role.professor.ProfessorSerumService;
import dev.caecorthus.sparkdlc.strength.role.detective.CriminologistService;
import dev.caecorthus.sparkdlc.strength.role.demonhunter.DemonHunterSniffService;
import dev.caecorthus.sparkdlc.strength.tablet.TabletStateService;
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
        PayloadTypeRegistry.playC2S().register(CoronerMorphC2SPacket.ID, CoronerMorphC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ProfessorRemoteFeedC2SPacket.ID, ProfessorRemoteFeedC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SelectCriminologistTargetC2SPacket.ID, SelectCriminologistTargetC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(DemonHunterSniffC2SPacket.ID, DemonHunterSniffC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestTabletSnapshotC2SPacket.ID, RequestTabletSnapshotC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SendTabletChatC2SPacket.ID, SendTabletChatC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CallTabletMeetingC2SPacket.ID, CallTabletMeetingC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CastTabletVoteC2SPacket.ID, CastTabletVoteC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ConfirmTabletVoteC2SPacket.ID, ConfirmTabletVoteC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ApproveSuspectRemovalC2SPacket.ID, ApproveSuspectRemovalC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenCriminologistScreenS2CPacket.ID, OpenCriminologistScreenS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenTabletScreenS2CPacket.ID, OpenTabletScreenS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncTabletSnapshotS2CPacket.ID, SyncTabletSnapshotS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncVeteranBlackoutS2CPacket.ID, SyncVeteranBlackoutS2CPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(NoisemakerGlowC2SPacket.ID, (payload, context) ->
                NoisemakerGlowService.tryUseBackpackGlow(context.player(), payload.targetPlayer())
        );
        ServerPlayNetworking.registerGlobalReceiver(CoronerMorphC2SPacket.ID, (payload, context) ->
                CoronerService.selectDisguise(context.player(), payload.targetPlayer())
        );
        ServerPlayNetworking.registerGlobalReceiver(ProfessorRemoteFeedC2SPacket.ID, (payload, context) ->
                ProfessorSerumService.tryRemoteFeed(context.player(), payload.targetPlayer(), payload.serumType())
        );
        ServerPlayNetworking.registerGlobalReceiver(SelectCriminologistTargetC2SPacket.ID,
                (payload, context) -> CriminologistService.handleSelection(
                        context.player(),
                        payload.victimUuid(),
                        payload.suspectUuid()
                ));
        ServerPlayNetworking.registerGlobalReceiver(DemonHunterSniffC2SPacket.ID,
                (payload, context) -> DemonHunterSniffService.trySniff(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(RequestTabletSnapshotC2SPacket.ID,
                (payload, context) -> TabletStateService.syncTo(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(SendTabletChatC2SPacket.ID,
                (payload, context) -> TabletStateService.sendChat(context.player(), payload.message()));
        ServerPlayNetworking.registerGlobalReceiver(CallTabletMeetingC2SPacket.ID,
                (payload, context) -> TabletStateService.callMeeting(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(CastTabletVoteC2SPacket.ID,
                (payload, context) -> TabletStateService.castVote(context.player(), payload.targetUuid()));
        ServerPlayNetworking.registerGlobalReceiver(ConfirmTabletVoteC2SPacket.ID,
                (payload, context) -> TabletStateService.confirmVote(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(ApproveSuspectRemovalC2SPacket.ID,
                (payload, context) -> TabletStateService.setSuspectRemovalApproval(
                        context.player(),
                        payload.suspectUuid(),
                        payload.approved()
                ));
    }
}
