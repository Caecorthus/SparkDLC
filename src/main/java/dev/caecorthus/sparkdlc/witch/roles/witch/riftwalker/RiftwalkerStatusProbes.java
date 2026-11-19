package dev.caecorthus.sparkdlc.witch.roles.witch.riftwalker;

import dev.caecorthus.sparkdlc.strength.component.engineer.EngineerStunnedPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.killer.hunter.HunterPlayerComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Riftwalker-shared "held in place" probes. Gate entry (C10) refuses a player rooted by a Hunter trap or held by the
 * SparkStrength capture-device stun (entry would be a free escape). Witches' Sabbath (D19) pulls such teammates and
 * they stay held at the landing spot; only the capture stun needs help, because it drags the body back to its lock
 * point every tick, so the pull moves that lock point. The SparkStrength component is called directly since the
 * SparkDLC fusion.
 * 隙行者共用的「被定住」探针。进门（C10）拒绝被猎人捕兽夹定住、或被 SparkStrength 捕捉装置眩晕的玩家（否则进门等于挣脱）。
 * 魔女集会（D19）会召集这类队友，他们到达落点后仍保持原状态；只有捕捉眩晕需要额外处理——它每 tick 会把本体拉回锁定点，
 * 因此召集时会把锁定点移到落点。融合进 SparkDLC 后直接调用 SparkStrength 组件。
 */
public final class RiftwalkerStatusProbes {
    /** Ticks passed to {@code stun(int)}; the max keeps the remaining stun. / 传给 {@code stun(int)} 的刻数；取最大值保留剩余眩晕。 */
    static final int RELOCK_TICKS = 1;

    private RiftwalkerStatusProbes() {
    }

    /**
     * SparkWitch's own Hunter trap root ({@code HunterPlayerComponent#isRooted}, the predicate the movement mixin
     * uses). A missing component reads as "not rooted".
     * SparkWitch 自有的猎人捕兽夹定身（{@code HunterPlayerComponent#isRooted}，与移动 mixin 使用的判定相同）。缺少组件时视为未定身。
     */
    public static boolean isHunterRooted(@Nullable PlayerEntity player) {
        return player != null
                && HunterPlayerComponent.KEY.maybeGet(player).map(HunterPlayerComponent::isRooted).orElse(false);
    }

    /**
     * Cross-module seam (the modules are fused in SparkDLC, so the call is direct): reads SparkStrength's CCA
     * component {@code sparkstrength:engineer_stunned} ({@link EngineerStunnedPlayerComponent}) through its own key and
     * asks its public {@code boolean isStunned()}. A null player or a player without the component reads as
     * "not stunned". Server and client safe.
     * 跨模块接缝（各模块已融合进 SparkDLC，因此为直接调用）：经 SparkStrength 的 CCA 组件
     * {@code sparkstrength:engineer_stunned}（{@link EngineerStunnedPlayerComponent}）自身的键读取，并询问其 public
     * {@code boolean isStunned()}。玩家为 null 或没有该组件时视为「未眩晕」。服务端与客户端均安全。
     */
    public static boolean isCaptureStunned(@Nullable PlayerEntity player) {
        EngineerStunnedPlayerComponent component = captureComponent(player);
        return component != null && component.isStunned();
    }

    /**
     * D19 fail-closed guard for Witches' Sabbath: capture-stunned while {@link #relockCaptureStun} cannot move the lock
     * point. That could only happen while {@code stun(int)} was a reflective lookup (unresolved, or disabled after a
     * failed call); it is a compiled call since the SparkDLC fusion, so the re-lock is always available and this is
     * always false. Kept so the Sabbath's skip rule and its caller stay as they are.
     * D19 魔女集会的失败即关闭防护：处于捕捉眩晕，且 {@link #relockCaptureStun} 无法移动锁定点。这只可能发生在
     * {@code stun(int)} 仍是反射查找的时期（无法解析，或调用失败后已关闭）；融合进 SparkDLC 后它是编译期调用，重新锁定始终可用，
     * 因此恒为 false。保留本方法，使魔女集会的跳过规则及其调用方保持不变。
     */
    public static boolean isCaptureStunPinned(@Nullable PlayerEntity player) {
        return false;
    }

    /**
     * D19, server only, right after a Witches' Sabbath teleport: if the player is still capture-stunned, calls
     * SparkStrength's public {@code stun(RELOCK_TICKS)} directly, which keeps {@code max(remaining, 1)} ticks and
     * moves the lock point to the player's current (landing) position, so the stun goes on there. A free player is never
     * touched (that call would stun them).
     * D19，仅服务端，在魔女集会传送之后立即调用：若玩家仍处于捕捉眩晕，则直接调用 SparkStrength 的 public
     * {@code stun(RELOCK_TICKS)}——它保留 {@code max(剩余, 1)} 刻，并把锁定点移到玩家当前（落点）位置，眩晕在落点继续。
     * 绝不触碰未眩晕的玩家（该调用会眩晕他们）。
     */
    public static void relockCaptureStun(@Nullable PlayerEntity player) {
        EngineerStunnedPlayerComponent component = captureComponent(player);
        if (component != null && component.isStunned()) {
            component.stun(RELOCK_TICKS);
        }
    }

    /** The SparkStrength component, or null (null player, or none attached). / SparkStrength 组件；玩家为 null 或未挂载时为 null。 */
    private static @Nullable EngineerStunnedPlayerComponent captureComponent(@Nullable PlayerEntity player) {
        return player == null ? null : EngineerStunnedPlayerComponent.KEY.getNullable(player);
    }
}
