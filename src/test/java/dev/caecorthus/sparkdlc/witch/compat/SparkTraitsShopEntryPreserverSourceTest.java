package dev.caecorthus.sparkdlc.witch.compat;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SparkTraitsShopEntryPreserverSourceTest {
    private static final Path SOURCE = Path.of(
            "src/main/java/dev/caecorthus/sparkdlc/witch/compat/SparkTraitsShopEntryPreserver.java"
    );

    @Test
    void preservesTheEntireSparkTraitsNamespaceWithoutSparkTraitsImplementationCoupling() throws Exception {
        String source = Files.readString(SOURCE);

        assertTrue(source.contains("private static final String SPARKTRAITS_SHOP_ID_PREFIX = \"sparktraits:\";"));
        assertTrue(source.contains("entry.id().startsWith(SPARKTRAITS_SHOP_ID_PREFIX)"));
        assertTrue(source.contains("containsEntryId"));
        assertTrue(source.contains("保留 SparkTraits 词条系统追加的商店条目"));
        assertFalse(source.contains("dev.caecorthus.sparkdlc.traits.impl"));
        assertFalse(source.contains("dev.caecorthus.sparkdlc.traits.component"));
    }
}
