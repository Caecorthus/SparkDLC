package dev.caecorthus.sparkdlc.mixin.faction;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.caecorthus.sparkdlc.faction.collision.EntityCollisionExemptions;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Applies the registered {@link EntityCollisionExemptions} to {@code collidesWith}, outside Wathe's player wall. Kept
 * apart from {@code mixin.trait.LastStandEntityCollisionMixin}: that one is a default-priority HEAD veto inside the
 * wall, with its own rule, so the two cannot share a class priority.
 * 在 Wathe 玩家实体墙外层对 {@code collidesWith} 应用已注册的 {@link EntityCollisionExemptions}。与
 * {@code mixin.trait.LastStandEntityCollisionMixin} 分开：后者是默认优先级、位于实体墙内层的 HEAD 否决，规则也不同，
 * 两者无法共用一个类优先级。
 */
@Mixin(value = Entity.class, priority = 1100)
public abstract class ExemptionEntityCollisionMixin {
    @WrapMethod(method = "collidesWith(Lnet/minecraft/entity/Entity;)Z")
    private boolean sparkdlc$cancelExemptCollision(Entity other, Operation<Boolean> original) {
        // Wraps Wathe's priority-1000 player wall. The inner chain is side-effect free and false for almost every
        // pair, so it runs first and the exemption predicates only veto pairs that would collide.
        // 以高于 Wathe 默认 1000 的优先级包裹 collidesWith（含玩家硬碰撞）。内层链无副作用且对绝大多数实体对返回 false，
        // 因此先求内层结果，仅对原本会碰撞的实体对用豁免谓词否决。
        // Still a local instant check: no extra DataTracker fields on Entity/LivingEntity/PlayerEntity.
        // 这里仍然只做本地即时判定，不注册 Entity/LivingEntity/PlayerEntity 的 DataTracker 字段，避免协议编号错位。
        boolean collides = original.call(other);
        return collides && !EntityCollisionExemptions.shouldCancelCollision((Entity) (Object) this, other);
    }
}
