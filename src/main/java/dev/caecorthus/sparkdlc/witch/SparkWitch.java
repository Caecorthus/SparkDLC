package dev.caecorthus.sparkdlc.witch;

import dev.caecorthus.sparkdlc.witch.impl.SparkWitchBuiltInSkills;
import dev.caecorthus.sparkdlc.witch.impl.SparkWitchEvents;
import dev.caecorthus.sparkdlc.witch.net.SparkWitchPackets;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public final class SparkWitch implements ModInitializer {
    public static final String MOD_ID = "sparkwitch";

    @Override
    public void onInitialize() {
        SparkWitchItems.register();
        SparkWitchRoles.register();
        SparkWitchBuiltInSkills.register();
        SparkWitchPackets.register();
        SparkWitchEvents.register();
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
