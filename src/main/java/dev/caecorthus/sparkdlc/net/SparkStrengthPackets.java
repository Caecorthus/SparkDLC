package dev.caecorthus.sparkdlc.net;

import dev.caecorthus.sparkdlc.role.killer.bandit.BanditRevolverActionC2SPayload;
import dev.caecorthus.sparkdlc.role.killer.bandit.BanditRevolverShootC2SPayload;
import dev.caecorthus.sparkdlc.role.killer.bomber.TimedBombMarkersPayload;
import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerMorphC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterSniffC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.undercover.UndercoverMaskOffC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.detective.DetectiveInterrogationResultS2CPacket;
import dev.caecorthus.sparkdlc.role.civilian.detective.InterrogateDetectiveSuspectC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.detective.OpenDetectiveFolderS2CPacket;
import dev.caecorthus.sparkdlc.role.civilian.detective.OpenDetectiveInterrogationS2CPacket;
import dev.caecorthus.sparkdlc.role.killer.bomber.drone.DronePackets;
import dev.caecorthus.sparkdlc.role.civilian.spiritualist.SpiritPossessionPackets;
import dev.caecorthus.sparkdlc.role.civilian.detective.SelectDetectiveCaseC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.detective.SetDetectiveKillerGuessC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.detective.SetDetectivePresumedKillerC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.detective.UpdateDetectiveCaseNotesC2SPacket;
import dev.caecorthus.sparkdlc.economy.SyncKillerTeamEconomyS2CPacket;
import dev.caecorthus.sparkdlc.item.m67.M67Packets;
import dev.caecorthus.sparkdlc.role.civilian.noisemaker.NoisemakerGlowC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.phantom.PhantomBackpackInvisibilityC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.professor.ProfessorRemoteFeedC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.reporter.ReporterCommunicationC2SPacket;
import dev.caecorthus.sparkdlc.tablet.net.ApproveSuspectRemovalC2SPacket;
import dev.caecorthus.sparkdlc.tablet.net.CallTabletMeetingC2SPacket;
import dev.caecorthus.sparkdlc.tablet.net.CastTabletVoteC2SPacket;
import dev.caecorthus.sparkdlc.tablet.net.ConfirmTabletVoteC2SPacket;
import dev.caecorthus.sparkdlc.tablet.net.OpenTabletScreenS2CPacket;
import dev.caecorthus.sparkdlc.tablet.net.RequestTabletSnapshotC2SPacket;
import dev.caecorthus.sparkdlc.tablet.net.SelectTabletChannelC2SPacket;
import dev.caecorthus.sparkdlc.tablet.net.SendTabletChatC2SPacket;
import dev.caecorthus.sparkdlc.tablet.net.SyncTabletSnapshotS2CPacket;
import dev.caecorthus.sparkdlc.tablet.net.TabletChatNotifyS2CPacket;
import dev.caecorthus.sparkdlc.role.neutral.shadowjester.SyncShadowJesterShowdownMusicS2CPacket;
import dev.caecorthus.sparkdlc.role.civilian.timekeeper.TimekeeperWatchModeC2SPacket;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSuperCurseC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerMenuC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerMenuS2CPacket;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerSummonC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerSwallowC2SPacket;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadFireC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.noisemaker.NoisemakerGlowService;
import dev.caecorthus.sparkdlc.role.killer.phantom.PhantomBackpackService;
import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerService;
import dev.caecorthus.sparkdlc.role.civilian.professor.ProfessorSerumService;
import dev.caecorthus.sparkdlc.role.civilian.detective.DetectiveCaseService;
import dev.caecorthus.sparkdlc.role.civilian.reporter.ReporterCommunicationService;
import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterSniffService;
import dev.caecorthus.sparkdlc.role.civilian.undercover.DoorBreachService;
import dev.caecorthus.sparkdlc.role.civilian.undercover.UndercoverService;
import dev.caecorthus.sparkdlc.role.civilian.timekeeper.TimekeeperWatchService;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSuperCurseService;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerSkillService;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerTaotieService;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadService;
import dev.caecorthus.sparkdlc.tablet.TabletStateService;
import dev.caecorthus.sparkdlc.sabotage.net.CancelBreakerRepairC2SPacket;
import dev.caecorthus.sparkdlc.sabotage.net.CancelChannelC2SPacket;
import dev.caecorthus.sparkdlc.sabotage.net.ChannelProgressS2CPacket;
import dev.caecorthus.sparkdlc.sabotage.net.CloseBreakerRepairS2CPacket;
import dev.caecorthus.sparkdlc.sabotage.net.CompleteBreakerRepairC2SPacket;
import dev.caecorthus.sparkdlc.sabotage.net.OpenBreakerRepairS2CPacket;
import dev.caecorthus.sparkdlc.sabotage.net.OpenEngineRepairS2CPacket;
import dev.caecorthus.sparkdlc.sabotage.net.SabotageStatusS2CPacket;
import dev.caecorthus.sparkdlc.sabotage.net.SyncBlackoutSightS2CPacket;
import dev.caecorthus.sparkdlc.sabotage.net.TriggerSabotageC2SPacket;
import dev.caecorthus.sparkdlc.sabotage.BreakerRepairService;
import dev.caecorthus.sparkdlc.sabotage.EngineRepairService;
import dev.caecorthus.sparkdlc.sabotage.JunctionBoxService;
import dev.caecorthus.sparkdlc.sabotage.SabotageService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * SparkStrength 网络包注册。
 */
