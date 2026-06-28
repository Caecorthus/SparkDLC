package dev.caecorthus.sparkdlc.traits;

import dev.caecorthus.sparkdlc.traits.component.SparkTraitsDataComponentTypes;
import dev.caecorthus.sparkdlc.traits.entity.SparkTraitsEntities;
import dev.caecorthus.sparkdlc.traits.impl.LastStandFinalMomentService;
import dev.caecorthus.sparkdlc.traits.impl.SparkTraitsCommands;
import dev.caecorthus.sparkdlc.traits.impl.LastStandService;
import dev.caecorthus.sparkdlc.traits.impl.SparkTraitsBuiltInTraits;
import dev.caecorthus.sparkdlc.traits.impl.SparkTraitsParticles;
import dev.caecorthus.sparkdlc.traits.impl.TraitGameHooks;
import dev.caecorthus.sparkdlc.traits.item.SparkTraitsItems;
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
        SparkTraitsDataComponentTypes.init();
        SparkTraitsItems.init();
        SparkTraitsEntities.init();
        SparkTraitsParticles.register();
        SparkTraitsBuiltInTraits.register();
        LastStandService.register();
        LastStandFinalMomentService.register();
        TraitGameHooks.register();
        SparkTraitsCommands.register();
    }
}
