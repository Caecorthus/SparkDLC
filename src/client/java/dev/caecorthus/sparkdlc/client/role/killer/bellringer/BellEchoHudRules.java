package dev.caecorthus.sparkdlc.client.role.killer.bellringer;

import dev.caecorthus.sparkdlc.client.hud.topleft.TopLeftHudLayout;
import dev.caecorthus.sparkdlc.util.TickTime;

/**
 * Pure presentation rules for the Bell Ringer's Echo HUD. The heard hint is a line pinned under the task rows of the
 * top-left block that Wathe 1.5.6 {@code MoodRenderer.renderHud} draws; where that block is and how deep it reaches
 * is described in one place, {@link TopLeftHudLayout} (Wathe's own block, or the re-laid corner, whose task column is
 * right of the body health figure while the figure shows), and the hint follows it in the task column: beside the
 * figure it may sit next to it, never on it. Re-check {@code TopLeftHudLayout} when the pinned Wathe jar changes.
 * 敲钟人回响 HUD 的纯展示规则。“听到钟声”提示是固定在 Wathe 1.5.6 {@code MoodRenderer.renderHud} 所绘左上角区块下方的
 * 任务行下方的一行文本；该区块的位置与最低到达处统一由 {@link TopLeftHudLayout} 描述（Wathe 自身的区块，或重排后的角落：
 * 身体健康示意图显示时其任务列位于示意图右侧），提示在任务列中随之定位：位于示意图旁边时可与其并排，但绝不压在其上。更换 Wathe 固定版本时需重新核对 {@code TopLeftHudLayout}。
 */
public final class BellEchoHudRules {
    public static final String TASK_KEY_PREFIX = "task.sparkdlc.bell_echo.";

    private BellEchoHudRules() {
    }

    /**
     * Left x of the heard hint: the block's task and warning text column.
     * “听到钟声”提示的左侧 x：区块中任务与警告文本所在列。
     */
    public static int hintX(TopLeftHudLayout layout) {
        return layout.taskX();
    }

    /**
     * Top y of the heard hint so it sits below the mood/task block or the psycho banner
     * ({@link TopLeftHudLayout#belowBlockY}). {@code moodBlockVisible} is false when Wathe skips its block (non-murder
     * mode), which leaves the statics stale. The civilian breakdown warning is reserved whenever
     * {@code moodRender < 0}, plus its shake amplitude, so the hint never overlaps it. {@code rowPresence} is how
     * present the task rows are this frame ({@code TopLeftHud.rowPresence()}).
     * “听到钟声”提示的顶部 y，使其位于情绪/任务区块或狂暴横幅下方（{@link TopLeftHudLayout#belowBlockY}）。Wathe 跳过区块
     * （非谋杀模式）时 {@code moodBlockVisible} 为 false，此时静态值已过期。只要 {@code moodRender < 0} 就为平民崩溃警告及其
     * 抖动幅度预留空间，保证提示不与其重叠。{@code rowPresence} 为本帧任务行的存在度（{@code TopLeftHud.rowPresence()}）。
     */
    public static int hintY(
            TopLeftHudLayout layout,
            boolean moodBlockVisible,
            boolean psychoActive,
            float moodOffset,
            float moodRender,
            float rowPresence
    ) {
        return layout.belowBlockY(moodBlockVisible, psychoActive, moodOffset, moodRender, rowPresence);
    }

    /** Whole seconds shown on the Echo task line, rounded up, never negative. / 回响任务行显示的整秒数，向上取整且不为负。 */
    public static int taskSeconds(int remainingTicks) {
        return TickTime.ceilSeconds(remainingTicks);
    }

    /** Lang key for a Wathe task name ({@code sleep/outside/eat/drink}). / Wathe 任务名对应的语言键。 */
    public static String taskKey(String watheTaskName) {
        return TASK_KEY_PREFIX + watheTaskName;
    }
}
