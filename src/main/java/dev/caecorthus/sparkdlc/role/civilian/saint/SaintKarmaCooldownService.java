package dev.caecorthus.sparkdlc.role.civilian.saint;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.component.WitchWorldComponent;
import dev.caecorthus.sparkdlc.mixin.cooldown.ItemCooldownEntryAccessor;
import dev.caecorthus.sparkdlc.mixin.cooldown.ItemCooldownManagerAccessor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Applies Karma to item types currently carried in main inventory, hotbar, or offhand; armor is excluded.
 * 将业障冷却施加到主背包、快捷栏和副手内的物品类型；盔甲栏不在范围内。
 */
public final class SaintKarmaCooldownService {
    /**
     * Carried-item scratch set reused by {@link #apply} (server thread only, called every tick while Karma runs);
     * emptied after each call. Each item's write is independent of the others, so the iteration order is irrelevant.
     * A re-entrant call (none exists today) takes a fresh set.
     * {@link #apply} 复用的携带物品临时集合（仅服务端线程，业障期间每 tick 调用），每次调用后清空。各物品的写入互不
     * 相关，迭代顺序无关紧要。重入调用（目前不存在）使用新集合。
     */
    private static final Set<Item> CARRIED_SCRATCH = new HashSet<>();
    private static boolean scratchInUse;

    private SaintKarmaCooldownService() {
    }

    public static void apply(ServerPlayerEntity player, int requestedTicks) {
        if (requestedTicks <= 0) {
            return;
        }

        boolean reuse = !scratchInUse;
        Set<Item> carriedItems = reuse ? CARRIED_SCRATCH : new HashSet<>();
        scratchInUse = true;
        try {
            collectItems(player.getInventory().main, carriedItems);
            collectItems(player.getInventory().offHand, carriedItems);

            WitchWorldComponent worldComponent = WitchWorldComponent.KEY.get(player.getServerWorld());
            ItemCooldownManager manager = player.getItemCooldownManager();
            ItemCooldownManagerAccessor managerAccessor = (ItemCooldownManagerAccessor) manager;
            Map<Item, ?> entries = managerAccessor.sparkdlc$getEntries();
            int currentTick = managerAccessor.sparkdlc$getTick();
            for (Item item : carriedItems) {
                if (worldComponent.isSaintKarmaAdminCleared(player.getUuid(), Registries.ITEM.getId(item))) {
                    continue;
                }
                int existingTicks = remainingTicks(entries.get(item), currentTick);
                int mergedTicks = SaintRules.mergeCooldownTicks(existingTicks, requestedTicks);
                if (mergedTicks > existingTicks) {
                    write(player, item, mergedTicks);
                }
            }
        } finally {
            if (reuse) {
                CARRIED_SCRATCH.clear();
                scratchInUse = false;
            }
        }
    }

    /**
     * An admin clear ({@code /sparkdlc:clearCooldown}) that really removed {@code item}'s cooldown sticks for the
     * running Karma: Karma stops re-covering that item until it ends or is triggered again.
     * 真正移除了 {@code item} 冷却的管理员清除（{@code /sparkdlc:clearCooldown}）在本次业障内保持有效：业障结束或
     * 再次触发前不再覆盖该物品。
     */
    public static void exemptAdminCleared(ServerPlayerEntity player, Item item) {
        WitchWorldComponent.KEY.get(player.getServerWorld())
                .exemptSaintKarmaAdminCleared(player.getUuid(), Registries.ITEM.getId(item));
    }

    /**
     * Exact through {@code SparkTraitsApi#setExactItemCooldownRemaining}, which performs the vanilla set itself (it
     * declines only a null player or item, and {@link #apply} passes a carried stack's item for a player it already
     * read). A shortened write (Fast Hands, NoellesRoles Stimulation) would stay below the Karma and be rewritten
     * every tick, restarting the slot's cooldown bar.
     * 经 {@code SparkTraitsApi#setExactItemCooldownRemaining} 精确写入，它会自行完成原版写入（仅在玩家或物品为空时拒绝，
     * 而 {@link #apply} 传入的是已读取过的玩家与携带物品堆的物品）。被缩短的写入（快手、NoellesRoles 兴奋剂）会一直低于
     * 业障并每 tick 重写，使栏位冷却条不断从头开始。
     */
    private static void write(ServerPlayerEntity player, Item item, int mergedTicks) {
        SparkTraitsApi.setExactItemCooldownRemaining(player, item, mergedTicks);
    }

    private static void collectItems(Iterable<ItemStack> stacks, Set<Item> items) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                items.add(stack.getItem());
            }
        }
    }

    private static int remainingTicks(Object entry, int currentTick) {
        if (!(entry instanceof ItemCooldownEntryAccessor accessor)) {
            return 0;
        }
        return Math.max(0, accessor.sparkdlc$getEndTick() - currentTick);
    }
}
