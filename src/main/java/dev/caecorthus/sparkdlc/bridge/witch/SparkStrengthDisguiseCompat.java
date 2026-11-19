package dev.caecorthus.sparkdlc.bridge.witch;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Cross-module seam: Black Raven disguise kits touch SparkStrength items by registry id only and fail closed
 * (SparkStrength always ships in the same jar since the SparkDLC fusion, so there is no mod-presence gate).
 * No SparkStrength class is imported, loaded or reflected here; a missing item simply grants nothing.
 * 跨模块接缝：黑羽鸦伪装物品只通过注册 id 接触 SparkStrength 物品，缺失时失败关闭
 * （融合进 SparkDLC 后 SparkStrength 始终随同一个 jar 发布，因此不再检查模组是否加载）。
 * 此处不导入、不加载、不反射任何 SparkStrength 类；物品缺失即不发放。
 */
public final class SparkStrengthDisguiseCompat {
    public static final String MOD_ID = "sparkdlc";
    /** SparkStrength's Attendant starter flashlight (SparkStrengthItems.FLASHLIGHT_ID). / SparkStrength 乘务员开局手电筒。 */
    public static final Identifier FLASHLIGHT_ID = Identifier.of(MOD_ID, "flashlight");

    private SparkStrengthDisguiseCompat() {
    }

    /** Always true: SparkStrength is fused into SparkDLC. / 恒为 true：SparkStrength 已融合进 SparkDLC。 */
    public static boolean isLoaded() {
        return true;
    }

    /** A fresh flashlight stack, or EMPTY when the item is absent. / 新手电筒物品；缺失时为 EMPTY。 */
    public static ItemStack flashlightStack() {
        return flashlightItem().map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    /**
     * Mirrors SparkStrength's duplicate guard for the starter flashlight (main, armor and offhand slots).
     * 镜像 SparkStrength 开局手电筒的防重复检查（主背包、护甲与副手）。
     */
    public static boolean hasFlashlight(@Nullable PlayerEntity player) {
        if (player == null) {
            return false;
        }
        Optional<Item> flashlight = flashlightItem();
        if (flashlight.isEmpty()) {
            return false;
        }
        PlayerInventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.size(); slot++) {
            if (inventory.getStack(slot).isOf(flashlight.get())) {
                return true;
            }
        }
        return false;
    }

    private static Optional<Item> flashlightItem() {
        try {
            return Registries.ITEM.getOrEmpty(FLASHLIGHT_ID);
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }
}
