package dev.caecorthus.sparkdlc.client.role.civilian.seeker.remote;

import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerCameraEntity;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerCameraLookRules;
import dev.caecorthus.sparkdlc.role.civilian.seeker.net.SeekerCameraLookC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * Viewer client: reports the camera view direction with {@code seeker_camera_look} while viewing a camera, so the
 * server can turn the head every player sees. Called once per client tick by {@link SeekerRemoteViewClient}; the
 * cadence is {@link SeekerCameraLookRules#shouldSend}. It never decides anything: the server re-validates the session
 * and clamps the look into the camera's cone.
 * <p>Several players may view one camera (owner spec 2026-11-28), each with its own picture, and the head follows the
 * latest look anybody sent. The quiet re-send therefore only repairs a look of ours that the server never showed
 * ({@link #acknowledged} still false): once the head has been seen at our last look, a head that points elsewhere was
 * turned by another viewer, and re-sending would make two idle viewers flip it back and forth forever.
 * 观看者客户端：观看摄像头时用 {@code seeker_camera_look} 上报视角，使服务端能转动所有玩家都能看到的机头。
 * 由 {@link SeekerRemoteViewClient} 每个客户端刻调用一次；节奏见 {@link SeekerCameraLookRules#shouldSend}。
 * 它不做任何决定：服务端会重新校验会话，并把视角钳制到摄像头的锥角内。
 * 多名玩家可以观看同一台摄像头（所有者规格 2026-11-28），各自拥有自己的画面，机头跟随任何人最新发送的视角。因此安静时的补发
 * 只用于修复服务端从未显示过的我方视角（{@link #acknowledged} 仍为 false）：一旦看到机头到过我们最后发送的视角，之后机头指向
 * 别处就是其他观看者转动的，此时补发会让两名静止的观看者永远来回抢转机头。
 */
public final class SeekerCameraLookSender {
    // A new viewpoint means a new binding; weak so a finished session never pins the old world.
    // 新的视点即新的绑定；使用弱引用，结束的会话不会拖住旧世界。
    private static WeakReference<SeekerCameraViewpoint> bound = new WeakReference<>(null);
    private static float lastYaw;
    private static float lastPitch;
    private static int ticksSinceSend;
    /**
     * The head has been seen at the last look we sent (or we have sent none for this viewpoint yet).
     * 已看到机头到过我们最后发送的视角（或尚未为该视点发送过任何视角）。
     */
    private static boolean acknowledged = true;

    private SeekerCameraLookSender() {
    }

    public static void tick(@Nullable SeekerCameraViewpoint viewpoint, int sessionId) {
        if (viewpoint == null) {
            return;
        }
        SeekerCameraEntity camera = viewpoint.mount();
        float yaw = MathHelper.wrapDegrees(viewpoint.getYaw());
        float pitch = viewpoint.getPitch();
        if (bound.get() != viewpoint) {
            // The viewpoint starts from the camera's synced look, so there is nothing to send yet.
            // 视点从摄像头同步的视角开始，因此此时无需发送。
            bound = new WeakReference<>(viewpoint);
            lastYaw = camera.lookYaw();
            lastPitch = camera.lookPitch();
            ticksSinceSend = 0;
            acknowledged = true;
        }
        ticksSinceSend = Math.min(ticksSinceSend + 1, SeekerCameraLookRules.RESYNC_TICKS);
        if (!SeekerCameraLookRules.differs(lastYaw, lastPitch, camera.lookYaw(), camera.lookPitch())) {
            acknowledged = true;
        }
        boolean moved = SeekerCameraLookRules.differs(yaw, pitch, lastYaw, lastPitch);
        boolean unacknowledged = !acknowledged
                && SeekerCameraLookRules.differs(yaw, pitch, camera.lookYaw(), camera.lookPitch());
        if (!SeekerCameraLookRules.shouldSend(ticksSinceSend, moved, unacknowledged)
                || !ClientPlayNetworking.canSend(SeekerCameraLookC2SPacket.ID)) {
            return;
        }
        ClientPlayNetworking.send(new SeekerCameraLookC2SPacket(sessionId, yaw, pitch));
        lastYaw = yaw;
        lastPitch = pitch;
        ticksSinceSend = 0;
        acknowledged = false;
    }
}
