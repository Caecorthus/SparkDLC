package dev.caecorthus.sparkdlc.strength;

import dev.caecorthus.sparkdlc.strength.event.SparkStrengthEvents;
import dev.caecorthus.sparkdlc.strength.network.SparkStrengthPackets;
import dev.caecorthus.sparkdlc.strength.replay.SparkStrengthReplayFormatters;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public final class SparkStrength implements ModInitializer {
    public static final String MOD_ID = "sparkstrength";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        SparkStrengthPackets.registerServer();
        SparkStrengthEvents.register();
        SparkStrengthReplayFormatters.register();
    }
}
