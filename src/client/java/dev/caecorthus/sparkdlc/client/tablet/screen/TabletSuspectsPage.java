package dev.caecorthus.sparkdlc.client.tablet.screen;

import dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.ButtonIcon;
import dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.ButtonStyle;
import dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.EmptyIcon;
import dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.Grid;
import dev.caecorthus.sparkdlc.tablet.net.ApproveSuspectRemovalC2SPacket;
import dev.caecorthus.sparkdlc.tablet.net.TabletSnapshot;
import dev.caecorthus.sparkdlc.tablet.net.TrackSuspectC2SPacket;
import dev.caecorthus.sparkdlc.tablet.TabletLayout.Rect;
import dev.caecorthus.sparkdlc.tablet.TabletRules;
import dev.caecorthus.sparkdlc.tablet.TabletUiSession;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.caecorthus.sparkdlc.client.tablet.screen.TabletMeetingPage.hasMeetingFeatures;
import static dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.KEY;

/**
 * Police suspects section: one card per suspect with an approve/cancel removal toggle and a Track button. Rebuilt on
 * every snapshot (approvals change); the server re-validates every toggle and every track.
 * 义警嫌疑分区：每名嫌疑人一张卡片，带建议/取消移除切换按钮与“追查”按钮。每次快照都会重建（赞成数变化）；
 * 服务端会重新校验每次切换与每次追查。
 *
 * <p>The Track button's state ({@link TrackPhase}) is read from the row's three world ticks against the client's world
 * time, like the other snapshot countdowns: its label is derived on every frame and its {@code active} flag on every
 * client tick, so the countdown runs and the button unlocks without a new snapshot (a running track changes none).
 * “追查”按钮的状态（TrackPhase）由该行的三个世界 tick 对照客户端世界时间得出，与其他快照倒计时相同：文字每帧计算，
 * active 标志每个客户端 tick 计算，因此无需新快照倒计时也会走动、按钮也会解锁（进行中的追查不会产生新快照）。</p>
 */
final class TabletSuspectsPage implements TabletPage {
    // A clicked Track button stays locked this long on the client's own clock, until the snapshot that carries the
    // track arrives; a second click in between would only earn the server's "cooling down" reply.
    // 点击后的“追查”按钮按客户端自己的时钟锁定这么久，直到带有该次追查的快照到达；期间的第二次点击只会换来服务端的“冷却中”回复。
    private static final int TRACK_PENDING_TICKS = 20;

    private final TabletScreen screen;
    // Track buttons of the current build, for the per-tick active flag. 当前构建中的“追查”按钮，供每 tick 更新 active 标志。
    private final List<TrackButton> trackButtons = new ArrayList<>();
    private final Map<UUID, Long> pendingTracks = new HashMap<>();

    TabletSuspectsPage(TabletScreen screen) {
        this.screen = screen;
    }

    @Override
    public void prepare(TabletSnapshot snapshot) {
        // Every init() starts here, whichever page is open: the previous build's widgets are gone.
        // 每次 init() 都从这里开始，无论打开的是哪一页：上一次构建的控件已被移除。
        trackButtons.clear();
        clampRows(snapshot);
    }

    @Override
    public void onSnapshot(TabletSnapshot snapshot, boolean laidOut) {
        if (laidOut) {
            clampRows(snapshot);
        }
    }

    private void clampRows(TabletSnapshot snapshot) {
        Grid suspects = suspectGrid();
        screen.session().applySuspectSnapshot(suspects.totalRows(snapshot.suspects().size()), suspects.visibleRows());
    }

    @Override
    public boolean needsRebuild(TabletSnapshot snapshot) {
        return true;
    }

    @Override
    public void init(TabletSnapshot snapshot) {
        if (hasMeetingFeatures(snapshot)) {
            initSuspects(snapshot);
        }
    }

    private void initSuspects(TabletSnapshot snapshot) {
        TextRenderer textRenderer = screen.textRenderer();
        Grid grid = suspectGrid();
        List<TabletSnapshot.SuspectRow> suspects = snapshot.suspects();
        int first = screen.session().suspectFirstRow();
        for (int row = 0; row < grid.visibleRows() && first + row < suspects.size(); row++) {
            TabletSnapshot.SuspectRow suspect = suspects.get(first + row);
            Rect toggle = TabletSuspectsPainter.suspectButtonRect(textRenderer, grid.area().x(), grid.rowY(row),
                    grid.area().width());
            screen.addButton("suspect:" + suspect.uuid(), toggle,
                    Text.translatable(KEY + (suspect.localApproved() ? "suspects.cancel" : "suspects.approve")),
                    suspect.localApproved() ? ButtonStyle.WARNING_TOGGLED : ButtonStyle.SECONDARY,
                    ButtonIcon.NONE,
                    () -> ClientPlayNetworking.send(new ApproveSuspectRemovalC2SPacket(suspect.uuid(), !suspect.localApproved())))
                    .active = snapshot.localMeetingParticipant();

            Rect trackRect = TabletSuspectsPainter.suspectTrackRect(textRenderer, grid.area().x(), grid.rowY(row),
                    grid.area().width());
            // Its own pressable instead of addButton: the label is a countdown, derived on every frame.
            // 不用 addButton 而是自建控件：文字是倒计时，每帧计算。
            TabletPressable trackButton = new TabletPressable(trackRect.x(), trackRect.y(), trackRect.width(),
                    trackRect.height(), Text.translatable(KEY + "suspects.track"), () -> track(suspect),
                    (context, button, hovered, delta) -> paintTrackButton(button, suspect, hovered));
            trackButton.active = trackPhase(snapshot, suspect, TabletClientState.worldTime()) == TrackPhase.READY;
            screen.keyed("track:" + suspect.uuid(), screen.addChild(trackButton));
            trackButtons.add(new TrackButton(trackButton, suspect));
        }
    }

