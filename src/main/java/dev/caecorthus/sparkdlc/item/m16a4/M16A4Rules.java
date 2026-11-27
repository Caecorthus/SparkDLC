package dev.caecorthus.sparkdlc.item.m16a4;

/**
 * Pure constants of the M16A4: the magazine, the three-round burst window, the cyclic rate, both reload lengths and the
 * revolver's recoil. Both sides may read it; nothing here touches the world.
 * M16A4 的纯常量：弹匣、三发点射窗口、循环射速、两种换弹时长，以及左轮同款后坐。两端均可读取；这里不触碰世界。
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
    /** Rewound player hitbox margin, the rifle's 0.1. / 回溯玩家命中箱余量，与步枪相同的 0.1。 */
    public static final double HIT_MARGIN = 0.1D;
    /** Gun range, set to 40 blocks (owner 2026-11-27). / 射程 40 格（所有者 2026-11-27）。 */
    public static final double RANGE = 40.0D;

    private M16A4Rules() {
    }
}
