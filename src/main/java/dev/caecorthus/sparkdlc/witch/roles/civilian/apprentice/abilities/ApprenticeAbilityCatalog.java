package dev.caecorthus.sparkdlc.witch.roles.civilian.apprentice.abilities;

import dev.caecorthus.sparkdlc.witch.roles.civilian.apprentice.abilities.Clairvoyance.ClairvoyanceAbility;
import dev.caecorthus.sparkdlc.witch.roles.civilian.apprentice.abilities.Healing.HealingAbility;
import dev.caecorthus.sparkdlc.witch.roles.civilian.apprentice.abilities.MightyForce.MightyForceAbility;
import dev.caecorthus.sparkdlc.witch.roles.civilian.apprentice.abilities.MurderSense.MurderSenseAbility;
import dev.caecorthus.sparkdlc.witch.roles.civilian.apprentice.abilities.SwiftStep.SwiftStepAbility;
import dev.doctor4t.wathe.game.GameConstants;
import java.util.List;
import net.minecraft.util.Identifier;

public final class ApprenticeAbilityCatalog {
    public static final List<Identifier> ABILITY_IDS = List.of(
            MightyForceAbility.ID,
            SwiftStepAbility.ID,
            MurderSenseAbility.ID,
            HealingAbility.ID,
            ClairvoyanceAbility.ID
    );

    public static final int INITIAL_COOLDOWN_TICKS = GameConstants.getInTicks(1, 0);

    private ApprenticeAbilityCatalog() {
    }
}
