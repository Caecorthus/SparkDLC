package dev.caecorthus.sparkdlc.witch;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PackagingGuardTest {
    @Test
    void packagedJarGuardRequiresClientVersionHandshakeClasses() throws IOException {
        String buildScript = Files.readString(Path.of("build.gradle"));

        assertTrue(buildScript.contains("dev/caecorthus/sparkdlc/witch/client/SparkWitchClient.class"));
        assertTrue(buildScript.contains("dev/caecorthus/sparkdlc/witch/net/SparkWitchServerConnection.class"));
        assertTrue(buildScript.contains(
                "dev/caecorthus/sparkdlc/witch/client/net/SparkWitchClientVersionHandshake.class"
        ));
    }
}
