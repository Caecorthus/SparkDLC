package dev.caecorthus.sparkdlc.trait.api;

import dev.caecorthus.sparkdlc.skill.api.SparkRoleApi;
import dev.caecorthus.sparkdlc.trait.component.TraitIdCodec;
import dev.caecorthus.sparkdlc.trait.component.TraitPlayerComponent;
import dev.caecorthus.sparkdlc.trait.component.TraitWorldComponent;
import dev.caecorthus.sparkdlc.trait.assignment.TraitRoleChangeRevalidation;
import dev.caecorthus.sparkdlc.trait.presentation.OwnerInventoryPresentation;
import dev.caecorthus.sparkdlc.trait.effective.EffectiveInstinctService;
import dev.caecorthus.sparkdlc.trait.lifecycle.RoundEndTraitResolver;
import dev.caecorthus.sparkdlc.trait.civilian.depression.DepressionTraitService;
import dev.caecorthus.sparkdlc.trait.civilian.laststand.LastStandService;
import dev.caecorthus.sparkdlc.trait.civilian.police.GoingDarkRules;
import dev.caecorthus.sparkdlc.trait.global.CautiousTrait;
import dev.caecorthus.sparkdlc.trait.killer.KillerTraitService;
import dev.caecorthus.sparkdlc.trait.killer.combat.CloseQuartersService;
import dev.caecorthus.sparkdlc.trait.killer.combat.ForcedMeleeCooldownService;
import dev.caecorthus.sparkdlc.trait.killer.conscience.BluePoisonInteropService;
import dev.caecorthus.sparkdlc.trait.killer.escape.LastEscapeService;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.item.ItemStack;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentProvider;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Stable, null-safe trait queries: the mod's public contract toward its other features and third-party add-ons.
 * 稳定且支持空值的天赋查询接口：本模组面向自身其他功能与第三方附属模组的公开契约。
 */
public final class SparkTraitsApi {
    /**
     * Visits an ordered snapshot of effective, owner-visible inventory trait texts.
     * Client only in effect, common-safe on servers. Mutable texts are detached copies.
     * An empty visit is successful; collection/visitor exceptions propagate to the presenter.
     */
    public static void visitOwnerInventoryTraits(PlayerEntity player, BiConsumer<Text, List<Text>> visitor) {
        OwnerInventoryPresentation.visit(player, visitor);
    }

    /**
     * V1 inventory presentation protocol, kept for its signature: the mod's own owner inventory card shows the owner's
     * traits itself (asked directly, it was this protocol's only registrant), so no external presenter is accepted
     * and the answer is always false.
     * V1 背包展示协议，仅为保留签名：本模组自己的本人背包卡片直接显示本人天赋（直接调用；它曾是该协议唯一的注册方），
     * 因此不接受任何外部展示方，恒返回 false。
     */
    public static boolean registerExternalInventoryPresenterV1(BooleanSupplier presenter) {
        return false;
    }

    /** One accepted trigger pull, including synthetic repeats; observers never own punishment.
     * 一次有效扣动扳机（含补射）；观察者不接管误杀惩罚。 */
    public interface GunShotCycleListener {
        void cycleStarted(ServerPlayerEntity shooter, UUID cycleId, net.minecraft.item.Item weapon);
        Object beforeTargetKill(ServerPlayerEntity shooter, UUID cycleId, ServerPlayerEntity target);
        void afterTargetKill(ServerPlayerEntity shooter, UUID cycleId, ServerPlayerEntity target, Object token);
        void initialCooldownEstablished(ServerPlayerEntity shooter, UUID cycleId, net.minecraft.item.Item weapon,
                                        int startTick, int endTick, int currentTick);
        void cycleClosed(ServerPlayerEntity shooter, UUID cycleId);
    }

    public static void registerGunShotCycleListener(GunShotCycleListener listener) {
        dev.caecorthus.sparkdlc.trait.civilian.police.GunShotCycles.addListener(listener);
    }

    /** Updates and syncs exact remaining time without applying cooldown modifiers twice.
     * 精确修改并同步剩余时间，不重复计算冷却倍率。
     * <p>The exact write runs past the trait cooldown modifiers and performs the vanilla set itself (negative
     * {@code ticks} count as 0). Returns true once it wrote; false, with nothing written, only for a null player or
     * item, and then the caller must fall back to {@code ItemCooldownManager.set}: never run both, that would send
     * two cooldown packets.
     * <p>精确写入越过词条冷却倍率，并自行完成原版写入（负的 {@code ticks} 按 0 处理）。写入后返回 true；仅在玩家或物品
     * 为空时不写入并返回 false，此时调用方需回退到 {@code ItemCooldownManager.set}：二者不可同时执行，否则会发送两次
     * 冷却数据包。 */
    public static boolean setExactItemCooldownRemaining(ServerPlayerEntity player, net.minecraft.item.Item item, int ticks) {
        if (player == null || item == null) {
            return false;
        }
        dev.caecorthus.sparkdlc.trait.killer.combat.ExactItemCooldowns.setExact(player, item, ticks);
        return true;
    }

    public static int getItemCooldownTick(PlayerEntity player) {
        return player == null ? 0 : ((dev.caecorthus.sparkdlc.mixin.cooldown.ItemCooldownManagerAccessor)
                player.getItemCooldownManager()).sparkdlc$getTick();
    }

    public static int getItemCooldownRemaining(PlayerEntity player, net.minecraft.item.Item item) {
        return dev.caecorthus.sparkdlc.trait.killer.combat.ExactItemCooldowns.remainingTicks(player, item);
    }

