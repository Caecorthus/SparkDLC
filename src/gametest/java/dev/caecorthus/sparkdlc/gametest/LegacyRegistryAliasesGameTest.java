package dev.caecorthus.sparkdlc.gametest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import dev.caecorthus.sparkdlc.SparkDlcItemGroups;
import dev.caecorthus.sparkdlc.legacy.LegacyIds;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Proves on a real server that a world, a command block or a structure file made before the fusion still finds
 * everything the five former mods registered: every old registry id of {@code legacy-ids.tsv} resolves to the entry
 * registered under the new id, through the lookups a save uses (by {@link Identifier}, aliased by Fabric API through
 * {@code LegacyRegistryAliases}) and through the lookups commands and structure files use (by {@link RegistryKey},
 * which Fabric API misses on 1.21.1 and {@code SimpleRegistryKeyAliasMixin} closes); that whatever is decoded from an
 * old id is written back under the new id; and that new and unknown ids behave as they did.
 * 在真实服务器上证明融合前制作的世界、命令方块或结构文件仍能找到五个旧模组注册的一切：{@code legacy-ids.tsv} 中每个旧注册表 id
 * 都解析为以新 id 注册的条目，既包括存档使用的查找（按 {@link Identifier}，由 Fabric API 经 {@code LegacyRegistryAliases}
 * 提供别名），也包括命令和结构文件使用的查找（按 {@link RegistryKey}，Fabric API 在 1.21.1 上遗漏，由
 * {@code SimpleRegistryKeyAliasMixin} 补上）；由旧 id 解码出的内容会以新 id 写回；新 id 和未知 id 的行为与以前一样。
 */
public final class LegacyRegistryAliasesGameTest implements FabricGameTest {
    /**
     * The registry kinds of the table with their row counts. The table is history (the ids the five mods had), so the
     * counts are exact: a check below cannot pass by looking at nothing, and a registry kind added to the table has
     * to be added here, with its save and command lookups covered.
     * 对照表中的注册表种类及其行数。对照表是历史记录（五个模组曾用的 id），所以行数是精确的：下面的检查不会因为什么都没看而
     * 通过；表里新增注册表种类时必须在此登记，并覆盖它的存档查找与命令查找。
     */
    private static final Map<String, Integer> REGISTRY_KIND_ROWS = Map.of(
            "item", 131, "block", 5, "entity_type", 27, "sound_event", 102, "data_component_type", 10,
            "mob_effect", 4, "particle_type", 4, "armor_material", 2, "creative_mode_tab", 2);
    /**
     * A value for each old data component type that no item carries by default, so the component can be saved under
     * its old id on a stack. The other types take the default value of an item that has one.
     * 没有任何物品默认携带的旧数据组件类型各自的取值，用来把组件以旧 id 存到物品堆上。其余类型取某个默认带有它的物品的默认值。
     */
    private static final Map<String, Object> COMPONENT_SAMPLES = Map.of(
            "sparkdlc:drone_charge", 3, "sparkdlc:drone_payload", true, "sparkdlc:timekeeper_watch_mode", 1,
            "sparkdlc:conscience_poisoner", "legacy", "sparkdlc:duel_knife", true, "sparkdlc:forced_shot_gun", true,
            "sparkdlc:sphinx_hao_loaded", true);
    /**
     * Ids that never existed: one in each legacy namespace, in the current one and in vanilla's.
     * 从未存在过的 id：每个旧命名空间、当前命名空间和原版命名空间各一个。
     */
    private static final List<String> UNKNOWN_IDS = Stream
            .concat(LegacyIds.LEGACY_NAMESPACES.stream(), Stream.of(LegacyIds.NAMESPACE, Identifier.DEFAULT_NAMESPACE))
            .map(namespace -> namespace + ":never_registered").toList();

