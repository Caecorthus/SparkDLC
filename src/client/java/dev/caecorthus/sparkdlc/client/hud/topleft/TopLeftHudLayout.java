package dev.caecorthus.sparkdlc.client.hud.topleft;

import dev.caecorthus.sparkdlc.client.health.BodyHealthHudRules;

/**
 * Every coordinate of the top-left corner of the in-round HUD, as pure numbers. Wathe 1.5.6 {@code MoodRenderer} stays
 * the driver of that corner; this class says where each of its parts ends up, and the mixins in
 * {@code client.mixin.health} only add the difference to Wathe's own coordinates.
 * <p>Wathe's original block ({@link #WATHE}, kept pixel-identical on a server that is not a confirmed SparkDLC one):
 * the 14x17 mood flag at (5, 6) sliding down by {@code 3 * moodOffset}, with the mood arrows drawn over it; the task
 * rows as text at (22, {@code 6 + 10 * offset}); under the last row the 1 px mood bar at
 * (22, {@code 17 + 10 * moodOffset}) and, while the mood is below zero, the shaking breakdown warning three pixels
 * below the bar; in Psycho mode instead a scrolling banner clipped to x 22..180, y 6..23 with its timer bar at
 * (26, 17) and the flag trail at (5, 6).
 * <p>The two re-laid layouts (confirmed SparkDLC server only) share one structure and differ only in x. One column
 * holds, top to bottom: the flag at a fixed spot (no slide) with the mood as a signed percentage right of it; the
 * header line directly under the flag row; the task rows, {@link #ROW_PITCH} apart; the mood bar is gone, and the
 * breakdown warning takes the line under the last row. In {@link #COMPACT} (no figure) that column is the left edge:
 * the flag stays at Wathe's (5, 6), header and rows start at the left margin. In {@link #WITH_FIGURE} the standing
 * body health figure owns the left edge (its rectangle is read from {@link BodyHealthHudRules}) and the whole column
 * sits {@link #FIGURE_GAP} px right of it, so the task list is beside the figure, never under it.
 * <p>Pure: no Minecraft type, no state. {@link TopLeftHud} picks the layout of the frame.
 * <p>对局 HUD 左上角的全部坐标，均为纯数值。该角落仍由 Wathe 1.5.6 {@code MoodRenderer} 驱动；本类说明其各部分最终的位置，
 * {@code client.mixin.health} 中的 mixin 只在 Wathe 自身坐标上加上差值。
 * <p>Wathe 的原始区块（{@link #WATHE}，在未确认为 SparkDLC 的服务器上保持逐像素一致）：14x17 的心情旗帜位于 (5, 6)，随
 * {@code 3 * moodOffset} 下移，心情箭头叠画在旗帜上；任务行是位于 (22, {@code 6 + 10 * offset}) 的文本；最后一行下方是
 * 1 像素高的理智条 (22, {@code 17 + 10 * moodOffset})，理智低于零时其下方 3 像素处还有抖动的崩溃警告；疯魔模式下则改为
 * 裁剪在 x 22..180、y 6..23 内的滚动横幅，计时条位于 (26, 17)，旗帜残影位于 (5, 6)。
 * <p>两种重新排布的布局（仅限已确认的 SparkDLC 服务器）结构相同，只在 x 上不同。同一列自上而下依次为：位置固定（不再
 * 下移）的旗帜及其右侧带符号的理智百分比；紧贴旗帜行下方的标题行；间隔 {@link #ROW_PITCH} 的任务行；理智条取消，崩溃警告
 * 占用最后一行下方的那一行。{@link #COMPACT}（无示意图）中该列即左缘：旗帜留在 Wathe 的 (5, 6)，标题与任务行从左边距开始。
 * {@link #WITH_FIGURE} 中站立的身体健康示意图占据左缘（其矩形读取自 {@link BodyHealthHudRules}），整列位于其右侧
 * {@link #FIGURE_GAP} 像素处，因此任务清单在示意图旁边，而不是在其下方。
 * <p>纯函数：不含 Minecraft 类型，也没有状态。本帧使用哪种布局由 {@link TopLeftHud} 决定。
 */
