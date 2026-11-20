package dev.caecorthus.sparkdlc.compat.noellesroles;

import dev.caecorthus.sparkdlc.SparkDlcRoles;
import dev.caecorthus.sparkdlc.role.witch.WitchFactionRules;
import dev.doctor4t.wathe.api.Role;

/**
 * Pure rules for the roles that can read hidden poison traps. Every accomplice (plain or special) counts.
 * 可额外识别隐藏毒陷阱的纯规则，只授予明确列出的职业；所有共犯（普通或特殊）都计入。
 */
public final class WitchPoisonVisionRules {
    private WitchPoisonVisionRules() {
    }

    public static boolean canSeeHiddenPoison(Role role) {
        return role != null
                && (role == SparkDlcRoles.murderousWitch()
                || WitchFactionRules.isAccompliceLike(role)
                || role == SparkDlcRoles.grandWitch()
                || role == SparkDlcRoles.guardianAngel());
    }
}
