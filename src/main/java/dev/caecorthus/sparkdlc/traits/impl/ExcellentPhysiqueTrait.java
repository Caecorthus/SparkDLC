package dev.caecorthus.sparkdlc.traits.impl;

import dev.caecorthus.sparkdlc.traits.SparkTraits;
import dev.caecorthus.sparkdlc.traits.api.Trait;
import dev.caecorthus.sparkdlc.traits.api.TraitAssignmentReason;
import dev.caecorthus.sparkdlc.traits.api.TraitRemovalReason;
import dev.caecorthus.sparkdlc.traits.api.TraitSelectionContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Global trait that doubles finite stamina capacity and natural recovery.
 * 全局天赋：翻倍有限体力条的上限与自然恢复速度。
 */
public final class ExcellentPhysiqueTrait implements Trait {
    public static final Identifier ID = SparkTraits.id("excellent_physique");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public int color() {
        return GlobalTraitService.EXCELLENT_PHYSIQUE_COLOR;
    }

    @Override
    public boolean canApply(TraitSelectionContext context) {
        return GlobalTraitService.canSelectExcellentPhysique(context.role());
    }

    @Override
    public void onAssigned(ServerPlayerEntity player, TraitAssignmentReason reason) {
        GlobalTraitService.applyExcellentPhysiqueStamina(player);
    }

    @Override
    public void onRemoved(ServerPlayerEntity player, TraitRemovalReason reason) {
        GlobalTraitService.removeExcellentPhysiqueStamina(player);
    }
}
