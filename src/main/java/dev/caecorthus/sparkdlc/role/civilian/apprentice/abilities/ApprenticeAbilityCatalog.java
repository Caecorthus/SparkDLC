package dev.caecorthus.sparkdlc.role.civilian.apprentice.abilities;

import dev.caecorthus.sparkdlc.role.civilian.apprentice.abilities.clairvoyance.ClairvoyanceAbility;
import dev.caecorthus.sparkdlc.role.civilian.apprentice.abilities.healing.HealingAbility;
import dev.caecorthus.sparkdlc.role.civilian.apprentice.abilities.mightyforce.MightyForceAbility;
import dev.caecorthus.sparkdlc.role.civilian.apprentice.abilities.murdersense.MurderSenseAbility;
import dev.caecorthus.sparkdlc.role.civilian.apprentice.abilities.swiftstep.SwiftStepAbility;
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

    // Opening cooldown 60 s -> 30 s (owner 2026-10-06 D10). / 开局冷却 60 秒 -> 30 秒（所有者 2026-10-06 D10）。
    public static final int INITIAL_COOLDOWN_TICKS = GameConstants.getInTicks(0, 30);

    private ApprenticeAbilityCatalog() {
    }
}
