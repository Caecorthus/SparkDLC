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
 * Third-person aiming pose for the M16A4, for every rendered player: while the rifle is the shown main-hand stack,
 * vanilla's plain {@code ITEM} answer becomes {@code BOW_AND_ARROW}, so both arms hold the levelled rifle exactly like
 * the sniper rifle's charged bow aim (an off-hand rifle cannot fire and keeps the plain pose). The M32A1 launcher's
 * {@code M32ArmPoseMixin} with another pose: the use action is NONE, so vanilla ends at its {@code ITEM} answer; any
 * inner pose (Wathe's bat, NoellesRoles' Jester Moment) is kept; the {@code @WrapMethod} at priority 1600 nests outside
 * Wathe's and NoellesRoles' TAIL injects and inside the Blind gate (2000); and the shown stack is read at the STORE of
 * vanilla's only {@code ItemStack} local, after Wathe's display substitution, so a viewer shown a fake item never sees
 * the rifle pose. It answers only for the M16A4 and chains with the other weapons' wrappers.
 * M16A4 的第三人称瞄准姿势，对所有被渲染的玩家生效：显示的主手物品是步枪时，把原版普通的 {@code ITEM} 结果改为
 * {@code BOW_AND_ARROW}，双臂就像狙击步枪蓄力拉弓那样平举步枪（副手步枪无法开火，保持普通姿势）。即 M32A1 炮筒的
 * {@code M32ArmPoseMixin} 换一个姿势：使用动作为 NONE，原版最终返回 {@code ITEM}；内层已有的姿势（Wathe 球棒、
 * NoellesRoles 小丑时刻）保持不变；优先级 1600 的 {@code @WrapMethod} 嵌套在 Wathe 与 NoellesRoles 的 TAIL 注入之外、
 * 盲人拦截（2000）之内；显示物品在原版唯一 {@code ItemStack} 局部变量的 STORE 处读取，位于 Wathe 显示替换之后，因此看到
 * 假物品的观察者不会看到持枪姿势。它只对 M16A4 作答，并与其他武器的包装串联。
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
        return hand == Hand.MAIN_HAND && m16a4Shown.get() && pose == BipedEntityModel.ArmPose.ITEM
                ? BipedEntityModel.ArmPose.BOW_AND_ARROW
                : pose;
    }
}