public final class TopLeftHudLayout {
    /** Wathe's flag origin and footprint. / Wathe 旗帜的原点与尺寸。 */
    public static final int WATHE_FLAG_X = 5;
    public static final int WATHE_FLAG_Y = 6;
    public static final int FLAG_WIDTH = 14;
    public static final int FLAG_HEIGHT = 17;
    /** Wathe's task text column and first row. / Wathe 任务文本所在列与首行。 */
    public static final int WATHE_TASK_X = 22;
    public static final int WATHE_FIRST_TASK_Y = 6;
    /** Wathe slides the flag down by this much per row of {@code moodOffset}. / Wathe 的旗帜随 {@code moodOffset} 每行下移的像素数。 */
    public static final float WATHE_FLAG_SLIDE = 3.0F;
    /** Rows are this far apart, in Wathe and in the re-laid layouts. / 行距，Wathe 与重排布局相同。 */
    public static final int ROW_PITCH = 10;
    /** Wathe has four task types, so at most four rows. / Wathe 共有四种任务，因此最多四行。 */
    public static final int MAX_TASK_ROWS = 4;
    /** Vanilla text height; Wathe's bar sits at {@code 8 + fontHeight}. / 原版文字高度；Wathe 的理智条位于 {@code 8 + fontHeight}。 */
    public static final int FONT_HEIGHT = 9;
    /** Height of a digit glyph, used to centre the percentage on the flag. / 数字字形高度，用于让百分比相对旗帜垂直居中。 */
    public static final int DIGIT_HEIGHT = 7;
    /** Left margin of the header and the task rows without the figure. / 无示意图时标题行与任务行的左边距。 */
    public static final int LEFT_MARGIN = 6;
    /** Gap between the flag and the percentage. / 旗帜与百分比之间的间距。 */
    public static final int PERCENT_GAP = 4;
    /** Gap between the figure and the column right of it. / 示意图与其右侧那一列之间的间距。 */
    public static final int FIGURE_GAP = 5;
    /** Gap between the flag row and the header. / 旗帜行与标题行之间的间距。 */
    public static final int FLAG_ROW_GAP = 3;
    /** Gap a line below the block keeps from Wathe's original block. / 区块下方的行与 Wathe 原始区块之间的间距。 */
    public static final int WATHE_BELOW_GAP = 3;
    /** Wathe shakes the flag and the warning by {@code |mood| * 3} px below zero. / 理智低于零时 Wathe 以 {@code |mood| * 3} 像素抖动旗帜与警告。 */
    public static final float SHAKE_SCALE = 3.0F;
    /**
     * Bands of the displayed mood fraction, the numbers of Wathe's flag thresholds: below the first is depressive,
     * below the second is mid.
     * 显示用理智比例的分档，数值取自 Wathe 的旗帜阈值：低于前者为抑郁，低于后者为一般。
     */
    public static final float MOOD_DEPRESSIVE_BELOW = 0.2F;
    public static final float MOOD_MID_BELOW = 0.55F;
    /** Percentage colours by those three bands (RGB); any negative value is depressive. / 百分比按这三档取色（RGB）；负值一律为抑郁色。 */
    public static final int PERCENT_HAPPY_RGB = 0x55FF55;
    public static final int PERCENT_MID_RGB = 0xFFFF55;
    public static final int PERCENT_DEPRESSIVE_RGB = 0xFF5555;
    /** Header colour (RGB): dimmer than the white rows under it. / 标题颜色（RGB）：比其下方的白色任务行更暗。 */
    public static final int HEADER_RGB = 0xAAAAAA;

    private static final int WATHE_BAR_HEIGHT = 1;
    private static final int WATHE_WARNING_GAP = 3;

