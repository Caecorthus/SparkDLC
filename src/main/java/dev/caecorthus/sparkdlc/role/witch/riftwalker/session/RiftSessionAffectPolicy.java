package dev.caecorthus.sparkdlc.role.witch.riftwalker.session;

import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.entity.player.PlayerEntity;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Player-affect policy ({@code PlayerAffectPolicy}, any denial wins): no player-affect action may target a player
 * inside a Rift Gate (D3: occupants are untouchable), except {@code noellesroles:swapper}, which must reach P9's portal
 * crush (D13), {@code wathe:poison}, the poisoner-attributed poison death that D3 keeps lethal, and the two piercing
 * terminal deaths {@code sparkdlc:bell_toll} / {@code sparkdlc:time_stolen} (C13). This closes the NoellesRoles packets
 * that ignore spectators (Assassin guess, Swapper, Morph/Voodoo, Reporter, Detective, Silencer, Party Animal) and every
 * killer-attributed {@code killPlayer} (the death reason is used as the action). The policy dispatch does not catch
 * policy exceptions, so this one fails closed itself: an error while the target may be inside denies. Side-neutral: on
 * a client only the owner's own state is known, the server decides.
 * 玩家影响策略（{@code PlayerAffectPolicy}，任一拒绝即生效）：任何玩家影响行为都不能作用于位于裂隙门内的玩家（D3：门内
 * 的人打不死），只放行必须到达 P9 传送门夹死逻辑的 {@code noellesroles:swapper}（D13）、D3 规定仍会致死、可归因到下毒者
 * 的毒杀 {@code wathe:poison}，以及两种穿透一切的终结死亡 {@code sparkdlc:bell_toll} / {@code sparkdlc:time_stolen}
 * （C13）。这同时封住了不检查旁观者的 NoellesRoles 数据包（刺客猜测、交换者、变形/巫毒、记者、侦探、沉默者、派对动物）
 * 以及所有归因到击杀者的 {@code killPlayer}（以死因作为行为 id）。策略分发不捕获策略异常，因此本策略自行失败即关闭：目
 * 标可能在门内时出错一律拒绝。两端通用：客户端只知道拥有者自己的状态，由服务端裁决。
 */
final class RiftSessionAffectPolicy {
    private static final AtomicBoolean ERROR_LOGGED = new AtomicBoolean();
    private static boolean registered;

    private RiftSessionAffectPolicy() {
    }

    static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        SparkFactionApi.registerPlayerAffectPolicy((actor, target, actionId, gameComponent) -> {
            try {
                return RiftSessionRules.occupantPolicyAllows(RiftSessionService.isInside(target),
                        isSelf(actor, target), actionId);
            } catch (RuntimeException | LinkageError error) {
                if (ERROR_LOGGED.compareAndSet(false, true)) {
                    SparkDLC.LOGGER.error("Rift Gate occupant affect policy failed; denying", error);
                }
                return false;
            }
        });
    }

    private static boolean isSelf(PlayerEntity actor, PlayerEntity target) {
        return actor != null && target != null && actor.getUuid().equals(target.getUuid());
    }
}
