package dev.caecorthus.sparkdlc.role.civilian.bear.ricochet;

import dev.caecorthus.sparkdlc.role.civilian.bodyguard.AttackOriginScope;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * Blast-scope seam (D3 step 2): the centre of the scoped blast whose kills run on this thread
 * ({@code role.civilian.bodyguard.AttackOriginScope#currentBlast()}, which is pushed around the Wathe grenade, M67
 * and Bomber drone kill loops for the Bodyguard's Democracy Shield), read by a direct call on the server thread.
 * Outside any scoped blast it answers
 * null: the blast's origin is then unknown, which counts as "not front" (fail closed toward the 25% roll). Since
 * 2026-11-13 the same centre also names the blast as an attack source ({@code BearRicochetRules.AttackSource}), so two
 * scoped blasts roll apart; without one, the attempt falls back to its call burst.
 * 爆炸作用域接缝（D3 第 2 步）：本线程正在结算其击杀的、带作用域的爆炸中心
 * （{@code role.civilian.bodyguard.AttackOriginScope#currentBlast()}，为保镖的民主盾牌而包在 Wathe 手雷、M67 与
 * 炸弹客无人机的击杀循环外），在服务端线程直接调用读取。不在任何带作用域的爆炸中时
 * 返回 null：该爆炸的来源即为未知，视为“非正面”（失败时偏向 25% 掷骰）。自 2026-11-13 起，同一爆心也用来标识该
 * 爆炸这一攻击来源（{@code BearRicochetRules.AttackSource}），因此两次带作用域的爆炸分别掷骰；没有爆心时该尝试回退为
 * 其调用批次。
 */
final class RoleKitBlastOrigin {
    private RoleKitBlastOrigin() {
    }

    static @Nullable Vec3d current() {
        return AttackOriginScope.currentBlast();
    }
}
