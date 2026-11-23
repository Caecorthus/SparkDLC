package dev.caecorthus.sparkdlc.role.witch.abysslistener;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.role.witch.accomplice.shop.AccompliceShopRules;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.event.BuildShopEntries;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Builds the Abyss Listener shop: every plain Accomplice entry, then the Deep Dark Spore Flask (unlimited stock, no
 * purchase cooldown; the flask has no use cooldown either, D10). The gate is the exact role only, never the round
 * state, because Wathe caches stock limits during STARTING. The tablet is never sold; it is issued free by faction.
 * 构建聆渊者商店：先放入普通共犯的全部条目，再加入深暗孢瓶（不限库存、无购买冷却；孢瓶也没有使用冷却，D10）。只按精确职
 * 业判定，从不依赖对局状态，因为 Wathe 在 STARTING 阶段缓存库存上限。平板从不出售，按阵营免费发放。
 */
public final class AbyssListenerShopService {
    private static boolean registered;

    private AbyssListenerShopService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        BuildShopEntries.EVENT.register(AbyssListenerShopService::buildEntries);
    }

    private static void buildEntries(PlayerEntity player, BuildShopEntries.ShopContext context) {
        Role role = GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
        if (!AbyssListenerRules.isAbyssListener(role)) {
            return;
        }

        context.clearEntries();
        AccompliceShopRules.entries().forEach(context::addEntry);
        context.addEntry(flaskEntry());
    }

    static ShopEntry flaskEntry() {
        return new ShopEntry.Builder(
                AbyssListenerRules.FLASK_SHOP_ENTRY_ID,
                SparkDlcItems.deepDarkSporeFlask().getDefaultStack(),
                AbyssListenerRules.FLASK_PRICE,
                ShopEntry.Type.TOOL
        ).build();
    }
}
