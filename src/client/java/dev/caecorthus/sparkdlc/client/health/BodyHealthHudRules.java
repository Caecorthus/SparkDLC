package dev.caecorthus.sparkdlc.client.health;

import dev.caecorthus.sparkdlc.health.BodyPart;
import dev.caecorthus.sparkdlc.health.HealthModeComponent;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Layout, colours and visibility of the body health figure: a standing paper-doll in the top-left corner of the
 * in-round HUD, one hollow cell per {@link BodyPart}: only its frame is drawn, coloured by how much health the part has left, and
 * the inside stays see-through; no number is drawn.
 * The figure has the proportions of the vanilla player model at two GUI pixels per skin pixel (head 8x8, torso 8x12,
 * arms and legs 4x12), the torso split into chest (upper 7) and abdomen (lower 5). It is seen from the FRONT, like a
 * player facing the viewer: the player's RIGHT arm and leg are on the LEFT of the screen. All rectangles are GUI
 * pixels; cell rectangles are relative to the figure origin ({@link #FIGURE_X}, {@link #FIGURE_Y}). The figure's
 * rectangle is reserved for it while {@link #visible} holds: Wathe's mood flag and task rows are moved out of it by
 * their own feature ({@code client.hud.topleft}), which reads these constants. Display only: nothing here changes
 * state.
 * 身体健康示意图的布局、配色与可见性：对局 HUD 左上角的站立纸娃娃，每个 {@link BodyPart} 一个空心格子：只绘制边框，边框按该部位
 * 剩余血量着色，内部保持透明；不绘制数字。示意图采用原版玩家模型的比例，每个皮肤像素对应两个 GUI 像素（头 8x8、躯干 8x12、手臂与腿 4x12），躯干分为胸部（上 7）
 * 与腹部（下 5）。示意图为正视（如同面向观察者的玩家）：玩家的右臂与右腿位于屏幕左侧。所有矩形单位为 GUI 像素；各格矩形
 * 相对于示意图原点（{@link #FIGURE_X}、{@link #FIGURE_Y}）。{@link #visible} 成立时，示意图的矩形保留给它：Wathe 的心情
 * 旗帜与任务行由其自身功能（{@code client.hud.topleft}）读取这些常量后移出该区域。仅用于展示：此处不改变任何状态。
 */
public final class BodyHealthHudRules {
    /** Figure origin and size on screen. / 示意图在屏幕上的原点与尺寸。 */
    public static final int FIGURE_X = 6, FIGURE_Y = 6, FIGURE_WIDTH = 32, FIGURE_HEIGHT = 64;

    /** Thickness of a cell's frame; the inside is left empty. / 格子边框的厚度；内部留空。 */
    public static final int FRAME_THICKNESS = 1;
    /** Frame of a part at zero: black. / 归零部位的边框：黑色。 */
    public static final int DESTROYED_FRAME_COLOR = 0xFF0B0B0D;

    /** Hue of a full part (green) in degrees; an almost empty part is at 0 (red). / 满血部位的色相（绿色，单位为度）；将尽的部位为 0（红色）。 */
    private static final float FULL_HUE_DEGREES = 120.0F;
    private static final float FRAME_SATURATION = 0.90F;
    private static final float FRAME_VALUE = 0.95F;

    // Front view: the player's right side is on the left of the screen. / 正视：玩家的右侧位于屏幕左侧。
    private static final Cell HEAD_CELL = new Cell(8, 0, 16, 16);
    private static final Cell CHEST_CELL = new Cell(8, 16, 16, 14);
    private static final Cell ABDOMEN_CELL = new Cell(8, 30, 16, 10);
    private static final Cell RIGHT_ARM_CELL = new Cell(0, 16, 8, 24);
    private static final Cell LEFT_ARM_CELL = new Cell(24, 16, 8, 24);
    private static final Cell RIGHT_LEG_CELL = new Cell(8, 40, 8, 24);
    private static final Cell LEFT_LEG_CELL = new Cell(16, 40, 8, 24);

    private BodyHealthHudRules() {
    }

    /** One part's rectangle, relative to the figure origin. / 单个部位的矩形，相对于示意图原点。 */
    public record Cell(int x, int y, int width, int height) {
    }

    /**
     * True only on a confirmed SparkDLC server with the health mode on, while the local player is playing and alive
     * with Wathe's train HUD shown and F1 off. The round gates are exactly those of Wathe's own mood flag (train HUD
     * and playing-and-alive, no ACTIVE or fade term), so the figure appears and leaves together with the flag that is
     * moved aside for it and the flag never jumps mid-round. Cheapest exits first; the world-wide mode flag is read
     * before any round state.
     * 仅在已确认的 SparkDLC 服务器上、健康模式开启、本地玩家参与且存活、Wathe 列车 HUD 显示且未按 F1 时为真。回合门槛与
     * Wathe 自身的心情旗帜完全一致（列车 HUD 与参与且存活，不含 ACTIVE 与淡入条件），因此示意图与为它让位的旗帜同时出现、
     * 同时消失，旗帜不会在回合中途跳动。先做开销最小的判断；先读全局模式开关，再读回合状态。
     */
    public static boolean visible(MinecraftClient client) {
        if (client == null || client.options.hudHidden || !SparkDlcServerConnection.isConfirmedServer()) {
            return false;
        }
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || !HealthModeComponent.isEnabled(client.world)) {
            return false;
        }
        boolean trainHudActive = WatheClient.trainComponent != null && WatheClient.trainComponent.hasHud();
        return shows(true, true, false, trainHudActive, GameFunctions.isPlayerPlayingAndAlive(player));
    }

    /**
     * Pure form of {@link #visible}: every gate as a value.
     * {@link #visible} 的纯函数形式：每个门槛都以值传入。
     */
    public static boolean shows(boolean confirmedServer, boolean healthMode, boolean hudHidden,
                                boolean trainHudActive, boolean playingAndAlive) {
        return confirmedServer && healthMode && !hudHidden && trainHudActive && playingAndAlive;
    }

    /**
     * The part's cell, relative to the figure origin. The switch is exhaustive on purpose: a new body part must get a
     * cell here before the mod compiles.
     * 该部位的格子，相对于示意图原点。此 switch 有意写成穷尽形式：新增身体部位时必须先在此处给出格子，模组才能编译。
     */
    public static Cell cell(BodyPart part) {
        return switch (part) {
            case HEAD -> HEAD_CELL;
            case CHEST -> CHEST_CELL;
            case ABDOMEN -> ABDOMEN_CELL;
            case LEFT_ARM -> LEFT_ARM_CELL;
            case RIGHT_ARM -> RIGHT_ARM_CELL;
            case LEFT_LEG -> LEFT_LEG_CELL;
            case RIGHT_LEG -> RIGHT_LEG_CELL;
        };
    }

    /**
     * Opaque ARGB of a cell's frame: a continuous hue from the greenest (full) to the reddest (almost empty), linear
     * in {@code current / max}; a part at zero is the black {@link #DESTROYED_FRAME_COLOR}. A part without a positive
     * maximum counts as full unless it is at zero.
     * 格子边框的不透明 ARGB：色相随 {@code current / max} 线性地从最绿（满血）连续变化到最红（将尽）；归零的部位为黑色
     * {@link #DESTROYED_FRAME_COLOR}。最大值不为正的部位，只要不为零即视为满血。
     */
    public static int frameColor(int current, int max) {
        if (current <= 0) {
            return DESTROYED_FRAME_COLOR;
        }
        float ratio = max <= 0 ? 1.0F : Math.min(1.0F, current / (float) max);
        return 0xFF000000 | hsvToRgb(FULL_HUE_DEGREES * ratio, FRAME_SATURATION, FRAME_VALUE);
    }

    /** RGB of a hue in degrees at the given saturation and value. / 给定饱和度与明度下某色相（度）的 RGB。 */
    private static int hsvToRgb(float hueDegrees, float saturation, float value) {
        float h = ((hueDegrees % 360.0F) + 360.0F) % 360.0F / 60.0F;
        int sector = (int) h;
        float f = h - sector;
        float p = value * (1.0F - saturation);
        float q = value * (1.0F - saturation * f);
        float t = value * (1.0F - saturation * (1.0F - f));
        float r;
        float g;
        float b;
        switch (sector) {
            case 0 -> { r = value; g = t; b = p; }
            case 1 -> { r = q; g = value; b = p; }
            case 2 -> { r = p; g = value; b = t; }
            case 3 -> { r = p; g = q; b = value; }
            case 4 -> { r = t; g = p; b = value; }
            default -> { r = value; g = p; b = q; }
        }
        return (Math.round(r * 255.0F) << 16) | (Math.round(g * 255.0F) << 8) | Math.round(b * 255.0F);
    }
}
