package dev.caecorthus.sparkdlc.tablet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The tablet item ({@code sparkdlc:tablet}) as the features that use it as a console see it (the Seeker console, the
 * Rift Gate console): whether it exists, whether a stack is one, and whether a player carries one anywhere. It is
 * looked up lazily by registry id, so asking never loads the item registry class and an item that is not registered
 * (yet) simply means "no tablet"; the Seeker's car-as-console fallback and its draw condition hang on that answer.
 * Those features never sell or grant the tablet: it is issued free at round start (and to players who become eligible
 * mid-round). Possession for the tablet's own channels is the hotbar-only rule in {@link TabletAccess}.
 * 把平板物品（{@code sparkdlc:tablet}）当作控制台使用的功能（搜寻者控制台、裂隙门控制台）所看到的平板：它是否存在、某个物品堆
 * 是否是平板、玩家是否在任意位置携带平板。平板按注册 id 惰性查找，因此查询不会加载物品注册类，物品（尚）未注册即视为
 * “没有平板”；搜寻者“小车充当控制台”的兜底与其抽取条件都取决于这一回答。这些功能从不出售或发放平板：平板在开局
 * （以及对局中途获得资格时）免费发放。平板自身频道的持有判定是 {@link TabletAccess} 中的仅快捷栏规则。
 */
public final class TabletItemLookup {
    public static final String MOD_ID = "sparkdlc";
    public static final Identifier TABLET_ID = Identifier.of(MOD_ID, "tablet");

    private static volatile Item cachedTablet;

    private TabletItemLookup() {
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
