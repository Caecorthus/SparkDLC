package dev.caecorthus.sparkdlc.client.round.dualselect;

import dev.caecorthus.sparkdlc.client.round.dualselect.book.BookPaint;
import dev.caecorthus.sparkdlc.client.round.dualselect.book.BookPalette;
import dev.caecorthus.sparkdlc.client.round.dualselect.book.BookRect;
import dev.caecorthus.sparkdlc.round.dualselect.DualRoleChoiceC2SPayload;
import dev.caecorthus.sparkdlc.round.dualselect.DualRoleSelectionComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * 双选模式的二选一屏（书页风格）。
 * <p>
 * Two modes, matching the original book UI:
 * <ul>
 *   <li>{@code canChoose = true}: two role cards; clicking one sends the choice but keeps the screen open until the
 *       shared countdown ends, so nobody can move before everyone is done.</li>
 *   <li>{@code canChoose = false}: a single-sheet notice telling the player which role they already hold.</li>
 * </ul>
 * 布局、配色、宝石与标题栏全部照搬书页风格的原实现。
 */
public class DualRoleSelectionScreen extends Screen {

    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_MARGIN = 40;
    private static final int BAND_HEIGHT = 23;
    private static final int PAD = 8;
    private static final int ROW_GAP = 4;
    private static final int ROW_PAD = 5;
    private static final int LINE_HEIGHT = 12;
    private static final int TEXT_INDENT = 19;

    private final DualRoleSelectionComponent component;
    private final Identifier firstRoleId;
    private final Identifier secondRoleId;
    private final boolean canChoose;
    private final long endTimeMs;

    private boolean choiceMade;
    private boolean choseSecond;

    public DualRoleSelectionScreen(DualRoleSelectionComponent component) {
        super(Text.translatable("screen.sparkdlc.dual_role.title"));
        this.component = component;
        List<Identifier> local = component.getLocalOptions();
        this.firstRoleId = local.isEmpty() ? null : local.get(0);
        this.secondRoleId = local.size() < 2 ? null : local.get(1);
        this.canChoose = this.secondRoleId != null;
        this.endTimeMs = System.currentTimeMillis() + Math.max(0, component.getTicksRemaining()) * 50L;
    }

    public static boolean isOpen() {
        Screen screen = net.minecraft.client.MinecraftClient.getInstance().currentScreen;
        return screen instanceof DualRoleSelectionScreen;
    }

    /** 点击选择：发送数据包但不关闭界面，等倒计时结束统一关闭。 */
    private void selectRole(boolean choseSecond) {
        if (choiceMade) {
            return;
        }
        this.choiceMade = true;
        this.choseSecond = choseSecond;
        ClientPlayNetworking.send(new DualRoleChoiceC2SPayload(choseSecond ? 1 : 0));
    }

    private boolean countdownDone() {
        return System.currentTimeMillis() >= endTimeMs;
    }

    private int countdownSeconds() {
        return (int) Math.ceil(Math.max(0, endTimeMs - System.currentTimeMillis()) / 1000.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!countdownDone()) {
            return;
        }
        if (canChoose && !choiceMade) {
            // 超时未选：默认保留第一身份
            selectRole(false);
        }
        forceClose();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 双选期间保持全黑，选完之后由服务端重放黑屏渐亮
        context.fill(0, 0, this.width, this.height, 0xFF000000);
        boolean choosing = canChoose && !choiceMade && countdownSeconds() > 3;
        if (choosing) {
            renderChoose(context, mouseX, mouseY);
        } else {
            renderHold(context);
        }
    }

    // ==================== 选择模式 ====================

