package dev.caecorthus.sparkdlc.role.neutral.taotie;

import dev.caecorthus.sparkdlc.cooldown.api.ForcedCooldowns;
import dev.caecorthus.sparkdlc.cooldown.api.FunctionRoleSkillCooldownStore;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadDazeComponent;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadPlayerComponent;
import dev.doctor4t.wathe.api.event.GameEvents;
import dev.doctor4t.wathe.api.event.KillPlayer;
import dev.doctor4t.wathe.api.event.ResetPlayer;
import dev.doctor4t.wathe.api.event.RoleAssigned;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.OptionalInt;

/**
 * Taotie head registration, called once from {@code RoleKitEvents} (main initializer, so the daze guards also run on
 * the client): the daze interaction lock, the forced-cooldown store ({@code ForcedCooldowns}) and the per-round
 * cleanup. The packet, entity, components, payload guard and replay line are wired in their own registries.
 * 饕餮头颅注册入口，由 {@code RoleKitEvents} 调用一次（主初始化器，因此眩晕守卫在客户端也会运行）：眩晕交互锁、强制冷却
 * 存储（{@code ForcedCooldowns}）与单局清理。数据包、实体、组件、数据包拦截与回放文本在各自的注册处接入。
 */
public final class TaotieHeadFeatureService {
    private static boolean registered;

    private TaotieHeadFeatureService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        TaotieHeadDazeGuards.register();
        // Forced-cooldown seam: forced-cooldown features may floor/extend the head cooldown. Nominal = the tier for the
        // current living stomach (60 s when empty).
        // 强制冷却接缝：强制冷却功能可为头颅冷却设下限或延长；标称值为当前体内存活人数对应的档位（体内为空时 60 秒）。
        ForcedCooldowns.registerRoleSkillStore(new FunctionRoleSkillCooldownStore(
                TaotieHeadRules.COOLDOWN_STORE_ID,
                TaotieHeadService::isTaotie,
                player -> TaotieHeadPlayerComponent.KEY.get(player).getCooldownTicks(),
                player -> OptionalInt.of(TaotieHeadRules.cooldownTicksFor(
                        TaotieHeadService.livingSwallowed(player).size())),
                (player, ticks) -> TaotieHeadPlayerComponent.KEY.get(player).setCooldownTicks(ticks)));

        // No round-start cooldown: every role assignment starts ready. / 无开局冷却：每次分配身份都从就绪开始。
        RoleAssigned.EVENT.register((player, role) -> TaotieHeadPlayerComponent.KEY.get(player).reset());
        ResetPlayer.EVENT.register(TaotieHeadFeatureService::clearPlayer);
        KillPlayer.AFTER.register((victim, killer, deathReason) -> TaotieHeadDaze.clear(victim));
        GameEvents.ON_FINISH_INITIALIZE.register((world, game) -> clearWorld(world));
        GameEvents.ON_FINISH_FINALIZE.register((world, game) -> clearWorld(world));
    }

    private static void clearPlayer(PlayerEntity player) {
        TaotieHeadPlayerComponent.KEY.get(player).reset();
        TaotieHeadDazeComponent.KEY.get(player).clear();
    }

    private static void clearWorld(Object world) {
        if (world instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                clearPlayer(player);
            }
        }
    }
}
