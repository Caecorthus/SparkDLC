package dev.caecorthus.sparkdlc.strength.event;

import dev.caecorthus.sparkdlc.strength.component.detective.DetectiveCasePlayerComponent;
import dev.caecorthus.sparkdlc.strength.component.demonhunter.DemonHunterSniffPlayerComponent;
import dev.caecorthus.sparkdlc.strength.component.morphling.MorphBodyDisguiseWorldComponent;
import dev.caecorthus.sparkdlc.strength.component.noisemaker.NoisemakerGlowTargetComponent;
import dev.caecorthus.sparkdlc.strength.component.noisemaker.NoisemakerGlowUserComponent;
import dev.caecorthus.sparkdlc.strength.component.phantom.PhantomBackpackTargetComponent;
import dev.caecorthus.sparkdlc.strength.component.phantom.PhantomBackpackUserComponent;
import dev.caecorthus.sparkdlc.strength.component.professor.ProfessorSerumTargetComponent;
import dev.caecorthus.sparkdlc.strength.component.professor.ProfessorSerumUserComponent;
import dev.caecorthus.sparkdlc.strength.item.m67.M67RoundService;
import dev.caecorthus.sparkdlc.strength.component.reporter.ReporterCommunicationComponent;
import dev.caecorthus.sparkdlc.strength.role.noisemaker.NoisemakerGlowService;
import dev.caecorthus.sparkdlc.strength.role.phantom.PhantomBackpackService;
import dev.caecorthus.sparkdlc.strength.role.attendant.AttendantFlashlightService;
import dev.caecorthus.sparkdlc.strength.role.attendant.DoorLogService;
import dev.caecorthus.sparkdlc.strength.role.bomber.drone.DroneCombatService;
import dev.caecorthus.sparkdlc.strength.role.bomber.drone.DronePilotService;
import dev.caecorthus.sparkdlc.strength.role.bomber.drone.DroneService;
import dev.caecorthus.sparkdlc.strength.role.bomber.BomberTrapService;
import dev.caecorthus.sparkdlc.strength.role.coroner.CoronerEconomyService;
import dev.caecorthus.sparkdlc.strength.role.coroner.CoronerEngineerService;
import dev.caecorthus.sparkdlc.strength.role.coroner.CoronerService;
import dev.caecorthus.sparkdlc.strength.role.coroner.CoronerShopService;
import dev.caecorthus.sparkdlc.strength.role.corruptcop.CorruptCopAbilityService;
import dev.caecorthus.sparkdlc.strength.role.corruptcop.CorruptCopFeatureService;
import dev.caecorthus.sparkdlc.strength.role.detective.DetectiveCaseService;
import dev.caecorthus.sparkdlc.strength.role.demonhunter.DemonHunterSniffService;
import dev.caecorthus.sparkdlc.strength.role.economy.RoleEconomyService;
import dev.caecorthus.sparkdlc.strength.role.economy.KillerTeamEconomyService;
import dev.caecorthus.sparkdlc.strength.role.engineer.EngineerCaptureDeviceService;
import dev.caecorthus.sparkdlc.strength.role.engineer.EngineerPowerRestorationService;
import dev.caecorthus.sparkdlc.strength.role.engineer.EngineerShopService;
import dev.caecorthus.sparkdlc.strength.role.morphling.MorphlingService;
import dev.caecorthus.sparkdlc.strength.role.morphling.MorphlingShopService;
import dev.caecorthus.sparkdlc.strength.role.pathogen.PathogenFeatureService;
import dev.caecorthus.sparkdlc.strength.role.poisoner.PoisonerEconomyService;
import dev.caecorthus.sparkdlc.strength.role.professor.ProfessorSerumShopService;
import dev.caecorthus.sparkdlc.strength.role.recaller.RecallerEconomyService;
import dev.caecorthus.sparkdlc.strength.role.recaller.RecallerShopService;
import dev.caecorthus.sparkdlc.strength.role.reporter.ReporterCommunicationManager;
import dev.caecorthus.sparkdlc.strength.role.toxicologist.ToxicologistAntidoteService;
import dev.caecorthus.sparkdlc.strength.role.toxicologist.ToxicologistBluePassiveService;
import dev.caecorthus.sparkdlc.strength.role.toxicologist.ToxicologistBlueVitriolService;
import dev.caecorthus.sparkdlc.strength.role.toxicologist.ToxicologistCapsuleShop;
import dev.caecorthus.sparkdlc.strength.role.attendant.FlashlightBlackoutService;
import dev.caecorthus.sparkdlc.strength.role.veteran.VeteranBlackoutService;
import dev.caecorthus.sparkdlc.strength.role.veteran.VeteranEconomyService;
import dev.caecorthus.sparkdlc.strength.role.veteran.VeteranKnifeService;
import dev.caecorthus.sparkdlc.strength.role.veteran.VeteranShopService;
import dev.caecorthus.sparkdlc.strength.role.vulture.VultureSkateboardService;
import dev.caecorthus.sparkdlc.strength.role.shadowjester.ShadowJesterShowdownService;
import dev.caecorthus.sparkdlc.strength.role.waiter.WaiterTaskRevealService;
import dev.caecorthus.sparkdlc.strength.role.timekeeper.TimekeeperWatchService;
import dev.caecorthus.sparkdlc.strength.component.timekeeper.TimekeeperWatchComponent;
import dev.caecorthus.sparkdlc.strength.tablet.TabletShopService;
import dev.caecorthus.sparkdlc.strength.tablet.TabletStateService;
import dev.doctor4t.wathe.api.event.GameEvents;
import dev.doctor4t.wathe.api.event.KillPlayer;
import dev.doctor4t.wathe.api.event.ResetPlayer;
import dev.doctor4t.wathe.api.event.RoleAssigned;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * 统一注册 SparkStrength 的服务端事件。
 */
