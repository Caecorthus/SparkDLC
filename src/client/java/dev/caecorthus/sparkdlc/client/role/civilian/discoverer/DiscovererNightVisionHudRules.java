package dev.caecorthus.sparkdlc.client.role.civilian.discoverer;

import dev.caecorthus.sparkdlc.client.role.neutral.fiend.FiendDashHudRules;
import dev.caecorthus.sparkdlc.role.civilian.discoverer.DiscovererRules;
import dev.caecorthus.sparkdlc.util.TickTime;

/**
 * Pure presentation rules of the Discoverer's owner-only Night Vision line: "ready: press G" or "cooling down Ns",
 * from the remaining cooldown synced only to that Discoverer. Text only and only the local player's own state, so it
 * never leaks another player. "Ready" says nothing about the light: only the server reads it, and answers a press in
 * a bright place on the action bar. The line takes the Fiend Dash line's bottom-right layout as is (the Athlete
 * precedent); a player has one role, so it never meets another role's line. Role-owned presentation; never the witch
 * skill inventory panel.
 * 探索者仅本人可见的夜视行的纯展示规则：依据只同步给该探索者的剩余冷却显示“就绪：按 G”或“冷却 N 秒”。只有文字，且只读本地
 * 玩家自己的状态，因此绝不会泄露其他玩家。“就绪”不代表亮度合适：亮度只由服务端读取，在亮处按下时由服务端在动作栏回复。直接
 * 沿用魔人疾驰行的右下角布局（运动员先例）；一名玩家只有一个职业，因此不会与其他职业的行同时出现。属于职业自有展示，从不使用
 * 魔女技能背包面板。
 */
public final class DiscovererNightVisionHudRules {
    public static final String READY_KEY = "hud.sparkdlc.discoverer.night_vision.ready";
    public static final String COOLDOWN_KEY = "hud.sparkdlc.discoverer.night_vision.cooldown";
    /** Opaque Discoverer role colour. / 不透明的探索者职业色。 */
    public static final int READY_COLOR = 0xFF000000 | DiscovererRules.COLOR;
    /** Same muted grey as the Fiend's, the Athlete's and Noa's cooldown lines. / 与魔人、运动员、诺亚冷却行相同的灰色。 */
    public static final int COOLDOWN_COLOR = 0xFF8C99A6;

    private DiscovererNightVisionHudRules() {
    }

    /**
     * The line's translation key, whether it takes the ability-key hint, its seconds argument (negative = none) and
     * colour.
     * 该行的翻译键、是否带技能按键提示、秒数参数（负数表示无）与颜色。
     */
    public record Line(String key, boolean keyHint, int seconds, int color) {
        public boolean hasSeconds() {
            return seconds >= 0;
        }
    }

    /**
     * Drawn only on the Discoverer's own client while it is playing and alive, in an ACTIVE round once Wathe's
     * round-start fade is over, with Wathe's train HUD shown and F1 off, and never while a Taotie has swallowed it (the
     * server refuses a swallowed request, so no "ready: press G" there).
     * 仅在探索者自己的客户端上、其参与且存活、回合为 ACTIVE 且 Wathe 开局淡入结束、Wathe 列车 HUD 显示且未按 F1 时绘制；
     * 被饕餮吞下时从不绘制（服务端会拒绝被吞下时的请求，因此不显示“就绪：按 G”）。
     */
    public static boolean showsHud(boolean hudHidden, boolean gameActive, int fade, boolean trainHudActive,
                                   boolean discoverer, boolean playingAndAlive, boolean swallowed) {
        return !hudHidden && gameActive && fade <= 0 && trainHudActive && discoverer && playingAndAlive && !swallowed;
    }

    /**
     * Ready (with the key hint) when no cooldown remains, else the cooldown in whole seconds rounded up.
     * 无剩余冷却时为就绪（带按键提示），否则为向上取整的冷却秒数。
     */
    public static Line line(int cooldownTicks) {
        if (cooldownTicks > 0) {
            return new Line(COOLDOWN_KEY, false, TickTime.ceilSeconds(cooldownTicks), COOLDOWN_COLOR);
        }
        return new Line(READY_KEY, true, -1, READY_COLOR);
    }

    /** Left x of a right-aligned line. / 右对齐行的左侧 x。 */
    public static int rowX(int scaledWidth, int textWidth) {
        return FiendDashHudRules.rowX(scaledWidth, textWidth);
    }

    /**
     * Top y of the line, with the shared corner layout (see {@link FiendDashHudRules#rowY}).
     * 该行的顶部 y，使用共享的角落布局（见 {@link FiendDashHudRules#rowY}）。
     */
    public static int rowY(int scaledWidth, int scaledHeight, int measuredWidth, int fontHeight,
                           boolean sharedLineOccupied) {
        return FiendDashHudRules.rowY(scaledWidth, scaledHeight, measuredWidth, fontHeight, sharedLineOccupied);
    }

    /** The seconds the line is measured with, so it does not jitter as digits change. / 测量行宽所用的秒数，避免数字变化时抖动。 */
    public static int measuredSeconds(int seconds) {
        return FiendDashHudRules.measuredSeconds(seconds);
    }
}