    /**
     * Every old id of every registry kind, through every lookup of the registry itself: by id and by key, the two
     * codecs saves use ({@code getCodec}, {@code getEntryCodec}), the registry wrappers commands and structure files
     * use, and {@code RegistryFixedCodec}. Encoding what was decoded writes the new id. Both old creative tabs lead
     * to the one new tab.
     * 每个注册表种类的每个旧 id，经注册表自身的每种查找：按 id、按键、存档使用的两个编解码器（{@code getCodec}、
     * {@code getEntryCodec}）、命令与结构文件使用的注册表包装器，以及 {@code RegistryFixedCodec}。把解码结果再编码，写出的是新 id。
     * 两个旧创造物品栏都指向同一个新物品栏。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void everyOldRegistryIdResolvesToItsNewEntry(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        List<String> problems = new ArrayList<>();
        Map<String, Integer> seen = new TreeMap<>();
        for (Registry<?> registry : Registries.REGISTRIES) {
            Identifier registryId = registry.getKey().getValue();
            if (!Identifier.DEFAULT_NAMESPACE.equals(registryId.getNamespace())) {
                continue;
            }
            Map<String, String> rows = LegacyIds.renamed(registryId.getPath());
            if (!rows.isEmpty()) {
                seen.put(registryId.getPath(), rows.size());
                checkRegistry(registry, rows, server.getRegistryManager(), problems);
            }
        }
        if (!seen.equals(new TreeMap<>(REGISTRY_KIND_ROWS))) {
            problems.add("registry kinds of legacy-ids.tsv changed: expected " + new TreeMap<>(REGISTRY_KIND_ROWS)
                    + ", found " + seen);
        }
        if (!Set.of(SparkDlcItemGroups.ITEMS_GROUP.getValue().toString())
                .equals(Set.copyOf(LegacyIds.renamed("creative_mode_tab").values()))) {
            problems.add("the old creative tabs do not all lead to " + SparkDlcItemGroups.ITEMS_GROUP.getValue()
                    + ": " + LegacyIds.renamed("creative_mode_tab"));
        }
        finish(context, problems);
    }

    private static <T> void checkRegistry(Registry<T> registry, Map<String, String> rows,
            DynamicRegistryManager manager, List<String> problems) {
        String kind = registry.getKey().getValue().getPath();
        RegistryOps<NbtElement> ops = manager.getOps(NbtOps.INSTANCE);
        Codec<RegistryEntry<T>> fixedCodec = RegistryFixedCodec.of(registry.getKey());
        rows.forEach((oldText, newText) -> {
            String row = kind + " " + oldText + ": ";
            Identifier oldId = Identifier.of(oldText);
            Identifier newId = Identifier.of(newText);
            RegistryKey<T> oldKey = RegistryKey.of(registry.getKey(), oldId);
            RegistryKey<T> newKey = RegistryKey.of(registry.getKey(), newId);
            Optional<RegistryEntry.Reference<T>> expected = registry.getEntry(newId);
            if (expected.isEmpty() || !expected.get().registryKey().equals(newKey)) {
                problems.add(row + "the new id " + newText + " is not registered");
                return;
            }
            RegistryEntry.Reference<T> entry = expected.get();
            T value = entry.value();
            // The aliases are no entries: nothing is registered, listed or synced under the old id.
            // 别名不是条目：旧 id 下没有任何注册、列出或同步的内容。
            if (registry.getIds().contains(oldId) || registry.getKeys().contains(oldKey)) {
                problems.add(row + "the old id is listed as a registered entry");
            }
            if (!newId.equals(registry.getId(value))) {
                problems.add(row + "getId gives " + registry.getId(value) + ", so a save would not write " + newText);
            }
            // Lookups by Identifier: what saves use. / 按 Identifier 的查找：存档使用。
            expect(problems, row + "get(Identifier)", value, registry.get(oldId));
            expect(problems, row + "getOrEmpty(Identifier)", Optional.of(value), registry.getOrEmpty(oldId));
            expect(problems, row + "containsId", true, registry.containsId(oldId));
            expect(problems, row + "getEntry(Identifier)", expected, registry.getEntry(oldId));
            // Lookups by RegistryKey: what commands, structure files and fixed codecs use.
            // 按 RegistryKey 的查找：命令、结构文件和固定编解码器使用。
            expect(problems, row + "get(RegistryKey)", value, registry.get(oldKey));
            expect(problems, row + "getOrEmpty(RegistryKey)", Optional.of(value), registry.getOrEmpty(oldKey));
            expect(problems, row + "contains(RegistryKey)", true, registry.contains(oldKey));
            expect(problems, row + "getEntry(RegistryKey)", expected, registry.getEntry(oldKey));
            expect(problems, row + "getReadOnlyWrapper().getOptional", expected,
                    registry.getReadOnlyWrapper().getOptional(oldKey));
            expect(problems, row + "getTagCreatingWrapper().getOptional", expected,
                    registry.getTagCreatingWrapper().getOptional(oldKey));
            expect(problems, row + "registry manager wrapper getOptional", expected,
                    manager.getWrapperOrThrow(registry.getKey()).getOptional(oldKey));
            // Codecs: decode the old id, encode the result, read the new id.
            // 编解码器：解码旧 id，把结果再编码，得到新 id。
            NbtElement oldNbt = NbtString.of(oldText);
            NbtElement newNbt = NbtString.of(newText);
            roundTrip(problems, row + "getCodec", registry.getCodec(), NbtOps.INSTANCE, oldNbt, value, newNbt);
            roundTrip(problems, row + "getEntryCodec", registry.getEntryCodec(), NbtOps.INSTANCE, oldNbt, entry,
                    newNbt);
            roundTrip(problems, row + "RegistryFixedCodec", fixedCodec, ops, oldNbt, entry, newNbt);
        });
    }

    /**
     * What a save holds. An item stack saved under an old item id, alone and with a component saved under each old
     * data component type id; a {@code can_break} predicate naming an old block id (a {@code RegistryFixedCodec}
     * reference inside item NBT); a block state as chunks store it and as structure files store it; an entity as a
     * chunk stores it; a status effect on a player; a particle and a sound event reference. Each is decoded from the
     * old id and written back under the new one.
     * 存档里保存的内容。以旧物品 id 保存的物品堆，单独保存以及带有以每个旧数据组件类型 id 保存的组件；指向旧方块 id 的
     * {@code can_break} 谓词（物品 NBT 内的 {@code RegistryFixedCodec} 引用）；区块保存形式与结构文件保存形式的方块状态；
     * 区块保存形式的实体；玩家身上的状态效果；粒子和声音事件引用。每一项都由旧 id 解码，并以新 id 写回。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void oldIdsInSavedDataLoadAndAreWrittenBackUnderTheNewId(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        DynamicRegistryManager registries = server.getRegistryManager();
        RegistryOps<NbtElement> ops = registries.getOps(NbtOps.INSTANCE);
        List<String> problems = new ArrayList<>();

        LegacyIds.renamed("item").forEach((oldId, newId) -> {
            String row = "item stack " + oldId + ": ";
            Optional<ItemStack> stack = ItemStack.fromNbt(registries, stackNbt(oldId, null));
            if (stack.isEmpty() || stack.get().isEmpty()) {
                problems.add(row + "did not load");
                return;
            }
            expect(problems, row + "item", Registries.ITEM.get(Identifier.of(newId)), stack.get().getItem());
            expect(problems, row + "saved id", newId, ((NbtCompound) stack.get().encode(registries)).getString("id"));
        });

        String oldCarrier = LegacyIds.renamed("item").keySet().iterator().next();
        LegacyIds.renamed("data_component_type").forEach((oldId, newId) -> {
            ComponentType<?> type = Registries.DATA_COMPONENT_TYPE.get(Identifier.of(newId));
            if (type == null) {
                problems.add("data component " + oldId + ": the new id " + newId + " is not registered");
                return;
            }
            // On a stick the component is never a default, so it has to survive as a change of the stack.
            // 木棍上该组件不可能是默认值，因此它必须作为物品堆的改动保留下来。
            for (String carrier : List.of("minecraft:stick", oldCarrier)) {
                checkSavedComponent(type, oldId, newId, carrier, registries, ops, problems);
            }
            // The "removed" form of a component change, `!id`, goes through the same key codec.
            // 组件改动的“移除”形式 `!id` 走同一个键编解码器。
            NbtCompound removed = new NbtCompound();
            removed.put("!" + oldId, new NbtCompound());
            NbtCompound removedNew = new NbtCompound();
            removedNew.put("!" + newId, new NbtCompound());
            Optional<ComponentChanges> changes = ComponentChanges.CODEC.parse(ops, removed).result();
            if (changes.isEmpty()) {
                problems.add("data component !" + oldId + ": did not decode");
            } else {
                expect(problems, "data component !" + oldId + ": saved form", Optional.of(removedNew),
                        ComponentChanges.CODEC.encodeStart(ops, changes.get()).result());
            }
        });

        LegacyIds.renamed("block").forEach((oldId, newId) -> {
            String row = "block state " + oldId + ": ";
            Block block = Registries.BLOCK.get(Identifier.of(newId));
            NbtCompound saved = new NbtCompound();
            saved.putString("Name", oldId);
            // Chunk sections (palette entries). / 区块段（调色板条目）。
            Optional<BlockState> fromChunk = BlockState.CODEC.parse(NbtOps.INSTANCE, saved).result();
            expect(problems, row + "BlockState.CODEC", Optional.of(block.getDefaultState()), fromChunk);
            fromChunk.ifPresent(state -> expect(problems, row + "BlockState.CODEC saved name", Optional.of(newId),
                    BlockState.CODEC.encodeStart(NbtOps.INSTANCE, state).result()
                            .map(nbt -> ((NbtCompound) nbt).getString("Name"))));
            // Structure files, falling blocks, piston heads: an unknown name becomes air here.
            // 结构文件、下落方块、活塞头：这里未知的名称会变成空气。
            BlockState fromStructure = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), saved);
            expect(problems, row + "NbtHelper.toBlockState", block.getDefaultState(), fromStructure);
            expect(problems, row + "NbtHelper.fromBlockState name", newId,
                    NbtHelper.fromBlockState(fromStructure).getString("Name"));
            // An adventure-mode predicate inside item NBT: RegistryEntryListCodec over RegistryFixedCodec.
            // 物品 NBT 内的冒险模式谓词：RegistryEntryListCodec 之下是 RegistryFixedCodec。
            NbtCompound predicate = new NbtCompound();
            predicate.putString("blocks", oldId);
            NbtCompound components = new NbtCompound();
            components.put("minecraft:can_break", predicate);
            Optional<ItemStack> tool = ItemStack.fromNbt(registries, stackNbt("minecraft:stick", components));
            if (tool.isEmpty()) {
                problems.add(row + "a stack with can_break naming it did not load");
            } else {
                String savedTool = tool.get().encode(registries).toString();
                if (!savedTool.contains(newId) || savedTool.contains(oldId)) {
                    problems.add(row + "can_break was saved as " + savedTool);
                }
            }
        });

        int savedEntities = 0;
        for (Map.Entry<String, String> row : LegacyIds.renamed("entity_type").entrySet()) {
            String label = "entity " + row.getKey() + ": ";
            EntityType<?> type = Registries.ENTITY_TYPE.get(Identifier.of(row.getValue()));
            NbtCompound saved = new NbtCompound();
            saved.putString("id", row.getKey());
            expect(problems, label + "EntityType.fromNbt", Optional.of(type), EntityType.fromNbt(saved));
            expect(problems, label + "EntityType.get", Optional.of(type), EntityType.get(row.getKey()));
            if (!type.isSaveable()) {
                continue;
            }
            // The entity is built and read as a chunk does it, but never added to the world.
            // 按区块加载的方式创建并读取实体，但不加入世界。
            Optional<Entity> entity = EntityType.getEntityFromNbt(saved, context.getWorld());
            if (entity.isEmpty()) {
                problems.add(label + "the saved entity did not load");
                continue;
            }
            NbtCompound written = new NbtCompound();
            expect(problems, label + "loaded type", type, entity.get().getType());
            expect(problems, label + "saveSelfNbt", true, entity.get().saveSelfNbt(written));
            expect(problems, label + "saved id", row.getValue(), written.getString("id"));
            entity.get().discard();
            savedEntities++;
        }
        if (savedEntities == 0) {
            problems.add("no saveable entity type was loaded and saved");
        }

        LegacyIds.renamed("mob_effect").forEach((oldId, newId) -> {
            String row = "status effect " + oldId + ": ";
            NbtCompound saved = new NbtCompound();
            saved.putString("id", oldId);
            saved.putInt("duration", 200);
            StatusEffectInstance effect = StatusEffectInstance.fromNbt(saved);
            if (effect == null) {
                problems.add(row + "did not load");
                return;
            }
            expect(problems, row + "effect", Registries.STATUS_EFFECT.get(Identifier.of(newId)),
                    effect.getEffectType().value());
            expect(problems, row + "saved id", newId, ((NbtCompound) effect.writeNbt()).getString("id"));
        });

        LegacyIds.renamed("particle_type").forEach((oldId, newId) -> {
            String row = "particle " + oldId + ": ";
            NbtCompound saved = new NbtCompound();
            saved.putString("type", oldId);
            Optional<ParticleEffect> particle = ParticleTypes.TYPE_CODEC.parse(ops, saved).result();
            if (particle.isEmpty()) {
                problems.add(row + "did not load");
                return;
            }
            expect(problems, row + "type", Registries.PARTICLE_TYPE.get(Identifier.of(newId)),
                    particle.get().getType());
            expect(problems, row + "saved type", Optional.of(newId),
                    ParticleTypes.TYPE_CODEC.encodeStart(ops, particle.get()).result()
                            .map(nbt -> ((NbtCompound) nbt).getString("type")));
        });

        LegacyIds.renamed("sound_event").forEach((oldId, newId) -> {
            // A reference to a registered sound event by id (RegistryElementCodec), as a jukebox song, an
            // enchantment's play_sound effect or a biome's music names it.
            // 以 id 引用已注册的声音事件（RegistryElementCodec），例如唱片机曲目、附魔的 play_sound 效果或生物群系音乐的写法。
            roundTrip(problems, "sound event " + oldId + ": SoundEvent.ENTRY_CODEC", SoundEvent.ENTRY_CODEC, ops,
                    NbtString.of(oldId), Registries.SOUND_EVENT.getEntry(Identifier.of(newId)).orElse(null),
                    NbtString.of(newId));
        });
        finish(context, problems);
    }

    private static <T> void checkSavedComponent(ComponentType<T> type, String oldId, String newId, String carrierId,
            DynamicRegistryManager registries, RegistryOps<NbtElement> ops, List<String> problems) {
        String row = "data component " + oldId + " on " + carrierId + ": ";
        T sample = sampleValue(type, newId);
        if (sample == null) {
            problems.add(row + "the test has no sample value for it");
            return;
        }
        Optional<NbtElement> savedValue = type.getCodecOrThrow().encodeStart(ops, sample).result();
        if (savedValue.isEmpty()) {
            problems.add(row + "the sample value does not encode");
            return;
        }
        NbtCompound components = new NbtCompound();
        components.put(oldId, savedValue.get());
        Optional<ItemStack> stack = ItemStack.fromNbt(registries, stackNbt(carrierId, components));
        if (stack.isEmpty()) {
            problems.add(row + "the stack did not load");
            return;
        }
        expect(problems, row + "loaded value", sample, stack.get().get(type));
        NbtCompound written = (NbtCompound) stack.get().encode(registries);
        NbtCompound writtenComponents = written.getCompound("components");
        // A value equal to the carrier's own default is no change of the stack and is rightly not saved.
        // 与载体物品自身默认值相同的取值不算物品堆的改动，本来就不会被保存。
        if (!sample.equals(Registries.ITEM.get(Identifier.of(written.getString("id"))).getComponents().get(type))) {
            expect(problems, row + "saved component keys", Set.of(newId), writtenComponents.getKeys());
            expect(problems, row + "saved value", savedValue.get(), writtenComponents.get(newId));
        }
        if (writtenComponents.contains(oldId)) {
            problems.add(row + "the old component id was written back");
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T sampleValue(ComponentType<T> type, String newId) {
        Object pinned = COMPONENT_SAMPLES.get(newId);
        if (pinned != null) {
            return (T) pinned;
        }
        for (Item item : Registries.ITEM) {
            T value = item.getComponents().get(type);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static NbtCompound stackNbt(String itemId, NbtCompound components) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", itemId);
        nbt.putInt("count", 1);
        if (components != null) {
            nbt.put("components", components);
        }
        return nbt;
    }

    /**
     * What an old map types. Each command is parsed by the server's own dispatcher, as a command block, a function
     * or an operator would have it parsed, and the parsed argument has to be the entry of the new id; nothing is
     * executed. These arguments look the id up by {@link RegistryKey}: without {@code SimpleRegistryKeyAliasMixin}
     * every one of them fails with "Unknown item / block / entity".
     * 旧地图里输入的内容。每条命令都由服务器自己的分发器解析，与命令方块、函数或管理员输入时相同，解析出的参数必须是新 id 的条目；
     * 不执行任何命令。这些参数按 {@link RegistryKey} 查找 id：没有 {@code SimpleRegistryKeyAliasMixin} 时它们全部以
     * “未知的物品 / 方块 / 实体”失败。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void oldIdsTypedInVanillaCommandsParseToTheNewEntry(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        DynamicRegistryManager registries = server.getRegistryManager();
        RegistryOps<NbtElement> ops = registries.getOps(NbtOps.INSTANCE);
        List<String> problems = new ArrayList<>();

        LegacyIds.renamed("item").forEach((oldId, newId) -> {
            Item item = Registries.ITEM.get(Identifier.of(newId));
            for (String id : List.of(oldId, newId)) {
                Object parsed = parsedArgument(server, "give @s " + id, "item", problems);
                if (parsed instanceof ItemStackArgument argument) {
                    expect(problems, "/give " + id, item, argument.getItem());
                }
                // Item predicates (`/clear`, `execute if items`) read the id the same way.
                // 物品谓词（`/clear`、`execute if items`）以同样方式读取 id。
                parsedArgument(server, "clear @s " + id, "item", problems);
            }
        });
        LegacyIds.renamed("data_component_type").forEach((oldId, newId) -> {
            ComponentType<?> type = Registries.DATA_COMPONENT_TYPE.get(Identifier.of(newId));
            if (type != null) {
                checkTypedComponent(server, type, oldId, newId, ops, problems);
            }
        });
        LegacyIds.renamed("block").forEach((oldId, newId) -> {
            BlockState state = Registries.BLOCK.get(Identifier.of(newId)).getDefaultState();
            for (String id : List.of(oldId, newId)) {
                Object parsed = parsedArgument(server, "setblock 0 0 0 " + id, "block", problems);
                if (parsed instanceof BlockStateArgument argument) {
                    expect(problems, "/setblock " + id, state, argument.getBlockState());
                }
                parsedArgument(server, "fill 0 0 0 0 0 0 minecraft:air replace " + id, "filter", problems);
            }
        });
        LegacyIds.renamed("entity_type").forEach((oldId, newId) -> {
            for (String id : List.of(oldId, newId)) {
                Object parsed = parsedArgument(server, "summon " + id, "entity", problems);
                if (parsed != null) {
                    expect(problems, "/summon " + id,
                            Registries.ENTITY_TYPE.getEntry(Identifier.of(newId)).orElse(null), parsed);
                }
            }
        });
        LegacyIds.renamed("mob_effect").forEach((oldId, newId) -> {
            for (String id : List.of(oldId, newId)) {
                Object parsed = parsedArgument(server, "effect give @s " + id, "effect", problems);
                if (parsed != null) {
                    expect(problems, "/effect give " + id,
                            Registries.STATUS_EFFECT.getEntry(Identifier.of(newId)).orElse(null), parsed);
                }
            }
        });
        LegacyIds.renamed("particle_type").forEach((oldId, newId) -> {
            for (String id : List.of(oldId, newId)) {
                Object parsed = parsedArgument(server, "particle " + id, "name", problems);
                if (parsed instanceof ParticleEffect particle) {
                    expect(problems, "/particle " + id, Registries.PARTICLE_TYPE.get(Identifier.of(newId)),
                            particle.getType());
                }
            }
        });
        finish(context, problems);
    }

    private static <T> void checkTypedComponent(MinecraftServer server, ComponentType<T> type, String oldId,
            String newId, RegistryOps<NbtElement> ops, List<String> problems) {
        T sample = sampleValue(type, newId);
        Optional<NbtElement> typed = sample == null ? Optional.empty()
                : type.getCodecOrThrow().encodeStart(ops, sample).result();
        if (typed.isEmpty()) {
            problems.add("/give with component " + oldId + ": the test has no sample value for it");
            return;
        }
        for (String id : List.of(oldId, newId)) {
            String command = "give @s minecraft:stick[" + id + "=" + typed.get() + "]";
            Object parsed = parsedArgument(server, command, "item", problems);
            if (parsed instanceof ItemStackArgument argument) {
                try {
                    expect(problems, "/" + command, sample, argument.createStack(1, false).get(type));
                } catch (CommandSyntaxException exception) {
                    problems.add("/" + command + ": " + exception.getMessage());
                }
            }
        }
    }

    /**
     * Parses without executing; a command that does not parse completely is a problem.
     * 只解析不执行；未能完整解析即为问题。
     */
    private static Object parsedArgument(MinecraftServer server, String command, String argument,
            List<String> problems) {
        ParseResults<ServerCommandSource> parse = parse(server, command);
        if (CommandManager.getException(parse) != null || parse.getContext().getCommand() == null) {
            problems.add("/" + command + " does not parse: " + CommandManager.getException(parse));
            return null;
        }
        ParsedArgument<ServerCommandSource, ?> parsed = parse.getContext().getArguments().get(argument);
        if (parsed == null) {
            problems.add("/" + command + " has no argument '" + argument + "'");
            return null;
        }
        return parsed.getResult();
    }