public final class SparkStrengthEvents {
    private SparkStrengthEvents() {
    }

    public static void register() {
        M67RoundService.initialize();
        DroneService.register();
        DroneCombatService.register();
        DronePilotService.register();
        CorruptCopFeatureService.register();
        CoronerEngineerService.register();
        CoronerService.register();
        CoronerShopService.register();
        DetectiveCaseService.register();
        DoorLogService.register();
        FlashlightBlackoutService.register();
        VeteranBlackoutService.register();
        RoleEconomyService.register();
        KillerTeamEconomyService.register();
        EngineerPowerRestorationService.register();
        EngineerShopService.register();
        MorphlingService.register();
        MorphlingShopService.register();
        PathogenFeatureService.register();
        PoisonerEconomyService.register();
        ProfessorSerumShopService.register();
        ReporterCommunicationManager.register();
        RecallerShopService.register();
        ToxicologistAntidoteService.register();
        ToxicologistBluePassiveService.register();
        ToxicologistCapsuleShop.register();
        ToxicologistBlueVitriolService.register();
        TabletShopService.register();
        TabletStateService.register();
        VeteranShopService.register();
        VultureSkateboardService.register();
        // 老兵经济服务同时注册死亡前阵营快照和死亡后金币结算。
        VeteranEconomyService.register();
        ShadowJesterShowdownService.register();
        // 任务完成后记录 30 秒的服务员专属透视状态。
        WaiterTaskRevealService.register();
        TimekeeperWatchService.register();
        // 回溯者被动收入需要按世界 tick 定时结算，注册在服务端世界 tick 末尾。
        ServerTickEvents.END_WORLD_TICK.register(CoronerEconomyService::tick);
        ServerTickEvents.END_WORLD_TICK.register(CoronerService::tick);
        ServerTickEvents.END_WORLD_TICK.register(RecallerEconomyService::tick);
        ServerTickEvents.END_WORLD_TICK.register(TabletStateService::tick);
        // Mid-round tablet grant reconciliation (replaces a RoleAssigned-time grant; see TabletShopService#tick).
        // 局中平板补发对账（取代 RoleAssigned 时发放；见 TabletShopService#tick）。
        ServerTickEvents.END_WORLD_TICK.register(TabletShopService::tick);
        // Must stay on END_WORLD_TICK: it reads what SparkTraits' player component tick did earlier in the same world tick.
        // 必须挂在 END_WORLD_TICK：它读取同一世界 tick 内 SparkTraits 玩家组件刚做的结果。
        ServerTickEvents.END_WORLD_TICK.register(ToxicologistBluePassiveService::tick);
        ServerTickEvents.END_WORLD_TICK.register(VeteranBlackoutService::tick);

        RoleAssigned.EVENT.register((player, role) -> {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                CorruptCopAbilityService.reset(serverPlayer);
                CoronerService.assignForRole(serverPlayer, role);
                RoleEconomyService.assignForRole(serverPlayer, role);
                AttendantFlashlightService.assignForRole(serverPlayer, role);
                DetectiveCaseService.assignForRole(serverPlayer, role);
                DemonHunterSniffService.assignForRole(serverPlayer, role);
                MorphlingService.assignForRole(serverPlayer, role);
                PhantomBackpackService.assignForRole(serverPlayer, role);
                ToxicologistAntidoteService.clearPlayer(serverPlayer);
                VeteranKnifeService.assignForRole(serverPlayer, role);
                if (role == org.agmas.noellesroles.Noellesroles.TIMEKEEPER) {
                    TimekeeperWatchComponent.KEY.get(serverPlayer).reset();
                    serverPlayer.giveItemStack(dev.caecorthus.sparkdlc.strength.SparkStrengthItems.dyingWatch().getDefaultStack());
                }
            }
        });

