package dev.caecorthus.sparkdlc.compat.noellesroles;

import dev.caecorthus.sparkdlc.SparkDLC;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.silencer.SilencedPlayerComponent;

/**
 * Narrow adapter for NoellesRoles silencing ({@code SilencedPlayerComponent.isPlayerSilenced}); any failure reports
 * "not silenced" and is logged once. Role silence gates (first the Time Stealer's) read it directly;
 * {@code SparkTraitsApi.isRoleSkillBlocked} also covers silenced killers. It never writes NoellesRoles state.
 * NoellesRoles 沉默状态的窄适配器（{@code SilencedPlayerComponent.isPlayerSilenced}）；任何失败都视为“未被沉默”并只记录一次日志。
 * 职业的沉默判定（最早是窃时者）直接读取它；{@code SparkTraitsApi.isRoleSkillBlocked} 也覆盖被沉默的杀手。
 * 从不写入 NoellesRoles 状态。
 */
public final class NoellesSilenceBridge {
    private static final AtomicBoolean FAILURE_LOGGED = new AtomicBoolean();

    private NoellesSilenceBridge() {
    }

    public static boolean isSilenced(PlayerEntity player) {
        if (player == null) {
            return false;
        }
        try {
            return SilencedPlayerComponent.isPlayerSilenced(player);
        } catch (RuntimeException | LinkageError error) {
            // A missing component or a changed NoellesRoles build must not break Clock use; fail open for silence only.
            // 组件缺失或 NoellesRoles 版本变化不得破坏时钟使用；仅沉默判定失败开放。
            if (FAILURE_LOGGED.compareAndSet(false, true)) {
                SparkDLC.LOGGER.warn("Cannot read the NoellesRoles silence state; treating players as not silenced",
                        error);
            }
            return false;
        }
    }
}
