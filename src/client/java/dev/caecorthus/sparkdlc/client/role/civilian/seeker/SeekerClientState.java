package dev.caecorthus.sparkdlc.client.role.civilian.seeker;

import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerCarState;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerCooldownReason;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerRules;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerSessionMode;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerState;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerStatusComponent;
import dev.caecorthus.sparkdlc.client.tablet.screen.TabletClientState;
import dev.caecorthus.sparkdlc.tablet.TabletFeature;
import dev.caecorthus.sparkdlc.tablet.net.TabletSnapshot;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Read helpers over the local player's own {@code sparkdlc:seeker_status}. Only the owner receives this component,
 * so every value here is about the local player; nothing is predicted. The session fields are also what a
 * police-channel member who is no Seeker holds while watching a Seeker's camera; the cameras such a viewer may watch
 * come from the tablet snapshot ({@link #viewableCameras}).
 * 读取本地玩家自己的 {@code sparkdlc:seeker_status} 的辅助方法。该组件只同步给拥有者，
 * 因此这里的每个值都属于本地玩家；不做任何预测。非搜寻者的义警频道成员观看搜寻者的摄像头时，持有的也是这些会话字段；
 * 这类观看者可以观看的摄像头来自平板快照（{@link #viewableCameras}）。
 */
public final class SeekerClientState {
    private SeekerClientState() {
    }

    @Nullable
    public static ClientPlayerEntity player() {
        return MinecraftClient.getInstance().player;
    }

    @Nullable
    public static SeekerStatusComponent own() {
        ClientPlayerEntity player = player();
        return player == null ? null : SeekerStatusComponent.KEY.get(player);
    }

    /** The local player's current Wathe role is exactly the Seeker. / 本地玩家当前的 Wathe 职业恰为搜寻者。 */
    public static boolean isSeeker() {
        ClientPlayerEntity player = player();
        return player != null && SeekerRules.isSeeker(GameWorldComponent.KEY.get(player.getWorld()).getRole(player));
    }

    public static SeekerCarState carState() {
        SeekerStatusComponent status = own();
        return status == null ? SeekerCarState.NONE : status.carState();
    }

    public static SeekerSessionMode sessionMode() {
        SeekerStatusComponent status = own();
        return status == null ? SeekerSessionMode.NONE : status.sessionMode();
    }

    public static boolean isInSession() {
        return sessionMode() != SeekerSessionMode.NONE;
    }

    public static int sessionId() {
        SeekerStatusComponent status = own();
        return status == null ? 0 : status.sessionId();
    }

    public static int carEntityId() {
        SeekerStatusComponent status = own();
        return status == null ? -1 : status.carEntityId();
    }

    /** The local owner's cameras in label order (synced). / 本地拥有者按编号排列的摄像头（同步值）。 */
    public static List<SeekerState.Camera> cameras() {
        SeekerStatusComponent status = own();
        return status == null ? List.of() : status.cameras();
    }

    public static int cameraCount() {
        SeekerStatusComponent status = own();
        return status == null ? 0 : status.cameraCount();
    }

    public static boolean hasCamera(int entityId) {
        SeekerStatusComponent status = own();
        return status != null && status.hasCamera(entityId);
    }

    /**
     * One camera the local player may view (owner spec 2026-11-28: every police-channel member views every Seeker's
     * cameras). {@code label} is the camera's own number at its owner ("CAM 01"); {@code owner} is the owning Seeker's
     * number from the tablet snapshot (0 when unknown) and {@code ownerName} that Seeker's name, empty when the server
     * withheld it; {@code own} marks the local Seeker's own cameras.
     * 本地玩家可以观看的一台摄像头（所有者规格 2026-11-28：义警频道的每名成员都能观看每名搜寻者的摄像头）。{@code label} 是
     * 该摄像头在其拥有者处的编号（“摄像头 01”）；{@code owner} 是平板快照给出的拥有者序号（未知时为 0），{@code ownerName} 是
     * 该搜寻者的名字，服务端未下发时为空；{@code own} 标记本地搜寻者自己的摄像头。
     */
    public record ViewCamera(int entityId, int label, int owner, String ownerName, boolean own) {
    }

    /**
     * Every camera the local player may view, in viewing order: the local Seeker's own cameras by label (from the
     * owner-synced component, which is immediate and needs no tablet in the hotbar), then the other Seekers' cameras
     * in the server's order (from the tablet snapshot's camera rows, which only a police-channel holder with the tablet
     * in the hotbar receives). A player who is no Seeker has only the second part. Presentation and prediction only:
     * the server validates every open request against its own list.
     * 本地玩家可以观看的全部摄像头，按观看顺序排列：先是本地搜寻者自己的摄像头按编号（来自仅同步给拥有者的组件，即时且不需要
     * 平板在快捷栏中），再是其他搜寻者的摄像头按服务端顺序（来自平板快照的摄像头行，只有平板在快捷栏中的义警频道持有者会收到）。
     * 非搜寻者的玩家只有后一部分。仅用于展示与预测：服务端按自己的列表校验每个打开请求。
     */
    public static List<ViewCamera> viewableCameras() {
        List<SeekerState.Camera> own = cameras();
        TabletSnapshot snapshot = TabletClientState.snapshot();
        List<TabletSnapshot.CameraRow> rows = snapshot.hasFeature(TabletFeature.CAMERAS)
                ? snapshot.cameras() : List.of();
        if (own.isEmpty() && rows.isEmpty()) {
            return List.of();
        }
        List<ViewCamera> cameras = new ArrayList<>(own.size() + rows.size());
        for (SeekerState.Camera camera : own) {
            TabletSnapshot.CameraRow row = rowOf(rows, camera.entityId());
            cameras.add(new ViewCamera(camera.entityId(), camera.label(), row == null ? 0 : row.owner(),
                    row == null ? "" : row.ownerName(), true));
        }
        for (TabletSnapshot.CameraRow row : rows) {
            if (!hasCamera(row.entityId())) {
                cameras.add(new ViewCamera(row.entityId(), row.label(), row.owner(), row.ownerName(), false));
            }
        }
        return cameras;
    }

    /**
     * {@link #viewableCameras} as the list the pure {@code SeekerCameraRules} cycle and count over: each element's
     * label is its 1-based viewing position, because two Seekers' own labels collide.
     * 把 {@link #viewableCameras} 转成纯规则 {@code SeekerCameraRules} 用于切换与计数的列表：每个元素的编号是其从 1 开始的
     * 观看位置，因为两名搜寻者各自的编号会重复。
     */
    public static List<SeekerState.Camera> viewingOrder() {
        List<ViewCamera> cameras = viewableCameras();
        List<SeekerState.Camera> order = new ArrayList<>(cameras.size());
        for (ViewCamera camera : cameras) {
            order.add(new SeekerState.Camera(camera.entityId(), order.size() + 1));
        }
        return order;
    }

    /** The viewable camera with that entity id, or null. / 该实体 id 对应的可观看摄像头，没有时为 null。 */
    @Nullable
    public static ViewCamera viewableCamera(int entityId) {
        if (entityId < 0) {
            return null;
        }
        for (ViewCamera camera : viewableCameras()) {
            if (camera.entityId() == entityId) {
                return camera;
            }
        }
        return null;
    }

    @Nullable
    private static TabletSnapshot.CameraRow rowOf(List<TabletSnapshot.CameraRow> rows, int entityId) {
        for (TabletSnapshot.CameraRow row : rows) {
            if (row.entityId() == entityId) {
                return row;
            }
        }
        return null;
    }

    /**
     * The synced session focus: the car or the exact camera being viewed; -1 without a session.
     * 同步的会话焦点：小车或正在观看的那台摄像头；无会话时为 -1。
     */
    public static int sessionFocusEntityId() {
        SeekerStatusComponent status = own();
        return status == null ? -1 : status.state().sessionFocusEntityId();
    }

    public static int carBattery() {
        SeekerStatusComponent status = own();
        return status == null ? 0 : SeekerRules.clampBattery(status.carBattery());
    }

    public static SeekerCooldownReason cooldownReason() {
        SeekerStatusComponent status = own();
        return status == null ? SeekerCooldownReason.NONE : status.cooldownReason();
    }

    @Nullable
    public static UUID markTarget() {
        SeekerStatusComponent status = own();
        return status == null || status.markRemainingTicks() <= 0 ? null : status.markTarget();
    }

    public static int markRemainingTicks() {
        SeekerStatusComponent status = own();
        return status == null ? 0 : Math.max(0, status.markRemainingTicks());
    }
}
