package dev.caecorthus.sparkdlc.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.caecorthus.sparkdlc.health.BodyHealthComponent;
import dev.caecorthus.sparkdlc.health.BodyHealthRules;
import dev.caecorthus.sparkdlc.health.BodyPart;
import dev.caecorthus.sparkdlc.health.HealthModeComponent;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * {@code /sparkdlc:healthMode [on|off | set <targets> <part> <value> | reset <targets>]}: shows or changes the
 * server-wide switch of the body-health system, and edits the stored body health of players. {@code set} and
 * {@code reset} work whether the switch is on or off, because the values are kept either way. Administrator tool
 * (node {@code sparkdlc.command.admin}); it sits outside {@code command.admin} because that package is shared core
 * and must not name this feature. Feedback uses translation keys with English fallbacks so dedicated-server consoles
 * stay readable.
 * {@code /sparkdlc:healthMode [on|off | set <targets> <part> <value> | reset <targets>]}：查看或修改部位血量系统的全服
 * 开关，并编辑玩家已存储的部位血量。{@code set} 与 {@code reset} 不受开关影响，因为无论开关如何数值都会保留。属于管理
 * 工具（节点 {@code sparkdlc.command.admin}）；它不放在 {@code command.admin} 中，因为该包属于共享核心，不得引用本功能。
 * 反馈使用带英文回退的翻译键，保证专用服务器控制台可读。
 */
public final class HealthModeCommand {
    private static final String KEY_PREFIX = "commands.sparkdlc.healthmode.";
    private static final DynamicCommandExceptionType UNKNOWN_PART = new DynamicCommandExceptionType(
            part -> Text.translatableWithFallback(KEY_PREFIX + "unknown_part", "Unknown body part: %s", part));
    private static final Dynamic3CommandExceptionType OUT_OF_RANGE = new Dynamic3CommandExceptionType(
            (part, max, value) -> Text.translatableWithFallback(
                    KEY_PREFIX + "out_of_range",
                    "The health of %s must be between 0 and %s, got %s",
                    part, max, value
            ));

    private HealthModeCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sparkdlc:healthMode")
                .requires(SparkDlcPermissions.admin())
                .executes(context -> showStatus(context.getSource()))
                .then(CommandManager.literal("on")
                        .executes(context -> setEnabled(context.getSource(), true)))
                .then(CommandManager.literal("off")
                        .executes(context -> setEnabled(context.getSource(), false)))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.argument("part", StringArgumentType.word())
                                        .suggests(HealthModeCommand::suggestParts)
                                        // The upper bound depends on the part, so it is checked in setHealth.
                                        // 上限取决于部位，因此在 setHealth 中校验。
                                        .then(CommandManager.argument("value", IntegerArgumentType.integer(0))
                                                .executes(HealthModeCommand::setHealth)))))
                .then(CommandManager.literal("reset")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(context -> reset(
                                        context.getSource(),
                                        EntityArgumentType.getPlayers(context, "targets")
                                )))));
    }

    private static int showStatus(ServerCommandSource source) {
        if (mode(source).isEnabled()) {
            source.sendFeedback(() -> Text.translatableWithFallback(
                    KEY_PREFIX + "status.enabled",
                    "Health mode is on"
            ), false);
        } else {
            source.sendFeedback(() -> Text.translatableWithFallback(
                    KEY_PREFIX + "status.disabled",
                    "Health mode is off"
            ), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setEnabled(ServerCommandSource source, boolean enabled) {
        mode(source).setEnabled(enabled);
        if (enabled) {
            source.sendFeedback(() -> Text.translatableWithFallback(
                    KEY_PREFIX + "enabled",
                    "Health mode turned on"
            ), true);
        } else {
            source.sendFeedback(() -> Text.translatableWithFallback(
                    KEY_PREFIX + "disabled",
                    "Health mode turned off"
            ), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealth(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        // Part and value are checked before the selector runs, so a mistyped part or an out-of-range value is
        // reported as such even when the selector finds nobody.
        // 先校验部位与数值再解析选择器，这样即使选择器没有选中任何人，写错的部位或越界的数值也会如实报错。
        BodyPart part = part(context);
        int value = IntegerArgumentType.getInteger(context, "value");
        if (!BodyHealthRules.isInRange(part, value)) {
            throw OUT_OF_RANGE.create(part.id(), part.maxHealth(), value);
        }
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        for (ServerPlayerEntity target : targets) {
            BodyHealthComponent.KEY.get(target).setHealth(part, value);
        }
        if (targets.size() == 1) {
            Text name = targets.iterator().next().getDisplayName();
            source.sendFeedback(() -> Text.translatableWithFallback(
                    KEY_PREFIX + "set.single",
                    "Set the %s health of %s to %s/%s",
                    part.id(), name, value, part.maxHealth()
            ), true);
        } else {
            source.sendFeedback(() -> Text.translatableWithFallback(
                    KEY_PREFIX + "set.multiple",
                    "Set the %s health of %s players to %s/%s",
                    part.id(), targets.size(), value, part.maxHealth()
            ), true);
        }
        return targets.size();
    }

    private static int reset(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        for (ServerPlayerEntity target : targets) {
            BodyHealthComponent.KEY.get(target).restoreAll();
        }
        if (targets.size() == 1) {
            Text name = targets.iterator().next().getDisplayName();
            source.sendFeedback(() -> Text.translatableWithFallback(
                    KEY_PREFIX + "reset.single",
                    "Restored the body health of %s",
                    name
            ), true);
        } else {
            source.sendFeedback(() -> Text.translatableWithFallback(
                    KEY_PREFIX + "reset.multiple",
                    "Restored the body health of %s players",
                    targets.size()
            ), true);
        }
        return targets.size();
    }

    private static HealthModeComponent mode(ServerCommandSource source) {
        return HealthModeComponent.KEY.get(source.getServer().getScoreboard());
    }

    private static BodyPart part(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String raw = StringArgumentType.getString(context, "part");
        BodyPart part = BodyPart.byId(raw);
        if (part == null) {
            throw UNKNOWN_PART.create(raw);
        }
        return part;
    }

    private static CompletableFuture<Suggestions> suggestParts(
            CommandContext<ServerCommandSource> context,
            SuggestionsBuilder builder
    ) {
        for (BodyPart part : BodyPart.values()) {
            if (part.id().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(part.id());
            }
        }
        return builder.buildFuture();
    }
}
