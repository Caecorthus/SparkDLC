package dev.caecorthus.sparkdlc.role.civilian.usec;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.trait.api.TraitShopEntries;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.event.BuildShopEntries;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

import java.util.List;

/**
 * Owned by WP1: replaces the USEC shop on both sides with {@link UsecShopRules#entries()} (magazine, FMJ, AP,
 * suppressor). Called once from {@link UsecFeatureService#register()}. Trait entries (e.g. the Impostor
 * revolver) survive the rebuild and every entry passes through the Charisma trait's discount wrapper. Rounds top up
 * a shown stack before falling back to Wathe's default insertion ({@link UsecShopPurchase}); the magazine and suppressor
 * keep the default buy handler. The tablet is issued free at round start, never sold here.
 * 归 WP1 所有：在两端把 USEC 商店替换为 {@link UsecShopRules#entries()}（弹匣、FMJ、AP、消音器）。由
 * {@link UsecFeatureService#register()} 调用一次。词条条目（如内鬼左轮）在重建后保留，每个条目经过
 * 魅力词条的折扣包装。子弹先补进显示中的物品堆，再回退到 Wathe 默认插入（{@link UsecShopPurchase}）；弹匣与消音器保留默认
 * 购买处理。平板开局免费发放，此处不出售。
 */
public final class UsecShopService {
    private static boolean registered;

    private UsecShopService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        BuildShopEntries.EVENT.register(UsecShopService::buildEntries);
    }

    private static void buildEntries(PlayerEntity player, BuildShopEntries.ShopContext context) {
        Role role = GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
        if (!UsecShopRules.rebuildsShopFor(role)) {
            return;
        }
        // Trait entries (e.g. the Impostor revolver) may already be present; keep them across the rebuild.
        // 词条条目（如内鬼左轮）可能已先加入；重建时保留它们。
        List<ShopEntry> preservedTraitEntries = TraitShopEntries.capture(context);
        context.clearEntries();
        for (UsecShopRules.EntrySpec spec : UsecShopRules.entries()) {
            ShopEntry entry = entry(spec);
            context.addEntry(SparkTraitsApi.discountShopEntryForCharisma(player, entry));
        }
        TraitShopEntries.restore(context, preservedTraitEntries);
    }

    /**
     * Never calls Wathe's hotbar-only {@code insertStackInFreeSlot} directly, so the second-row {@code onBuy} wrapper
     * ({@code ShopEntrySecondRowMixin}) can fall back to the second row. A round's handler tops up a shown stack, else
     * runs the handler-less twin's
     * default {@code onBuy}; both entries share id, stack, price, type and stock, so the list shape is unchanged.
     * A magazine is sold empty (no CUSTOM_DATA).
     * 从不直接调用 Wathe 只放快捷栏的 {@code insertStackInFreeSlot}，使第二行对 {@code onBuy} 的包装
     * （{@code ShopEntrySecondRowMixin}）能回退到第二行。子弹的处理器先补进显示中的物品堆，否则执行不带处理器的孪生条目的
     * 默认 {@code onBuy}；两者的 id、物品、价格、类型与库存相同，因此列表形状不变。弹匣以空弹匣出售（无 CUSTOM_DATA）。
     */
    private static ShopEntry entry(UsecShopRules.EntrySpec spec) {
        ShopEntry defaultEntry = builder(spec).build();
        if (!spec.kind().topsUpStacks()) {
            return defaultEntry;
        }
        return builder(spec).onBuy(player -> UsecShopPurchase.buyRound(player, defaultEntry)).build();
    }

    private static ShopEntry.Builder builder(UsecShopRules.EntrySpec spec) {
        ShopEntry.Builder builder = new ShopEntry.Builder(spec.id(), item(spec.kind()).getDefaultStack(), spec.price(),
                spec.type());
        if (spec.hasStockLimit()) {
            builder.stock(spec.stock());
        }
        return builder;
    }

    private static Item item(UsecShopRules.EntryKind kind) {
        return switch (kind) {
            case MAGAZINE -> SparkDlcItems.usecMagazine();
            case FMJ_ROUND -> SparkDlcItems.usecFmjRound();
            case AP_ROUND -> SparkDlcItems.usecApRound();
            case SUPPRESSOR -> SparkDlcItems.usecSuppressor();
        };
    }
}
