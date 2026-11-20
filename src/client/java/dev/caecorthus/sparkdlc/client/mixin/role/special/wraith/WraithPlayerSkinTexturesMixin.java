package dev.caecorthus.sparkdlc.client.mixin.role.special.wraith;

import dev.caecorthus.sparkdlc.client.render.WraithSteveProjection;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Replaces the complete skin descriptor for Wraith projection. / 替换冤魂投影视角读取的完整皮肤描述。 */
@Mixin(value = AbstractClientPlayerEntity.class, priority = 2000)
public abstract class WraithPlayerSkinTexturesMixin {
    @Inject(method = "getSkinTextures", at = @At("HEAD"), cancellable = true)
    private void sparkdlc$projectSteveSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        if (WraithSteveProjection.shouldAnonymizePlayer(player)) {
            cir.setReturnValue(WraithSteveProjection.steveSkinTextures());
        }
    }
}
