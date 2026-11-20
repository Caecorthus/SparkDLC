package dev.caecorthus.sparkdlc.sabotage.net;

import dev.caecorthus.sparkdlc.role.civilian.undercover.DoorBreachService;
import dev.caecorthus.sparkdlc.sabotage.BreakerRepairService;
import dev.caecorthus.sparkdlc.sabotage.EngineRepairService;
import dev.caecorthus.sparkdlc.sabotage.JunctionBoxService;
import dev.caecorthus.sparkdlc.sabotage.SabotageService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of sabotage: triggers, channels, breaker and engine repair, and the blackout
 * sight sync. Registered once, from {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of
 * the payload classes.
 * 破坏系统（触发、读条、电闸与动力维修，以及停电视野同步）的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class SabotagePackets {
    private SabotagePackets() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(SyncBlackoutSightS2CPacket.ID, SyncBlackoutSightS2CPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(TriggerSabotageC2SPacket.ID, TriggerSabotageC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CancelChannelC2SPacket.ID, CancelChannelC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CompleteBreakerRepairC2SPacket.ID, CompleteBreakerRepairC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CancelBreakerRepairC2SPacket.ID, CancelBreakerRepairC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SabotageStatusS2CPacket.ID, SabotageStatusS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ChannelProgressS2CPacket.ID, ChannelProgressS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenBreakerRepairS2CPacket.ID, OpenBreakerRepairS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(CloseBreakerRepairS2CPacket.ID, CloseBreakerRepairS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenEngineRepairS2CPacket.ID, OpenEngineRepairS2CPacket.CODEC);
        // Every handler re-validates on the server. / 每个处理器都在服务端重新校验。
        ServerPlayNetworking.registerGlobalReceiver(TriggerSabotageC2SPacket.ID,
                (payload, context) -> SabotageService.handleTrigger(context.player(), payload.typeWire()));
        // Junction box channels and the Undercover's door breach share the cancel packet; their ids never overlap.
        // 接线箱读条与卧底拆门共用取消包；两者的编号互不重叠。
        ServerPlayNetworking.registerGlobalReceiver(CancelChannelC2SPacket.ID, (payload, context) -> {
            if (!DoorBreachService.handleCancel(context.player(), payload.channelId())) {
                JunctionBoxService.handleCancel(context.player(), payload.channelId());
            }
        });
        // Breaker and engine panels share these two packets; their wire ids (complete_breaker_repair /
        // cancel_breaker_repair) are kept for compatibility, since other mods' action restriction lists match on them.
        // Session ids never repeat between the two services, and each ignores ids it does not own.
        // 电闸与动力面板共用这两个包；保留原 wire id（complete_breaker_repair / cancel_breaker_repair）是为了兼容，
        // 其他模组的动作限制名单按它们拦截。两个服务的会话 id 不会重复，各自忽略不属于自己的 id。
        ServerPlayNetworking.registerGlobalReceiver(CompleteBreakerRepairC2SPacket.ID, (payload, context) -> {
            BreakerRepairService.handleComplete(context.player(), payload.sessionId());
            EngineRepairService.handleComplete(context.player(), payload.sessionId());
        });
        ServerPlayNetworking.registerGlobalReceiver(CancelBreakerRepairC2SPacket.ID, (payload, context) -> {
            BreakerRepairService.handleCancel(context.player(), payload.sessionId());
            EngineRepairService.handleCancel(context.player(), payload.sessionId());
        });
    }
}
