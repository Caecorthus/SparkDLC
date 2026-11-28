package dev.caecorthus.sparkdlc.role.civilian.seeker.remote;

import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerSessionMode;
import org.jetbrains.annotations.Nullable;

/**
 * Pure, ordered {@code seeker_remote_open} checklist (plan §3.6). The service gathers the facts on the server thread;
 * this class only decides, so the order is testable without a world. The first failing check wins and returns the
 * suffix of {@code message.sparkdlc.seeker.remote.denied.<suffix>}; null means the open is allowed.
 * 纯粹、有序的 {@code seeker_remote_open} 检查清单（计划 §3.6）。服务在服务端线程上收集事实，本类只负责判定，
 * 因此无需世界即可测试顺序。第一个失败的检查生效，返回 {@code message.sparkdlc.seeker.remote.denied.<suffix>} 的后缀；
 * 返回 null 表示允许打开。
 */
public final class SeekerRemoteOpenRules {
    public static final String DENY_BLOCKED = "blocked";
    public static final String DENY_SWALLOWED = "swallowed";
    public static final String DENY_NOT_GROUNDED = "not_grounded";
    public static final String DENY_BUSY = "busy";
    public static final String DENY_TOO_FAST = "too_fast";
    public static final String DENY_NO_TABLET = "no_tablet";
    public static final String DENY_NO_DEVICE = "no_device";
    public static final String DENY_OUT_OF_RANGE = "out_of_range";

    private SeekerRemoteOpenRules() {
    }

    /**
     * Facts about one open request, all read on the server.
     * 一次打开请求的全部事实，均在服务端读取。
     *
     * @param commonDeny     {@code SeekerTargeting.sessionDenyReason} for the requested mode (null = passed): the
     *                       owner-only gate for the car, the police-channel gate for a camera / 所请求模式的公共门槛结果：
     *                       小车为仅限拥有者的门槛，摄像头为义警频道的门槛
     * @param requested      decoded mode; NONE for an unknown byte / 解码后的模式，未知字节为 NONE
     * @param current        current session mode / 当前会话模式
     * @param sameDevice     the resolved target is the device the open session already shows
     *                       / 解析出的目标正是当前会话显示的设备
     * @param swallowed      body swallowed by a Taotie / 本体被饕餮吞噬
     * @param lastStand      Last Stand trait pending / 最后一搏词条待定
     * @param grounded       {@link SeekerRemoteRules#isBodyGrounded} / 本体立足
     * @param throttled      {@link SeekerRemoteRules#isOpenThrottled} / 打开节流中
     * @param consoleDevice  a console device anywhere in the inventory / 背包任意位置持有控制台设备
     * @param deviceUsable   target device exists, is alive and this player's to view (the own car, or any camera of
     *                       the round) / 目标设备存在、存活且该玩家可以观看（自己的小车，或本局的任意摄像头）
     * @param inPlayArea     inside the Wathe play area; the only spatial check, with no distance limit from the body
     *                       (2026-10-04) / 在 Wathe 游戏区域内；唯一的空间检查，与本体之间没有距离限制（2026-10-04）
     */
    public record Facts(@Nullable String commonDeny, SeekerSessionMode requested, SeekerSessionMode current,
                        boolean sameDevice, boolean swallowed, boolean lastStand, boolean grounded, boolean throttled,
                        boolean consoleDevice, boolean deviceUsable, boolean inPlayArea) {
    }

    @Nullable
    public static String denyReason(Facts facts) {
        if (facts.commonDeny() != null) {
            return facts.commonDeny();
        }
        if (facts.requested() == SeekerSessionMode.NONE) {
            return DENY_BLOCKED;
        }
        if (facts.swallowed()) {
            return DENY_SWALLOWED;
        }
        if (facts.lastStand()) {
            return DENY_BLOCKED;
        }
        if (!facts.grounded()) {
            return DENY_NOT_GROUNDED;
        }
        if (isBusy(facts.current(), facts.requested(), facts.sameDevice())) {
            return DENY_BUSY;
        }
        if (facts.throttled()) {
            return DENY_TOO_FAST;
        }
        if (!facts.consoleDevice()) {
            return DENY_NO_TABLET;
        }
        if (!facts.deviceUsable()) {
            return DENY_NO_DEVICE;
        }
        // The stable suffix "out_of_range" now means "outside the play area" only. / 稳定后缀现仅表示“在游戏区域外”。
        if (!facts.inPlayArea()) {
            return DENY_OUT_OF_RANGE;
        }
        return null;
    }

    /**
     * Already showing what was asked: the same CAR session, or a CAMERA session on the same camera. Another camera
     * while viewing one is not busy but an atomic switch.
     * 已在显示所请求的内容：同一小车会话，或同一台摄像头的摄像头会话。观看一台摄像头时请求另一台不算忙碌，而是原子切换。
     */
    public static boolean isBusy(SeekerSessionMode current, SeekerSessionMode requested, boolean sameDevice) {
        return current == requested && (requested != SeekerSessionMode.CAMERA || sameDevice);
    }

    /**
     * A different mode, or another camera, while a session is open: an atomic switch.
     * 会话中请求另一模式或另一台摄像头：原子切换。
     */
    public static boolean isSwitch(SeekerSessionMode current, SeekerSessionMode requested, int currentFocusEntityId,
                                   int targetEntityId) {
        return current != SeekerSessionMode.NONE && requested != SeekerSessionMode.NONE
                && (current != requested || currentFocusEntityId != targetEntityId);
    }
}
