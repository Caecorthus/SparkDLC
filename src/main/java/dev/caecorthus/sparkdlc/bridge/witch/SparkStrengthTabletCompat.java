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
 * Cross-module seam: SparkWitch touches the SparkStrength tablet by registry id only (SparkStrength always ships in
 * the same jar since the SparkDLC fusion, so there is no mod-presence gate). No SparkStrength class is
 * imported, loaded or reflected here; a missing item simply means "no tablet". SparkWitch never sells or grants
 * the tablet: SparkStrength issues it free at round start (and to players who become eligible mid-round).
 * 跨模块接缝：SparkWitch 只通过注册 id 接触 SparkStrength 平板（融合进 SparkDLC 后 SparkStrength 始终随同一个 jar 发布，
 * 因此不再检查模组是否加载）。此处不导入、不加载、不反射任何 SparkStrength 类；物品缺失即视为“没有平板”。
 * SparkWitch 从不出售或发放平板：SparkStrength 在开局（以及对局中途获得资格时）免费发放。
 */
public final class SparkStrengthTabletCompat {
    public static final String MOD_ID = "sparkdlc";
    public static final Identifier TABLET_ID = Identifier.of(MOD_ID, "tablet");

    private static volatile Item cachedTablet;

    private SparkStrengthTabletCompat() {
    }

    /**
     * The tablet item, resolved lazily after registration and cached once found.
     * 平板物品；注册完成后惰性解析，找到后缓存。
     */
    public static Optional<Item> tabletItem() {
        Item tablet = cachedTablet;
        if (tablet != null) {
            return Optional.of(tablet);
        }
        Optional<Item> resolved = Registries.ITEM.getOrEmpty(TABLET_ID);
        resolved.ifPresent(item -> cachedTablet = item);
        return resolved;
    }

    public static boolean isAvailable() {
        return tabletItem().isPresent();
    }

    public static boolean isTablet(@Nullable ItemStack stack) {
        return stack != null && !stack.isEmpty() && TABLET_ID.equals(Registries.ITEM.getId(stack.getItem()));
    }

    /**
     * The spec's "current inventory": main inventory, hotbar, offhand and the cursor stack. Deliberately not
     * SparkStrength's hotbar-only access rule.
     * 规格中的“当前背包”：主背包、快捷栏、副手与光标上的物品。刻意不采用 SparkStrength 的仅快捷栏规则。
     */
    public static boolean hasTabletAnywhere(@Nullable PlayerEntity player) {
        if (player == null) {
            return false;
        }
        PlayerInventory inventory = player.getInventory();
        for (ItemStack stack : inventory.main) {
            if (isTablet(stack)) {
                return true;
            }
        }
        for (ItemStack stack : inventory.offHand) {
            if (isTablet(stack)) {
                return true;
            }
        }
        return player.currentScreenHandler != null && isTablet(player.currentScreenHandler.getCursorStack());
    }
}
