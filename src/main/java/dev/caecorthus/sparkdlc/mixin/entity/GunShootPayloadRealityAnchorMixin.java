package dev.caecorthus.sparkdlc.mixin.entity;

import dev.caecorthus.sparkdlc.entity.RealityAnchorHits;
import dev.doctor4t.wathe.util.GunShootPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wathe revolver family shots destroy a placed reality anchor on their line (owner 2026-11-27). Hooked at the same
 * accepted-shot anchor as the BEAR smoke carve, i.e. after Wathe's spectator, gun-tag, cooldown and derringer checks,
 * and never cancels the shot: the player kill still resolves later in the receiver. The line is the shooter's eyes to
 * the first block within Wathe's target cap.
 * Wathe 左轮系的射击会摧毁弹道上的现实稳定锚（所有者 2026-11-27）。挂在与 BEAR 烟雾通道同一个"已接受射击"锚点，即位于
 * Wathe 的旁观、枪械标签、冷却与德林加检查之后，且从不取消射击：玩家击杀仍在该接收器稍后结算。射线从射手眼睛到目标上限内
 * 的第一个方块。
 */
@Mixin(value = GunShootPayload.Receiver.class, remap = false)
public abstract class GunShootPayloadRealityAnchorMixin {
    /** Wathe's revolver target cap, the same one the smoke carve follows. / Wathe 左轮的目标上限，与烟雾通道一致。 */
    private static final double RANGE = 65.0D;

    @Inject(method = "receive(Ldev/doctor4t/wathe/util/GunShootPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
            at = @At(value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/record/GameRecordManager;recordItemUse(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/Identifier;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/nbt/NbtCompound;)V"))
    private void sparkdlc$destroyRealityAnchor(GunShootPayload payload, ServerPlayNetworking.Context context,
                                               CallbackInfo ci) {
        ServerPlayerEntity shooter = context.player();
        RealityAnchorHits.destroyAlongRay(shooter.getServerWorld(), shooter, shooter.getEyePos(),
                shooter.getEyePos().add(shooter.getRotationVec(1.0F).multiply(RANGE)));
    }
}
