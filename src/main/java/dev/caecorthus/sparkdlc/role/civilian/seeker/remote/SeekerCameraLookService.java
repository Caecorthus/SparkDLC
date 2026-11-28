package dev.caecorthus.sparkdlc.role.civilian.seeker.remote;

import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerSessionMode;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerStatusComponent;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerCameraEntity;
import dev.caecorthus.sparkdlc.role.civilian.seeker.net.SeekerCameraLookC2SPacket;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Server authority for {@code seeker_camera_look}: only a player's own open CAMERA session may turn the camera it
 * views, and {@link SeekerCameraEntity#setLook} clamps the value into that camera's cone. The session need not be the
 * camera owner's: every police-channel viewer of a camera turns its head (owner spec 2026-11-28), the head being
 * shared state of the entity, so with several viewers the latest look wins; the session itself was validated when it
 * opened and is re-validated every tick ({@code SeekerRemoteSessionService}). There is no per-tick gate: packets that
 * arrive together (network jitter) all apply and the latest wins, while the DataTracker still sends the look to other
 * clients at most once per tick. Every rejection is silent and never counts as cheating; the look is cosmetic (each
 * viewer's picture is its own client's).
 * {@code seeker_camera_look} 的服务端权威：只有玩家自己打开的 CAMERA 会话能转动其正在观看的摄像头，并由
 * {@link SeekerCameraEntity#setLook} 钳制到该摄像头的锥角内。该会话不必属于摄像头的拥有者：义警频道中观看某台摄像头的每名
 * 玩家都能转动其机头（所有者规格 2026-11-28），机头是实体的共享状态，因此多人观看时以最新的视角为准；会话本身在打开时已校验，
 * 并且每刻重新校验（{@code SeekerRemoteSessionService}）。没有逐刻闸门：因网络抖动同时到达的包全部应用、以最新的为准，
 * 而 DataTracker 向其他客户端发送视角仍然每刻最多一次。所有拒绝都静默处理且不计为作弊；视角只是外观（每名观看者的画面
 * 由其自己的客户端决定）。
 */
public final class SeekerCameraLookService {
    private SeekerCameraLookService() {
    }

    public static void handleLook(ServerPlayerEntity player, SeekerCameraLookC2SPacket packet) {
        if (player == null || packet == null || !Float.isFinite(packet.yaw()) || !Float.isFinite(packet.pitch())) {
            return;
        }
        SeekerStatusComponent status = SeekerStatusComponent.KEY.get(player);
        SeekerSessionState session = status.sessionState();
        if (session == null || status.sessionMode() != SeekerSessionMode.CAMERA
                || packet.sessionId() != status.sessionId() || session.sessionId != status.sessionId()
                || session.focusEntityId != status.state().sessionFocusEntityId()) {
            return;
        }
        Entity focus = player.getServerWorld().getEntityById(session.focusEntityId);
        if (!(focus instanceof SeekerCameraEntity camera) || camera.isRemoved()) {
            return;
        }
        camera.setLook(packet.yaw(), packet.pitch());
    }
}
