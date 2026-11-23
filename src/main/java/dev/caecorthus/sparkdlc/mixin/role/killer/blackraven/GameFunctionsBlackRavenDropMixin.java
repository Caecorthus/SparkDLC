package dev.caecorthus.sparkdlc.mixin.role.killer.blackraven;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Excludes the Black Raven loadout (Feather Blade, ledger, Raven Mask) before Wathe's death-drop loop; the
 * death cleanup removes them afterwards. A disguise's live items still follow Wathe's normal drop rules.
 * 在 Wathe 死亡掉落循环之前排除黑羽鸦装备（羽刃、账本、鸦羽假面），随后由死亡清理移除；伪装身份的当前物品仍按 Wathe 常规规则掉落。
 */
@Mixin(GameFunctions.class)
public abstract class GameFunctionsBlackRavenDropMixin {
    @Inject(method = "shouldDropOnDeath", at = @At("HEAD"), cancellable = true)
    private static void sparkdlc$excludeBlackRavenLoadout(
            ItemStack stack,
            PlayerEntity victim,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (stack.isOf(SparkDlcItems.featherBlade()) || stack.isOf(SparkDlcItems.blackRavenLedger())
                || stack.isOf(SparkDlcItems.blackRavenMask())) {
            cir.setReturnValue(false);
        }
    }
}
