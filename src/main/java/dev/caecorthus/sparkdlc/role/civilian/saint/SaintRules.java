package dev.caecorthus.sparkdlc.role.civilian.saint;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Pure Saint tuning and compatibility predicates (owner rebalance 2026-11-28: no immunity to good players, a passive
 * Hellfire on her killer, guns she may carry but never kill with).
 * 圣徒的纯数值与兼容判断集中在这里，运行时接线由外层模块负责（所有者 2026-11-28 重做：不再免疫好人击杀，业火改为
 * 作用于凶手的被动，可以持枪但无法用枪杀人）。
 */
public final class SaintRules {
    public static final int COLOR = 0xEEBC78;
    /**
     * Passive Hellfire: whoever kills the Saint burns for {@code coins / 50 + 5} seconds (integer division), counted on
     * the coins she held when she died.
     * 被动业火：击杀圣徒者承受 {@code 金币 / 50 + 5} 秒业火（整除），金币取她死亡时的余额。
     */
    public static final int HELLFIRE_BASE_SECONDS = 5;
    public static final int HELLFIRE_COINS_PER_SECOND = 50;
    /**
     * A player hit by the Saint's gunshot is not killed: rooted for 3 s (the Hunter trap's root, no fracture) and
     * glowing for 10 s.
     * 被圣徒枪击命中的玩家不会死亡：禁锢 3 秒（猎人捕兽夹的禁锢，不附带骨折）并发光 10 秒。
     */
    public static final int GUN_SHOT_ROOT_TICKS = 60;
    public static final int GUN_SHOT_GLOW_TICKS = 200;

    public static final Identifier SAINT_ROLE_ID = Identifier.of("sparkdlc", "saint");
    private static final Identifier GRAND_WITCH_ROLE_ID = Identifier.of("sparkdlc", "grand_witch");
    // These ID-only seams keep Saint rules independent from NoellesRoles implementation classes.
    // 这些仅依赖 ID 的接缝让圣徒规则不依赖 NoellesRoles 的实现类。
    public static final Identifier VOODOO_DEATH_REASON_ID = Identifier.of("noellesroles", "voodoo");
    public static final Identifier DEMON_HUNTER_SHOT_DEATH_REASON_ID =
            Identifier.of("noellesroles", "demon_hunter_shot");

    private SaintRules() {
    }

    public static boolean isSaint(@Nullable Role role) {
        return hasRoleId(role, SAINT_ROLE_ID);
    }

    /** The one role Hellfire never burns: the Grand Witch. / 业火唯一不会灼烧的职业：大魔女。 */
    static boolean isKarmaImmune(@Nullable Role role) {
        return hasRoleId(role, GRAND_WITCH_ROLE_ID);
    }

    /** Seconds of Hellfire a killer receives for a Saint holding {@code balance} coins. / 圣徒持有 {@code balance} 金币时凶手承受的业火秒数。 */
    public static int hellfireSeconds(int balance) {
        return Math.max(0, balance) / HELLFIRE_COINS_PER_SECOND + HELLFIRE_BASE_SECONDS;
    }

    public static int hellfireTicks(int balance) {
        return hellfireSeconds(balance) * 20;
    }

    public static int mergeCooldownTicks(int currentTicks, int requestedTicks) {
        return Math.max(currentTicks, requestedTicks);
    }

    public static boolean isBlockedDeathReason(@Nullable Identifier deathReason) {
        return VOODOO_DEATH_REASON_ID.equals(deathReason);
    }

    /**
     * The Saint's only remaining immunity: Voodoo. Kills by good players reach her like anyone else's.
     * 圣徒仅存的免疫：巫毒。好人的击杀与其他人的击杀一样对她生效。
     */
    public static boolean blocksKill(boolean saintVictim, @Nullable Identifier deathReason) {
        return saintVictim && isBlockedDeathReason(deathReason);
    }

    /**
     * Death reasons of a bullet: Wathe's {@code gun_shot}, which every gun in {@code wathe:guns} uses (revolver,
     * derringer, the Hunter's shotgun, the Serial Killer pistols, the Bandit revolver) and so do the rifles outside the
     * tag, and the NoellesRoles Demon Hunter pistol's own reason. A kill with one of these credited to a Saint is her
     * gunshot.
     * 子弹的死因：Wathe 的 {@code gun_shot}（{@code wathe:guns} 中的每把枪都用它：左轮、德林加、猎人猎枪、连环杀手手枪、
     * 强盗改装左轮，标签之外的步枪同样如此），以及 NoellesRoles 猎魔枪自己的死因。记在圣徒名下的这类击杀就是她的枪击。
     */
    public static boolean isGunDeathReason(@Nullable Identifier deathReason) {
        return GameConstants.DeathReasons.GUN.equals(deathReason)
                || DEMON_HUNTER_SHOT_DEATH_REASON_ID.equals(deathReason);
    }

    private static boolean hasRoleId(@Nullable Role role, Identifier id) {
        return role != null && id.equals(role.identifier());
    }
}