    private void renderChoose(DrawContext context, int mouseX, int mouseY) {
        TextRenderer font = this.textRenderer;
        OptionEntry first = buildEntry(firstRoleId, false);
        OptionEntry second = buildEntry(secondRoleId, true);
        ChooseLayout layout = chooseLayout(first, second);

        BookRect panel = layout.panel();
        BookPaint.panel(context, panel.x(), panel.y(), panel.width(), panel.height());
        BookPaint.titleBand(context, panel.x(), panel.y(), panel.width());
        BookPaint.gem(context, panel.x() + 7, panel.y() + 9, 0xFFBF49, false);
        context.drawText(font, this.title, panel.x() + 15, panel.y() + 7, BookPalette.TITLE, false);

        boolean firstHovered = !choiceMade && layout.firstRect().contains(mouseX, mouseY);
        boolean secondHovered = !choiceMade && layout.secondRect().contains(mouseX, mouseY);
        renderOptionRow(context, first, layout.firstRect(), firstHovered, mouseX, mouseY);
        renderOptionRow(context, second, layout.secondRect(), secondHovered, mouseX, mouseY);

        Text status = choiceMade
                ? Text.translatable("screen.sparkdlc.dual_role.chosen")
                : Text.translatable("screen.sparkdlc.dual_role.countdown", countdownSeconds());
        context.drawCenteredTextWithShadow(font, status, panel.x() + panel.width() / 2,
                layout.secondRect().bottom() + 6, BookPalette.COIN);
    }

    private void renderOptionRow(DrawContext context, OptionEntry entry, BookRect rect, boolean hovered,
                                 int mouseX, int mouseY) {
        TextRenderer font = this.textRenderer;
        boolean selected = choiceMade && entry.second() == choseSecond;
        boolean dimmed = choiceMade && !selected;
        BookPaint.rowState(context, rect.x(), rect.y(), rect.right(), rect.height(), hovered, selected);
        BookPaint.gem(context, rect.x() + 10, rect.y() + ROW_PAD, entry.gemColor(), hovered || selected);

        Text prefix = Text.translatable(entry.second()
                ? "screen.sparkdlc.dual_role.switch_prefix"
                : "screen.sparkdlc.dual_role.keep_prefix");
        int nameX = rect.x() + TEXT_INDENT;
        int prefixColor = dimmed ? BookPalette.FAINT : BookPalette.MUTED;
        int nameColor = dimmed ? BookPalette.FAINT : (hovered || selected ? BookPalette.TITLE : BookPalette.TEXT_HI);
        context.drawText(font, prefix, nameX, rect.y() + ROW_PAD, prefixColor, true);
        int tagX = nameX + font.getWidth(prefix);
        Text separator = Text.literal(" · ");
        int sepColor = dimmed ? BookPalette.FAINT : BookPalette.INK_FAINT;
        context.drawText(font, separator, tagX, rect.y() + ROW_PAD, sepColor, true);
        tagX += font.getWidth(separator);
        Text tag = entry.tag();
        if (!tag.getString().isEmpty()) {
            int tagColor = dimmed ? BookPalette.FAINT : BookPalette.COIN;
            context.drawText(font, tag, tagX, rect.y() + ROW_PAD, tagColor, true);
            tagX += font.getWidth(tag);
            context.drawText(font, separator, tagX, rect.y() + ROW_PAD, sepColor, true);
            tagX += font.getWidth(separator);
        }
        context.drawText(font, entry.name(), tagX, rect.y() + ROW_PAD, nameColor, true);

        Text hint = Text.translatable(entry.second()
                ? "screen.sparkdlc.dual_role.switch_hint"
                : "screen.sparkdlc.dual_role.keep_hint");
        int hintColor = dimmed ? BookPalette.FAINT : BookPalette.MUTED;
        context.drawText(font, BookPaint.ellipsize(font, hint.getString(), rect.width() - TEXT_INDENT - 8),
                nameX, rect.y() + ROW_PAD + LINE_HEIGHT, hintColor, true);

        // 悬停时把该身份的目标摊出来。 / Show the role objective while hovering.
        Text goals = entry.goals();
        if (hovered && goals != null) {
            context.drawTooltip(font, goals, mouseX, mouseY);
        }
    }

    private OptionEntry buildEntry(Identifier roleId, boolean second) {
        Role role = findRole(roleId);
        int gemColor = role == null ? BookPalette.BRASS_HI : (role.color() & 0xFFFFFF);
        return new OptionEntry(roleName(roleId), second, gemColor, factionTag(role), goals(roleId));
    }

