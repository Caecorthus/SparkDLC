package dev.caecorthus.sparkdlc.item.m67;

import dev.caecorthus.sparkdlc.SparkDlcEntities;
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
import java.util.UUID;

/**
 * What a match M67 blast hits besides players: Seeker devices, Magician puppets and Black Raven crows. The grenade has
 * no blast event, so a detonation is recognised heuristically from {@code ServerEntityEvents.ENTITY_UNLOAD} and the
 * grenade's two public getters: a {@code sparkdlc:m67} entity unloaded with reason
 * DISCARDED whose {@code getDetonateAt()} is set and already reached, and whose {@code getThrowerUuid()} player is
 * online (the M67 kills players first, then discards itself). Only a match grenade counts
 * ({@link #breaksDevices}, the {@link dev.caecorthus.sparkdlc.util.OffMatchUse} rule): the Wathe status is exactly
 * ACTIVE and the thrower holds a match role. Every in-flight M67 is discarded on a phase change, so a
 * detonation during STOPPING was thrown during STOPPING; that grenade, like a non-participant's, is presentation-only.
 * The blast then breaks devices in a 3.5-block sphere with line of sight to the grenade centre, attributed to the
 * thrower; the same blast ends Magician puppets in the M67's own kill radius ({@code MagicianPuppetHits.onM67Blast})
 * and kills the owner of every crow in that sphere ({@code BlackRavenCrowHits.onM67Blast}). The listener is registered
 * with the Seeker feature ({@code SeekerFeatureService}), whose device rule it was written for; of the grenade it
 * calls only those two getters (pinned by a source test).
 * 对局中的 M67 爆炸除玩家之外还会命中什么：搜寻者设备、魔术师皮套与黑羽鸦的乌鸦。手雷没有爆炸事件，因此以
 * {@code ServerEntityEvents.ENTITY_UNLOAD} 与手雷的两个 public getter 启发式识别引爆：
 * {@code sparkdlc:m67} 实体以 DISCARDED 原因卸载、{@code getDetonateAt()} 已设置且已到时、
 * {@code getThrowerUuid()} 对应玩家在线（M67 先击杀玩家再移除自身）。只有对局手雷才算
 * （{@link #breaksDevices}，即 {@code OffMatchUse} 规则）：Wathe 状态恰为 ACTIVE 且投掷者持有对局职业。
 * 阶段切换时所有飞行中的 M67 都会被移除，因此 STOPPING 期间的引爆必定是 STOPPING 期间投出的；它与非参与者的手雷一样
 * 仅作表现。随后打坏以手雷中心为球心、半径 3.5、有视线的设备，归属于投掷者；同一次爆炸按 M67 自身的击杀半径结束魔术师皮套
 * （{@code MagicianPuppetHits.onM67Blast}），并处死该球内每只乌鸦的主人（{@code BlackRavenCrowHits.onM67Blast}）。
 * 监听器随搜寻者功能（{@code SeekerFeatureService}）注册，它最初是为搜寻者的设备规则编写的；对手雷只调用这两个 getter
 * （由源码测试固定）。
 */
public final class M67MatchBlastHits {
    private static boolean registered;

    private M67MatchBlastHits() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerEntityEvents.ENTITY_UNLOAD.register(M67MatchBlastHits::onUnload);
    }

    private static void onUnload(Entity entity, ServerWorld world) {
        // Both the pinned id and the pinned class: sparkdlc:m67 is the M67GrenadeEntity.
        // 同时要求固定的 id 与固定的类：sparkdlc:m67 即 M67GrenadeEntity。
        if (world == null || !(entity instanceof M67GrenadeEntity grenade)
                || entity.getRemovalReason() != Entity.RemovalReason.DISCARDED
                || !SparkDlcEntities.M67_ID.equals(Registries.ENTITY_TYPE.getId(entity.getType()))) {
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
