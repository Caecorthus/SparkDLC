package dev.caecorthus.sparkdlc.bridge.strength;

import dev.caecorthus.sparkdlc.faction.api.FactionCapabilities;
import dev.caecorthus.sparkdlc.faction.api.PlayerAffectPolicy;
import dev.caecorthus.sparkdlc.faction.api.PoliceRoles;
import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.round.settlement.SettlementExclusions;
import dev.caecorthus.sparkdlc.role.jiahao.corruptcop.CorruptCopRules;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Predicate;

/** Public faction API bridges. The modules are fused in SparkDLC, so every call here is a direct, compiled call.
 *  公开阵营 API 桥接。各模块已融合进 SparkDLC，因此这里的每个调用都是直接的编译期调用。 */
public final class SparkFactionCompat {
    /** SparkWitch's witch faction id (Grand Witch, Accomplice, Curser). / SparkWitch 魔女阵营 id。 */
    public static final Identifier WITCH_FACTION_ID = Identifier.of("sparkdlc", "witch");
    /** SparkWitch's Team Jiahao (Insider, NoellesRoles Corrupt Cop). / SparkWitch 嘉豪阵营 id（内应、黑警）。 */
    public static final Identifier JIAHAO_FACTION_ID = Identifier.of("sparkdlc", "jiahao");

    private SparkFactionCompat() {
    }

    /**
     * Registers a veto with SparkFactionAPI's player-affect policies: {@code action} from any actor is denied on targets
     * {@code protectedTarget} accepts. The modules are fused in SparkDLC, so the policy is a compiled
     * {@link PlayerAffectPolicy} registered directly and this always returns true.
     * 向 SparkFactionAPI 的玩家影响策略注册一条否决：任何人对 protectedTarget 认可的目标执行 action 都会被拒绝。
     * 各模块已融合进 SparkDLC，因此策略是直接注册的编译期 {@link PlayerAffectPolicy}，本方法恒返回 true。
     */
    public static boolean denyAction(Identifier action, Predicate<PlayerEntity> protectedTarget) {
        SparkFactionApi.registerPlayerAffectPolicy(new DenyActionPolicy(action, protectedTarget));
        return true;
    }

    public static boolean canAffectPlayer(PlayerEntity actor, PlayerEntity target, Identifier action) {
        return SparkFactionApi.canAffectPlayer(actor, target, action, GameWorldComponent.KEY.get(actor.getWorld()));
    }

    /** Police tablet-network classification only: this does not change NoellesRoles allocation or faction.
     *  仅注册义警平板网络分类，不改变 NoellesRoles 的身份分配或阵营。 */
    public static boolean registerCorruptCopPoliceRole() {
        PoliceRoles.register(CorruptCopRules.CORRUPT_COP_ID);
        return true;
    }

    /** Never null for a non-null role now that the modules are fused in SparkDLC and {@code PoliceRoles} is called
     *  directly; the nullable return type stays for the callers, which keep their legacy fallback rule.
     *  各模块已融合进 SparkDLC 并直接调用 {@code PoliceRoles}，非 null 身份不再返回 null；为调用方保留可空返回类型，
     *  调用方仍保留旧版回退规则。
     *  The registry is read on every call so registrations made later remain visible.
     *  每次调用都读取注册表，确保稍后注册的身份立即可见。 */
    public static @Nullable Boolean isPoliceRole(@Nullable Role role) {
        if (role == null) {
            return false;
        }
        // Idempotent: Corrupt Cop is registered before the first classification, whatever the initialization order.
        // 幂等：无论初始化顺序如何，黑警都会在首次分类前完成注册。
        registerCorruptCopPoliceRole();
        return PoliceRoles.contains(role.identifier());
    }

    /** Role-only base faction, deterministic on client and server; null only for a null role, now that the modules
     *  are fused in SparkDLC and the API is called directly.
     *  仅由身份决定的基础阵营，客户端与服务端结果一致；各模块已融合进 SparkDLC 并直接调用该 API，仅当身份为 null 时为 null。 */
    public static @Nullable Identifier baseFaction(@Nullable Role role) {
        if (role == null) {
            return null;
        }
        return SparkFactionApi.resolveBaseFaction(role);
    }

    /** The witch faction is declared through SparkFactionAPI, so its base faction id identifies every witch role.
     *  魔女阵营经 SparkFactionAPI 声明，因此基础阵营 id 即可识别所有魔女身份。 */
    public static boolean isWitchFactionRole(@Nullable Role role) {
        return WITCH_FACTION_ID.equals(baseFaction(role));
    }