    /** Wathe's own block, untouched. / Wathe 自身的区块，不做任何改动。 */
    public static final TopLeftHudLayout WATHE = new TopLeftHudLayout(
            false, false, WATHE_FLAG_X, WATHE_FLAG_Y, WATHE_FLAG_Y, WATHE_TASK_X, WATHE_FIRST_TASK_Y, WATHE_FLAG_X, 0, 0);
    /** Re-laid, health figure hidden: the column is the left edge. / 重排，健康示意图未显示：该列即左缘。 */
    public static final TopLeftHudLayout COMPACT = relaid(false, WATHE_FLAG_X, LEFT_MARGIN, LEFT_MARGIN, 0, 0);
    /** Re-laid, the column right of the health figure. / 重排，该列位于健康示意图右侧。 */
    public static final TopLeftHudLayout WITH_FIGURE = relaid(
            true,
            BodyHealthHudRules.FIGURE_X + BodyHealthHudRules.FIGURE_WIDTH + FIGURE_GAP,
            BodyHealthHudRules.FIGURE_X + BodyHealthHudRules.FIGURE_WIDTH + FIGURE_GAP,
            BodyHealthHudRules.FIGURE_X,
            BodyHealthHudRules.FIGURE_X + BodyHealthHudRules.FIGURE_WIDTH,
            BodyHealthHudRules.FIGURE_Y + BodyHealthHudRules.FIGURE_HEIGHT);

    private final boolean relaid;
    private final boolean hasFigure;
    private final int flagX;
    private final int flagY;
    private final int headerY;
    private final int taskX;
    private final int firstTaskY;
    private final int leftX;
    private final int figureRight;
    private final int figureBottom;

    private TopLeftHudLayout(boolean relaid, boolean hasFigure, int flagX, int flagY, int headerY, int taskX,
                             int firstTaskY, int leftX, int figureRight, int figureBottom) {
        this.relaid = relaid;
        this.hasFigure = hasFigure;
        this.flagX = flagX;
        this.flagY = flagY;
        this.headerY = headerY;
        this.taskX = taskX;
        this.firstTaskY = firstTaskY;
        this.leftX = leftX;
        this.figureRight = figureRight;
        this.figureBottom = figureBottom;
    }

    /** Both re-laid layouts share every y: Wathe's flag row, the header under it, the rows under the header. / 两种重排布局的 y 完全相同：Wathe 的旗帜行、其下的标题行、标题下的任务行。 */
    private static TopLeftHudLayout relaid(boolean hasFigure, int flagX, int taskX, int leftX, int figureRight,
                                           int figureBottom) {
        int headerY = WATHE_FLAG_Y + FLAG_HEIGHT + FLAG_ROW_GAP;
        return new TopLeftHudLayout(true, hasFigure, flagX, WATHE_FLAG_Y, headerY, taskX, headerY + ROW_PITCH, leftX,
                figureRight, figureBottom);
    }

    /**
     * The layout of a frame: Wathe's own unless the server is a confirmed SparkDLC one, then by the figure.
     * 某一帧的布局：只有已确认的 SparkDLC 服务器才重排，再按示意图是否显示区分。
     */
    public static TopLeftHudLayout select(boolean confirmedServer, boolean figureVisible) {
        if (!confirmedServer) {
            return WATHE;
        }
        return figureVisible ? WITH_FIGURE : COMPACT;
    }

    /** False only for {@link #WATHE}. / 仅 {@link #WATHE} 为 false。 */
    public boolean relaid() {
        return relaid;
    }

    public boolean hasFigure() {
        return hasFigure;
    }

    /**
     * Right edge (exclusive) and bottom edge (exclusive) of the health figure's rectangle, 0 without the figure.
     * 健康示意图矩形的右缘与下缘（均不含），无示意图时为 0。
     */
    public int figureRight() {
        return figureRight;
    }

    public int figureBottom() {
        return figureBottom;
    }

    /**
     * Left edge of everything the corner draws: Wathe's flag, the left margin, or the figure. A line a feature pins
     * under the whole corner starts here.
     * 该角落所绘全部内容的左缘：Wathe 的旗帜、左边距或示意图。某功能固定在整个角落下方的行从这里开始。
     */
    public int leftX() {
        return leftX;
    }

