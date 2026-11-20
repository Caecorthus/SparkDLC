package dev.caecorthus.sparkdlc.role.witch.riftwalker.tablet;

import dev.caecorthus.sparkdlc.tablet.TabletItemLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.jetbrains.annotations.Nullable;

/**
 * Both sides: the Rift Gate console's device is the tablet, matched by registry id through {@link TabletItemLookup}
 * (no tablet → no console, gates still work). Unlike the Seeker console it follows the tablet's own hotbar-only rule
 * (slots 0–8), so the "Witch network" re-issue always reaches a tablet the tablet feature accepts.
 * 双端：裂隙门控制台的设备是平板，通过 {@link TabletItemLookup} 按注册 id 识别（没有平板时没有控制台，门照常可用）。
 * 与搜寻者控制台不同，它沿用平板自己的仅快捷栏规则（0–8 格），因此「魔女网络」重发的使用一定落在平板功能认可的平板上。
 */
public final class RiftGateConsoleDevices {
    private RiftGateConsoleDevices() {
    }

    /** A tablet in hotbar slots 0–8. / 快捷栏 0–8 格中有平板。 */
    public static boolean hasTabletInHotbar(@Nullable PlayerEntity player) {
        if (player == null) {
            return false;
        }
        PlayerInventory inventory = player.getInventory();
        for (int slot = 0; slot < RiftGateConsoleRules.HOTBAR_SIZE; slot++) {
            if (TabletItemLookup.isTablet(inventory.getStack(slot))) {
                return true;
            }
        }
        return false;
    }
}
