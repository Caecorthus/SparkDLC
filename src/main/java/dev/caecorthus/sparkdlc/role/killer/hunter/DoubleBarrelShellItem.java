package dev.caecorthus.sparkdlc.role.killer.hunter;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.util.Identifier;

/**
 * Buckshot, the 9-pellet spread (renamed from the plain shell 2026-10-29; the item id is unchanged).
 * 霰弹：9 颗弹丸的散射（2026-10-29 由普通弹药更名，物品 id 不变）。
 */
public final class DoubleBarrelShellItem extends DoubleBarrelAmmoItem {
    public static final Identifier ID = SparkDLC.id("double_barrel_shell");

    public DoubleBarrelShellItem(Settings settings) {
        super(settings);
    }

    @Override
    public int shellType() {
        return HunterRules.SHELL_BUCKSHOT;
    }
}
