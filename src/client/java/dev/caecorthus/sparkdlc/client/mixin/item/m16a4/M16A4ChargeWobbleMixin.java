package dev.caecorthus.sparkdlc.client.mixin.item.m16a4;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.caecorthus.sparkdlc.SparkDlcItems;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Vanilla's bow sway keeps sampling while a bow stays in use, which would make the M16A4 tremble during a burst. The
 * rifle is drawn in a fixed aim pose, so the sway is dropped for its main-hand stack at every use tick.
 * 原版弓在持续使用时会不断采样摆动，会让 M16A4 在点射期间抖动。本枪固定为瞄准姿势，因此对它的主手物品取消这段摆动。
 */
@Mixin(HeldItemRenderer.class)
public abstract class M16A4ChargeWobbleMixin {

    @WrapOperation(
            method = "renderFirstPersonItem",
            slice = @Slice(from = @At(value = "CONSTANT", args = "floatValue=20.0F")),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
                    ordinal = 0
            )
    )
    private void sparkdlc$holdM16a4Steady(MatrixStack matrices, float x, float y, float z,
                                          Operation<Void> original, AbstractClientPlayerEntity player,
                                          float tickDelta, float pitch, Hand hand, float swingProgress,
                                          ItemStack item) {
        if (item.isOf(SparkDlcItems.m16a4()) && hand == Hand.MAIN_HAND) {
            return;
        }
        original.call(matrices, x, y, z);
    }
}
