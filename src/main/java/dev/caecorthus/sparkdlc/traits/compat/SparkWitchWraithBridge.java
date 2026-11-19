package dev.caecorthus.sparkdlc.traits.compat;

import dev.caecorthus.sparkdlc.witch.api.SparkWitchApi;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Bridge to SparkWitch's Wraith owner API. The modules are fused in SparkDLC, so this calls
 * {@link SparkWitchApi#isWraithActive} directly; it sits on the per-tick sync paths.
 * 到 SparkWitch 冤魂所有者 API 的桥接。各模块已融合进 SparkDLC，因此这里直接调用 {@link SparkWitchApi#isWraithActive}；
 * 它位于每 tick 同步路径上。
 */
public final class SparkWitchWraithBridge {
    private SparkWitchWraithBridge() {
    }

    public static boolean isWraithActive(PlayerEntity player) {
        if (player == null) {
            return false;
        }
        return SparkWitchApi.isWraithActive(player);
    }
}
