package dev.caecorthus.sparkdlc.client.tablet.screen;

import dev.caecorthus.sparkdlc.tablet.net.TabletSnapshot;
import dev.caecorthus.sparkdlc.tablet.TabletLayout;
import dev.caecorthus.sparkdlc.tablet.TabletRules;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

import static dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.BUTTON_ICON;
import static dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.BUTTON_ICON_GAP;
import static dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.CARD_R;
import static dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.KEY;

/**
 * Stateless painters for the suspects section: one card per suspect; its two buttons are widgets placed by
 * TabletSuspectsPage, the approve/cancel removal toggle at {@link #suspectButtonRect} and, right of it at the card's
 * edge, the Track button at {@link #suspectTrackRect}.
 * 嫌疑分区的无状态绘制器：每名嫌疑人一张卡片；其两个按钮是 TabletSuspectsPage 放置的控件：建议/取消移除切换按钮位于
 * suspectButtonRect，其右侧贴卡片边缘的“追查”按钮位于 suspectTrackRect。
 */
final class TabletSuspectsPainter {
    static final int SUSPECT_CARD_H = 40;
    static final int SUSPECT_ROW_STEP = 46;

    private static final int SUSPECT_AVATAR = 24;
    private static final int SUSPECT_TEXT_X = 49;
    private static final int SUSPECT_BAR_W = 120;
    private static final int SUSPECT_BAR_H = 4;
    private static final int SUSPECT_BUTTON_MIN_W = 92;
    private static final int SUSPECT_BUTTON_H = 24;
    private static final int SUSPECT_BUTTON_INSET = 8;
    private static final int SUSPECT_BUTTON_TEXT_PAD = 28;
    private static final int SUSPECT_BUTTON_GAP = 6;
    private static final int SUSPECT_CONTENT_GAP = 10;
    // Track button: wide enough for its widest state; on a narrow card it shrinks to this before the toggle does.
    // “追查”按钮：宽度容得下其最宽的状态；卡片较窄时先于切换按钮缩到此宽度。
    private static final int SUSPECT_TRACK_MIN_W = 52;
    private static final int SUSPECT_TRACK_TEXT_PAD = 20;
    // Narrow card: the buttons give way until the name keeps this much room; the toggle never drops below its floor.
    // 卡片较窄时按钮让出空间，直到名字保有这么多宽度；切换按钮不会小于其下限。
    private static final int SUSPECT_MIN_TEXT_W = 56;
    private static final int SUSPECT_BUTTON_FLOOR_W = 48;

    private TabletSuspectsPainter() {
    }

