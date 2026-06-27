package dev.caecorthus.sparkdlc.factionapi.impl;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class MixinCompatibilityTest {
    @Test
    void mixinsAvoidKnownSparkTraitsRedirectCollisions() throws IOException {
        String mixinConfig = readProjectFile("src/main/resources/sparkfactionapi.mixins.json");
        assertFalse(mixinConfig.contains("GunShootPayloadReceiverMixin"));

        assertNoWathePredicateRedirect(
                "src/main/java/dev/caecorthus/sparkdlc/factionapi/mixin/PlayerShopComponentMixin.java"
        );
        assertNoWathePredicateRedirect(
                "src/main/java/dev/caecorthus/sparkdlc/factionapi/mixin/MurderGameModeMixin.java"
        );
        assertNoWathePredicateRedirect(
                "src/main/java/dev/caecorthus/sparkdlc/factionapi/mixin/GameFunctionsMixin.java"
        );
    }

    private static void assertNoWathePredicateRedirect(String file) throws IOException {
        String source = readProjectFile(file);
        assertFalse(source.contains("@Redirect") && source.contains("canUseKillerFeatures"));
        assertFalse(source.contains("@Redirect") && source.contains("isInnocent"));
    }

    private static String readProjectFile(String file) throws IOException {
        return Files.readString(Path.of(file));
    }
}
