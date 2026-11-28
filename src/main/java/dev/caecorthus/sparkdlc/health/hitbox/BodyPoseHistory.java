package dev.caecorthus.sparkdlc.health.hitbox;

import dev.caecorthus.sparkdlc.util.hitscan.HitscanLagRules;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-thread ring buffer of every player's {@link BodyPose} at the end of each server tick, the body-part twin of
 * {@code util.hitscan.PlayerHitboxHistory}: a shooter aims at a body where and how its client drew it, so the part a
 * shot landed on is looked up in the same rewind window the weapon's targeting used. The recorder is also what
 * carries each player's body yaw from tick to tick ({@link BodyYawRules}). In-memory and transient: no NBT, no sync,
 * no packets; dropped on disconnect and server stop.
 * 仅在服务端线程维护的环形缓冲，记录每名玩家在每个服务端刻结束时的 {@link BodyPose}，是
 * {@code util.hitscan.PlayerHitboxHistory} 的部位版：射手瞄准的是其客户端所绘制的身体位置与姿态，因此命中部位要在武器瞄准
 * 所用的同一回溯窗口内查找。记录器同时负责逐刻推进每名玩家的身体偏航角（{@link BodyYawRules}）。纯内存、临时：不写 NBT、
 * 不同步、不发数据包；断线与服务端停止时清除。
 */
public final class BodyPoseHistory {
    private static final Map<UUID, ArrayDeque<Sample>> HISTORY = new HashMap<>();
    private static boolean registered;

    private BodyPoseHistory() {
    }

    private record Sample(int tick, RegistryKey<World> world, BodyPose pose) {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(BodyPoseHistory::record);
        // Fabric may fire DISCONNECT on the Netty thread; the map is only touched on the server thread.
        // Fabric 可能在 Netty 线程触发 DISCONNECT；该映射只在服务端线程访问。
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID uuid = handler.getPlayer().getUuid();
            server.execute(() -> HISTORY.remove(uuid));
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> HISTORY.clear());
    }

    /**
     * Newest first: the pose of {@code target} right now, then its recorded poses no older than the rewind window of
     * {@code shooter} (the ping-independent view delay when there is no shooter), stopping at a dimension change. A
     * player without a history (a mock player, the tick of a join) answers with its current pose alone.
     * 最新在前：{@code target} 此刻的姿态，然后是不早于 {@code shooter} 回溯窗口的历史姿态（没有射手时取与延迟无关的视图
     * 滞后），遇到维度变化即停止。没有历史的玩家（模拟玩家、刚加入的那一刻）只返回当前姿态。
     */
    static List<BodyPose> recentPoses(@Nullable ServerPlayerEntity shooter, PlayerEntity target) {
        List<BodyPose> poses = new ArrayList<>();
        ArrayDeque<Sample> samples = HISTORY.get(target.getUuid());
        Sample newest = samples == null ? null : samples.peekFirst();
        RegistryKey<World> world = target.getWorld().getRegistryKey();
        // The live position with the tracked body yaw: the server's own bodyYaw only trails the head.
        // 实时位置配合推算出的身体偏航角：服务端自身的 bodyYaw 只会跟随头部。
        poses.add(BodyHitboxes.poseOf(target, newest != null && newest.world().equals(world)
                ? newest.pose().bodyYaw() : target.bodyYaw));
        MinecraftServer server = target.getServer();
        if (samples == null || server == null) {
            return poses;
        }
        int rewind = shooter == null ? HitscanLagRules.CLIENT_VIEW_DELAY_TICKS
                : HitscanLagRules.rewindTicks(shooter.networkHandler.getLatency());
        int oldestTick = server.getTicks() - rewind;
        for (Sample sample : samples) {
            if (sample.tick() < oldestTick || !sample.world().equals(world)) {
                break;
            }
            poses.add(sample.pose());
        }
        return poses;
    }

    private static void record(MinecraftServer server) {
        int tick = server.getTicks();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ArrayDeque<Sample> samples = HISTORY.computeIfAbsent(player.getUuid(), uuid -> new ArrayDeque<>());
            RegistryKey<World> world = player.getWorld().getRegistryKey();
            Sample previous = samples.peekFirst();
            float bodyYaw;
            if (previous == null || !previous.world().equals(world)) {
                bodyYaw = player.getYaw();
            } else {
                Vec3d moved = player.getPos().subtract(previous.pose().position());
                // A jump (teleport, respawn) is no walk: the body keeps its yaw, limited by the head as always.
                // 跳变（传送、重生）不是行走：身体保持原偏航角，照常受头部限制。
                boolean walked = moved.horizontalLengthSquared()
                        <= HitscanLagRules.MAX_SWEEP_STEP * HitscanLagRules.MAX_SWEEP_STEP;
                bodyYaw = BodyYawRules.next(previous.pose().bodyYaw(), player.getYaw(),
                        walked ? moved.x : 0.0, walked ? moved.z : 0.0, player.handSwinging);
            }
            samples.addFirst(new Sample(tick, world, BodyHitboxes.poseOf(player, bodyYaw)));
            while (samples.size() > HitscanLagRules.HISTORY_TICKS) {
                samples.removeLast();
            }
        }
    }
}
