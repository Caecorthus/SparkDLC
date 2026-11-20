package dev.caecorthus.sparkdlc.faction.blackout;

import dev.caecorthus.sparkdlc.mixin.wathe.PlayerShopComponentAccessor;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Adapts Wathe blackout shop purchases to custom-faction cooldown sharing.
 * 将 wathe 熄灯商店购买适配到自定义阵营冷却共享语义。
 */
public final class FactionBlackoutCooldownAdapter {
    private static final String BLACKOUT_SHOP_ENTRY_ID = "blackout";
    private static final String BLACKOUT_COOLDOWN_ID = "blackout";

    private FactionBlackoutCooldownAdapter() {
    }

    public static void afterShopPurchase(ServerPlayerEntity purchaser, ShopEntry entry, int index, int pricePaid) {
        if (!BLACKOUT_SHOP_ENTRY_ID.equals(entry.id())) {
            return;
        }

        // Wathe and the traits have already shared native killer cooldowns; add only custom bridges here.
        // wathe 与词条功能已经分发原生杀手冷却；这里仅追加自定义阵营桥接。
        GameWorldComponent gameComponent = GameWorldComponent.KEY.get(purchaser.getWorld());
        int cooldownTicks = GameConstants.getInTicks(5, 0);
        for (ServerPlayerEntity player : purchaser.getServerWorld().getPlayers()) {
            if (FactionBlackoutRules.sharesCustomBlackoutCooldown(player, gameComponent)) {
                setBlackoutCooldown(player, cooldownTicks);
            }
        }
    }

    private static void setBlackoutCooldown(ServerPlayerEntity player, int cooldownTicks) {
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
        ((PlayerShopComponentAccessor) (Object) shop).sparkdlc$getCooldowns()
                .put(BLACKOUT_COOLDOWN_ID, cooldownTicks);
        shop.sync();
    }
}
