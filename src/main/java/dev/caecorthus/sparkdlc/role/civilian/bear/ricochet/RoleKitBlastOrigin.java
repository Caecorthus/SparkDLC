package dev.caecorthus.sparkdlc.role.civilian.bear.ricochet;

import dev.caecorthus.sparkdlc.role.civilian.bodyguard.AttackOriginScope;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * SparkStrength seam (D3 step 2): the centre of the SparkStrength-scoped blast whose kills run on this thread
 * ({@code dev.caecorthus.sparkdlc.role.civilian.bodyguard.AttackOriginScope#currentBlast()}, which SparkStrength pushes around the
 * Wathe grenade, M67 and Bomber drone kill loops for its Democracy Shield). The modules are fused in SparkDLC, so this
 * is a direct call on the server thread. Outside any SparkStrength blast it answers
 * null: the blast's origin is then unknown, which counts as "not front" (fail closed toward the 25% roll). Since
 * 2026-11-13 the same centre also names the blast as an attack source ({@code BearRicochetRules.AttackSource}), so two
 * SparkStrength blasts roll apart; without one, the attempt falls back to its call burst.
 * SparkStrength 接缝（D3 第 2 步）：本线程正在结算其击杀的、由 SparkStrength 标注的爆炸中心
 * （{@code dev.caecorthus.sparkdlc.role.civilian.bodyguard.AttackOriginScope#currentBlast()}，SparkStrength 为民主盾牌把它包在 Wathe 手雷、
 * M67 与炸弹客无人机的击杀循环外）。各模块已融合进 SparkDLC，因此在服务端线程直接调用。不在任何 SparkStrength 爆炸中时
 * 返回 null：该爆炸的来源即为未知，视为“非正面”（失败时偏向 25% 掷骰）。自 2026-11-13 起，同一爆心也用来标识该
 * 爆炸这一攻击来源（{@code BearRicochetRules.AttackSource}），因此两次 SparkStrength 爆炸分别掷骰；没有爆心时该尝试回退为其调用批次。
 */
final class RoleKitBlastOrigin {
    private RoleKitBlastOrigin() {
    }

    static @Nullable Vec3d current() {
        return AttackOriginScope.currentBlast();
    }
}
