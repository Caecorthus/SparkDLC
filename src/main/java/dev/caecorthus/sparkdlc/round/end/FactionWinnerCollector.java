package dev.caecorthus.sparkdlc.round.end;

import dev.caecorthus.sparkdlc.faction.api.FactionCoWinnerRule;
import dev.caecorthus.sparkdlc.faction.registry.EffectiveFactionResolvers;
import dev.caecorthus.sparkdlc.faction.registry.FactionRoleCatalog;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Owns custom faction winner collection for round-end state.
 * 持有自定义阵营结算胜者收集规则。
 */
public final class FactionWinnerCollector {
    // Applied in registration order after the members were collected; the collector itself names no role.
    // 在收集完成员之后按注册顺序应用；收集器自身不点名任何职业。
    private static final List<FactionCoWinnerRule> CO_WINNER_RULES = new CopyOnWriteArrayList<>();

    private FactionWinnerCollector() {
    }

    public static void registerCoWinnerRule(FactionCoWinnerRule rule) {
        CO_WINNER_RULES.add(Objects.requireNonNull(rule, "rule"));
    }

    /**
     * Custom faction wins (Witch, Murderous Witch and every other registered faction) collect their winners here: the
     * faction's members, then the co-winners of every registered rule.
     * 自定义阵营胜利（魔女、杀意魔女以及其他已注册阵营）在此收集赢家：先是该阵营的成员，再是每条已注册规则的共同赢家。
     */
    public static Set<UUID> collectFactionWinners(
            ServerWorld world,
            GameWorldComponent gameComponent,
            Identifier factionId
    ) {
        Set<UUID> winners = collectFactionWinners(gameComponent.getAllPlayers(), uuid -> {
            PlayerEntity player = world.getPlayerByUuid(uuid);
            if (player != null && gameComponent.hasAnyRole(player)) {
                return EffectiveFactionResolvers.resolve(player, gameComponent);
            }
            return FactionRoleCatalog.resolveBaseFaction(gameComponent.getRole(uuid));
        }, factionId);
        return withCoWinners(CO_WINNER_RULES, world, gameComponent, factionId, winners);
    }

    /** Lets every rule, in order, add to {@code winners} and returns that set. / 让每条规则依次向 winners 添加并返回该集合。 */
    static Set<UUID> withCoWinners(
            Iterable<? extends FactionCoWinnerRule> rules,
            ServerWorld world,
            GameWorldComponent gameComponent,
            Identifier factionId,
            Set<UUID> winners
    ) {
        for (FactionCoWinnerRule rule : rules) {
            rule.addCoWinners(world, gameComponent, factionId, winners);
        }
        return winners;
    }

    static Set<UUID> collectFactionWinners(
            Iterable<UUID> roundPlayers,
            Function<UUID, Identifier> factionResolver,
            Identifier factionId
    ) {
        LinkedHashSet<UUID> winners = new LinkedHashSet<>();
        for (UUID uuid : roundPlayers) {
            if (factionId.equals(factionResolver.apply(uuid))) {
                winners.add(uuid);
            }
        }
        return winners;
    }
}
