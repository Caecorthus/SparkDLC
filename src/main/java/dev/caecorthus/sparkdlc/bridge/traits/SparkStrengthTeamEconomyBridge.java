package dev.caecorthus.sparkdlc.bridge.traits;

import dev.caecorthus.sparkdlc.economy.KillerTeamEconomyService;

/**
 * Only an economy provider that applies the contribution bonus makes Team First selectable. The modules are fused in
 * SparkDLC, so SparkStrength's capability probe is asked directly.
 * 只有支持团队贡献加成的经济提供方才允许抽取团队至上。各模块已融合进 SparkDLC，因此直接询问 SparkStrength 的能力探针。
 */
public final class SparkStrengthTeamEconomyBridge {
    private SparkStrengthTeamEconomyBridge() {
    }

    public static boolean isAvailable() {
        return KillerTeamEconomyService.supportsTraitContributionBonus();
    }
}
