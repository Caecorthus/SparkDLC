package dev.caecorthus.sparkdlc.trait.civilian.depression;

import dev.caecorthus.sparkdlc.trait.api.Trait;
import dev.caecorthus.sparkdlc.trait.api.TraitAssignmentReason;
import dev.caecorthus.sparkdlc.trait.api.TraitAudience;
import dev.caecorthus.sparkdlc.trait.api.TraitRemovalReason;
import dev.caecorthus.sparkdlc.trait.api.TraitSelectionContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Set;
import dev.caecorthus.sparkdlc.trait.civilian.CivilianTraitService;
import dev.caecorthus.sparkdlc.trait.civilian.CivilianTraits;
import dev.caecorthus.sparkdlc.trait.civilian.impostor.ImpostorTrait;
import dev.caecorthus.sparkdlc.trait.civilian.laststand.LastStandTrait;

/**
 * Civilian-side Depression trait with runtime stamina hooks.
 * 平民侧抑郁天赋，并在天赋生命周期中维护体力修正。
 */
public final class DepressionTrait implements Trait {
    @Override
    public Identifier id() {
        return CivilianTraits.DEPRESSION;
    }

    @Override
    public int color() {
        return DepressionTraitService.COLOR;
    }

    @Override
    public TraitAudience audience() {
        return TraitAudience.INNOCENT_ONLY;
    }

    @Override
    public Set<Identifier> incompatibleTraits() {
        return Set.of(ImpostorTrait.ID, LastStandTrait.ID, CivilianTraits.INTROVERTED, CivilianTraits.EXTROVERTED);
    }

    @Override
    public boolean canApply(TraitSelectionContext context) {
        return Trait.super.canApply(context)
                && CivilianTraitService.canSelectCivilianTrait(context.role(), context.selectedTraitIds())
                && DepressionRules.canSelectDepression(
                        context.role(),
                        context.selectedTraitIds(),
                        context.startingPlayerCount(),
                        context.enforceStartingPlayerCount()
                );
    }

    @Override
    public void onAssigned(ServerPlayerEntity player, TraitAssignmentReason reason) {
        DepressionStaminaPenalty.applyDepressionStamina(player);
    }

    @Override
    public void onRemoved(ServerPlayerEntity player, TraitRemovalReason reason) {
        DepressionStaminaPenalty.removeDepressionStamina(player);
    }
}
