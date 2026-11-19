package dev.caecorthus.sparkdlc.gametest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.caecorthus.sparkdlc.legacy.LegacyCommandAliases;
import dev.caecorthus.sparkdlc.legacy.LegacyCommandIds;
import dev.caecorthus.sparkdlc.legacy.LegacyIds;
import dev.caecorthus.sparkdlc.legacy.LegacyPermissions;
import dev.caecorthus.sparkdlc.skill.api.WitchSkillRegistry;
import dev.caecorthus.sparkdlc.trait.api.TraitRegistry;
import dev.caecorthus.sparkdlc.trait.component.TraitWorldComponent;
import io.netty.buffer.Unpooled;
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.IntPredicate;

/**
 * Commands against the real server dispatcher: the full list of what the mod registers (pinned, so a change is
 * deliberate), and the compatibility layer for old names: every command name of the five former mods is the same
 * command as its current name, ids typed in a former namespace resolve, and a permission provider that still grants
 * or denies a former node is honoured.
 * 针对真实服务器分发器的命令测试：本模组注册的完整命令列表（固定下来，任何改动都必须是有意的），以及旧名称兼容层：五个旧模组的
 * 每个命令名与其当前名字是同一条命令，以旧命名空间输入的 id 能够解析，仍在授予或拒绝旧节点的权限提供者继续生效。
 */
public final class SparkDlcCommandTest implements FabricGameTest {
    /**
     * Every executable path of the mod's own roots, in registration order (the order the five mods registered in
     * before the fusion): 44 current paths, {@code /replay}, and the three misspelt aliases SparkStrength always had.
     * 本模组自有根节点的全部可执行路径，按注册顺序排列（即融合前五个模组的注册顺序）：44 条当前路径、{@code /replay}，以及
     * SparkStrength 一直保留的三个拼写别名。
     */
    private static final List<String> CURRENT_COMMANDS = List.of(
            "replay",
            "replay chat",
            "sparkdlc:clearCooldown",
            "sparkdlc:clearCooldown <targets>",
            "sparkdlc:addTask <task>",
            "sparkdlc:addTask <task> <targets>",
            "sparkdlc:deleteTask <task>",
            "sparkdlc:deleteTask <task> <targets>",
            "sparkdlc:completeTask <task>",
            "sparkdlc:completeTask <task> <targets>",
            "sparkdlc:setSanity <value>",
            "sparkdlc:setSanity <value> <targets>",
            "sparkdlc:antifastround",
            "sparkdlc:antifastround <enabled>",
            "sparkdlc:antifastround <enabled> <seconds>",
            "sparkdlc:emergencyMeetingChances <num>",
            "sparkdlc:voteTime <sec>",
            "sparkstength:voteTime <sec>",
            "sparkdlc:detectiveCaseLimit",
            "sparkdlc:detectiveCaseLimit <num>",
            "sparkStrength:detectiveCaseLimit",
            "sparkStrength:detectiveCaseLimit <num>",
            "sparkStrength:tabletChatInterval",
            "sparkStrength:tabletChatInterval <sec>",
            "sparkdlc:tabletChatInterval",
            "sparkdlc:tabletChatInterval <sec>",
            "sparkdlc:setPlayerAlive <player>",
            "sparkdlc:setPlayerDead <player>",
            "sparkdlc:listTraits",
            "sparkdlc:enableTrait <trait>",
            "sparkdlc:disableTrait <trait>",
            "sparkdlc:addTrait <trait> <players>",
            "sparkdlc:removeTrait <trait> <players>",
            "sparkdlc:clearTraits <players>",
            "sparkdlc:listPlayerTraits <players>",
            "sparkdlc:traitSlotRollChance",
            "sparkdlc:traitSlotRollChance <percent>",
            "sparkdlc:setMana <amount>",
            "sparkdlc:setMana <amount> <targets>",
            "sparkdlc:forceAbility <ability> <players>",
            "sparkdlc:forcePromotion <promotionRole> <players>",
            "sparkdlc:forceAccompliceRole <role> <players>",
            "sparkdlc:ghostChance <chance>",
            "sparkdlc:ghostMinRequirement <players>",
            "sparkdlc:thirdFactionMinRequirement <players>",
            "sparkdlc:witchFactor",
            "sparkdlc:witchFactor Rule Divide",
            "sparkdlc:witchFactor Rule MaxCarrier",
            "sparkdlc:witchFactor DivideMode <num>",
            "sparkdlc:witchFactor MaxCarrierMode unlimited",
            "sparkdlc:witchFactor MaxCarrierMode <num>");