    @Override
    public void tick(boolean open) {
        if (!open || trackButtons.isEmpty()) {
            return;
        }
        TabletSnapshot snapshot = TabletClientState.snapshot();
        long worldTime = TabletClientState.worldTime();
        pendingTracks.values().removeIf(until -> until <= worldTime);
        for (TrackButton entry : trackButtons) {
            entry.button().active = trackPhase(snapshot, entry.suspect(), worldTime) == TrackPhase.READY;
        }
    }

    private void track(TabletSnapshot.SuspectRow suspect) {
        long worldTime = TabletClientState.worldTime();
        if (trackPhase(TabletClientState.snapshot(), suspect, worldTime) != TrackPhase.READY) {
            return;
        }
        pendingTracks.put(suspect.uuid(), worldTime + TRACK_PENDING_TICKS);
        ClientPlayNetworking.send(new TrackSuspectC2SPacket(suspect.uuid()));
    }

    /**
     * What the Track button of {@code row} shows at {@code worldTime}. Pure presentation: the server decides again on
     * the click (who may track, the pair's cooldown).
     * row 的“追查”按钮在 worldTime 时显示的状态。仅为呈现：点击时由服务端重新判定（谁可以追查、该组合的冷却）。
     */
    private TrackPhase trackPhase(TabletSnapshot snapshot, TabletSnapshot.SuspectRow row, long worldTime) {
        // The same viewers the removal toggle serves (alive police members holding the tablet); nobody tracks
        // themselves. 与移除切换按钮服务的查看者相同（持有平板的存活义警成员）；不能追查自己。
        if (!snapshot.localMeetingParticipant() || row.uuid().equals(screen.localUuid())) {
            return TrackPhase.UNAVAILABLE;
        }
        if (worldTime < row.trackRevealStartTick()) {
            return TrackPhase.LOCATING;
        }
        if (worldTime < row.trackRevealEndTick()) {
            return TrackPhase.REVEALING;
        }
        if (worldTime < row.trackCooldownEndTick()) {
            return TrackPhase.COOLDOWN;
        }
        Long pendingUntil = pendingTracks.get(row.uuid());
        return pendingUntil != null && worldTime < pendingUntil ? TrackPhase.LOCATING : TrackPhase.READY;
    }

    private void paintTrackButton(TabletPressable button, TabletSnapshot.SuspectRow suspect, boolean hovered) {
        TextRenderer textRenderer = screen.textRenderer();
        long worldTime = TabletClientState.worldTime();
        Text name = Text.translatable(KEY + "suspects.track");
        ButtonStyle style = ButtonStyle.SECONDARY;
        ButtonIcon icon = ButtonIcon.NONE;
        Text label = name;
        switch (trackPhase(TabletClientState.snapshot(), suspect, worldTime)) {
            case READY -> style = ButtonStyle.PRIMARY;
            case UNAVAILABLE -> {
                // A narrow button drops the lock rather than the word. 按钮较窄时去掉锁形图标而不是文字。
                if (TabletSectionPainter.buttonFits(textRenderer, name, ButtonIcon.LOCK, button.getWidth())) {
                    icon = ButtonIcon.LOCK;
                }
            }
            case LOCATING -> {
                icon = ButtonIcon.SPINNER;
                // Between the click and its snapshot the row has no start tick yet: show the full delay.
                // 点击后、快照到达前该行还没有开始 tick：显示完整的延迟。
                label = seconds(suspect.trackRevealStartTick() > worldTime
                        ? TabletSnapshot.secondsUntil(suspect.trackRevealStartTick(), worldTime)
                        : TabletRules.secondsCeil(TabletRules.SUSPECT_TRACK_DELAY_TICKS));
            }
            case REVEALING -> {
                // Inactive WARNING_TOGGLED keeps its warning ink: reads as "on", not as disabled.
                // 未启用的 WARNING_TOGGLED 保留警示色文字：读作“进行中”而非禁用。
                style = ButtonStyle.WARNING_TOGGLED;
                int left = TabletSnapshot.secondsUntil(suspect.trackRevealEndTick(), worldTime);
                label = Text.translatable(KEY + "suspects.track.revealing", left);
                if (!TabletSectionPainter.buttonFits(textRenderer, label, ButtonIcon.NONE, button.getWidth())) {
                    label = seconds(left);
                }
            }
            case COOLDOWN -> {
                icon = ButtonIcon.CLOCK;
                label = seconds(TabletSnapshot.secondsUntil(suspect.trackCooldownEndTick(), worldTime));
            }
        }
        TabletSectionPainter.button(screen.canvas(), textRenderer, button, style, icon, label, hovered, screen.accent());
    }

