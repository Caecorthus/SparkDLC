package dev.caecorthus.sparkdlc.traits.compat;

import dev.caecorthus.sparkdlc.strength.role.coroner.CoronerService;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Bridge to SparkStrength's Coroner body disguise, which native killers see as a cohort. The modules are fused in
 * SparkDLC, so this calls {@link CoronerService} directly.
 * 到 SparkStrength 验尸官尸体伪装的桥接；原生杀手会把伪装成杀手/卧底的验尸官看作同伙。各模块已融合进 SparkDLC，
 * 因此这里直接调用 {@link CoronerService}。
 */
public final class SparkStrengthCoronerBridge {
    private SparkStrengthCoronerBridge() {
    }

    /** Mirrors SparkStrength's own killer-cohort disguise check: killer-faction or Undercover body identity.
     *  与 SparkStrength 自身的杀手同伙伪装判定一致：杀手阵营或卧底尸体身份。 */
    public static boolean appearsAsKillerCohort(PlayerEntity player) {
        if (player == null) {
            return false;
        }
        return CoronerService.hasKillerFactionDisguise(player) || CoronerService.hasUndercoverDisguise(player);
    }
}
