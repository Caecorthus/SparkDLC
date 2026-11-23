package dev.caecorthus.sparkdlc.faction.api;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.UUID;

/**
 * Players who share a custom faction's win without being members of that faction. When a custom faction wins, its
 * members are collected first; every registered rule then adds its co-winners to the same insertion-ordered set, in
 * registration order (a player already in the set keeps its place). The set is what the round-end state syncs, so a
 * co-winner is treated as part of the winning faction by the settlement rows and by {@code didWin}. Register with
 * {@link SparkFactionApi#registerFactionCoWinnerRule} from the owning feature's registration code.
 * 不属于某自定义阵营却与其共同获胜的玩家。自定义阵营获胜时先收集其成员；随后每条已注册规则按注册顺序把自己的共同赢家加入同一个
 * 保持插入顺序的集合（已在集合中的玩家保持原位置）。回合结束状态同步的正是这个集合，因此结算行与 {@code didWin} 都把共同赢家
 * 视作获胜阵营的一员。在拥有该规则的功能的注册代码中通过 {@link SparkFactionApi#registerFactionCoWinnerRule} 注册。
 */
@FunctionalInterface
public interface FactionCoWinnerRule {
    void addCoWinners(ServerWorld world, GameWorldComponent gameComponent, Identifier winningFaction, Set<UUID> winners);
}
