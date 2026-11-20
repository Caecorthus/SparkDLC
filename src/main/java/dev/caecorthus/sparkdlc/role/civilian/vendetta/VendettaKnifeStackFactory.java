package dev.caecorthus.sparkdlc.role.civilian.vendetta;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.doctor4t.wathe.cosmetic.CosmeticDataCache;
import dev.doctor4t.wathe.index.WatheDataComponentTypes;
import dev.doctor4t.wathe.item.KnifeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

final class VendettaKnifeStackFactory {
    private VendettaKnifeStackFactory() {
    }

    static ItemStack create(ServerPlayerEntity player) {
        ItemStack knife = new ItemStack(SparkDlcItems.vendettaKnife());
        var cosmetic = CosmeticDataCache.getCosmetic(player.getUuid(), KnifeItem.ITEM_ID);
        if (cosmetic != null) {
            knife.set(WatheDataComponentTypes.SKIN, cosmetic);
        }
        return knife;
    }
}
