package dev.caecorthus.sparkdlc.client.role.civilian.guardianangel;

import dev.caecorthus.sparkdlc.skill.api.SparkWitchApi;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelRules;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/** Resolves the Guardian Angel's owner-private shield outline. / 裁决守护天使仅本人可见的护盾描边。 */
public final class GuardianAngelClientHooks {
    private GuardianAngelClientHooks() {
    }

    /** Runs before Wraith privacy resolution. / 先于冤魂隐私裁决，使护盾目标保持隔墙可见。 */
    public static @Nullable Integer shieldTargetHighlight(PlayerEntity viewer, PlayerEntity target) {
        if (!isActiveGuardianAngel(viewer) || target == null || target == viewer) {
            return null;
        }
        GuardianAngelPlayerComponent component = GuardianAngelPlayerComponent.KEY.get(viewer);
        return component.hasActiveShieldTarget()
                && target.getUuid().equals(component.getShieldTargetUuid())
                ? GuardianAngelRules.COLOR
                : null;
    }

    private static boolean isActiveGuardianAngel(PlayerEntity player) {
        if (!SparkDlcServerConnection.isConfirmedServer()
                || player == null
                || !SparkWitchApi.isWraithActive(player)) {
            return false;
        }
        Role role = GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
        return GuardianAngelRules.isGuardianAngel(role);
    }
}