    /** Marksman range multiplier for downstream police weapons: 1.3 when the player's runtime police role
     * holds an active Marksman, otherwise 1.0. Never Niko's fixed range. Null-safe, client- and server-safe.
     * 下游警用武器的精确枪手射程倍率：运行时警职持有生效的精确枪手时为 1.3，否则为 1.0；
     * 绝不返回 Niko 固定射程。支持空值，客户端与服务端均可调用。 */
    public static double getMarksmanRangeMultiplier(PlayerEntity player) {
        return player == null || player.getWorld() == null ? 1.0
                : dev.caecorthus.sparkdlc.trait.civilian.police.VigilanteVeteranTraitService.marksmanRangeMultiplier(player);
    }

    /** Target range for a downstream gun that validates its own shots (today the Savior pistol): Niko's
     * fixed 300 blocks while {@code shooter} is a sneaking Niko holder and {@code gun} is one Niko covers (revolver,
     * derringer, Savior pistol), otherwise {@code baseRange}; never below {@code baseRange}. It is the range the
     * client already aims with, so a receiver that keeps its own tolerance rejects Niko's long shots. Null-safe,
     * client- and server-safe.
     * 供自行校验射击的下游枪械（目前为救世主手枪）查询目标距离：{@code shooter} 是潜行的 Niko 持有者且
     * {@code gun} 属于 Niko 覆盖的枪（左轮、德林加、救世主手枪）时为固定 300 格，否则为 {@code baseRange}；绝不低于
     * {@code baseRange}。这正是客户端瞄准所用的射程，接收方若沿用自身容差会拒绝 Niko 的远距离射击。支持空值，
     * 客户端与服务端均可调用。 */
    public static double getServerGunTargetRange(PlayerEntity shooter, net.minecraft.item.Item gun, double baseRange) {
        return shooter == null || shooter.getWorld() == null || gun == null ? baseRange
                : Math.max(baseRange, dev.caecorthus.sparkdlc.trait.civilian.police.VigilanteVeteranTraitService
                        .serverGunTargetRange(baseRange, shooter, gun));
    }

    /** Server-only, null-safe: whether a downstream police gun's wathe:gun_shot kill of victim by shooter counts as a Heavy Artillery shot (active trait on a gun-police runtime role, within the trait's 5-block feet-to-feet reach, shooter != victim). Never kills or mutates anything.
     *  仅服务端、支持空值：下游警用枪械以 wathe:gun_shot 击杀时，是否算作重炮手射击（持有生效词条的警职、在 5 格内、射手不是受害者）。不击杀也不修改任何状态。 */
    public static boolean isHeavyArtilleryGunShot(ServerPlayerEntity shooter, ServerPlayerEntity victim) {
        // Eligibility only: the Jester-transition guard belongs to the revolver's second kill call, and the owner
        // decided downstream guns also pierce Jester-moment armour (2026-10-07).
        // 仅判定资格：小丑过渡守卫属于左轮的第二次击杀调用，所有者决定下游枪械同样可击穿小丑时刻护甲（2026-10-07）。
        return dev.caecorthus.sparkdlc.trait.civilian.police.VigilanteVeteranTraitService.isHeavyArtilleryShot(
                shooter, victim, dev.doctor4t.wathe.game.GameConstants.DeathReasons.GUN);
    }

    /** Throw charge for downstream throwables that time their own charge instead of vanilla item use:
     * shortened when the player holds an active Herculean Strength, otherwise {@code baseTicks}.
     * Launch speed needs no call; the trait scales every thrown projectile itself. Null-safe, both sides.
     * 供自行计时蓄力（不走原版使用流程）的下游投掷物查询蓄力时长：玩家持有生效的力大无穷时缩短，
     * 否则返回 {@code baseTicks}。初速无需调用，该天赋会自行放大所有投掷物。支持空值，两端均可调用。 */
    public static int getThrowChargeTicks(PlayerEntity player, int baseTicks) {
        return player == null || player.getWorld() == null ? baseTicks
                : dev.caecorthus.sparkdlc.trait.killer.HerculeanStrengthService.throwChargeTicks(player, baseTicks);
    }

    /** Knife windup for downstream HUDs: the first held tick on which releasing Wathe's knife stabs. Pass Wathe's
     * own windup as {@code baseTicks} (11: the knife needs more than 10 elapsed ticks); the result is shorter while
     * the player holds an active Seasoned (9) or raises the knife with Close Quarters (3). It is the same rule the
     * knife's release check uses, so a meter driven by it fills exactly when a release starts to count.
     * {@code knife} is the stack being raised. Null-safe, both sides.
     * 供下游 HUD 查询小刀前摇：松手即可出刀的第一个按住刻。{@code baseTicks} 传入 Wathe 自身的前摇（11：小刀要求
     * 已用刻数超过 10）；玩家持有生效的老练时缩短为 9，以狭路相逢举刀时缩短为 3。它与小刀松手判定使用同一条规则，
     * 因此据此绘制的进度条恰好在松手开始有效时读满。{@code knife} 为正在举起的物品。支持空值，两端均可调用。 */
    public static int getKnifeWindupTicks(PlayerEntity player, ItemStack knife, int baseTicks) {
        return player == null || player.getWorld() == null || knife == null || knife.isEmpty() || baseTicks <= 0
                ? baseTicks
                : dev.caecorthus.sparkdlc.trait.killer.combat.CloseQuartersService
                        .knifeReleaseThreshold(player, knife, baseTicks - 1) + 1;
    }

