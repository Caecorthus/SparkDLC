package dev.caecorthus.sparkdlc.client.hooks;

import dev.caecorthus.sparkdlc.role.witch.WitchFactionRules;
import dev.caecorthus.sparkdlc.net.version.SparkWitchServerConnection;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Client-side Grand Witch cohort checks shared by events and rendering.
 * 客户端大魔女同伙判断，供事件隐藏默认文本和自定义渲染共用。
 */
public final class WitchCohortClientHooks {
    public static final int WITCH_COHORT_COLOR = 0xE9D5F0;

    private WitchCohortClientHooks() {
    }

    public static boolean isGrandWitchCohortPair(PlayerEntity viewer, PlayerEntity target) {
        if (!SparkWitchServerConnection.isConfirmedServer()) {
            return false;
        }
        GameWorldComponent gameComponent = GameWorldComponent.KEY.get(viewer.getWorld());
        Role viewerRole = gameComponent.getRole(viewer);
        Role targetRole = gameComponent.getRole(target);
        return WitchFactionRules.isWitchFactionMember(viewerRole)
                && WitchFactionRules.isWitchFactionMember(targetRole)
                && !viewer.getUuid().equals(target.getUuid());
    }
}
