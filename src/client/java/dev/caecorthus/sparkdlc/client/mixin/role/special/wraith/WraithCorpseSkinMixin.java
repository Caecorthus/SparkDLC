package dev.caecorthus.sparkdlc.client.mixin.role.special.wraith;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.caecorthus.sparkdlc.client.render.WraithSteveProjection;
import dev.doctor4t.wathe.client.render.entity.PlayerBodyEntityRenderer;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Shows every corpse as Steve to the local Wraith. / 向本地冤魂把所有尸体显示为 Steve。 */
@Mixin(value = PlayerBodyEntityRenderer.class, priority = 100)
public abstract class WraithCorpseSkinMixin {
    @ModifyReturnValue(
            method = "getTexture(Ldev/doctor4t/wathe/entity/PlayerBodyEntity;)Lnet/minecraft/util/Identifier;",
            at = @At("RETURN")
    )
    private Identifier sparkdlc$projectCorpseSteveTexture(Identifier original, PlayerBodyEntity body) {
        return WraithSteveProjection.shouldAnonymizeCorpses()
                ? WraithSteveProjection.steveTexture()
                : original;
    }
}