    /** The two branches the mod adds to Wathe's own command. / 本模组挂在 Wathe 自有命令下的两个分支。 */
    private static final List<String> WATHE_BRANCHES = List.of(
            "wathe:gameSettings set roleDividend third_faction <dividend>",
            "wathe:gameSettings set roleDividend ghost <dividend>");

    /**
     * Old command name to current name: the 28 roots of the five former mods, in the order the aliases are added.
     * Must equal the {@code command} rows of {@code tools/unify/legacy-ids.tsv}.
     * 旧命令名到当前命令名：五个旧模组的 28 个根节点，按别名添加顺序排列。必须与 {@code tools/unify/legacy-ids.tsv} 的
     * {@code command} 行一致。
     */
    private static final Map<String, String> OLD_NAMES = oldNames(
            "sparkfactionapi:addTask", "sparkfactionapi:antifastround", "sparkfactionapi:clearCooldown",
            "sparkfactionapi:completeTask", "sparkfactionapi:deleteTask", "sparkfactionapi:setSanity",
            "sparkstrength:detectiveCaseLimit", "sparkstrength:emergencyMeetingChances",
            "sparkstrength:setPlayerAlive", "sparkstrength:setPlayerDead", "sparkstrength:tabletChatInterval",
            "sparkstrength:voteTime",
            "sparktraits:addTrait", "sparktraits:clearTraits", "sparktraits:disableTrait", "sparktraits:enableTrait",
            "sparktraits:listPlayerTraits", "sparktraits:listTraits", "sparktraits:removeTrait",
            "sparktraits:traitSlotRollChance",
            "sparkwitch:forceAbility", "sparkwitch:forceAccompliceRole", "sparkwitch:forcePromotion",
            "sparkwitch:ghostChance", "sparkwitch:ghostMinRequirement", "sparkwitch:setMana",
            "sparkwitch:thirdFactionMinRequirement", "sparkwitch:witchFactor");

    /**
     * One input per shape of every current {@code sparkdlc:} command, parsed under both names. Selectors only have
     * to parse; nothing here is executed.
     * 每条当前 {@code sparkdlc:} 命令的每种形态各一条输入，用新旧两个名字解析。选择器只需能解析；这里不执行任何命令。
     */
    private static final List<String> SAMPLE_INPUTS = List.of(
            "sparkdlc:clearCooldown", "sparkdlc:clearCooldown @a",
            "sparkdlc:addTask Sleep", "sparkdlc:addTask All @a",
            "sparkdlc:deleteTask Eat", "sparkdlc:deleteTask Drink @a",
            "sparkdlc:completeTask outside", "sparkdlc:completeTask Breathe_Air @a",
            "sparkdlc:setSanity 50", "sparkdlc:setSanity -12.5 @a",
            "sparkdlc:antifastround", "sparkdlc:antifastround true", "sparkdlc:antifastround false 30",
            "sparkdlc:emergencyMeetingChances 1",
            "sparkdlc:voteTime 60",
            "sparkdlc:detectiveCaseLimit", "sparkdlc:detectiveCaseLimit 2",
            "sparkdlc:tabletChatInterval", "sparkdlc:tabletChatInterval 1.5",
            "sparkdlc:setPlayerAlive @p", "sparkdlc:setPlayerDead @p",
            "sparkdlc:listTraits",
            "sparkdlc:enableTrait anxiety", "sparkdlc:disableTrait sparkdlc:anxiety",
            "sparkdlc:addTrait anxiety @a", "sparkdlc:removeTrait sparktraits:anxiety @a",
            "sparkdlc:clearTraits @a", "sparkdlc:listPlayerTraits @a",
            "sparkdlc:traitSlotRollChance", "sparkdlc:traitSlotRollChance 50",
            "sparkdlc:setMana 5", "sparkdlc:setMana 5 @a",
            "sparkdlc:forceAbility bell_echo @a",
            "sparkdlc:forcePromotion curser @a",
            "sparkdlc:forceAccompliceRole accomplice @a",
            "sparkdlc:ghostChance 50", "sparkdlc:ghostMinRequirement 4",
            "sparkdlc:thirdFactionMinRequirement 4",
            "sparkdlc:witchFactor", "sparkdlc:witchFactor Rule Divide", "sparkdlc:witchFactor Rule MaxCarrier",
            "sparkdlc:witchFactor DivideMode 3", "sparkdlc:witchFactor MaxCarrierMode unlimited",
            "sparkdlc:witchFactor MaxCarrierMode 2");