    /** Flag origin (top-left of the 14x17 sprite), before Wathe's slide and shake. / 旗帜原点（14x17 sprite 的左上角），不含 Wathe 的下移与抖动。 */
    public int flagX() {
        return flagX;
    }

    public int flagY() {
        return flagY;
    }

    /** Offset of the flag from Wathe's (5, 6). / 旗帜相对 Wathe (5, 6) 的偏移。 */
    public int flagDx() {
        return flagX - WATHE_FLAG_X;
    }

    public int flagDy() {
        return flagY - WATHE_FLAG_Y;
    }

    /**
     * The y translation of the flag block that replaces Wathe's {@code 3 * moodOffset} slide: Wathe's own slide in
     * {@link #WATHE}; in a re-laid layout the flag has a fixed spot in the first row, so only the offset remains.
     * 取代 Wathe {@code 3 * moodOffset} 下移量的旗帜区块 y 平移：{@link #WATHE} 中即 Wathe 自身的下移；重排布局中旗帜在
     * 第一行位置固定，因此只剩偏移量。
     */
    public float flagTranslateY(float watheSlide) {
        return relaid ? flagDy() : watheSlide;
    }

    /** Percentage text origin, right of the flag and vertically centred on it. / 百分比文本原点：旗帜右侧并相对旗帜垂直居中。 */
    public int percentX() {
        return flagX + percentDx();
    }

    public int percentY() {
        return flagY + percentDy();
    }

    /** The same origin relative to the flag origin. / 同一原点相对旗帜原点的偏移。 */
    public static int percentDx() {
        return FLAG_WIDTH + PERCENT_GAP;
    }

    public static int percentDy() {
        return (FLAG_HEIGHT - DIGIT_HEIGHT) / 2;
    }

    /** Header origin, in the task column; only the re-laid layouts draw a header. / 标题行原点，位于任务列；只有重排布局绘制标题。 */
    public int headerX() {
        return taskX;
    }

    public int headerY() {
        return headerY;
    }

    /** Task text column and the y of row 0. / 任务文本所在列与第 0 行的 y。 */
    public int taskX() {
        return taskX;
    }

    public int firstTaskY() {
        return firstTaskY;
    }

    /** Offset of the task rows from Wathe's (22, 6). / 任务行相对 Wathe (22, 6) 的偏移。 */
    public int taskDx() {
        return taskX - WATHE_TASK_X;
    }

    public int taskDy() {
        return firstTaskY - WATHE_FIRST_TASK_Y;
    }

    /**
     * Top of the area the task rows may draw in. Wathe slides a new row in from row offset -1, which in its own
     * layout is the top edge of the screen; in a re-laid layout that spot is the header's line, so the rows are
     * clipped right under the header and a new row slides out from beneath it.
     * 任务行可绘制区域的顶部。Wathe 让新任务行从行偏移 -1 处滑入，在其自身布局中那里是屏幕上缘；重排布局中那里是标题所在行，
     * 因此任务行在标题正下方被裁剪，新任务行从标题下方滑出。
     */
    public int rowClipTop() {
        return firstTaskY - 1;
    }

    /** Top y of the row at Wathe's animated row offset. / 位于 Wathe 动画行偏移处的任务行顶部 y。 */
    public float rowY(float rowOffset) {
        return firstTaskY + ROW_PITCH * rowOffset;
    }

