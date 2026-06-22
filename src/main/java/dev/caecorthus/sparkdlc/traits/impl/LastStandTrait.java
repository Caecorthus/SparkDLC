package dev.caecorthus.sparkdlc.traits.impl;

import dev.caecorthus.sparkdlc.traits.SparkTraits;
import dev.caecorthus.sparkdlc.traits.api.Trait;
import dev.caecorthus.sparkdlc.traits.api.TraitAssignmentReason;
import dev.caecorthus.sparkdlc.traits.api.TraitAudience;
import dev.caecorthus.sparkdlc.traits.api.TraitSelectionContext;
import dev.doctor4t.wathe.api.Faction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Hidden civilian trait that can turn one qualifying death into a delayed last stand.
 * 隐藏好人天赋：在满足条件的一次死亡后，延迟触发一次背水一战。
 */
public final class LastStandTrait implements Trait {
    public static final Identifier ID = SparkTraits.id("last_stand");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public int color() {
        return 0xFFFFFF;
    }

    @Override
    public boolean uniquePerGame() {
        return true;
    }

    @Override
    public boolean hiddenFromOwnerAtStart() {
        return true;
    }

    @Override
    public TraitAudience audience() {
        return TraitAudience.INNOCENT_ONLY;
    }

    @Override
    public boolean canApply(TraitSelectionContext context) {
        return Trait.super.canApply(context)
                && context.role() != null
                && context.role().getFaction() == Faction.CIVILIAN
                && context.gameComponent().getAllKillerTeamPlayers().size() >= 2;
    }

    @Override
    public void onAssigned(ServerPlayerEntity player, TraitAssignmentReason reason) {
        LastStandService.recordReturnPoint(player);
    }
}
