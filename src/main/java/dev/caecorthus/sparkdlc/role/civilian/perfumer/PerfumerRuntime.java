package dev.caecorthus.sparkdlc.role.civilian.perfumer;

import dev.caecorthus.sparkdlc.component.PerfumerPlayerComponent;
import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesHiddenBodiesBridge;
import dev.caecorthus.sparkdlc.role.civilian.perfumer.kit.PerfumerKitRules;
import dev.caecorthus.sparkdlc.role.civilian.perfumer.kit.PerfumerScentComponent;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerCorpseComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.event.KillPlayer;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Owns server-authoritative scent promotion, Cologne pulses, and corpse sanity drain.
 * 持有服务端权威的气味晋升、古龙水脉冲与尸体理智下降。
 */
public final class PerfumerRuntime {
    private static final Identifier PERFUMER_ROLE_ID = Identifier.of(PerfumerRules.ROLE_ID);
    private static boolean registered;

    private PerfumerRuntime() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        KillPlayer.AFTER.register(PerfumerRuntime::afterKill);
    }

    /**
     * Called by the owner-only Perfumer component on the logical server.
     * 由仅同步给拥有者的调香师组件在逻辑服务端调用。
     */
    public static void tickPlayer(ServerPlayerEntity player, PerfumerPlayerComponent component) {
        if (component.tickColognePulse() && isActivePlayer(player)) {
            PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
            mood.setMood(PerfumerRules.applyCologneMoodPulse(mood.getMood()));
        }

        if (!isActivePerfumer(player)) {
            return;
        }
        PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
        if (mood.tasks.isEmpty() || !hasNearbyVisibleCorpse(player)) {
            return;
        }
        // Zephyr Perfume also halves this extra corpse-proximity drain: the per-task drain read here is scaled the way
        // Wathe's own read is (PerfumerKitRules, the Perfumer kit's rules, not this package's PerfumerRules).
        // 晨风香水同样将这份靠近尸体的额外理智下降减半：此处读取的每任务下降量按 Wathe 自身读取的方式缩放（PerfumerKitRules，
        // 即调香师套件的规则，而非本包的 PerfumerRules）。
        float baselineDrain = mood.tasks.size()
                * PerfumerKitRules.zephyrMoodDrain(
                        GameConstants.MOOD_DRAIN, PerfumerScentComponent.KEY.get(player).isZephyrActive());
        float extraDrain = PerfumerRules.extraCorpseMoodDrain(baselineDrain, 1);
        mood.setMood(mood.getMood() - extraDrain);
    }

    static boolean isActivePerfumer(PlayerEntity player) {
        GameWorldComponent gameComponent = GameWorldComponent.KEY.get(player.getWorld());
        return isPerfumerRole(gameComponent.getRole(player))
                && isActivePlayer(player);
    }

    static boolean isActivePlayer(PlayerEntity player) {
        return GameFunctions.isPlayerPlayingAndAlive(player)
                && !GameFunctions.isPlayerSpectatingOrCreative(player);
    }

    static boolean isPerfumerRole(@Nullable Role role) {
        return role != null && PERFUMER_ROLE_ID.equals(role.identifier());
    }

    private static void afterKill(
            ServerPlayerEntity victim,
            @Nullable ServerPlayerEntity killer,
            Identifier deathReason
    ) {
        ServerWorld world = victim.getServerWorld();
        GameWorldComponent gameComponent = GameWorldComponent.KEY.get(world);
        UUID victimUuid = victim.getUuid();
        UUID killerUuid = killer == null ? null : killer.getUuid();
        boolean attributedKill = killerUuid != null && !killerUuid.equals(victimUuid);

        for (ServerPlayerEntity owner : world.getPlayers()) {
            PerfumerPlayerComponent component = PerfumerPlayerComponent.KEY.get(owner);
            if (attributedKill && isPerfumerRole(gameComponent.getRole(owner))) {
                component.promoteMarked(killerUuid);
            }
            component.removeTarget(victimUuid);
        }
    }

    private static boolean hasNearbyVisibleCorpse(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        return !world.getEntitiesByClass(
                PlayerBodyEntity.class,
                player.getBoundingBox().expand(PerfumerRules.CORPSE_SANITY_RANGE_BLOCKS),
                body -> PerfumerRules.isWithinCorpseSanityRange(player.squaredDistanceTo(body))
                        && !NoellesHiddenBodiesBridge.isHidden(world, body.getPlayerUuid())
                        // A killed Partygoer's corpse doesn't weigh on the mind. / 派对客的尸体不会压在心头。
                        && !PartygoerCorpseComponent.isPartygoerCorpse(body)
        ).isEmpty();
    }
}
