package dev.caecorthus.sparkdlc.witch.roles.civilian.prophet;

import dev.caecorthus.sparkdlc.witch.api.WitchSkillUseContext;
import dev.caecorthus.sparkdlc.witch.api.WitchSkillUseResult;
import dev.caecorthus.sparkdlc.witch.component.WitchPlayerComponent;

public final class ProphetSkillService {
    private ProphetSkillService() {
    }

    public static WitchSkillUseResult use(WitchSkillUseContext context) {
        if (!ProphetRules.isProphet(context.role())) {
            return WitchSkillUseResult.fail("message.sparkwitch.skill.unavailable");
        }
        WitchPlayerComponent component = WitchPlayerComponent.KEY.get(context.player());
        component.beginDeathOmenWindow(ProphetRules.ACTIVE_TICKS);
        return WitchSkillUseResult.successAfterActiveWindow(
                ProphetRules.POST_COOLDOWN_TICKS,
                "message.sparkwitch.skill.death_omen.activated"
        );
    }
}
