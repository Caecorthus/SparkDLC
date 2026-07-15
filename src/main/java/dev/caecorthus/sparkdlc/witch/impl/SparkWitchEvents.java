package dev.caecorthus.sparkdlc.witch.impl;

import dev.caecorthus.sparkdlc.witch.component.PerfumerPlayerComponent;
import dev.caecorthus.sparkdlc.witch.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.witch.component.WitchWorldComponent;
import dev.caecorthus.sparkdlc.witch.economy.WitchEconomyService;
import dev.caecorthus.sparkdlc.witch.item.ceremonialsword.CeremonialSwordCombatService;
import dev.caecorthus.sparkdlc.witch.item.ceremonialsword.CeremonialSwordDashService;
import dev.caecorthus.sparkdlc.witch.item.firepoker.FirePokerCombatService;
import dev.caecorthus.sparkdlc.witch.item.firepoker.FirePokerFallAttributionService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.apprentice.abilities.MightyForce.MightyForceCombatService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.orthopedist.OrthopedistSkillService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.perfumer.PerfumerEconomyService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.perfumer.PerfumerFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.perfumer.PerfumerRuntime;
import dev.caecorthus.sparkdlc.witch.roles.civilian.perfumer.PerfumerShopService;
import dev.caecorthus.sparkdlc.witch.roles.witch.grandwitch.GrandWitchActiveSkillService;
import dev.caecorthus.sparkdlc.witch.roles.witch.WitchFactionFeatureService;
import dev.caecorthus.sparkdlc.witch.mana.WitchManaService;
import dev.caecorthus.sparkdlc.witch.roles.neutral.murderouswitch.MurderousWitchFeature.MurderousWitchFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.piggod.PigGodEconomyService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.piggod.PigGodChaseRuntime;
import dev.caecorthus.sparkdlc.witch.roles.civilian.piggod.PigGodFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.saint.SaintFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.civilian.tarotreader.TarotReaderFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.killer.hunter.HunterFeatureService;
import dev.caecorthus.sparkdlc.witch.roles.killer.kidnapper.KidnapperDragLifecycle;
import dev.caecorthus.sparkdlc.witch.roles.killer.ninja.NinjaFeatureService;
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

        CeremonialSwordCombatService.register();
        CeremonialSwordDashService.register();
        MightyForceCombatService.register();
        FirePokerCombatService.register();
        WitchFactionFeatureService.register();
        MurderousWitchFeatureService.register();
        PigGodFeatureService.register();
        PigGodEconomyService.register();
        SaintFeatureService.register();
        PerfumerShopService.register();
        PerfumerFeatureService.register();
        PerfumerRuntime.register();
        PerfumerEconomyService.register();
        NinjaFeatureService.register();
        HunterFeatureService.register();
        OrthopedistSkillService.registerReplayFormatter();
        KidnapperDragLifecycle.register();
        TarotReaderFeatureService.register();
        RoleAssigned.EVENT.register((player, role) -> {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                PerfumerPlayerComponent.KEY.get(serverPlayer).clear();
                WitchSkillAssignmentService.assignForRole(serverPlayer, role);
                WitchManaService.assignForRole(serverPlayer, role);
                WitchFactionFeatureService.assignForRole(serverPlayer, role);
                MurderousWitchFeatureService.assignForRole(serverPlayer, role);
                PigGodEconomyService.assignForRole(serverPlayer, role);
                SaintFeatureService.assignForRole(serverPlayer, role);
                PerfumerEconomyService.assignForRole(serverPlayer, role);
                NinjaFeatureService.assignForRole(serverPlayer, role);
                OrthopedistSkillService.assignForRole(serverPlayer, role);
            }
        });
        TaskComplete.EVENT.register(WitchManaService::onTaskComplete);
        TaskComplete.EVENT.register((player, taskType) -> GrandWitchActiveSkillService.onTaskComplete(player));
        TaskComplete.EVENT.register((player, taskType) -> PigGodEconomyService.onTaskComplete(player));
        TaskComplete.EVENT.register((player, taskType) -> PerfumerEconomyService.onTaskComplete(player));
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
            PerfumerPlayerComponent.KEY.get(player).clear();
            if (player instanceof ServerPlayerEntity serverPlayer) {
                OrthopedistSkillService.clearPlayer(serverPlayer);
            }
        });
        GameEvents.ON_FINISH_FINALIZE.register((world, gameComponent) -> {
            if (world instanceof ServerWorld serverWorld) {
                // Round end clears only SparkWitch runtime state; role maps and other mods remain owned by wathe.
                // 回合结束只清理 SparkWitch 运行态，身份表和其他模组状态仍由 wathe 自己管理。
                WitchWorldComponent.KEY.get(serverWorld).clearRoundState();
                FirePokerFallAttributionService.clearAll();
                for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                    WitchFactionFeatureService.clearPlayerRuntime(player);
                    WitchPlayerComponent.KEY.get(player).clear();
                    PerfumerPlayerComponent.KEY.get(player).clear();
                    OrthopedistSkillService.clearPlayer(player);
                }
            }
        });
    }
}
