package dev.caecorthus.sparkdlc.trait.global;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.trait.api.Trait;
import net.minecraft.util.Identifier;

/**
 * Global trait that keeps the owner's own movement and consumption quiet.
 * 全局天赋：让拥有者自身的移动与进食饮用保持安静。
 */
public final class CautiousTrait implements Trait {
    public static final Identifier ID = SparkDLC.id("cautious");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public int color() {
        return GlobalTraitService.CAUTIOUS_COLOR;
    }
}
