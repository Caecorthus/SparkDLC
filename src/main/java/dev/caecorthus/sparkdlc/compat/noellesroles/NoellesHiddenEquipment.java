package dev.caecorthus.sparkdlc.compat.noellesroles;

import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesHiddenEquipmentRegistry;
import net.minecraft.item.Item;

/**
 * Registers held items for NoellesRoles' existing equipment-hiding path.
 * 将手持物品注册到 NoellesRoles 现有的装备隐藏流程。
 */
public final class NoellesHiddenEquipment {
    private NoellesHiddenEquipment() {
    }

    /**
     * Idempotently registers an item by identity.
     * 按物品实例幂等注册。
     */
    public static void register(Item item) {
        NoellesHiddenEquipmentRegistry.register(item);
    }
}
