package dev.caecorthus.sparkdlc.mixin.sabotage;

import dev.doctor4t.wathe.cca.WorldBlackoutComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Exact Adapter for Wathe's blackout list and world timer, used to stretch a sabotage outage and to bring the lamps
 * back one by one after a repair.
 * Wathe 停电列表与世界计时的精确 Adapter，用于拉长破坏造成的熄灯，以及维修后让灯逐盏亮回。
 */
@Mixin(value = WorldBlackoutComponent.class, remap = false)
public interface WorldBlackoutComponentAccessor {
    @Accessor(value = "blackouts", remap = false)
    List<WorldBlackoutComponent.BlackoutDetails> sparkdlc$getBlackouts();

    @Accessor(value = "ticks", remap = false)
    int sparkdlc$getTicks();

    @Accessor(value = "ticks", remap = false)
    void sparkdlc$setTicks(int ticks);
}