    /**
     * Current permission node to the commands it guards. The nodes and their former names come from the legacy
     * table; which command asks which node is pinned here.
     * 当前权限节点到它所保护命令的映射。节点及其旧名字来自对照表；哪条命令询问哪个节点固定在这里。
     */
    private static final Map<String, List<String>> NODE_COMMANDS = Map.of(
            "sparkdlc.command.admin", List.of("sparkdlc:clearCooldown", "sparkdlc:addTask", "sparkdlc:deleteTask",
                    "sparkdlc:completeTask", "sparkdlc:setSanity", "sparkdlc:antifastround"),
            "sparkdlc.command.setmana", List.of("sparkdlc:setMana"),
            "sparkdlc.command.forceability", List.of("sparkdlc:forceAbility"),
            "sparkdlc.command.forcepromotion", List.of("sparkdlc:forcePromotion"),
            "sparkdlc.command.forceaccomplicerole", List.of("sparkdlc:forceAccompliceRole"),
            "sparkdlc.command.ghostchance", List.of("sparkdlc:ghostChance"),
            "sparkdlc.command.ghostminrequirement", List.of("sparkdlc:ghostMinRequirement"),
            "sparkdlc.command.thirdfactionminrequirement", List.of("sparkdlc:thirdFactionMinRequirement"),
            "sparkdlc.command.witch_factor", List.of("sparkdlc:witchFactor"));
    /** The one renamed node that guards an item instead of a command. / 唯一保护物品而非命令的改名节点。 */
    private static final String ITEM_NODE = "sparkdlc.item.riftgateremover";
    /** Commands that never had a permission node: only the op level decides. / 从来没有权限节点的命令：只由 OP 等级决定。 */
    private static final List<String> COMMANDS_WITHOUT_A_NODE = List.of(
            "sparkdlc:emergencyMeetingChances", "sparkdlc:voteTime", "sparkstength:voteTime",
            "sparkdlc:detectiveCaseLimit", "sparkStrength:detectiveCaseLimit", "sparkdlc:tabletChatInterval",
            "sparkStrength:tabletChatInterval", "sparkdlc:setPlayerAlive", "sparkdlc:setPlayerDead",
            "sparkdlc:enableTrait", "sparkdlc:disableTrait", "sparkdlc:addTrait", "sparkdlc:removeTrait",
            "sparkdlc:clearTraits", "sparkdlc:listPlayerTraits");

    /**
     * Stand-in for a permission provider such as LuckPerms: answers only for the probe source of this class, from
     * {@link #PROVIDER_ANSWERS}, falling back to {@link #providerDefault}.
     * 代替 LuckPerms 之类的权限提供者：只对本类的探测命令源作答，答案取自 {@link #PROVIDER_ANSWERS}，否则为
     * {@link #providerDefault}。
     */
    private static final String PROBE_NAME = "SparkDlcCommandTest permission probe";
    private static final Map<String, TriState> PROVIDER_ANSWERS = new HashMap<>();
    private static TriState providerDefault = TriState.DEFAULT;
    private static boolean providerRegistered;

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void theCommandListIsPinned(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();
        if (!OLD_NAMES.equals(LegacyCommandAliases.names())
                || !List.copyOf(OLD_NAMES.keySet()).equals(List.copyOf(LegacyCommandAliases.names().keySet()))) {
            throw new GameTestException("the command rows of legacy-ids.tsv changed: expected " + OLD_NAMES
                    + ", found " + LegacyCommandAliases.names());
        }
        List<String> expected = new ArrayList<>(CURRENT_COMMANDS);
        for (Map.Entry<String, String> name : OLD_NAMES.entrySet()) {
            int before = expected.size();
            for (String command : CURRENT_COMMANDS) {
                if (rootOf(command).equals(name.getValue())) {
                    expected.add(name.getKey() + command.substring(name.getValue().length()));
                }
            }
            if (expected.size() == before) {
                throw new GameTestException("no pinned command under " + name.getValue());
            }
        }
        List<String> own = new ArrayList<>();
        List<String> all = List.of(dispatcher.getAllUsage(dispatcher.getRoot(), server.getCommandSource(), false));
        for (String command : all) {
            String root = rootOf(command);
            if (root.equals("replay") || root.regionMatches(true, 0, "spark", 0, 5)) {
                own.add(command);
            }
        }
        if (!own.equals(expected)) {
            Set<String> missing = new TreeSet<>(expected);
            own.forEach(missing::remove);
            Set<String> unexpected = new TreeSet<>(own);
            expected.forEach(unexpected::remove);
            throw new GameTestException("the commands of the mod changed (or their order). Missing: " + missing
                    + "; not pinned: " + unexpected + "; registered: " + own);
        }
        for (String branch : WATHE_BRANCHES) {
            if (!all.contains(branch)) {
                throw new GameTestException("missing " + branch);
            }
        }
        context.complete();
    }

