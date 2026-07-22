package dev.caecorthus.sparkdlc.witch.roles.civilian.windspirit;

import dev.caecorthus.sparkdlc.factionapi.api.FactionIds;
import dev.caecorthus.sparkdlc.factionapi.api.FactionRoleDefinition;
import dev.caecorthus.sparkdlc.witch.SparkWitch;
import dev.doctor4t.wathe.api.Faction;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.util.Identifier;

/** Non-rollable civilian identity awarded by Wraith promotion. / 冤魂晋升授予的不可随机平民身份。 */
public final class WindSpiritRole {
    public static final Identifier ROLE_ID = SparkWitch.id("wind_spirit");
    public static final Identifier ID = ROLE_ID;
    public static final int COLOR = 0x59D8E6;
    public static final FactionRoleDefinition DEFINITION = FactionRoleDefinition.builder(ROLE_ID, FactionIds.CIVILIAN)
            .color(COLOR)
            .moodType(Role.MoodType.NONE)
            .maxSprintTime(GameConstants.getInTicks(0, 10))
            .canSeeTime(false)
            .appearanceCondition(context -> false)
            .nativeWatheFaction(Faction.CIVILIAN)
            .build();

    private WindSpiritRole() {
    }
}
