package dev.caecorthus.sparkdlc.role.special.wraith.progression;

import dev.caecorthus.sparkdlc.SparkDlcRoles;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithState;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.random.RandomGenerator;

final class WraithPromotionRoles {
    private WraithPromotionRoles() {
    }

    static List<Role> pool(WraithState.Alignment alignment) {
        return pool(alignment, true);
    }

    static List<Role> pool(WraithState.Alignment alignment, boolean vendettaEligible) {
        return switch (alignment) {
            case GOOD -> vendettaEligible
                    ? List.of(
                            SparkDlcRoles.windSpirit(),
                            SparkDlcRoles.guardianAngel(),
                            SparkDlcRoles.vendetta()
                    )
                    : List.of(
                            SparkDlcRoles.windSpirit(),
                            SparkDlcRoles.guardianAngel()
                    );
            case KILLER -> List.of(SparkDlcRoles.saboteur());
            case WITCH -> List.of(SparkDlcRoles.curser());
        };
    }

    static Role find(Identifier roleId) {
        if (SparkDlcRoles.WIND_SPIRIT_ID.equals(roleId)) {
            return SparkDlcRoles.windSpirit();
        }
        if (SparkDlcRoles.GUARDIAN_ANGEL_ID.equals(roleId)) {
            return SparkDlcRoles.guardianAngel();
        }
        if (SparkDlcRoles.VENDETTA_ID.equals(roleId)) {
            return SparkDlcRoles.vendetta();
        }
        if (SparkDlcRoles.SABOTEUR_ID.equals(roleId)) {
            return SparkDlcRoles.saboteur();
        }
        if (SparkDlcRoles.CURSER_ID.equals(roleId)) {
            return SparkDlcRoles.curser();
        }
        return null;
    }

    static Role pick(WraithState.Alignment alignment, RandomGenerator random) {
        return pick(alignment, true, random);
    }

    static Role pick(WraithState.Alignment alignment, boolean vendettaEligible, RandomGenerator random) {
        List<Role> pool = pool(alignment, vendettaEligible);
        return pool.get(random.nextInt(pool.size()));
    }

    static boolean shouldExcludeFromAssassinGuess(Role role) {
        return role.canUseKiller() && !SparkDlcRoles.SABOTEUR_ID.equals(role.identifier());
    }
}
