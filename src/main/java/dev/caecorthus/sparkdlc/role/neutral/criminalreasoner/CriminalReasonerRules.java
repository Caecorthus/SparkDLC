package dev.caecorthus.sparkdlc.role.neutral.criminalreasoner;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.faction.api.PoliceRoles;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * 犯罪推理学家 Criminal Reasoner — neutral role that wins by naming every victim's killer.
 * <p>Ported from NoellesRoles' Parox build (owner request, 2026-11-01). Owner-facing rules:
 * neutral faction with a fake sanity bar and limited sprint stamina, cannot pick up the revolver, carries the
 * neutral master key, reasons about the un-solved dead through a menu, and each new success grants one 20 s
 * invisibility charge that no instinct highlight can see through.
 * 犯罪推理学家——靠指认每名死者的凶手获胜的中立身份。
 * <p>按所有者要求（2026-11-01）从 NoellesRoles 的 Parox 版本移植。对外规则：
 * 中立阵营、假理智条、疾跑体力有限、不能捡左轮、携带中立万能钥匙；
 * 通过菜单推理尚未解出的死者，每获得一次新的成功就得到一次 20 秒隐身，且任何本能透视都看不穿。
 */
public final class CriminalReasonerRules {

    public static final Identifier ROLE_ID = SparkDLC.id("criminal_reasoner");

    /** Forced-cooldown store id of the reasoning skill (store 31). 推理技能的强制冷却存储 id（第 31 个存储）。 */
    public static final Identifier REASONING_SKILL_ID = SparkDLC.id("criminal_reasoner_reasoning");

    /** Identity colour from the source build: (112, 75, 75). 沿用原实现的身份色 (112, 75, 75)。 */
    public static final int COLOR = 0x704B4B;

    /** Round-start cooldown for this role's skill, in ticks (45 s, owner 2026-11-02).
     * 该身份技能的回合初始冷却（45 秒，所有者 2026-11-02）。 */
    public static final int INITIAL_COOLDOWN_TICKS = 45 * 20;

    /** Cooldown after a correct reasoning, in ticks (5 s). 推理成功后的冷却（5 秒）。 */
    public static final int SUCCESS_COOLDOWN_TICKS = 5 * 20;

    /** Cooldown after a wrong reasoning, in ticks (60 s). 推理失败后的冷却（60 秒）。 */
    public static final int FAIL_COOLDOWN_TICKS = 60 * 20;

    /** How long one invisibility charge lasts, in ticks (20 s). 一次隐身持续的时长（20 秒）。 */
    public static final int INVISIBILITY_TICKS = 20 * 20;

    /** Delay before the success notice reaches the police, in ticks (5 s). 成功通知发给警察的延迟（5 秒）。 */
    public static final int NOTICE_DELAY_TICKS = 5 * 20;

    /** One charge is granted per new solved victim. 每解出一名新死者给一次充能。 */
    public static final int CHARGES_PER_SUCCESS = 1;

    /** Victim count divisor for the win threshold: floor(players / 3). 胜利阈值：⌊开局人数 / 3⌋。 */
    public static final int WIN_THRESHOLD_DIVISOR = 3;

    /** How long a menu roster stays valid on the server, in ticks (30 s). 菜单名单在服务端的有效期（30 秒）。 */
    public static final int MENU_SESSION_TICKS = 30 * 20;

    private CriminalReasonerRules() {
    }

    /** The exact role, never a disguise. 精确职业，从不是伪装。 */
    public static boolean isReasoner(@Nullable Role role) {
        return role != null && ROLE_ID.equals(role.identifier());
    }

    /** Victims to solve for the win: floor(players / 3), at least 1. 获胜需解出的死者数：⌊人数 / 3⌋，至少 1。 */
    public static int requiredSolves(int roundPlayers) {
        return Math.max(1, roundPlayers / WIN_THRESHOLD_DIVISOR);
    }

    /**
     * Who reads the anonymous success notice: a living player (playing, alive, not spectating) whose assigned role is a
     * real police role, never the reasoner. A real police role is in {@link PoliceRoles} and is not on Team Jiahao:
     * the Corrupt Cop, the Insider and the Sniper are in that list only to get the police tablet, so they do not read
     * it (the Black Raven, a good-side police role, does). Owner 2026-11-20, "这个改一下" and "内鬼不算警察吧": it went
     * to every player while no shared police list was reachable; with no living real police nobody reads it.
     * 谁会收到匿名成功公告：存活（参与对局、存活、非旁观）且所分配职业为真正警察的玩家，推理学家本人除外。真正的警察指属于
     * {@link PoliceRoles} 且不属于嘉豪阵营的职业：黑警、内应与狙击手只为领到警用平板才在该名单中，因此收不到（好人阵营的警察职业
     * 黑羽鸦可以收到）。所有者 2026-11-20「这个改一下」「内鬼不算警察吧」：此前拿不到共用警察名单时发给所有玩家；没有存活的真正
     * 警察时无人收到。
     */
    public static boolean receivesNotice(boolean isTheReasoner, boolean livingPlayer, @Nullable Role role,
                                         boolean teamJiahao) {
        return !isTheReasoner && livingPlayer && !teamJiahao && PoliceRoles.contains(role);
    }

    /** A threshold of 0 means "not set up this round" and never wins. 阈值为 0 表示本局未初始化，永不获胜。 */
    public static boolean hasWon(int solved, int required) {
        return required > 0 && solved >= required;
    }
}
