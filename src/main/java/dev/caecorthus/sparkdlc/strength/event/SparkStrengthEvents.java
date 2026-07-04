package dev.caecorthus.sparkdlc.strength.event;

import dev.caecorthus.sparkdlc.strength.component.detective.CriminologistPlayerComponent;
import dev.caecorthus.sparkdlc.strength.component.detective.CriminologistWorldComponent;
import dev.caecorthus.sparkdlc.strength.component.noisemaker.NoisemakerGlowTargetComponent;
import dev.caecorthus.sparkdlc.strength.component.noisemaker.NoisemakerGlowUserComponent;
import dev.caecorthus.sparkdlc.strength.noisemaker.NoisemakerGlowService;
import dev.caecorthus.sparkdlc.strength.role.attendant.AttendantFlashlightService;
import dev.caecorthus.sparkdlc.strength.role.corruptcop.CorruptCopFeatureService;
import dev.caecorthus.sparkdlc.strength.role.detective.CriminologistService;
import dev.caecorthus.sparkdlc.strength.role.economy.RoleEconomyService;
import dev.caecorthus.sparkdlc.strength.role.toxicologist.ToxicologistCapsuleShop;
import dev.caecorthus.sparkdlc.strength.role.attendant.FlashlightBlackoutService;
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
        CriminologistService.register();
        FlashlightBlackoutService.register();
        RoleEconomyService.register();
        ToxicologistCapsuleShop.register();
        TabletShopService.register();
        ServerTickEvents.END_WORLD_TICK.register(TabletStateService::tick);

        RoleAssigned.EVENT.register((player, role) -> {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                RoleEconomyService.assignForRole(serverPlayer, role);
                AttendantFlashlightService.assignForRole(serverPlayer, role);
                CriminologistService.assignForRole(serverPlayer, role);
            }
        });

        ResetPlayer.EVENT.register(player -> {
            // Wathe 在死亡、重置玩家、新一局开始等场景会触发 ResetPlayer。
            // 这里把点亮冷却和目标倒计时都清掉，避免跨局残留。
            NoisemakerGlowUserComponent.KEY.get(player).reset();
            NoisemakerGlowTargetComponent.KEY.get(player).reset();
            CriminologistPlayerComponent.KEY.get(player).clearAll();
        });

        KillPlayer.AFTER.register((victim, killer, deathReason) -> {
            // 大嗓门死亡后的“杀手发光 15 秒”是被动效果，不写入回放。
            NoisemakerGlowService.glowKillerWhenNoisemakerDies(victim, killer);
            CriminologistService.afterKill(victim, killer, deathReason);
        });

        GameEvents.ON_FINISH_FINALIZE.register((world, gameComponent) -> {
            if (world instanceof ServerWorld serverWorld) {
                CriminologistWorldComponent.KEY.get(serverWorld).clearRoundState();
                TabletStateService.clearRoundState(serverWorld);
                for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                    CriminologistPlayerComponent.KEY.get(player).clearAll();
                }
            }
        });
    }
}
