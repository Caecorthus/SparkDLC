package dev.caecorthus.sparkdlc.role.civilian.seeker;

import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesTaotieSeekerBridge;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.ControlExpertStunCounter;
import dev.caecorthus.sparkdlc.role.civilian.spiritualist.SpiritPossessionService;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.trait.api.TraitIds;
import dev.caecorthus.sparkdlc.role.killer.blackraven.crow.BlackRavenCrowSessions;
import dev.caecorthus.sparkdlc.role.killer.bomber.drone.DronePilotService;
import dev.caecorthus.sparkdlc.role.killer.kidnapper.KidnapperControlComponent;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithStateService;
import dev.caecorthus.sparkdlc.role.witch.grandwitch.GrandWitchFearService;
import dev.caecorthus.sparkdlc.role.witch.riftwalker.session.RiftSessionService;
import dev.caecorthus.sparkdlc.tablet.TabletChannelResolver;
import dev.caecorthus.sparkdlc.util.RoundParticipants;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

/**
 * Frozen contract: the two Seeker predicates. "Owner of record" (online, exactly Seeker, game running, match id bound)
 * drives cleanup; "active participant" (plus alive, not spectator/creative, not an active Wraith) gates every action.
 * Living-spectator states (Taotie-swallowed body, the Last Stand trait pending or the Depression trait's fake death)
 * fail only the participant predicate, so they end sessions (WP-09) but never clear state, devices, LostTo or
 * PendingReturn.
 * Server only: the match id is never synced, so every predicate is false on a client.
 * <p>Watching a camera is the one action that is not the owner's alone (owner spec 2026-11-28): every living round
 * participant on the tablet's police channel may watch the cameras of every Seeker of the round
 * ({@link #canWatchCameras}, {@link #cameraViewDenyReason}). Everything else (the car, placing, recalling, the breaker
 * mark, the economy) stays behind the two owner predicates.
 * 观看摄像头是唯一不专属于拥有者的行动（所有者规格 2026-11-28）：平板义警频道中每名存活的对局参与者都可以观看本局所有搜寻者的
 * 摄像头（{@link #canWatchCameras}、{@link #cameraViewDenyReason}）。其余一切（小车、放置、回收、损坏者标记、经济）
 * 仍由两个拥有者谓词把关。
 * 冻结契约：搜寻者的两个谓词。“记录中的拥有者”（在线、恰为搜寻者、对局进行中、matchId 一致）用于清理；
 * “可行动的参与者”（另加存活、非旁观/创造、非激活冤魂）作为所有行动的门槛。活着的旁观状态（本体被饕餮吞噬、
 * 背水一战词条待定或抑郁词条假死）只让参与者谓词失败，因此只结束会话（WP-09），从不清空状态、设备、LostTo 或 PendingReturn。
 * 仅服务端：matchId 从不同步，客户端上所有谓词均为 false。
 */
public final class SeekerTargeting {
    /** Suffixes of {@code message.sparkdlc.seeker.remote.denied.<suffix>}. / 拒绝提示键的后缀。 */
    static final String DENY_BLOCKED = "blocked";
    static final String DENY_SWALLOWED = "swallowed";
    static final String DENY_FEARED = "feared";
    static final String DENY_STUNNED = "stunned";
    static final String DENY_IMPOSTOR = "impostor";

    private SeekerTargeting() {
    }

