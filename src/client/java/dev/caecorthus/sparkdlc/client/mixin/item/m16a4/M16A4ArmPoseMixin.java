package dev.caecorthus.sparkdlc.client.mixin.item.m16a4;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.caecorthus.sparkdlc.SparkDlcItems;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Third-person aiming pose for the M16A4: while the rifle is in the main hand, vanilla's plain {@code ITEM} answer
 * becomes {@code BOW_AND_ARROW}, so both arms hold the levelled rifle exactly like the sniper rifle's charged bow aim.
 * Any inner pose (Wathe's bat, a Jester moment) is kept, and the shown stack is read after Wathe's display substitution
 * so a viewer shown a fake item never sees the rifle pose. Same wrapper shape as the M32 launcher's.
 * M16A4 的第三人称瞄准姿势：步枪在主手时，把原版普通的 {@code ITEM} 结果改为 {@code BOW_AND_ARROW}，双臂就像狙击步枪蓄力
 * 拉弓那样平举步枪。内层已有的姿势（Wathe 球棒、小丑时刻）保持不变；显示物品在 Wathe 替换之后读取，因此看到假物品的观察者
 * 不会看到持枪姿势。包装形式与 M32 炮筒相同。
 */
@Mixin(value = PlayerEntityRenderer.class, priority = 1600)
public abstract class M16A4ArmPoseMixin {
    @ModifyVariable(method = "getArmPose", at = @At("STORE"))
    private static ItemStack sparkdlc$noteShownM16a4(ItemStack shown, @Share("m16a4Shown") LocalBooleanRef m16a4Shown) {
        m16a4Shown.set(shown.isOf(SparkDlcItems.m16a4()));
        return shown;
    }

    @WrapMethod(method = "getArmPose(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;")
    private static BipedEntityModel.ArmPose sparkdlc$aimM16a4(AbstractClientPlayerEntity player, Hand hand,
                                                              Operation<BipedEntityModel.ArmPose> original,
                                                              @Share("m16a4Shown") LocalBooleanRef m16a4Shown) {
        BipedEntityModel.ArmPose pose = original.call(player, hand);
        return m16a4Shown.get() && pose == BipedEntityModel.ArmPose.ITEM
                ? BipedEntityModel.ArmPose.BOW_AND_ARROW
                : pose;
    }
}
