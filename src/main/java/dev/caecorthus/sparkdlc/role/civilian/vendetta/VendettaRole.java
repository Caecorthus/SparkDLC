package dev.caecorthus.sparkdlc.role.civilian.vendetta;

import dev.caecorthus.sparkdlc.faction.api.FactionIds;
import dev.caecorthus.sparkdlc.faction.api.FactionRoleDefinition;
import dev.caecorthus.sparkdlc.SparkDLC;
import dev.doctor4t.wathe.api.Faction;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.util.Identifier;

/** Non-rollable civilian identity awarded by Wraith promotion. / 冤魂晋升授予的不可随机平民身份。 */
public final class VendettaRole {
    public static final Identifier ROLE_ID = SparkDLC.id("vendetta");
    public static final Identifier ID = ROLE_ID;
    public static final int COLOR = 0xE34B5F;
    public static final FactionRoleDefinition DEFINITION = FactionRoleDefinition.builder(ROLE_ID, FactionIds.CIVILIAN)
            .color(COLOR)
            .moodType(Role.MoodType.NONE)
            .maxSprintTime(-1)
            .canSeeTime(false)
            .appearanceCondition(context -> false)
            .nativeWatheFaction(Faction.CIVILIAN)
            .build();

    private VendettaRole() {
    }
}
