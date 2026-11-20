package dev.caecorthus.sparkdlc.role.civilian.attendant;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Gives Attendants their role-kit starter flashlight.
 * 给乘务员发放职业增强的开局手电筒。
 */
public final class AttendantFlashlightService {
    private AttendantFlashlightService() {
    }

    public static void assignForRole(ServerPlayerEntity player, Role role) {
        if (AttendantRules.shouldGiveStarterFlashlight(role, hasFlashlight(player))) {
            player.giveItemStack(starterFlashlight());
        }
    }

    /**
     * A fresh starter flashlight ({@code sparkdlc:flashlight}): what an Attendant gets with the role and what the Black
     * Raven's Attendant disguise kit adds.
     * 一支新的开局手电筒（{@code sparkdlc:flashlight}）：乘务员随职业获得的物品，也是黑羽鸦乘务员伪装物品追加的物品。
     */
    public static ItemStack starterFlashlight() {
        return new ItemStack(SparkDlcItems.flashlight());
    }

    /**
     * The starter flashlight's duplicate guard: any inventory slot (main, armor, offhand) already holds a flashlight.
     * Also asked by the Black Raven's Attendant disguise kit, which follows the same rule.
     * 开局手电筒的防重复检查：任一背包格（主背包、护甲、副手）已有手电筒。黑羽鸦的乘务员伪装物品遵循同一规则，也查询本方法。
     */
    public static boolean hasFlashlight(ServerPlayerEntity player) {
        // 角色分配事件在某些流程里可能重复触发；先检查背包，避免刷出多个开局手电。
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            if (player.getInventory().getStack(slot).isOf(SparkDlcItems.flashlight())) {
                return true;
            }
        }
        return false;
    }
}
