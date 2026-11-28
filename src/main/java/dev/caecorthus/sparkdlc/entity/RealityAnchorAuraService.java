package dev.caecorthus.sparkdlc.entity;

import dev.caecorthus.sparkdlc.util.ServerDisconnectCleanup;
import dev.doctor4t.wathe.api.event.GameEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Night vision granted by placed reality anchors (owner 2026-11-27): players within 5 blocks get one minute of night
 * vision, refreshed every 20 ticks while any anchor is in range, and lose it within 20 ticks once they left every
 * anchor or their anchor is gone. Also wires the round-end sweep of the anchors themselves.
 * 现实稳定锚给予的夜视（所有者 2026-11-27）：5 格内的玩家获得一分钟夜视，范围内有锚时每 20 刻刷新；离开所有锚或锚消失后
 * 20 刻内失去。同时负责接入锚本身的回合结束清理。
 *
 * <p>Night vision has many other sources (blackout, Niko, the Savior, Wind Spirit, traits, potions), so the service
 * only ever touches an effect that is provably its own. It remembers when its last grant runs out and what that grant
 * looks like (ambient, no particles, no icon, amplifier 0), like the Coroner's and Niko's owned night vision do. A
 * player who already has night vision is left alone. If the effect on a tracked player stops matching the record
 * (another source lengthened it or changed its flags), the service forgets the player and never removes it: a longer
 * or foreign effect is neither shortened nor stripped, at the price of the anchor's own effect running out by itself
 * (60 s at most) in that case. The grant's flags differ from the mod's other night vision (not ambient, icon shown) on
 * purpose: vanilla copies a later grant's flags onto the running effect, so any other source leaves a visible trace
 * even when its effect is shorter and would otherwise change nothing.
 * <p>夜视还有许多其他来源（熄灯、Niko、救世主、风精灵、词条、药水），因此本服务只会触碰能证明属于自己的效果。它记住自己最后
 * 一次发放何时到期以及该效果的特征（环境效果、无粒子、无图标、等级 0），与验尸官和 Niko 的自有夜视做法相同。已有夜视的玩家完全
 * 不处理。若被跟踪玩家身上的效果与记录不再吻合（其他来源延长了它或改变了它的标志），本服务就忘掉该玩家且绝不移除：更长的或
 * 来自别处的效果既不会被缩短也不会被剥掉，代价是这种情况下锚自己的效果会自行到期（最多 60 秒）。发放时的标志有意与本模组其他
 * 夜视（非环境效果、显示图标）不同：原版会把后来发放的效果的标志复制到正在生效的效果上，因此即使其他来源的效果更短、本来不会
 * 改变任何东西，也会留下可见的痕迹。
 */
public final class RealityAnchorAuraService {
    /** Night-vision radius in blocks. / 夜视半径（格）。 */
    static final double NIGHT_VISION_RADIUS = 5.0D;
    /** One minute of night vision. / 一分钟夜视。 */
    static final int NIGHT_VISION_TICKS = 20 * 60;
    /** Check and refresh interval. / 检测与刷新间隔。 */
    private static final int CHECK_INTERVAL_TICKS = 20;
    /** Slack between the server tick counter and the effect's own countdown. / 服务器刻计数与效果自身倒计时之间的容差。 */
    static final int OWNED_TOLERANCE_TICKS = 5;
    /** Player -> server tick at which the anchor's own grant runs out. / 玩家 -> 锚自己发放的夜视到期的服务器刻。 */
    private static final Map<UUID, Integer> OWNED_UNTIL = new HashMap<>();
    private static boolean registered;

