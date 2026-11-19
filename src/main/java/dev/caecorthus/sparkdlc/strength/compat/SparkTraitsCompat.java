package dev.caecorthus.sparkdlc.strength.compat;

import dev.caecorthus.sparkdlc.strength.role.economy.KillerTeamEconomyRules;
import dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi;
import dev.doctor4t.wathe.util.ShopEntry;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * SparkTraits bridge. The modules are fused in SparkDLC, so every query calls the public facade
 * {@link SparkTraitsApi} directly; SparkStrength still reaches SparkTraits through that facade only.
 * SparkTraits 桥接。各模块已融合进 SparkDLC，因此每个查询都直接调用公开门面 {@link SparkTraitsApi}；
 * SparkStrength 仍然只经由该门面访问 SparkTraits。
 */
public final class SparkTraitsCompat {
    private static final Identifier CONSCIENCE_ID = Identifier.of("sparktraits", "conscience");
    private static final Identifier IMPOSTOR_ID = Identifier.of("sparktraits", "impostor");
    private static final Identifier TEAM_FIRST_ID = Identifier.of("sparktraits", "team_first");

    private SparkTraitsCompat() {
    }

    public static boolean hasImpostor(PlayerEntity player) {
        return hasTrait(player, IMPOSTOR_ID);
    }

    /**
     * 计时员普通模式的目标判断：原始好人和善良杀手可以被刷新，内鬼好人不能被刷新。
     * 该规则独立于通用“有效阵营”判断，避免改动其它职业已经依赖的优先级。
     */
    public static boolean isTimekeeperNormalTarget(Role role, PlayerEntity player) {
        return role != null
                && !hasImpostor(player)
                && (role.isInnocent() || hasConscience(player));
    }

    /** 计时员拥有 impostor 时的反转目标：非善良杀手和其它内鬼好人。 */
    public static boolean isTimekeeperReverseTarget(Role role, PlayerEntity player) {
        return role != null
                && !hasConscience(player)
                && (role.canUseKiller() || hasImpostor(player));
    }

    /**
     * 判断玩家是否属于 SparkTraits 规则下的有效杀手阵营。
     *
     * <p>规则与 SparkTraits 的公开有效阵营实现保持一致：内鬼词条会把原始好人
     * 翻为杀手；善良词条会把原始杀手翻为好人。若两个词条同时存在，则按
     * SparkTraits 的优先级由内鬼词条负责判定为杀手。</p>
     */
    public static boolean isEffectiveKiller(Role role, PlayerEntity player) {
        if (role == null) {
            return false;
        }
        if (hasImpostor(player)) {
            return true;
        }
        if (hasConscience(player)) {
            return false;
        }
        return role.canUseKiller();
    }

    /**
     * 判断玩家是否持有指定的 SparkTraits 词条。
     *
     * <p>这里只调用 SparkTraits 公开门面，避免 SparkStrength 直接依赖 SparkTraits 的内部实现包。
     * 各模块已融合进 SparkDLC，因此是对 {@code SparkTraitsApi.hasActiveTrait} 的直接调用。
     * Only the public facade is used, so SparkStrength never depends on SparkTraits' internal packages; the modules
     * are fused in SparkDLC, so this is a direct call to {@code SparkTraitsApi.hasActiveTrait}.</p>
     */
    public static boolean hasTrait(PlayerEntity player, Identifier traitId) {
        return player != null && traitId != null && SparkTraitsApi.hasActiveTrait(player, traitId);
    }

    /** Owner-synced like Impostor, so client shop listings and server validation agree.
     *  与内鬼一样同步给本人，保证客户端商店列表与服务端校验一致。 */
    public static boolean hasConscience(PlayerEntity player) {
        return player != null && SparkTraitsApi.hasActiveTrait(player, CONSCIENCE_ID);
    }

    /**
     * Always true now that the modules are fused in SparkDLC and the trait facade is a compiled call: the shared
     * economy can no longer be switched off by an installed but unreadable trait API. Kept for its callers.
     * 各模块已融合进 SparkDLC、词条门面为编译期调用，因此恒为 true：团队经济不会再因“已安装但公开 API 不可读”而关闭。
     * 为调用方保留。
     */
    public static boolean isTeamEconomyAvailable() {
        return true;
    }

    public static boolean isGenuineKillerTeamMember(PlayerEntity player, boolean killerRole) {
        if (player == null) {
            return false;
        }
        boolean conscience = SparkTraitsApi.hasActiveTrait(player, CONSCIENCE_ID);
        boolean impostor = SparkTraitsApi.hasActiveTrait(player, IMPOSTOR_ID);
        return KillerTeamEconomyRules.isGenuineKiller(killerRole, impostor, conscience);
    }

    public static boolean hasTeamFirst(PlayerEntity player) {
        return player != null && SparkTraitsApi.hasActiveTrait(player, TEAM_FIRST_ID);
    }

    public static boolean isKillerInteractionBlocked(PlayerEntity player) {
        return player != null && SparkTraitsApi.isKillerInteractionBlocked(player);
    }