    /** Makes a death reason terminal: kills with it skip the trait revive/counter hooks (an unprotectable penalty).
     * Read live at kill time, so registering at server start is early enough; idempotent; {@code reason} must not
     * be null.
     * 把死亡原因登记为终结原因：以它造成的击杀会跳过词条的复活/反击钩子（不可保护的惩罚）。击杀时实时读取，因此在服务端
     * 启动时登记即足够早；可重复登记；{@code reason} 不可为空。 */
    public static void registerTerminalDeathReason(Identifier reason) {
        dev.caecorthus.sparkdlc.trait.lifecycle.TerminalDeathRules.register(reason);
    }

    /** Whether a death reason is registered terminal (an unprotectable penalty); a null reason answers false.
     * 该死亡原因是否已登记为终结原因（不可保护的惩罚）；原因为空时返回 false。 */
    public static boolean isTerminalDeathReason(Identifier reason) {
        return dev.caecorthus.sparkdlc.trait.lifecycle.TerminalDeathRules.contains(reason);
    }

    /** Last Escape or silenced effective killer. The trait feature gates NoellesRoles' skill packets itself; every
     * other role's own skill packets must ask this themselves.
     * 脱险或被沉默的有效杀手。词条功能只自行拦截 NoellesRoles 的技能数据包；其余职业自有的技能数据包必须自行查询。 */
    public static boolean isRoleSkillBlocked(PlayerEntity player) {
        return player != null && (isKillerInteractionBlocked(player)
                || dev.caecorthus.sparkdlc.trait.compat.noellesroles.SilencedKillerRestrictionService.isRestricted(player));
    }

    private SparkTraitsApi() {
    }

    /**
     * Returns whether the player currently owns the active trait.
     * 返回玩家当前是否拥有指定的生效天赋。
     */
    public static boolean hasActiveTrait(PlayerEntity player, Identifier traitId) {
        return player != null
                && traitId != null
                && TraitPlayerComponent.KEY.maybeGet(player)
                        .map(component -> component.hasActiveTrait(traitId))
                        .orElse(false);
    }

    /**
     * Killer-team membership after the alignment flips, for a caller that already holds the player's role: Impostor
     * turns an original civilian into a killer, Conscience turns an original killer into a civilian, and with both
     * traits Impostor decides (killer). A null role is on neither side; a null player counts as holding neither trait.
     * 判断玩家是否属于阵营翻转后的有效杀手阵营（供已持有玩家身份的调用方使用）：内鬼词条会把原始好人翻为杀手；
     * 善良词条会把原始杀手翻为好人。若两个词条同时存在，则由内鬼词条负责判定为杀手。身份为空时不属于任何一方；
     * 玩家为空时视为两个词条都没有。
     */
    public static boolean isEffectiveKiller(Role role, PlayerEntity player) {
        if (role == null) {
            return false;
        }
        if (hasActiveTrait(player, TraitIds.IMPOSTOR)) {
            return true;
        }
        if (hasActiveTrait(player, TraitIds.CONSCIENCE)) {
            return false;
        }
        return role.canUseKiller();
    }

    /**
     * Civilian-team membership after the alignment flips, the counterpart of {@link #isEffectiveKiller(Role,
     * PlayerEntity)}: Conscience is asked first (civilian), then Impostor (not a civilian); without either trait the
     * role's original Wathe side decides. A null role is on neither side.
     * 判断玩家是否属于“有效好人阵营”，与 {@link #isEffectiveKiller(Role, PlayerEntity)} 对应：善良词条优先视作好人；
     * 内鬼词条优先视作杀手；没有相关词条时回退到 Wathe 原始角色阵营。身份为空时不属于任何一方。
     */
    public static boolean isEffectiveCivilian(Role role, PlayerEntity player) {
        if (role == null) {
            return false;
        }
        if (hasActiveTrait(player, TraitIds.CONSCIENCE)) {
            return true;
        }
        if (hasActiveTrait(player, TraitIds.IMPOSTOR)) {
            return false;
        }
        return role.isInnocent();
    }

    /**
     * Active escape is a timed state, not a query for ownership of the hidden trait.
     * 脱险是已触发的限时状态，不暴露隐藏词条的持有情况。
     */
    public static boolean isLastEscapeActive(PlayerEntity player) {
        return player != null && LastEscapeService.isActive(player);
    }

    public static boolean isKillerInteractionBlocked(PlayerEntity player) {
        return isLastEscapeActive(player);
    }

    public static boolean hasLastEscapeGrayscale(PlayerEntity player) {
        return player != null && LastEscapeService.hasGrayscale(player);
    }

    public static float getLastEscapeDesaturation(PlayerEntity player) {
        return hasLastEscapeGrayscale(player) ? 0.5f : 0.0f;
    }

    /**
     * Forced melee lock is independent of ordinary weapon ability/attack cooldowns.
     * 强制近战禁用独立于武器普通技能和攻击蓄力冷却。
     */
    public static int getForcedMeleeCooldownTicks(PlayerEntity player, ItemStack weapon) {
        return player == null || weapon == null || weapon.isEmpty()
                ? 0 : ForcedMeleeCooldownService.remainingTicks(player, weapon);
    }

    /** New action gate, not a kill-source filter: emitted projectiles and marks keep resolving. True while killer
     * interactions are blocked (Last Escape) or the weapon is under a forced melee cooldown. Null-safe, both sides.
     * 仅拦截新动作，不过滤击杀来源；已发出的投射物与标记继续结算。杀手交互被禁用（脱险）或该武器处于强制近战冷却时为
     * true。支持空值，两端均可调用。 */
    public static boolean blocksWeaponAction(PlayerEntity player, ItemStack weapon) {
        return isKillerInteractionBlocked(player) || getForcedMeleeCooldownTicks(player, weapon) > 0;
    }