    private RealityAnchorAuraService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(RealityAnchorAuraService::tick);
        // A leaver does not keep the anchor's night vision in their saved data. / 离开的玩家不会把锚的夜视存进存档。
        ServerDisconnectCleanup.register((player, server) -> release(player, server.getTicks()));
        // Players are saved while the server stops: take the effect back first, then forget everything.
        // 停服时会保存玩家：先收回效果，再清空全部记录。
        ServerLifecycleEvents.SERVER_STOPPING.register(RealityAnchorAuraService::releaseAll);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> OWNED_UNTIL.clear());
        // No match anchor carries into the lobby or the next round (the finalize event is the one that has a world).
        // 对局中的锚不会带入大厅或下一局（收尾事件才携带世界）。
        GameEvents.ON_FINISH_FINALIZE.register((world, game) -> {
            if (world instanceof ServerWorld serverWorld) {
                RealityAnchorEntity.discardMatchAnchors(serverWorld);
            }
        });
    }

    /**
     * Grants or refreshes the anchor night vision for one player. A player whose night vision is not the anchor's own
     * is left completely alone.
     * 给一名玩家发放或续上锚的夜视。身上的夜视不属于锚的玩家完全不处理。
     */
    static void grant(ServerPlayerEntity player) {
        int now = player.server.getTicks();
        Integer ownedUntil = OWNED_UNTIL.get(player.getUuid());
        StatusEffectInstance current = player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (current != null && !isOwn(current, ownedUntil, now)) {
            OWNED_UNTIL.remove(player.getUuid());
            return;
        }
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION, NIGHT_VISION_TICKS, 0, true, false, false));
        OWNED_UNTIL.put(player.getUuid(), now + NIGHT_VISION_TICKS);
    }

    private static void tick(MinecraftServer server) {
        if (OWNED_UNTIL.isEmpty() || server.getTicks() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        int now = server.getTicks();
        Iterator<Map.Entry<UUID, Integer>> iterator = OWNED_UNTIL.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
            } else if (!nearAnchor(player)) {
                removeIfOwn(player, entry.getValue(), now);
                iterator.remove();
            }
        }
    }

    private static void release(ServerPlayerEntity player, int now) {
        Integer ownedUntil = OWNED_UNTIL.remove(player.getUuid());
        if (ownedUntil != null) {
            removeIfOwn(player, ownedUntil, now);
        }
    }

    private static void releaseAll(MinecraftServer server) {
        int now = server.getTicks();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            release(player, now);
        }
    }

    private static void removeIfOwn(ServerPlayerEntity player, int ownedUntil, int now) {
        StatusEffectInstance current = player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (current != null && isOwn(current, ownedUntil, now)) {
            player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    private static boolean isOwn(StatusEffectInstance effect, @Nullable Integer ownedUntil, int now) {
        return ownedUntil != null && matchesOwnGrant(effect.getAmplifier(), effect.isAmbient(),
                effect.shouldShowParticles(), effect.shouldShowIcon(), effect.isInfinite(), effect.getDuration(),
                ownedUntil - now);
    }

    /**
     * Pure rule: whether a night-vision instance is still exactly what this service granted. Anything another source
     * could have changed (a longer duration, flags, the amplifier) makes it foreign.
     * 纯规则：某个夜视效果是否仍与本服务发放的完全一致。其他来源可能改变的任何一项（更长的时长、标志、等级）都会使它被视为
     * 外来效果。
     */
    static boolean matchesOwnGrant(int amplifier, boolean ambient, boolean showParticles, boolean showIcon,
                                   boolean infinite, int durationTicks, int expectedRemainingTicks) {
        return amplifier == 0 && ambient && !showParticles && !showIcon && !infinite
                && expectedRemainingTicks > 0
                && Math.abs(durationTicks - expectedRemainingTicks) <= OWNED_TOLERANCE_TICKS;
    }

    /** Whether any anchor is within the night-vision radius of the player. / 玩家附近是否还有锚在夜视半径内。 */
    private static boolean nearAnchor(ServerPlayerEntity player) {
        return !player.getServerWorld().getEntitiesByClass(
                RealityAnchorEntity.class,
                player.getBoundingBox().expand(NIGHT_VISION_RADIUS),
                anchor -> !anchor.isRemoved()
                        && anchor.squaredDistanceTo(player) <= NIGHT_VISION_RADIUS * NIGHT_VISION_RADIUS
        ).isEmpty();
    }
}
