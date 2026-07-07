package dev.caecorthus.sparkdlc.traits.impl.compatibility.sparkfactionapi;

import dev.caecorthus.sparkdlc.traits.impl.effective.EffectiveTraitService;
import dev.caecorthus.sparkdlc.traits.impl.traits.civilian.impostor.ImpostorTrait;
import dev.caecorthus.sparkdlc.traits.impl.traits.killer.conscience.ConscienceTrait;
import dev.doctor4t.wathe.api.WatheRoles;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SparkFactionApiEffectiveFactionBridgeTest {
    private static final Identifier CIVILIAN = Identifier.of("wathe", "civilian");
    private static final Identifier KILLER = Identifier.of("wathe", "killer");
    private static final Identifier MURDEROUS_WITCH = Identifier.of("sparkwitch", "murderous_witch");

    @Test
    void ordinaryPlayersKeepSparkFactionApiCurrentFaction() {
        assertNull(SparkFactionApiEffectiveFactionBridge.resolveEffectiveFaction(Set.of(), CIVILIAN));
        assertNull(SparkFactionApiEffectiveFactionBridge.resolveEffectiveFaction(Set.of(), KILLER));
    }

    @Test
    void impostorTurnsRawCivilianIntoEffectiveKillerFaction() {
        Set<Identifier> traits = Set.of(ImpostorTrait.ID);

        assertEquals(KILLER, SparkFactionApiEffectiveFactionBridge.resolveEffectiveFaction(traits, CIVILIAN));
        assertTrue(EffectiveTraitService.isEffectiveKiller(WatheRoles.CIVILIAN, traits));
        assertFalse(EffectiveTraitService.isEffectiveCivilian(WatheRoles.CIVILIAN, traits));
    }

    @Test
    void conscienceTurnsRawKillerIntoEffectiveCivilianFaction() {
        Set<Identifier> traits = Set.of(ConscienceTrait.ID);

        assertEquals(CIVILIAN, SparkFactionApiEffectiveFactionBridge.resolveEffectiveFaction(traits, KILLER));
        assertTrue(EffectiveTraitService.isEffectiveCivilian(WatheRoles.KILLER, traits));
        assertFalse(EffectiveTraitService.isEffectiveKiller(WatheRoles.KILLER, traits));
    }

    @Test
    void customEffectiveFactionsAreNotOverwritten() {
        assertNull(SparkFactionApiEffectiveFactionBridge.resolveEffectiveFaction(
                Set.of(ImpostorTrait.ID),
                MURDEROUS_WITCH
        ));
        assertNull(SparkFactionApiEffectiveFactionBridge.resolveEffectiveFaction(
                Set.of(ConscienceTrait.ID),
                MURDEROUS_WITCH
        ));
    }

    @Test
    void sparkFactionApi67KillerShopGateKeepsRawWatheShopAccess() {
        StackTraceElement[] shopStack = shopStack(
                "dev.caecorthus.sparkdlc.factionapi.impl.shop.FactionShopAccessRules",
                "canUseKillerShop"
        );

        assertTrue(SparkFactionApiEffectiveFactionBridge.isSparkFactionApiKillerShopCapabilityLookup(shopStack));
        assertNull(SparkFactionApiEffectiveFactionBridge.resolveEffectiveFaction(
                Set.of(ImpostorTrait.ID),
                CIVILIAN,
                shopStack
        ));
        assertNull(SparkFactionApiEffectiveFactionBridge.resolveEffectiveFaction(
                Set.of(ConscienceTrait.ID),
                KILLER,
                shopStack
        ));
    }

    @Test
    void sparkFactionApi66KillerShopGateKeepsRawWatheShopAccess() {
        StackTraceElement[] shopStack = shopStack(
                "dev.caecorthus.sparkdlc.factionapi.impl.FactionCapabilityBridge",
                "canUseKillerFeatureAccess"
        );

        assertTrue(SparkFactionApiEffectiveFactionBridge.isSparkFactionApiKillerShopCapabilityLookup(shopStack));
        assertNull(SparkFactionApiEffectiveFactionBridge.resolveEffectiveFaction(
                Set.of(ImpostorTrait.ID),
                CIVILIAN,
                shopStack
        ));
        assertNull(SparkFactionApiEffectiveFactionBridge.resolveEffectiveFaction(
                Set.of(ConscienceTrait.ID),
                KILLER,
                shopStack
        ));
    }

    private static StackTraceElement[] shopStack(String sparkFactionClass, String sparkFactionMethod) {
        return new StackTraceElement[] {
                new StackTraceElement(sparkFactionClass, sparkFactionMethod, "Unknown Source", -1),
                new StackTraceElement(
                        "dev.doctor4t.wathe.game.KillerShopBuilder",
                        "buildShop",
                        "KillerShopBuilder.java",
                        34
                )
        };
    }
}
