package dev.caecorthus.sparkdlc;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * The one creative tab of SparkDLC. It replaces the two tabs the separate mods had ({@code sparkdlc:items},
 * {@code sparkdlc:items}) and holds what they held: the SparkStrength entries first, then the SparkWitch entries,
 * each in its former order. Operator-only items stay in vanilla's Operator Utilities tab.
 * SparkDLC 唯一的创造模式物品栏。它取代两个独立 mod 各自的物品栏（{@code sparkdlc:items}、{@code sparkdlc:items}），
 * 内容不变：先是 SparkStrength 的条目，再是 SparkWitch 的条目，各自保持原有顺序。管理员专用物品仍只在原版「管理员用品」物品栏。
 */
public final class SparkDlcItemGroups {
    // Stable creative tab id; other mods may target it via ItemGroupEvents.
    // 稳定的创造物品栏标签页 ID，其他模组可通过 ItemGroupEvents 引用。
    public static final RegistryKey<ItemGroup> ITEMS_GROUP =
            RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(SparkDLC.MOD_ID, "items"));
    private static boolean registered;

    private SparkDlcItemGroups() {
    }

    /** Called once, after every module has registered its items. / 在所有模块注册完物品之后调用一次。 */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        Registry.register(
                Registries.ITEM_GROUP,
                ITEMS_GROUP,
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemGroup.sparkdlc.items"))
                        .icon(() -> new ItemStack(SparkStrengthItems.m67()))
                        .entries((displayContext, entries) -> {
                            SparkStrengthItemGroups.addEntries(entries);
                            SparkWitchItemGroups.addEntries(entries);
                        })
                        .build()
        );
        registered = true;
    }
}