    /**
     * Whether the role keeps SparkStrength's "neutral" perks (neutral master key train doors, the Coroner's borrowed
     * neutral key). SparkFactionAPI's native Wathe factions move the witch faction and Team Jiahao off Wathe NEUTRAL,
     * so {@code isNeutral()} turns false for them; {@code SparkFactionApi.isNeutralAligned(Role)} still says true
     * (owner D3). The modules are fused in SparkDLC, so that method is called directly and there is no
     * {@code role.isNeutral()} fallback any more. Role-only, so client and server agree.
     * 该身份是否保留 SparkStrength 的“中立”待遇（中立万能钥匙开列车门、验尸官借用中立钥匙）。SparkFactionAPI 的原生
     * Wathe 阵营让魔女阵营与嘉豪阵营离开 Wathe NEUTRAL，其 isNeutral() 变为 false；SparkFactionApi.isNeutralAligned(Role)
     * 仍返回 true（所有者 D3）。各模块已融合进 SparkDLC，因此直接调用该方法，不再有 role.isNeutral() 回退。
     * 只看身份，客户端与服务端结果一致。
     */
    public static boolean isNeutralAligned(@Nullable Role role) {
        return role != null && SparkFactionApi.isNeutralAligned(role);
    }

    /**
     * Whether the player's effective faction declares an instinct key ({@code FactionCapabilities.canUseInstinct}):
     * Wathe killers, SparkWitch's witch faction and the Murderous Witch today. Per faction only, so role-specific keys
     * (Insider, Wind Spirit, ...) are not covered. Direct calls into SparkFactionAPI since the SparkDLC fusion.
     * 玩家的有效阵营是否声明了本能键（FactionCapabilities.canUseInstinct）：目前为 Wathe 杀手、SparkWitch 魔女阵营与杀意魔女。
     * 只按阵营判断，因此不覆盖职业专属按键（内应、风精灵……）。融合进 SparkDLC 后直接调用 SparkFactionAPI。
     */
    public static boolean canUseInstinct(PlayerEntity player, GameWorldComponent game) {
        Identifier faction = SparkFactionApi.resolveEffectiveFaction(player, game);
        FactionCapabilities capabilities = faction == null ? null : SparkFactionApi.capabilities(faction);
        return capabilities != null && capabilities.canUseInstinct();
    }

    /**
     * Whether SparkFactionAPI's {@code SettlementExclusions} says this player never counts as a living player for round
     * settlement (SparkWitch's Black Raven). It reads the raw role, never {@code isRole}, and checks no liveness:
     * callers keep their own alive test. The modules are fused in SparkDLC, so the registry is asked directly.
     * SparkFactionAPI 的 SettlementExclusions 是否判定该玩家在回合结算中不算存活玩家（SparkWitch 的黑羽鸦）。
     * 读取原始身份而非 isRole，且不判断存活，调用方保留自己的存活判断。各模块已融合进 SparkDLC，因此直接询问该注册表。
     */
    public static boolean isSettlementExcluded(@Nullable GameWorldComponent game, @Nullable UUID player) {
        return game != null && player != null && SettlementExclusions.isExcluded(game, player);
    }

    /**
     * The veto {@link #denyAction} registers: every action passes except {@code action} on a target
     * {@code protectedTarget} accepts. A plain class, so equality stays object identity like the proxy it replaces.
     * {@link #denyAction} 注册的否决：除了对 {@code protectedTarget} 认可的目标执行 {@code action} 之外一律放行。
     * 使用普通类，相等性与它所取代的代理一样是对象身份。
     */
    private static final class DenyActionPolicy implements PlayerAffectPolicy {
        private final Identifier action;
        private final Predicate<PlayerEntity> protectedTarget;

        private DenyActionPolicy(Identifier action, Predicate<PlayerEntity> protectedTarget) {
            this.action = action;
            this.protectedTarget = protectedTarget;
        }

        @Override
        public boolean canAffect(PlayerEntity actor, PlayerEntity target, Identifier actionId,
                                 GameWorldComponent gameComponent) {
            return !(action.equals(actionId) && target != null && protectedTarget.test(target));
        }

        @Override
        public String toString() {
            return "SparkStrength deny " + action;
        }
    }
}