    /**
     * An old name must be the same command, not a look-alike: the same executor and requirement objects on the
     * root, the same child nodes, and for every input the same parse (same nodes after the root, same executor,
     * nothing left over).
     * 旧名字必须是同一条命令，而不是相似的另一条：根节点上是同一个执行器与权限要求对象，子节点相同，并且每条输入的解析结果相同
     * （根节点之后的节点相同、执行器相同、没有剩余输入）。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void everyOldCommandNameIsTheSameCommand(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();
        ServerCommandSource source = server.getCommandSource();
        Map<String, String> oldNameOf = new LinkedHashMap<>();
        for (Map.Entry<String, String> name : LegacyCommandAliases.names().entrySet()) {
            CommandNode<ServerCommandSource> current = dispatcher.getRoot().getChild(name.getValue());
            CommandNode<ServerCommandSource> old = dispatcher.getRoot().getChild(name.getKey());
            if (current == null || old == null) {
                throw new GameTestException("missing command " + (current == null ? name.getValue() : name.getKey()));
            }
            if (old == current || old.getCommand() != current.getCommand()
                    || old.getRequirement() != current.getRequirement() || old.getRedirect() != null
                    || !sameNodes(List.copyOf(old.getChildren()), List.copyOf(current.getChildren()))) {
                throw new GameTestException(name.getKey() + " is not the same command as " + name.getValue());
            }
            oldNameOf.put(name.getValue(), name.getKey());
        }
        Set<String> sampled = new TreeSet<>();
        for (String input : SAMPLE_INPUTS) {
            String currentRoot = rootOf(input);
            String oldRoot = oldNameOf.get(currentRoot);
            if (oldRoot == null) {
                throw new GameTestException("sample of a command without an old name: " + input);
            }
            sampled.add(currentRoot);
            ParseResults<ServerCommandSource> current = parseFully(dispatcher, input, source);
            ParseResults<ServerCommandSource> old =
                    parseFully(dispatcher, oldRoot + input.substring(currentRoot.length()), source);
            List<CommandNode<ServerCommandSource>> currentNodes = nodes(current);
            List<CommandNode<ServerCommandSource>> oldNodes = nodes(old);
            if (old.getContext().getCommand() != current.getContext().getCommand()
                    || !sameNodes(oldNodes.subList(1, oldNodes.size()), currentNodes.subList(1, currentNodes.size()))) {
                throw new GameTestException("'" + input + "' does not parse to the same command under " + oldRoot);
            }
        }
        if (!sampled.equals(new TreeSet<>(oldNameOf.keySet()))) {
            throw new GameTestException("every renamed command needs a sample input; sampled " + sampled);
        }
        context.complete();
    }

    /**
     * What a client is sent. An old name shares its child nodes with the current name on the server; the tree built
     * for a player and the command tree packet must cope with that, and the tree a client rebuilds from the packet
     * must know both names with the same shape, so an old name typed in chat is recognised and its arguments are
     * completed. ({@code makeTreeForSource} is private; the server calls it from {@code sendCommandTree}.)
     * 客户端收到的内容。旧名字在服务端与当前名字共用子节点；为玩家构建的命令树和命令树数据包必须能处理这一点，客户端根据数据包
     * 重建的命令树也必须以相同的结构包含两个名字，这样在聊天栏输入旧名字时能被识别并获得参数补全。
     * （{@code makeTreeForSource} 是私有方法；服务器在 {@code sendCommandTree} 中调用它。）
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void theCommandTreeSentToClientsKnowsBothNames(TestContext context) throws ReflectiveOperationException {
        MinecraftServer server = context.getWorld().getServer();
        CommandManager commands = server.getCommandManager();
        ServerCommandSource source = server.getCommandSource();
        RootCommandNode<CommandSource> built = new RootCommandNode<>();
        Map<CommandNode<ServerCommandSource>, CommandNode<CommandSource>> builtNodes = new HashMap<>();
        builtNodes.put(commands.getDispatcher().getRoot(), built);
        Method makeTreeForSource = CommandManager.class.getDeclaredMethod("makeTreeForSource", CommandNode.class,
                CommandNode.class, ServerCommandSource.class, Map.class);
        makeTreeForSource.setAccessible(true);
        makeTreeForSource.invoke(commands, commands.getDispatcher().getRoot(), built, source, builtNodes);

        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        CommandTreeS2CPacket.CODEC.encode(buffer, new CommandTreeS2CPacket(built));
        RootCommandNode<CommandSource> received = CommandTreeS2CPacket.CODEC.decode(buffer).getCommandTree(
                CommandRegistryAccess.of(server.getRegistryManager(), server.getSaveProperties().getEnabledFeatures()));
        CommandDispatcher<CommandSource> client = new CommandDispatcher<>(received);
        for (Map.Entry<String, String> name : LegacyCommandAliases.names().entrySet()) {
            CommandNode<CommandSource> current = received.getChild(name.getValue());
            CommandNode<CommandSource> old = received.getChild(name.getKey());
            if (current == null || old == null) {
                throw new GameTestException("the client tree lacks " + (current == null ? name.getValue() : name.getKey()));
            }
            if ((old.getCommand() == null) != (current.getCommand() == null)
                    || !List.copyOf(old.getChildren()).equals(List.copyOf(current.getChildren()))) {
                throw new GameTestException("the client sees " + name.getKey() + " with another shape than "
                        + name.getValue());
            }
        }
        for (String input : SAMPLE_INPUTS) {
            String currentRoot = rootOf(input);
            String oldRoot = LegacyCommandAliases.names().entrySet().stream()
                    .filter(name -> name.getValue().equals(currentRoot)).findFirst().orElseThrow().getKey();
            for (String typed : List.of(input, oldRoot + input.substring(currentRoot.length()))) {
                ParseResults<CommandSource> parse = client.parse(typed, source);
                if (parse.getReader().canRead() || !parse.getExceptions().isEmpty()
                        || parse.getContext().getCommand() == null) {
                    throw new GameTestException("a client would show '" + typed + "' as an unknown command");
                }
            }
        }
        context.complete();
    }

    /**
     * End to end through {@code CommandManager}, both ways: a root that is executable by itself (the case a
     * Brigadier redirect cannot serve), a query, and a change made under the old name that the new name reads back.
     * Each run answers exactly once.
     * 经由 {@code CommandManager} 的端到端执行，新旧名字各一遍：本身可执行的根节点（Brigadier redirect 无法处理的情形）、一次
     * 查询，以及用旧名字做出、再用新名字读回的修改。每次执行恰好应答一次。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void anOldCommandNameRunsTheSameCommand(TestContext context) {
        ServerWorld world = context.getWorld();
        TraitWorldComponent traits = TraitWorldComponent.KEY.get(world);
        float chanceBefore = traits.getTraitSlotRollChance();
        try {
            expectSameSingleAnswer(world, "sparkdlc:antifastround", "sparkfactionapi:antifastround");
            expectSameSingleAnswer(world, "sparkdlc:traitSlotRollChance", "sparktraits:traitSlotRollChance");
            expectSameSingleAnswer(world, "sparkdlc:detectiveCaseLimit", "sparkstrength:detectiveCaseLimit");
            expectSameSingleAnswer(world, "sparkdlc:witchFactor", "sparkwitch:witchFactor");

            List<String> set = run(world, "sparktraits:traitSlotRollChance 37");
            List<String> readBack = run(world, "sparkdlc:traitSlotRollChance");
            if (set.size() != 1 || !set.getFirst().contains("37") || readBack.size() != 1
                    || !readBack.getFirst().contains("37")) {
                throw new GameTestException("sparktraits:traitSlotRollChance 37 answered " + set
                        + ", then sparkdlc:traitSlotRollChance answered " + readBack);
            }
            expectSameSingleAnswer(world, "sparkdlc:traitSlotRollChance 12", "sparktraits:traitSlotRollChance 12");
        } finally {
            traits.setTraitSlotRollChance(chanceBefore);
        }
        context.complete();
    }

    /**
     * A trait, skill or role id typed in a former namespace gives the same result as the current id, under either
     * command name; an unknown old id is reported, not guessed; suggestions offer current ids only.
     * 以旧命名空间输入的词条、技能或职业 id 与当前 id 结果相同，无论使用哪个命令名；未知的旧 id 会报错而不是被猜测；补全只提供
     * 当前 id。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void typedOldIdsResolveToCurrentIds(TestContext context) throws CommandSyntaxException {
        ServerWorld world = context.getWorld();
        MinecraftServer server = world.getServer();
        CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();
        ServerCommandSource source = server.getCommandSource();

        int traits = 0;
        for (Map.Entry<String, String> trait : LegacyIds.renamed("trait").entrySet()) {
            Identifier current = Identifier.of(trait.getValue());
            if (TraitRegistry.contains(current)) {
                expectResolved(LegacyCommandIds.trait(Identifier.of(trait.getKey())), current);
                traits++;
            }
        }
        int skills = 0;
        for (Map.Entry<String, String> skill : LegacyIds.renamed("skill").entrySet()) {
            Identifier current = Identifier.of(skill.getValue());
            if (WitchSkillRegistry.get(current) != null) {
                expectResolved(LegacyCommandIds.skill(Identifier.of(skill.getKey())), current);
                skills++;
            }
        }
        if (traits < 40 || skills < 15) {
            throw new GameTestException("expected the traits and skills that existed before the rename, found "
                    + traits + " traits and " + skills + " skills with an old id");
        }

        // Saved state: the trait toggle stores the id it was given. / 持久化状态：词条开关保存的是传入的 id。
        Identifier anxiety = Identifier.of("sparkdlc", "anxiety");
        TraitWorldComponent worldTraits = TraitWorldComponent.KEY.get(world);
        boolean enabledBefore = worldTraits.isTraitEnabled(anxiety);
        try {
            run(world, "sparkdlc:disableTrait sparktraits:anxiety");
            if (worldTraits.isTraitEnabled(anxiety)) {
                throw new GameTestException("sparkdlc:disableTrait sparktraits:anxiety did not disable " + anxiety);
            }
            run(world, "sparktraits:enableTrait sparktraits:anxiety");
            if (!worldTraits.isTraitEnabled(anxiety)) {
                throw new GameTestException("sparktraits:enableTrait sparktraits:anxiety did not enable " + anxiety);
            }
            run(world, "sparktraits:disableTrait anxiety");
            if (worldTraits.isTraitEnabled(anxiety)) {
                throw new GameTestException("sparktraits:disableTrait anxiety did not disable " + anxiety);
            }
        } finally {
            worldTraits.setTraitEnabled(anxiety, enabledBefore);
        }

        // With nobody online a command whose id resolved stops at the player selector; an unknown id stops earlier.
        // 没有玩家在线时，id 解析成功的命令会停在玩家选择器；未知 id 则更早失败。
        for (String command : List.of(
                "sparkdlc:addTrait sparktraits:anxiety @a", "sparktraits:addTrait sparktraits:anxiety @a",
                "sparkdlc:removeTrait sparktraits:anxiety @a", "sparktraits:removeTrait sparkdlc:anxiety @a",
                "sparkdlc:forceAbility sparkwitch:bell_echo @a", "sparkwitch:forceAbility sparkwitch:bell_echo @a",
                "sparkwitch:forceAbility bell_echo @a",
                "sparkdlc:forcePromotion sparkwitch:curser @a", "sparkwitch:forcePromotion sparkwitch:wind_spirit @a",
                "sparkwitch:forcePromotion sparkdlc:vendetta @a",
                "sparkdlc:forceAccompliceRole sparkwitch:accomplice @a",
                "sparkwitch:forceAccompliceRole sparkwitch:accomplice @a",
                "sparkwitch:forceAccompliceRole accomplice @a")) {
            CommandSyntaxException failure = failureOf(dispatcher, command, source);
            if (failure == null || failure.getType() != EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION) {
                throw new GameTestException("'" + command + "' should resolve its id and stop at the empty player"
                        + " selector, but: " + (failure == null ? "it ran" : failure.getMessage()));
            }
        }
        for (String command : List.of(
                "sparkdlc:addTrait sparktraits:no_such_trait @a", "sparktraits:addTrait sparktraits:wraith @a",
                "sparkdlc:enableTrait sparkwitch:anxiety", "sparkdlc:forceAbility sparktraits:bell_echo @a",
                "sparkwitch:forcePromotion sparkwitch:no_such_role @a",
                "sparkdlc:forceAccompliceRole sparktraits:accomplice @a")) {
            CommandSyntaxException failure = failureOf(dispatcher, command, source);
            if (failure == null || failure.getType() == EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION) {
                throw new GameTestException("'" + command + "' names no current id and must be rejected as unknown");
            }
        }

        for (String command : List.of("sparkdlc:enableTrait ", "sparkdlc:addTrait ", "sparkdlc:forceAbility ",
                "sparkdlc:forcePromotion ", "sparkdlc:forceAccompliceRole ", "sparkdlc:addTask ")) {
            String currentRoot = rootOf(command.trim());
            String oldRoot = LegacyCommandAliases.names().entrySet().stream()
                    .filter(name -> name.getValue().equals(currentRoot)).findFirst().orElseThrow().getKey();
            List<String> current = suggestions(dispatcher, command, source);
            List<String> old = suggestions(dispatcher, oldRoot + " ", source);
            if (current.isEmpty() || !current.equals(old)) {
                throw new GameTestException("suggestions of '" + command + "' " + current + " differ from those of '"
                        + oldRoot + "' " + old);
            }
            for (String suggestion : current) {
                int colon = suggestion.indexOf(':');
                if (colon > 0 && LegacyIds.isLegacyNamespace(suggestion.substring(0, colon))) {
                    throw new GameTestException("'" + command + "' suggests the old id " + suggestion);
                }
            }
        }
        context.complete();
    }

    /**
     * A permission provider is simulated through fabric-permissions-api's own event. For every renamed node, and
     * under both command names: the current node decides when set, otherwise the former node, otherwise the op
     * level; an explicit deny on the former node also stops an operator. Commands that never had a node still
     * ignore every provider.
     * 通过 fabric-permissions-api 自身的事件模拟权限提供者。对每个改名的节点、且在新旧两个命令名下：当前节点已设置时由它决定，
     * 否则由旧节点决定，再否则由 OP 等级决定；旧节点上的明确拒绝同样能拦住管理员。从来没有节点的命令仍然不理会任何权限提供者。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void formerPermissionNodesAreStillHonoured(TestContext context) {
        ServerWorld world = context.getWorld();
        CommandDispatcher<ServerCommandSource> dispatcher = world.getServer().getCommandManager().getDispatcher();
        registerProvider();
        Map<String, String> oldNameOf = new HashMap<>();
        LegacyCommandAliases.names().forEach((old, current) -> oldNameOf.put(current, old));
        Set<String> renamedNodes = new TreeSet<>(LegacyIds.renamed("permission").values());
        Set<String> pinnedNodes = new TreeSet<>(NODE_COMMANDS.keySet());
        pinnedNodes.add(ITEM_NODE);
        if (!renamedNodes.equals(pinnedNodes)) {
            throw new GameTestException("the permission rows of legacy-ids.tsv changed: " + renamedNodes);
        }
        try {
            for (String node : renamedNodes) {
                List<String> formerNodes = LegacyPermissions.legacyNodes(node);
                if (formerNodes.size() != 1) {
                    throw new GameTestException(node + " should have one former name, has " + formerNodes);
                }
                String formerNode = formerNodes.getFirst();
                List<Check> checks = new ArrayList<>();
                checks.add(new Check(node, level -> LegacyPermissions.check(probe(world, level), node, 2)));
                for (String command : NODE_COMMANDS.getOrDefault(node, List.of())) {
                    for (String name : List.of(command, oldNameOf.get(command))) {
                        CommandNode<ServerCommandSource> root = dispatcher.getRoot().getChild(name);
                        checks.add(new Check(name, level -> root.canUse(probe(world, level))));
                    }
                }
                for (Check check : checks) {
                    expect(check, 0, false, "no provider answer, not an operator");
                    expect(check, 2, true, "no provider answer, operator");
                    PROVIDER_ANSWERS.put(formerNode, TriState.TRUE);
                    expect(check, 0, true, "former node " + formerNode + " granted");
                    PROVIDER_ANSWERS.put(formerNode, TriState.FALSE);
                    expect(check, 4, false, "former node " + formerNode + " denied to an operator");
                    PROVIDER_ANSWERS.put(node, TriState.TRUE);
                    expect(check, 0, true, "current node granted, former node denied");
                    PROVIDER_ANSWERS.put(node, TriState.FALSE);
                    PROVIDER_ANSWERS.put(formerNode, TriState.TRUE);
                    expect(check, 4, false, "current node denied, former node granted");
                    PROVIDER_ANSWERS.clear();
                }
            }
            for (String command : COMMANDS_WITHOUT_A_NODE) {
                String oldName = oldNameOf.get(command);
                for (String name : oldName == null ? List.of(command) : List.of(command, oldName)) {
                    CommandNode<ServerCommandSource> root = dispatcher.getRoot().getChild(name);
                    Check check = new Check(name, level -> root.canUse(probe(world, level)));
                    providerDefault = TriState.TRUE;
                    expect(check, 0, false, "a provider that grants everything, not an operator");
                    providerDefault = TriState.FALSE;
                    expect(check, 2, true, "a provider that denies everything, operator");
                    providerDefault = TriState.DEFAULT;
                }
            }
        } finally {
            PROVIDER_ANSWERS.clear();
            providerDefault = TriState.DEFAULT;
        }
        context.complete();
    }

    private record Check(String what, IntPredicate allowedAtLevel) {
    }

    private static void expect(Check check, int level, boolean allowed, String situation) {
        if (check.allowedAtLevel().test(level) != allowed) {
            throw new GameTestException(check.what() + " must be " + (allowed ? "allowed" : "refused") + " at op level "
                    + level + ": " + situation);
        }
    }

    private static void registerProvider() {
        if (!providerRegistered) {
            providerRegistered = true;
            PermissionCheckEvent.EVENT.register((source, permission) ->
                    source instanceof ServerCommandSource server && PROBE_NAME.equals(server.getName())
                            ? PROVIDER_ANSWERS.getOrDefault(permission, providerDefault)
                            : TriState.DEFAULT);
        }
    }

    private static ServerCommandSource probe(ServerWorld world, int level) {
        return source(world, level, PROBE_NAME, new ArrayList<>());
    }

    /** A command source that records what it is told instead of broadcasting it. / 记录收到的消息而不广播的命令源。 */
    private static ServerCommandSource source(ServerWorld world, int level, String name, List<String> answers) {
        CommandOutput output = new CommandOutput() {
            @Override
            public void sendMessage(Text message) {
                answers.add(message.getString());
            }

            @Override
            public boolean shouldReceiveFeedback() {
                return true;
            }

            @Override
            public boolean shouldTrackOutput() {
                return true;
            }

            @Override
            public boolean shouldBroadcastConsoleToOps() {
                return false;
            }
        };
        return new ServerCommandSource(output, Vec3d.ZERO, Vec2f.ZERO, world, level, name, Text.literal(name),
                world.getServer(), null);
    }

