package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.command.admin.SparkFactionAdminCommands;
import dev.caecorthus.sparkdlc.faction.FactionCompatibilityEvents;
import dev.caecorthus.sparkdlc.round.antifastround.AntiFastRoundBootstrap;
import dev.caecorthus.sparkdlc.round.mapreset.LobbyMapResetBootstrap;
import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesCollisionCompatibility;
import dev.caecorthus.sparkdlc.record.MatchRecordBroadcaster;
import dev.caecorthus.sparkdlc.record.PsychoRecorder;
import dev.caecorthus.sparkdlc.replay.SparkReplayBootstrap;
import dev.caecorthus.sparkdlc.team.CaecorthusTeamSync;
import dev.caecorthus.sparkdlc.net.version.ServerVersionHandshake;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SparkFactionApiMod implements ModInitializer {
    public static final String MOD_ID = "sparkdlc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ServerVersionHandshake.registerServer();
        SparkFactionApi.bootstrap();
        NoellesCollisionCompatibility.register();
        FactionCompatibilityEvents.register();
        SparkReplayBootstrap.register();
        MatchRecordBroadcaster.register();
        PsychoRecorder.register();
        AntiFastRoundBootstrap.register();
        LobbyMapResetBootstrap.register();
        CaecorthusTeamSync.start();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SparkFactionAdminCommands.register(dispatcher));
    }
}
