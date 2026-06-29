package dev.caecorthus.sparkdlc.assist.client.sound;

import dev.caecorthus.sparkdlc.assist.client.SparkAssistClient;
import dev.caecorthus.sparkdlc.assist.sound.EventSoundVolumeRules;
import net.minecraft.client.sound.SoundInstance;

public final class EventSoundVolumeController {
    private EventSoundVolumeController() {
    }

    public static float adjustedVolume(SoundInstance soundInstance, float originalVolume) {
        if (SparkAssistClient.configManager() == null || soundInstance == null) {
            return originalVolume;
        }
        return EventSoundVolumeRules.scaledVolume(
                soundInstance.getId(),
                originalVolume,
                SparkAssistClient.configManager().config().eventSoundVolume()
        );
    }
}
