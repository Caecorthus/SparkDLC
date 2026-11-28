package dev.caecorthus.sparkdlc.client.tablet.screen;

import dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.ButtonIcon;
import dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.ButtonStyle;
import dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.EmptyIcon;
import dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.Grid;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerSessionMode;
import dev.caecorthus.sparkdlc.role.civilian.seeker.net.SeekerRemoteOpenC2SPacket;
import dev.caecorthus.sparkdlc.tablet.TabletFeature;
import dev.caecorthus.sparkdlc.tablet.TabletLayout.Rect;
import dev.caecorthus.sparkdlc.tablet.TabletUiSession;
import dev.caecorthus.sparkdlc.tablet.TabletUiSession.Section;
import dev.caecorthus.sparkdlc.tablet.net.TabletSnapshot;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

import java.util.List;

import static dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.KEY;

/**
 * Camera section (owner spec 2026-11-28): one card per Seeker wall camera with a view button, for every living member
 * of the police network. It is the entry to the Seeker's camera view for players who have no Seeker Console. The page
 * only asks ({@link SeekerRemoteOpenC2SPacket}, CAMERA mode with that camera as the target, the request the Seeker's own
 * A/D cycling sends); the server re-validates every request and opens the session, and the Seeker's remote-view client
 * owns everything after that (camera, input, closing this screen, the A/D switch and Shift exit).
 * 摄像头分区（所有者规格 2026-11-28）：每台搜寻者摄像头一张带观看按钮的卡片，面向义警网络的每名存活成员。对没有搜寻者控制台的
 * 玩家，它是进入搜寻者摄像头视角的入口。本页只发出请求（SeekerRemoteOpenC2SPacket，CAMERA 模式并以该摄像头为目标，即搜寻者
 * 自己 A/D 切换时发送的请求）；服务端重新校验每个请求并打开会话，此后的一切（视角、输入、关闭本界面、A/D 切换与 Shift 退出）
 * 由搜寻者的遥控视角客户端负责。
 */
final class TabletCamerasPage implements TabletPage {
    // How long a sent view request shows "connecting" before the buttons re-arm. 观看请求显示“连接中”的时长。
    private static final int PENDING_VIEW_TICKS = 40;

    private final TabletScreen screen;
    // Camera rows of the current build; a snapshot that changes them rebuilds the widgets. 当前构建所依据的摄像头行。
    private List<TabletSnapshot.CameraRow> lastRows = List.of();
    private int pendingEntityId = -1;
    private int pendingTicks;
    // First visible row when the latest snapshot arrived, before it was clamped. 最新快照到达时（限制范围前）的首个可见行。
    private int snapshotFirstRow;

    TabletCamerasPage(TabletScreen screen) {
        this.screen = screen;
    }

    @Override
    public void prepare(TabletSnapshot snapshot) {
        clampRows(snapshot);
        lastRows = cameraRows(snapshot);
    }

    @Override
    public void onSnapshot(TabletSnapshot snapshot, boolean laidOut) {
        snapshotFirstRow = screen.session().cameraFirstRow();
        if (laidOut) {
            clampRows(snapshot);
        }
        if (pendingEntityId >= 0 && !lists(cameraRows(snapshot), pendingEntityId)) {
            // The camera is gone: the request can no longer be granted. 摄像头已消失：请求不可能再被批准。
            pendingEntityId = -1;
            pendingTicks = 0;
        }
    }

    private void clampRows(TabletSnapshot snapshot) {
        screen.session().clampCameras(cameraRows(snapshot).size(), cameraGrid().visibleRows());
    }

    @Override
    public boolean needsRebuild(TabletSnapshot snapshot) {
        return snapshotFirstRow != screen.session().cameraFirstRow() || !cameraRows(snapshot).equals(lastRows);
    }

    @Override
    public void tick(boolean open) {
        if (pendingTicks > 0 && --pendingTicks == 0) {
            // No session started (refused, e.g. the body is not on the ground; the server said why on the action bar):
            // re-arm the buttons. 会话未开始（被拒绝，例如本体未站在地面上；服务端已在动作栏说明原因）：恢复按钮。
            pendingEntityId = -1;
            if (open) {
                screen.refresh();
            }
        }
    }

    /**
     * One view button per visible camera card. While a request is pending every button waits; the server re-validates
     * every request.
     * 每张可见摄像头卡片一个观看按钮。请求未决时所有按钮都等待；服务端会重新校验每个请求。
     */
    @Override
    public void init(TabletSnapshot snapshot) {
        TextRenderer textRenderer = screen.textRenderer();
        Grid grid = cameraGrid();
        List<TabletSnapshot.CameraRow> rows = cameraRows(snapshot);
        int first = screen.session().cameraFirstRow();
        for (int row = 0; row < grid.visibleRows() && first + row < rows.size(); row++) {
            TabletSnapshot.CameraRow camera = rows.get(first + row);
            boolean connecting = camera.entityId() == pendingEntityId;
            Rect rect = TabletCamerasPainter.cameraButtonRect(textRenderer, grid.area().x(), grid.rowY(row),
                    grid.area().width());
            // Police accent whatever the channel: the cameras belong to the police network. 无论频道都用义警强调色：摄像头属于义警网络。
            screen.addButton("camera:" + camera.entityId(), rect,
                    Text.translatable(KEY + (connecting ? "cameras.connecting" : "cameras.view")),
                    connecting ? ButtonStyle.SECONDARY : ButtonStyle.PRIMARY,
                    connecting ? ButtonIcon.SPINNER : ButtonIcon.LINK,
                    TabletTheme.POLICE, () -> viewCamera(camera))
                    .active = pendingEntityId < 0;
        }
    }

