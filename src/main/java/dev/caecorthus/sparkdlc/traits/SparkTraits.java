package dev.caecorthus.sparkdlc.traits;

import dev.caecorthus.sparkdlc.traits.component.SparkTraitsDataComponentTypes;
import dev.caecorthus.sparkdlc.traits.impl.traits.civilian.laststand.LastStandFinalMomentService;
import dev.caecorthus.sparkdlc.traits.impl.command.admin.SparkTraitsCommands;
import dev.caecorthus.sparkdlc.traits.impl.traits.civilian.laststand.LastStandService;
import dev.caecorthus.sparkdlc.traits.impl.registry.SparkTraitsBuiltInTraits;
import dev.caecorthus.sparkdlc.traits.impl.resource.SparkTraitsParticles;
import dev.caecorthus.sparkdlc.traits.impl.resource.SparkTraitsSounds;
import dev.caecorthus.sparkdlc.traits.impl.lifecycle.TraitGameHooks;
import dev.caecorthus.sparkdlc.traits.net.version.SparkTraitsPackets;
import dev.caecorthus.sparkdlc.traits.net.version.SparkTraitsVersionHandshake;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkTraits implements ModInitializer {
    public static final String MOD_ID = "sparktraits";
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
        TraitGameHooks.register();
        SparkTraitsCommands.register();
    }
}
