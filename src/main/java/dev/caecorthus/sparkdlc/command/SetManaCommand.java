package dev.caecorthus.sparkdlc.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.caecorthus.sparkdlc.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.legacy.LegacyPermissions;
import dev.caecorthus.sparkdlc.util.RoleCommandPermissions;
import dev.doctor4t.wathe.Wathe;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

/**
 * Registers the /sparkdlc:setMana command.
 * 注册 /sparkdlc:setMana 命令，用于直接设定玩家魔力值。
 */
public final class SetManaCommand {
    private SetManaCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("sparkdlc:setMana")
                        .requires(LegacyPermissions.require(
                                RoleCommandPermissions.COMMAND_SET_MANA,
                                RoleCommandPermissions.DEFAULT_COMMAND_LEVEL
                        ))
                        .then(
                                CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(context -> execute(
                                                context.getSource(),
                                                ImmutableList.of(context.getSource().getPlayerOrThrow()),
                                                IntegerArgumentType.getInteger(context, "amount")
                                        ))
                                        .then(
                                                CommandManager.argument("targets", EntityArgumentType.players())
                                                        .executes(context -> execute(
                                                                context.getSource(),
                                                                EntityArgumentType.getPlayers(context, "targets"),
                                                                IntegerArgumentType.getInteger(context, "amount")
                                                        ))
                                        )
                        )
        );
    }

    private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int amount) {
        return Wathe.executeSupporterCommand(source, () -> {
            for (ServerPlayerEntity target : targets) {
                WitchPlayerComponent.KEY.get(target).setMana(amount);
            }
        });
    }
}
