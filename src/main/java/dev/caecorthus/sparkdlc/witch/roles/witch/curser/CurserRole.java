package dev.caecorthus.sparkdlc.witch.roles.witch.curser;

import dev.caecorthus.sparkdlc.factionapi.api.FactionRoleDefinition;
import dev.caecorthus.sparkdlc.witch.SparkWitch;
import dev.caecorthus.sparkdlc.witch.SparkWitchFactions;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.util.Identifier;

/** Non-rollable Witch identity awarded by Wraith promotion. / 冤魂晋升授予的不可随机魔女身份。 */
public final class CurserRole {
    public static final Identifier ROLE_ID = SparkWitch.id("curser");
    public static final Identifier ID = ROLE_ID;
    public static final FactionRoleDefinition DEFINITION =
            FactionRoleDefinition.builder(ROLE_ID, SparkWitchFactions.WITCH)
                    .color(0xC13838)
                    .moodType(Role.MoodType.NONE)
                    .maxSprintTime(-1)
                    .canSeeTime(true)
                    .appearanceCondition(context -> false)
                    .build();

    private CurserRole() {
    }
}
