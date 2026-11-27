package dev.caecorthus.sparkdlc.client.mixin.item.m16a4;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The M16A4 is always held in the sniper rifle's charged-aim first-person pose: the same quarter-screen shift towards
 * the middle, the same slight drop and forward nudge and the same 12 degree roll the M1903A4 takes only while charging.
 * Keyed on the rendered main-hand stack, so the other hand and every other rendered player keep vanilla's pose.
 * M16A4 常驻狙击步枪蓄力瞄准时的第一人称姿势：同样向屏幕中间平移四分之一屏宽、同样的下沉与前移、同样的 12 度滚转，
 * 而 M1903A4 只在蓄力时使用该姿势。判定依据是正在渲染的主手物品，因此另一只手与其他被渲染的玩家保持原版姿势。
 */
@Mixin(HeldItemRenderer.class)
public abstract class M16A4AimPoseMixin {

    @Inject(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            )
    )
    private void sparkdlc$aimM16a4(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                                   float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                                   VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!item.isOf(SparkDlcItems.m16a4()) || hand != Hand.MAIN_HAND) {
            return;
        }
        boolean rightArmed = player.getMainArm() == Arm.RIGHT;
        // The rifle reports UseAction.NONE, so vanilla never folds the bow's charge-dependent transforms in for it; the
        // fully-charged frame is rebuilt here on every rendered tick (the fixed bow offset, the full pull's 1.2 depth
        // scale and the 45 degree turn that puts the flat sprite in the aim plane), then the sniper rifle's aim shift.
        // 步枪返回 UseAction.NONE，原版从不为它折叠随蓄力变化的弓变换；这里每帧重建满蓄力框架（弓的固定偏移、满拉的 1.2
        // 深度缩放，以及让平面贴图落到瞄准平面内的 45 度转向），再接狙击步枪的瞄准平移。
        float side = rightArmed ? 1.0F : -1.0F;
        matrices.translate(side * -0.2785682F, 0.18344387F, 0.15731531F);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * 35.3F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * -9.785F));
        matrices.translate(0.0F, 0.0F, 0.04F);
        matrices.scale(1.0F, 1.0F, 1.2F);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(side * 45.0F));
        matrices.translate(rightArmed ? -0.1875F : 0.1875F, -0.08F, 0.1F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rightArmed ? 12.0F : -12.0F));
    }
}
