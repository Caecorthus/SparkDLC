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
        assertTrue(buildScript.contains("dev/caecorthus/sparkdlc/witch/net/SparkWitchPackets.class"));
        assertTrue(buildScript.contains("dev/caecorthus/sparkdlc/witch/net/SparkWitchServerConfirmS2CPacket.class"));
        assertTrue(buildScript.contains(
                "dev/caecorthus/sparkdlc/witch/client/net/version/SparkWitchClientVersionHandshake.class"
        ));
    }

    @Test
    void playStageConfirmationIsWiredToClientConfirmationGate() throws IOException {
        String packetsSource = Files.readString(Path.of(
                "src/main/java/dev/caecorthus/sparkdlc/witch/net/SparkWitchPackets.java"
        ));
        String clientHandshakeSource = Files.readString(Path.of(
                "src/client/java/dev/caecorthus/sparkdlc/witch/client/net/version/SparkWitchClientVersionHandshake.java"
        ));

        assertTrue(packetsSource.contains("ServerPlayConnectionEvents.JOIN"));
        assertTrue(packetsSource.contains("SparkWitchServerConfirmS2CPacket"));
        assertTrue(clientHandshakeSource.contains("ClientPlayNetworking.registerGlobalReceiver"));
        assertTrue(clientHandshakeSource.contains("SparkWitchServerConnection.confirmCompatible"));
        assertTrue(clientHandshakeSource.contains("SparkWitchRoles.refreshAssassinGuessRoleOrder()"));
    }

    @Test
    void packagedJarGuardChecksRuntimeDependencyVersions() throws IOException {
        String buildScript = Files.readString(Path.of("build.gradle"));

        assertTrue(buildScript.contains("\"noellesroles\""));
        assertTrue(buildScript.contains("\"sparkfactionapi\" : \">=${project.sparkfactionapi_version}\""));
        assertTrue(buildScript.contains("\"ratatouille\""));
        assertTrue(buildScript.contains("Included SparkFactionAPI"));
        assertTrue(buildScript.contains("Bundled SparkFactionAPI"));
    }
}
