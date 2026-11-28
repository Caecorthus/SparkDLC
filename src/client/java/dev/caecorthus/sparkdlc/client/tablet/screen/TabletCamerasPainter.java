package dev.caecorthus.sparkdlc.client.tablet.screen;

import dev.caecorthus.sparkdlc.client.role.civilian.seeker.remote.SeekerCctvRules;
import dev.caecorthus.sparkdlc.tablet.TabletLayout;
import dev.caecorthus.sparkdlc.tablet.net.TabletSnapshot;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

import static dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.BUTTON_ICON;
import static dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.BUTTON_ICON_GAP;
import static dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.CARD_R;
import static dev.caecorthus.sparkdlc.client.tablet.screen.TabletSectionPainter.KEY;

/**
 * Stateless painter for the camera section: one card per Seeker wall camera the viewer may watch. Each card's view
 * button is a widget placed by TabletCamerasPage at {@link #cameraButtonRect}.
 * 摄像头分区的无状态绘制器：查看者可以观看的每台搜寻者摄像头一张卡片。每张卡片的观看按钮是 TabletCamerasPage 按
 * cameraButtonRect 放置的控件。
 */
final class TabletCamerasPainter {
    static final int CAMERA_CARD_H = 40;
    static final int CAMERA_ROW_STEP = 46;

    private static final int CAMERA_TILE = 24;
    private static final int CAMERA_TILE_INSET = 8;
    private static final int CAMERA_GLYPH = 16;
    private static final int CAMERA_TEXT_GAP = 9;
    private static final int CAMERA_BUTTON_MIN_W = 72;
    private static final int CAMERA_BUTTON_H = 24;
    private static final int CAMERA_BUTTON_INSET = 8;
    private static final int CAMERA_BUTTON_TEXT_PAD = 22;
    private static final int CAMERA_CONTENT_GAP = 10;

    private TabletCamerasPainter() {
    }

    /**
     * One camera card: lens tile, then "CAM 01" over whose camera it is. The owner line names the Seeker only when the
     * list holds cameras of more than one ({@code showOwner}); a name the server withheld (police identities still
     * locked for this viewer) reads as that Seeker's number instead.
     * 一张摄像头卡片：镜头图块，右侧上行“摄像头 01”，下行说明是谁的摄像头。只有列表中包含不止一名搜寻者的摄像头时（showOwner）
     * 才在归属行写出搜寻者；服务端未下发的名字（该查看者尚未解锁义警身份）改为显示该搜寻者的序号。
     */
    static void cameraCard(TabletCanvas c, TextRenderer tr, int x, int y, int w, TabletSnapshot.CameraRow row,
                           boolean showOwner) {
        int h = CAMERA_CARD_H;
        if (w <= 0) {
            return;
        }
        int centerY = y + h / 2;
        c.roundRect(x, y, w, h, CARD_R, TabletTheme.SURFACE);
        int tone = TabletTheme.POLICE.base();
        int tileX = x + CAMERA_TILE_INSET;
        int tileY = centerY - CAMERA_TILE / 2;
        c.roundRect(tileX, tileY, CAMERA_TILE, CAMERA_TILE, 7f, TabletTheme.withAlpha(tone, 0x2E));
        TabletIcons.eye(c, tileX + (CAMERA_TILE - CAMERA_GLYPH) / 2f, tileY + (CAMERA_TILE - CAMERA_GLYPH) / 2f,
                CAMERA_GLYPH, tone);

        int textX = tileX + CAMERA_TILE + CAMERA_TEXT_GAP;
        int room = cameraButtonRect(tr, x, y, w).x() - CAMERA_CONTENT_GAP - textX;
        if (room <= 0) {
            return;
        }
        // Two lines centred as a block: name (8) + gap 6 + owner line (8). 两行整体居中：名称、间距、归属行。
        String name = Text.translatable(KEY + "cameras.name", SeekerCctvRules.cameraNumber(row.label())).getString();
        c.text(tr, TabletCanvas.trim(tr, name, room), textX, centerY - 11, TabletTheme.TEXT);
        c.text(tr, TabletCanvas.trim(tr, ownerLine(row, showOwner), room), textX, centerY + 3, TabletTheme.TEXT_3);
    }

    private static String ownerLine(TabletSnapshot.CameraRow row, boolean showOwner) {
        if (!showOwner) {
            return Text.translatable(KEY + "cameras.subtitle").getString();
        }
        return row.ownerName().isEmpty()
                ? Text.translatable(KEY + "cameras.owner_unknown", row.owner()).getString()
                : Text.translatable(KEY + "cameras.owner", row.ownerName()).getString();
    }

    /**
     * Where the screen must place a camera card's view TabletPressable (right-aligned, vertically centred). Its width
     * fits the wider of its two labels, identical on every row, so a pending request never shifts the layout.
     * 界面放置摄像头卡片“观看”按钮的位置（右对齐、垂直居中）。宽度取两种文字中较宽者，每行相同，请求未决时布局不动。
     */
    static TabletLayout.Rect cameraButtonRect(TextRenderer tr, int x, int y, int w) {
        int labelW = 0;
        for (String key : new String[]{"cameras.view", "cameras.connecting"}) {
            labelW = Math.max(labelW, tr.getWidth(Text.translatable(KEY + key)) + BUTTON_ICON + BUTTON_ICON_GAP);
        }
        int width = Math.max(CAMERA_BUTTON_MIN_W, labelW + CAMERA_BUTTON_TEXT_PAD);
        // Upper clamp never drops below the minimum, like droneButtonRect. 上限不低于最小宽度，与 droneButtonRect 相同。
        width = Math.min(width, Math.max(CAMERA_BUTTON_MIN_W, w / 3));
        width = Math.min(width, Math.max(0, w - 2 * CAMERA_BUTTON_INSET));
        return new TabletLayout.Rect(x + w - CAMERA_BUTTON_INSET - width, y + (CAMERA_CARD_H - CAMERA_BUTTON_H) / 2,
                width, CAMERA_BUTTON_H);
    }
}
