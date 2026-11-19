package dev.caecorthus.sparkdlc.witch.compat;

import dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/** Public-facade queries. SparkTraits is fused into SparkDLC, so each call is direct (compiled) instead of reflective;
 * only the public facade {@code SparkTraitsApi} is named, never Traits' internal packages.
 * 公共门面查询。SparkTraits 已融合进 SparkDLC，各调用均为直接（编译期）调用而非反射；只引用公共门面
 * {@code SparkTraitsApi}，从不引用 Traits 的内部包。 */
public final class SparkTraitsKillerBridge {
    private SparkTraitsKillerBridge() {
    }

    /** Hot (every player, every server tick, from {@code PlayerHitboxHistory}): a plain static call.
     * 热路径（{@code PlayerHitboxHistory} 每个服务端 tick 对每名玩家调用）：普通静态调用。 */
    public static boolean isLastEscapeActive(PlayerEntity player) {
        return player != null && SparkTraitsApi.isLastEscapeActive(player);
    }

    public static boolean isKillerInteractionBlocked(PlayerEntity player) {
        return player != null && SparkTraitsApi.isKillerInteractionBlocked(player);
    }

    public static int getForcedMeleeCooldownTicks(PlayerEntity player, ItemStack weapon) {
        return player == null ? 0 : Math.max(0, SparkTraitsApi.getForcedMeleeCooldownTicks(player, weapon));
    }

    /** New action gate, not a kill-source filter: emitted projectiles and marks keep resolving.
     * 仅拦截新动作，不过滤击杀来源；已发出的投射物与标记继续结算。 */
    public static boolean blocksWeaponAction(PlayerEntity player, ItemStack weapon) {
        return isKillerInteractionBlocked(player) || getForcedMeleeCooldownTicks(player, weapon) > 0;
    }

    /** Call only after target/weapon eligibility, immediately before real melee side effects.
     * 仅在目标及武器资格验证后、实际近战副作用前调用。 */
    public static boolean shouldCancelMeleeAttack(ServerPlayerEntity attacker, ServerPlayerEntity victim,
                                                 ItemStack weapon) {
        return SparkTraitsApi.shouldCancelMeleeAttack(attacker, victim, weapon);
    }

    public static float getLastEscapeDesaturation(PlayerEntity player) {
        if (player == null) {
            return 0.0F;
        }
        float factor = SparkTraitsApi.getLastEscapeDesaturation(player);
        return Float.isFinite(factor) ? Math.clamp(factor, 0.0F, 1.0F) : 0.0F;
    }

    /** Last Escape or silenced effective killer; SparkWitch packets must ask themselves.
     * 脱险或被沉默的有效杀手；SparkWitch 自有数据包必须自行查询。 */
    public static boolean isRoleSkillBlocked(PlayerEntity player) {
        return player != null && SparkTraitsApi.isRoleSkillBlocked(player);
    }

    /** Whether Traits' Last Stand intercepted this death; a null player answers false (not intercepted). Direct call:
     * SparkTraits is fused into SparkDLC.
     * Traits 背水一战是否拦截了这次死亡；玩家为空时返回 false（未拦截）。SparkTraits 已融合进 SparkDLC，此处为直接调用。 */
    public static boolean isLastStandDeathIntercepted(PlayerEntity player) {
        return player != null && SparkTraitsApi.isLastStandDeathIntercepted(player);
    }

    /** Makes a reason skip Traits revive/counter hooks; returns false only for a null reason (SparkTraits is fused
     * into SparkDLC, so the registration itself is a direct call).
     * 使该死亡原因跳过 Traits 复活/反击钩子；仅在原因为空时返回 false（SparkTraits 已融合进 SparkDLC，登记为直接调用）。 */
    public static boolean registerTerminalDeathReason(Identifier reason) {
        if (reason == null) {
            return false;
        }
        SparkTraitsApi.registerTerminalDeathReason(reason);
        return true;
    }

    /** Whether a reason is registered SparkTraits-terminal (an unprotectable penalty); a null reason answers false.
     * Direct call: SparkTraits is fused into SparkDLC.
     * 该死亡原因是否已注册为 SparkTraits 终结原因（不可保护的惩罚）；原因为空时返回 false。SparkTraits 已融合进 SparkDLC，
     * 此处为直接调用。 */
    public static boolean isTerminalDeathReason(Identifier reason) {
        return reason != null && SparkTraitsApi.isTerminalDeathReason(reason);
    }

    /** Writes an exact cooldown past Traits modifiers, performing the vanilla set itself; on false (only a null
     * player or item now that SparkTraits is fused into SparkDLC) the caller must fall back to
     * {@code ItemCooldownManager.set}.
     * 越过 Traits 冷却倍率写入精确冷却（其内部自行完成原版写入）；返回 false 时（SparkTraits 已融合进 SparkDLC，现仅在玩家
     * 或物品为空时出现）调用方需回退到原版冷却写入。 */
    public static boolean setExactItemCooldownRemaining(ServerPlayerEntity player, Item item, int ticks) {
        if (player == null || item == null) {
            return false;
        }
        SparkTraitsApi.setExactItemCooldownRemaining(player, item, Math.max(0, ticks));
        return true;
    }

    /** Exact remaining ticks of an item cooldown; 0 when the item is not cooling or an argument is null.
     *  物品冷却的精确剩余刻数；未在冷却或参数为空时为 0。 */
    public static int getItemCooldownRemaining(PlayerEntity player, Item item) {
        if (player == null || item == null) {
            return 0;
        }
        return Math.max(0, SparkTraitsApi.getItemCooldownRemaining(player, item));
    }

    /** Whether Traits turned this attacker's hit into a non-final kill (e.g. a Depression fake death).
     * Traits 是否把该攻击者的这一击变为非最终击杀（如抑郁假死）。 */
    public static boolean isNonFinalKillPending(ServerPlayerEntity victim, ServerPlayerEntity attacker) {
        return victim != null && attacker != null && SparkTraitsApi.isNonFinalKillPending(victim, attacker);
    }

    /** Cooldown a weapon keeps after a non-final kill; never longer than the original.
     * 非最终击杀后武器保留的冷却；不会超过原冷却。 */
    public static int nonFinalKillCooldownTicks(int originalTicks) {
        int original = Math.max(0, originalTicks);
        return Math.min(original, Math.max(0, SparkTraitsApi.getNonFinalKillCooldownTicks(original)));
    }

    /** Runs a kill that happens away from the attacker's hands, naming the weapon Traits should discount if the
     * kill turns non-final. The kill always runs exactly once: Traits runs it inside its weapon-hint scope (a null
     * weapon runs it without a hint), and whatever it throws surfaces unchanged.
     * 执行不在攻击者手中发生的击杀，并告知 Traits 若成为非最终击杀应缩短哪件武器的冷却；击杀恰好执行一次：Traits 在其武器提示
     * 作用域内执行它（武器为空时不带提示直接执行），击杀抛出的异常原样抛出。 */
    public static void runWithNonFinalKillWeapon(Item weapon, Runnable kill) {
        SparkTraitsApi.runWithNonFinalKillWeapon(weapon, kill);
    }
}
