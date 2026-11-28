package dev.caecorthus.sparkdlc.client.hud.topleft;

import dev.caecorthus.sparkdlc.client.health.BodyHealthHudRules;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.client.gui.MoodRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * The thin, stateful half of the top-left corner ({@link TopLeftHudLayout} is the pure half): picks the layout of the
 * frame, remembers how present the task rows are, and draws the two things Wathe does not draw itself, the task-list
 * header and the mood percentage. Wathe 1.5.6 {@code MoodRenderer} stays the driver; the mixin
 * {@code client.mixin.health.TopLeftHudMoodRendererMixin} calls in here from Wathe's own draw calls, and the features
 * that pin a line to that corner read {@link #layout()} and {@link #rowPresence()}.
 * <p>The only state is {@link #rowPresence}, rebuilt on every {@code renderHud}: reset at its HEAD, raised by every
 * row drawn (Wathe's rows and the Abyss pseudo row). A reader outside {@code renderHud} must run after Wathe's main
 * HUD in the same frame, like the readers of {@code MoodRenderer.moodOffset}.
 * <p>Display only: nothing here changes game state, and on a server that is not a confirmed SparkDLC one
 * {@link #layout()} is {@link TopLeftHudLayout#WATHE} and nothing is drawn.
 * <p>左上角的轻量、有状态部分（纯函数部分为 {@link TopLeftHudLayout}）：决定本帧的布局，记录任务行的存在度，并绘制 Wathe
 * 自身不绘制的两样内容：任务清单标题与理智百分比。该角落仍由 Wathe 1.5.6 {@code MoodRenderer} 驱动；mixin
 * {@code client.mixin.health.TopLeftHudMoodRendererMixin} 从 Wathe 自身的绘制调用处调用本类，在该角落固定一行文本的功能
 * 读取 {@link #layout()} 与 {@link #rowPresence()}。
 * <p>唯一的状态是 {@link #rowPresence}，每次 {@code renderHud} 都会重建：在其 HEAD 处清零，每绘制一行（Wathe 的任务行与
 * 深渊临时任务行）即抬高。{@code renderHud} 之外的读取方必须在同一帧 Wathe 主 HUD 之后运行，与
 * {@code MoodRenderer.moodOffset} 的读取方相同。
 * <p>仅用于展示：此处不改变任何游戏状态；在未确认为 SparkDLC 的服务器上 {@link #layout()} 为
 * {@link TopLeftHudLayout#WATHE}，且不绘制任何内容。
 */
public final class TopLeftHud {
    public static final String TASK_LIST_KEY = "hud.sparkdlc.task_list";

    private static final Text HEADER = Text.translatable(TASK_LIST_KEY);
    // Render-thread cache of "-100%".."100%", so a frame allocates none. / 渲染线程上 "-100%".."100%" 的缓存，使每帧不产生分配。
    private static final String[] PERCENTS = new String[201];

    /** Largest alpha among the task rows drawn in the current {@code renderHud}. / 当前 {@code renderHud} 中已绘制任务行的最大透明度。 */
    private static float rowPresence;

    private TopLeftHud() {
    }

    /**
     * The layout of this frame. The re-layout applies only on a confirmed SparkDLC server; the figure decides which
     * of the two re-laid layouts. Cheapest check first, so any other server leaves before a component lookup.
     * 本帧的布局。只有已确认的 SparkDLC 服务器才重排；示意图是否显示决定使用两种重排布局中的哪一种。先做开销最小的判断，
     * 其他服务器在任何组件查询之前返回。
     */
    public static TopLeftHudLayout layout() {
        if (!SparkDlcServerConnection.isConfirmedServer()) {
            return TopLeftHudLayout.WATHE;
        }
        return TopLeftHudLayout.select(true, BodyHealthHudRules.visible(MinecraftClient.getInstance()));
    }

    /** HEAD of Wathe's {@code renderHud}: no row is drawn yet. / Wathe {@code renderHud} 的 HEAD：尚未绘制任何任务行。 */
    public static void beginFrame() {
        rowPresence = 0.0F;
    }

    /** A task row was drawn at this alpha (0..1). / 以该透明度（0..1）绘制了一行任务。 */
    public static void rowShown(float alpha) {
        if (alpha > rowPresence) {
            rowPresence = Math.min(1.0F, alpha);
        }
    }

    /**
     * How present the task rows are in this frame: 0 without rows (also in Psycho mode and while Wathe skips its
     * block), else the largest row alpha.
     * 本帧任务行的存在度：没有任务行时为 0（疯魔模式以及 Wathe 跳过其区块时同样如此），否则为最大的行透明度。
     */
    public static float rowPresence() {
        return rowPresence;
    }

    /**
     * The header line 「任务清单」 above the rows, only in a re-laid layout and only while at least one row is drawn; it
     * fades with the rows.
     * 任务行上方的标题行「任务清单」，仅在重排布局中、且至少绘制了一行任务时显示，并随任务行淡入淡出。
     */
    public static void renderHeader(DrawContext context, TextRenderer textRenderer) {
        TopLeftHudLayout layout = layout();
        if (!layout.relaid()) {
            return;
        }
        int color = TopLeftHudLayout.headerColor(rowPresence);
        if (color == 0) {
            return;
        }
        context.drawTextWithShadow(textRenderer, HEADER, layout.headerX(), layout.headerY(), color);
    }

    /**
     * The signed mood percentage right of the flag Wathe just drew at ({@code flagX}, {@code flagY}) in the current
     * matrix, so it follows the flag's position and its breakdown shake. Only in a re-laid layout. The value is
     * Wathe's smoothed {@code moodRender} as a fraction of the local player's mood range
     * ({@code SparkTraitsApi.displayedMoodFraction}, -100% .. 100%), asked once per frame: the only caller is the wrap
     * of {@code renderCivilian}'s single icon draw, which Wathe reaches only for an effective REAL mood.
     * 在当前矩阵下，于 Wathe 刚绘制的旗帜（{@code flagX}, {@code flagY}）右侧绘制带符号的理智百分比，因此它跟随旗帜的位置与
     * 崩溃抖动。仅在重排布局中绘制。数值为 Wathe 平滑后的 {@code moodRender} 占本地玩家理智范围的比例
     * （{@code SparkTraitsApi.displayedMoodFraction}，-100% .. 100%），每帧只询问一次：唯一的调用方是对 {@code renderCivilian}
     * 中唯一一次图标绘制的包装，而 Wathe 只在有效理智类型为 REAL 时才进入该方法。
     */
    public static void renderMoodPercent(DrawContext context, int flagX, int flagY) {
        if (!layout().relaid()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        float fraction = SparkTraitsApi.displayedMoodFraction(client.player, Role.MoodType.REAL, MoodRenderer.moodRender);
        context.drawTextWithShadow(
                client.textRenderer,
                percentText(TopLeftHudLayout.moodPercent(fraction)),
                flagX + TopLeftHudLayout.percentDx(),
                flagY + TopLeftHudLayout.percentDy(),
                TopLeftHudLayout.moodPercentColor(fraction));
    }

    /** {@code percent} is -100..100. / {@code percent} 取值 -100..100。 */
    private static String percentText(int percent) {
        int index = percent + 100;
        String cached = PERCENTS[index];
        if (cached == null) {
            cached = percent + "%";
            PERCENTS[index] = cached;
        }
        return cached;
    }
}