    /**
     * Suspect card; everything right of {@link #suspectButtonRect} minus 10 px stays free for the two button widgets
     * (the removal toggle, then the Track button).
     * 切换按钮区域左侧 10 px 以右留给两个按钮控件（移除切换按钮，其右为“追查”按钮）。
     */
    static void suspectCard(TabletCanvas c, TextRenderer tr, int x, int y, int w, TabletSnapshot.SuspectRow row,
                            TabletTheme.Accent a) {
        int h = SUSPECT_CARD_H;
        int centerY = y + h / 2;
        c.roundRect(x, y, w, h, CARD_R, TabletTheme.SURFACE);
        c.roundRect(x + 6, centerY - 9, 3, 18, 1.5f, TabletTheme.WARNING);

        int avatarX = x + 15;
        int avatarY = centerY - SUSPECT_AVATAR / 2;
        c.avatar(row.uuid(), row.name(), avatarX, avatarY, SUSPECT_AVATAR, 6f, TabletTheme.SURFACE);
        c.roundRectOutline(avatarX - 1, avatarY - 1, SUSPECT_AVATAR + 2, SUSPECT_AVATAR + 2, 7f, 1f,
                TabletTheme.withAlpha(TabletTheme.WARNING, 0x99));

        int textX = x + SUSPECT_TEXT_X;
        int contentRight = suspectButtonRect(tr, x, y, w).x() - SUSPECT_CONTENT_GAP;
        int room = contentRight - textX;
        if (room <= 0) {
            return;
        }
        // Two lines centred as a block: name (8) + gap 8 + bar/votes line (8). 两行整体居中：名字、间距、进度行。
        int nameY = centerY - 11;
        int lineY = centerY + 3;
        c.text(tr, TabletCanvas.trim(tr, row.name(), room), textX, nameY, TabletTheme.TEXT);

        int required = Math.max(0, row.requiredApprovals());
        int approvals = Math.max(0, row.approvals());
        String full = Text.translatable(KEY + "suspects.votes", approvals, required).getString();
        String compact = approvals + "/" + required;
        String label;
        int barW;
        int fullW = tr.getWidth(full);
        if (room >= 60 + 6 + fullW) {
            label = full;
            barW = Math.min(SUSPECT_BAR_W, room - 6 - fullW);
        } else {
            // Narrow card: keep the bar, fall back to "a/b". 卡片较窄：保留进度条，文字退化为“a/b”。
            label = compact;
            barW = Math.min(SUSPECT_BAR_W, room - 6 - tr.getWidth(compact));
            if (barW < 16) {
                barW = 0;
                label = TabletCanvas.trim(tr, compact, room);
            }
        }
        if (barW > 0) {
            float fraction = required <= 0 ? 0f : Math.max(0f, Math.min(1f, approvals / (float) required));
            float barY = lineY + 4 - SUSPECT_BAR_H / 2f;
            c.roundRect(textX, barY, barW, SUSPECT_BAR_H, SUSPECT_BAR_H / 2f, TabletTheme.FAINT);
            if (fraction > 0f) {
                c.roundRect(textX, barY, Math.max(SUSPECT_BAR_H, barW * fraction), SUSPECT_BAR_H,
                        SUSPECT_BAR_H / 2f, TabletTheme.WARNING);
            }
        }
        c.text(tr, label, textX + (barW > 0 ? barW + 6 : 0), lineY, TabletTheme.TEXT_3);
    }

    /**
     * Removal toggle rect inside a suspect card (vertically centred, immediately left of {@link #suspectTrackRect}).
     * The width fits the wider of the approve/cancel labels (clamped to [92, card width / 3]) and is identical for
     * every row, so buttons line up and never change size when toggled; a narrow card shrinks it, see
     * {@link #buttonWidths}. Painter and screen must both call this so paint and hit box stay identical.
     * 嫌疑卡片内的移除切换按钮区域（垂直居中，紧贴 suspectTrackRect 左侧）。宽度取“建议/取消”两种文字中较宽者
     * （限制在 [92, 卡片宽/3]），每行相同，按钮对齐且切换时不变宽；卡片较窄时会缩小，见 buttonWidths。
     * 绘制器与界面都必须调用此方法，使绘制与点击区域一致。
     */
    static TabletLayout.Rect suspectButtonRect(TextRenderer tr, int x, int y, int w) {
        ButtonWidths widths = buttonWidths(tr, w);
        int trackX = x + w - SUSPECT_BUTTON_INSET - widths.track();
        return new TabletLayout.Rect(trackX - SUSPECT_BUTTON_GAP - widths.removal(),
                y + (SUSPECT_CARD_H - SUSPECT_BUTTON_H) / 2, widths.removal(), SUSPECT_BUTTON_H);
    }

    /**
     * Track button rect inside a suspect card (right-aligned, vertically centred). Its width fits the widest state the
     * button can show (ready or locked, winding up, revealing, cooling down) and is identical for every row, so the
     * countdowns never shift the layout.
     * 嫌疑卡片内的“追查”按钮区域（右对齐、垂直居中）。宽度取按钮可能显示的最宽状态（就绪或锁定、定位中、显示中、冷却中），
     * 每行相同，倒计时不会让布局移动。
     */
    static TabletLayout.Rect suspectTrackRect(TextRenderer tr, int x, int y, int w) {
        int width = buttonWidths(tr, w).track();
        return new TabletLayout.Rect(x + w - SUSPECT_BUTTON_INSET - width, y + (SUSPECT_CARD_H - SUSPECT_BUTTON_H) / 2,
                width, SUSPECT_BUTTON_H);
    }

