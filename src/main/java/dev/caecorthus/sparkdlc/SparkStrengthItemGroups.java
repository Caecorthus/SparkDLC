package dev.caecorthus.sparkdlc;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;

import java.util.LinkedHashSet;
import java.util.Set;

public final class SparkStrengthItemGroups {
    /** The single SparkDLC tab ({@link SparkDlcItemGroups}); SparkStrength fills its first section. / SparkDLC 唯一的物品栏。 */
    public static final RegistryKey<ItemGroup> ITEMS_GROUP = SparkDlcItemGroups.ITEMS_GROUP;
    /**
     * The items SparkStrength registered, in registration order. Captured when this module finishes registering,
     * before SparkWitch registers anything: every module shares one namespace, so the namespace alone no longer
     * says which module an item belongs to.
     * SparkStrength 注册的物品，按注册顺序。在本模块注册完成时、SparkWitch 注册任何物品之前记录：所有模块共用一个命名空间，
     * 仅凭命名空间已无法判断物品属于哪个模块。
     */
    private static final Set<Item> OWN_ITEMS = new LinkedHashSet<>();
    private static boolean registered;

    private SparkStrengthItemGroups() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        for (Item item : Registries.ITEM) {
            if (SparkStrength.MOD_ID.equals(Registries.ITEM.getId(item).getNamespace())) {
                OWN_ITEMS.add(item);
            }
        }
        // The admin-only configurator lives in the vanilla operator tab. / 仅限管理员的线路配置器放在原版管理员物品栏。
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
            if (entries.shouldShowOpRestrictedItems()) {
                entries.add(SparkStrengthItems.circuitConfigurator());
            }
        });
        registered = true;
    }

    /** Whether SparkStrength registered {@code item}. / 该物品是否由 SparkStrength 注册。 */
    public static boolean isOwnItem(Item item) {
        return OWN_ITEMS.contains(item);
    }

    /**
     * The SparkStrength section of the tab: every item this module registered, in registration order, so new items
     * join automatically.
     * 物品栏的 SparkStrength 部分：按注册顺序列出本模块注册的全部物品，新增物品无需再手动加入。
     */
    static void addEntries(ItemGroup.Entries entries) {
        for (Item item : OWN_ITEMS) {
            if (item != SparkStrengthItems.circuitConfigurator()) {
                entries.add(item);
            }
        }
    }
}
