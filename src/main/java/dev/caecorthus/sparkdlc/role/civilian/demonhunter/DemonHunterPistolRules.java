package dev.caecorthus.sparkdlc.role.civilian.demonhunter;

import dev.doctor4t.wathe.api.Role;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Pure decisions of the Demon Hunter pistol rework (owner spec 2026-11-28). The pistol is NoellesRoles'
 * {@code noellesroles:demon_hunter_pistol}; "frenzy" is Wathe's psycho mode of any kind (a player with psycho ticks
 * left), not only the public one {@code GameWorldComponent#isPsychoActive} counts.
 * <ul>
 *     <li>A Demon Hunter carries the pistol for the whole round, empty until a frenzy starts (NoellesRoles then adds
 *     2 bullets to a carried pistol). Nothing takes it back before the round reset: not the last bullet, not the end
 *     of the last frenzy.</li>
 *     <li>While anyone is frenzied the pistol never goes on cooldown; a shot fired with nobody frenzied (leftover
 *     bullets) keeps NoellesRoles' 10 s.</li>
 *     <li>A shot that hits a frenzied player costs no bullet (spent, then given back), whoever fires it.</li>
 *     <li>For a Demon Hunter the pistol alone is no "already has a gun" when picking a gun up from the ground.</li>
 * </ul>
 * 猎魔枪重做（所有者 2026-11-28）的纯规则。枪是 NoellesRoles 的 {@code noellesroles:demon_hunter_pistol}；“疯魔”指任何一种
 * Wathe 疯魔（玩家还有疯魔 tick），不只是 {@code GameWorldComponent#isPsychoActive} 统计的公开疯魔。
 * <ul>
 *     <li>猎魔人整局都带着猎魔枪，有人进入疯魔前是空枪（之后 NoellesRoles 给随身的枪加 2 发）。回合重置之前没有任何东西会把它
 *     收走：打光最后一发不会，最后一段疯魔结束也不会。</li>
 *     <li>只要有人处于疯魔，猎魔枪就不进冷却；没人疯魔时用剩余子弹开的枪保留 NoellesRoles 的 10 秒。</li>
 *     <li>打中疯魔玩家的那一枪不耗子弹（先扣再还），不论开枪的是谁。</li>
 *     <li>对猎魔人来说，从地上捡枪时只带着猎魔枪不算“已经有枪”。</li>
 * </ul>
 */
public final class DemonHunterPistolRules {
    /** Bullets in the pistol handed out with the role. / 随职业发放的猎魔枪里的子弹数。 */
    public static final int ISSUED_BULLETS = 0;

    /**
     * NoellesRoles tooltip lines the rework made wrong ("CD 10s", "hit the Jester to reload", "vanishes when all
     * frenzies end"), each with the mod's own key. An upstream key is never redefined (its value would depend on the
     * namespace order, see {@code util.UpstreamTextKeys}); the call site is pointed at the mod's key instead.
     * 重做后不再正确的 NoellesRoles 提示行（“冷却 10 秒”“命中小丑自动装填”“疯魔结束后消散”）及其对应的本模组键。上游的键从不
     * 重新定义（生效的值取决于命名空间顺序，见 {@code util.UpstreamTextKeys}），而是把调用点指向本模组的键。
     */
    private static final Map<String, String> TOOLTIP_KEYS = Map.of(
            "tip.noellesroles.demon_hunter_pistol.usage", "tip.sparkdlc.demon_hunter_pistol.usage",
            "tip.noellesroles.demon_hunter_pistol.jester", "tip.sparkdlc.demon_hunter_pistol.refund",
            "tip.noellesroles.demon_hunter_pistol.expire", "tip.sparkdlc.demon_hunter_pistol.keep"
    );

    private DemonHunterPistolRules() {
    }

    /**
     * Whether a player of this (raw) role keeps the pistol all round. Only the Demon Hunter: a Coroner's loaned
     * pistol and a pistol in anyone else's hands keep NoellesRoles' and the Coroner's own lifecycle.
     * 该（原始）职业的玩家是否整局保留猎魔枪。只有猎魔人：验尸官借来的枪以及其他人手里的枪仍按 NoellesRoles 与验尸官各自的
     * 生命周期处理。
     */
    public static boolean keepsPistol(@Nullable Role role) {
        return DemonHunterSniffRules.isDemonHunter(role);
    }

    /**
     * Whether a hit gives its bullet back: the target was frenzied at the hit. A Jester target is left to
     * NoellesRoles' own Jester branch (which refunds a Jester it kills), so one shot never returns two bullets.
     * 这次命中是否返还子弹：目标在命中时处于疯魔。小丑目标交给 NoellesRoles 自己的小丑分支（击杀小丑时返还），因此一枪绝不会
     * 返还两发。
     */
    public static boolean refundsBullet(boolean targetFrenzied, boolean targetIsJester) {
        return targetFrenzied && !targetIsJester;
    }

    /**
     * Whether the pistol that just fired its last bullet stays in the inventory with 0 bullets instead of being
     * removed: always for a Demon Hunter, and for anyone when the bullet is about to be refunded (the refund then
     * goes back into the very stack that fired, so a Coroner's loan keeps its loan mark).
     * 刚打出最后一发的猎魔枪是否以 0 发留在背包里而不被移除：猎魔人总是如此；子弹即将返还时任何人都如此（返还的子弹回到开枪的
     * 那一把里，验尸官借来的枪因此保留借出标记）。
     */
    public static boolean keepsEmptiedPistol(boolean shooterKeepsPistol, boolean bulletRefunded) {
        return shooterKeepsPistol || bulletRefunded;
    }

    /**
     * Whether a cooldown NoellesRoles is about to put on the pistol (10 s after a shot, 5 s on a pistol it hands out
     * at a frenzy start) applies: only while nobody is frenzied.
     * NoellesRoles 即将给猎魔枪加的冷却（开枪后 10 秒、疯魔开始时新发的枪 5 秒）是否生效：只在没有人疯魔时生效。
     */
    public static boolean coolsDown(boolean anyoneFrenzied) {
        return !anyoneFrenzied;
    }

    /**
     * Whether, for this ground pickup, the picker's pistols are left out of "already has a gun": the picker is a
     * Demon Hunter and the stack on the ground is a gun other than the pistol (a second pistol is still refused).
     * 这次地面拾取中，拾取者身上的猎魔枪是否不计入“已经有枪”：拾取者是猎魔人，且地上的是猎魔枪以外的枪（第二把猎魔枪仍然拒绝）。
     */
    public static boolean ignoresPistolForPickup(boolean pickerKeepsPistol, boolean groundIsGun,
                                                 boolean groundIsPistol) {
        return pickerKeepsPistol && groundIsGun && !groundIsPistol;
    }

    /** The mod's key for a pistol tooltip line, or the key itself. / 猎魔枪提示行对应的本模组键；没有则原样返回。 */
    public static String tooltipKey(String key) {
        return TOOLTIP_KEYS.getOrDefault(key, key);
    }
}
