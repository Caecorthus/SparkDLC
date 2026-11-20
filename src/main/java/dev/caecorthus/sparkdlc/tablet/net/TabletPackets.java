package dev.caecorthus.sparkdlc.tablet.net;

import dev.caecorthus.sparkdlc.tablet.TabletStateService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of the tablet: snapshot, chat, channels, meetings and votes. Registered once, from
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of the payload classes.
 * 平板电脑（快照、聊天、频道、会议与投票）的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class TabletPackets {
    private TabletPackets() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(RequestTabletSnapshotC2SPacket.ID, RequestTabletSnapshotC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SendTabletChatC2SPacket.ID, SendTabletChatC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CallTabletMeetingC2SPacket.ID, CallTabletMeetingC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CastTabletVoteC2SPacket.ID, CastTabletVoteC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ConfirmTabletVoteC2SPacket.ID, ConfirmTabletVoteC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ApproveSuspectRemovalC2SPacket.ID, ApproveSuspectRemovalC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SelectTabletChannelC2SPacket.ID, SelectTabletChannelC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenTabletScreenS2CPacket.ID, OpenTabletScreenS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(TabletChatNotifyS2CPacket.ID, TabletChatNotifyS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncTabletSnapshotS2CPacket.ID, SyncTabletSnapshotS2CPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RequestTabletSnapshotC2SPacket.ID,
                (payload, context) -> TabletStateService.requestSnapshot(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(SendTabletChatC2SPacket.ID,
                (payload, context) -> TabletStateService.sendChat(
                        context.player(),
                        payload.expectedChannelWire(),
                        payload.message()
                ));
        ServerPlayNetworking.registerGlobalReceiver(SelectTabletChannelC2SPacket.ID,
                (payload, context) -> TabletStateService.selectChannel(context.player(), payload.channelWire()));
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
