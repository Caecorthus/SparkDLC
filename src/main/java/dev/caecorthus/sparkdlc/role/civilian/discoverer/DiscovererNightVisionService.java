package dev.caecorthus.sparkdlc.role.civilian.discoverer;

import dev.caecorthus.sparkdlc.net.DiscovererNightVisionC2SPacket;
import dev.doctor4t.wathe.api.event.ResetPlayer;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.LightType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 探索者's Night Vision key (owner 2026-11-26): only works where the packed light level — skylight included — is at or
 * below {@link DiscovererRules#NIGHT_VISION_MAX_LIGHT}; refuses silently otherwise, except for a short action-bar
 * hint. The first activation of the round takes 60 s, every later one 75 s. Blackouts are not immunised against: the
 * effect is plain Night Vision, which Wathe's blackout clears like any other.
 * 探索者的夜视键（所有者 2026-11-26）：只有总亮度（含天光）不高于 {@link DiscovererRules#NIGHT_VISION_MAX_LIGHT} 时才生效；
 * 否则静默拒绝，只在动作栏给一句提示。本局第一次 60 秒冷却，之后每次 75 秒。不免疫停电：效果就是普通夜视，
 * Wathe 的停电会像清除其它效果一样清掉它。
 */
public final class DiscovererNightVisionService {

    private static final Map<UUID, Long> READY_AT = new HashMap<>();
    private static final Map<UUID, Boolean> USED = new HashMap<>();
    private static boolean registered;

    private DiscovererNightVisionService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        PayloadTypeRegistry.playC2S().register(DiscovererNightVisionC2SPacket.ID, DiscovererNightVisionC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DiscovererNightVisionC2SPacket.ID, (payload, context) ->
                context.server().execute(() -> tryActivate(context.player())));
        ResetPlayer.EVENT.register(player -> {
            READY_AT.remove(player.getUuid());
            USED.remove(player.getUuid());
        });
    }

    private static void tryActivate(ServerPlayerEntity player) {
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getServerWorld());
        if (!DiscovererRules.isDiscoverer(game.getRole(player))) {
            return;
        }
        long now = player.getServerWorld().getTime();
        long readyAt = READY_AT.getOrDefault(player.getUuid(), 0L);
        if (readyAt > now) {
            player.sendMessage(Text.translatable("message.sparkdlc.discoverer.night_vision.cooldown",
                    (readyAt - now + 19) / 20), true);
            return;
        }
        if (!isDarkEnough(player)) {
            player.sendMessage(Text.translatable("message.sparkdlc.discoverer.night_vision.too_bright"), true);
            return;
        }
        boolean first = USED.put(player.getUuid(), Boolean.TRUE) == null;
        READY_AT.put(player.getUuid(), now + (first
                ? DiscovererRules.NIGHT_VISION_INITIAL_COOLDOWN_TICKS
                : DiscovererRules.NIGHT_VISION_COOLDOWN_TICKS));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,
                DiscovererRules.NIGHT_VISION_TICKS, 0, true, false, true));
        player.sendMessage(Text.translatable("message.sparkdlc.discoverer.night_vision.granted"), true);
    }

    /** Packed light, skylight included, at the player's feet. / 玩家脚下的总亮度（含天光）。 */
    private static boolean isDarkEnough(ServerPlayerEntity player) {
        var world = player.getServerWorld();
        var pos = player.getBlockPos();
        int light = Math.max(world.getLightLevel(LightType.BLOCK, pos), world.getLightLevel(LightType.SKY, pos));
        return light <= DiscovererRules.NIGHT_VISION_MAX_LIGHT;
    }
}