    /**
     * Call after normal target validation but before an immediate melee action spends resources.
     * 在普通目标校验之后、即时近战行动消耗资源之前调用；抵挡会产生一次反制效果。
     */
    public static boolean shouldCancelMeleeAttack(
            ServerPlayerEntity attacker,
            ServerPlayerEntity victim,
            ItemStack weapon
    ) {
        return attacker != null && victim != null && weapon != null
                && CloseQuartersService.shouldCancelMeleeAttack(attacker, victim, weapon);
    }

    /**
     * The whole trait gate of a melee weapon that settles its own hit: the attack is refused while the attacker's
     * weapon action is blocked ({@link #blocksWeaponAction}, asked first so that a blocked attacker never spends the
     * victim's counter), otherwise when {@link #shouldCancelMeleeAttack} cancels it. Call only after normal role,
     * range, target permission and charge checks, before any costs. False when an argument is null.
     * 自行结算命中的近战武器所需的完整词条判定：攻击者的武器动作被禁用时拒绝（{@link #blocksWeaponAction}，先行检查，
     * 因此被禁用的攻击者不会消耗受害者的反制），否则在 {@link #shouldCancelMeleeAttack} 取消时拒绝。
     * 仅在职业、距离、目标权限及次数校验后、消耗前调用；反制由服务端词条功能结算。任一参数为空时返回 false。
     */
    public static boolean blocksOrCancelsMeleeAttack(
            ServerPlayerEntity attacker,
            ServerPlayerEntity victim,
            ItemStack weapon
    ) {
        return attacker != null && victim != null && weapon != null
                && (blocksWeaponAction(attacker, weapon) || shouldCancelMeleeAttack(attacker, victim, weapon));
    }

    /**
     * Re-sends the player's trait component using the current Wathe life-state permissions.
     * 按当前 Wathe 存活状态权限重新同步玩家词条组件。
     *
     * <p>This is a small compatibility seam for external debug tools that modify Wathe's
     * {@code deadPlayers} marker without running the normal death event. It does not alter
     * any trait state; it only makes the new visibility decision reach the client immediately.</p>
     */
    public static void syncTraitVisibility(ServerPlayerEntity player) {
        if (player != null && !player.getWorld().isClient()) {
            TraitPlayerComponent.KEY.sync(player);
        }
    }

    /**
     * Creates the spectator-visible death trait snapshot used by the debug life-state command.
     * 创建调试存活状态命令所需的旁观者可见死亡词条快照。
     *
     * <p>This does not kill the player, clear traits, spawn a body, or trigger any death event.
     * It only mirrors the snapshot step that normally happens in the traits' real-death hook,
     * so the existing RoleNameRenderer path has data to display immediately.</p>
     */
    public static void snapshotDeathTraitsForDebug(ServerPlayerEntity player) {
        if (player == null || player.getWorld().isClient()) {
            return;
        }
        TraitWorldComponent traitWorld = TraitWorldComponent.KEY.get(player.getWorld());
        Collection<Identifier> traits = TraitPlayerComponent.KEY.get(player).getActiveTraitIds();
        if (traits.isEmpty()) {
            // 如果组件同步/生命周期正好处于切换窗口，回退到本局分配时保存的权威快照。
            traits = traitWorld.getRoundTraitSnapshot(player.getUuid());
        }
        traitWorld.snapshotDeathTraits(player.getUuid(), traits);

        /*
         * CCA 的实体组件 sync(player) 只覆盖目标本人和实体追踪者；多人调试时，远处
         * 玩家可能因此收不到第一次状态转换。死亡快照属于世界组件，这里显式对同世界
         * 在线玩家重发一次，确保创造/旁观查看者都立即拿到同一份快照。
         */
        if (player.getServer() != null) {
            for (ServerPlayerEntity recipient : player.getServer().getPlayerManager().getPlayerList()) {
                if (recipient.getServerWorld() == player.getServerWorld()) {
                    // World 和 ServerPlayerEntity 由 CCA Mixin 在运行时实现 ComponentProvider。
                    TraitWorldComponent.KEY.syncWith(recipient, (ComponentProvider) player.getWorld());
                    TraitPlayerComponent.KEY.syncWith(recipient, (ComponentProvider) player);
                }
            }
        }
    }

    /**
     * Returns an immutable snapshot of the player's active trait ids.
     * 返回玩家当前生效天赋 id 的不可变快照。
     */
    public static Collection<Identifier> getActiveTraitIds(PlayerEntity player) {
        return player == null
                ? List.of()
                : TraitPlayerComponent.KEY.maybeGet(player)
                        .map(TraitPlayerComponent::getActiveTraitIds)
                        .orElseGet(List::of);
    }

    /**
     * Immutable set of the player's active trait ids, as strings, limited to the traits this side's
     * {@link TraitRegistry} knows (an id from a newer server is skipped). A client is only sent a player's own
     * traits, so for the local player this is what the guide book and the achievement tracker may show. Cheap enough
     * for a per-tick poll: it reads the player's few active ids instead of asking about every registered trait.
     * 玩家生效天赋 id（字符串形式）的不可变集合，仅包含本端 {@link TraitRegistry} 认识的天赋（来自更新服务端的 id 会被跳过）。
     * 客户端只会收到玩家本人的天赋，因此对本地玩家而言，这正是指南与成就追踪可以展示的内容。开销足够小，可每 tick 轮询：
     * 直接读取玩家仅有的几个生效天赋，而不是逐个询问全部已注册天赋。
     */
    public static Set<String> getRegisteredActiveTraitIdStrings(PlayerEntity player) {
        if (player == null) {
            return Set.of();
        }
        Set<String> activeIds = new LinkedHashSet<>();
        for (Identifier id : getActiveTraitIds(player)) {
            if (TraitRegistry.contains(id)) {
                activeIds.add(id.toString());
            }
        }
        return Set.copyOf(activeIds);
    }

