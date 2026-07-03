package dev.caecorthus.sparkdlc.traits.impl;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PackagingGuardTest {
    @Test
    void packagedJarGuardRequiresClientVersionHandshakeClasses() throws IOException {
        String buildScript = Files.readString(Path.of("build.gradle"));

        assertTrue(buildScript.contains("dev/caecorthus/sparkdlc/traits/client/SparkTraitsClient.class"));
        assertTrue(buildScript.contains(
                "dev/caecorthus/sparkdlc/traits/client/net/SparkTraitsClientVersionHandshake.class"
        ));
    }
}
