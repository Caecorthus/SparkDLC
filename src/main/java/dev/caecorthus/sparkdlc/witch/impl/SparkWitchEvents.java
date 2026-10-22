package dev.caecorthus.sparkdlc.witch.impl;

import dev.caecorthus.sparkdlc.witch.roles.killer.witchmaiden.wither.MiniWitherFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.usec.UsecFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.witch.potiongunner.PotionGunnerFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.emma.EmmaGunService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.emma.EmmaLifecycle;
import dev.caecorthus.sparkdlc.witch.compat.SparkTraitsWraithBridge;
import dev.caecorthus.sparkdlc.witch.component.PerfumerPlayerComponent;
import dev.caecorthus.sparkdlc.witch.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.witch.component.WitchWorldComponent;
import dev.caecorthus.sparkdlc.witch.economy.WitchEconomyService;
import dev.caecorthus.sparkdlc.witch.item.ceremonialsword.CeremonialSwordCombatService;
import dev.caecorthus.sparkdlc.witch.item.ceremonialsword.CeremonialSwordDashService;
import dev.caecorthus.sparkdlc.witch.item.firepoker.FirePokerCombatService;
import dev.caecorthus.sparkdlc.witch.item.firepoker.FirePokerFallAttributionService;
import dev.caecorthus.sparkdlc.witch.item.tofana.TofanaProtectionService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.apprentice.ApprenticeFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.apprentice.abilities.MightyForce.MightyForceCombatService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.blind.BlindFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.controlexpert.ControlExpertFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.fisher.FisherFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.seeker.SeekerFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.judge.JudgeRuntime;
import dev.caecorthus.sparkdlc.witch.roles.civilian.orthopedist.OrthopedistSkillService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.guardianangel.GuardianAngelFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.guardianangel.GuardianAngelRules;
import dev.caecorthus.sparkdlc.witch.roles.civilian.vendetta.VendettaDisconnectService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.vendetta.VendettaInteractionService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.vendetta.VendettaReplayService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.vendetta.VendettaTerminalService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.perfumer.PerfumerEconomyService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.perfumer.PerfumerFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.perfumer.PerfumerRuntime;
import dev.caecorthus.sparkdlc.witch.roles.civilian.perfumer.PerfumerShopService;
import dev.caecorthus.sparkdlc.witch.roles.witch.grandwitch.GrandWitchActiveSkillService;
import dev.caecorthus.sparkdlc.witch.roles.witch.grandwitch.GrandWitchFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.witch.WitchFactionFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.witch.abysslistener.AbyssListenerFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.witch.bewitched.BewitchedFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.witch.bewitched.BewitchedPromotionService;
import dev.caecorthus.sparkdlc.witch.mana.WitchManaService;
import dev.caecorthus.sparkdlc.witch.roles.neutral.murderouswitch.MurderousWitchFeature.MurderousWitchFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.neutral.fiend.FiendFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.neutral.insider.InsiderFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.witch.riftwalker.RiftwalkerFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.piggod.PigGodEconomyService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.piggod.PigGodChaseRuntime;
import dev.caecorthus.sparkdlc.witch.roles.civilian.piggod.PigGodFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.prophet.ProphetDeathLedger;
import dev.caecorthus.sparkdlc.witch.roles.civilian.prophet.ProphetProphecyService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.prophet.ProphetEconomyService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.prophet.ProphetRuntime;
import dev.caecorthus.sparkdlc.witch.roles.civilian.saint.SaintEconomyService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.saint.SaintFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.tarotreader.TarotReaderFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.windspirit.WindSpiritFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.killer.bellringer.BellRingerFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.killer.blackraven.BlackRavenFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.killer.blackraven.crow.BlackRavenCrowFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.killer.blackraven.crow.BlackRavenCrowWorldFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.killer.hunter.HunterFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.killer.kidnapper.KidnapperDragLifecycle;
import dev.caecorthus.sparkdlc.witch.roles.killer.kidnapper.KidnapperKnockoutService;
import dev.caecorthus.sparkdlc.witch.roles.killer.ninja.NinjaFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.killer.saboteur.SaboteurFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.killer.timestealer.TimeStealerFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.killer.witchmaiden.FocusedFootstepsRuntime;
import dev.caecorthus.sparkdlc.witch.roles.killer.witchmaiden.PoisonApplePlateService;
import dev.caecorthus.sparkdlc.witch.roles.killer.witchmaiden.WitchMaidenShopService;
import dev.caecorthus.sparkdlc.witch.roles.killer.magician.MagicianPlaybackManager;
import dev.caecorthus.sparkdlc.witch.roles.killer.magician.MagicianPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.killer.magician.MagicianReplayEvents;
import dev.caecorthus.sparkdlc.witch.roles.killer.magician.MagicianPuppetAttackHandlers;
import dev.caecorthus.sparkdlc.witch.roles.killer.magician.MagicianDecoyBodies;
import dev.caecorthus.sparkdlc.witch.roles.killer.magician.MagicianRoster;
import dev.caecorthus.sparkdlc.witch.roles.special.wraith.WraithStateService;
import dev.caecorthus.sparkdlc.witch.roles.special.wraith.conversion.WraithConversion;
import dev.caecorthus.sparkdlc.witch.roles.special.wraith.runtime.WraithLifecycle;
import dev.caecorthus.sparkdlc.witch.skill.WitchSkillAssignmentService;
import dev.doctor4t.wathe.api.event.GameEvents;
import dev.doctor4t.wathe.api.event.KillPlayer;
import dev.doctor4t.wathe.api.event.ResetPlayer;
import dev.doctor4t.wathe.api.event.RoleAssigned;
import dev.doctor4t.wathe.api.event.TaskComplete;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class SparkWitchEvents {
    private static boolean registered;

    private SparkWitchEvents() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;

        JudgeRuntime.register();
        CeremonialSwordCombatService.register();
        CeremonialSwordDashService.register();
        GrandWitchFeatureService.register();
        EmmaLifecycle.register();
        EmmaGunService.register();
        MightyForceCombatService.register();
        FirePokerCombatService.register();
        TofanaProtectionService.register();
        WitchFactionFeatureService.register();
        MurderousWitchFeatureService.register();
        PigGodFeatureService.register();
        PigGodEconomyService.register();
        ProphetRuntime.register();
        ProphetDeathLedger.register();
        ProphetProphecyService.register();
        ProphetEconomyService.register();
        SaintFeatureService.register();
        PerfumerShopService.register();
        PerfumerFeatureService.register();
        PerfumerRuntime.register();
        PerfumerEconomyService.register();
        NinjaFeatureService.register();
        HunterFeatureService.register();
        OrthopedistSkillService.registerReplayFormatter();
        GuardianAngelFeatureService.register();
        VendettaInteractionService.register();
        VendettaDisconnectService.register();
        VendettaReplayService.register();
        VendettaTerminalService.register();
        KidnapperKnockoutService.register();
        KidnapperDragLifecycle.register();
        TarotReaderFeatureService.register();
        BlackRavenFeatureService.register();
        BlackRavenCrowFeatureService.register();
        BlackRavenCrowWorldFeatureService.register();
        BellRingerFeatureService.register();
        TimeStealerFeatureService.register();
        WindSpiritFeatureService.register();
        SaboteurFeatureService.register();
        FocusedFootstepsRuntime.register();
        WitchMaidenShopService.register();
        MiniWitherFeatureService.register();
        WraithLifecycle.register();
        ControlExpertFeatureService.register();
        SeekerFeatureService.register();
        FisherFeatureService.register();
        FiendFeatureService.register();
        InsiderFeatureService.register();
        BlindFeatureService.register();
        AbyssListenerFeatureService.register();
        PotionGunnerFeatureService.register();
        RiftwalkerFeatureService.register();
        BewitchedFeatureService.register();
        ApprenticeFeatureService.register();
        MagicianPlaybackManager.init();
        MagicianReplayEvents.register();
        MagicianPuppetAttackHandlers.register();
        RoleAssigned.EVENT.register((player, role) -> {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                PerfumerPlayerComponent.KEY.get(serverPlayer).clear();
                if (role != null && dev.caecorthus.sparkdlc.witch.SparkWitchRoles.MAGICIAN_ID.equals(role.identifier())) {
                    // One sync with the final state instead of one per write. / 只用最终状态同步一次，而不是每次写入各同步一次。
                    MagicianPlayerComponent magician = MagicianPlayerComponent.KEY.get(serverPlayer);
                    magician.syncOnceAfter(() -> {
                        magician.reset();
                        magician.assignInitialCooldown();
                        // Mid-round assignments get the roster now; round start re-captures it once every role is dealt.
                        // 回合中途分配时立即记录名单；回合开始时会在全部身份发放后重新记录。
                        MagicianRoster.capture(serverPlayer);
                    });
                }
                ProphetRuntime.assignForRole(serverPlayer, role);
                ProphetEconomyService.assignForRole(serverPlayer, role);
                WitchSkillAssignmentService.assignForRole(serverPlayer, role);
                WitchManaService.assignForRole(serverPlayer, role);
                WitchFactionFeatureService.assignForRole(serverPlayer, role);
                MurderousWitchFeatureService.assignForRole(serverPlayer, role);
                PigGodEconomyService.assignForRole(serverPlayer, role);
                SaintFeatureService.assignForRole(serverPlayer, role);
                PerfumerEconomyService.assignForRole(serverPlayer, role);
                NinjaFeatureService.assignForRole(serverPlayer, role);
                OrthopedistSkillService.assignForRole(serverPlayer, role);
                if (!GuardianAngelRules.isGuardianAngel(role)) {
                    GuardianAngelFeatureService.detachPlayer(serverPlayer);
                }
            }
        });
        TaskComplete.EVENT.register(WitchManaService::onTaskComplete);
        TaskComplete.EVENT.register((player, taskType) -> GrandWitchActiveSkillService.onTaskComplete(player));
        TaskComplete.EVENT.register((player, taskType) -> WitchEconomyService.onTaskComplete(player));
        // Bewitched promotion progress (C3): counts and enqueues only; the role changes at END_SERVER_TICK.
        // 魔化使晋升进度（C3）：只计数与入队；身份在 END_SERVER_TICK 变更。
        TaskComplete.EVENT.register((player, taskType) -> BewitchedPromotionService.onTaskComplete(player));
        TaskComplete.EVENT.register((player, taskType) -> PigGodEconomyService.onTaskComplete(player));
        TaskComplete.EVENT.register((player, taskType) -> ProphetEconomyService.onTaskComplete(player));
        TaskComplete.EVENT.register((player, taskType) -> SaintEconomyService.onTaskComplete(player));
        TaskComplete.EVENT.register((player, taskType) -> PerfumerEconomyService.onTaskComplete(player));
        KillPlayer.AFTER.register(WraithConversion::afterKill);
        KillPlayer.AFTER.register(WitchManaService::afterKill);
        KillPlayer.AFTER.register(WitchEconomyService::afterKill);
        KillPlayer.AFTER.register((victim, killer, deathReason) -> {
            WitchFactionFeatureService.clearPlayerRuntime(victim);
            FirePokerFallAttributionService.clearPlayer(victim);
            PigGodChaseRuntime.clear(victim, WitchPlayerComponent.KEY.get(victim));
            PerfumerPlayerComponent.KEY.get(victim).stopCologne();
            OrthopedistSkillService.clearPlayer(victim);
        });
        ResetPlayer.EVENT.register(player -> {
            WitchFactionFeatureService.clearPlayerRuntime(player);
            FirePokerFallAttributionService.clearPlayer(player);
            WitchPlayerComponent.KEY.get(player).clear();
            MagicianPlayerComponent.KEY.get(player).reset();
            PerfumerPlayerComponent.KEY.get(player).clear();
            if (player instanceof ServerPlayerEntity serverPlayer) {
                OrthopedistSkillService.clearPlayer(serverPlayer);
                GuardianAngelFeatureService.clearRoundPlayer(serverPlayer);
                if (WraithStateService.isActive(serverPlayer)) {
                    // SparkTraits observes active Wraith state while dispatching terminal trait removals.
                    // SparkTraits 分发终局天赋移除事件时必须仍能观察到激活的冤魂状态。
                    SparkTraitsWraithBridge.clear(serverPlayer, true);
                }
                WraithLifecycle.clearPlayer(serverPlayer);
            }
        });
        GameEvents.ON_FINISH_INITIALIZE.register((world, gameComponent) -> {
            if (world instanceof ServerWorld serverWorld) {
                WraithConversion.beginRound(serverWorld, gameComponent.getAllPlayers().size());
                MagicianRoster.captureAll(serverWorld);
            }
        });
        GameEvents.ON_FINISH_FINALIZE.register((world, gameComponent) -> {
            if (world instanceof ServerWorld serverWorld) {
                // Round end clears only SparkWitch runtime state; role maps and other mods remain owned by wathe.
                // 回合结束只清理 SparkWitch 运行态，身份表和其他模组状态仍由 wathe 自己管理。
                WitchWorldComponent.KEY.get(serverWorld).clearRoundState();
                FirePokerFallAttributionService.clearAll();
                WraithLifecycle.clearRoundState(serverWorld);
                PoisonApplePlateService.clearLoadedPlates();
                MagicianPlaybackManager.clearAll();
                MagicianDecoyBodies.clear();
                for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                    WitchFactionFeatureService.clearPlayerRuntime(player);
                    WitchPlayerComponent.KEY.get(player).clear();
                    MagicianPlayerComponent.KEY.get(player).reset();
                    PerfumerPlayerComponent.KEY.get(player).clear();
                    OrthopedistSkillService.clearPlayer(player);
                    GuardianAngelFeatureService.clearRoundPlayer(player);
                }
            }
        });
        // USEC (police variant): appended last so every existing listener keeps its order; the hub fixes the order of
        // the USEC services. / USEC（警察变体）：追加在最后，使所有既有监听器保持顺序；USEC 各服务的顺序由注册中心固定。
        UsecFeatureService.register();
    }
}
