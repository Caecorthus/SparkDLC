package dev.caecorthus.sparkdlc.client.hud.rolecard;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Pure rules of the round-start role card ({@link RoleCardHud}): when it shows, how it moves, where it sits and which
 * description key it reads. No game state; the HUD passes everything in.
 * <p>
 * Timing follows Wathe's welcome ({@code RoundTextRenderer}, 280 ticks: the role line at 180, the premise at 120,
 * the goal at 60 ticks left). The card rises as the goal line appears, so it never covers the role line, holds for
 * 10 s and sinks back below the screen.
 * <p>
 * 开局身份卡（{@link RoleCardHud}）的纯规则：何时显示、如何移动、放在哪里、读取哪个简介键。不读游戏状态，由 HUD 传入。
 * 时间跟随 Wathe 的欢迎词（{@code RoundTextRenderer}，共 280 刻：剩 180 刻出现职业行、120 刻出现前提、60 刻出现目标）。
 * 卡片在目标行出现时升起，因此不会盖住职业行；停留 10 秒后沉回屏幕下方。
 */
public final class RoleCardRules {
    /** Wathe's welcome length and the tick its goal line appears at. / Wathe 欢迎词的时长与目标行出现的剩余刻。 */
    public static final int WATHE_WELCOME_TICKS = 280;
    public static final int WATHE_GOAL_LINE_TICKS_LEFT = 60;
    /** Ticks from the welcome to the card's rise. / 从欢迎词开始到卡片升起的刻数。 */
    public static final int SHOW_DELAY_TICKS = WATHE_WELCOME_TICKS - WATHE_GOAL_LINE_TICKS_LEFT;
    /** On screen for 10 s, the rise and the fall included. / 在屏幕上 10 秒，含升起与落下。 */
    public static final int VISIBLE_TICKS = 200;
    public static final int SLIDE_TICKS = 12;
    /** Ticks after the welcome when the card is gone for good. / 欢迎词之后卡片彻底消失的刻数。 */
    public static final int END_TICKS = SHOW_DELAY_TICKS + VISIBLE_TICKS;

    /** Lang prefix of the one-line descriptions (PR #5). / 一句话简介的语言键前缀（PR #5）。 */
    public static final String DESCRIPTION_PREFIX = "role.desc.";

    /**
     * Card widths tried in turn until the description fits {@link #PREFERRED_LINES} lines (long English lines need
     * the wider ones); each is capped by the screen.
     * 依次尝试的卡片宽度，直到简介不超过 {@link #PREFERRED_LINES} 行（较长的英文需要更宽的卡片）；每个宽度都受屏幕限制。
     */
    public static final List<Integer> WIDTHS = List.of(248, 288, 328);
    /** Phone-narrow GUI scales keep a 16 px gutter on each side. / 窄屏时两侧各留 16 像素。 */
    public static final int SIDE_GUTTER = 16;
    /** Clears the hotbar, the hearts and the experience bar. / 让开快捷栏、生命值与经验条。 */
    public static final int BOTTOM_CLEARANCE = 58;
    /** The coloured ticket stub on the left. / 左侧的身份色票根。 */
    public static final int STUB_WIDTH = 24;
    public static final int BODY_LEFT_PAD = 9;
    public static final int BODY_RIGHT_PAD = 8;
    public static final int HEADER_HEIGHT = 43;
    public static final int LINE_HEIGHT = 10;
    public static final int BOTTOM_PAD = 6;
    public static final int PREFERRED_LINES = 5;
    public static final int MAX_LINES = 7;

    private RoleCardRules() {
    }

    /**
     * How far the card is out, 0 (below the screen) to 1 (in place), or -1 when it is not drawn at all.
     * {@code age} counts ticks since the welcome, partial ticks included.
     * 卡片露出的程度：0（在屏幕下方）到 1（就位），不绘制时为 -1。{@code age} 为欢迎词之后的刻数（含不足一刻的部分）。
     */
    public static float reveal(float age) {
        float t = age - SHOW_DELAY_TICKS;
        if (t < 0.0F || t > VISIBLE_TICKS) {
            return -1.0F;
        }
        if (t < SLIDE_TICKS) {
            return easeOut(t / SLIDE_TICKS);
        }
        if (t > VISIBLE_TICKS - SLIDE_TICKS) {
            return easeOut((VISIBLE_TICKS - t) / SLIDE_TICKS);
        }
        return 1.0F;
    }

    /** Cubic ease-out, clamped. / 三次缓出，夹在 0..1。 */
    public static float easeOut(float x) {
        float clamped = Math.max(0.0F, Math.min(1.0F, x));
        float inverse = 1.0F - clamped;
        return 1.0F - inverse * inverse * inverse;
    }

    /**
     * Drawn this frame: the setting on, F1 off, a running round past its fade, the player alive in it, and a mode
     * with a per-role welcome (not Loose Ends, where everyone holds the same role, nor Discovery).
     * 本帧是否绘制：设置开启、未按 F1、对局进行中且已过渡完毕、玩家在局内存活，并且是按职业欢迎的模式（不是人人同一身份的
     * 「残局」，也不是「探索」）。
     */
    public static boolean shows(boolean enabled, boolean hudHidden, boolean gameActive, int fade,
                                boolean playingAndAlive, boolean perRoleMode) {
        return enabled && !hudHidden && gameActive && fade <= 0 && playingAndAlive && perRoleMode;
    }

    /**
     * The description key for a role: {@code role.desc.<path>}, else {@code role.desc.<namespace>.<path>}, else
     * {@code null} (no card for a role without a description).
     * 职业的简介键：先 {@code role.desc.<path>}，再 {@code role.desc.<namespace>.<path>}，都没有时为 {@code null}（没有
     * 简介的职业不显示卡片）。
     */
    public static @Nullable String descriptionKey(Identifier role, Predicate<String> hasTranslation) {
        for (String key : List.of(DESCRIPTION_PREFIX + role.getPath(),
                DESCRIPTION_PREFIX + role.getNamespace() + "." + role.getPath())) {
            if (hasTranslation.test(key)) {
                return key;
            }
        }
        return null;
    }

    /** One candidate width on this screen. / 本屏幕上的一个候选宽度。 */
    public static int cardWidth(int candidate, int screenWidth) {
        return Math.max(0, Math.min(candidate, screenWidth - 2 * SIDE_GUTTER));
    }

    /** Width of the wrapped description. / 简介换行的宽度。 */
    public static int textWidth(int cardWidth) {
        return cardWidth - STUB_WIDTH - BODY_LEFT_PAD - BODY_RIGHT_PAD;
    }

    public static int cardHeight(int lines) {
        return HEADER_HEIGHT + Math.max(1, Math.min(MAX_LINES, lines)) * LINE_HEIGHT + BOTTOM_PAD;
    }

    /** Top of the card at {@code reveal}: from just below the screen up to its place above the hotbar. / 卡片顶边。 */
    public static int cardY(int screenHeight, int cardHeight, float reveal) {
        int rest = screenHeight - BOTTOM_CLEARANCE - cardHeight;
        int hidden = screenHeight + 2;
        return Math.round(hidden + (rest - hidden) * Math.max(0.0F, Math.min(1.0F, reveal)));
    }
}
