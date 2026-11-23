package dev.caecorthus.sparkdlc.client.mixin.trait;

import dev.caecorthus.sparkdlc.trait.civilian.police.VigilanteVeteranTraitService;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.doctor4t.wathe.item.RevolverItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Softens only the local first-shot recoil that starts Niko's revolver burst.
 * 只降低触发 Niko 左轮三连发的本地首发后坐力，不给补发两枪新增视角后坐力。
 */
@Mixin(value = RevolverItem.class, remap = false)
public abstract class NikoRevolverRecoilMixin {
    @ModifyConstant(method = "use", constant = @Constant(floatValue = 4.0f), remap = false)
    private float sparkdlc$reduceNikoRevolverBurstRecoil(float recoil, World world, PlayerEntity user, Hand hand) {
        if (!SparkDlcServerConnection.isConfirmedServer()) {
            return recoil;
        }
        return VigilanteVeteranTraitService.adjustedNikoRevolverRecoil(recoil, user);
    }
}