    /**
     * Returns an immutable snapshot of the trait ids revealed to their owner.
     * 返回已向持有者揭示的天赋 id 不可变快照。
     */
    public static Collection<Identifier> getRevealedTraitIds(PlayerEntity player) {
        return player == null
                ? List.of()
                : TraitPlayerComponent.KEY.maybeGet(player)
                        .<Collection<Identifier>>map(TraitPlayerComponent::getRevealedTraitIds)
                        .orElseGet(List::of);
    }

    /**
     * Applies the killer charisma discount to a shop entry when the player truly owns that trait.
     * 仅当玩家真实拥有杀手词条【魅力】时，对商店条目套用一次折扣包装。
     *
     * <p>这个公共门面专门给天赋功能之外（含第三方附属模组）的 shop 构建阶段使用，避免调用方直接依赖
     * 天赋包里的实现细节。</p>
     */
    public static @Nullable ShopEntry discountShopEntryForCharisma(
            PlayerEntity player,
            @Nullable ShopEntry entry
    ) {
        if (entry == null) {
            return null;
        }
        return KillerTraitService.applyCharismaDiscount(player, entry);
    }

    /**
     * Restores an exact runtime trait snapshot without exposing component internals downstream.
     * 恢复精确的运行时天赋快照，避免下游直接依赖组件内部实现。
     */
    public static void restoreActiveTraitsForRuntime(
            ServerPlayerEntity player,
            Collection<Identifier> activeTraitIds,
            Collection<Identifier> revealedTraitIds
    ) {
        if (player == null || activeTraitIds == null || revealedTraitIds == null) {
            return;
        }
        TraitPlayerComponent.KEY.maybeGet(player).ifPresent(component -> component.restoreActiveTraitsForRuntime(
                List.copyOf(activeTraitIds),
                List.copyOf(revealedTraitIds),
                TraitAssignmentReason.INTERNAL
        ));
    }

    /**
     * Server only; call after a mid-round role change, once the player's new balance is written. Built for the former
     * Grand Witch recruitment (removed 2026-10-05); kept as a public role-change seam.
     * Removes every active trait the current role could not have rolled at round start (no slot cap; unique-trait
     * memory kept), then draws one replacement per removed trait, hidden ones included, from that role's pool, never
     * Pig, Childish or a removed trait; a free trait (Well Supplied) earns no draw. If the player then holds Well
     * Supplied, kept from any former role or drawn, the current balance is multiplied again like starting money.
     * Calls {@code visitor} once, only when something was removed, with the owner-visible removed names and the visible
     * drawn names, each in order; hidden traits are left out. Nothing happens outside a running game, for a null
     * visitor, for a dead, role-less or active-Wraith player, during a pending Last Stand or Depression fake death, or
     * while Depression psycho is active.
     * 仅服务端；在局中换身份、写入新余额后调用。原为大魔女招募（已于 2026-10-05 移除）而建，作为公共换身份
     * 接口保留。移除当前身份在开局时无法获得的全部生效天赋（不设槽位上限，
     * 保留每局唯一记录），再按移除数量（隐藏天赋同样计入）从该身份候选池中逐个补抽，不会抽到猪、幼稚或被移除的天赋；
     * 免费天赋（物资充沛）不补抽。之后若玩家持有物资充沛（从任何原身份保留或补抽到），当前余额会按起始金币再加成一次。仅在有天赋被移除时调用 {@code visitor} 一次，依次传入本人可见的被移除
     * 天赋名称与可见的补抽天赋名称；隐藏天赋不列出。对局未运行、visitor 为空、玩家已死亡、无身份、为激活冤魂、处于
     * 背水一战/抑郁假死待决或抑郁疯魔中时不做任何事。
     */
    public static void replaceTraitsIneligibleForCurrentRole(
            ServerPlayerEntity player,
            BiConsumer<List<Text>, List<Text>> visitor
    ) {
        // An active Wraith's trait snapshot belongs to the Wraith API. / 激活冤魂的天赋快照归冤魂 API 管理。
        if (visitor != null && !isWraithActive(player)) {
            TraitRoleChangeRevalidation.replaceTraitsIneligibleForCurrentRole(player, visitor);
        }
    }

    /**
     * Server-authoritative: whether the player is in a Depression psycho (the trait's own psycho, not plain Wathe psycho),
     * whose stash holds their real inventory. Built for the former Grand Witch recruitment (removed
     * 2026-10-05), which refused such targets (owner decision 2026-10-04); kept as a public role-change seam. Null-safe;
     * always false on a remote client.
     * 服务端权威：玩家是否处于抑郁疯魔（该天赋自身的疯魔，而非普通 Wathe 疯魔），其暂存保存着真实物品。原为
     * 大魔女招募（已于 2026-10-05 移除）而建，该招募会拒绝此类目标（所有者 2026-10-04 决定）；作为公共换身份接口保留。
     * 支持空值；远端客户端始终返回 false。
     */
    public static boolean isDepressionPsychoActive(PlayerEntity player) {
        return player != null && DepressionTraitService.isPsychoActive(player);
    }

