package dev.caecorthus.sparkdlc.bridge.witch;

import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerCorpseComponent;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerHitGeometry;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerService;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;

/**
 * SparkStrength Partygoer seams (owner decision 2026-10-21): the Party Animal's summoned zombie
 * ({@code sparkdlc:partygoer}) dies in one hit to anything that can kill a player, and the corpse it leaves is not a
 * passenger's. The modules are fused in SparkDLC, so the seams are direct calls; this stays the only SparkWitch class
 * that names them:
 * SparkStrength 派对客接缝（所有者 2026-10-21 决定）：派对狂召唤的僵尸（{@code sparkdlc:partygoer}）会被任何能杀死
 * 玩家的东西一击打死，它留下的尸体不是乘客的尸体。各模块已融合进 SparkDLC，因此这些接缝均为直接调用；仍然只有本类点名它们：
 * <ul>
 *   <li>{@code dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerService#killByWeapon(ServerPlayerEntity, Entity,
 *   Identifier)}: server; SparkStrength pays the reward, drops whoever it holds and leaves the corpse.
 *   / 服务端；SparkStrength 发放奖励、放下手中的人并留下尸体。</li>
 *   <li>{@code PartygoerService#isKillable(Entity)}: server; false while it is inside a Taotie. / 服务端；在饕餮肚里时为 false。</li>
 *   <li>{@code dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerCorpseComponent#isPartygoerCorpse(Entity)}: both
 *   sides. / 双端可用。</li>
 *   <li>{@code dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerHitGeometry#preferNearer(Entity, HitResult, double)}:
 *   any side; the strictly nearer Partygoer on a gun ray, used by the Savior pistol's client pick (PR #200) like the
 *   SparkStrength Serial Killer pistols. / 双端；枪械射线上严格更近的派对客，救世主手枪的客户端选靶（PR #200）与
 *   SparkStrength 连环杀手手枪一样使用。</li>
 * </ul>
 */
public final class SparkStrengthPartygoerCompat {
    private static final String MOD_ID = "sparkdlc";
    /** The entity type, checked before any SparkStrength call. / 实体类型 id，在调用 SparkStrength 之前检查。 */
    public static final Identifier PARTYGOER_ID = Identifier.of(MOD_ID, "partygoer");

    private SparkStrengthPartygoerCompat() {
    }

    /** Cheap type check, any side. / 廉价的类型判断，双端可用。 */
    public static boolean isPartygoer(@Nullable Entity entity) {
        return entity != null && PARTYGOER_ID.equals(Registries.ENTITY_TYPE.getId(entity.getType()));
    }

    /**
     * Server: a Partygoer a weapon may hit right now (not inside a Taotie, not already dead).
     * 服务端：此刻可被武器命中的派对客（不在饕餮肚里、尚未死亡）。
     */
    public static boolean isKillable(@Nullable Entity entity) {
        // The kill seam is a compiled call since the SparkDLC fusion, so the old "no kill seam, never a target" guard
        // (a Partygoer nobody could kill would shield players and spend skills for nothing) has nothing left to check.
        // 融合进 SparkDLC 后击杀接缝是编译期调用，原先“没有击杀接缝就不当作目标”的守卫（杀不掉的派对客会白白挡住玩家、
        // 耗掉技能）已无可检查之处。
        if (!isPartygoer(entity) || entity.isRemoved() || entity.isInvisible()) {
            return false;
        }
        return PartygoerService.isKillable(entity);
    }

    /**
     * Server: kills the Partygoer as {@code attacker}'s hit with SparkWitch's own death reason. The caller has already
     * validated its weapon (charge, cooldown, reach, aim); SparkStrength only checks the target. True iff it died.
     * 服务端：把这次命中当作 {@code attacker} 的武器击杀派对客，死因为 SparkWitch 自己的死因。调用方已校验武器（蓄力、冷却、距离、
     * 瞄准）；SparkStrength 只检查目标。仅当它确实死亡时返回 true。
     */
    public static boolean killByWeapon(@Nullable ServerPlayerEntity attacker, @Nullable Entity target,
                                       @Nullable Identifier deathReason) {
        if (!isKillable(target)) {
            return false;
        }
        return PartygoerService.killByWeapon(attacker, target, deathReason);
    }

    /** Any side: a corpse left by a killed Partygoer (not a passenger). / 双端：被打死的派对客留下的尸体（不是乘客）。 */
    public static boolean isPartygoerCorpse(@Nullable Entity entity) {
        return entity != null && PartygoerCorpseComponent.isPartygoerCorpse(entity);
    }

    /**
     * Any side: {@code original}, or a strictly nearer targetable Partygoer on the shooter's gun ray within
     * {@code range}. A null shooter, pick or answer returns {@code original} unchanged.
     * 双端：返回 {@code original}，或射手枪械射线上 {@code range} 内严格更近、可被瞄准的派对客。射手、原选择或结果为 null 时
     * 原样返回 {@code original}。
     */
    public static @Nullable HitResult preferNearer(@Nullable Entity shooter, @Nullable HitResult original,
                                                   double range) {
        if (shooter == null || original == null) {
            return original;
        }
        HitResult result = PartygoerHitGeometry.preferNearer(shooter, original, range);
        return result != null ? result : original;
    }
}
