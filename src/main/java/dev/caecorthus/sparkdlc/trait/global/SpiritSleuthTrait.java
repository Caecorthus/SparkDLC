package dev.caecorthus.sparkdlc.trait.global;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.trait.api.Trait;
import net.minecraft.util.Identifier;

/**
 * Global trait that lets the owner see players in spectator mode.
 * 全局天赋：让拥有者看见处于旁观模式的玩家。
 */
public final class SpiritSleuthTrait implements Trait {
    public static final Identifier ID = SparkDLC.id("spirit_sleuth");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public double rollWeight() {
        return 0.0D;
    }

    @Override
    public int color() {
        return GlobalTraitService.SPIRIT_SLEUTH_COLOR;
    }
}
