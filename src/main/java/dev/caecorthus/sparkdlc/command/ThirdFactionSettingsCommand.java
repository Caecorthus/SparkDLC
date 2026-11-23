package dev.caecorthus.sparkdlc.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.legacy.LegacyPermissions;
import dev.doctor4t.wathe.Wathe;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Registers the third-faction player minimum (owner 2026-11-16). The value lives in the faction core, which owns the
 * round-start third-faction draw and reads it when the next round begins; 0 means no minimum. The member-cap dividend
 * stays on Wathe's {@code /wathe:gameSettings set roleDividend third_faction}, which the faction core attaches.
 * 注册第三阵营最低人数命令（所有者 2026-11-16）。数值保存在负责开局第三阵营抽取的阵营核心中，下一局开始时读取；
 * 0 表示没有最低人数。成员上限除数仍由阵营核心挂在 Wathe 的
 * {@code /wathe:gameSettings set roleDividend third_faction} 上。
 */
public final class ThirdFactionSettingsCommand {
    private ThirdFactionSettingsCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sparkdlc:thirdFactionMinRequirement")
                .requires(LegacyPermissions.require(
                        SparkDlcPermissions.COMMAND_THIRD_FACTION_MIN_REQUIREMENT,
                        SparkDlcPermissions.DEFAULT_COMMAND_LEVEL))
                .then(CommandManager.argument("players", IntegerArgumentType.integer(0))
                        .executes(context -> setMinimum(
                                context.getSource(), IntegerArgumentType.getInteger(context, "players")))));
    }

    /**
     * Returns 0 when the supporter gate did not run the change.
     * 支持者门控未执行更改时返回 0。
     */
    private static int setMinimum(ServerCommandSource source, int players) {
        boolean[] applied = {false};
        int result = Wathe.executeSupporterCommand(source, () -> {
            SparkFactionApi.setThirdFactionMinPlayers(source.getServer(), players);
            applied[0] = true;
            source.sendFeedback(() -> Text.translatable(
                    "command.sparkdlc.third_faction_min_requirement.success", players), true);
        });
        return applied[0] ? result : 0;
    }
}
