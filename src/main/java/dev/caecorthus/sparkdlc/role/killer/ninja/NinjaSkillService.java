package dev.caecorthus.sparkdlc.role.killer.ninja;

import dev.caecorthus.sparkdlc.skill.api.WitchSkillUseContext;
import dev.caecorthus.sparkdlc.skill.api.WitchSkillUseResult;
import dev.caecorthus.sparkdlc.component.WitchPlayerComponent;

/**
 * Server-authoritative entry point for Ninja's short parry window.
 * 忍者短暂格挡窗口的服务端权威入口。
 */
public final class NinjaSkillService {
    private NinjaSkillService() {
    }

    public static WitchSkillUseResult use(WitchSkillUseContext context) {
        if (!NinjaRules.isNinja(context.role())) {
            return WitchSkillUseResult.fail("message.sparkwitch.skill.unavailable");
        }
        WitchPlayerComponent component = WitchPlayerComponent.KEY.get(context.player());
        if (component.isNinjaParryActive()) {
            return WitchSkillUseResult.fail("message.sparkwitch.skill.unavailable");
        }

        component.beginNinjaParryWindow(NinjaRules.PARRY_WINDOW_TICKS);
        return WitchSkillUseResult.successAfterActiveWindow(
                NinjaRules.PARRY_COOLDOWN_TICKS,
                "message.sparkwitch.skill.ninja_parry.activated"
        );
    }
}