    private static Text seconds(int seconds) {
        return Text.translatable(KEY + "suspects.track.seconds", seconds);
    }

    @Override
    public boolean mouseScrolled(TabletSnapshot snapshot, double x, double y, double verticalAmount) {
        TabletUiSession session = screen.session();
        Grid grid = suspectGrid();
        if (hasMeetingFeatures(snapshot) && grid.area().contains(x, y)) {
            int previous = session.suspectFirstRow();
            session.scrollSuspects(verticalAmount, grid.totalRows(snapshot.suspects().size()), grid.visibleRows());
            if (session.suspectFirstRow() != previous) {
                screen.refresh();
            }
            return true;
        }
        return false;
    }

    @Override
    public int paintHeaderChip(TabletSnapshot snapshot, int right, int centerY, int maxWidth, long now) {
        return screen.chrome().paintPill(right, centerY, maxWidth, String.valueOf(snapshot.suspects().size()),
                TabletTheme.TEXT_2, TabletTheme.SURFACE, 0, TabletIcons::target, TabletTheme.TEXT_2);
    }

    @Override
    public void paintBadge(TabletSnapshot snapshot, Rect icon, long now) {
        TabletCanvas c = screen.canvas();
        TextRenderer textRenderer = screen.textRenderer();
        int count = snapshot.suspects().size();
        if (count > 0) {
            String text = count > 99 ? "99+" : String.valueOf(count);
            int width = Math.max(11, textRenderer.getWidth(text) + 4);
            float left = icon.right() - 4;
            float top = icon.y() - 4;
            c.roundRect(left, top, width, 10f, 5f, TabletTheme.WARNING);
            c.text(textRenderer, text, Math.round(left + (width - textRenderer.getWidth(text) + 1) / 2f),
                    Math.round(top + 2), TabletTheme.WARNING_INK);
        }
    }

    @Override
    public void paintBody(TabletSnapshot snapshot, int hoverX, int hoverY, long now) {
        if (hasMeetingFeatures(snapshot)) {
            paintSuspects(snapshot);
        }
    }

    private void paintSuspects(TabletSnapshot snapshot) {
        TabletCanvas c = screen.canvas();
        TextRenderer textRenderer = screen.textRenderer();
        Grid grid = suspectGrid();
        List<TabletSnapshot.SuspectRow> suspects = snapshot.suspects();
        if (suspects.isEmpty()) {
            TabletSectionPainter.emptyState(c, textRenderer, grid.area(), EmptyIcon.SHIELD,
                    Text.translatable(KEY + "suspects.none"));
            return;
        }
        int first = screen.session().suspectFirstRow();
        for (int row = 0; row < grid.visibleRows() && first + row < suspects.size(); row++) {
            TabletSuspectsPainter.suspectCard(c, textRenderer, grid.area().x(), grid.rowY(row), grid.area().width(),
                    suspects.get(first + row), screen.accent());
        }
        TabletSectionPainter.scrollThumb(c, screen.chrome().scrollTrack(grid.area()), first, grid.visibleRows(), suspects.size());
    }

    private Grid suspectGrid() {
        return Grid.of(screen.layout().bodyNoFooter(), 1, TabletSuspectsPainter.SUSPECT_CARD_H,
                TabletSuspectsPainter.SUSPECT_ROW_STEP);
    }

    /**
     * READY: clickable. LOCATING: the 3 s before the outline (spinner). REVEALING: the suspect is outlined through
     * walls for this viewer. COOLDOWN: this viewer may not track this suspect again yet. UNAVAILABLE: the suspect
     * is the viewer, or the viewer may not track at all.
     * READY：可点击。LOCATING：描边出现前的 3 秒（转圈）。REVEALING：该嫌疑人正对此查看者穿墙描边。COOLDOWN：此查看者暂时
     * 不能再次追查该嫌疑人。UNAVAILABLE：嫌疑人就是查看者本人，或查看者根本不能追查。
     */
    private enum TrackPhase { READY, LOCATING, REVEALING, COOLDOWN, UNAVAILABLE }

    private record TrackButton(TabletPressable button, TabletSnapshot.SuspectRow suspect) {
    }
}
