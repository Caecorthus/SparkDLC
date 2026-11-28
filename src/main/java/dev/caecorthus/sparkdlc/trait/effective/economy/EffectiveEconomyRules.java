package dev.caecorthus.sparkdlc.trait.effective.economy;

import dev.caecorthus.sparkdlc.trait.effective.alignment.EffectiveAlignment;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

import java.util.Collection;

/** Effective economy and reward policy created by alignment-flipping traits.
 *  阵营翻转天赋产生的有效经济与奖励规则。 */
public final class EffectiveEconomyRules {
    public static final int TASK_MONEY_REWARD = 50;
    /** Owner 2026-11-28 (nerf): an Impostor's kill pays 50, no longer Wathe's killer reward
     *  ({@code GameConstants.MONEY_PER_KILL}, 100). The Depression fake-death half share follows this value.
     *  所有者 2026-11-28（削弱）：内鬼击杀奖励为 50，不再沿用 Wathe 的杀手击杀奖励
     *  （{@code GameConstants.MONEY_PER_KILL}，100）。抑郁假死的半额奖励随此值变化。 */
    public static final int IMPOSTOR_KILL_REWARD = 50;

    private EffectiveEconomyRules() {
    }

    /**
     * Grants the Conscience/Impostor +50 task money. Owner rule: it stacks on top of any role-owned task income
     * (NoellesRoles Bartender, this mod's Detective income, ...), so the role never suppresses it.
     * 发放善良/内鬼的 +50 任务金币。所有者规则：与职业自带的任务收入（NoellesRoles 酒保、本模组的侦探收入等）叠加，
     * 职业本身不会取消该奖励。
     */
    public static boolean shouldRewardTaskMoney(Role role, Collection<Identifier> traits) {
        return traits != null
                && (EffectiveAlignment.hasConscience(traits) || EffectiveAlignment.hasImpostor(traits));
    }

    /** Roles whose task money NoellesRoles pays itself; only Money Tree eligibility reads this, never the trait payout.
     *  由 NoellesRoles 自行发放任务金币的职业；仅用于摇钱树资格判定，不参与词条任务金币发放。 */
    public static boolean hasNativeTaskMoneyReward(Role role) {
        return role != null
                && (role.equals(Noellesroles.BARTENDER)
                || role.equals(Noellesroles.RECALLER)
                || role.equals(Noellesroles.TIMEKEEPER)
                || role.equals(Noellesroles.REPORTER)
                || role.equals(Noellesroles.WAITER));
    }

    /** Lets Party Animal reward any non-self target; NoellesRoles keeps self-buzz and repeat-level gates.
     *  允许派对狂对任意非自己目标发放变声奖励；自变声和重复等级限制仍由 NoellesRoles 原逻辑处理。 */
    public static boolean shouldBlockPartyAnimalTargetReward(Role targetRole, Collection<Identifier> targetTraits) {
        return false;
    }

    public static boolean shouldRewardConscienceKill(Role victimRole, Collection<Identifier> victimTraits) {
        return victimRole != null && !EffectiveAlignment.isEffectiveCivilian(victimRole, victimTraits);
    }

    /** Rewards Impostors for killing public non-killers, including neutral roles.
     *  内鬼击杀公开非杀手时获得击杀奖励，包含好人与中立角色。 */
    public static boolean shouldRewardImpostorKill(Role victimRole, Collection<Identifier> victimTraits) {
        return victimRole != null
                && victimRole != WatheRoles.NO_ROLE
                && !EffectiveAlignment.isEffectiveKiller(victimRole, victimTraits);
    }

    public static int impostorKillReward(Role victimRole, Collection<Identifier> victimTraits, boolean canAccessShop) {
        return canAccessShop && shouldRewardImpostorKill(victimRole, victimTraits) ? IMPOSTOR_KILL_REWARD : 0;
    }

    /** Keeps Wathe's original killer kill reward away from Conscience killers.
     *  防止善良杀手继续获得 Wathe 原始杀手击杀奖励。 */
    public static boolean shouldReceiveOriginalKillerReward(boolean canUseKillerFeatures, boolean hasConscience) {
        return canUseKillerFeatures && !hasConscience;
    }

}
