package dev.caecorthus.sparkdlc.compat.noellesroles;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

/**
 * Registers held items for NoellesRoles' existing equipment-hiding path; {@code HiddenEquipmentHelperMixin} asks
 * {@link #shouldHide} after NoellesRoles' own decision.
 * 将手持物品注册到 NoellesRoles 现有的装备隐藏流程；{@code HiddenEquipmentHelperMixin} 在 NoellesRoles 自身判定之后
 * 询问 {@link #shouldHide}。
 */
public final class NoellesHiddenEquipmentRegistry {
    private static final Set<Item> HIDDEN_ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());

    private NoellesHiddenEquipmentRegistry() {
    }

    /**
     * Idempotently registers an item by identity.
     * 按物品实例幂等注册。
     */
    public static void register(Item item) {
        HIDDEN_ITEMS.add(Objects.requireNonNull(item, "item"));
    }

    public static boolean shouldHide(ItemStack stack) {
        return !stack.isEmpty() && HIDDEN_ITEMS.contains(stack.getItem());
    }
}
