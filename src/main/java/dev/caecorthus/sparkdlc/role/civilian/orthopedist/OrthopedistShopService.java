package dev.caecorthus.sparkdlc.role.civilian.orthopedist;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.SparkDlcRoles;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.doctor4t.wathe.api.event.BuildShopEntries;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * The Orthopedist's shop (owner 2026-11-28): the Exoskeleton, {@link ExoskeletonRules#PRICE} coins, unlimited stock,
 * added on both sides and passed through the Charisma trait's discount like the mod's other role shops. The role gate
 * ignores the running state because Wathe builds the list on both sides, including while the round is still STARTING.
 * 骨科大夫的商店（所有者 2026-11-28）：外骨骼，{@link ExoskeletonRules#PRICE} 金币，不限购；在两端加入，并像本模组其他职业
 * 商店一样经过魅力词条的折扣。职业门槛不看运行状态，因为 Wathe 在两端构建列表，包括对局仍处于 STARTING 时。
 */
public final class OrthopedistShopService {
    private static boolean registered;

    private OrthopedistShopService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        BuildShopEntries.EVENT.register(OrthopedistShopService::buildEntries);
    }

    private static void buildEntries(PlayerEntity player, BuildShopEntries.ShopContext context) {
        // Widened by the Black Raven acting overlay; getRole stays raw. The disguise shop keeps this entry by its id
        // (OrthopedistDisguiseAdapter). / 黑羽鸦扮演覆盖层会放宽此判定；getRole 仍为真实身份。伪装商店按条目 id 保留本条目
        // （OrthopedistDisguiseAdapter）。
        if (!GameWorldComponent.KEY.get(player.getWorld()).isRole(player, SparkDlcRoles.orthopedist())) {
            return;
        }
        ShopEntry exoskeleton = new ShopEntry.Builder(
                ExoskeletonRules.SHOP_ENTRY_ID,
                new ItemStack(SparkDlcItems.exoskeleton()),
                ExoskeletonRules.PRICE,
                ShopEntry.Type.TOOL
        ).build();
        context.addEntry(SparkTraitsApi.discountShopEntryForCharisma(player, exoskeleton));
    }
}