    /**
     * Top y of the line directly under the task rows, where the breakdown warning is drawn. {@code moodOffset} is
     * Wathe's smoothed offset of the last row and {@code rowPresence} the largest alpha among the rows drawn this
     * frame (0 without rows). Re-laid: one pitch under the last row, and with the rows fading out the line glides up
     * to the header's line, which is free then (the header is drawn only with rows). {@link #WATHE}: the warning's
     * own y, {@code 20 + 10 * moodOffset}, whatever the rows do.
     * 任务行正下方那一行（崩溃警告所在行）的顶部 y。{@code moodOffset} 是 Wathe 平滑后的最后一行偏移，{@code rowPresence}
     * 是本帧已绘制任务行中最大的透明度（没有任务行时为 0）。重排布局：位于最后一行下方一个行距处；任务行淡出时该行平滑上移
     * 到标题所在行（标题只在有任务行时绘制，此时该行空闲）。{@link #WATHE}：警告自身的 y，即
     * {@code 20 + 10 * moodOffset}，与任务行状态无关。
     */
    public float belowRowsY(float moodOffset, float rowPresence) {
        float offset = Float.isFinite(moodOffset) ? Math.max(0.0F, moodOffset) : 0.0F;
        if (!relaid) {
            return firstTaskY + ROW_PITCH * offset + watheWarningDy();
        }
        float presence = Float.isFinite(rowPresence) ? Math.min(1.0F, Math.max(0.0F, rowPresence)) : 0.0F;
        return headerY + presence * ROW_PITCH * (offset + 2.0F);
    }

    /**
     * Top y of the first line that is free in the task column ({@link #taskX()}) this frame, for a line a feature
     * pins under the task rows (the Bell Echo hint). {@code moodBlockVisible} is false when Wathe skips its block (not
     * the murder game mode), which leaves its statics stale. Re-laid: the flag row itself without the block, the
     * header's line in Psycho mode (under the banner), else the line under the rows, one pitch and the shake further
     * down while the breakdown warning holds that line. With the figure the column is right of the figure's
     * rectangle, so such a line may sit beside the figure and never overlaps it; a line that starts further left must
     * use {@link #maxBottom()} instead. {@link #WATHE}: under the lower of flag and bar, and under the warning with
     * its shake, plus {@link #WATHE_BELOW_GAP}.
     * 本帧任务列（{@link #taskX()}）中第一条空闲行的顶部 y，供某功能固定在任务行下方的行使用（敲钟回响提示）。Wathe 跳过其区块
     * （非谋杀模式）时 {@code moodBlockVisible} 为 false，此时其静态值已过期。重排布局：没有区块时为旗帜行本身；疯魔模式时为
     * 标题所在行（横幅下方）；否则为任务行下方那一行，崩溃警告占用该行时再下移一个行距与抖动幅度。有示意图时该列位于示意图
     * 矩形右侧，因此这样的行可以位于示意图旁边而绝不与其重叠；起点更靠左的行必须改用 {@link #maxBottom()}。
     * {@link #WATHE}：位于旗帜与理智条中较低者之下，并在警告及其抖动之下，再加 {@link #WATHE_BELOW_GAP}。
     */
    public int belowBlockY(boolean moodBlockVisible, boolean psychoActive, float moodOffset, float moodRender,
                           float rowPresence) {
        boolean breakdown = Float.isFinite(moodRender) && moodRender < 0.0F;
        float shake = breakdown ? Math.min(1.0F, -moodRender) * SHAKE_SCALE : 0.0F;
        if (!relaid) {
            if (!moodBlockVisible) {
                return flagY;
            }
            if (psychoActive) {
                return flagY + FLAG_HEIGHT + WATHE_BELOW_GAP;
            }
            float offset = Float.isFinite(moodOffset) ? Math.max(0.0F, moodOffset) : 0.0F;
            float flagBottom = flagY + FLAG_HEIGHT + WATHE_FLAG_SLIDE * offset;
            float barBottom = firstTaskY + ROW_PITCH * offset + watheBarDy() + WATHE_BAR_HEIGHT;
            float bottom = Math.max(flagBottom, barBottom);
            if (breakdown) {
                bottom = Math.max(bottom, belowRowsY(moodOffset, rowPresence) + FONT_HEIGHT + shake);
            }
            return (int) Math.ceil(bottom) + WATHE_BELOW_GAP;
        }
        if (!moodBlockVisible) {
            return flagY;
        }
        if (psychoActive) {
            return headerY;
        }
        float y = belowRowsY(moodOffset, rowPresence);
        if (breakdown) {
            y += ROW_PITCH + shake;
        }
        return (int) Math.ceil(y);
    }

