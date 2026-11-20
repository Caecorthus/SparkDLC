package dev.caecorthus.sparkdlc.bridge.witch;

import dev.caecorthus.sparkdlc.SparkWitchItems;
import dev.caecorthus.sparkdlc.role.killer.kidnapper.KidnapperKnockoutService;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * 给其他模块调用的稳定入口。
 * Stable entry point for the other modules.
 *
 * <p>当前主要服务验尸官伪装为绑架者的场景（临时迷药与商店条目）。
 * Used by the Coroner when disguised as the Kidnapper (the loaned knockout drug and its shop entry).</p>
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