    /**
     * Captures Wraith-preserved trait state as an opaque, ordered NBT payload for the Wraith role.
     * 将冤魂保留的天赋状态保存为供冤魂职业使用的不透明有序 NBT 载荷。
     */
    public static NbtCompound captureWraithTraitSnapshot(PlayerEntity player) {
        NbtCompound snapshot = new NbtCompound();
        if (player == null) {
            return snapshot;
        }
        return TraitPlayerComponent.KEY.maybeGet(player)
                .map(component -> captureWraithTraitSnapshot(
                        component.getActiveTraitIds(),
                        component.getRevealedTraitIds()
                ))
                .orElse(snapshot);
    }

    /**
     * Restores the opaque Wraith trait snapshot and appends owner-visible Cautious beyond normal slots.
     * 恢复不透明冤魂天赋快照，并在普通槽位上限之外追加本人可见的小心翼翼。
     */
    public static void restoreWraithTraitSnapshot(PlayerEntity player, NbtCompound snapshot) {
        if (player == null || snapshot == null) {
            return;
        }
        TraitPlayerComponent.KEY.maybeGet(player).ifPresent(component -> restoreWraithTraitSnapshot(
                snapshot,
                (active, revealed) -> component.restoreActiveTraitsForRuntime(
                        active,
                        revealed,
                        TraitAssignmentReason.INTERNAL
                )
        ));
    }

    /**
     * Returns whether Last Stand currently owns a pending death transition for the player.
     * 返回背水一战当前是否持有该玩家的待决死亡转换。
     */
    public static boolean isLastStandPending(PlayerEntity player) {
        return player != null
                && (LastStandService.isPending(player)
                || TraitPlayerComponent.KEY.maybeGet(player)
                        .map(TraitPlayerComponent::isLastStandPending)
                        .orElse(false));
    }

    /**
     * Returns whether Last Stand approved the current death or already owns its pending transition.
     * 返回背水一战是否已批准当前死亡拦截，或已经持有其待决转换。
     */
    public static boolean isLastStandDeathIntercepted(PlayerEntity player) {
        return player != null && LastStandService.isDeathIntercepted(player);
    }

    /**
     * Clears Wraith-preserved traits with the lifecycle reason that downstream role ownership selects.
     * 使用下游身份所有者选择的生命周期原因清除冤魂保留天赋。
     */
    public static void clearWraithTraits(PlayerEntity player, boolean gameEnd) {
        if (player == null) {
            return;
        }
        TraitPlayerComponent.KEY.maybeGet(player).ifPresent(component -> clearWraithTraits(
                gameEnd,
                component::clearActiveTraits
        ));
    }

    /**
     * Delegates Wraith activity to the Wraith role's own query ({@code SparkRoleApi.isWraithActive}).
     * 将冤魂活动状态委托给冤魂职业自身的查询（{@code SparkRoleApi.isWraithActive}）。
     */
    public static boolean isWraithActive(PlayerEntity player) {
        return SparkRoleApi.isWraithActive(player);
    }

    /**
     * Returns whether Last Stand has already triggered for the player this round.
     * 返回该玩家的背水一战是否已经在本局触发。
     */
    public static boolean hasLastStandTriggeredThisRound(ServerWorld world, UUID playerUuid) {
        return world != null
                && playerUuid != null
                && LastStandService.hasTriggeredThisRound(world, playerUuid);
    }

    /**
     * Returns the ordered trait ids the player ended the round with, including traits hidden from their owner:
     * online active traits, else the latest death snapshot, else the assignment-time round snapshot.
     * Server-only; meaningful until Wathe's finalize resets players (e.g. inside its replay generation). Never null.
     * Also right for a victim inside Wathe's {@code KillPlayer.AFTER}, on either side of the trait feature's own AFTER
     * listener (which snapshots, then wipes, the victim's traits).
     * 返回玩家本局结束时持有的有序词条 id（包含对本人隐藏的词条）：在线时取当前词条，否则取最近死亡快照，再否则取分配时快照。
     * 仅限服务端；在 Wathe 结算重置玩家之前（如回放生成期间）有效。永不返回 null。在 Wathe 的 {@code KillPlayer.AFTER}
     * 中查询受害者同样正确，无论调用发生在词条功能自己的 AFTER 监听器（先快照、再清空受害者词条）之前还是之后。
     */
    public static List<Identifier> getRoundEndTraitIds(ServerWorld world, UUID playerUuid) {
        if (world == null || playerUuid == null) {
            return List.of();
        }
        return TraitWorldComponent.KEY.maybeGet(world)
                .map(traitWorld -> List.copyOf(RoundEndTraitResolver.resolve(world, traitWorld, playerUuid)))
                .orElseGet(List::of);
    }

    /**
     * Returns whether the world is currently in Last Stand's final moment. The flag is synced with the world, so
     * clients can ask too.
     * 返回当前世界是否处于背水一战的终局时刻。该标记随世界同步，客户端同样可用。
     */
    public static boolean isFinalMomentActive(World world) {
        return world != null
                && TraitWorldComponent.KEY.maybeGet(world)
                        .map(TraitWorldComponent::isFinalMomentActive)
                        .orElse(false);
    }

    /**
     * Returns whether this attacker's hit just turned into a non-final kill (e.g. a Depression fake death):
     * the victim survives, and the weapon should keep only {@link #getNonFinalKillCooldownTicks(int)}.
     * 返回该攻击者的这一击是否刚刚成为非最终击杀（如抑郁假死）：受害者未死亡，武器只保留
     * {@link #getNonFinalKillCooldownTicks(int)} 的冷却。
     */
    public static boolean isNonFinalKillPending(ServerPlayerEntity victim, ServerPlayerEntity attacker) {
        return victim != null && attacker != null && DepressionTraitService.isPendingFrom(victim, attacker);
    }

