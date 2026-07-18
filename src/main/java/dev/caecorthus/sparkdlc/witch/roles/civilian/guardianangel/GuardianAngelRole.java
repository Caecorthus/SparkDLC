package dev.caecorthus.sparkdlc.witch.roles.civilian.guardianangel;

import dev.caecorthus.sparkdlc.factionapi.api.FactionIds;
import dev.caecorthus.sparkdlc.factionapi.api.FactionRoleDefinition;
import dev.caecorthus.sparkdlc.witch.SparkWitch;
import dev.doctor4t.wathe.api.Faction;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import net.minecraft.util.Identifier;

/**
 * Defines the non-rollable Civilian identity awarded to a promoted Wraith.
 * 定义冤魂晋升后获得且不会参与开局抽取的平民身份。
 */
public final class GuardianAngelRole {
    public static final Identifier ID = SparkWitch.id("guardian_angel");
    public static final int COLOR = 0x36E51B;

    private GuardianAngelRole() {
    }

    public static FactionRoleDefinition definition() {
        return FactionRoleDefinition.builder(ID, FactionIds.CIVILIAN)
                .color(COLOR)
                .moodType(Role.MoodType.NONE)
                .maxSprintTime(WatheRoles.CIVILIAN.getMaxSprintTime())
                .canSeeTime(WatheRoles.CIVILIAN.canSeeTime())
                .appearanceCondition(context -> false)
                .nativeWatheFaction(Faction.CIVILIAN)
                .build();
    }
}
