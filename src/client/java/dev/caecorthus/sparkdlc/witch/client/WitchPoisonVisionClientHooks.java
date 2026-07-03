package dev.caecorthus.sparkdlc.witch.client;

import dev.caecorthus.sparkdlc.witch.impl.WitchPoisonVisionRules;
import dev.caecorthus.sparkdlc.witch.net.SparkWitchServerConnection;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Client bridge for wathe's hidden poison particle visibility event.
 * wathe 隐藏毒粒子可见性事件的客户端桥接，只读取本地同步到的职业状态。
 */
public final class WitchPoisonVisionClientHooks {
    private WitchPoisonVisionClientHooks() {
    }

    public static boolean canSeeHiddenPoison(PlayerEntity viewer) {
        if (!SparkWitchServerConnection.isConfirmedServer() || viewer == null) {
            return false;
        }
        Role role = GameWorldComponent.KEY.get(viewer.getWorld()).getRole(viewer);
        return WitchPoisonVisionRules.canSeeHiddenPoison(role);
    }
}
