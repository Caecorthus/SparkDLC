package dev.caecorthus.sparkdlc.trait.api;

import dev.caecorthus.sparkdlc.trait.civilian.impostor.ImpostorRevolverService;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * The shop entries that a trait adds to its holder's shop, by entry id. A role shop that clears and rebuilds its
 * entries has to carry these over, whatever the role is. The set is explicit on purpose: trait entries used to be
 * recognised by their {@code sparktraits:} id prefix, which stopped identifying them when every id of the mod moved
 * to one namespace (other shop entries use the {@code namespace:path} form too). A trait that adds a shop entry
 * lists its id here.
 * 天赋为持有者商店追加的条目（按条目 id）。会清空并重建条目的职业商店必须保留它们，与职业无关。这里有意写成显式集合：
 * 过去靠 id 前缀 {@code sparktraits:} 识别天赋条目，但全部 id 归入同一命名空间后该前缀不再能区分它们（其他商店条目也使用
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
}
