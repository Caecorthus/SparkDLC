package dev.caecorthus.sparkdlc.client.mixin.health;

import dev.caecorthus.sparkdlc.client.health.BodyHitboxDebugRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Body-part hitbox debug view: after vanilla has drawn an entity's hitbox (F3+B), {@link BodyHitboxDebugRenderer}
 * adds the seven part boxes of a player into the same lines buffer. The hook is the TAIL of the private static
 * {@code EntityRenderDispatcher#renderHitbox} on purpose: its only caller is {@code render}, behind
 * {@code renderHitboxes && !entity.isInvisible() && !hasReducedDebugInfo()}, so every vanilla gate is inherited, and
 * so is {@code EntityRenderProtocolDispatcherMixin}'s suppression of the hitbox during an {@code EntityRedraw}. At
 * that point the matrix origin is the entity's interpolated position (the renderer's position offset is already
 * undone, and the ender dragon branch pops what it pushes). This mixin only forwards; display only.
 * 部位命中箱调试视图：原版绘制完实体碰撞箱（F3+B）之后，由 {@link BodyHitboxDebugRenderer} 向同一线段缓冲补画玩家的七个部位
 * 箱体。有意挂在私有静态方法 {@code EntityRenderDispatcher#renderHitbox} 的 TAIL：其唯一调用方是 {@code render}，位于
 * {@code renderHitboxes && !entity.isInvisible() && !hasReducedDebugInfo()} 之后，因此继承原版的全部门槛，也继承
 * {@code EntityRenderProtocolDispatcherMixin} 在 {@code EntityRedraw} 期间对碰撞箱的屏蔽。此时矩阵原点为实体的插值位置
 * （渲染器的位置偏移已被撤销，末影龙分支的压栈也已弹出）。本 mixin 只做转发；仅用于展示。
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class BodyHitboxDebugMixin {
    // Static on purpose: the target is static, and Mixin rejects an instance handler on a static target.
    // 刻意写成静态：目标是静态方法，Mixin 会拒绝静态目标上的实例回调。
    @Inject(
            method = "renderHitbox(Lnet/minecraft/client/util/math/MatrixStack;"
                    + "Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/entity/Entity;FFFF)V",
            at = @At("TAIL"),
            require = 1,
            allow = 1
    )
    private static void sparkdlc$drawBodyPartBoxes(MatrixStack matrices, VertexConsumer vertices, Entity entity,
                                                   float tickDelta, float red, float green, float blue,
                                                   CallbackInfo ci) {
        BodyHitboxDebugRenderer.render(matrices, vertices, entity, tickDelta);
    }
}
