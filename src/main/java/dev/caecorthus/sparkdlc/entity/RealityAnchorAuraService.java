package dev.caecorthus.sparkdlc.entity;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * Night vision granted by placed reality anchors (owner 2026-11-27): players within 5 blocks get one minute of night
 * vision, refreshed every 20 ticks while any anchor is in range. Every 20 ticks the service re-checks each player it
 * granted the effect to and removes it once they left every anchor or their anchor was destroyed. Only effects this
 * service granted are removed, so night vision from another source is never stripped.
 * 现实稳定锚给予的夜视（所有者 2026-11-27）：5 格内的玩家获得一分钟夜视，范围内有锚时每 20 刻刷新。服务每 20 刻复查
 * 自己发过夜视的玩家，一旦离开所有锚或锚被摧毁就移除该效果。只移除本服务发放的效果，其他来源的夜视不会被剥掉。
 */
public final class RealityAnchorAuraService {
    /** Night-vision radius in blocks. / 夜视半径（格）。 */
    static final double NIGHT_VISION_RADIUS = 5.0D;
    /** One minute of night vision. / 一分钟夜视。 */
    private static final int NIGHT_VISION_TICKS = 20 * 60;
    /** Check and refresh interval. / 检测与刷新间隔。 */
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final Set<UUID> GRANTED = new HashSet<>();
    private static boolean registered;

    private RealityAnchorAuraService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(RealityAnchorAuraService::tick);
    }

    /**
     * Grants or refreshes the anchor night vision for one player. A player who already has night vision from another
     * source is left completely alone.
     * 给一名玩家发放或续上锚的夜视。已有其他来源夜视的玩家完全不处理。
     */
    static void grant(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        if (!GRANTED.contains(id) && player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            return;
        }
        GRANTED.add(id);
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION, NIGHT_VISION_TICKS, 0, true, false, false));
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        Iterator<UUID> iterator = GRANTED.iterator();
        while (iterator.hasNext()) {
            UUID id = iterator.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(id);
            if (player == null) {
                iterator.remove();
                continue;
            }
            if (!nearAnchor(player)) {
                player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                iterator.remove();
            }
        }
    }

    /** Whether any anchor is within the night-vision radius of the player. / 玩家附近是否还有锚在夜视半径内。 */
    private static boolean nearAnchor(ServerPlayerEntity player) {
        return !player.getServerWorld().getEntitiesByClass(
                RealityAnchorEntity.class,
                player.getBoundingBox().expand(NIGHT_VISION_RADIUS),
                anchor -> anchor.squaredDistanceTo(player) <= NIGHT_VISION_RADIUS * NIGHT_VISION_RADIUS
        ).isEmpty();
    }
}