    /**
     * Weapon cooldown to keep after a non-final kill: 20% of the original, rounded up.
     * The trait already applies it to item cooldowns; weapons with their own cooldown state use this value.
     * Never negative and never longer than the original (0 for a non-positive original).
     * 非最终击杀后武器应保留的冷却：原冷却的 20%，向上取整。物品冷却已由该天赋处理，
     * 自带冷却状态的武器使用此值。不会为负，也不会超过原冷却（原冷却不为正时为 0）。
     */
    public static int getNonFinalKillCooldownTicks(int originalTicks) {
        return dev.caecorthus.sparkdlc.trait.civilian.depression.DepressionFakeKillCooldowns.discountedTicks(originalTicks);
    }

    /**
     * Runs a kill that happens away from the attacker's hands (e.g. a push that later makes the victim fall),
     * naming the weapon item that keeps the non-final-kill cooldown if the kill turns out non-final.
     * The kill always runs exactly once, inside the weapon-hint scope (a null weapon runs it without a hint), and
     * whatever it throws surfaces unchanged.
     * 执行不在攻击者手中发生的击杀（如推人后受害者坠车），并指明若成为非最终击杀时应缩短冷却的武器物品。
     * 击杀恰好执行一次：在武器提示作用域内执行（武器为空时不带提示直接执行），击杀抛出的异常原样抛出。
     */
    public static void runWithNonFinalKillWeapon(net.minecraft.item.Item weapon, Runnable kill) {
        if (weapon == null) {
            kill.run();
            return;
        }
        dev.caecorthus.sparkdlc.trait.civilian.depression.DepressionFakeKillCooldowns.withWeaponHint(weapon, kill);
    }

    /**
     * Returns whether the entity is an exact fake-death body owned by the traits' runtime state.
     * 返回该实体是否为天赋运行时状态精确记录的假死尸体。
     */
    public static boolean isFakeDeathBody(Entity entity) {
        return entity instanceof PlayerBodyEntity body
                && (LastStandService.isFakeDeathBody(body) || DepressionTraitService.isFakeDeathBody(body));
    }

    /**
     * Returns whether a trait currently prevents this viewer from highlighting the target by instinct.
     * 返回天赋当前是否阻止该观察者通过本能透视高亮目标。
     */
    public static boolean isInstinctHidden(PlayerEntity viewer, PlayerEntity target) {
        if (viewer == null || target == null) {
            return false;
        }

        TraitPlayerComponent targetTraits = TraitPlayerComponent.KEY.maybeGet(target).orElse(null);
        boolean finalMomentActive = TraitWorldComponent.KEY.maybeGet(viewer.getWorld())
                .map(TraitWorldComponent::isFinalMomentActive)
                .orElse(false);

        TraitPlayerComponent viewerTraits = TraitPlayerComponent.KEY.maybeGet(viewer).orElse(null);
        GameWorldComponent game = GameWorldComponent.KEY.maybeGet(viewer.getWorld()).orElse(null);
        boolean goingDarkSuppressed = false;
        if (targetTraits != null && viewerTraits != null && game != null) {
            boolean viewerPlayingAndAlive = GameFunctions.isPlayerPlayingAndAlive(viewer);
            boolean viewerCanSeeSpectatorInformation = GameFunctions.isPlayerSpectatingOrCreative(viewer)
                    && !viewerPlayingAndAlive;
            goingDarkSuppressed = GoingDarkRules.shouldSuppressInstinct(
                    targetTraits.isGoingDarkInstinctHidden(),
                    viewerPlayingAndAlive,
                    viewerCanSeeSpectatorInformation,
                    finalMomentActive,
                    game.getRole(viewer),
                    viewerTraits.getActiveTraitIds()
            );
        }

        // Spirit projection leaves a defenseless body; downstream instinct sources must still outline it, so
        // projection is not an input here.
        // 灵魂出窍留下的肉身毫无防备，下游本能来源仍须为其描边，因此出窍状态不参与此处判断。
        return EffectiveInstinctService.shouldHideFromInstinct(
                finalMomentActive,
                targetTraits != null && targetTraits.isLastStandPending(),
                targetTraits != null && targetTraits.isKillerInstinctHidden(),
                goingDarkSuppressed
        );
    }

    /**
     * Server only. Swaps a beverage plate's native Wathe poison for Conscience blue poison. With native poison present,
     * clears it, adds a blue layer owned by {@code poisoner} unless one already exists (the old owner is kept), and
     * returns true. Returns false without changes when there is no native poison, on client worlds, for null
     * arguments, or when {@code pos} holds no plate.
     * 仅服务端。把餐盘上的 Wathe 原生毒换成善良蓝毒：存在原生毒时清除它，若尚无蓝毒层则添加归属
     * {@code poisoner} 的蓝毒层（已有蓝毒层保留原归属者），并返回 true。无原生毒、客户端世界、参数为空
     * 或该位置不是餐盘时不做修改并返回 false。
     */
    public static boolean convertPlatePoisonToBlue(World world, BlockPos pos, UUID poisoner) {
        return BluePoisonInteropService.convertPlatePoisonToBlue(world, pos, poisoner);
    }

    /**
     * Server only. Bed variant of {@link #convertPlatePoisonToBlue}: {@code pos} may be either bed half, and both the
     * native scorpion and the blue scorpion are read from and written to the bed head.
     * 仅服务端。{@link #convertPlatePoisonToBlue} 的床版本：{@code pos} 可以是床的任一半，原生蝎子与蓝蝎子
     * 都在床头读写。
     */
    public static boolean convertBedPoisonToBlue(World world, BlockPos pos, UUID poisoner) {
        return BluePoisonInteropService.convertBedPoisonToBlue(world, pos, poisoner);
    }

