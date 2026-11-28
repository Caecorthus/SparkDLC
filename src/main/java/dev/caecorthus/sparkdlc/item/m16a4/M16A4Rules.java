package dev.caecorthus.sparkdlc.item.m16a4;

/**
 * Pure constants and timing rules of the M16A4: the magazine, the three-round burst window, the cyclic rate, both
 * reload lengths and the revolver's recoil. Both sides may read it; nothing here touches the world.
 * M16A4 的纯常量与计时规则：弹匣、三发点射窗口、循环射速、两种换弹时长，以及左轮同款后坐。两端均可读取；这里不触碰世界。
 */
public final class M16A4Rules {
    /** Magazine capacity, also the tactical reload result. / 弹匣容量，也是战术换弹后的弹数。 */
    public static final int MAGAZINE_SIZE = 31;
    /** An empty reload chambers one round short of a full magazine. / 空仓换弹比满弹匣少一发。 */
    public static final int EMPTY_RELOAD_AMMO = 30;
    /** One trigger pull fires at most this many rounds. / 一次扣扳机最多打出这么多发。 */
    public static final int BURST_SIZE = 3;
    /** 15 rounds per second, kept as a fractional tick interval so the average rate stays at 15. / 每秒 15 发，使用小数刻间隔保持平均射速。 */
    public static final double TICKS_PER_SHOT = 20.0D / 15.0D;
    /** Tactical reload (something still in the magazine): 2.2 s. / 战术换弹（弹匣内还有子弹）：2.2 秒。 */
    public static final int TACTICAL_RELOAD_TICKS = 44;
    /** Empty reload: 3 s. / 空仓换弹：3 秒。 */
    public static final int EMPTY_RELOAD_TICKS = 60;
    /** The revolver's recoil kick, 4 degrees per shot. / 与左轮相同的后坐，每发 4 度。 */
    public static final float RECOIL_PITCH = 4.0F;
    /** Gun range, set to 40 blocks (owner 2026-11-27). / 射程 40 格（所有者 2026-11-27）。 */
    public static final double RANGE = 40.0D;
    /**
     * Slack of the deadline comparisons: three intervals of 20/15 do not add up to exactly 4.0 in floating point.
     * 截止时刻比较的容差：三个 20/15 的间隔在浮点数中加起来并不恰好是 4.0。
     */
    private static final double DEADLINE_SLACK = 1.0E-6D;

    private M16A4Rules() {
    }

    /**
     * Whether a round may leave at server tick {@code now}, given the deadline the shooter's previous round set.
     * Asked for every round, the first of a pull included, so no click rate or packet rate beats the cyclic rate. A
     * deadline more than one interval ahead cannot have been set by this clock (the world time ran backwards) and is
     * ignored instead of locking the rifle.
     * 在服务端第 {@code now} 刻能否出膛一发，依据射手上一发设定的截止时刻。每一发都要询问，包括一次扣扳机的第一发，因此
     * 任何点击频率或发包频率都无法超过循环射速。超前一个间隔以上的截止时刻不可能出自这个时钟（世界时间倒退了），会被忽略
     * 而不是把步枪锁死。
     */
    public static boolean roundDue(double now, double deadline) {
        return now + DEADLINE_SLACK >= deadline || deadline > now + TICKS_PER_SHOT + DEADLINE_SLACK;
    }

    /**
     * The deadline of the round after one fired at {@code now}. A deadline that fell within the last tick is the one
     * this round waited for, so the interval is counted from it and the fraction carries over: rounds leave on ticks
     * 0, 2, 3, 4, 6, 7, 8, ... and the rate stays at fifteen a second although a tick fires at most once. Any other
     * deadline (long past, or ignored by {@link #roundDue}) restarts the schedule at {@code now}.
     * 在第 {@code now} 刻出膛一发之后，下一发的截止时刻。落在最近一刻之内的截止时刻就是这一发所等待的那个，因此间隔从它
     * 起算、小数部分得以延续：子弹在第 0、2、3、4、6、7、8…刻出膛，尽管每刻最多开一次火，射速仍保持每秒 15 发。其他
     * 截止时刻（早已过去，或被 {@link #roundDue} 忽略）则从 {@code now} 重新开始计时。
     */
    public static double nextDeadline(double now, double deadline) {
        double base = deadline <= now + DEADLINE_SLACK && deadline > now - 1.0D ? deadline : now;
        return base + TICKS_PER_SHOT;
    }
}
