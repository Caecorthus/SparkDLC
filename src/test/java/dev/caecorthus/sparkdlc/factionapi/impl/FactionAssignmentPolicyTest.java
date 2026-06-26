package dev.caecorthus.sparkdlc.factionapi.impl;

import dev.caecorthus.sparkdlc.factionapi.api.FactionAssignmentContext;
import dev.caecorthus.sparkdlc.factionapi.api.FactionAssignmentPhase;
import dev.caecorthus.sparkdlc.factionapi.api.FactionAssignmentPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FactionAssignmentPolicyTest {
    @Test
    void minimumPlayersPolicyOnlyRequestsSlotsAfterThreshold() {
        FactionAssignmentPolicy policy = FactionAssignmentPolicy.minimumPlayers(8, 1);

        assertEquals(0, policy.desiredSlots(context(7)));
        assertEquals(1, policy.desiredSlots(context(8)));
        assertEquals(1, policy.desiredSlots(context(12)));
    }

    private static FactionAssignmentContext context(int players) {
        return new FactionAssignmentContext(
                null,
                null,
                java.util.List.of(),
                players,
                players,
                FactionAssignmentPhase.BEFORE_CIVILIANS
        );
    }
}
