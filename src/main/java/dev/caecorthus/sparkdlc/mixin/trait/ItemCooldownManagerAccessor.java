package dev.caecorthus.sparkdlc.mixin.trait;

import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ItemCooldownManager.class)
public interface ItemCooldownManagerAccessor {
    @Accessor("entries")
    Map<Item, Object> sparkdlc$getEntriesForTraits();

    @Accessor("tick")
    int sparkdlc$getTickForTraits();
}
