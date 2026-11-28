package dev.caecorthus.sparkdlc.mixin.entity;

import dev.caecorthus.sparkdlc.entity.RealityAnchorHits;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.doctor4t.wathe.util.GunShootPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wathe revolver family shots (revolver, derringer and every police gun of this mod that rides this receiver) destroy
 * a placed reality anchor on their line (owner 2026-11-27). Hooked at the same accepted-shot anchor as the BEAR smoke
 * carve, i.e. after Wathe's spectator, gun-tag, cooldown and spent-derringer checks, and never cancels: the click,
 * the record, the kill, the punishment and the cooldown all resolve in the receiver exactly as without an anchor. The
 * line runs from the shooter's eyes along the server rotation for the revolver's client pick range (the Niko trait
 * widens it together with the receiver's target cap) and ends at the first block or at the entity the client
 * targeted, so an anchor behind the player who was shot stays. Wathe member: class-level {@code remap = false}.
 * Wathe 左轮系（左轮、德林加，以及经由该接收器的本模组所有警用枪）的射击会摧毁弹道上的现实稳定锚（所有者 2026-11-27）。
 * 挂在与 BEAR 烟雾通道同一个“已接受射击”锚点，即位于 Wathe 的旁观、枪械标签、冷却与德林加已用检查之后，且从不取消：点击声、
 * 记录、击杀、惩罚与冷却都在接收器中照常结算，与没有锚时完全相同。弹道从射手眼睛沿服务端朝向延伸左轮的客户端选靶距离
 * （Niko 词条会连同接收器的目标上限一起放宽它），在第一个方块或客户端选中的实体处结束，因此被击中玩家身后的锚不受影响。
 * Wathe 成员：类级 {@code remap = false}。
 */
@Mixin(value = GunShootPayload.Receiver.class, remap = false)
public abstract class GunShootPayloadRealityAnchorMixin {
    @Inject(method = "receive(Ldev/doctor4t/wathe/util/GunShootPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
            at = @At(value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/record/GameRecordManager;recordItemUse(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/Identifier;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/nbt/NbtCompound;)V"))
    private void sparkdlc$destroyRealityAnchor(GunShootPayload payload, ServerPlayNetworking.Context context,
                                               CallbackInfo ci) {
        ServerPlayerEntity shooter = context.player();
        RealityAnchorHits.gunShot(shooter, shooter.getServerWorld().getEntityById(payload.target()),
                RealityAnchorHits.WATHE_REVOLVER_RANGE,
                SparkTraitsApi.getServerGunTargetRange(shooter, shooter.getMainHandStack().getItem(),
                        RealityAnchorHits.GUN_TARGET_CAP));
    }
}
