package dev.caecorthus.sparkdlc.assist.client.screen;

import dev.caecorthus.sparkdlc.assist.client.SparkAssistClient;
import dev.caecorthus.sparkdlc.assist.client.sound.EventSoundVolumeController;
import dev.caecorthus.sparkdlc.assist.config.SparkAssistConfig;
import dev.caecorthus.sparkdlc.assist.sound.EventSoundGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

public final class EventSoundOptionsScreen extends GameOptionsScreen {
    private static final Text TITLE = Text.translatable("screen.sparkassist.event_sound_options");

    public EventSoundOptionsScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, TITLE);
    }

    @Override
    protected void addOptions() {
        for (EventSoundGroup group : EventSoundGroup.values()) {
            this.body.addSingleOptionEntry(createEventVolumeOption(group));
        }
    }

    private static SimpleOption<Double> createEventVolumeOption(EventSoundGroup group) {
        return new SimpleOption<>(
                group.translationKey(),
                SimpleOption.emptyTooltip(),
                EventSoundOptionsScreen::eventVolumeText,
                SimpleOption.DoubleSliderCallbacks.INSTANCE,
                SparkAssistClient.configManager().config().eventSoundVolume(group),
                volume -> {
                    SparkAssistConfig config = SparkAssistClient.configManager().config();
                    config.setEventSoundVolume(group, volume);
                    SparkAssistClient.configManager().save();
                    EventSoundVolumeController.refreshPlayingSounds();
                }
        );
    }

    private static Text eventVolumeText(Text optionText, double value) {
        int percent = (int) Math.round(value * 100.0D);
        return Text.translatable("option.sparkassist.event_sound_volume.value", optionText, percent);
    }
}