    private static ParseResults<ServerCommandSource> parse(MinecraftServer server, String command) {
        CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();
        return dispatcher.parse(command, server.getCommandSource());
    }

    /**
     * The layer acts only when an old name arrives. An id that was never registered is still unknown in every
     * lookup and in every command, also in a legacy namespace and in the defaulted registries (where a wrong answer
     * would be air or a pig); a key of another registry does not find an entry of the same id; a current id gives
     * the same entry through the key lookup as through the id lookup, for every entry of every static registry.
     * 兼容层只在旧名字到来时起作用。从未注册过的 id 在每种查找和每条命令里仍然未知，旧命名空间下以及带默认值的注册表里也一样
     * （那里答错就会得到空气或猪）；属于其他注册表的键找不到同 id 的条目；对所有静态注册表的每个条目，当前 id 经按键查找与按 id
     * 查找得到同一个条目。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void newAndUnknownIdsBehaveAsBefore(TestContext context) {
        MinecraftServer server = context.getWorld().getServer();
        List<String> problems = new ArrayList<>();
        for (Registry<?> registry : Registries.REGISTRIES) {
            checkUnchanged(registry, problems);
        }
        // A block that is also an item under the same id: the item registry must not answer for the block's key,
        // neither under the new id nor under the old one.
        // 同一 id 既是方块也是物品时：物品注册表不能应答方块的键，新 id 和旧 id 都一样。
        int blocksWithAnItem = 0;
        for (Map.Entry<String, String> row : LegacyIds.renamed("block").entrySet()) {
            if (!Registries.ITEM.containsId(Identifier.of(row.getValue()))) {
                continue;
            }
            blocksWithAnItem++;
            for (String id : List.of(row.getKey(), row.getValue())) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                Optional<?> crossed = ((Registry) Registries.ITEM).getEntry(
                        (RegistryKey) RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(id)));
                expect(problems, "item registry asked for the block key " + id, Optional.empty(), crossed);
            }
        }
        if (blocksWithAnItem == 0) {
            problems.add("no legacy block has an item of the same id; the cross-registry check saw nothing");
        }
        for (String id : UNKNOWN_IDS) {
            for (String command : List.of("give @s " + id, "clear @s " + id, "setblock 0 0 0 " + id, "summon " + id,
                    "effect give @s " + id, "particle " + id)) {
                if (CommandManager.getException(parse(server, command)) == null) {
                    problems.add("/" + command + " parses although the id was never registered");
                }
            }
            NbtCompound saved = new NbtCompound();
            saved.putString("id", id);
            expect(problems, "entity saved as " + id, Optional.empty(), EntityType.fromNbt(saved));
            expect(problems, "item stack saved as " + id, Optional.empty(),
                    ItemStack.fromNbt(server.getRegistryManager(), stackNbt(id, null)));
            NbtCompound state = new NbtCompound();
            state.putString("Name", id);
            expect(problems, "structure block state " + id, Blocks.AIR.getDefaultState(),
                    NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), state));
        }
        finish(context, problems);
    }

    private static <T> void checkUnchanged(Registry<T> registry, List<String> problems) {
        String kind = registry.getKey().getValue().getPath();
        for (String text : UNKNOWN_IDS) {
            Identifier id = Identifier.of(text);
            RegistryKey<T> key = RegistryKey.of(registry.getKey(), id);
            if (registry.containsId(id) || registry.contains(key) || registry.getEntry(id).isPresent()
                    || registry.getEntry(key).isPresent() || registry.getReadOnlyWrapper().getOptional(key).isPresent()
                    || registry.getOrEmpty(key).isPresent()) {
                problems.add(kind + " " + text + " resolves although it was never registered");
            }
        }
        for (RegistryEntry.Reference<T> entry : registry.streamEntries().toList()) {
            RegistryKey<T> key = entry.registryKey();
            Optional<RegistryEntry.Reference<T>> byKey = registry.getEntry(key);
            if (byKey.isEmpty() || byKey.get() != entry || registry.getEntry(key.getValue()).orElse(null) != entry
                    || registry.getReadOnlyWrapper().getOptional(key).orElse(null) != entry
                    || registry.get(key) != entry.value()) {
                problems.add(kind + " " + key.getValue() + " no longer resolves to its own entry");
            }
            if (LegacyIds.isLegacyNamespace(key.getValue().getNamespace())) {
                problems.add(kind + " " + key.getValue() + " is registered in a legacy namespace");
            }
        }
    }

    /**
     * Tags are no registry entries and get no alias: the mod's own tags moved to {@code data/sparkdlc/tags}, and code
     * and bundled data have to agree on the new ids. Every tag of the table is loaded under its new id with entries,
     * and nothing is loaded under an old tag id (a file left behind in {@code data/<legacy namespace>/tags} would
     * feed a tag nobody reads).
     * 标签不是注册表条目，没有别名：本模组自己的标签已迁到 {@code data/sparkdlc/tags}，代码与随附数据必须在新 id 上保持一致。
     * 对照表中的每个标签都以新 id 加载且有内容；没有任何内容以旧标签 id 加载（遗留在 {@code data/<旧命名空间>/tags} 里的文件只会
     * 喂给一个没人读取的标签）。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void ownTagsAreLoadedUnderTheirNewIds(TestContext context) {
        List<String> problems = new ArrayList<>();
        Map<String, Integer> seen = new LinkedHashMap<>();
        for (Registry<?> registry : Registries.REGISTRIES) {
            String kind = "tag/" + registry.getKey().getValue().getPath();
            LegacyIds.renamed(kind).forEach((oldId, newId) -> {
                seen.merge(kind, 1, Integer::sum);
                checkTag(registry, kind, oldId, newId, problems);
            });
        }
        expect(problems, "tag kinds of legacy-ids.tsv", Map.of("tag/item", 4, "tag/entity_type", 2, "tag/block", 1),
                seen);
        finish(context, problems);
    }

    private static <T> void checkTag(Registry<T> registry, String kind, String oldId, String newId,
            List<String> problems) {
        if (registry.getEntryList(TagKey.of(registry.getKey(), Identifier.of(newId)))
                .filter(entries -> entries.size() > 0).isEmpty()) {
            problems.add(kind + " " + newId + " is not loaded or is empty");
        }
        if (registry.getEntryList(TagKey.of(registry.getKey(), Identifier.of(oldId))).isPresent()) {
            problems.add(kind + " " + oldId + " is still loaded under its old id");
        }
    }

    private static <A, V> void roundTrip(List<String> problems, String what, Codec<A> codec,
            DynamicOps<V> ops, V oldForm, A expected, V newForm) {
        Optional<A> decoded = codec.parse(ops, oldForm).result();
        if (decoded.isEmpty()) {
            problems.add(what + " does not decode " + oldForm);
            return;
        }
        expect(problems, what + " decoded", expected, decoded.get());
        expect(problems, what + " encoded", Optional.of(newForm), codec.encodeStart(ops, decoded.get()).result());
    }

    private static void expect(List<String> problems, String what, Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            problems.add(what + ": expected " + expected + ", found " + actual);
        }
    }

    private static void finish(TestContext context, List<String> problems) {
        if (!problems.isEmpty()) {
            throw new GameTestException(problems.size() + " problems, first of them: "
                    + problems.subList(0, Math.min(problems.size(), 12)));
        }
        context.complete();
    }
}
