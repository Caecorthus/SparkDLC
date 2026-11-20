package dev.caecorthus.sparkdlc.client.role.killer.natsume;

import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.caecorthus.sparkdlc.role.killer.natsume.NatsumeComponent;
import dev.caecorthus.sparkdlc.role.killer.natsume.NatsumeRules;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 夏目安安 local client reads: the raw role check and the local player's own {@code sparkdlc:natsume_anan_player} copy
 * (synced to its owner). The server starts, times and ends the hypnosis; the client only honours its own synced seconds
 * and never predicts them. A null player reads as "not hypnotised".
 * 夏目安安的本地客户端读取：真实职业判断，以及本地玩家自身 {@code sparkdlc:natsume_anan_player} 副本（仅同步给拥有者）。
 * 催眠的开始、计时与结束都由服务端决定；客户端只遵守自己收到的同步秒数，从不预测。玩家为 null 时视为“未被催眠”。
 */
public final class NatsumeClientState {
    private NatsumeClientState() {
    }

    /** Pure: whether a raw Wathe role is 夏目安安. / 纯函数：Wathe 真实职业是否为夏目安安。 */
    public static boolean isNatsume(@Nullable Role role) {
        return role != null && NatsumeRules.ROLE_ID.equals(role.identifier());
    }

    /** Whether this player's own synced copy says hypnotised; null-safe. / 该玩家自身同步副本是否处于催眠中；空安全。 */
    public static boolean isHypnotised(@Nullable PlayerEntity player) {
        if (player == null) {
            return false;
        }
        return NatsumeComponent.KEY.get(player).isHypnotised();
    }

    /**
     * True while the hypnosis binds the local player: a confirmed SparkDLC server, a living participant, and its own
     * synced hypnosis running. Called for every instinct query and HUD text draw, so the O(1) checks run first and the
     * participant lookup only for a hypnotised player. Dead or spectating viewers are never bound.
     * 催眠约束本地玩家时返回 true：已确认 SparkDLC 服务器、存活参与者、且其自身同步的催眠正在进行。每次本能查询与 HUD
     * 文字绘制都会调用，因此先做 O(1) 判断，只有被催眠时才查询参与状态。死亡或旁观的观察者永不受约束。
     */
    public static boolean bindsLocalPlayer() {
        if (!SparkDlcServerConnection.isConfirmedServer()) {
            return false;
        }
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return isHypnotised(player) && GameFunctions.isPlayerPlayingAndAlive(player);
    }
}
