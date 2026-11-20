package dev.caecorthus.sparkdlc.role.civilian.emma;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.mixin.accessor.ItemCooldownEntryAccessor;
import dev.caecorthus.sparkdlc.mixin.accessor.ItemCooldownManagerAccessor;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;

/** Exact fallback writes bypass set-time multipliers but retain vanilla client synchronization.
 * 精确回退写入不重复应用倍率，仍保留原版客户端冷却同步。
 * <p>{@link #setExact} asks the trait feature's exact write first; that write declines only a null player or item, so
 * the vanilla fallback below it (and the {@link #exactDuration} it feeds) is not reached with real arguments.
 * <p>{@link #setExact} 先交给词条功能的精确写入；该写入仅在玩家或物品为空时不执行，因此其后的原版回退（以及由它供给的
 * {@link #exactDuration}）在参数有效时不会被走到。 */
public final class EmmaCooldowns {
    private static final ThreadLocal<Write> WRITE = new ThreadLocal<>();

    private EmmaCooldowns() { }

    public static int tick(ServerPlayerEntity player) {
        return ((ItemCooldownManagerAccessor) player.getItemCooldownManager()).sparkdlc$getTickForRoles();
    }

    public static int start(ServerPlayerEntity player, Item item) {
        Object entry = ((ItemCooldownManagerAccessor) player.getItemCooldownManager()).sparkdlc$getEntriesForRoles().get(item);
        return entry instanceof ItemCooldownEntryAccessor value ? value.sparkdlc$getStartTickForRoles() : tick(player);
    }

    public static int end(ServerPlayerEntity player, Item item) {
        Object entry = ((ItemCooldownManagerAccessor) player.getItemCooldownManager()).sparkdlc$getEntriesForRoles().get(item);
        return entry instanceof ItemCooldownEntryAccessor value ? value.sparkdlc$getEndTickForRoles() : tick(player);
    }

    public static void setExact(ServerPlayerEntity player, Item item, int ticks) {
        if (SparkTraitsApi.setExactItemCooldownRemaining(player, item, ticks)) return;
        ItemCooldownManager manager = player.getItemCooldownManager();
        Write previous = WRITE.get();
        WRITE.set(new Write(manager, item, Math.max(0, ticks)));
        try {
            manager.set(item, Math.max(0, ticks));
        } finally {
            if (previous == null) WRITE.remove(); else WRITE.set(previous);
        }
    }

    public static Integer exactDuration(ItemCooldownManager manager, Item item) {
        Write write = WRITE.get();
        return write != null && write.manager() == manager && write.item() == item ? write.ticks() : null;
    }

    private record Write(ItemCooldownManager manager, Item item, int ticks) { }
}
