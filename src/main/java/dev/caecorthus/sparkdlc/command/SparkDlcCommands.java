package dev.caecorthus.sparkdlc.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.caecorthus.sparkdlc.command.admin.AntiFastRoundCommand;
import dev.caecorthus.sparkdlc.command.admin.CooldownCommand;
import dev.caecorthus.sparkdlc.command.admin.SanityCommand;
import dev.caecorthus.sparkdlc.command.admin.TaskCommand;
import dev.caecorthus.sparkdlc.command.replay.ReplayCommand;
import dev.caecorthus.sparkdlc.command.trait.SparkTraitsCommands;
import dev.caecorthus.sparkdlc.legacy.LegacyCommandAliases;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

/**
 * The one place where SparkDLC registers commands. Until the fusion each of the five mods hooked
 * {@code CommandRegistrationCallback} on its own (five listeners in five classes); now there is one listener, and
 * {@link #registerAll} lists every command of the mod.
 *
 * <p>The order is the order the separate listeners ran in (the former mods started alphabetically: sparkfactionapi
 * with its replay command first, sparkstrength, sparktraits, sparkwitch), and each command's tree, permission
 * requirement and feedback are still built by its own class. Two commands are not in this list because they are not
 * registered through the callback: {@code /wathe:gameSettings set roleDividend third_faction} is attached to Wathe's
 * own tree by {@code GameSettingsCommandThirdFactionMixin} when Wathe registers it, and the list output of
 * {@code /wathe:gameSettings listRoles} is a mixin into Wathe's command.</p>
 *
 * <p>The old command names ({@code /sparkwitch:setMana}, ...) are added last, as aliases of the roots registered
 * above; see {@link LegacyCommandAliases}.</p>
 *
 * SparkDLC 注册命令的唯一位置。融合之前五个模组各自监听 {@code CommandRegistrationCallback}（五个类里共五个监听器）；现在只有
 * 一个监听器，{@link #registerAll} 列出本模组的全部命令。
 *
 * <p>顺序与原先各监听器的执行顺序一致（旧模组按字母序启动：sparkfactionapi 及其回放命令在最前，然后是 sparkstrength、
 * sparktraits、sparkwitch），每条命令的命令树、权限要求和反馈仍由它自己的类构建。有两条命令不在此列表中，因为它们不经由该回调
 * 注册：{@code /wathe:gameSettings set roleDividend third_faction} 由 {@code GameSettingsCommandThirdFactionMixin} 在
 * Wathe 注册自己的命令树时挂接；{@code /wathe:gameSettings listRoles} 的列表输出则是对 Wathe 命令的 mixin。</p>
 *
 * <p>旧命令名（{@code /sparkwitch:setMana} 等）在最后添加，作为上面所注册根节点的别名，见 {@link LegacyCommandAliases}。</p>
 */
public final class SparkDlcCommands {
    private SparkDlcCommands() {
    }

    /** Hooks the one listener; called once from {@code SparkDLC#onInitialize}. / 挂接唯一的监听器；由 {@code SparkDLC#onInitialize} 调用一次。 */
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                registerAll(dispatcher));
    }

    /** Registers every command of the mod, then the old names. / 注册本模组的全部命令，然后注册旧命令名。 */
    public static void registerAll(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Replay: /replay, /replay chat (players only, no permission node).
        // 回放：/replay、/replay chat（仅限玩家，无权限节点）。
        ReplayCommand.register(dispatcher);

        // Administrator tools of the faction layer (node sparkdlc.command.admin).
        // 阵营层的管理工具（节点 sparkdlc.command.admin）。
        CooldownCommand.register(dispatcher);
        TaskCommand.register(dispatcher);
        SanityCommand.register(dispatcher);
        AntiFastRoundCommand.register(dispatcher);

        // Tablet, detective and player-state commands (op level 2), with their three misspelt aliases.
        // 平板、侦探与玩家状态命令（2 级 OP），以及它们的三个拼写别名。
        SparkStrengthCommands.register(dispatcher);

        // Trait commands (op level 2; listTraits and the traitSlotRollChance query are open).
        // 词条命令（2 级 OP；listTraits 与 traitSlotRollChance 的查询不设限制）。
        SparkTraitsCommands.register(dispatcher);

        // Witch faction commands (one node each) and the ghost branch of /wathe:gameSettings.
        // 魔女阵营命令（各有一个节点）以及 /wathe:gameSettings 的 ghost 分支。
        SetManaCommand.register(dispatcher);
        ForceAbilityCommand.register(dispatcher);
        ForcePromotionCommand.register(dispatcher);
        ForceAccompliceRoleCommand.register(dispatcher);
        GhostSettingsCommand.register(dispatcher);
        WatheGhostDividendCommand.register(dispatcher);
        ThirdFactionSettingsCommand.register(dispatcher);
        WitchFactorCommand.register(dispatcher);

        // Old names last: an alias copies the finished root it stands for.
        // 旧命令名放在最后：别名复制的是它所代表的、已经注册完成的根节点。
        LegacyCommandAliases.register(dispatcher);
    }
}
