package dev.caecorthus.sparkdlc.role.killer.witchmaiden;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/** Namespaced custom-data marker used only to bridge Wathe's missing cocktail poison call. */
public final class PoisonAppleDrinkMarker {
    /**
     * Renamed from its former-mod name on 2026-11-20 (owner: "就数据可以改"); a stack saved with the old name is
     * renamed on load (legacy.LegacyDataNames).
     * 2026-11-20 起不再使用旧模组名（所有者：“就数据可以改”）；以旧名字保存的物品堆在载入时改名（legacy.LegacyDataNames）。
     */
    private static final String MARKER_KEY = "sparkdlc:poison_apple_drink";

    private PoisonAppleDrinkMarker() {
    }

    public static boolean isMarked(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        return customData != null && customData.copyNbt().getBoolean(MARKER_KEY);
    }

    public static void mark(ItemStack stack) {
        NbtCompound nbt = customData(stack);
        nbt.putBoolean(MARKER_KEY, true);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public static void clear(ItemStack stack) {
        NbtCompound nbt = customData(stack);
        nbt.remove(MARKER_KEY);
        if (nbt.isEmpty()) {
            stack.remove(DataComponentTypes.CUSTOM_DATA);
        } else {
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }
    }

    private static NbtCompound customData(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        return customData == null ? new NbtCompound() : customData.copyNbt();
    }
}
