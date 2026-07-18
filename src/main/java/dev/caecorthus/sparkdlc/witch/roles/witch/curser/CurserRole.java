package dev.caecorthus.sparkdlc.witch.roles.witch.curser;

import dev.caecorthus.sparkdlc.factionapi.api.FactionRoleDefinition;
import dev.caecorthus.sparkdlc.witch.SparkWitch;
import dev.caecorthus.sparkdlc.witch.SparkWitchFactions;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.util.Identifier;

/**
 * Defines the non-rollable Witch-faction identity awarded to a promoted Wraith.
 * 定义冤魂晋升后获得且不会参与开局抽取的魔女阵营身份。
 */
public final class CurserRole {
    public static final Identifier ID = SparkWitch.id("curser");
    public static final int COLOR = 0xC13838;

    private CurserRole() {
    }

    public static FactionRoleDefinition definition() {
        return FactionRoleDefinition.builder(ID, SparkWitchFactions.WITCH)
                .color(COLOR)
                .moodType(Role.MoodType.NONE)
                .maxSprintTime(-1)
                .canSeeTime(true)
                .appearanceCondition(context -> false)
                .build();
    }
}
