package dev.caecorthus.sparkdlc.client.hud.rolecard;

import static dev.caecorthus.sparkdlc.client.guidebook.ui.ExpressPalette.INK;
import static dev.caecorthus.sparkdlc.client.guidebook.ui.ExpressPalette.INK_MUTED;
import static dev.caecorthus.sparkdlc.client.guidebook.ui.ExpressPalette.INK_RULE;
import static dev.caecorthus.sparkdlc.client.guidebook.ui.ExpressPalette.INK_RULE_SOFT;
import static dev.caecorthus.sparkdlc.client.guidebook.ui.ExpressPalette.PAPER;

import dev.caecorthus.sparkdlc.client.config.SparkDlcClientSettings;
import dev.caecorthus.sparkdlc.client.gui.card.InventoryCardPaint;
import dev.caecorthus.sparkdlc.client.gui.card.InventoryInfoCard;
import dev.caecorthus.sparkdlc.client.guidebook.ui.ExpressPaint;
import dev.caecorthus.sparkdlc.client.text.HudFontOptions;
import dev.caecorthus.sparkdlc.client.text.RoleDisplayTexts;
import dev.caecorthus.sparkdlc.faction.api.FactionDefinition;
import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.guidebook.decor.DecorPalette;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.caecorthus.sparkdlc.util.RoleDisplayTextRules;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

/**
 * The round-start role card: a paper ticket that rises above the hotbar as Wathe's welcome reaches its goal line,
 * stays 10 s and sinks again ({@link RoleCardRules}). It shows the player's own synced role (read when the card
 * rises, so a role reassigned in between is the one shown): a stub in the role colour with its gem, a caption, the
 * role name, the effective faction as a chip (the same faction the trait-adjusted welcome goal speaks for) and the
 * one-line description {@code role.desc.*}. A role without a description gets no card. Display only, own player only;
 * drawn on a confirmed SparkDLC server and switched off by the {@code role_card} setting.
 * 开局身份卡：一张纸质车票，在 Wathe 欢迎词进行到目标行时从快捷栏上方升起，停留 10 秒后落下（{@link RoleCardRules}）。显示
 * 玩家自己已同步的职业（在卡片升起时读取，因此期间被重新分配的职业以新职业为准）：身份色票根与宝石、标题、职业名、作为徽记的
 * 有效阵营（与词条调整后的欢迎目标所指的阵营一致），以及一句话简介 {@code role.desc.*}。没有简介的职业不显示卡片。仅用于
 * 展示、仅本人可见；只在已确认的 SparkDLC 服务器上绘制，可由 {@code role_card} 设置关闭。
 */
public final class RoleCardHud {
    private static final String CAPTION_KEY = "hud.sparkdlc.role_card.caption";
    private static final int NAME_SCALE = 2;
    private static final int CHIP_HEIGHT = 11;
    private static final int CHIP_PAD = 4;

    /** What the card says, read once when it rises. / 卡片内容，升起时读取一次。 */
    private record Content(Identifier role, Text name, int roleColor, @Nullable Text faction, int factionColor,
                           Text description) {
    }

    /** Wrapped lines and the chosen card width for one content, screen width, language and font. / 换行结果及其缓存键。 */
    private record Layout(Content content, int screenWidth, Language language, TextRenderer renderer, int fontOptions,
                          int cardWidth, List<OrderedText> lines, boolean nameFitsLarge) {
    }

    // Render-thread caches, dropped by every welcome and every clear. / 渲染线程缓存，每次欢迎词与清除时丢弃。
    private static @Nullable Content content;
    private static boolean resolved;
    private static @Nullable Layout layout;