    /**
     * Deepest pixel the corner can reach, at any x: the deeper of the figure's bottom and of four task rows with the
     * breakdown warning under them and its shake. A line that starts at the left edge and must never jump with the
     * task count sits under it ({@link #WATHE}: 62).
     * 该角落在任意 x 处可能到达的最低像素：示意图下缘与“四行任务、其下方的崩溃警告及抖动”两者中较深的一个。从左缘开始且
     * 不应随任务数量跳动的行位于其下方（{@link #WATHE}：62）。
     */
    public int maxBottom() {
        int rowsBottom = (int) Math.ceil(belowRowsY(MAX_TASK_ROWS - 1, 1.0F) + FONT_HEIGHT + SHAKE_SCALE);
        return Math.max(rowsBottom, figureBottom);
    }

    /**
     * The signed percentage shown for a displayed mood fraction (-1..1, see
     * {@code SparkTraitsApi.displayedMoodFraction}): -100..100, rounded. A fraction that is not a number reads 0.
     * 显示用理智比例（-1..1，见 {@code SparkTraitsApi.displayedMoodFraction}）对应的带符号百分比：-100..100，四舍五入。
     * 非数值的比例显示 0。
     */
    public static int moodPercent(float moodFraction) {
        if (Float.isNaN(moodFraction)) {
            return 0;
        }
        return Math.round(Math.max(-1.0F, Math.min(1.0F, moodFraction)) * 100.0F);
    }

    /**
     * Opaque ARGB of the percentage by the displayed fraction: green from {@link #MOOD_MID_BELOW}, yellow from
     * {@link #MOOD_DEPRESSIVE_BELOW}, red below, so any negative value is red.
     * 按显示比例得到的百分比不透明 ARGB：不低于 {@link #MOOD_MID_BELOW} 为绿色，不低于 {@link #MOOD_DEPRESSIVE_BELOW}
     * 为黄色，其余为红色，因此负值一律为红色。
     */
    public static int moodPercentColor(float moodFraction) {
        int rgb;
        if (!(moodFraction >= MOOD_DEPRESSIVE_BELOW)) {
            rgb = PERCENT_DEPRESSIVE_RGB;
        } else if (moodFraction < MOOD_MID_BELOW) {
            rgb = PERCENT_MID_RGB;
        } else {
            rgb = PERCENT_HAPPY_RGB;
        }
        return 0xFF000000 | rgb;
    }

    /** Alpha (0..1) of a drawn row from its ARGB text colour. / 由任务行文本的 ARGB 颜色得到其透明度（0..1）。 */
    public static float rowAlpha(int argb) {
        return (argb >>> 24) / 255.0F;
    }

    /**
     * ARGB of the header at the rows' presence, with Wathe's alpha encoding, or 0 when it must not be drawn: the text
     * renderer treats an alpha below 4 as fully opaque.
     * 按任务行存在度得到的标题 ARGB（使用 Wathe 的透明度编码）；不应绘制时为 0：文字渲染器会把低于 4 的透明度当作完全不透明。
     */
    public static int headerColor(float rowPresence) {
        if (!(rowPresence > 0.0F)) {
            return 0;
        }
        int alpha = (int) (Math.min(1.0F, rowPresence) * 255.0F);
        return (alpha & 0xFC) == 0 ? 0 : (alpha << 24) | HEADER_RGB;
    }

    /** Wathe's bar under a row's top: {@code 8 + fontHeight - 6}. / Wathe 理智条相对行顶部的距离。 */
    private static int watheBarDy() {
        return 8 + FONT_HEIGHT - WATHE_FIRST_TASK_Y;
    }

    private static int watheWarningDy() {
        return watheBarDy() + WATHE_WARNING_GAP;
    }
}
