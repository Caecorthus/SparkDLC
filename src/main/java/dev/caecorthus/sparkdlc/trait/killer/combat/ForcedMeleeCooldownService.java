package dev.caecorthus.sparkdlc.trait.killer.combat;

import dev.caecorthus.sparkdlc.cooldown.api.AdminCooldownClear;
import dev.caecorthus.sparkdlc.trait.net.ForcedMeleeCooldownPayload;
import dev.caecorthus.sparkdlc.util.ExactItemCooldowns;
import dev.doctor4t.wathe.api.event.GameEvents;
import dev.doctor4t.wathe.api.event.ResetPlayer;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Independent item-type deadlines survive stack replacement, normal cooldown writes and disconnects. */
public final class ForcedMeleeCooldownService {
    private static final Map<UUID, Penalties> SERVER = new HashMap<>();
    private static final Map<Identifier, Long> CLIENT = new HashMap<>();
    private static final Set<UUID> EXPIRED = new HashSet<>();
    private static UUID clientOwner;
    private static boolean registered;

    private ForcedMeleeCooldownService() {
    }

    public static void register() {
        if (registered) return;
        registered = true;
        // Admin /sparkdlc:clearCooldown lifts the forced floor on the held item before its remove; otherwise
        // ExactItemCooldownMixin cancels that remove. Only the admin path does so.
        // 管理员 /sparkdlc:clearCooldown 在移除之前解除手持物品的强制下限，否则 ExactItemCooldownMixin 会取消该移除。
        // 只有管理员路径如此。
        AdminCooldownClear.registerBeforeRemove(ForcedMeleeCooldownService::clearItem);
        PayloadTypeRegistry.playS2C().register(ForcedMeleeCooldownPayload.ID, ForcedMeleeCooldownPayload.CODEC);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER.clear());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER.clear());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            restoreFloors(player);
            sync(player); // Empty snapshots explicitly reset stale owner state on a new connection.
        });
        ResetPlayer.EVENT.register(ForcedMeleeCooldownService::clearPlayer);
        GameEvents.ON_FINISH_FINALIZE.register((world, game) -> {
            if (world instanceof ServerWorld serverWorld) clearRound(serverWorld);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = server.getOverworld().getTime();
            SERVER.values().forEach(state -> state.deadlines.values().removeIf(deadline -> deadline <= now));
            // A player whose last floor just expired gets one empty snapshot; players without floors need no resync.
            SERVER.entrySet().removeIf(entry -> entry.getValue().deadlines.isEmpty() && EXPIRED.add(entry.getKey()));
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                boolean expired = EXPIRED.remove(player.getUuid());
                if (!GameFunctions.isPlayerPlayingAndAlive(player)) {
                    if (SERVER.containsKey(player.getUuid())) clearPlayer(player);
                    continue;
                }
                restoreFloors(player);
                if (expired || (server.getTicks() % 20 == 0 && SERVER.containsKey(player.getUuid()))) sync(player);
            }
            EXPIRED.clear();
        });
    }

    public static int remainingTicks(PlayerEntity player, ItemStack stack) {
        return stack == null || stack.isEmpty() ? 0 : remainingTicks(player, stack.getItem());
    }

    public static int remainingTicks(PlayerEntity player, Item item) {
        if (player == null || item == null) return 0;
        if (player.getWorld().isClient) {
            if (!player.getUuid().equals(clientOwner)) return 0;
            return CombatTimingRules.remaining(CLIENT.getOrDefault(Registries.ITEM.getId(item), 0L), player.age);
        }
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return 0;
        Penalties state = SERVER.get(player.getUuid());
        if (state == null || !state.world.equals(player.getWorld().getRegistryKey())) return 0;
        return CombatTimingRules.remaining(state.deadlines.getOrDefault(item, 0L), now(serverPlayer));
    }

    public static void impose(ServerPlayerEntity player, Item item, int ticks) {
        if (ticks <= 0) return;
        Penalties state = SERVER.computeIfAbsent(player.getUuid(), ignored ->
                new Penalties(player.getWorld().getRegistryKey(), new HashMap<>()));
        state.deadlines.merge(item, now(player) + ticks, Math::max);
        ExactItemCooldowns.setPastModifiers(player, item,
                Math.max(remainingTicks(player, item), ExactItemCooldowns.remainingTicks(player, item)));
        sync(player);
    }

    /**
     * The duration the vanilla {@code Entry} gets in every server {@code ItemCooldownManager.set}, after all HEAD
     * modifiers ({@code ExactItemCooldownMixin}): the exact duration of a running {@code ExactItemCooldowns}
     * write-past-modifiers, else the modified one, never below this item's forced floor.
     * 每次服务端 {@code ItemCooldownManager.set} 中原版 {@code Entry} 得到的时长，在所有 HEAD 倍率之后计算
     * （{@code ExactItemCooldownMixin}）：正在进行的 {@code ExactItemCooldowns} 越过倍率写入取其精确时长，否则取修改后的时长，
     * 且绝不低于该物品的强制下限。
     */
    public static int durationForWrite(ItemCooldownManager manager, Item item, int modifiedDuration,
                                       ServerPlayerEntity owner) {
        int exactTicks = ExactItemCooldowns.exactWriteTicks(manager, item);
        boolean exact = exactTicks != ExactItemCooldowns.NO_EXACT_WRITE;
        int duration = exact ? exactTicks : Math.max(0, modifiedDuration);
        int floor = owner == null ? 0 : remainingTicks(owner, item);
        // A normal rewrite cannot erase a longer existing cooldown while this penalty is active.
        // Deliberate exact halving may reduce that normal component, but never the forced floor.
        return CombatTimingRules.resolveWriteDuration(duration, floor,
                ExactItemCooldowns.remainingTicks(manager, item), exact);
    }

    /** Called by the client receiver on its main thread; no client classes enter the common initializer. */
    public static void acceptClientSnapshot(PlayerEntity player, ForcedMeleeCooldownPayload payload) {
        clearClient();
        if (player == null || !player.getUuid().equals(payload.owner())) return;
        clientOwner = payload.owner();
        payload.remaining().forEach((item, ticks) -> CLIENT.put(item, (long) player.age + ticks));
    }

    public static void clearClient() {
        CLIENT.clear();
        clientOwner = null;
    }

    public static void clearPlayer(ServerPlayerEntity player) {
        Penalties state = SERVER.remove(player.getUuid());
        if (state != null) {
            // Remove only the floor we installed; do not erase a longer normal cooldown.
            state.deadlines.forEach((item, deadline) -> {
                int floor = CombatTimingRules.remaining(deadline, now(player));
                if (ExactItemCooldowns.remainingTicks(player, item) <= floor) player.getItemCooldownManager().remove(item);
            });
        }
        sync(player);
    }

    /**
     * Admin clear of one item ({@code /sparkdlc:clearCooldown}, through {@code AdminCooldownClear} right before the
     * command's {@code remove}): drops only its forced floor, so that {@code remove} is no longer cancelled and the
     * floor is not restored next tick. The vanilla cooldown is left to the caller. Every other {@code remove} still
     * keeps the floor.
     * 管理员清除单个物品（{@code /sparkdlc:clearCooldown}，经由 {@code AdminCooldownClear}，紧接在命令的 {@code remove} 之前）：
     * 只去掉其强制下限，使该 {@code remove} 不再被取消，下限也不会在下一刻恢复。原版冷却留给调用方处理。其他任何
     * {@code remove} 仍保留下限。
     */
    public static void clearItem(ServerPlayerEntity player, Item item) {
        Penalties state = SERVER.get(player.getUuid());
        if (state == null || state.deadlines.remove(item) == null) return;
        if (state.deadlines.isEmpty()) SERVER.remove(player.getUuid());
        sync(player);
    }

    public static void clearRound(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) clearPlayer(player);
        SERVER.values().removeIf(state -> state.world.equals(world.getRegistryKey()));
    }

    private static void restoreFloors(ServerPlayerEntity player) {
        Penalties state = SERVER.get(player.getUuid());
        if (state == null) return;
        state.deadlines.forEach((item, deadline) -> {
            int floor = remainingTicks(player, item);
            if (floor > ExactItemCooldowns.remainingTicks(player, item)) ExactItemCooldowns.setPastModifiers(player, item, floor);
        });
    }

    private static void sync(ServerPlayerEntity player) {
        if (!ServerPlayNetworking.canSend(player, ForcedMeleeCooldownPayload.ID)) return;
        Map<Identifier, Integer> remaining = new HashMap<>();
        Penalties state = SERVER.get(player.getUuid());
        if (state != null) state.deadlines.forEach((item, deadline) -> {
            int ticks = remainingTicks(player, item);
            if (ticks > 0) remaining.put(Registries.ITEM.getId(item), ticks);
        });
        ServerPlayNetworking.send(player, new ForcedMeleeCooldownPayload(player.getUuid(), remaining));
    }

    private static long now(ServerPlayerEntity player) {
        return player.getServer().getOverworld().getTime();
    }

    private record Penalties(RegistryKey<World> world, Map<Item, Long> deadlines) {
    }
}
