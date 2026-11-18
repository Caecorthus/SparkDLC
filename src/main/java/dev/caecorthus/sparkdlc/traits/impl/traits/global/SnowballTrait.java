package dev.caecorthus.sparkdlc.traits.impl.traits.global;

import dev.caecorthus.sparkdlc.traits.SparkTraits;
import dev.caecorthus.sparkdlc.traits.api.Trait;
import dev.caecorthus.sparkdlc.traits.api.TraitSelectionContext;
import net.minecraft.util.Identifier;

/**
 * Global trait that pays a bonus whenever income reaches a new multiple of 100 money.
 * 全局天赋：收入每使金钱达到新的 100 整数倍时发放奖励。
 */
public final class SnowballTrait implements Trait {
    public static final Identifier ID = SparkTraits.id("snowball");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public int color() {
        return GlobalTraitService.SNOWBALL_COLOR;
    }

    @Override
    public boolean canApply(TraitSelectionContext context) {
        return !GlobalTraitService.blocksMoneyTraits(context.role())
                && GlobalTraitService.usesMoneySystem(context.player(), context.gameComponent(), context.role());
    }
}
