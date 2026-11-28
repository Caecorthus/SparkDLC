package dev.caecorthus.sparkdlc.role.civilian.discoverer;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Stable contract: 探索者 Discoverer, a passenger role built around open-air travel. Owner spec: Speed II in the open
 * air, 25% slower sprint drain, no passive income (+50 coins per finished task), a shop selling one spyglass and, on
 * the ability key, 15 s of Night Vision in a dark place (PR #12). Holds the owner numbers, the literal shop id and
 * side-neutral pure predicates; the services only read them.
 * 稳定契约：探索者，围绕露天行走设计的乘客职业。所有者设定：露天时速度 II、疾跑体力消耗慢 25%、没有被动收入
 * （每完成一个任务 +50 金币），商店出售一个望远镜，按技能键可在暗处获得 15 秒夜视（PR #12）。本类保存所有者数值、商店条目
 * 字面量 id 与两端通用的纯判定；各服务只读取。
 */
public final class DiscovererRules {

    public static final Identifier ROLE_ID = SparkDLC.id("discoverer");

    /** Owner-specified identity colour. / 所有者指定的身份色。 */
    public static final int COLOR = 0xAF9042;

    // ---- Open-air speed ----

    /** Speed II (amplifier 1). / 速度 II（等级参数 1）。 */
    public static final int OPEN_AIR_SPEED_AMPLIFIER = 1;

    /**
     * Effect length (the Emma pattern): with the refresh below, the Speed fades 1–2 s after the player steps inside.
     * 效果时长（沿用艾玛的写法）：配合下方的续期阈值，进入室内后速度在 1–2 秒内消失。
     */
    public static final int OPEN_AIR_SPEED_DURATION_TICKS = 40;

    /**
     * Re-applied once the remaining time is at or below this, so the effect is refreshed about once a second.
     * 剩余时长不超过此值时重新施加，因此大约每秒续期一次。
     */
    public static final int OPEN_AIR_SPEED_REFRESH_AT_OR_BELOW_TICKS = 20;

    // ---- Stamina ----

    /**
     * Sprint drain multiplier: 0.75 stamina per sprinting tick instead of Wathe's 1. The cap (the plain civilian 10 s)
     * and the regeneration stay Wathe's.
     * 疾跑消耗倍率：每个疾跑刻消耗 0.75 体力，而不是 Wathe 的 1。上限（普通平民的 10 秒）与恢复速度保持 Wathe 原样。
     */
    public static final double SPRINT_DRAIN_MULTIPLIER = 0.75D;

    /**
     * Wathe's hard-coded drain per sprinting tick ({@code PlayerEntityMixin#wathe$limitSprint}: {@code ticks - 1}).
     * Wathe 写死的每个疾跑刻消耗（{@code PlayerEntityMixin#wathe$limitSprint}：{@code ticks - 1}）。
     */
    public static final float WATHE_SPRINT_DRAIN_PER_TICK = 1.0F;

    /** "Nothing captured this tick" marker. / “本刻未捕获”的标记。 */
    public static final float NOT_CAPTURED = Float.NaN;

    // ---- Night vision (PR #12, owner 2026-11-26) ----

    /** Forced-cooldown store id of the Night Vision skill. / 夜视技能的强制冷却存储 id。 */
    public static final Identifier NIGHT_VISION_SKILL_ID = SparkDLC.id("discoverer_night_vision");
    /** Fifteen seconds of Night Vision per activation. / 每次激活 15 秒夜视。 */
    public static final int NIGHT_VISION_TICKS = 15 * 20;
    /** Cooldown after the first activation of a round. / 本局第一次激活后的冷却。 */
    public static final int NIGHT_VISION_INITIAL_COOLDOWN_TICKS = 60 * 20;
    /** Cooldown after every later activation. / 之后每次激活后的冷却。 */
    public static final int NIGHT_VISION_COOLDOWN_TICKS = 75 * 20;
    /**
     * The key only works at or below this light level. The level is the server's effective one: block light, or sky
     * light less the ambient darkness of the time of day and weather, whichever is higher. Open air is 15 at noon
     * (refused), 10 at Wathe's sundown (refused) and 4 at night (works); a lamp-lit room is refused too.
     * 只有亮度不高于该值时才生效。亮度取服务端的有效亮度：方块光，或天光减去由时间与天气决定的环境暗度，取较高者。露天在正午为
     * 15（拒绝），在 Wathe 的黄昏为 10（拒绝），在夜里为 4（可用）；亮着灯的房间同样被拒绝。
     */
    public static final int NIGHT_VISION_MAX_LIGHT = 8;

    // ---- Economy ----

    /** Coins per finished task; the role has no passive income. / 每完成一个任务的金币；该职业没有被动收入。 */
    public static final int TASK_REWARD = 50;

    // ---- Shop ----

    /** Bare shop-entry id, like the mod's other role shops. / 不带命名空间的商店条目 id，与本模组其他职业商店一致。 */
    public static final String SPYGLASS_ENTRY_ID = "discoverer_spyglass";
    public static final int SPYGLASS_PRICE = 150;
    public static final int SPYGLASS_STOCK = 1;

    private DiscovererRules() {
    }

    public static boolean isDiscoverer(@Nullable Role role) {
        return role != null && ROLE_ID.equals(role.identifier());
    }

    /**
     * Gate for the open-air Speed and the task pay, the USEC/Knight/Control Expert {@code canEarn} shape: an ACTIVE
     * round, playing and alive in Wathe, exactly the Discoverer, survival (so a Taotie-swallowed player or a Rift
     * occupant, both alive spectators, is refused) and not a restricted Wraith.
     * 露天速度与任务收入的门槛，沿用 USEC、骑士与控场专家的 {@code canEarn}：对局 ACTIVE、在 Wathe 中参与且存活、身份恰为
     * 探索者、生存模式（因此被饕餮吞下的玩家与裂隙门内的玩家这类存活旁观者都被拒绝）、不是受限冤魂。
     */
    public static boolean receivesPassives(boolean active, boolean playingAndAlive, @Nullable Role role,
                                           boolean spectator, boolean creative, boolean wraithRestricted) {
        return active && playingAndAlive && isDiscoverer(role) && !spectator && !creative && !wraithRestricted;
    }

    /**
     * Pays only a non-Impostor: the Impostor trait pays its holder's task income itself, so the two never stack.
     * 只向非内鬼发放：内鬼词条自行支付持有者的任务收入，两者绝不叠加。
     */
    public static boolean receivesTaskMoney(boolean impostor) {
        return !impostor;
    }

    /**
     * Whether to (re)apply the open-air Speed: no Speed, a weaker one (vanilla keeps it as the hidden effect and
     * restores it when ours runs out), or ours running low. A stronger or infinite Speed is left alone.
     * 是否（重新）施加露天速度：没有速度、已有更弱的速度（原版会把它存为隐藏效果，在本效果结束后恢复），或本效果即将结束。
     * 更强或无限时长的速度不做改动。
     */
    public static boolean refreshesSpeed(boolean present, int amplifier, boolean infinite, int duration) {
        if (!present || amplifier < OPEN_AIR_SPEED_AMPLIFIER) {
            return true;
        }
        return amplifier == OPEN_AIR_SPEED_AMPLIFIER && !infinite
                && duration <= OPEN_AIR_SPEED_REFRESH_AT_OR_BELOW_TICKS;
    }

    /** Outcome of a Night Vision request. / 夜视请求的结果。 */
    public enum NightVisionVerdict {
        /** Grant the effect and start the cooldown. / 发放效果并开始冷却。 */
        USE,
        /** Refused silently; costs nothing. / 静默拒绝，不消耗任何东西。 */
        REFUSE,
        /** Still cooling down; the player is told how long. / 仍在冷却，提示剩余时间。 */
        COOLING_DOWN,
        /** Too bright here; the player is told so. / 此处太亮，给出提示。 */
        TOO_BRIGHT,
        /** A Night Vision that lasts at least as long is already running; costs nothing. / 已有不短于本技能的夜视，不消耗冷却。 */
        ALREADY_ACTIVE,
        /** Refused by Grand Witch Fear; the player is told why. / 被大魔女恐惧拒绝，并提示原因。 */
        FEARED
    }

    /**
     * Night Vision gate, checked in order (Noa's painting set): the real Discoverer in an ACTIVE round, playing, alive
     * and not a spectator, not swallowed by a Taotie, not stunned by the Control Expert, not role-skill blocked
     * (SparkTraitsApi); then the cooldown, the light and a Night Vision already running, each with its own hint; Fear
     * is checked last so its message only appears when the skill would otherwise fire. Safe time, the Taotie daze and
     * the Seeker, Riftwalker and Knight duel sessions drop the payload before it gets here.
     * 夜视门槛，按顺序检查（与诺亚作画相同）：处于 ACTIVE 回合的真实探索者，在局存活且不是旁观者，未被饕餮吞下，未被控场专家
     * 眩晕，未被职业技能封锁（SparkTraitsApi）；然后是冷却、亮度与已有的夜视，各有提示；恐惧最后检查，使其提示只在本可生效时
     * 出现。安全时间、饕餮眩晕以及搜寻者、隙行者与骑士决斗会话会在此之前丢弃数据包。
     */
    public static NightVisionVerdict nightVisionVerdict(boolean discoverer, boolean roundActive,
                                                        boolean playingAndAlive, boolean swallowed, boolean stunned,
                                                        boolean roleSkillBlocked, boolean ready, boolean darkEnough,
                                                        boolean alreadyActive, boolean feared) {
        if (!discoverer || !roundActive || !playingAndAlive || swallowed || stunned || roleSkillBlocked) {
            return NightVisionVerdict.REFUSE;
        }
        if (!ready) {
            return NightVisionVerdict.COOLING_DOWN;
        }
        if (!darkEnough) {
            return NightVisionVerdict.TOO_BRIGHT;
        }
        if (alreadyActive) {
            return NightVisionVerdict.ALREADY_ACTIVE;
        }
        return feared ? NightVisionVerdict.FEARED : NightVisionVerdict.USE;
    }

    /** Whether the effective light level allows the skill. / 有效亮度是否允许使用技能。 */
    public static boolean isDarkEnoughForNightVision(int light) {
        return light <= NIGHT_VISION_MAX_LIGHT;
    }

    /**
     * Whether a Night Vision the player already has (gin, the Savior's domain, a blackout grant) makes the skill
     * pointless: vanilla never shortens an effect, so an infinite one or one with at least the skill's length left
     * would swallow the grant while the cooldown was still spent.
     * 玩家已有的夜视（金酒、救世主领域、停电发放等）是否让本技能失去意义：原版从不缩短效果，因此无限时长或剩余时间不短于本技能的
     * 夜视会吞掉这次发放，冷却却照样消耗。
     */
    public static boolean nightVisionAlreadyCovers(boolean present, boolean infinite, int duration) {
        return present && (infinite || duration >= NIGHT_VISION_TICKS);
    }

    /** 60 s after the first activation of a round, 75 s after every later one. / 本局第一次激活后 60 秒，之后每次 75 秒。 */
    public static int nightVisionCooldownTicks(boolean usedThisRound) {
        return usedThisRound ? NIGHT_VISION_COOLDOWN_TICKS : NIGHT_VISION_INITIAL_COOLDOWN_TICKS;
    }

    /**
     * Stamina side gate: every server player, and on a client only the local player (whose HUD and sprint state this
     * stamina drives). / 体力端侧门槛：服务端的所有玩家；客户端只处理本地玩家（其 HUD 与疾跑状态由这份体力驱动）。
     */
    public static boolean tracksStaminaOnThisSide(boolean serverPlayer, boolean mainPlayer) {
        return serverPlayer || mainPlayer;
    }

    /**
     * Same gate as Wathe's own stamina tick (running round, alive and survival) plus the exact Discoverer role.
     * 与 Wathe 自身体力刻相同的门槛（对局进行中、存活且生存模式），外加身份恰为探索者。
     */
    public static boolean slowsSprintDrain(boolean running, boolean aliveAndSurvival, @Nullable Role role) {
        return running && aliveAndSurvival && isDiscoverer(role);
    }

    /**
     * Stamina after a sprinting tick, rewritten from Wathe's {@code max(start - 1, 0)} to {@code max(start - 0.75, 0)}.
     * {@code start} is the captured value after Wathe's own clamp (only when the cap shrank this tick). The drain is
     * read from Wathe's result ({@code start - current}), so another source that already scaled it (e.g. the
     * Orthopedist's Bone Setting) stacks multiplicatively; at Wathe's zero floor, where the clamp hides the drain, the
     * nominal {@link #WATHE_SPRINT_DRAIN_PER_TICK} is used. Never below {@code current}, so it only ever refunds.
     * 疾跑刻之后的体力：把 Wathe 的 {@code max(start - 1, 0)} 改写为 {@code max(start - 0.75, 0)}。{@code start} 为捕获值经
     * Wathe 自身上限截断之后的值（仅在本刻上限缩小时截断）。消耗量取自 Wathe 的结果（{@code start - current}），因此已被其他
     * 来源缩放过的消耗（如正骨师的正骨效果）按乘法叠加；在 Wathe 的零下限处，截断掩盖了消耗量，改用标称值
     * {@link #WATHE_SPRINT_DRAIN_PER_TICK}。结果绝不低于 {@code current}，因此只会返还体力。
     */
    public static float slowedSprintStamina(float previous, int previousMax, int maxSprintTime, float current) {
        if (!Float.isFinite(previous) || previous <= 0.0F || maxSprintTime <= 0 || !Float.isFinite(current)) {
            return current;
        }
        float start = previousMax > 0 && maxSprintTime < previousMax ? Math.min(previous, maxSprintTime) : previous;
        if (current >= start) {
            return current;
        }
        float drain = current > 0.0F ? start - current : WATHE_SPRINT_DRAIN_PER_TICK;
        float slowed = Math.max(start - drain * (float) SPRINT_DRAIN_MULTIPLIER, 0.0F);
        return Math.max(current, slowed);
    }
}
