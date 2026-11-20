package dev.caecorthus.sparkdlc.role.civilian.toxicologist;

import dev.caecorthus.sparkdlc.SparkStrengthItems;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Blue Vitriol and Blue Belladonna shop entries, shared by the Toxicologist shop and the Coroner's Toxicologist
 * disguise shop so both stay identical.
 * 蓝矾与蓝颠茄商店条目，由毒理学家商店与验尸官的毒理学家伪装商店共用，保证两边完全一致。
 *
 * <p>Wathe buys by list index and rebuilds the list on both sides, so the entries are always listed, in this order,
 * on both sides; nothing about them depends on state only one side knows.
 * Wathe 按下标购买且双端各自重建列表，因此两端始终按此顺序列出这些条目；条目不依赖任何只有一端知道的状态。</p>
 */
public final class ToxicologistBlueShop {
    private ToxicologistBlueShop() {
    }

    public static List<ShopEntry> entries() {
        return List.of(
                new ShopEntry.Builder(
                        ToxicologistBlueRules.BLUE_VITRIOL_ENTRY_ID,
                        displayStack(SparkStrengthItems.blueVitriol(), "shop.sparkdlc.toxicologist.blue_vitriol"),
                        ToxicologistBlueRules.BLUE_VITRIOL_PRICE,
                        ShopEntry.Type.POISON
                ).onBuy(ToxicologistBlueShop::buyBlueVitriol).build(),
                new ShopEntry.Builder(
                        ToxicologistBlueRules.BLUE_BELLADONNA_ENTRY_ID,
                        displayStack(SparkStrengthItems.blueBelladonna(), "shop.sparkdlc.toxicologist.blue_belladonna"),
                        ToxicologistBlueRules.BLUE_BELLADONNA_PRICE,
                        ShopEntry.Type.POISON
                ).onBuy(ToxicologistBlueShop::buyBlueBelladonna).build()
        );
    }

    private static boolean buyBlueVitriol(PlayerEntity buyer) {
        return giveToHotbar(buyer, SparkStrengthItems.blueVitriol().getDefaultStack());
    }

    /** The fruit is stamped with the buyer's blue marker, so whoever eats it is credited to the buyer.
     *  果实写入购买者的蓝毒标记，任何人吃下都记在购买者名下。 */
    private static boolean buyBlueBelladonna(PlayerEntity buyer) {
        ItemStack fruit = SparkStrengthItems.blueBelladonna().getDefaultStack();
        // Fail closed: the fruit is handed over (and charged) only when it really carries the buyer's marker. An
        // empty stack is never stamped, so the read-back also covers it.
        // 失败时关闭：只有果实确实带有购买者的标记才交付（并扣费）。空物品不会被写入标记，回读同样覆盖这种情况。
        SparkTraitsApi.markStackBluePoison(fruit, buyer.getUuid());
        if (!buyer.getUuid().equals(SparkTraitsApi.getStackBluePoisoner(fruit))) {
            return false;
        }
        return giveToHotbar(buyer, fruit);
    }

    /**
     * Hotbar-only delivery like Wathe's default, but stacks onto an equal stack first; false means not charged.
     * 与 Wathe 默认一样只放入快捷栏，但优先叠加到相同物品上；返回 false 表示不扣费。
     */
    static boolean giveToHotbar(PlayerEntity buyer, ItemStack bought) {
        PlayerInventory inventory = buyer.getInventory();
        int slot = ToxicologistBlueItemRules.hotbarSlot(
                ToxicologistBlueItemRules.HOTBAR_SIZE,
                index -> canMerge(inventory.getStack(index), bought),
                index -> inventory.getStack(index).isEmpty()
        );
        if (slot == ToxicologistBlueItemRules.NO_SLOT) {
            return false;
        }
        ItemStack existing = inventory.getStack(slot);
        if (existing.isEmpty()) {
            inventory.setStack(slot, bought);
        } else {
            existing.increment(bought.getCount());
        }
        return true;
    }

    private static boolean canMerge(ItemStack existing, ItemStack bought) {
        return !existing.isEmpty()
                && ItemStack.areItemsAndComponentsEqual(existing, bought)
                && existing.getCount() + bought.getCount() <= existing.getMaxCount();
    }

    private static ItemStack displayStack(Item item, String translationKey) {
        ItemStack stack = item.getDefaultStack();
        stack.set(DataComponentTypes.ITEM_NAME, Text.translatable(translationKey));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.translatable(translationKey + ".description")
                        .styled(style -> style.withColor(0x808080).withItalic(false))
        )));
        return stack;
    }
}
