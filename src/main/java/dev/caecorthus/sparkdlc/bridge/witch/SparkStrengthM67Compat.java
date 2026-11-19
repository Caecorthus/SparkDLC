package dev.caecorthus.sparkdlc.bridge.witch;

import dev.caecorthus.sparkdlc.entity.M67GrenadeEntity;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerBreakSource;
import dev.caecorthus.sparkdlc.role.civilian.seeker.hit.SeekerDamageRules;
import dev.caecorthus.sparkdlc.role.civilian.seeker.hit.SeekerDeviceHits;
import dev.caecorthus.sparkdlc.role.killer.blackraven.crow.hit.BlackRavenCrowHits;
import dev.caecorthus.sparkdlc.role.killer.magician.MagicianPuppetHits;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import java.util.UUID;

/**
 * Cross-module seam: SparkStrength M67 blasts break Seeker devices via {@code ServerEntityEvents.ENTITY_UNLOAD} and the
 * grenade's two public getters, called directly since the SparkDLC fusion (SparkStrength always ships in the same jar).
 * SparkStrength exposes no blast event, so a detonation is recognised heuristically: an {@code sparkstrength:m67} entity unloaded with reason
 * DISCARDED whose {@code getDetonateAt()} is set and already reached, and whose {@code getThrowerUuid()} player is
 * online (M67 kills players first, then discards itself). Only a match grenade breaks devices
 * ({@link #breaksDevices}, the {@link dev.caecorthus.sparkdlc.util.OffMatchUse} rule): the Wathe status is exactly
 * ACTIVE and the thrower holds a match role. SparkStrength discards every in-flight M67 on a phase change, so a
 * detonation during STOPPING was thrown during STOPPING; that grenade, like a non-participant's, is presentation-only.
 * The blast then breaks devices in a 3.5-block sphere with line of sight to the grenade centre, attributed to the
 * thrower; the same blast ends Magician puppets in the M67's own kill radius ({@code MagicianPuppetHits.onM67Blast}).
 * Apart from the Magician replay seam ({@link SparkStrengthM67ReplayCompat}, the M67 use service), only this class may
 * name SparkStrength M67 implementation classes, and only those two getters (pinned by a source test); they are
 * compiled calls, so there is no lookup left to fail or to disable.
 * 跨模块接缝：SparkStrength M67 爆炸经 {@code ServerEntityEvents.ENTITY_UNLOAD} 与手雷的两个 public getter
 * 打坏搜寻者设备；融合进 SparkDLC 后为直接调用（SparkStrength 始终随同一个 jar 发布）。SparkStrength 没有爆炸事件，因此以启发式识别引爆：
 * {@code sparkstrength:m67} 实体以 DISCARDED 原因卸载、{@code getDetonateAt()} 已设置且已到时、
 * {@code getThrowerUuid()} 对应玩家在线（M67 先击杀玩家再移除自身）。只有对局手雷会打坏设备
 * （{@link #breaksDevices}，即 {@code OffMatchUse} 规则）：Wathe 状态恰为 ACTIVE 且投掷者持有对局职业。SparkStrength
 * 在阶段切换时会移除所有飞行中的 M67，因此 STOPPING 期间的引爆必定是 STOPPING 期间投出的；它与非参与者的手雷一样
 * 仅作表现。随后打坏以手雷中心为球心、半径 3.5、有视线的设备，归属于投掷者；同一次爆炸按 M67 自身的击杀半径结束魔术师皮套
 * （{@code MagicianPuppetHits.onM67Blast}）。除魔术师回放接缝（{@link SparkStrengthM67ReplayCompat}，M67 使用服务）外，
 * 只有本类可以提及 SparkStrength M67 实现类且只限这两个 getter（由源码测试固定）；两者均为编译期调用，不再有会失败或被关闭的查找。
 */
public final class SparkStrengthM67Compat {
    public static final String MOD_ID = "sparkstrength";
    public static final Identifier M67_ENTITY_ID = Identifier.of(MOD_ID, "m67");
    private static boolean registered;

    private SparkStrengthM67Compat() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerEntityEvents.ENTITY_UNLOAD.register(SparkStrengthM67Compat::onUnload);
    }

    private static void onUnload(Entity entity, ServerWorld world) {
        // The pinned id and the pinned class, as before: sparkstrength:m67 is SparkStrength's M67GrenadeEntity.
        // 与此前一样同时要求固定的 id 与固定的类：sparkstrength:m67 即 SparkStrength 的 M67GrenadeEntity。
        if (world == null || !(entity instanceof M67GrenadeEntity grenade)
                || entity.getRemovalReason() != Entity.RemovalReason.DISCARDED
                || !M67_ENTITY_ID.equals(Registries.ENTITY_TYPE.getId(entity.getType()))) {
            return;
        }
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        boolean roundActive = game.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE;
        if (!roundActive) {
            return;
        }
        long detonateAt = grenade.getDetonateAt();
        UUID throwerUuid = grenade.getThrowerUuid();
        if (throwerUuid == null) {
            return;
        }
        if (!breaksDevices(roundActive, game.hasAnyRole(throwerUuid), detonateAt, world.getTime())) {
            return;
        }
        ServerPlayerEntity thrower = world.getServer().getPlayerManager().getPlayer(throwerUuid);
        if (thrower == null) {
            return;
        }
        SeekerDeviceHits.onBlast(world, entity.getBoundingBox().getCenter(), SeekerDamageRules.M67_RADIUS, thrower,
                SeekerBreakSource.M67);
        // Magician seam: the same match blast ends puppets in the M67's own kill radius.
        // 魔术师接缝：同一次对局爆炸按 M67 自身的击杀半径结束皮套。
        MagicianPuppetHits.onM67Blast(world, entity.getBoundingBox().getCenter(), thrower);
        // Raven Form seam: the same blast kills the owner of every crow in the M67 kill sphere.
        // 化鸦接缝：同一次爆炸处死 M67 击杀球内每只乌鸦的主人。
        BlackRavenCrowHits.onM67Blast(world, entity.getBoundingBox().getCenter(), thrower);
    }

    /**
     * A match grenade whose fuse ran out: an ACTIVE round and a thrower with a match role.
     * 引信已到时的对局手雷：对局 ACTIVE 且投掷者持有对局职业。
     */
    static boolean breaksDevices(boolean roundActive, boolean throwerIsParticipant, long detonateAt, long worldTime) {
        return roundActive && throwerIsParticipant && detonated(detonateAt, worldTime);
    }

    /** A set fuse that has already run out. / 引信已设置且已到时。 */
    static boolean detonated(long detonateAt, long worldTime) {
        return detonateAt >= 0L && worldTime >= detonateAt;
    }
}
