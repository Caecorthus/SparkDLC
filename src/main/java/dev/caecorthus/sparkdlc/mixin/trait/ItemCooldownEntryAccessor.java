package dev.caecorthus.sparkdlc.mixin.trait;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.entity.player.ItemCooldownManager$Entry")
public interface ItemCooldownEntryAccessor {
    @Accessor("startTick")
    int sparkdlc$getStartTickForTraits();

    @Accessor("endTick")
    int sparkdlc$getEndTickForTraits();
}
