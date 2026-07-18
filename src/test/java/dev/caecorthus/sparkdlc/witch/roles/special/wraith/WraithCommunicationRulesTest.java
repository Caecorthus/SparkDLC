package dev.caecorthus.sparkdlc.witch.roles.special.wraith;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WraithCommunicationRulesTest {
    @Test
    void everyActiveWraithLosesOutgoingTextAndVoice() {
        assertFalse(WraithCommunicationRules.canSendText(true));
        assertFalse(WraithCommunicationRules.canSendVoice(true));
        assertTrue(WraithCommunicationRules.canSendText(false));
        assertTrue(WraithCommunicationRules.canSendVoice(false));
    }
}
