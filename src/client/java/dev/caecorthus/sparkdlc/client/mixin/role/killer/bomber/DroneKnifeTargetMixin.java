package dev.caecorthus.sparkdlc.client.mixin.role.killer.bomber;

import dev.caecorthus.sparkdlc.role.killer.bomber.drone.DroneHitGeometry;
import dev.caecorthus.sparkdlc.role.killer.bomber.drone.DroneWeaponRules;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerHitGeometry;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.wathe.item.KnifeItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Client knife target: Wathe's {@code KnifeItem.getKnifeTarget} only sees players, so a Bomber drone strictly nearer on
 * the look ray replaces the result. Every caller shares it: the charged release, the instant stabs (NoellesRoles
 * Scavenger, SparkStrength Veteran/Coroner disguise) and Wathe's crosshair knife hint; the server
 * ({@code DroneKnifeStabPayloadMixin}) validates and breaks. Client only: server-side calls are returned untouched.
 * Wathe member with a unique name, so the selector is name-only with {@code remap = false}.
 * 客户端刀目标：Wathe 的 KnifeItem.getKnifeTarget 只看玩家，因此视线上严格更近的炸弹客无人机会替换结果。所有调用方共用：
 * 蓄力释放、瞬刺（NoellesRoles 拾荒者、SparkStrength 老兵/验尸官伪装）以及 Wathe 准星刀提示；由服务端
 * DroneKnifeStabPayloadMixin 校验并击毁。仅客户端：服务端调用原样返回。Wathe 成员且方法名唯一，选择器仅用方法名并 remap = false。
 *
 * <p>A Partygoer strictly nearer than that result replaces it in turn; {@code DroneKnifeStabPayloadMixin} validates and
 * kills it. / 严格比该结果更近的派对客再次替换它；由 DroneKnifeStabPayloadMixin 校验并击杀。</p>
 *
 * <p>Priority 999 (set when the nine mixin configs were merged into two): keeps this mixin applied before the Seeker
 * target mixin on the same method, as it was while SparkStrength's client config was listed before SparkWitch's
 * common config. With equal priority the merged configs would let a Seeker device lose a near-tie to a drone.
 * 优先级 999（九个 mixin 配置合并为两个时设定）：使本 mixin 仍先于同一方法上的 Seeker 目标 mixin 应用，与原先
 * SparkStrength 客户端配置排在 SparkWitch 通用配置之前时一致；若优先级相同，合并后的配置会让探查者装置在近似并列时
 * 输给无人机。</p>
 */
@Mixin(value = KnifeItem.class, priority = 999)
public abstract class DroneKnifeTargetMixin {
    @ModifyReturnValue(method = "getKnifeTarget", at = @At("RETURN"), remap = false)
    private static HitResult sparkdlc$preferNearerDrone(HitResult original, PlayerEntity user) {
        if (user == null || !user.getWorld().isClient()) {
            return original;
        }
        return PartygoerHitGeometry.preferNearer(user,
                DroneHitGeometry.preferNearerDrone(user, original, DroneWeaponRules.KNIFE_RANGE),
                DroneWeaponRules.KNIFE_RANGE);
    }
}