    public static int getForcedMeleeCooldownTicks(PlayerEntity player, ItemStack weapon) {
        if (player == null || weapon == null) {
            return 0;
        }
        return Math.max(0, SparkTraitsApi.getForcedMeleeCooldownTicks(player, weapon));
    }

    /** Check phase before weapon locks; both are direct SparkTraits facade calls since the SparkDLC fusion.
     *  先检查脱险交互禁用，再检查武器锁；融合进 SparkDLC 后两者都是对 SparkTraits 门面的直接调用。 */
    public static boolean isMeleeActionBlocked(PlayerEntity player, ItemStack weapon) {
        return isKillerInteractionBlocked(player) || getForcedMeleeCooldownTicks(player, weapon) > 0;
    }

    /** Call only after normal role, range, target permission and charge checks, before any costs.
     *  仅在职业、距离、目标权限及次数校验后、消耗前调用；反制由服务端 Traits 结算。 */
    public static boolean shouldCancelMeleeAttack(
            ServerPlayerEntity attacker, ServerPlayerEntity victim, ItemStack weapon
    ) {
        if (attacker == null || victim == null || weapon == null) {
            return false;
        }
        return isMeleeActionBlocked(attacker, weapon)
                || SparkTraitsApi.shouldCancelMeleeAttack(attacker, victim, weapon);
    }

    /** Preserve the provider wrapper: its marker prevents a second purchase discount.
     *  保留提供方包装对象：其标记防止购买时重复折扣。 */
    public static ShopEntry discountShopEntryForCharisma(PlayerEntity player, ShopEntry entry) {
        if (player == null || entry == null) {
            return entry;
        }
        ShopEntry discounted = SparkTraitsApi.discountShopEntryForCharisma(player, entry);
        return discounted != null ? discounted : entry;
    }

    /** Self-timed throw charges only: SparkTraits already scales launch speed, so never multiply it here.
     *  A non-positive answer keeps the base charge. Direct facade call since the SparkDLC fusion.
     *  仅用于自行计时的投掷蓄力：初速已由 SparkTraits 统一放大，此处不得再乘。
     *  非正数结果保持基础蓄力。融合进 SparkDLC 后为对门面的直接调用。 */
    public static int getThrowChargeTicks(PlayerEntity player, int baseTicks) {
        if (player == null) {
            return baseTicks;
        }
        int ticks = SparkTraitsApi.getThrowChargeTicks(player, baseTicks);
        return ticks > 0 ? ticks : baseTicks;
    }

    /** Reads SparkTraits' world-synced Final Moment flag; usable on clients. Direct facade call since the fusion.
     *  读取 SparkTraits 同步到世界的终局时刻标记，客户端可用；融合进 SparkDLC 后为对门面的直接调用。 */
    public static boolean isFinalMomentActive(World world) {
        return world != null && SparkTraitsApi.isFinalMomentActive(world);
    }

    /**
     * 通知 SparkTraits 重新同步目标的词条可见性。
     *
     * <p>调试存活命令只修改 Wathe 的 deadPlayers 集合，不会触发 Wathe 正常死亡事件；
     * 因此这里通过 SparkTraits 公共 API 请求一次组件同步。各模块已融合进 SparkDLC，这是对
     * {@code SparkTraitsApi.syncTraitVisibility} 的直接调用。
     * The debug alive command only edits Wathe's deadPlayers set and fires no Wathe death event, so this asks
     * SparkTraits' public API for one component sync; a direct call since the SparkDLC fusion.</p>
     */
    public static void syncTraitVisibility(ServerPlayerEntity player) {
        if (player != null) {
            SparkTraitsApi.syncTraitVisibility(player);
        }
    }

    /**
     * 请求 SparkTraits 创建调试用死亡词条快照。
     *
     * <p>真实死亡后的 RoleNameRenderer 使用世界级 deathTraitSnapshots 显示词条；
     * 该方法让 SparkStrength 的调试状态转换复用同一显示数据源，但不触发真实死亡流程。
     * 各模块已融合进 SparkDLC，这是对 {@code SparkTraitsApi.snapshotDeathTraitsForDebug} 的直接调用。
     * Lets SparkStrength's debug state changes reuse the death trait snapshot the RoleNameRenderer shows, without a
     * real death; a direct call since the SparkDLC fusion.</p>
     */
    public static void snapshotDeathTraitsForDebug(ServerPlayerEntity player) {
        if (player != null) {
            SparkTraitsApi.snapshotDeathTraitsForDebug(player);
        }
    }

    /**
     * 判断玩家是否属于“有效好人阵营”。
     *
     * <p>规则优先级和 SparkTraits 保持一致：
     * 善良词条优先视作好人；内鬼词条优先视作杀手；没有相关词条时回退到
     * Wathe 原始角色阵营。</p>
     */
    public static boolean isEffectiveCivilian(Role role, PlayerEntity player) {
        if (role == null) {
            return false;
        }
        if (hasConscience(player)) {
            return true;
        }
        if (hasImpostor(player)) {
            return false;
        }
        return role.isInnocent();
    }
}