    /**
     * Stack variant of {@link #convertPlatePoisonToBlue}: removes Wathe's poisoner component and adds a blue marker
     * owned by {@code poisoner} unless one is already present. Returns false when the stack carries no native poison.
     * {@link #convertPlatePoisonToBlue} 的物品版本：移除 Wathe 投毒者组件，若尚无蓝毒标记则添加归属
     * {@code poisoner} 的标记。物品没有原生毒时返回 false。
     */
    public static boolean convertStackPoisonToBlue(ItemStack stack, UUID poisoner) {
        return BluePoisonInteropService.convertStackPoisonToBlue(stack, poisoner);
    }

    /**
     * Stamps (overwrites) a blue-poison marker owned by {@code poisoner}; eating the stack springs the blue trap.
     * No-op for null or empty stacks.
     * 写入（覆盖）归属 {@code poisoner} 的蓝毒标记；食用该物品会触发蓝毒陷阱。空值或空物品时不做任何事。
     */
    public static void markStackBluePoison(ItemStack stack, UUID poisoner) {
        BluePoisonInteropService.markStackBluePoison(stack, poisoner);
    }

    /**
     * Returns the blue-poison owner on the stack, or null when absent or malformed. Never throws.
     * 返回物品上的蓝毒归属者；没有标记或格式错误时返回 null，不会抛异常。
     */
    public static @Nullable UUID getStackBluePoisoner(ItemStack stack) {
        return BluePoisonInteropService.getStackBluePoisoner(stack);
    }

    /**
     * Server only. Springs a blue trap with Conscience Poisoner's alignment rules: effective civilians get a short
     * sanity-drain window, everyone else lethal blue poison. No-op unless the target is playing and alive.
     * 仅服务端。按善良毒师的阵营规则触发蓝毒陷阱：有效好人获得短暂扣理智窗口，其余玩家中致死蓝毒。
     * 目标不在局内或已死亡时不做任何事。
     */
    public static void applyBlueTrap(ServerPlayerEntity target, UUID poisoner) {
        BluePoisonInteropService.applyBlueTrap(target, poisoner);
    }

    /**
     * Server only. Extends the blue sanity-drain window to at least {@code ticks} (never shortens it).
     * No-op for {@code ticks <= 0} or targets that are not playing and alive.
     * 仅服务端。把蓝毒扣理智窗口延长到至少 {@code ticks}（不会缩短）。{@code ticks <= 0} 或目标不在局内存活时不做任何事。
     */
    public static void applyBlueSanityDrain(ServerPlayerEntity target, int ticks) {
        BluePoisonInteropService.applyBlueSanityDrain(target, ticks);
    }

    /**
     * Server-authoritative remaining blue sanity-drain ticks. The window is never synced, so this is 0 on clients
     * and for null players.
     * 服务端权威的蓝毒扣理智剩余 tick。该窗口不会同步，因此客户端与空玩家始终返回 0。
     */
    public static int getBlueSanityDrainTicks(PlayerEntity player) {
        return BluePoisonInteropService.getBlueSanityDrainTicks(player);
    }

    /**
     * Registers a server-thread predicate; while any registered predicate returns true for a player, the blue-poison
     * tick skips that player's blue sanity drain but still counts the window down (so the owner can apply its own
     * effect).
     * A throwing predicate counts as not exempt. Registering the same instance twice has no extra effect.
     * 注册服务端线程谓词：只要任一谓词对某玩家返回 true，蓝毒逻辑就跳过该玩家的蓝毒扣理智，但窗口照常倒计时
     * （便于注册方施加自己的效果）。抛异常的谓词视为不豁免；重复注册同一实例没有额外效果。
     */
    public static void registerBlueSanityDrainExemption(Predicate<ServerPlayerEntity> exemption) {
        BluePoisonInteropService.registerBlueSanityDrainExemption(exemption);
    }

    private static NbtCompound captureWraithTraitSnapshot(
            Collection<Identifier> active,
            Collection<Identifier> revealed
    ) {
        NbtCompound snapshot = new NbtCompound();
        snapshot.put("ActiveTraits", TraitIdCodec.toNbt(active));
        snapshot.put("RevealedTraits", TraitIdCodec.toNbt(revealed));
        return snapshot;
    }

    private static void restoreWraithTraitSnapshot(
            NbtCompound snapshot,
            BiConsumer<Collection<Identifier>, Collection<Identifier>> restore
    ) {
        LinkedHashSet<Identifier> active = readIdentifiers(snapshot, "ActiveTraits");
        active.add(CautiousTrait.ID);
        LinkedHashSet<Identifier> revealed = readIdentifiers(snapshot, "RevealedTraits");
        revealed.add(CautiousTrait.ID);
        restore.accept(active, revealed);
    }

    private static void clearWraithTraits(
            boolean gameEnd,
            Consumer<TraitRemovalReason> clear
    ) {
        clear.accept(gameEnd ? TraitRemovalReason.GAME_END : TraitRemovalReason.DEATH);
    }

    private static LinkedHashSet<Identifier> readIdentifiers(NbtCompound snapshot, String key) {
        LinkedHashSet<Identifier> identifiers = new LinkedHashSet<>();
        TraitIdCodec.fromNbt(snapshot.getList(key, NbtElement.STRING_TYPE), identifiers);
        return identifiers;
    }
}