    private static Role findRole(Identifier id) {
        if (id == null) {
            return null;
        }
        for (Role role : WatheRoles.ROLES) {
            if (role.identifier().equals(id)) {
                return role;
            }
        }
        return null;
    }

    private static Text roleName(Identifier id) {
        if (id == null) {
            return Text.empty();
        }
        // 各模组都把身份名注册在 announcement.role.<path>；再退回带命名空间的旧键。
        // Every add-on registers its display name under announcement.role.<path>; older keys stay as fallbacks.
        Text announcement = Text.translatable("announcement.role." + id.getPath());
        if (!announcement.getString().startsWith("announcement.role.")) {
            return announcement;
        }
        Text full = Text.translatable("role." + id.getNamespace() + "." + id.getPath());
        if (!full.getString().startsWith("role.")) {
            return full;
        }
        return Text.translatable("role." + id.getPath());
    }

    /** 阵营徽标，例如「【乘客 · 警职】」。 / Faction badge such as "【乘客 · 警职】". */
    private static Text factionTag(Role role) {
        if (role == null) {
            return Text.empty();
        }
        if (dev.caecorthus.sparkdlc.faction.api.PoliceRoles.contains(role)) {
            return Text.translatable("tag.sparkdlc.dual_role.police");
        }
        Identifier faction = dev.caecorthus.sparkdlc.faction.api.SparkFactionApi.resolveEffectiveFaction(role);
        if (dev.caecorthus.sparkdlc.SparkDlcFactions.JIAHAO.equals(faction)) {
            return Text.translatable("tag.sparkdlc.dual_role.jiahao");
        }
        if (dev.caecorthus.sparkdlc.SparkDlcFactions.WITCH.equals(faction)
                || dev.caecorthus.sparkdlc.SparkDlcFactions.MURDEROUS_WITCH.equals(faction)) {
            return Text.translatable("tag.sparkdlc.dual_role.witch");
        }
        if (role.isInnocent()) {
            return Text.translatable("tag.sparkdlc.dual_role.civilian");
        }
        if (role.getFaction() == dev.doctor4t.wathe.api.Faction.KILLER) {
            return Text.translatable("tag.sparkdlc.dual_role.killer");
        }
        return Text.translatable("tag.sparkdlc.dual_role.neutral");
    }

    /** 身份目标文本，缺这个键时返回 null。 / Role objective text, or null when the key is missing. */
    private static Text goals(Identifier id) {
        if (id == null) {
            return null;
        }
        Text key = Text.translatable("announcement.goals." + id.getPath());
        if (key.getString().startsWith("announcement.goals.")) {
            return null;
        }
        return key;
    }

    /** 告示页该显示哪个身份：选过「换成」就显示第二个。 / Which role the sheet announces. */
    private Identifier shownRoleId() {
        return choseSecond && secondRoleId != null ? secondRoleId : firstRoleId;
    }

    private ChooseLayout chooseLayout(OptionEntry first, OptionEntry second) {
        int panelWidth = panelWidth();
        int rowWidth = panelWidth - PAD * 2;
        int panelHeight = BAND_HEIGHT + 6 + rowHeight() + ROW_GAP + rowHeight() + 6 + LINE_HEIGHT + 6;
        BookRect panel = centeredPanel(panelWidth, panelHeight);
        int rowY = panel.y() + BAND_HEIGHT + 6;
        BookRect firstRect = new BookRect(panel.x() + PAD, rowY, rowWidth, rowHeight());
        rowY += rowHeight() + ROW_GAP;
        BookRect secondRect = new BookRect(panel.x() + PAD, rowY, rowWidth, rowHeight());
        return new ChooseLayout(panel, firstRect, secondRect);
    }

    private static int rowHeight() {
        return ROW_PAD * 2 + LINE_HEIGHT * 2;
    }

    // ==================== 宣告模式 ====================

