package dev.caecorthus.sparkdlc.strength;

import dev.caecorthus.sparkdlc.strength.command.SparkStrengthCommands;
import dev.caecorthus.sparkdlc.strength.event.SparkStrengthEvents;
import dev.caecorthus.sparkdlc.strength.network.SparkStrengthPackets;
import dev.caecorthus.sparkdlc.strength.replay.SparkStrengthReplayFormatters;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SparkStrength implements ModInitializer {
    public static final String MOD_ID = "sparkstrength";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        SparkStrengthSounds.initialize();
        SparkStrengthEntities.register();
        SparkStrengthItems.register();
        SparkStrengthItemGroups.register();
        SparkStrengthPackets.registerServer();
        SparkStrengthEvents.register();
        SparkStrengthReplayFormatters.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SparkStrengthCommands.register(dispatcher));
    }
}
