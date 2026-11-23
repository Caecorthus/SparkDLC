package dev.caecorthus.sparkdlc.round.modevote;

import com.mojang.brigadier.CommandDispatcher;
import dev.caecorthus.sparkdlc.round.modevote.ModeVotingComponent;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Opens the game-mode vote, the counterpart of Wathe's {@code /wathe:mapvote}.
 * <p>
 * 唤起游戏模式投票，与 {@code /wathe:mapvote} 对应。
 */
public final class ModeVoteCommand {

    private ModeVoteCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sparkdlc:modevote")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ModeVotingComponent voting = ModeVotingComponent.KEY.get(
                            context.getSource().getServer().getScoreboard());
                    if (voting.isVotingActive()) {
                        context.getSource().sendError(Text.literal("Mode voting is already active"));
                        return 0;
                    }
                    voting.startVoting();
                    return 1;
                })
        );
    }
}