    /** Runs a command the way the server does and returns everything it answered. / 按服务器的方式执行命令，返回它的全部应答。 */
    private static List<String> run(ServerWorld world, String command) {
        List<String> answers = new ArrayList<>();
        world.getServer().getCommandManager()
                .executeWithPrefix(source(world, 4, "SparkDlcCommandTest", answers), command);
        return answers;
    }

    private static void expectSameSingleAnswer(ServerWorld world, String current, String old) {
        List<String> currentAnswers = run(world, current);
        List<String> oldAnswers = run(world, old);
        if (currentAnswers.size() != 1 || currentAnswers.getFirst().isBlank() || !currentAnswers.equals(oldAnswers)) {
            throw new GameTestException("'" + current + "' answered " + currentAnswers + " but '" + old
                    + "' answered " + oldAnswers);
        }
    }

    private static void expectResolved(Identifier resolved, Identifier current) {
        if (!current.equals(resolved)) {
            throw new GameTestException("an old id resolved to " + resolved + " instead of " + current);
        }
    }

    private static ParseResults<ServerCommandSource> parseFully(CommandDispatcher<ServerCommandSource> dispatcher,
            String input, ServerCommandSource source) {
        ParseResults<ServerCommandSource> parse = dispatcher.parse(input, source);
        if (parse.getReader().canRead() || !parse.getExceptions().isEmpty()
                || parse.getContext().getCommand() == null) {
            throw new GameTestException("'" + input + "' does not parse to a command: " + parse.getExceptions());
        }
        return parse;
    }

