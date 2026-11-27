package dev.caecorthus.sparkdlc.mixin.entity;

import dev.caecorthus.sparkdlc.entity.RealityAnchorHits;
import dev.doctor4t.wathe.entity.GrenadeEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A Wathe grenade blast destroys every reality anchor inside its own hit box (owner 2026-11-27). Wathe's blast box is
 * the grenade's bounding box expanded by 3 blocks, the same region in which it kills players. Server only; the
 * grenade's own player blast is untouched.
 * Wathe 手雷爆炸会摧毁命中盒内的所有现实稳定锚（所有者 2026-11-27）。Wathe 的爆炸盒是手雷包围盒向外扩 3 格，与它击杀玩家的
 * 范围相同。仅服务端；手雷本身的玩家爆炸不受影响。
 */
@Mixin(GrenadeEntity.class)
public abstract class GrenadeEntityRealityAnchorMixin {
    /** Wathe's own blast expansion, read from {@code GrenadeEntity.lambda$onCollision$0}. / Wathe 自身的爆炸外扩量。 */
    private static final double BLAST_EXPANSION = 3.0D;

    @Inject(method = "onCollision", at = @At("TAIL"))
    private void sparkdlc$destroyAnchors(HitResult hitResult, CallbackInfo ci) {
        Entity grenade = (Entity) (Object) this;
        if (grenade.getWorld() instanceof ServerWorld world) {
            RealityAnchorHits.destroyInBox(world, grenade.getBoundingBox().expand(BLAST_EXPANSION));
        }
    }
}
