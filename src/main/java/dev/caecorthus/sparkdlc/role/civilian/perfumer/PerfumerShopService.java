package dev.caecorthus.sparkdlc.role.civilian.perfumer;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.trait.api.TraitShopEntries;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.event.BuildShopEntries;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

/**
 * Replaces the Perfumer's shop with its two unlimited consumables.
 * 用两种不限购消耗品替换调香师的商店内容。
 */
public final class PerfumerShopService {
    private static boolean registered;

    private PerfumerShopService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        BuildShopEntries.EVENT.register(PerfumerShopService::buildEntries);
    }

    private static void buildEntries(PlayerEntity player, BuildShopEntries.ShopContext context) {
        Role role = GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
        if (!PerfumerRuntime.isPerfumerRole(role)) {
            return;
        }

        // SparkTraits 的词条商店监听可能先于 SparkWitch 执行；先保存 sparkdlc:* 条目，
        // 避免下面重建调香师专属商店时误删内鬼左轮等词条追加商品。
        List<ShopEntry> preservedTraitEntries = TraitShopEntries.capture(context);
        context.clearEntries();
        context.addEntry(new ShopEntry.Builder(
                "perfume_essence",
                SparkDlcItems.perfumeEssence().getDefaultStack(),
                PerfumerRules.PERFUME_ESSENCE_PRICE,
                ShopEntry.Type.TOOL
        ).build());
        context.addEntry(new ShopEntry.Builder(
                "cologne",
                SparkDlcItems.cologne().getDefaultStack(),
                PerfumerRules.COLOGNE_PRICE,
                ShopEntry.Type.TOOL
        ).build());
        TraitShopEntries.restore(context, preservedTraitEntries);
    }
}
