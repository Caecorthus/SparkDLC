package dev.caecorthus.sparkdlc.client.bridge.witch;

import dev.caecorthus.sparkdlc.client.economy.KillerTeamEconomyClientHooks;

/**
 * Bridge to the rows SparkStrength stacks under Wathe's money (the killer team wallet). The modules are fused in
 * SparkDLC, so this calls {@link KillerTeamEconomyClientHooks} directly.
 * 到 SparkStrength 在 Wathe 金币下方叠加的 HUD 行（杀手团队余额）的桥接。各模块已融合进 SparkDLC，因此这里直接调用
 * {@link KillerTeamEconomyClientHooks}。
 */
public final class SparkStrengthHudBridge {
    private SparkStrengthHudBridge() {
    }

    /**
     * Exclusive bottom y of SparkStrength's extra top-right rows, or 0 when none is showing.
     * SparkStrength 右上角额外行的底边 y（不含）；未显示时为 0。
     */
    public static int topRightHudBottom() {
        return Math.max(0, KillerTeamEconomyClientHooks.topRightHudBottom());
    }
}