    private static List<CommandNode<ServerCommandSource>> nodes(ParseResults<ServerCommandSource> parse) {
        List<CommandNode<ServerCommandSource>> nodes = new ArrayList<>();
        for (ParsedCommandNode<ServerCommandSource> parsed : parse.getContext().getNodes()) {
            nodes.add(parsed.getNode());
        }
        return nodes;
    }

    /** Element-wise identity: the very same node objects in the same order. / 逐个比较引用：同一批节点对象且顺序相同。 */
    private static boolean sameNodes(List<CommandNode<ServerCommandSource>> left,
            List<CommandNode<ServerCommandSource>> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int index = 0; index < left.size(); index++) {
            if (left.get(index) != right.get(index)) {
                return false;
            }
        }
        return true;
    }

    private static CommandSyntaxException failureOf(CommandDispatcher<ServerCommandSource> dispatcher, String command,
            ServerCommandSource source) {
        try {
            dispatcher.execute(command, source);
            return null;
        } catch (CommandSyntaxException failure) {
            return failure;
        }
    }

    private static List<String> suggestions(CommandDispatcher<ServerCommandSource> dispatcher, String input,
            ServerCommandSource source) {
        List<String> texts = new ArrayList<>();
        for (Suggestion suggestion : dispatcher.getCompletionSuggestions(dispatcher.parse(input, source)).join()
                .getList()) {
            texts.add(suggestion.getText());
        }
        return texts;
    }

    private static String rootOf(String command) {
        int space = command.indexOf(' ');
        return space < 0 ? command : command.substring(0, space);
    }

    private static Map<String, String> oldNames(String... oldNames) {
        Map<String, String> names = new LinkedHashMap<>();
        for (String oldName : oldNames) {
            names.put(oldName, "sparkdlc" + oldName.substring(oldName.indexOf(':')));
        }
        return names;
    }
}