        ResetPlayer.EVENT.register(player -> {
            // Wathe fires ResetPlayer in baseInitialize (round start) and in resetPlayer, which serverTick runs while no
            // game is running for players still inside the play area; never on death.
            // Wathe 只在开局 baseInitialize，以及非对局期间 serverTick 对仍在游戏区域内玩家调用的 resetPlayer 中触发 ResetPlayer；死亡时不会触发。
            // 这里把点亮冷却和目标倒计时都清掉，避免跨局残留。
            NoisemakerGlowUserComponent.KEY.get(player).reset();
            NoisemakerGlowTargetComponent.KEY.get(player).reset();
            PhantomBackpackUserComponent.KEY.get(player).reset();
            PhantomBackpackTargetComponent.KEY.get(player).reset();
            ProfessorSerumUserComponent.KEY.get(player).reset();
            ProfessorSerumTargetComponent.KEY.get(player).reset();
            ReporterCommunicationComponent.KEY.get(player).reset();
            if (player instanceof ServerPlayerEntity serverPlayer) {
                WaiterTaskRevealService.reset(serverPlayer);
                TimekeeperWatchComponent.KEY.get(serverPlayer).reset();
            }
            DetectiveCasePlayerComponent.KEY.get(player).clearAll();
            DemonHunterSniffPlayerComponent.KEY.get(player).clearSniff();
            if (player instanceof ServerPlayerEntity serverPlayer) {
                CorruptCopAbilityService.reset(serverPlayer);
                CoronerService.clearPlayer(serverPlayer);
                EngineerCaptureDeviceService.clearPlayer(serverPlayer);
                MorphlingService.reset(serverPlayer);
                ToxicologistAntidoteService.clearPlayer(serverPlayer);
                ToxicologistBluePassiveService.clearPlayer(serverPlayer);
                VeteranKnifeService.reset(serverPlayer);
            }
        });

        KillPlayer.AFTER.register((victim, killer, deathReason) -> {
            // 大嗓门死亡后的“杀手发光 15 秒”是被动效果，不写入回放。
            NoisemakerGlowService.glowKillerWhenNoisemakerDies(victim, killer);
            CoronerService.afterKill(victim);
            MorphlingService.afterKill(victim, killer, deathReason);
            ToxicologistBluePassiveService.clearPlayer(victim);
        });

        GameEvents.ON_FINISH_FINALIZE.register((world, gameComponent) -> {
            if (world instanceof ServerWorld serverWorld) {
                DetectiveCaseService.clearRoundState(serverWorld);
                MorphBodyDisguiseWorldComponent.KEY.get(serverWorld).clearRoundState();
                BomberTrapService.clearRoundState(serverWorld);
                EngineerCaptureDeviceService.clearRoundState(serverWorld);
                TabletStateService.clearRoundState(serverWorld);
                VeteranBlackoutService.clear(serverWorld);
                VeteranEconomyService.clearRoundState();
                for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                    CorruptCopAbilityService.reset(player);
                    CoronerService.clearPlayer(player);
                    DetectiveCasePlayerComponent.KEY.get(player).clearAll();
                    EngineerCaptureDeviceService.clearPlayer(player);
                    MorphlingService.reset(player);
                    PhantomBackpackService.clearPlayer(player);
                    ToxicologistAntidoteService.clearPlayer(player);
                    ToxicologistBluePassiveService.clearPlayer(player);
                    ProfessorSerumUserComponent.KEY.get(player).reset();
                    ProfessorSerumTargetComponent.KEY.get(player).reset();
                    ReporterCommunicationComponent.KEY.get(player).reset();
                    WaiterTaskRevealService.reset(player);
                    DemonHunterSniffService.clearPlayer(player);
                    VeteranKnifeService.reset(player);
                    TimekeeperWatchComponent.KEY.get(player).reset();
                }
            }
        });

        GameEvents.ON_FINISH_INITIALIZE.register((world, gameComponent) -> {
            if (world instanceof ServerWorld serverWorld) {
                MorphBodyDisguiseWorldComponent.KEY.get(serverWorld).clearRoundState();
                BomberTrapService.clearRoundState(serverWorld);
                EngineerCaptureDeviceService.clearRoundState(serverWorld);
                // Also clear at round start: an aborted round or restart must not carry tablet chat/suspects forward.
                // 开局时也清理：异常结束或重启的对局不能把平板聊天/嫌疑人带入下一局。
                TabletStateService.clearRoundState(serverWorld);
                // After the clear (it resets the per-round grant set): every tablet-eligible player gets one free tablet.
                // 必须在清理之后（清理会重置本局发放记录）：每名符合条件的玩家免费获得一台平板。
                TabletShopService.grantStarterTablets(serverWorld, gameComponent);
                // Crime-scene snapshots are round state; clear before granting kits so a new round starts empty.
                // 案发快照属于单局状态；先清空再发放侦探道具，保证新一局从空白开始。
                DetectiveCaseService.clearRoundState(serverWorld);
                DetectiveCaseService.grantStarterKits(serverWorld);
                VeteranEconomyService.clearRoundState();
                for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                    // 新一局初始化时清理上一局可能残留的服务员任务透视倒计时。
                    WaiterTaskRevealService.reset(player);
                    TimekeeperWatchComponent.KEY.get(player).reset();
                }
            }
        });
    }
}
