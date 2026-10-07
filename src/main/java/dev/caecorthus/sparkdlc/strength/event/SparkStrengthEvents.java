package dev.caecorthus.sparkdlc.strength.event;

import dev.caecorthus.sparkdlc.strength.component.detective.CriminologistPlayerComponent;
import dev.caecorthus.sparkdlc.strength.component.detective.CriminologistWorldComponent;
import dev.caecorthus.sparkdlc.strength.component.demonhunter.DemonHunterSniffPlayerComponent;
import dev.caecorthus.sparkdlc.strength.component.morphling.MorphBodyDisguiseWorldComponent;
import dev.caecorthus.sparkdlc.strength.component.noisemaker.NoisemakerGlowTargetComponent;
import dev.caecorthus.sparkdlc.strength.component.noisemaker.NoisemakerGlowUserComponent;
import dev.caecorthus.sparkdlc.strength.component.phantom.PhantomBackpackTargetComponent;
import dev.caecorthus.sparkdlc.strength.component.phantom.PhantomBackpackUserComponent;
import dev.caecorthus.sparkdlc.strength.component.professor.ProfessorSerumTargetComponent;
import dev.caecorthus.sparkdlc.strength.component.professor.ProfessorSerumUserComponent;
import dev.caecorthus.sparkdlc.strength.component.reporter.ReporterCommunicationComponent;
import dev.caecorthus.sparkdlc.strength.role.noisemaker.NoisemakerGlowService;
import dev.caecorthus.sparkdlc.strength.role.phantom.PhantomBackpackService;
import dev.caecorthus.sparkdlc.strength.role.attendant.AttendantFlashlightService;
import dev.caecorthus.sparkdlc.strength.role.bomber.BomberTrapService;
import dev.caecorthus.sparkdlc.strength.role.coroner.CoronerEconomyService;
import dev.caecorthus.sparkdlc.strength.role.coroner.CoronerEngineerService;
import dev.caecorthus.sparkdlc.strength.role.coroner.CoronerService;
import dev.caecorthus.sparkdlc.strength.role.coroner.CoronerShopService;
import dev.caecorthus.sparkdlc.strength.role.corruptcop.CorruptCopAbilityService;
import dev.caecorthus.sparkdlc.strength.role.corruptcop.CorruptCopFeatureService;
import dev.caecorthus.sparkdlc.strength.role.detective.CriminologistService;
import dev.caecorthus.sparkdlc.strength.role.demonhunter.DemonHunterSniffService;
import dev.caecorthus.sparkdlc.strength.role.economy.RoleEconomyService;
import dev.caecorthus.sparkdlc.strength.role.engineer.EngineerCaptureDeviceService;
import dev.caecorthus.sparkdlc.strength.role.engineer.EngineerPowerRestorationService;
import dev.caecorthus.sparkdlc.strength.role.engineer.EngineerShopService;
import dev.caecorthus.sparkdlc.strength.role.morphling.MorphlingService;
import dev.caecorthus.sparkdlc.strength.role.morphling.MorphlingShopService;
import dev.caecorthus.sparkdlc.strength.role.poisoner.PoisonerEconomyService;
import dev.caecorthus.sparkdlc.strength.role.professor.ProfessorSerumShopService;
import dev.caecorthus.sparkdlc.strength.role.recaller.RecallerEconomyService;
import dev.caecorthus.sparkdlc.strength.role.recaller.RecallerShopService;
import dev.caecorthus.sparkdlc.strength.role.reporter.ReporterCommunicationManager;
import dev.caecorthus.sparkdlc.strength.role.toxicologist.ToxicologistAntidoteService;
import dev.caecorthus.sparkdlc.strength.role.toxicologist.ToxicologistCapsuleShop;
import dev.caecorthus.sparkdlc.strength.role.attendant.FlashlightBlackoutService;
import dev.caecorthus.sparkdlc.strength.role.veteran.VeteranBlackoutService;
import dev.caecorthus.sparkdlc.strength.role.veteran.VeteranEconomyService;
import dev.caecorthus.sparkdlc.strength.role.veteran.VeteranKnifeService;
import dev.caecorthus.sparkdlc.strength.role.veteran.VeteranShopService;
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
        CorruptCopFeatureService.register();
        CoronerEngineerService.register();
        CoronerService.register();
        CoronerShopService.register();
        CriminologistService.register();
        FlashlightBlackoutService.register();
        VeteranBlackoutService.register();
        RoleEconomyService.register();
        EngineerPowerRestorationService.register();
        EngineerShopService.register();
        MorphlingService.register();
        MorphlingShopService.register();
        PoisonerEconomyService.register();
        ProfessorSerumShopService.register();
        ReporterCommunicationManager.register();
        RecallerShopService.register();
        ToxicologistAntidoteService.register();
        ToxicologistCapsuleShop.register();
        TabletShopService.register();
        VeteranShopService.register();
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
        ServerTickEvents.END_WORLD_TICK.register(VeteranBlackoutService::tick);

        RoleAssigned.EVENT.register((player, role) -> {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                CorruptCopAbilityService.reset(serverPlayer);
                CoronerService.assignForRole(serverPlayer, role);
                RoleEconomyService.assignForRole(serverPlayer, role);
                AttendantFlashlightService.assignForRole(serverPlayer, role);
                CriminologistService.assignForRole(serverPlayer, role);
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
            // Wathe 在死亡、重置玩家、新一局开始等场景会触发 ResetPlayer。
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
            CriminologistPlayerComponent.KEY.get(player).clearAll();
            DemonHunterSniffPlayerComponent.KEY.get(player).clearSniff();
            if (player instanceof ServerPlayerEntity serverPlayer) {
                CorruptCopAbilityService.reset(serverPlayer);
                CoronerService.clearPlayer(serverPlayer);
                EngineerCaptureDeviceService.clearPlayer(serverPlayer);
                MorphlingService.reset(serverPlayer);
                ToxicologistAntidoteService.clearPlayer(serverPlayer);
                VeteranKnifeService.reset(serverPlayer);
            }
        });

        KillPlayer.AFTER.register((victim, killer, deathReason) -> {
            // 大嗓门死亡后的“杀手发光 15 秒”是被动效果，不写入回放。
            NoisemakerGlowService.glowKillerWhenNoisemakerDies(victim, killer);
            CriminologistService.afterKill(victim, killer, deathReason);
            CoronerService.afterKill(victim);
            MorphlingService.afterKill(victim, killer, deathReason);
        });

        GameEvents.ON_FINISH_FINALIZE.register((world, gameComponent) -> {
            if (world instanceof ServerWorld serverWorld) {
                CriminologistWorldComponent.KEY.get(serverWorld).clearRoundState();
                MorphBodyDisguiseWorldComponent.KEY.get(serverWorld).clearRoundState();
                BomberTrapService.clearRoundState(serverWorld);
                EngineerCaptureDeviceService.clearRoundState(serverWorld);
                TabletStateService.clearRoundState(serverWorld);
                VeteranBlackoutService.clear(serverWorld);
                VeteranEconomyService.clearRoundState();
                for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                    CorruptCopAbilityService.reset(player);
                    CoronerService.clearPlayer(player);
                    CriminologistPlayerComponent.KEY.get(player).clearAll();
                    EngineerCaptureDeviceService.clearPlayer(player);
                    MorphlingService.reset(player);
                    PhantomBackpackService.clearPlayer(player);
                    ToxicologistAntidoteService.clearPlayer(player);
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