    private RoleCardHud() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register(RoleCardHud::render);
    }

    static void invalidate() {
        content = null;
        resolved = false;
        layout = null;
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        float reveal = RoleCardRules.reveal(RoleCardState.age(tickCounter.getTickDelta(false)));
        if (reveal < 0.0F || !SparkDlcServerConnection.isConfirmedServer()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return;
        }
        GameWorldComponent game = GameWorldComponent.KEY.get(client.world);
        GameMode mode = game.getGameMode();
        if (!RoleCardRules.shows(SparkDlcClientSettings.roleCard(), client.options.hudHidden,
                game.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE, game.getFade(),
                GameFunctions.isPlayerPlayingAndAlive(player),
                mode != WatheGameModes.LOOSE_ENDS && mode != WatheGameModes.DISCOVERY)) {
            return;
        }
        if (!resolved) {
            content = resolve(player, game);
            resolved = true;
        }
        Content shown = content;
        if (shown == null) {
            return;
        }
        int screenWidth = context.getScaledWindowWidth();
        if (RoleCardRules.textWidth(RoleCardRules.cardWidth(RoleCardRules.WIDTHS.get(0), screenWidth)) < 60) {
            return;
        }
        Layout laid = layout(shown, screenWidth, client.textRenderer);
        int width = laid.cardWidth();
        int height = RoleCardRules.cardHeight(laid.lines().size());
        int x = (context.getScaledWindowWidth() - width) / 2;
        int y = RoleCardRules.cardY(context.getScaledWindowHeight(), height, reveal);
        draw(context, client.textRenderer, laid, x, y, width, height);
    }

    private static @Nullable Content resolve(ClientPlayerEntity player, GameWorldComponent game) {
        Role role = game.getRole(player);
        if (role == null) {
            return null;
        }
        Language language = Language.getInstance();
        String descriptionKey = RoleCardRules.descriptionKey(role.identifier(), language::hasTranslation);
        if (descriptionKey == null) {
            return null;
        }
        Text name = RoleDisplayTexts.roleName(RoleDisplayTextRules.roleTranslationKey(role));
        Identifier factionId = SparkFactionApi.resolveEffectiveFaction(player, game);
        FactionDefinition faction = factionId == null ? null : SparkFactionApi.getFaction(factionId).orElse(null);
        Text factionText = faction == null ? null
                : Text.translatableWithFallback(faction.translationKeyPrefix(), factionId.getPath());
        int factionColor = faction == null ? role.color() : faction.color();
        return new Content(role.identifier(), name, role.color() & 0xFFFFFF, factionText, factionColor & 0xFFFFFF,
                Text.translatable(descriptionKey));
    }

    private static Layout layout(Content shown, int screenWidth, TextRenderer renderer) {
        Language language = Language.getInstance();
        int fontOptions = HudFontOptions.current();
        Layout cached = layout;
        if (cached != null && cached.content() == shown && cached.screenWidth() == screenWidth
                && cached.language() == language && cached.renderer() == renderer
                && cached.fontOptions() == fontOptions) {
            return cached;
        }
        // The narrowest width that keeps the description within the preferred lines, else the widest.
        // 取能让简介不超过建议行数的最窄宽度，否则取最宽的。
        int cardWidth = 0;
        List<OrderedText> wrapped = List.of();
        for (int candidate : RoleCardRules.WIDTHS) {
            int width = RoleCardRules.cardWidth(candidate, screenWidth);
            if (width == cardWidth) {
                break;
            }
            cardWidth = width;
            wrapped = InventoryInfoCard.wrapParagraph(renderer, shown.description(), RoleCardRules.textWidth(width));
            if (wrapped.size() <= RoleCardRules.PREFERRED_LINES) {
                break;
            }
        }
        int textWidth = RoleCardRules.textWidth(cardWidth);
        List<OrderedText> lines = new ArrayList<>(wrapped.subList(0, Math.min(wrapped.size(), RoleCardRules.MAX_LINES)));
        if (wrapped.size() > RoleCardRules.MAX_LINES) {
            // A description longer than the card ends in an ellipsis on its last line. / 超长简介在最后一行以省略号结束。
            String last = InventoryCardPaint.ellipsize(renderer, plain(wrapped.get(RoleCardRules.MAX_LINES - 1)) + "…",
                    textWidth);
            lines.set(RoleCardRules.MAX_LINES - 1, Text.literal(last).asOrderedText());
        }
        // The name has its own row under the caption and the chip, after the gem.
        // 名字独占标题与徽记下方的一行，位于宝石之后。
        boolean large = renderer.getWidth(shown.name()) * NAME_SCALE <= textWidth - 14;
        Layout laid = new Layout(shown, screenWidth, language, renderer, fontOptions, cardWidth, List.copyOf(lines),
                large);
        layout = laid;
        return laid;
    }

    private static void draw(DrawContext c, TextRenderer f, Layout laid, int x, int y, int w, int h) {
        Content shown = laid.content();
        int roleColour = 0xFF000000 | shown.roleColor();
        ExpressPaint.dropShadow(c, x, y, w, h);
        ExpressPaint.paper(c, x, y, w, h);
        // Stub: the role colour with a deeper edge (pale colours stay visible on the paper), a punched hole near the
        // top like a conductor's ticket, then the dashed tear line.
        // 票根：身份色加较深的边（浅色在纸面上也看得清），顶部有一个检票打孔，然后是虚线撕口。
        int stubRight = x + RoleCardRules.STUB_WIDTH;
        int stubEdge = DecorPalette.mix(roleColour, INK, 0.35);
        c.fill(x + 2, y + 2, stubRight, y + h - 2, roleColour);
        c.fill(x + 2, y + 2, x + 3, y + h - 2, DecorPalette.mix(roleColour, 0xFFFFFFFF, 0.35));
        c.fill(stubRight - 1, y + 2, stubRight, y + h - 2, stubEdge);
        int holeX = x + (RoleCardRules.STUB_WIDTH - 4) / 2 + 1;
        ExpressPaint.roundedFill(c, holeX, y + 7, 5, 5, stubEdge);
        c.fill(holeX + 1, y + 8, holeX + 4, y + 11, DecorPalette.mix(roleColour, INK, 0.6));
        for (int dash = y + 4; dash < y + h - 4; dash += 4) {
            c.fill(stubRight + 1, dash, stubRight + 2, Math.min(dash + 2, y + h - 4), INK_RULE);
        }

        int left = stubRight + RoleCardRules.BODY_LEFT_PAD;
        int right = x + w - RoleCardRules.BODY_RIGHT_PAD;
        c.drawText(f, Text.translatable(CAPTION_KEY), left, y + 7, INK_MUTED, false);
        if (shown.faction() != null) {
            drawChip(c, f, shown.faction(), shown.factionColor(), right, y + 5);
        }
        // The identity gem leads the name. / 身份宝石在名字前。
        int nameLeft = left + 14;
        if (laid.nameFitsLarge()) {
            ExpressPaint.gem2x(c, left, y + 22, shown.roleColor());
            c.getMatrices().push();
            c.getMatrices().translate(nameLeft, y + 19, 0.0F);
            c.getMatrices().scale(NAME_SCALE, NAME_SCALE, 1.0F); // exact 2x only / 仅允许精确 2 倍
            c.drawText(f, shown.name(), 0, 0, INK, false);
            c.getMatrices().pop();
        } else {
            ExpressPaint.gem(c, left + 2, y + 24, shown.roleColor(), true);
            c.drawText(f, InventoryCardPaint.ellipsize(f, shown.name().getString(), right - nameLeft), nameLeft, y + 23,
                    INK, false);
        }
        // Rule in the role colour over a soft ink line. / 身份色细线叠在淡墨线上。
        int ruleY = y + RoleCardRules.HEADER_HEIGHT - 5;
        c.fill(left, ruleY + 1, right, ruleY + 2, INK_RULE_SOFT);
        c.fill(left, ruleY, left + Math.min(36, right - left), ruleY + 1, stubEdge);
        int lineY = y + RoleCardRules.HEADER_HEIGHT;
        for (OrderedText line : laid.lines()) {
            c.drawText(f, line, left, lineY, INK, false);
            lineY += RoleCardRules.LINE_HEIGHT;
        }
    }

    private static int chipWidth(TextRenderer f, @Nullable Text label) {
        return label == null ? 0 : f.getWidth(label) + 2 * CHIP_PAD;
    }

    /** Faction chip, right-aligned: a wash of the faction colour, readable ink of the same hue. / 阵营徽记，右对齐。 */
    private static void drawChip(DrawContext c, TextRenderer f, Text label, int rgb, int right, int y) {
        int colour = 0xFF000000 | rgb;
        int w = chipWidth(f, label);
        int x = right - w;
        ExpressPaint.roundedFill(c, x, y, w, CHIP_HEIGHT, DecorPalette.mix(colour, PAPER, 0.78));
        ExpressPaint.roundedOutline(c, x, y, w, CHIP_HEIGHT, DecorPalette.mix(colour, INK, 0.3));
        c.drawText(f, label, x + CHIP_PAD, y + 2, DecorPalette.mix(colour, INK, 0.55), false);
    }

    private static String plain(OrderedText text) {
        StringBuilder out = new StringBuilder();
        text.accept((index, style, codePoint) -> {
            out.appendCodePoint(codePoint);
            return true;
        });
        return Objects.toString(out);
    }
}
