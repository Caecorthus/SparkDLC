package dev.caecorthus.sparkdlc.mixin.trait;

import dev.caecorthus.sparkdlc.trait.civilian.laststand.LastStandService;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disables entity-to-entity collision while Last Stand is waiting to revive a player.
 * 当背水一战玩家处于等待复活状态时，禁用实体之间的碰撞体积。
 * Not a registered collision exemption ({@code mixin.faction.ExemptionEntityCollisionMixin} wraps Wathe's wall at
 * priority 1100); this is a default-priority HEAD veto that also covers {@code isCollidable}.
 * 不是已注册的碰撞豁免（{@code mixin.faction.ExemptionEntityCollisionMixin} 以优先级 1100 包裹 Wathe 实体墙）；
 * 这里是默认优先级的 HEAD 否决，并且也覆盖 {@code isCollidable}。
 */
@Mixin(Entity.class)
public abstract class LastStandEntityCollisionMixin {
    @Inject(method = "isCollidable", at = @At("HEAD"), cancellable = true)
    private void sparkdlc$disableLastStandPendingCollidable(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (!SparkDlcServerConnection.isUnconfirmedClientEntity(entity)
                && LastStandService.shouldDisablePendingCollision(entity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "collidesWith", at = @At("HEAD"), cancellable = true)
    private void sparkdlc$disableLastStandPendingCollision(Entity other, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (!SparkDlcServerConnection.isUnconfirmedClientEntity(entity)
                && LastStandService.shouldDisablePendingCollision(entity, other)) {
            cir.setReturnValue(false);
        }
    }
}
