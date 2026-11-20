package dev.caecorthus.sparkdlc.mixin.wathe;

import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Keeps Wathe corpses safe for LambDynamicLights and other equipment scanners: a body answers an empty armour list
 * instead of {@code null}. One handler (two former mods each shipped this same one; the second always found the list
 * already non-null and did nothing).
 * 让 wathe 尸体兼容 LambDynamicLights 等动态光源与装备扫描逻辑：尸体返回空的装备列表而不是 {@code null}。只有一个处理器
 * （两个旧 mod 曾各带一份相同的；第二份总是发现列表已非 null，什么都不做）。
 */
@Mixin(PlayerBodyEntity.class)
public abstract class PlayerBodyEntityEquipmentMixin {
    @Inject(method = "getArmorItems", at = @At("RETURN"), cancellable = true)
    private void sparkdlc$emptyArmorItemsForBody(CallbackInfoReturnable<Iterable<ItemStack>> cir) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue(List.of());
        }
    }
}
