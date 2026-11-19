package dev.caecorthus.sparkdlc.witch.compat;

import dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * SparkTraits 魅力折扣的桥接（SparkTraits 已融合进 SparkDLC，此处为直接调用）。
 * Bridge for the SparkTraits Charisma discount (SparkTraits is fused into SparkDLC, so the call is direct).
 *
 * <p>外部只给我们一个玩家和一个商店条目；把条目交给 SparkTraits 的公共 API 套用一次【魅力】折扣。
 * 玩家或条目为空、或 API 未返回条目时，原样返回条目。</p>
 */
public final class SparkTraitsCharismaBridge {
    private SparkTraitsCharismaBridge() {
    }

    public static @Nullable ShopEntry discountShopEntry(@Nullable PlayerEntity player, @Nullable ShopEntry entry) {
        if (player == null || entry == null) {
            return entry;
        }
        // A null answer keeps the original entry. / 返回空时保留原条目。
        ShopEntry discounted = SparkTraitsApi.discountShopEntryForCharisma(player, entry);
        return discounted != null ? discounted : entry;
    }
}
