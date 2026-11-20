package dev.caecorthus.sparkdlc.role.civilian.emma;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.mixin.cooldown.ItemCooldownEntryAccessor;
import dev.caecorthus.sparkdlc.mixin.cooldown.ItemCooldownManagerAccessor;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;

/** Reads of the revolver cooldown clock and Emma's exact write of it.
 * 左轮冷却时钟的读取，以及艾玛对它的精确写入。
 * <p>{@link #setExact} is the trait feature's exact write ({@code SparkTraitsApi.setExactItemCooldownRemaining}): it
 * runs past the set-time multipliers and performs the vanilla set (and its client sync) itself. That write declines
 * only a null player or item, and the one caller ({@code EmmaGunService.applyCooldown}) passes a shooter it has
 * already read and the constant {@code WatheItems.REVOLVER}; the vanilla fallback that used to follow it, its
 * thread-local scope and {@code EmmaExactCooldownMixin} (which only acted inside that scope) could not run and are
 * removed.
 * <p>{@link #setExact} 即词条功能的精确写入（{@code SparkTraitsApi.setExactItemCooldownRemaining}）：它越过写入时的倍率，
 * 并自行完成原版写入（及其客户端同步）。该写入仅在玩家或物品为空时不执行，而唯一的调用方
 * （{@code EmmaGunService.applyCooldown}）传入的是它已经读取过的射手与常量 {@code WatheItems.REVOLVER}；原先跟在其后的原版回退、
 * 其线程局部作用域以及只在该作用域内生效的 {@code EmmaExactCooldownMixin} 都无法执行，已移除。 */
public final class EmmaCooldowns {
    private EmmaCooldowns() { }

    public static int tick(ServerPlayerEntity player) {
        return ((ItemCooldownManagerAccessor) player.getItemCooldownManager()).sparkdlc$getTick();
    }

    public static int start(ServerPlayerEntity player, Item item) {
        Object entry = ((ItemCooldownManagerAccessor) player.getItemCooldownManager()).sparkdlc$getEntries().get(item);
        return entry instanceof ItemCooldownEntryAccessor value ? value.sparkdlc$getStartTick() : tick(player);
    }

    public static int end(ServerPlayerEntity player, Item item) {
        Object entry = ((ItemCooldownManagerAccessor) player.getItemCooldownManager()).sparkdlc$getEntries().get(item);
        return entry instanceof ItemCooldownEntryAccessor value ? value.sparkdlc$getEndTick() : tick(player);
    }

    public static void setExact(ServerPlayerEntity player, Item item, int ticks) {
        SparkTraitsApi.setExactItemCooldownRemaining(player, item, ticks);
    }
}
