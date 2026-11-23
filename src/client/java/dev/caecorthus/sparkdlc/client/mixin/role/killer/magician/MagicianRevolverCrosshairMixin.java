package dev.caecorthus.sparkdlc.client.mixin.role.killer.magician;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.caecorthus.sparkdlc.client.role.killer.magician.MagicianPuppetAim;
import dev.doctor4t.wathe.client.gui.CrosshairRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Keeps Wathe's revolver and derringer crosshair target pip consistent with the selectors: its private
 * {@code getVisibleGunTarget(player, range)} (30 for the revolver, 7 for the derringer, only asked off cooldown) now
 * also lights up for a Magician puppet strictly nearer than its pick, with the same geometry as the selectors.
 * {@code @WrapMethod} encloses the Marksman trait's HEAD range replacement.
 * 使 Wathe 左轮与德林加的准星目标提示与选靶一致：其私有的 {@code getVisibleGunTarget(player, range)}（左轮 30、德林加 7，
 * 仅在非冷却时调用）现在对严格更近的魔术师皮套同样亮起，几何与选靶相同。{@code @WrapMethod} 包住神射手词条的
 * HEAD 射程替换。
 */
@Mixin(value = CrosshairRenderer.class, remap = false)
public abstract class MagicianRevolverCrosshairMixin {
    @WrapMethod(method = "getVisibleGunTarget")
    private static HitResult sparkdlc$preferNearerVisiblePuppet(PlayerEntity player, double range,
                                                                  Operation<HitResult> original) {
        return MagicianPuppetAim.preferNearerPuppet(player, original.call(player, range), range, true);
    }
}