public final class SparkStrengthPackets {
    private SparkStrengthPackets() {
    }

    public static void registerServer() {
        M67Packets.initialize();
        DronePackets.initialize();
        SpiritPossessionPackets.initialize();
        PayloadTypeRegistry.playC2S().register(NoisemakerGlowC2SPacket.ID, NoisemakerGlowC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(PhantomBackpackInvisibilityC2SPacket.ID, PhantomBackpackInvisibilityC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CoronerMorphC2SPacket.ID, CoronerMorphC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ProfessorRemoteFeedC2SPacket.ID, ProfessorRemoteFeedC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ReporterCommunicationC2SPacket.ID, ReporterCommunicationC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SelectDetectiveCaseC2SPacket.ID, SelectDetectiveCaseC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateDetectiveCaseNotesC2SPacket.ID, UpdateDetectiveCaseNotesC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SetDetectiveKillerGuessC2SPacket.ID, SetDetectiveKillerGuessC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SetDetectivePresumedKillerC2SPacket.ID, SetDetectivePresumedKillerC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(InterrogateDetectiveSuspectC2SPacket.ID, InterrogateDetectiveSuspectC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(DemonHunterSniffC2SPacket.ID, DemonHunterSniffC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(UndercoverMaskOffC2SPacket.ID, UndercoverMaskOffC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(VultureSuperCurseC2SPacket.ID, VultureSuperCurseC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(PartygoerMenuC2SPacket.ID, PartygoerMenuC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(PartygoerSummonC2SPacket.ID, PartygoerSummonC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(PartygoerSwallowC2SPacket.ID, PartygoerSwallowC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PartygoerMenuS2CPacket.ID, PartygoerMenuS2CPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestTabletSnapshotC2SPacket.ID, RequestTabletSnapshotC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SendTabletChatC2SPacket.ID, SendTabletChatC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CallTabletMeetingC2SPacket.ID, CallTabletMeetingC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CastTabletVoteC2SPacket.ID, CastTabletVoteC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ConfirmTabletVoteC2SPacket.ID, ConfirmTabletVoteC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ApproveSuspectRemovalC2SPacket.ID, ApproveSuspectRemovalC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SelectTabletChannelC2SPacket.ID, SelectTabletChannelC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(TimekeeperWatchModeC2SPacket.ID, TimekeeperWatchModeC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(TaotieHeadFireC2SPacket.ID, TaotieHeadFireC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenDetectiveFolderS2CPacket.ID, OpenDetectiveFolderS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenDetectiveInterrogationS2CPacket.ID, OpenDetectiveInterrogationS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(
                DetectiveInterrogationResultS2CPacket.ID,
                DetectiveInterrogationResultS2CPacket.CODEC
        );
        PayloadTypeRegistry.playC2S().register(SerialPistolShootC2SPayload.ID, SerialPistolShootC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BanditRevolverShootC2SPayload.ID, BanditRevolverShootC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BanditRevolverActionC2SPayload.ID, BanditRevolverActionC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenTabletScreenS2CPacket.ID, OpenTabletScreenS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(TabletChatNotifyS2CPacket.ID, TabletChatNotifyS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncTabletSnapshotS2CPacket.ID, SyncTabletSnapshotS2CPacket.CODEC);
        // Planted timed-bomb traps, sent to trap viewers only (TimedBombMarkerSync). / 已埋设的定时炸弹陷阱，只发给陷阱观察者。
        PayloadTypeRegistry.playS2C().register(TimedBombMarkersPayload.ID, TimedBombMarkersPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncBlackoutSightS2CPacket.ID, SyncBlackoutSightS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncKillerTeamEconomyS2CPacket.ID, SyncKillerTeamEconomyS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(
                SyncShadowJesterShowdownMusicS2CPacket.ID,
                SyncShadowJesterShowdownMusicS2CPacket.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(NoisemakerGlowC2SPacket.ID, (payload, context) ->
                NoisemakerGlowService.tryUseBackpackGlow(context.player(), payload.targetPlayer())
        );
        ServerPlayNetworking.registerGlobalReceiver(PhantomBackpackInvisibilityC2SPacket.ID, (payload, context) ->
                PhantomBackpackService.tryUseBackpackInvisibility(context.player(), payload.targetPlayer())
        );
        ServerPlayNetworking.registerGlobalReceiver(CoronerMorphC2SPacket.ID, (payload, context) ->
                CoronerService.selectDisguise(context.player(), payload.targetPlayer())
        );
        ServerPlayNetworking.registerGlobalReceiver(ProfessorRemoteFeedC2SPacket.ID, (payload, context) ->
                ProfessorSerumService.tryRemoteFeed(context.player(), payload.targetPlayer(), payload.serumType())
        );
        ServerPlayNetworking.registerGlobalReceiver(ReporterCommunicationC2SPacket.ID, (payload, context) ->
                ReporterCommunicationService.handle(payload, context.player())
        );
        // Detective folder edits: the service re-validates role, liveness and case ownership (client input is untrusted).
        // 侦探文件夹编辑：服务端重新校验身份、存活状态与案件归属，客户端输入不可信。
        ServerPlayNetworking.registerGlobalReceiver(SelectDetectiveCaseC2SPacket.ID,
                (payload, context) -> DetectiveCaseService.handleSelectCase(context.player(), payload.caseId()));
        ServerPlayNetworking.registerGlobalReceiver(UpdateDetectiveCaseNotesC2SPacket.ID,
                (payload, context) -> DetectiveCaseService.handleUpdateNotes(
                        context.player(),
                        payload.caseId(),
                        payload.notes()
                ));
        ServerPlayNetworking.registerGlobalReceiver(SetDetectiveKillerGuessC2SPacket.ID,
                (payload, context) -> DetectiveCaseService.handleSetKillerGuess(
                        context.player(),
                        payload.caseId(),
                        payload.roleId()
                ));
        ServerPlayNetworking.registerGlobalReceiver(SetDetectivePresumedKillerC2SPacket.ID,
                (payload, context) -> DetectiveCaseService.handleSetPresumedKiller(
                        context.player(),
                        payload.caseId(),
                        payload.playerUuid()
                ));
        ServerPlayNetworking.registerGlobalReceiver(InterrogateDetectiveSuspectC2SPacket.ID,
                (payload, context) -> DetectiveCaseService.handleInterrogate(context.player(), payload.playerUuid()));
        ServerPlayNetworking.registerGlobalReceiver(DemonHunterSniffC2SPacket.ID,
                (payload, context) -> DemonHunterSniffService.trySniff(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(UndercoverMaskOffC2SPacket.ID,
                (payload, context) -> UndercoverService.takeMaskOff(context.player()));
        // Runs on the server thread; the service re-checks round, role, liveness, skill locks and cooldown.
        // 在服务端主线程执行；服务层重新校验回合、职业、存活、技能封锁与冷却。
        ServerPlayNetworking.registerGlobalReceiver(VultureSuperCurseC2SPacket.ID,
                (payload, context) -> VultureSuperCurseService.tryCurse(context.player()));
        // Party Animal's Partygoer: picker request and summon; the service re-checks every gate.
        // 派对狂的派对客：选房请求与召唤；服务层重新校验所有门槛。
        ServerPlayNetworking.registerGlobalReceiver(PartygoerMenuC2SPacket.ID,
                (payload, context) -> PartygoerSkillService.openMenu(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(PartygoerSummonC2SPacket.ID,
                (payload, context) -> PartygoerSkillService.summon(context.player(), payload.spawnRoom(),
                        payload.destinationRoom()));
        // Taotie swallowing a Partygoer (NoellesRoles ability key); the service re-checks every gate.
        // 饕餮吞派对客（NoellesRoles 技能键）；服务层重新校验所有门槛。
        ServerPlayNetworking.registerGlobalReceiver(PartygoerSwallowC2SPacket.ID,
                (payload, context) -> PartygoerTaotieService.handleSwallow(context.player(), payload));
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
        ServerPlayNetworking.registerGlobalReceiver(TimekeeperWatchModeC2SPacket.ID,
                (payload, context) -> TimekeeperWatchService.switchMode(context.player(), payload.modeOrdinal()));
        // Empty request: the service re-validates role, round, liveness, locks, cooldown and the stomach.
        // 空请求：服务端重新校验身份、对局、存活、各类锁定、冷却与体内玩家。
        ServerPlayNetworking.registerGlobalReceiver(TaotieHeadFireC2SPacket.ID,
                (payload, context) -> TaotieHeadService.tryFire(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(SerialPistolShootC2SPayload.ID,
                SerialPistolShootC2SPayload::receive);
        ServerPlayNetworking.registerGlobalReceiver(BanditRevolverShootC2SPayload.ID,
                BanditRevolverShootC2SPayload::receive);
        ServerPlayNetworking.registerGlobalReceiver(BanditRevolverActionC2SPayload.ID,
                BanditRevolverActionC2SPayload::receive);
        registerSabotage();
    }

    private static void registerSabotage() {
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
