package dev.caecorthus.sparkdlc.client.input;

import dev.caecorthus.sparkdlc.client.SparkDLCClient;
import dev.caecorthus.sparkdlc.client.config.SparkDlcConfigManager;
import dev.caecorthus.sparkdlc.config.SparkDlcClientConfig.InstinctKeyMode;
import dev.caecorthus.sparkdlc.input.InstinctKeyRules;
import dev.caecorthus.sparkdlc.input.InstinctToggleState;

public final class InstinctKeyController {
    private static final InstinctToggleState STATE = new InstinctToggleState();

    private InstinctKeyController() {
    }

    public static boolean shouldHandle(String translationKey) {
        return InstinctKeyRules.shouldHandle(translationKey, SparkDLCClient.configManager() != null);
    }

    public static boolean effectivePressed(boolean physicalDown) {
        SparkDlcConfigManager configManager = SparkDLCClient.configManager();
        if (configManager == null) {
            return physicalDown;
        }
        InstinctKeyMode mode = configManager.config().instinctKeyMode();
        return STATE.effectivePressed(mode, physicalDown);
    }

    public static void reset() {
        STATE.reset();
    }
}
