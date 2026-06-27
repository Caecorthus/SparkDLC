package dev.caecorthus.sparkdlc.witch.impl;

import dev.caecorthus.sparkdlc.witch.api.WitchSkillDefinition;
import dev.caecorthus.sparkdlc.witch.api.WitchSkillRegistry;

public final class SparkWitchBuiltInSkills {
    private static boolean registered;

    private SparkWitchBuiltInSkills() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        WitchSkillRegistry.register(new WitchSkillDefinition(
                GrandWitchActiveSkillService.CEREMONIAL_SWORD_SKILL_ID,
                0xF2DFF7,
                1,
                0,
                context -> GrandWitchRules.isGrandWitch(context.role()),
                GrandWitchActiveSkillService::use
        ));
    }
}
