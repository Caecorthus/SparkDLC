package dev.caecorthus.sparkdlc.faction.vision;

import dev.caecorthus.sparkdlc.faction.capability.FactionCapabilityLookup;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public final class FactionCohortRules {
    private FactionCohortRules() {
    }

    public static boolean sharesCohort(
            PlayerEntity viewer,
            PlayerEntity target,
            GameWorldComponent gameComponent
    ) {
        Identifier viewerFaction = FactionCapabilityLookup.effectiveFaction(viewer, gameComponent);
        Identifier targetFaction = FactionCapabilityLookup.effectiveFaction(target, gameComponent);
        return viewerFaction.equals(targetFaction)
                && FactionCapabilityLookup.capabilitiesForFaction(viewerFaction).sharesCohort()
                && FactionCapabilityLookup.capabilitiesForFaction(targetFaction).sharesCohort();
    }
}
