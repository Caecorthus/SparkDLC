package dev.caecorthus.sparkdlc.assist.client.input;

import dev.caecorthus.sparkdlc.assist.client.SparkAssistClient;
import dev.caecorthus.sparkdlc.assist.client.config.SparkAssistConfigManager;
import dev.caecorthus.sparkdlc.assist.config.SparkAssistConfig.InstinctKeyMode;
import dev.caecorthus.sparkdlc.assist.input.InstinctKeyRules;
import dev.caecorthus.sparkdlc.assist.input.InstinctToggleState;

public final class InstinctKeyController {
    private static final InstinctToggleState STATE = new InstinctToggleState();

    private InstinctKeyController() {
    }

    public static boolean shouldHandle(String translationKey) {
        return InstinctKeyRules.shouldHandle(translationKey, SparkAssistClient.configManager() != null);
    }

    public static boolean effectivePressed(boolean physicalDown) {
        SparkAssistConfigManager configManager = SparkAssistClient.configManager();
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
