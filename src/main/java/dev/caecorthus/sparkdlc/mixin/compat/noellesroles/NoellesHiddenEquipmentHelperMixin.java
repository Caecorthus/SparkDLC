package dev.caecorthus.sparkdlc.mixin.compat.noellesroles;

import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesHiddenEquipmentRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Extends NoellesRoles' hidden-equipment decision. {@code @Pseudo}, the string target and {@code require = 0} kept
 * NoellesRoles a soft dependency; it is a required one now, so the tolerant path is unreachable since the fusion.
 * 扩展 NoellesRoles 的隐藏装备判定。{@code @Pseudo}、字符串目标与 {@code require = 0} 原本用于不硬依赖 NoellesRoles；
 * 它现在是必需依赖，这条宽容路径融合后不可达。
 */
@Pseudo
@Mixin(targets = "org.agmas.noellesroles.util.HiddenEquipmentHelper", remap = false)
public abstract class NoellesHiddenEquipmentHelperMixin {
    @Inject(method = "shouldHideItem", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private static void sparkdlc$includeRegisteredItems(
            ItemStack stack,
            PlayerEntity holder,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValueZ() && NoellesHiddenEquipmentRegistry.shouldHide(stack)) {
            cir.setReturnValue(true);
        }
    }
}
