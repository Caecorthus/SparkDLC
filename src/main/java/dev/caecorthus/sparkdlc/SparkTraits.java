package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.SparkTraitsDataComponentTypes;
import dev.caecorthus.sparkdlc.trait.civilian.jester.JesterTraitService;
import dev.caecorthus.sparkdlc.trait.civilian.laststand.LastStandFinalMomentService;
import dev.caecorthus.sparkdlc.trait.civilian.laststand.LastStandService;
import dev.caecorthus.sparkdlc.trait.registry.SparkTraitsBuiltInTraits;
import dev.caecorthus.sparkdlc.trait.replay.SparkTraitsReplayEvents;
import dev.caecorthus.sparkdlc.trait.replay.SparkTraitsReplayTooltips;
import dev.caecorthus.sparkdlc.SparkTraitsParticles;
import dev.caecorthus.sparkdlc.SparkTraitsSounds;
import dev.caecorthus.sparkdlc.trait.lifecycle.TraitGameHooks;
import dev.caecorthus.sparkdlc.net.SparkTraitsPackets;
import dev.caecorthus.sparkdlc.net.version.SparkTraitsVersionHandshake;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkTraits implements ModInitializer {
    public static final String MOD_ID = "sparkdlc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        SparkTraitsVersionHandshake.registerServer();
        SparkTraitsPackets.register();
        SparkTraitsDataComponentTypes.init();
        SparkTraitsParticles.register();
        SparkTraitsSounds.initialize();
        SparkTraitsBuiltInTraits.register();
        LastStandService.register();
        LastStandFinalMomentService.register();
        JesterTraitService.register();
        TraitGameHooks.register();
        SparkTraitsReplayTooltips.register();
        SparkTraitsReplayEvents.registerFormatters();
    }
}