    /**
     * Widths of the two buttons for a card {@code w} wide; they depend on the card width and the language only, never
     * on the row. When both at full width would leave the name less than {@link #SUSPECT_MIN_TEXT_W}, the Track
     * button shrinks first (its countdowns are short; TabletSuspectsPage picks a label that fits), then the toggle
     * (its label is trimmed); neither ever leaves the card.
     * 宽为 w 的卡片上两个按钮的宽度；只取决于卡片宽度与语言，与行无关。若两者都取全宽会让名字不足 SUSPECT_MIN_TEXT_W，
     * 先缩小“追查”按钮（其倒计时很短；TabletSuspectsPage 会选用放得下的文字），再缩小切换按钮（其文字被截断）；
     * 两者都不会超出卡片。
     */
    private static ButtonWidths buttonWidths(TextRenderer tr, int w) {
        int labelW = Math.max(tr.getWidth(Text.translatable(KEY + "suspects.approve")),
                tr.getWidth(Text.translatable(KEY + "suspects.cancel")));
        int removal = Math.max(SUSPECT_BUTTON_MIN_W, labelW + SUSPECT_BUTTON_TEXT_PAD);
        // Upper clamp never drops below the minimum (Math.clamp would throw when w / 3 < 92).
        // 上限不低于最小宽度（w / 3 < 92 时 Math.clamp 会抛异常）。
        removal = Math.min(removal, Math.max(SUSPECT_BUTTON_MIN_W, w / 3));
        int track = Math.max(SUSPECT_TRACK_MIN_W, trackLabelWidth(tr) + SUSPECT_TRACK_TEXT_PAD);

        int budget = w - SUSPECT_TEXT_X - SUSPECT_MIN_TEXT_W - SUSPECT_CONTENT_GAP - SUSPECT_BUTTON_GAP
                - SUSPECT_BUTTON_INSET;
        int over = track + removal - budget;
        if (over > 0) {
            int trackCut = Math.min(over, track - SUSPECT_TRACK_MIN_W);
            track -= trackCut;
            removal = Math.max(SUSPECT_BUTTON_FLOOR_W, removal - (over - trackCut));
        }
        // Degenerate canvas: the buttons stay inside the card even if nothing else fits. 退化画布：按钮仍留在卡片内。
        int room = Math.max(0, w - 2 * SUSPECT_BUTTON_INSET);
        track = Math.min(track, room);
        removal = Math.max(0, Math.min(removal, room - track - SUSPECT_BUTTON_GAP));
        return new ButtonWidths(track, removal);
    }

    /** Widest content the Track button shows at full width, icon included. “追查”按钮全宽时显示的最宽内容（含图标）。 */
    private static int trackLabelWidth(TextRenderer tr) {
        int iconSpace = BUTTON_ICON + BUTTON_ICON_GAP;
        int ready = tr.getWidth(Text.translatable(KEY + "suspects.track")) + iconSpace;
        int countdown = tr.getWidth(Text.translatable(KEY + "suspects.track.seconds",
                TabletRules.secondsCeil(TabletRules.SUSPECT_TRACK_COOLDOWN_TICKS))) + iconSpace;
        int revealing = tr.getWidth(Text.translatable(KEY + "suspects.track.revealing",
                TabletRules.secondsCeil(TabletRules.SUSPECT_TRACK_REVEAL_TICKS)));
        return Math.max(ready, Math.max(countdown, revealing));
    }

    private record ButtonWidths(int track, int removal) {
    }
}
