package dev.caecorthus.sparkdlc.witch.compat;

import dev.caecorthus.sparkdlc.strength.sabotage.JunctionBoxService;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * SparkStrength junction-box seam (owner decision 2026-10-21): a weapon hit on a saboteur junction box
 * ({@code sparkstrength:junction_box}). The modules are fused in SparkDLC, so this calls
 * {@link JunctionBoxService#damageByWeapon(ServerPlayerEntity, BlockPos, int)} directly on the server thread; it stays
 * the only SparkWitch class that names that service. SparkStrength owns every sabotage rule (who may pry,
 * closed and wired box, sabotage still locked, map ready) and checks them silently; reaching 0 durability opens the
 * box like a finished pry, credited to the attacker. A refused gate means the hit does nothing to the box.
 * SparkStrength 接线箱接缝（所有者 2026-10-21 决定）：武器命中破坏者接线箱（{@code sparkstrength:junction_box}）。
 * 各模块已融合进 SparkDLC，因此本类在服务端线程直接调用
 * {@link JunctionBoxService#damageByWeapon(ServerPlayerEntity, BlockPos, int)}；仍然只有本类会点名该服务。
 * 所有破坏规则（谁可撬箱、箱子关闭且已接线、破坏仍处于锁定、地图就绪）由 SparkStrength
 * 自行静默校验；耐久降至 0 时如同撬箱完成般打开箱子，并记在攻击者名下。任一门槛不通过时，这次命中对箱子毫无作用。
 */
public final class SparkStrengthJunctionBoxCompat {
    private static final String MOD_ID = "sparkstrength";
    /**
     * The block a caller filters on before calling {@link #damage}; public because the callers live outside this package.
     * 调用方在调用 {@link #damage} 之前用来过滤的方块 id；调用方不在本包内，因此为 public。
     */
    public static final Identifier JUNCTION_BOX_ID = Identifier.of(MOD_ID, "junction_box");

    private SparkStrengthJunctionBoxCompat() {
    }

    /**
     * Deals {@code damage} durability (SparkStrength's unit: hundredths of a percent, 10_000 = 100%) to the junction
     * box at {@code pos} as {@code attacker}'s weapon hit. Server thread only. True iff SparkStrength applied it.
     * 以 {@code attacker} 的武器命中对 {@code pos} 处接线箱造成 {@code damage} 点耐久伤害（SparkStrength 的单位：
     * 万分之一，10_000 = 100%）。仅限服务端线程。仅当 SparkStrength 实际扣除时返回 true。
     */
    public static boolean damage(ServerPlayerEntity attacker, BlockPos pos, int damage) {
        if (attacker == null || pos == null || damage <= 0) {
            return false;
        }
        return JunctionBoxService.damageByWeapon(attacker, pos, damage);
    }
}
