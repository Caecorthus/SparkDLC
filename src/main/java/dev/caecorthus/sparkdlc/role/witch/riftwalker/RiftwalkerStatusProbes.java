package dev.caecorthus.sparkdlc.role.witch.riftwalker;

import dev.caecorthus.sparkdlc.role.civilian.engineer.EngineerStunnedPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterPlayerComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Riftwalker-shared "held in place" probes. Gate entry (C10) refuses a player rooted by a Hunter trap or held by the
 * Engineer's capture-device stun (entry would be a free escape). Witches' Sabbath (D19) pulls such teammates and they
 * stay held at the landing spot; only the capture stun needs help, because it drags the body back to its lock point
 * every tick, so the pull moves that lock point through {@link EngineerStunnedPlayerComponent}.
 * 隙行者共用的「被定住」探针。进门（C10）拒绝被猎人捕兽夹定住、或被工程师捕捉装置眩晕的玩家（否则进门等于挣脱）。魔女集
 * 会（D19）会召集这类队友，他们到达落点后仍保持原状态；只有捕捉眩晕需要额外处理——它每 tick 会把本体拉回锁定点，因此召
 * 集时会经 {@link EngineerStunnedPlayerComponent} 把锁定点移到落点。
 */
public final class RiftwalkerStatusProbes {
    /** Ticks passed to {@code stun(int)}; the max keeps the remaining stun. / 传给 {@code stun(int)} 的刻数；取最大值保留剩余眩晕。 */
    static final int RELOCK_TICKS = 1;

    private RiftwalkerStatusProbes() {
    }

    /**
     * The Hunter trap root ({@code HunterPlayerComponent#isRooted}, the predicate the movement mixin
     * uses). A null player reads as "not rooted".
     * 猎人捕兽夹定身（{@code HunterPlayerComponent#isRooted}，与移动 mixin 使用的判定相同）。玩家为 null 时视为未定身。
     */
    public static boolean isHunterRooted(@Nullable PlayerEntity player) {
        return player != null
                && HunterPlayerComponent.KEY.get(player).isRooted();
    }

    /**
     * Reads the Engineer's CCA component {@code sparkdlc:engineer_stunned} ({@link EngineerStunnedPlayerComponent})
     * through its own key and asks its {@code isStunned()}. A null player reads as "not stunned". Server and client
     * safe.
     * 经工程师的 CCA 组件 {@code sparkdlc:engineer_stunned}（{@link EngineerStunnedPlayerComponent}）自身的键读取，并询
     * 问其 {@code isStunned()}。玩家为 null 时视为「未眩晕」。服务端与客户端均安全。
     */
    public static boolean isCaptureStunned(@Nullable PlayerEntity player) {
        EngineerStunnedPlayerComponent component = captureComponent(player);
        return component != null && component.isStunned();
    }

    /**
     * D19, server only, right after a Witches' Sabbath teleport: if the player is still capture-stunned, calls
     * the component's {@code stun(RELOCK_TICKS)}, which keeps {@code max(remaining, 1)} ticks and
     * moves the lock point to the player's current (landing) position, so the stun goes on there. A free player is never
     * touched (that call would stun them).
     * D19，仅服务端，在魔女集会传送之后立即调用：若玩家仍处于捕捉眩晕，则调用该组件的
     * {@code stun(RELOCK_TICKS)}——它保留 {@code max(剩余, 1)} 刻，并把锁定点移到玩家当前（落点）位置，眩晕在落点继续。
     * 绝不触碰未眩晕的玩家（该调用会眩晕他们）。
     */
    public static void relockCaptureStun(@Nullable PlayerEntity player) {
        EngineerStunnedPlayerComponent component = captureComponent(player);
        if (component != null && component.isStunned()) {
            component.stun(RELOCK_TICKS);
        }
    }

    /** The capture-stun component, or null for a null player. / 捕捉眩晕组件；玩家为 null 时为 null。 */
    private static @Nullable EngineerStunnedPlayerComponent captureComponent(@Nullable PlayerEntity player) {
        return player == null ? null : EngineerStunnedPlayerComponent.KEY.get(player);
    }
}
