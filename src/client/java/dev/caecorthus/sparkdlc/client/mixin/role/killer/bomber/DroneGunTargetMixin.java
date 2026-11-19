package dev.caecorthus.sparkdlc.client.mixin.role.killer.bomber;

import dev.caecorthus.sparkdlc.role.killer.bomber.drone.DroneHitGeometry;
import dev.caecorthus.sparkdlc.role.killer.bomber.drone.DroneWeaponRules;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerHitGeometry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.item.DerringerItem;
import dev.doctor4t.wathe.item.RevolverItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Client gun target: wraps the {@code getGunTarget} call inside Revolver/Derringer {@code use} (vanilla override,
 * remapped) rather than the static method itself, because SparkTraits Marksman replaces {@code getGunTarget} at HEAD
 * with its own players-only raycast. The original is called first (keeping Marksman's range), then a Bomber drone
 * strictly nearer than its hit replaces it, so Wathe sends the drone id and {@code DroneGunShootPayloadMixin} validates
 * and breaks it; the player behind is not shot. Chains with SparkWitch's Seeker wrapper on the same call. The
 * {@code @At} targets keep the default remap so their Minecraft descriptor types are remapped in production.
 * 客户端枪械目标：包装左轮/德林加 use（原版覆写，需重映射）中对 getGunTarget 的调用，而不是该静态方法本身，因为 SparkTraits
 * 神射手会在 HEAD 用只看玩家的射线替换 getGunTarget。先调用原方法（保留神射手射程），若有严格更近的炸弹客无人机则替换结果，
 * Wathe 随即发送无人机 id，由 DroneGunShootPayloadMixin 校验并击毁；身后的玩家不会中枪。与 SparkWitch 搜寻者在同一调用上的包装链式共存。
 * @At 目标保持默认 remap，使其中的 Minecraft 描述符类型在生产环境被重映射。
 *
 * <p>A Partygoer strictly nearer than that result replaces it in turn (so the nearest of player, drone and Partygoer
 * wins); {@code DroneGunShootPayloadMixin} validates and kills it.
 * 严格比该结果更近的派对客再次替换它（因此玩家、无人机与派对客中最近者命中）；由 DroneGunShootPayloadMixin 校验并击杀。</p>
 *
 * <p>Priority 999 (set when the nine mixin configs were merged into two): keeps this mixin applied before the Seeker
 * target mixin on the same method, as it was while SparkStrength's client config was listed before SparkWitch's
 * common config. With equal priority the merged configs would let a Seeker device lose a near-tie to a drone.
 * 优先级 999（九个 mixin 配置合并为两个时设定）：使本 mixin 仍先于同一方法上的 Seeker 目标 mixin 应用，与原先
 * SparkStrength 客户端配置排在 SparkWitch 通用配置之前时一致；若优先级相同，合并后的配置会让探查者装置在近似并列时
 * 输给无人机。</p>
 */
@Mixin(value = {RevolverItem.class, DerringerItem.class}, priority = 999)
public abstract class DroneGunTargetMixin {
    @WrapOperation(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
            at = {
                    @At(value = "INVOKE",
                            target = "Ldev/doctor4t/wathe/item/RevolverItem;getGunTarget(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/hit/HitResult;"),
                    @At(value = "INVOKE",
                            target = "Ldev/doctor4t/wathe/item/DerringerItem;getGunTarget(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/hit/HitResult;")
            })
    private HitResult sparkstrength$preferNearerDrone(PlayerEntity user, Operation<HitResult> original) {
        HitResult result = original.call(user);
        double range = (Object) this instanceof DerringerItem
                ? DroneWeaponRules.DERRINGER_RANGE : DroneWeaponRules.REVOLVER_RANGE;
        return PartygoerHitGeometry.preferNearer(user, DroneHitGeometry.preferNearerDrone(user, result, range), range);
    }
}