    private void viewCamera(TabletSnapshot.CameraRow camera) {
        // No-op when the server never declared the Seeker's open channel (or there is no connection).
        // 服务端未声明搜寻者的打开通道（或无连接）时不发送。
        if (pendingEntityId >= 0 || !ClientPlayNetworking.canSend(SeekerRemoteOpenC2SPacket.ID)) {
            return;
        }
        ClientPlayNetworking.send(SeekerRemoteOpenC2SPacket.of(SeekerSessionMode.CAMERA, camera.entityId()));
        pendingEntityId = camera.entityId();
        pendingTicks = PENDING_VIEW_TICKS;
        // The remote-view client closes the tablet for the camera picture; reopening it lands back on the camera list.
        // 遥控视角客户端会关闭平板以显示摄像头画面；之后再打开平板时回到摄像头列表。
        TabletClientState.resumeOnNextOpen(Section.CAMERAS);
        screen.refresh();
    }

    @Override
    public boolean mouseScrolled(TabletSnapshot snapshot, double x, double y, double verticalAmount) {
        TabletUiSession session = screen.session();
        Grid grid = cameraGrid();
        if (grid.area().contains(x, y)) {
            // Each visible card owns a button, so a moved window rebuilds like DRONE. 每张可见卡片带按钮，滚动后与无人机分区一样重建。
            int previous = session.cameraFirstRow();
            session.scrollCameras(verticalAmount, cameraRows(snapshot).size(), grid.visibleRows());
            if (session.cameraFirstRow() != previous) {
                screen.refresh();
            }
            return true;
        }
        return false;
    }

    /** The camera count. 摄像头数量。 */
    @Override
    public int paintHeaderChip(TabletSnapshot snapshot, int right, int centerY, int maxWidth, long now) {
        return screen.chrome().paintPill(right, centerY, maxWidth, String.valueOf(cameraRows(snapshot).size()),
                TabletTheme.TEXT_2, TabletTheme.SURFACE, 0, TabletIcons::eye, TabletTheme.TEXT_2);
    }

    /**
     * One card per camera (view buttons are widgets, see init) in the server's viewing order; the empty state only
     * shows in the moment between the last camera going and the section leaving the rail.
     * 每台摄像头一张卡片（观看按钮是控件，见 init），按服务端的观看顺序排列；空状态只会出现在最后一台摄像头消失与该分区离开侧栏
     * 之间的一瞬。
     */
    @Override
    public void paintBody(TabletSnapshot snapshot, int hoverX, int hoverY, long now) {
        TabletCanvas c = screen.canvas();
        TextRenderer textRenderer = screen.textRenderer();
        Grid grid = cameraGrid();
        List<TabletSnapshot.CameraRow> rows = cameraRows(snapshot);
        if (rows.isEmpty()) {
            TabletSectionPainter.emptyState(c, textRenderer, grid.area(), EmptyIcon.CAMERA,
                    Text.translatable(KEY + "cameras.empty"), Text.translatable(KEY + "cameras.empty_hint"));
            return;
        }
        boolean showOwner = severalOwners(rows);
        int first = screen.session().cameraFirstRow();
        for (int row = 0; row < grid.visibleRows() && first + row < rows.size(); row++) {
            TabletCamerasPainter.cameraCard(c, textRenderer, grid.area().x(), grid.rowY(row), grid.area().width(),
                    rows.get(first + row), showOwner);
        }
        TabletSectionPainter.scrollThumb(c, screen.chrome().scrollTrack(grid.area()), first, grid.visibleRows(),
                rows.size());
    }

    private Grid cameraGrid() {
        return Grid.of(screen.layout().bodyNoFooter(), 1, TabletCamerasPainter.CAMERA_CARD_H,
                TabletCamerasPainter.CAMERA_ROW_STEP);
    }

    /** The viewer's camera rows in server order; empty without the feature. 查看者的摄像头行（服务端顺序）；无此功能时为空。 */
    private static List<TabletSnapshot.CameraRow> cameraRows(TabletSnapshot snapshot) {
        return snapshot.hasFeature(TabletFeature.CAMERAS) ? snapshot.cameras() : List.of();
    }

    private static boolean lists(List<TabletSnapshot.CameraRow> rows, int entityId) {
        for (TabletSnapshot.CameraRow row : rows) {
            if (row.entityId() == entityId) {
                return true;
            }
        }
        return false;
    }

    /** More than one Seeker owns a listed camera, so the cards say whose each one is. 列出的摄像头属于不止一名搜寻者时，卡片注明归属。 */
    private static boolean severalOwners(List<TabletSnapshot.CameraRow> rows) {
        for (TabletSnapshot.CameraRow row : rows) {
            if (row.owner() != rows.get(0).owner()) {
                return true;
            }
        }
        return false;
    }
}