    /**
     * Online server player whose Wathe role is exactly Seeker in a running game, bound to the current match.
     * Alive or not: the final-death decision belongs to the lifecycle and the component's fallback.
     * 在线的服务端玩家，其 Wathe 职业在进行中的对局里恰为搜寻者，且绑定当前对局。不要求存活：最终死亡由生命周期与组件兜底判定。
     */
    public static boolean isOwnerOfRecord(@Nullable PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer) || serverPlayer.isDisconnected()) {
            return false;
        }
        GameWorldComponent game = GameWorldComponent.KEY.get(serverPlayer.getWorld());
        return ownerOfRecord(game.isRunning(), SeekerRules.isSeeker(game.getRole(serverPlayer)),
                SeekerStatusComponent.KEY.get(serverPlayer).state().matchId(), currentMatchId(serverPlayer.getWorld()));
    }

    /**
     * Owner of record who is also alive in Wathe, not spectating or creative, and not an active Wraith.
     * 同时在 Wathe 中存活、非旁观/创造、非激活冤魂的记录拥有者。
     */
    public static boolean isActiveParticipant(@Nullable PlayerEntity player) {
        return isOwnerOfRecord(player)
                && GameFunctions.isPlayerPlayingAndAlive(player)
                && !player.isSpectator()
                && !player.isCreative()
                && !WraithStateService.isActive(player);
    }

    /**
     * Role-agnostic round participant for actions taken AGAINST a Seeker (the device-break gate): an online server
     * player, alive in Wathe, not spectating or creative, not an active Wraith, holding any role. Unlike
     * {@link #isActiveParticipant} it never requires the Seeker role or a bound match id, so killers, civilians, police
     * and neutrals all qualify ("other players", owner decision Q4). {@code ControlExpertTargeting.isParticipant}'s rule
     * plus the online-server-player gate ({@link RoundParticipants#CONNECTED_ROLE_HOLDER}, the player's own world).
     * 与职业无关的对局参与者，用于针对搜寻者的行动（设备损坏门槛）：在线的服务端玩家，在 Wathe 中存活、非旁观/创造、
     * 非激活冤魂，且拥有任意职业。与 {@link #isActiveParticipant} 不同，它从不要求搜寻者职业或绑定的对局 id，
     * 因此杀手、平民、警察与中立都满足（所有者决定 Q4 的“其他玩家”）。即控场专家 {@code isParticipant} 的规则另加在线服务端
     * 玩家条件（{@link RoundParticipants#CONNECTED_ROLE_HOLDER}，读取玩家自身世界）。
     */
    public static boolean isRoundParticipant(@Nullable PlayerEntity player) {
        return RoundParticipants.CONNECTED_ROLE_HOLDER.test(player);
    }

    /**
     * Deploy/place gate: the common gate (incl. the Q5-b impostor rule), an available body, no open session, and no
     * trait weapon-action block ({@code SparkTraitsApi.blocksWeaponAction}) for this stack.
     * 部署与放置门槛：公共门槛（含 Q5-b 内鬼规则）、本体可用、没有打开的会话，且该物品未被词条武器动作封锁
     * （{@code SparkTraitsApi.blocksWeaponAction}）。
     */
    public static boolean canUseDevice(ServerPlayerEntity player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) {
            return false;
        }
        return commonDenyReason(player) == null
                && isBodyAvailable(player)
                && SeekerStatusComponent.KEY.get(player).sessionMode() == SeekerSessionMode.NONE
                && !SparkTraitsApi.blocksWeaponAction(player, stack);
    }

    /** Body not swallowed, not Last-Stand pending, not kidnapper-controlled. / 本体未被吞噬、非最后一搏、未被绑架控制。 */
    public static boolean isBodyAvailable(ServerPlayerEntity player) {
        return player != null
                && !NoellesTaotieSeekerBridge.isSwallowed(player)
                && !SparkTraitsApi.isLastStandPending(player)
                && !isKidnapped(player);
    }

    /**
     * Shared gate of {@code seeker_remote_open} and {@code seeker_car_recall} (game ACTIVE, exact role, participant,
     * not feared, not skill-blocked, not stunned, not kidnapped, not impostor). Returns null when allowed, otherwise the
     * suffix of {@code message.sparkdlc.seeker.remote.denied.<suffix>}. A swallowed body is reported as
     * {@code swallowed} before the participant check that it would otherwise fail as {@code blocked}. STOPPING (the
     * post-win fade, still "running" for Wathe) is denied so nobody reopens a view or deploys after the round ended.
     * {@code seeker_remote_open} 与 {@code seeker_car_recall} 的公共门槛。允许时返回 null，
     * 否则返回 {@code message.sparkdlc.seeker.remote.denied.<suffix>} 的后缀。被吞噬的本体在参与者检查之前报告为
     * {@code swallowed}，否则会因参与者检查失败而报告为 {@code blocked}。STOPPING（胜负判定后的淡出阶段，
     * Wathe 仍视为“进行中”）一律拒绝，回合结束后无人能重新打开视角或部署。
     */
    @Nullable
    public static String commonDenyReason(ServerPlayerEntity player) {
        if (player == null) {
            return DENY_BLOCKED;
        }
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
        return denyReason(
                game.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE,
                SeekerRules.isSeeker(game.getRole(player)),
                () -> NoellesTaotieSeekerBridge.isSwallowed(player),
                () -> isActiveParticipant(player),
                () -> GrandWitchFearService.isPlayerFeared(player),
                () -> SparkTraitsApi.isRoleSkillBlocked(player),
                () -> ControlExpertStunCounter.isCounting(player),
                () -> isKidnapped(player),
                () -> SeekerRules.devicesDenied(SparkTraitsApi.hasActiveTrait(player, TraitIds.IMPOSTOR)));
    }

    /**
     * Who may watch the cameras of the round's Seekers (owner spec 2026-11-28): a member of the tablet's police
     * channel by identity, that is a police-network role (the Seeker is one) or an Impostor-trait holder. This is
     * {@code TabletChannelResolver.access(player).isMember(TabletChannel.POLICE)} for a living player, read without the
     * resolver's bookkeeping writes; it replaces both "exactly Seeker" and "not an Impostor" for camera viewing only.
     * Side-neutral (owner-synced data), but only the server decides.
     * 谁可以观看本局搜寻者的摄像头（所有者规格 2026-11-28）：按身份属于平板义警频道的成员，即义警网络身份（搜寻者是其中之一）
     * 或内鬼天赋持有者。对存活玩家它等同于 {@code TabletChannelResolver.access(player).isMember(TabletChannel.POLICE)}，
     * 但不触发解析器的记录写入；仅对观看摄像头而言，它同时取代“恰为搜寻者”与“不是内鬼”两项检查。
     * 两端通用（只读同步给本人的数据），但只有服务端作出决定。
     */
    public static boolean canWatchCameras(@Nullable PlayerEntity player) {
        return player != null && TabletChannelResolver.isPoliceNetworkMember(player);
    }

    /**
     * The gate of a CAMERA {@code seeker_remote_open} and of a running CAMERA session, for the owner and every other
     * viewer alike: {@link #commonDenyReason} with the police-channel membership in place of the Seeker role, the
     * role-agnostic {@link #isRoundParticipant} in place of the owner's participant rule, and no Impostor denial.
     * Viewers who are no Seeker can hold another remote body (a Black Raven's crow, a Riftwalker's rift, a piloted
     * drone or a possessed Wraith, which a Seeker never has); such a player is refused as {@code blocked}, so two
     * remote views never fight over one client camera.
     * CAMERA 模式的 {@code seeker_remote_open} 与进行中的 CAMERA 会话共用的门槛，拥有者与其他观看者一视同仁：即
     * {@link #commonDenyReason}，但以义警频道成员资格取代搜寻者职业、以与职业无关的 {@link #isRoundParticipant}
     * 取代拥有者的参与者规则，且不再拒绝内鬼。非搜寻者的观看者可能正持有另一个远程身体（黑羽的乌鸦、裂隙行者的裂隙、
     * 驾驶中的无人机或附身的冤魂，搜寻者从不会有）；此时按 {@code blocked} 拒绝，两个远程视角绝不争夺同一个客户端相机。
     */
    @Nullable
    public static String cameraViewDenyReason(ServerPlayerEntity player) {
        if (player == null) {
            return DENY_BLOCKED;
        }
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
        return denyReason(
                game.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE,
                canWatchCameras(player),
                () -> NoellesTaotieSeekerBridge.isSwallowed(player),
                () -> isRoundParticipant(player) && !holdsAnotherRemoteBody(player),
                () -> GrandWitchFearService.isPlayerFeared(player),
                () -> SparkTraitsApi.isRoleSkillBlocked(player),
                () -> ControlExpertStunCounter.isCounting(player),
                () -> isKidnapped(player),
                () -> false);
    }

    /**
     * The gate of one session mode: {@link #cameraViewDenyReason} for CAMERA, the owner-only
     * {@link #commonDenyReason} for the car (and for an unknown mode).
     * 单个会话模式的门槛：CAMERA 用 {@link #cameraViewDenyReason}，小车（以及未知模式）用仅限拥有者的
     * {@link #commonDenyReason}。
     */
    @Nullable
    public static String sessionDenyReason(ServerPlayerEntity player, @Nullable SeekerSessionMode mode) {
        return mode == SeekerSessionMode.CAMERA ? cameraViewDenyReason(player) : commonDenyReason(player);
    }

    /**
     * Current match binding id (Wathe's replay match UUID as a string), or null when no match is active or on a client.
     * 当前对局绑定 id（Wathe 回放对局 UUID 的字符串形式）；无进行中对局或在客户端上时为 null。
     */
    @Nullable
    public static String currentMatchId(World world) {
        if (world == null || world.isClient() || !GameRecordManager.hasActiveMatch()) {
            return null;
        }
        GameRecordManager.MatchRecord match = GameRecordManager.getCurrentMatch();
        return match == null || match.getMatchId() == null ? null : match.getMatchId().toString();
    }

    /** Pure owner-of-record rule; an unbound (null) match never matches. / 纯规则；未绑定（null）的对局永不匹配。 */
    static boolean ownerOfRecord(boolean running, boolean seeker, @Nullable String boundMatch,
                                 @Nullable String currentMatch) {
        return running && seeker && boundMatch != null && boundMatch.equals(currentMatch);
    }

    /**
     * Pure ordered gate; later seams are consulted only after every earlier one passed.
     * 纯有序门槛；只有前序条件全部通过才会查询后续接缝。
     */
    @Nullable
    static String denyReason(boolean running, boolean seeker, BooleanSupplier swallowed, BooleanSupplier participant,
                             BooleanSupplier feared, BooleanSupplier roleSkillBlocked, BooleanSupplier stunned,
                             BooleanSupplier kidnapped, BooleanSupplier impostorDenied) {
        if (!running || !seeker) {
            return DENY_BLOCKED;
        }
        if (swallowed.getAsBoolean()) {
            return DENY_SWALLOWED;
        }
        if (!participant.getAsBoolean()) {
            return DENY_BLOCKED;
        }
        if (feared.getAsBoolean()) {
            return DENY_FEARED;
        }
        if (roleSkillBlocked.getAsBoolean()) {
            return DENY_BLOCKED;
        }
        if (stunned.getAsBoolean()) {
            return DENY_STUNNED;
        }
        if (kidnapped.getAsBoolean()) {
            return DENY_BLOCKED;
        }
        if (impostorDenied.getAsBoolean()) {
            return DENY_IMPOSTOR;
        }
        return null;
    }

    private static boolean isKidnapped(PlayerEntity player) {
        return KidnapperControlComponent.KEY.get(player).isControlled();
    }

    /**
     * Raven Form, a rift, a piloted drone or a possessed Wraith: the session itself, whether or not its view is
     * currently being streamed.
     * 化鸦、裂隙、驾驶中的无人机或附身的冤魂：以会话本身为准，与其视野此刻是否正在流式加载无关。
     */
    private static boolean holdsAnotherRemoteBody(ServerPlayerEntity player) {
        return BlackRavenCrowSessions.isActive(player)
                || RiftSessionService.isInside(player)
                || DronePilotService.isPiloting(player)
                || SpiritPossessionService.isPossessing(player);
    }
}
