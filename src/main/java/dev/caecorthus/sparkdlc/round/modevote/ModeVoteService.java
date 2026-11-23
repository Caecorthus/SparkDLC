package dev.caecorthus.sparkdlc.round.modevote;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

/**
 * Wires the "which mode next round" vote: the packet players click with, the command that starts it by hand, and the
 * hook the {@code finalizeVoting} tail mixin calls once the map has been picked and everybody stands on it.
 * <p>
 * 接线「下一局用哪种模式」的投票：玩家点击时发的包、手动开启投票的指令，以及地图选完、所有人都已到位后由
 * {@code finalizeVoting} 尾部 mixin 调用的钩子。
 */
public final class ModeVoteService {

    private ModeVoteService() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ModeVoteC2SPayload.ID, ModeVoteC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ModeVoteC2SPayload.ID, new ModeVoteC2SPayload.Receiver());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ModeVoteCommand.register(dispatcher));
        SparkDLC.LOGGER.info("[modevote] registered, dual mode id {}", ModeVotingComponent.DUAL_ROLE_MODE_ID);
    }

    /**
     * Called from the {@code GameFunctions.finalizeVoting} tail mixin: the map is chosen and everyone has been
     * teleported, so the players can now pick the mode for the upcoming round.
     * <p>
     * 由 {@code GameFunctions.finalizeVoting} 尾部的 mixin 调用：地图已选定、玩家已全部传送，此时让玩家选下一局的模式。
     */
    public static void startVoteAfterTeleport(ServerWorld world) {
        MinecraftServer server = world.getServer();
        ModeVotingComponent component = ModeVotingComponent.KEY.get(server.getScoreboard());
        component.setServer(server);
        component.startVoting();
    }
}
