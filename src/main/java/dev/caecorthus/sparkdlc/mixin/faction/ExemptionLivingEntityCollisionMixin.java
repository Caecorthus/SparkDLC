package dev.caecorthus.sparkdlc.mixin.faction;

import dev.caecorthus.sparkdlc.faction.collision.EntityCollisionExemptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels {@code pushAway} when either entity is exempt through {@link EntityCollisionExemptions}. Kept apart from
 * {@code mixin.trait.LastStandLivingEntityCollisionMixin}, which applies the Last Stand pending rule (also to
 * {@code isPushable}, and not on an unconfirmed client).
 * 任一方经 {@link EntityCollisionExemptions} 豁免时取消 {@code pushAway}。与
 * {@code mixin.trait.LastStandLivingEntityCollisionMixin} 分开：后者应用背水一战等待复活规则（也作用于 {@code isPushable}，
 * 且在未确认的客户端上不生效）。
 */
@Mixin(LivingEntity.class)
public abstract class ExemptionLivingEntityCollisionMixin {
    @Inject(
            method = "pushAway(Lnet/minecraft/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sparkdlc$cancelExemptPush(Entity other, CallbackInfo ci) {
        if (EntityCollisionExemptions.shouldCancelPush((Entity) (Object) this, other)) {
            ci.cancel();
        }
    }
}
