package dev.caecorthus.sparkdlc.witch.voice;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WraithVoicePluginContractTest {
    @Test
    void activeWraithCommunicationPolicyFiltersSendAndReceiveAtMaximumPriority() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/dev/caecorthus/sparkdlc/witch/voice/SparkWitchVoiceChatPlugin.java"))
                .replaceAll("\\s+", " ");
        assertTrue(source.contains("MicrophonePacketEvent.class, this::blockWraithSpeaker, Integer.MAX_VALUE"));
        assertTrue(source.contains("WraithCommunicationPolicy.shouldBlockCommunication"));
        assertTrue(source.contains("speaker.isCreative()"));
        assertTrue(source.contains("recipient.isCreative()"));
        assertTrue(source.contains("EntitySoundPacketEvent.class, this::blockRestrictedRecipient"));
        assertTrue(source.contains("LocationalSoundPacketEvent.class, this::blockRestrictedRecipient"));
        assertTrue(source.contains("StaticSoundPacketEvent.class, this::blockRestrictedRecipient"));
        assertTrue(source.contains("SaboteurVoiceRules.shouldBlockPacket(event) || shouldBlockWraithRecipient(event)"));
        assertTrue(Files.readString(Path.of(
                "src/main/java/dev/caecorthus/sparkdlc/witch/voice/SaboteurVoiceRules.java"))
                .contains("getReceiverConnection"));
    }
}
