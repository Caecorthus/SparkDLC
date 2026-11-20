package dev.caecorthus.sparkdlc.trait.global;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.trait.api.Trait;
import dev.caecorthus.sparkdlc.trait.api.TraitAssignmentReason;
import dev.caecorthus.sparkdlc.trait.api.TraitRemovalReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Set;
import dev.caecorthus.sparkdlc.trait.global.pig.PigTrait;
import dev.caecorthus.sparkdlc.trait.global.pig.PigTraitService;

/**
 * Global trait that scales the whole player body down to seventy-five percent; clients draw it big-headed.
 * 全局天赋：将玩家整体体型缩小到百分之七十五，客户端按头大身子小绘制。
 */
public final class ChildishTrait implements Trait {
    public static final Identifier ID = SparkDLC.id("childish");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public int color() {
        return GlobalTraitService.CHILDISH_COLOR;
    }

    @Override
    public int weight() {
        return PigTraitService.WEIGHT;
    }

    @Override
    public double rollWeight() {
        return PigTraitService.ROLL_WEIGHT;
    }

    @Override
    public Set<Identifier> incompatibleTraits() {
        return Set.of(PigTrait.ID);
    }

    @Override
    public void onAssigned(ServerPlayerEntity player, TraitAssignmentReason reason) {
        GlobalTraitService.applyChildishScale(player);
    }

    @Override
    public void onRemoved(ServerPlayerEntity player, TraitRemovalReason reason) {
        GlobalTraitService.removeChildishScale(player);
    }
}
