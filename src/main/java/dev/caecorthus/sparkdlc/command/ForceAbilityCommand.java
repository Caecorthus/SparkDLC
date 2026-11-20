package dev.caecorthus.sparkdlc.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.legacy.LegacyCommandIds;
import dev.caecorthus.sparkdlc.legacy.LegacyPermissions;
import dev.caecorthus.sparkdlc.skill.api.WitchSkillDefinition;
import dev.caecorthus.sparkdlc.skill.api.WitchSkillRegistry;
import dev.caecorthus.sparkdlc.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.skill.WitchSkillLockValidationService;
import dev.doctor4t.wathe.Wathe;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Registers the /sparkdlc:forceAbility command for next-round ability locks.
 * 注册 /sparkdlc:forceAbility 命令，用于锁定玩家下一局魔女能力。
 */
public final class ForceAbilityCommand {
    private static final DynamicCommandExceptionType UNKNOWN_SKILL =
            new DynamicCommandExceptionType(id -> Text.literal("Unknown witch ability: " + id));

    private ForceAbilityCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("sparkdlc:forceAbility")
                        .requires(LegacyPermissions.require(
                                SparkDlcPermissions.COMMAND_FORCE_ABILITY,
                                SparkDlcPermissions.DEFAULT_COMMAND_LEVEL
                        ))
                        .then(CommandManager.argument("ability", IdentifierArgumentType.identifier())
                                .suggests(ForceAbilityCommand::suggestSkills)
                                .then(CommandManager.argument("players", EntityArgumentType.players())
                                        .executes(context -> execute(
                                                context.getSource(),
                                                requireSkill(getSkillId(context)),
                                                EntityArgumentType.getPlayers(context, "players")
                                        ))))
        );
    }

    private static int execute(
            ServerCommandSource source,
            WitchSkillDefinition skill,
            Collection<ServerPlayerEntity> players
    ) {
        return Wathe.executeSupporterCommand(source, () -> {
            int changed = 0;
            String singleChangedPlayerName = null;
            for (ServerPlayerEntity player : players) {
                WitchSkillLockValidationService.RoleConflict conflict =
                        WitchSkillLockValidationService.findForcedRoleConflict(source, player, skill);
                if (conflict != null) {
                    source.sendFeedback(() -> Text.literal("Skipped " + player.getGameProfile().getName()
                            + ": " + WitchSkillLockValidationService.forceAbilityConflictMessage(conflict)), false);
                    continue;
                }

                if (WitchPlayerComponent.KEY.get(player).setForcedSkill(skill.id())) {
                    changed++;
                    singleChangedPlayerName = player.getGameProfile().getName();
                }
            }
            int finalChanged = changed;
            String finalSingleChangedPlayerName = finalChanged == 1 ? singleChangedPlayerName : null;
            source.sendFeedback(() -> formatForceAbilityFeedback(skill, finalChanged, finalSingleChangedPlayerName), true);
        });
    }

    static Text formatForceAbilityFeedback(WitchSkillDefinition skill, int changed, String singleChangedPlayerName) {
        MutableText message = Text.literal("Forced witch ability ")
                .append(Text.literal(formatSkillIdForCommand(skill.id())).withColor(skill.color()))
                .append(Text.literal(" for "));
        if (changed == 1 && singleChangedPlayerName != null) {
            return message.append(Text.literal(singleChangedPlayerName + "."));
        }
        return message.append(Text.literal(changed + " player(s)."));
    }

    private static Identifier getSkillId(CommandContext<ServerCommandSource> context) {
        return normalizeSkillId(IdentifierArgumentType.getIdentifier(context, "ability"));
    }

    static Identifier normalizeSkillId(Identifier parsed) {
        if (parsed.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return SparkDLC.id(parsed.getPath());
        }
        // An id typed in the former namespace (sparkwitch:x) means today's skill. / 以旧命名空间输入的 id（sparkwitch:x）指现在的技能。
        return LegacyCommandIds.skill(parsed);
    }

    private static WitchSkillDefinition requireSkill(Identifier skillId) throws CommandSyntaxException {
        WitchSkillDefinition skill = WitchSkillRegistry.get(skillId);
        if (skill == null) {
            throw UNKNOWN_SKILL.create(skillId);
        }
        return skill;
    }

    static CompletableFuture<Suggestions> suggestSkills(
            CommandContext<ServerCommandSource> context,
            SuggestionsBuilder builder
    ) {
        for (WitchSkillDefinition skill : WitchSkillRegistry.values()) {
            String suggestion = formatSkillIdForCommand(skill.id());
            if (CommandSource.shouldSuggest(builder.getRemaining(), suggestion)) {
                builder.suggest(suggestion);
            }
        }
        return builder.buildFuture();
    }

    public static String formatSkillIdForCommand(Identifier skillId) {
        if (skillId.getNamespace().equals(SparkDLC.MOD_ID)) {
            return skillId.getPath();
        }
        return skillId.toString();
    }
}
