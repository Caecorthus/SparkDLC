package dev.caecorthus.sparkdlc.client.mixin.role.killer.morphling;

import dev.caecorthus.sparkdlc.client.role.killer.morphling.MorphlingAppearanceClientHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapeFeatureRenderer.class)
public abstract class MorphlingCapeRendererMixin {
    @WrapOperation(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getSkinTextures()Lnet/minecraft/client/util/SkinTextures;"
            )
    )
    private SkinTextures sparkdlc$useMorphlingReagentCapeSkin(
            AbstractClientPlayerEntity player,
            Operation<SkinTextures> original
    ) {
        SkinTextures skinTextures = MorphlingAppearanceClientHelper.resolveActivePlayerSkinTextures(player);
        return skinTextures != null ? skinTextures : original.call(player);
    }
}
