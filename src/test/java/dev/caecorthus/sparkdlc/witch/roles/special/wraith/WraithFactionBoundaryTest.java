package dev.caecorthus.sparkdlc.witch.roles.special.wraith;

import dev.caecorthus.sparkdlc.factionapi.api.FactionIds;
import dev.caecorthus.sparkdlc.factionapi.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.witch.SparkWitchFactions;
import dev.caecorthus.sparkdlc.witch.SparkWitchRoles;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WraithFactionBoundaryTest {
    @BeforeAll
    static void registerRoles() {
        SparkWitchRoles.register();
    }

    @Test
    void savedAlignmentOverridesOnlyRestrictedWraith() {
        assertEquals(FactionIds.CIVILIAN,
                WraithFactionService.restrictedFaction(true, WraithState.Alignment.GOOD));
        assertEquals(FactionIds.KILLER,
                WraithFactionService.restrictedFaction(true, WraithState.Alignment.KILLER));
        assertNull(WraithFactionService.restrictedFaction(false, WraithState.Alignment.KILLER));
        assertEquals(SparkWitchFactions.WITCH,
                SparkFactionApi.resolveBaseFaction(SparkWitchRoles.curser()));
    }

    @Test
    void promotionNeverResurrectsCurserOrRemovesWatheDeadMembership() throws Exception {
        String promotion = Files.readString(Path.of(
                "src/main/java/dev/caecorthus/sparkdlc/witch/roles/special/wraith/WraithPromotionService.java"));
        assertTrue(promotion.contains("wraith.promote()"));
        assertTrue(promotion.contains("WraithRoleTransitionService.transition(player, role)"));
        assertTrue(!promotion.contains("removeDeadPlayer") && !promotion.contains("setPlayerAlive"));
    }
}
