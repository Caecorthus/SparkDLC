package dev.caecorthus.sparkdlc.factionapi;

import dev.caecorthus.sparkdlc.factionapi.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.factionapi.command.SparkFactionAdminCommands;
import dev.caecorthus.sparkdlc.factionapi.impl.FactionCompatibilityEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;

public final class SparkFactionApiMod implements ModInitializer {
    public static final String MOD_ID = "sparkfactionapi";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        SparkFactionApi.bootstrap();
        FactionCompatibilityEvents.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SparkFactionAdminCommands.register(dispatcher));
    }
}
