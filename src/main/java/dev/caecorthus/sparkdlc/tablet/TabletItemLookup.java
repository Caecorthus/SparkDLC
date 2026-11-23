package dev.caecorthus.sparkdlc.tablet;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The tablet item ({@code sparkdlc:tablet}) as the features that use it as a console see it (the Seeker console, the
 * Rift Gate console): whether a stack is one, and whether a player carries one anywhere. A stack is matched against the
 * registered item ({@code SparkDlcItems.tablet()}). Those features never sell or grant the tablet: it is issued free at
 * round start (and to players who become eligible mid-round). Possession for the tablet's own channels is the
 * hotbar-only rule in {@link TabletAccess}.
 * 把平板物品（{@code sparkdlc:tablet}）当作控制台使用的功能（搜寻者控制台、裂隙门控制台）所看到的平板：某个物品堆是否是平板、
 * 玩家是否在任意位置携带平板。物品堆与已注册的物品（{@code SparkDlcItems.tablet()}）比对。这些功能从不出售或发放平板：平板
 * 在开局（以及对局中途获得资格时）免费发放。平板自身频道的持有判定是 {@link TabletAccess} 中的仅快捷栏规则。
 *
 * <p>There is no "does the tablet item exist" question any more: this mod registers the tablet itself, unconditionally,
 * during start-up, so the answer was always yes (the lazy registry lookup and its cache are removed with the Seeker's
 * branches that hung on it).
 * 不再有“平板物品是否存在”这个问题：本模组在启动时无条件地自行注册平板，答案恒为是（惰性注册表查找及其缓存，连同搜寻者依赖它的
 * 分支一并移除）。</p>
 */
public final class TabletItemLookup {
    private TabletItemLookup() {
    }

    public static boolean isTablet(@Nullable ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.isOf(SparkDlcItems.tablet());
    }

    /**
     * The spec's "current inventory": main inventory, hotbar, offhand and the cursor stack. Deliberately not the
     * tablet's own hotbar-only access rule ({@link TabletAccess#hasTabletInHotbar}).
     * 规格中的“当前背包”：主背包、快捷栏、副手与光标上的物品。刻意不采用平板自身的仅快捷栏规则
     * （{@link TabletAccess#hasTabletInHotbar}）。
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
