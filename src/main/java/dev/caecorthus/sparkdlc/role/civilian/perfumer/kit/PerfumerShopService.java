package dev.caecorthus.sparkdlc.role.civilian.perfumer.kit;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.role.civilian.perfumer.kit.PerfumerScentComponent;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.util.ShopStacks;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Appends the Perfumer kit to the Perfumer's final shop list (after the role's own entries from
 * {@code perfumer.PerfumerShopService}).
 * 在调香师最终商店列表（职业自身条目即 {@code perfumer.PerfumerShopService} 之后）追加调香师道具。
 */
public final class PerfumerShopService {
    private PerfumerShopService() {
    }

    /**
     * Called from {@code M67ShopEntriesMixin} on both sides; Wathe buys by index, so the answer must only depend on
     * synced state (the real role). Deliberately not gated on {@code isPlayerPlayingAndAlive}: Wathe's
     * {@code initializeShopsForPlayers} caches Zephyr's {@code stock(1)} from this list before the round is ACTIVE.
     * 由 M67ShopEntriesMixin 在两端调用；Wathe 按下标购买，因此结果只能依赖已同步状态（真实身份）。
     * 刻意不检查 isPlayerPlayingAndAlive：Wathe 在对局变为 ACTIVE 前就从此列表缓存晨风香水的 stock(1)。
     */
    public static List<ShopEntry> appendToFinalEntries(PlayerEntity player, List<ShopEntry> entries) {
        if (player == null
                || !PerfumerKitRules.isPerfumer(GameWorldComponent.KEY.get(player.getWorld()).getRole(player))) {
            return entries;
        }

        List<ShopEntry> result = null;
        Item coolingOil = SparkDlcItems.coolingOil();
        if (!contains(entries, PerfumerKitRules.COOLING_OIL_ENTRY_ID, coolingOil)) {
            result = add(result, entries, player, new ShopEntry.Builder(
                    PerfumerKitRules.COOLING_OIL_ENTRY_ID,
                    ShopStacks.display(coolingOil.getDefaultStack(), "shop.sparkdlc.cooling_oil"),
                    PerfumerKitRules.COOLING_OIL_PRICE,
                    ShopEntry.Type.TOOL
            ).actualStack(coolingOil.getDefaultStack())
                    // Purchase handler (PlayerShopComponent.tryBuy), capped at the throwable stack size.
                    // 购买处理（PlayerShopComponent.tryBuy），堆叠上限为投掷物堆叠数。
                    .onBuy(buyer -> ShopStacks.giveStackable(buyer, coolingOil, PerfumerKitRules.THROWABLE_MAX_STACK))
                    .build());
        }
        Item aromaOrb = SparkDlcItems.aromaOrb();
        if (!contains(entries, PerfumerKitRules.AROMA_ORB_ENTRY_ID, aromaOrb)) {
            result = add(result, entries, player, new ShopEntry.Builder(
                    PerfumerKitRules.AROMA_ORB_ENTRY_ID,
                    ShopStacks.display(aromaOrb.getDefaultStack(), "shop.sparkdlc.aroma_orb"),
                    PerfumerKitRules.AROMA_ORB_PRICE,
                    ShopEntry.Type.TOOL
            ).actualStack(aromaOrb.getDefaultStack())
                    .onBuy(buyer -> ShopStacks.giveStackable(buyer, aromaOrb, PerfumerKitRules.THROWABLE_MAX_STACK))
                    .build());
        }
        Item zephyrPerfume = SparkDlcItems.zephyrPerfume();
        if (!contains(entries, PerfumerKitRules.ZEPHYR_PERFUME_ENTRY_ID, zephyrPerfume)) {
            result = add(result, entries, player, new ShopEntry.Builder(
                    PerfumerKitRules.ZEPHYR_PERFUME_ENTRY_ID,
                    ShopStacks.display(zephyrPerfume.getDefaultStack(), "shop.sparkdlc.zephyr_perfume"),
                    PerfumerKitRules.ZEPHYR_PERFUME_PRICE,
                    ShopEntry.Type.TOOL
            ).actualStack(zephyrPerfume.getDefaultStack())
                    .stock(PerfumerKitRules.ZEPHYR_PERFUME_STOCK)
                    .onBuy(buyer -> buyZephyr(buyer, zephyrPerfume))
                    .build());
        }
        return result == null ? entries : result;
    }

    /**
     * Hard cap behind {@code stock(1)}: stock is cached only for roles held at round start, so a mid-round
     * Perfumer would otherwise have unlimited Zephyr. Returning false charges nothing.
     * stock(1) 之外的硬上限：库存只为开局时的身份缓存，局中成为调香师者否则可无限购买晨风香水。返回 false 不扣费。
     */
    private static boolean buyZephyr(PlayerEntity player, Item zephyrPerfume) {
        if (PerfumerScentComponent.KEY.get(player).isZephyrActive()
                || player.getInventory().contains(stack -> stack.isOf(zephyrPerfume))) {
            return false;
        }
        // The shop lives in the inventory screen, so a bottle may be held on the cursor while buying.
        // 商店位于物品栏界面，购买时香水可能正被鼠标拿起。
        if (player.currentScreenHandler != null && player.currentScreenHandler.getCursorStack().isOf(zephyrPerfume)) {
            return false;
        }
        return ShopEntry.insertStackInFreeSlot(player, zephyrPerfume.getDefaultStack());
    }

    private static boolean contains(List<ShopEntry> entries, String entryId, Item item) {
        for (ShopEntry entry : entries) {
            if (entryId.equals(entry.id()) || entry.stack().isOf(item)) {
                return true;
            }
        }
        return false;
    }

    private static List<ShopEntry> add(
            List<ShopEntry> result, List<ShopEntry> entries, PlayerEntity player, ShopEntry entry
    ) {
        List<ShopEntry> target = result == null ? new ArrayList<>(entries) : result;
        target.add(SparkTraitsApi.discountShopEntryForCharisma(player, entry));
        return target;
    }
}
