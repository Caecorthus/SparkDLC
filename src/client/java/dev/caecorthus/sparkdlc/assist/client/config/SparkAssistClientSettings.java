package dev.caecorthus.sparkdlc.assist.client.config;

import dev.caecorthus.sparkdlc.assist.client.SparkAssistClient;
import dev.caecorthus.sparkdlc.assist.client.input.InstinctKeyController;
import dev.caecorthus.sparkdlc.assist.client.sound.EventSoundVolumeController;
import dev.caecorthus.sparkdlc.assist.config.SparkAssistConfig;
import dev.caecorthus.sparkdlc.assist.config.SparkAssistConfig.GuidebookDecor;
import dev.caecorthus.sparkdlc.assist.config.SparkAssistConfig.InstinctKeyMode;
import dev.caecorthus.sparkdlc.assist.sound.EventSoundGroup;

/**
 * Client settings Adapter that keeps screen Modules shallow.
 * 客户端设置适配器，用于保持界面 Module 的浅层职责。
 */
public final class SparkAssistClientSettings {
    private SparkAssistClientSettings() {
    }

    public static InstinctKeyMode instinctKeyMode() {
        return config().instinctKeyMode();
    }

    public static void setInstinctKeyMode(InstinctKeyMode mode) {
        SparkAssistConfigManager manager = SparkAssistClient.configManager();
        manager.config().setInstinctKeyMode(mode);
        manager.save();
        InstinctKeyController.reset();
    }

    public static double eventSoundVolume(EventSoundGroup group) {
        return config().eventSoundVolume(group);
    }

    public static void setEventSoundVolume(EventSoundGroup group, double volume) {
        SparkAssistConfigManager manager = SparkAssistClient.configManager();
        manager.config().setEventSoundVolume(group, volume);
        manager.save();
        EventSoundVolumeController.refreshPlayingSounds();
    }

    public static GuidebookDecor guidebookDecor() {
        return config().guidebookDecor();
    }

    public static void setGuidebookDecor(GuidebookDecor decor) {
        SparkAssistConfigManager manager = SparkAssistClient.configManager();
        manager.config().setGuidebookDecor(decor);
        manager.save();
    }

    public static boolean guidebookFoliage() {
        return config().guidebookFoliage();
    }

    public static void setGuidebookFoliage(boolean foliage) {
        SparkAssistConfigManager manager = SparkAssistClient.configManager();
        manager.config().setGuidebookFoliage(foliage);
        manager.save();
    }

    private static SparkAssistConfig config() {
        return SparkAssistClient.configManager().config();
    }
}
