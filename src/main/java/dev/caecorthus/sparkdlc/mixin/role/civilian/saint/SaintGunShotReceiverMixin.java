package dev.caecorthus.sparkdlc.mixin.role.civilian.saint;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.caecorthus.sparkdlc.role.civilian.saint.SaintGunShotService;
import dev.doctor4t.wathe.api.event.ShouldPunishGunShooter;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.util.GunShootPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Wathe's gun receiver (revolver and derringer) for a Saint shooter (owner 2026-11-28): her shot cannot kill
 * ({@code GameFunctionsSaintGunShotMixin}), so it never punishes her either. Keyed on the shooter's raw role only:
 * <ul>
 *   <li>{@code ShouldPunishGunShooter} answers cancel without asking the listeners: no revolver backfire, no
 *   innocent-shot punishment (she keeps the gun and does not die), no custom punishment of a listener.</li>
 *   <li>The passenger mood loss after the shot is skipped.</li>
 * </ul>
 * Nothing else changes: the HEAD vetoes of the receiver still decide whether the shot happens at all, and the click,
 * the record, the derringer's spent state, the shot sound, the muzzle flash and the gun's own cooldown stay Wathe's.
 * Chaining {@code @WrapOperation}s only (never {@code @Redirect}), so the Judge and Dominator wrappers on the same
 * calls compose in any order: each of them cancels for its own case and passes every other shot on.
 * Wathe 枪械接收器（左轮与德林加）上针对圣徒射手的钩子（所有者 2026-11-28）：她的射击无法杀人
 * （{@code GameFunctionsSaintGunShotMixin}），因此也从不让她受罚。只按射手的真实职业判定：
 * {@code ShouldPunishGunShooter} 不询问监听器直接返回取消：左轮不会走火，没有误伤惩罚（她保留枪且不会死亡），也没有监听器的
 * 自定义惩罚；开枪后的乘客理智扣减被跳过。其余一切不变：接收器的 HEAD 否决仍决定这一枪是否发生，咔哒声、记录、德林加的已用
 * 状态、枪声、枪口火光与枪械自身的冷却都仍由 Wathe 处理。只使用可串联的 {@code @WrapOperation}（绝不 {@code @Redirect}），
 * 与同一调用上的法官、支配者包装可按任意顺序组合：各自只取消自己的情形，其他射击照常传递。
 */
@Mixin(GunShootPayload.Receiver.class)
public abstract class SaintGunShotReceiverMixin {
    @WrapOperation(method = "receive(Ldev/doctor4t/wathe/util/GunShootPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/api/event/ShouldPunishGunShooter;shouldPunish(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/player/PlayerEntity;)Ldev/doctor4t/wathe/api/event/ShouldPunishGunShooter$PunishResult;"))
    private ShouldPunishGunShooter.PunishResult sparkdlc$saintShotIsNeverPunished(
            ShouldPunishGunShooter event, PlayerEntity shooter, PlayerEntity victim,
            Operation<ShouldPunishGunShooter.PunishResult> original) {
        return SaintGunShotService.isSaint(shooter)
                ? ShouldPunishGunShooter.PunishResult.cancel()
                : original.call(event, shooter, victim);
    }

    @WrapOperation(method = "receive(Ldev/doctor4t/wathe/util/GunShootPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/PlayerMoodComponent;setMood(F)V"))
    private void sparkdlc$saintShotCostsNoMood(PlayerMoodComponent mood, float amount, Operation<Void> original,
                                                 GunShootPayload payload, ServerPlayNetworking.Context context) {
        if (!SaintGunShotService.isSaint(context.player())) {
            original.call(mood, amount);
        }
    }
}
