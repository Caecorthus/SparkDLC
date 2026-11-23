package dev.caecorthus.sparkdlc.trait.api;

import dev.caecorthus.sparkdlc.trait.civilian.impostor.ImpostorRevolverService;
import dev.doctor4t.wathe.api.event.BuildShopEntries;
import dev.doctor4t.wathe.util.ShopEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The shop entries that a trait adds to its holder's shop, by entry id. A role shop that clears and rebuilds its
 * entries has to carry these over, whatever the role is. The set is explicit on purpose: trait entries used to be
 * recognised by their {@code sparkdlc:} id prefix, which stopped identifying them when every id of the mod moved
 * to one namespace (other shop entries use the {@code namespace:path} form too). A trait that adds a shop entry
 * lists its id here.
 * 天赋为持有者商店追加的条目（按条目 id）。会清空并重建条目的职业商店必须保留它们，与职业无关。这里有意写成显式集合：
 * 过去靠 id 前缀 {@code sparkdlc:} 识别天赋条目，但全部 id 归入同一命名空间后该前缀不再能区分它们（其他商店条目也使用
 * {@code namespace:path} 形式）。新增商店条目的天赋需要把 id 登记在这里。
 */
public final class TraitShopEntries {
    private static final Set<String> IDS = Set.of(ImpostorRevolverService.REVOLVER_SHOP_ID.toString());

    private TraitShopEntries() {
    }

    /** Whether {@code entryId} is a shop entry added by a trait. / 该条目 id 是否为天赋追加的商店条目。 */
    public static boolean isTraitEntry(@Nullable String entryId) {
        return entryId != null && IDS.contains(entryId);
    }

    /**
     * Captures the trait entries that are already in the shop, in shop order. A role shop that clears and rebuilds
     * its entries calls this before the clear and {@link #restore} after the rebuild.
     * 按商店顺序捕获当前商店中已有的天赋条目。会清空并重建条目的职业商店在清空前调用它，重建后调用 {@link #restore}。
     */
    public static List<ShopEntry> capture(BuildShopEntries.ShopContext context) {
        List<ShopEntry> preservedEntries = new ArrayList<>();
        for (ShopEntry entry : context.getEntries()) {
            if (isTraitEntry(entry.id())) {
                preservedEntries.add(entry);
            }
        }
        return preservedEntries;
    }

    /**
     * Puts the captured trait entries back after the role rebuilt its own shop, so the Impostor revolver and every
     * other entry listed here survive the {@code clearEntries}. An entry whose id the rebuilt shop already contains is
     * not added twice; a null or empty capture restores nothing.
     * 在职业重建自己的商店后放回捕获的天赋条目，保证内鬼左轮以及登记在此处的其他天赋商品不会被 {@code clearEntries} 吞掉。
     * 重建后的商店已含有相同 id 的条目时不重复添加；捕获结果为空时不做任何事。
     */
    public static void restore(BuildShopEntries.ShopContext context, List<ShopEntry> preservedEntries) {
        if (preservedEntries == null || preservedEntries.isEmpty()) {
            return;
        }
        for (ShopEntry entry : preservedEntries) {
            String entryId = entry.id();
            if (entryId != null && !containsEntryId(context, entryId)) {
                context.addEntry(entry);
            }
        }
    }

    private static boolean containsEntryId(BuildShopEntries.ShopContext context, String entryId) {
        for (ShopEntry entry : context.getEntries()) {
            if (entry.id().equals(entryId)) {
                return true;
            }
        }
        return false;
    }
}
