package dev.caecorthus.sparkdlc.client.mixin.trait;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.caecorthus.sparkdlc.trait.component.TraitPlayerComponent;
import dev.caecorthus.sparkdlc.trait.global.SensitiveTrait;
import dev.caecorthus.sparkdlc.net.version.SparkTraitsServerConnection;
import dev.doctor4t.wathe.client.gui.TimeRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Shows the round timer to 敏感 holders whose role cannot normally see it.
 * 让身份本身看不到计时器的“敏感”持有者也能看到本局剩余时间。
 */
@Mixin(value = TimeRenderer.class, remap = false)
public abstract class SensitiveTimeRendererMixin {
    @ModifyExpressionValue(
            method = "renderHud",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/api/Role;canSeeTime()Z")
    )
    private static boolean sparktraits$sensitiveSeesRoundTimer(boolean roleCanSeeTime) {
        if (roleCanSeeTime || !SparkTraitsServerConnection.isConfirmedServer()) {
            return roleCanSeeTime;
        }
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player != null && TraitPlayerComponent.KEY.get(player).hasActiveTrait(SensitiveTrait.ID);
    }
}
