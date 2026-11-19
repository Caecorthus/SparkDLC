package dev.caecorthus.sparkdlc.witch.compat;

import dev.caecorthus.sparkdlc.witch.SparkWitchItems;
import dev.caecorthus.sparkdlc.witch.roles.killer.kidnapper.KidnapperKnockoutService;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * 给其他模块调用的稳定入口。
 * Stable entry point for the other modules.
 *
 * <p>当前主要服务 SparkStrength 验尸官伪装为绑架者的场景；各模块已融合进 SparkDLC，
 * SparkStrength 的 {@code SparkWitchCompat} 现在直接调用本类方法（此前为按名称反射）。
 * Used by SparkStrength's Coroner when disguised as the Kidnapper; since the SparkDLC fusion its
 * {@code SparkWitchCompat} calls these methods directly (it used to reflect them by name).</p>
 */
public final class SparkWitchKidnapperCompat {
    private SparkWitchKidnapperCompat() {
    }

    public static ItemStack createKnockoutDrugStack() {
        return SparkWitchItems.knockoutDrug().getDefaultStack();
    }

    public static ShopEntry createKnockoutDrugShopEntry() {
        return KidnapperKnockoutService.shopEntry();
    }

    public static ShopEntry createKnockoutDrugShopEntry(PlayerEntity player) {
        return KidnapperKnockoutService.shopEntry(player);
    }
}
