package dev.caecorthus.sparkdlc.witch.roles.special.wraith;

import dev.caecorthus.sparkdlc.witch.compat.SparkTraitsWraithBridge;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WraithTraitBridgeTest {
    @Test
    void absentOptionalProviderFailsClosed() {
        assertTrue(SparkTraitsWraithBridge.capture(null).isEmpty());
        SparkTraitsWraithBridge.restore(null, null);
        SparkTraitsWraithBridge.clear(null, false);
        assertFalse(SparkTraitsWraithBridge.hasLastStandTriggered(null, null));
        assertFalse(SparkTraitsWraithBridge.didLastStandTriggerSince(null, false));
    }

    @Test
    void reflectionNamesOnlyTheFourApprovedPublicMethods() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/caecorthus/sparkdlc/witch/compat/SparkTraitsWraithBridge.java"));
        assertTrue(source.contains("dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi"));
        assertTrue(source.contains("captureWraithTraitSnapshot"));
        assertTrue(source.contains("restoreWraithTraitSnapshot"));
        assertTrue(source.contains("clearWraithTraits"));
        assertTrue(source.contains("hasLastStandTriggeredThisRound"));
        assertFalse(source.contains("dev.caecorthus.sparkdlc.traits.impl"));
        assertFalse(source.contains("dev.caecorthus.sparkdlc.traits.component"));
    }
}
