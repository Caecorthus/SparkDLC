package dev.caecorthus.sparkdlc.role.civilian.seeker.remote;

import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesTaotieSeekerBridge;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.ControlExpertStunCounter;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerCameraRules;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerCarState;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerExitReason;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerRules;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerSessionMode;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerState;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerStatusComponent;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerTargeting;
import dev.caecorthus.sparkdlc.role.civilian.seeker.console.SeekerConsoleDevices;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerCameraEntity;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerCarEntity;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerDeviceEntity;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerDeviceService;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerSharedCameras;
import dev.caecorthus.sparkdlc.role.civilian.seeker.net.SeekerCarCorrectS2CPacket;
import dev.caecorthus.sparkdlc.role.civilian.seeker.net.SeekerNetworking;
import dev.caecorthus.sparkdlc.role.civilian.seeker.net.SeekerRemoteCloseC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.seeker.net.SeekerRemoteOpenC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.kidnapper.KidnapperControlComponent;
import dev.caecorthus.sparkdlc.role.witch.grandwitch.GrandWitchFearService;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Frozen contract: the server-owned remote-view session (open validation, per-tick exit checks, sprint clear, end).
 * The camera is switched only on the owner's client; this service never switches the server-side camera entity, so
 * server camera writers (Taotie, Last Stand, Depression) keep working and the body is never dragged along.
 * Authority: the client never predicts entry; every exit except the owner's own Shift is detected here and reaches
 * the owner through the owner-only component sync.
 * <p>"Owner" in this class is the player whose session it is. For a CAR session that is the Seeker who owns the car.
 * A CAMERA session may be anybody's on the tablet's police channel (owner spec 2026-11-28,
 * {@code SeekerTargeting.cameraViewDenyReason}) and may show any camera of the round
 * ({@code SeekerSharedCameras}); several players may view one camera at once. The session of such a viewer lives in the
 * viewer's own {@code sparkdlc:seeker_status} (its session fields and this service's {@code SeekerSessionState}), so
 * the lock, the payload guard, the streaming and the client view are the same code for everyone. A viewer whose
 * camera goes away is not ended by the device service (that ends only the camera owner's own session): the viewer's
 * next tick finds no focus and ends with FOCUS_LOST.
 * 冻结契约：服务端持有的遥控视角会话（打开校验、逐刻退出检查、清除疾跑、结束）。
 * 相机只在拥有者客户端切换；本服务从不切换服务端相机实体，因此服务端相机写入方（饕餮、最后一搏、抑郁）照常工作，本体也不会被拖动。
 * 权威划分：客户端从不预测进入；除拥有者自己按 Shift 外，所有退出都在此检测，并经仅拥有者可见的组件同步下发。
 * 本类中的“拥有者”指会话所属的玩家。小车会话即拥有小车的搜寻者。摄像头会话可以属于平板义警频道中的任何人（所有者规格
 * 2026-11-28，{@code SeekerTargeting.cameraViewDenyReason}），并可显示本局的任意摄像头（{@code SeekerSharedCameras}）；
 * 多名玩家可以同时观看同一台摄像头。这类观看者的会话保存在其自己的 {@code sparkdlc:seeker_status} 中（会话字段与本服务的
 * {@code SeekerSessionState}），因此锁定、数据包拦截、流式加载与客户端视角对所有人都是同一套代码。观看者所看的摄像头消失时，
 * 设备服务不会结束其会话（它只结束摄像头拥有者自己的会话）：观看者的下一刻找不到焦点，以 FOCUS_LOST 结束。
 */
public final class SeekerRemoteSessionService {
    private static final String DENIED_KEY_PREFIX = "message.sparkdlc.seeker.remote.denied.";
    /** Server-only open throttle bookkeeping; weak so it never pins a player. / 仅服务端的打开节流记录，弱引用不会滞留玩家。 */
    private static final Map<PlayerEntity, Long> LAST_OPEN_TICK = new WeakHashMap<>();
    private static boolean registered;

    private SeekerRemoteSessionService() {
    }

    /**
     * Also registers the Q17 body push exemption ({@code SparkFactionApi.registerEntityCollisionExemption}: a player
     * in a Seeker session is not pushed). The predicate is side-neutral: the owner's client sees its own synced mode,
     * every other client sees NONE for other players, and the server sees the authoritative mode.
     * 同时注册 Q17 本体推挤豁免（处于搜寻者会话中的玩家不被推挤）。谓词两端通用：拥有者客户端读取自己同步来的模式，
     * 其他客户端看到的他人模式恒为 NONE，服务端读取权威模式。
     */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        SparkFactionApi.registerEntityCollisionExemption(entity ->
                entity instanceof PlayerEntity player && isInSession(player));
    }

    /**
     * Validates {@code seeker_remote_open} in the plan §3.6 order ({@link SeekerRemoteOpenRules}); on success opens a
     * new session (fresh sessionId, anchor = body position, attach deadline now + 40) or atomically switches mode or
     * camera. A CAMERA request without a target views {@link #defaultCamera}; an explicit target must be a live camera
     * of the round (never trusted beyond that: the camera's reference by its owner of record, liveness, world and play
     * area are re-checked; there is no distance limit from the body, owner decision 2026-10-04). The gate depends on
     * the requested mode ({@code SeekerTargeting.sessionDenyReason}): the car is the Seeker's alone, a camera is open
     * to the police channel.
     * 按计划 §3.6 的顺序校验 {@code seeker_remote_open}；成功时打开新会话（新 sessionId、锚点为本体位置、挂接截止为当前 + 40 刻），
     * 或原子地切换模式或摄像头。未指定目标的摄像头请求观看 {@link #defaultCamera}；指定的目标必须是本局的存活摄像头
     * （除此之外不信任客户端：记录中的拥有者对该摄像头的引用、存活、世界与游戏区域都会重新校验；与本体之间没有距离限制，
     * 所有者决定 2026-10-04）。门槛取决于所请求的模式（{@code SeekerTargeting.sessionDenyReason}）：小车只属于搜寻者，
     * 摄像头对义警频道开放。
     */
    public static void handleOpen(ServerPlayerEntity player, SeekerRemoteOpenC2SPacket packet) {
        if (player == null || packet == null) {
            return;
        }
        SeekerStatusComponent status = SeekerStatusComponent.KEY.get(player);
        long now = serverTick(player);
        SeekerSessionMode requested = packet.sessionMode();
        String commonDeny = SeekerTargeting.sessionDenyReason(player, requested);
        Box playArea = playArea(player);
        SeekerDeviceEntity device = commonDeny == null
                ? usableDevice(player, status, requested, packet.targetEntityId(), playArea) : null;
        Long lastOpen = LAST_OPEN_TICK.get(player);
        SeekerRemoteOpenRules.Facts facts = new SeekerRemoteOpenRules.Facts(
                commonDeny,
                requested,
                status.sessionMode(),
                device != null && status.sessionMode() == requested
                        && status.state().sessionFocusEntityId() == device.getId(),
                NoellesTaotieSeekerBridge.isSwallowed(player),
                SparkTraitsApi.isLastStandPending(player),
                SeekerRemoteRules.isBodyGrounded(player.isOnGround(), player.hasVehicle(), player.isTouchingWater()),
                SeekerRemoteRules.isOpenThrottled(lastOpen == null ? -1L : lastOpen, now),
                SeekerConsoleDevices.hasConsoleDevice(player),
                device != null,
                device != null && SeekerRemoteRules.insidePlayArea(playArea, device.getPos()));
        String deny = SeekerRemoteOpenRules.denyReason(facts);
        if (deny != null || device == null) {
            player.sendMessage(Text.translatable(DENIED_KEY_PREFIX
                    + (deny == null ? SeekerRemoteOpenRules.DENY_NO_DEVICE : deny)), true);
            return;
        }
        open(player, status, requested, device, now);
    }

    /** Always accepted, even while stunned or feared; stale ids are ignored. / 始终接受，即使眩晕或恐惧；过期 id 忽略。 */
    public static void handleClose(ServerPlayerEntity player, SeekerRemoteCloseC2SPacket packet) {
        if (player == null || packet == null) {
            return;
        }
        SeekerStatusComponent status = SeekerStatusComponent.KEY.get(player);
        if (status.sessionMode() == SeekerSessionMode.NONE
                || packet.sessionId() != status.sessionId()) {
            return;
        }
        end(player, SeekerExitReason.PLAYER_EXIT);
    }

    /**
     * The one way any service ends a session, called BEFORE its own state transition: clears WP-09's
     * {@code SeekerSessionState}, applies {@code state.closeSession(reason)}, returns the car to idle, syncs, and sends
     * {@code reason.translationKey()} to the owner's action bar only when {@code reason.notifiesOwner()}. Idempotent; a
     * no-op when no session is open.
     * 任何服务结束会话的唯一途径，须在其自身状态转移之前调用：清除 WP-09 的会话状态、执行 closeSession、
     * 让小车回到空闲、同步，并仅在 {@code notifiesOwner()} 为真时向拥有者动作栏发送提示。幂等；无会话时为空操作。
     */
    public static void end(ServerPlayerEntity player, SeekerExitReason reason) {
        if (player == null || reason == null) {
            return;
        }
        SeekerStatusComponent status = SeekerStatusComponent.KEY.get(player);
        status.setSessionState(null);
        SeekerSessionMode mode = status.sessionMode();
        if (mode == SeekerSessionMode.NONE) {
            return;
        }
        // apply() syncs the owner when the transition is dirty. / apply() 在状态有变化时同步给拥有者。
        status.apply(status.state().closeSession(reason));
        if (mode == SeekerSessionMode.CAR) {
            settleCar(player);
        }
        if (reason.notifiesOwner()) {
            player.sendMessage(Text.translatable(reason.translationKey()), true);
        }
    }

    /**
     * Side-neutral: reads the (owner-synced) component and {@code SeekerRules.isLocked}, which honours
     * {@code LOCK_SCOPE}. Other players' components read as unlocked on a client because they are never synced there.
     * 两端通用：读取（仅同步给拥有者的）组件与遵循 {@code LOCK_SCOPE} 的 {@code SeekerRules.isLocked}。
     * 他人的组件从不同步到本客户端，因此在客户端读取时视为未锁定。
     */
    public static boolean isLocked(PlayerEntity player) {
        if (player == null) {
            return false;
        }
        SeekerStatusComponent status = SeekerStatusComponent.KEY.get(player);
        return SeekerRules.isLocked(status.carState(), status.sessionMode());
    }

    /**
     * Called from the component's server tick (step 3): clears sprint while locked, drops a stale session state when
     * the synced mode is NONE, and ends the session with the first failing exit check (plan §3.11.1).
     * 由组件服务端刻调用（第 3 步）：锁定时清除疾跑；同步模式为 NONE 时丢弃残留会话状态；按计划 §3.11.1 以第一个失败的退出检查结束会话。
     */
    public static void tick(ServerPlayerEntity player, SeekerStatusComponent component) {
        if (player == null || component == null) {
            return;
        }
        if (player.isSprinting() && isLocked(player)) {
            player.setSprinting(false);
        }
        SeekerSessionMode mode = component.sessionMode();
        SeekerSessionState session = component.sessionState();
        if (mode == SeekerSessionMode.NONE) {
            if (session != null) {
                component.setSessionState(null);
            }
            return;
        }
        if (session == null || session.sessionId != component.sessionId() || session.mode != mode) {
            // The server lost its bookkeeping (e.g. a reload); never keep an unvalidated session.
            // 服务端丢失了会话记录（例如重载）；绝不保留未经校验的会话。
            end(player, SeekerExitReason.TIMEOUT);
            return;
        }
        SeekerExitReason reason = exitReason(player, component, session, serverTick(player));
        if (reason != null) {
            end(player, reason);
        }
    }

    // ---- Internals ----

    private static void open(ServerPlayerEntity player, SeekerStatusComponent status, SeekerSessionMode mode,
                             SeekerDeviceEntity device, long now) {
        // Atomic switch (mode or camera): openSession closes and reopens in one transition, so the owner never sees
        // NONE between. A switch keeps the previous anchor, so repeated switches cannot walk the frozen body away step
        // by step. The old bookkeeping is replaced only after the transition succeeded (a new sessionId); a refused
        // switch keeps the old session.
        // 原子切换（模式或摄像头）：openSession 在一次转移中关闭并重开，拥有者之间不会看到 NONE。切换沿用原锚点，
        // 反复切换无法让冻结的本体逐步走远。只有转移成功（产生新的 sessionId）后才替换旧记录；被拒绝的切换保留原会话。
        SeekerSessionState previous = status.sessionState();
        Vec3d anchor = SeekerRemoteOpenRules.isSwitch(status.sessionMode(), mode,
                status.state().sessionFocusEntityId(), device.getId()) && previous != null
                && previous.sessionId == status.sessionId() && previous.mode == status.sessionMode()
                ? previous.anchor : player.getPos();
        int previousSessionId = status.sessionId();
        // usableDevice validated the device: a camera this player does not list is another Seeker's live camera.
        // usableDevice 已校验设备：该玩家未登记的摄像头即其他搜寻者的存活摄像头。
        status.apply(status.state().openSession(mode, device.getId(), mode == SeekerSessionMode.CAMERA));
        if (status.sessionMode() != mode || status.sessionId() == previousSessionId) {
            player.sendMessage(Text.translatable(DENIED_KEY_PREFIX + SeekerRemoteOpenRules.DENY_BLOCKED), true);
            return;
        }
        status.setSessionState(new SeekerSessionState(status.sessionId(), mode, device.getId(), anchor, now,
                now + SeekerRules.ATTACH_TIMEOUT_TICKS));
        LAST_OPEN_TICK.put(player, now);
        player.setSprinting(false);
        if (player.isUsingItem()) {
            // Clear, never release: releasing a Wathe knife would stab. / 只清除、绝不释放：释放 Wathe 小刀会触发刺击。
            player.clearActiveItem();
        }
        if (mode == SeekerSessionMode.CAR && device instanceof SeekerCarEntity car) {
            SeekerNetworking.sendCorrection(player, new SeekerCarCorrectS2CPacket(status.sessionId(),
                    car.getX(), car.getY(), car.getZ(), car.getYaw()));
        }
        // Server-side far view: stream the new focus now, right after the session sync.
        // 服务端远程视野：紧随会话同步立即流式加载新焦点。
        SeekerRemoteStreaming.onSessionOpened(player);
    }

    @Nullable
    private static SeekerExitReason exitReason(ServerPlayerEntity player, SeekerStatusComponent status,
                                               SeekerSessionState session, long now) {
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
        // STOPPING (the post-win fade) still counts as "running" for Wathe but fails the common gate; report it as the
        // silent ROUND_END rather than the notifying BLOCKED. / STOPPING（胜负后的淡出）对 Wathe 仍算“进行中”，
        // 但无法通过公共门槛；按静默的 ROUND_END 结束，而不是会提示的 BLOCKED。
        if (game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) {
            return SeekerExitReason.ROUND_END;
        }
        // The car is the Seeker's; a camera is watched by police-channel membership, which a role change, a lost
        // Impostor trait or a new faction can end mid-session.
        // 小车属于搜寻者；摄像头凭义警频道成员资格观看，职业变更、失去内鬼天赋或阵营变化都可能在会话中途结束它。
        if (session.mode == SeekerSessionMode.CAMERA
                ? !SeekerTargeting.canWatchCameras(player)
                : !SeekerRules.isSeeker(game.getRole(player))) {
            return SeekerExitReason.ROLE_CHANGED;
        }
        if (NoellesTaotieSeekerBridge.isSwallowed(player)) {
            return SeekerExitReason.BODY_SWALLOWED;
        }
        if (SparkTraitsApi.isLastStandPending(player)) {
            return SeekerExitReason.LAST_STAND;
        }
        if (!player.isAlive() || game.isPlayerDead(player.getUuid())) {
            return SeekerExitReason.DIED;
        }
        if (player.isSpectator()) {
            return SeekerExitReason.SPECTATOR;
        }
        if (ControlExpertStunCounter.isCounting(player)) {
            return SeekerExitReason.STUNNED;
        }
        if (KidnapperControlComponent.KEY.get(player).isControlled()) {
            return SeekerExitReason.KIDNAPPED;
        }
        if (GrandWitchFearService.isPlayerFeared(player)) {
            return SeekerExitReason.FEARED;
        }
        String deny = SeekerTargeting.sessionDenyReason(player, session.mode);
        if (deny != null) {
            return SeekerRemoteRules.exitReasonForDeny(deny);
        }
        if (!SeekerConsoleDevices.hasConsoleDevice(player)) {
            return SeekerExitReason.CONSOLE_LOST;
        }
        Entity focus = liveFocus(player, status, session);
        if (focus == null) {
            return SeekerExitReason.FOCUS_LOST;
        }
        if (SeekerRemoteRules.bodyMoved(session.anchor, player.getPos())) {
            return SeekerExitReason.BODY_MOVED;
        }
        if (SeekerRemoteRules.isBodyGrounded(player.isOnGround(), player.hasVehicle(), player.isTouchingWater())) {
            session.ungroundedTicks = 0;
        } else if (++session.ungroundedTicks > SeekerRemoteRules.UNGROUNDED_GRACE_TICKS) {
            return SeekerExitReason.BODY_MOVED;
        }
        // No distance limit from the body (2026-10-04): OUT_OF_RANGE now means the focus left the play area.
        // 与本体之间没有距离限制（2026-10-04）：OUT_OF_RANGE 现仅表示焦点离开了游戏区域。
        if (!SeekerRemoteRules.insidePlayArea(playArea(player), focus.getPos())) {
            return SeekerExitReason.OUT_OF_RANGE;
        }
        if (session.mode == SeekerSessionMode.CAR
                && SeekerRemoteRules.carTimedOut(session.attached, session.attachDeadline, session.lastMoveTick, now)) {
            return SeekerExitReason.TIMEOUT;
        }
        return null;
    }

    /**
     * The session's focus entity if it is still a live device of the right kind that this player may view: the
     * player's own car, or a camera of the round ({@link SeekerSharedCameras#find}).
     * 会话的焦点实体，前提是它仍是该玩家可以观看的、类型正确的存活设备：该玩家自己的小车，或本局的一台摄像头
     * （{@link SeekerSharedCameras#find}）。
     */
    @Nullable
    private static Entity liveFocus(ServerPlayerEntity player, SeekerStatusComponent status,
                                    SeekerSessionState session) {
        SeekerDeviceEntity device = switch (session.mode) {
            case CAR -> status.carState() == SeekerCarState.DEPLOYED && status.carEntityId() == session.focusEntityId
                    ? SeekerDeviceService.findCar(player) : null;
            case CAMERA -> SeekerSharedCameras.find(player, session.focusEntityId);
            case NONE -> null;
        };
        return device != null && device.getId() == session.focusEntityId && isViewableAndAlive(player, device)
                ? device : null;
    }

    /**
     * Open target: exists, viewable by this player, alive, in the body's world and matching the synced id (CAMERA: the
     * explicit target, or {@link #defaultCamera}; the target id is ignored for CAR).
     * 打开目标的有效性：存在、该玩家可以观看、存活、与本体同世界且与同步的 id 一致（摄像头：指定的目标或
     * {@link #defaultCamera}；小车忽略目标 id）。
     */
    @Nullable
    private static SeekerDeviceEntity usableDevice(ServerPlayerEntity player, SeekerStatusComponent status,
                                                   SeekerSessionMode mode, int targetEntityId,
                                                   @Nullable Box playArea) {
        SeekerDeviceEntity device = switch (mode) {
            case CAR -> {
                if (status.carState() != SeekerCarState.DEPLOYED) {
                    yield null;
                }
                SeekerCarEntity car = SeekerDeviceService.findCar(player);
                yield car != null && car.getId() == status.carEntityId() ? car : null;
            }
            case CAMERA -> SeekerSharedCameras.find(player,
                    targetEntityId >= 0 ? targetEntityId : defaultCamera(player, status, playArea));
            case NONE -> null;
        };
        return device != null && isViewableAndAlive(player, device) ? device : null;
    }

    /**
     * The camera an untargeted CAMERA open views: the last-viewed camera if it is still usable (alive, in the body's
     * world and inside the play area, at any distance), otherwise the first usable one in viewing order
     * ({@link SeekerSharedCameras#viewingOrder}: the player's own cameras by label, then the other Seekers'). With
     * none usable it falls back to the same order over merely live cameras, so the player is told "outside the play
     * area" rather than "no device".
     * 未指定目标的摄像头打开所观看的摄像头：最近观看的那台若仍可用（存活、与本体同世界且位于游戏区域内，距离不限）则选它，
     * 否则选观看顺序（{@link SeekerSharedCameras#viewingOrder}：先是该玩家自己的摄像头按编号，再是其他搜寻者的）中第一台
     * 可用的。都不可用时按同样顺序退回到仅存活的摄像头，使玩家收到“不在游戏区域内”而非“设备不可用”。
     */
    private static int defaultCamera(ServerPlayerEntity player, SeekerStatusComponent status,
                                     @Nullable Box playArea) {
        int lastViewed = status.state().lastViewedCameraId();
        List<SeekerState.Camera> cameras = SeekerSharedCameras.viewingOrder(player);
        int reachable = SeekerCameraRules.defaultCamera(cameras, lastViewed, id -> {
            SeekerCameraEntity camera = SeekerSharedCameras.find(player, id);
            return camera != null && SeekerRemoteRules.insidePlayArea(playArea, camera.getPos());
        });
        if (reachable >= 0) {
            return reachable;
        }
        return SeekerCameraRules.defaultCamera(cameras, lastViewed, id -> SeekerSharedCameras.find(player, id) != null);
    }

    /**
     * Alive in the body's world, and this player's to view: a car only for its owner, a camera for whoever passed
     * {@link SeekerSharedCameras#find} (the caller resolved it through that).
     * 存活且与本体同世界，并且该玩家可以观看：小车仅限其拥有者；摄像头只要通过 {@link SeekerSharedCameras#find}
     * （调用方已经由它解析）即可。
     */
    private static boolean isViewableAndAlive(ServerPlayerEntity player, SeekerDeviceEntity device) {
        return !device.isRemoved() && device.isAlive() && device.getWorld() == player.getWorld()
                && (device instanceof SeekerCameraEntity || player.getUuid().equals(device.ownerUuid()));
    }

    private static boolean isInSession(PlayerEntity player) {
        return SeekerStatusComponent.KEY.get(player).sessionMode() != SeekerSessionMode.NONE;
    }

    /** Stops the car's driven motion so the idle server tick resumes cleanly. / 停止驾驶运动，使服务端空闲刻平稳接管。 */
    private static void settleCar(ServerPlayerEntity player) {
        SeekerCarEntity car = SeekerDeviceService.findCar(player);
        if (car != null && !car.isRemoved()) {
            car.setVelocity(new Vec3d(0.0, Math.min(0.0, car.getVelocity().y), 0.0));
        }
    }

    @Nullable
    static Box playArea(PlayerEntity player) {
        return MapVariablesWorldComponent.KEY.get(player.getWorld()).getPlayArea();
    }

    static long serverTick(PlayerEntity player) {
        MinecraftServer server = player.getServer();
        return server != null ? server.getTicks() : player.getWorld().getTime();
    }
}
