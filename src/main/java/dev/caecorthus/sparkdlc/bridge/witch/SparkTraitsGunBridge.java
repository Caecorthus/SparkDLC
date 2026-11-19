package dev.caecorthus.sparkdlc.bridge.witch;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.SparkWitch;
import dev.caecorthus.sparkdlc.role.civilian.emma.EmmaGunService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.UUID;

/** Only the provider's public facade is named. SparkTraits is fused into SparkDLC, so the Emma protocol is always
 * complete and every call (and the shot-cycle listener) is direct instead of reflective.
 * 仅引用提供方公共门面。SparkTraits 已融合进 SparkDLC，因此 Emma 协议始终完整，所有调用（以及射击周期监听器）均为直接
 * 调用而非反射。 */
public final class SparkTraitsGunBridge {
    private static boolean initialized;

    private SparkTraitsGunBridge() { }

    public static synchronized void register() {
        if (initialized) return;
        initialized = true;
        SparkTraitsApi.registerTerminalDeathReason(SparkWitch.id("factor_backlash"));
        SparkTraitsApi.registerGunShotCycleListener(new EmmaGunListener());
    }

    public static boolean isRoleSkillBlocked(PlayerEntity player) {
        return SparkTraitsApi.isRoleSkillBlocked(player);
    }

    public static boolean deathWasIntercepted(PlayerEntity target) {
        return SparkTraitsApi.isLastStandDeathIntercepted(target);
    }

    public static boolean setExactCooldown(ServerPlayerEntity player, Item item, int ticks) {
        SparkTraitsApi.setExactItemCooldownRemaining(player, item, Math.max(0, ticks));
        return true;
    }

    /** Forwards Traits' accepted-shot cycle to Emma's own service. / 把 Traits 的有效射击周期转发给 Emma 自己的服务。 */
    private static final class EmmaGunListener implements SparkTraitsApi.GunShotCycleListener {
        @Override
        public void cycleStarted(ServerPlayerEntity shooter, UUID cycleId, Item weapon) {
            EmmaGunService.cycleStarted(shooter, cycleId, weapon);
        }

        @Override
        public Object beforeTargetKill(ServerPlayerEntity shooter, UUID cycleId, ServerPlayerEntity target) {
            return EmmaGunService.beforeTargetKill(shooter, cycleId, target);
        }

        @Override
        public void afterTargetKill(ServerPlayerEntity shooter, UUID cycleId, ServerPlayerEntity target, Object token) {
            EmmaGunService.afterTargetKill(shooter, cycleId, target, token);
        }

        @Override
        public void initialCooldownEstablished(ServerPlayerEntity shooter, UUID cycleId, Item weapon,
                                               int startTick, int endTick, int currentTick) {
            EmmaGunService.initialCooldownEstablished(shooter, cycleId, weapon, startTick, endTick, currentTick);
        }

        @Override
        public void cycleClosed(ServerPlayerEntity shooter, UUID cycleId) {
            EmmaGunService.cycleClosed(shooter, cycleId);
        }

        @Override
        public String toString() {
            return "SparkWitchEmmaGunListener";
        }
    }
}
