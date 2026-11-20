package dev.caecorthus.sparkdlc.trait.registry;

import dev.caecorthus.sparkdlc.trait.api.TraitRegistry;
import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.trait.killer.conscience.ConscienceTrait;
import dev.caecorthus.sparkdlc.trait.global.CautiousTrait;
import dev.caecorthus.sparkdlc.trait.global.ChildishTrait;
import dev.caecorthus.sparkdlc.trait.global.EconomyTrait;
import dev.caecorthus.sparkdlc.trait.global.ExcellentPhysiqueTrait;
import dev.caecorthus.sparkdlc.trait.global.FastHandsTrait;
import dev.caecorthus.sparkdlc.trait.global.SensitiveTrait;
import dev.caecorthus.sparkdlc.trait.global.SnowballTrait;
import dev.caecorthus.sparkdlc.trait.global.StandsFirmTrait;
import dev.caecorthus.sparkdlc.trait.global.PreparefullTrait;
import dev.caecorthus.sparkdlc.trait.global.SteadyTrait;
import dev.caecorthus.sparkdlc.trait.global.SpiritSleuthTrait;
import dev.caecorthus.sparkdlc.trait.global.TaskMasterTrait;
import dev.caecorthus.sparkdlc.trait.global.WellSuppliedTrait;
import dev.caecorthus.sparkdlc.trait.civilian.CivilianTraits;
import dev.caecorthus.sparkdlc.trait.civilian.impostor.ImpostorTrait;
import dev.caecorthus.sparkdlc.trait.civilian.jester.JesterTrait;
import dev.caecorthus.sparkdlc.trait.killer.KillerTraits;
import dev.caecorthus.sparkdlc.trait.civilian.laststand.LastStandTrait;
import dev.caecorthus.sparkdlc.trait.global.pig.PigTrait;
import dev.caecorthus.sparkdlc.trait.civilian.police.PoliceTraits;

/**
 * Registers bundled SparkTraits trait definitions.
 * 注册 SparkTraits 自带的天赋定义。
 */
public final class BuiltInTraits {
    private BuiltInTraits() {
    }

    public static void register() {
        TraitRegistry.register(new LastStandTrait());
        TraitRegistry.register(new JesterTrait());
        TraitRegistry.register(new ConscienceTrait());
        TraitRegistry.register(new ImpostorTrait());
        TraitRegistry.register(new CautiousTrait());
        TraitRegistry.register(new TaskMasterTrait());
        TraitRegistry.register(new FastHandsTrait());
        TraitRegistry.register(new ChildishTrait());
        TraitRegistry.register(new PigTrait());
        TraitRegistry.register(new SteadyTrait());
        TraitRegistry.register(new ExcellentPhysiqueTrait());
        TraitRegistry.register(new SpiritSleuthTrait());
        TraitRegistry.register(new SnowballTrait());
        TraitRegistry.register(new WellSuppliedTrait());
        TraitRegistry.register(new EconomyTrait());
        TraitRegistry.register(new SensitiveTrait());
        TraitRegistry.register(new StandsFirmTrait());
        TraitRegistry.register(new PreparefullTrait());
        CivilianTraits.register();
        PoliceTraits.register();
        KillerTraits.register();
    }
}
