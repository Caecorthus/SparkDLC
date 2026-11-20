package dev.caecorthus.sparkdlc.cooldown.provider;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.cooldown.api.AdminCooldownClear;
import dev.caecorthus.sparkdlc.item.DroneItem;
import dev.caecorthus.sparkdlc.item.m67.M67RoundService;
import dev.caecorthus.sparkdlc.role.civilian.bodyguard.BodyguardShieldService;
import dev.caecorthus.sparkdlc.role.killer.bomber.drone.DroneService;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSkateboardService;
import dev.caecorthus.sparkdlc.sabotage.SabotageService;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Admin path only: {@code /sparkdlc:clearCooldown} just removed {@code item}'s vanilla cooldown for {@code player}
 * ({@link #afterRemove}, an {@link AdminCooldownClear} listener, calls {@link #onCooldownCleared} once the clear
 * took effect). The locks below are re-synced every tick from their own state (Bomber drones, the M67 opening, the
 * Vulture skateboard, the democracy shield, the sabotage tablet), so that remove alone comes back on the next tick;
 * the item's owner releases its own lock, for this player only. Role mechanics that remove cooldowns (Timekeeper
 * refresh, Serial Killer reset) never come through here, and other items need nothing.
 * 仅管理员路径：/sparkdlc:clearCooldown 刚为该玩家移除了该物品的原版冷却（{@link #afterRemove} 是 {@link AdminCooldownClear}
 * 的监听器，在清除生效后调用 {@link #onCooldownCleared}）。以下锁每刻按自身状态重新同步（炸弹客无人机、M67 开局锁、秃鹫滑板、
 * 民主盾牌、破坏平板），仅移除原版冷却会在下一刻恢复；由物品的所属逻辑只为该玩家解除自己的锁。移除冷却的职业机制（计时员刷新、
 * 连环杀手重置）不会经过这里，其他物品无需处理。
 */
public final class AdminClearedItemLocks {
    private static boolean registered;

    private AdminClearedItemLocks() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        AdminCooldownClear.registerAfterRemove(AdminClearedItemLocks::afterRemove);
    }

    /** Only a clear that took effect releases the lock. / 仅在清除确实生效时解除锁定。 */
    static void afterRemove(ServerPlayerEntity player, Item item, boolean wasCooling) {
        if (AdminCooldownClear.tookEffect(player, item)) {
            onCooldownCleared(player, item);
        }
    }

    public static void onCooldownCleared(@Nullable ServerPlayerEntity player, @Nullable Item item) {
        if (player == null || item == null) {
            return;
        }
        if (item instanceof DroneItem) {
            DroneService.onAdminCooldownCleared(player, item);
        } else if (item == SparkDlcItems.m67()) {
            M67RoundService.onAdminCooldownCleared(player);
        } else if (item == SparkDlcItems.skateboard()) {
            VultureSkateboardService.onAdminCooldownCleared(player);
        } else if (item == SparkDlcItems.democracyShield()) {
            BodyguardShieldService.onAdminCooldownCleared(player);
        } else if (item == SparkDlcItems.tablet()) {
            // The killer team's sabotage cooldown and unlock wait. / 杀手阵营的破坏冷却与解锁等待。
            SabotageService.onAdminCooldownCleared(player);
        }
    }
}