    private void renderHold(DrawContext context) {
        TextRenderer font = this.textRenderer;
        Identifier finalId = shownRoleId();
        Text roleName = roleName(finalId);
        Role role = findRole(finalId);
        int gemColor = role == null ? BookPalette.BRASS_HI : (role.color() & 0xFFFFFF);

        int panelWidth = panelWidth();
        Text message = Text.translatable("screen.sparkdlc.dual_role.hold", roleName);
        List<net.minecraft.text.OrderedText> messageLines = font.wrapLines(message, panelWidth - PAD * 2 - 24);
        int sheetHeight = 42 + messageLines.size() * LINE_HEIGHT + LINE_HEIGHT * 3;
        BookRect panel = centeredPanel(panelWidth, BAND_HEIGHT + 5 + sheetHeight + 5);

        BookPaint.panel(context, panel.x(), panel.y(), panel.width(), panel.height());
        BookPaint.titleBand(context, panel.x(), panel.y(), panel.width());
        BookPaint.gem(context, panel.x() + 7, panel.y() + 9, gemColor, false);
        context.drawText(font, roleName, panel.x() + 15, panel.y() + 7, BookPalette.HEADING, false);

        BookRect sheet = new BookRect(panel.x() + 4, panel.y() + 22, panel.width() - 8, panel.height() - 26);
        BookPaint.paper(context, sheet.x(), sheet.y(), sheet.width(), sheet.height());

        int centerX = panel.x() + panel.width() / 2;
        int textY = sheet.y() + 12;
        for (net.minecraft.text.OrderedText line : messageLines) {
            context.drawText(font, line, centerX - font.getWidth(line) / 2, textY, BookPalette.INK, false);
            textY += LINE_HEIGHT;
        }
        // 职业功能简介：优先 announcement.goals.<path>，其次带命名空间的键。
        // Role summary lives under announcement.goals.<path>; fall back to the namespaced key.
        if (finalId != null) {
            Text goals = Text.translatable("role.desc." + finalId.getPath());
            if (goals.getString().startsWith("role.desc.")) {
                goals = Text.translatable("role.desc." + finalId.getNamespace() + "." + finalId.getPath());
            }
            if (!goals.getString().startsWith("role.desc.")) {
                textY += 6;
                for (net.minecraft.text.OrderedText line : font.wrapLines(goals, panelWidth - PAD * 2 - 24)) {
                    context.drawText(font, line, centerX - font.getWidth(line) / 2, textY, BookPalette.INK_MUTED, false);
                    textY += LINE_HEIGHT;
                }
            }
        }
        Text countdown = Text.translatable("screen.sparkdlc.dual_role.hold_countdown", countdownSeconds());
        context.drawText(font, countdown, centerX - font.getWidth(countdown) / 2, textY + 8, BookPalette.INK_FAINT, false);
    }

    // ==================== 布局 ====================

    private int panelWidth() {
        return Math.max(180, Math.min(PANEL_WIDTH, this.width - PANEL_MARGIN));
    }

    private BookRect centeredPanel(int panelWidth, int panelHeight) {
        int x = (this.width - panelWidth) / 2;
        int y = Math.max(4, (this.height - panelHeight) / 2);
        return new BookRect(x, y, panelWidth, panelHeight);
    }

    // ==================== 输入 ====================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && canChoose && !choiceMade) {
            ChooseLayout layout = chooseLayout(buildEntry(firstRoleId, false), buildEntry(secondRoleId, true));
            if (layout.firstRect().contains(mouseX, mouseY)) {
                selectRole(false);
                return true;
            }
            if (layout.secondRect().contains(mouseX, mouseY)) {
                selectRole(true);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        // 倒计时结束前禁止关闭（含 ESC），防止有人提前行动
        if (!countdownDone()) {
            return;
        }
        forceClose();
    }

    private void forceClose() {
        if (this.client != null) {
            this.client.setScreen(null);
        }
    }

    private record OptionEntry(Text name, boolean second, int gemColor, Text tag, Text goals) {
    }

    private record ChooseLayout(BookRect panel, BookRect firstRect, BookRect secondRect) {
    }
}
