package dev.caecorthus.sparkdlc.client.mixin.item.m16a4;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.caecorthus.sparkdlc.SparkDlcItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * No equip animation on a trigger pull. {@code MinecraftClient#doItemUse} always calls
 * {@code HeldItemRenderer#resetEquipProgress} after an accepted use, which raises the item into the hand again and reads
 * as re-picking the rifle up on every right click. Wathe skips that call for its guns and the Savior pistol copies it;
 * this does the same for the M16A4.
 * 扣扳机不播装备动画。{@code MinecraftClient#doItemUse} 在物品使用成功后总会调用 {@code HeldItemRenderer#resetEquipProgress}，
 * 把物品重新抬回手中，看起来像每次右键都把步枪重新拿起来一次。Wathe 对自己的枪械跳过该调用，救世主手枪也复制了它；这里为
 * M16A4 照做。
 */
@Mixin(MinecraftClient.class)
public abstract class M16A4EquipMixin {
    @WrapWithCondition(
            method = "doItemUse()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;"
                            + "resetEquipProgress(Lnet/minecraft/util/Hand;)V"
            )
    )
    private boolean sparkdlc$keepM16a4EquipProgress(HeldItemRenderer renderer, Hand hand) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player == null || !player.getStackInHand(hand).isOf(SparkDlcItems.m16a4());
    }
}
