package dev.caecorthus.sparkdlc.role.civilian.seeker.console;

import dev.caecorthus.sparkdlc.tablet.TabletItemLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Stable contract, side-neutral: which stacks act as the Seeker console. The tablet does (matched by registry id),
 * and nothing else. The whole inventory counts (main, hotbar, offhand, cursor).
 * 稳定契约，两端通用：哪些物品可充当搜寻者控制台。平板可以（按注册 id 识别），除此之外没有别的。
 * 整个背包都算（主背包、快捷栏、副手、光标）。
 *
 * <p>While the tablet belonged to another mod, the Search Car item stood in as the console when that mod was absent
 * (a forced Seeker without a tablet item in the game). The tablet is registered by this mod itself now
 * ({@code SparkDlcItems.registerRoleKitItems}), so "the tablet item exists" is always true by the time anything can
 * ask, and the car fallback with its pure rule could not run: removed.
 * 平板还属于另一个模组时，若该模组缺失（游戏里没有平板物品、被强制指定的搜寻者），搜寻小车物品会代替充当控制台。如今平板由本模组
 * 自己注册（{@code SparkDlcItems.registerRoleKitItems}），在任何代码能够询问之前“平板物品存在”就已恒为真，小车兜底及其纯规则
 * 无法执行：已移除。</p>
 */
public final class SeekerConsoleDevices {
    private SeekerConsoleDevices() {
    }

    public static boolean isConsoleDevice(@Nullable PlayerEntity player, @Nullable ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) {
            return false;
        }
        return TabletItemLookup.isTablet(stack);
    }

    /** Whole-inventory scan. / 全背包扫描。 */
    public static boolean hasConsoleDevice(@Nullable PlayerEntity player) {
        if (player == null) {
            return false;
        }
        return TabletItemLookup.hasTabletAnywhere(player);
    }
}
