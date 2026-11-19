package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.event.SparkStrengthEvents;
import dev.caecorthus.sparkdlc.net.SparkStrengthPackets;
import dev.caecorthus.sparkdlc.replay.role.SparkStrengthReplayFormatters;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SparkStrength implements ModInitializer {
    public static final String MOD_ID = "sparkdlc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        SparkStrengthSounds.initialize();
        SparkStrengthEntities.register();
        SparkStrengthDataComponents.register();
        SparkStrengthBlocks.register();
        SparkStrengthItems.register();
        SparkStrengthItemGroups.register();
        SparkStrengthPackets.registerServer();
        SparkStrengthEvents.register();
        SparkStrengthReplayFormatters.register();
    }
}
