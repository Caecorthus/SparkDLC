package dev.caecorthus.sparkdlc.mixin.trait;

import dev.caecorthus.sparkdlc.trait.civilian.police.VigilanteVeteranTraitService;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerItemCooldownManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Applies Fast Reload only to police-category roles' revolver and Savior pistol cooldowns.
 * 只把快速装填作用到警职类别身份的左轮手枪与救世主手枪冷却上。
 */
@Mixin(value = ItemCooldownManager.class, priority = 925)
public abstract class FastReloadCooldownMixin {
    @ModifyVariable(method = "set", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int sparkdlc$reduceFastReloadRevolverCooldown(int duration, Item item) {
        if ((Object) this instanceof ServerItemCooldownManager serverCooldownManager) {
            ServerPlayerEntity player = ((ServerItemCooldownManagerAccessor) (Object) serverCooldownManager).sparkdlc$getPlayerForTraits();
            return VigilanteVeteranTraitService.fastReloadCooldown(item, duration, player);
        }
        return duration;
    }
}
