package dev.caecorthus.sparkdlc.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.SparkDlcModule;
import dev.caecorthus.sparkdlc.faction.registry.FactionCatalog;
import dev.caecorthus.sparkdlc.legacy.LegacyCommandAliases;
import dev.caecorthus.sparkdlc.net.version.SparkDlcVersionHandshake;
import dev.caecorthus.sparkdlc.skill.api.WitchSkillRegistry;
import dev.caecorthus.sparkdlc.trait.api.TraitRegistry;
import dev.doctor4t.wathe.api.WatheRoles;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

/**
 * Boots a real dedicated server with the whole mod loaded (`./gradlew runGameTest`, no window, no EULA) and checks
 * what the unit tests cannot: that all common-side mixins of the mod apply against the real Wathe,
 * NoellesRoles and Minecraft classes, that the fused mod still answers for the five legacy mod ids, and that the
 * rename to the single {@code sparkdlc} namespace is complete: nothing the running server knows of this mod is left
 * in a legacy namespace, and every registered item, block, entity type and effect has its name in both locales.
 * 启动一个加载了整个模组的真实专用服务器（`./gradlew runGameTest`，无窗口、无需 EULA），检查单元测试覆盖不到的内容：
 * 本模组的全部通用端 mixin 都能应用到真实的 Wathe、NoellesRoles 与 Minecraft 类上，融合后的 mod 仍然能以五个旧 mod id
 * 被识别，并且统一到 {@code sparkdlc} 命名空间的改名是完整的：运行中的服务器所知的本模组内容没有留在旧命名空间里的，
 * 每个已注册的物品、方块、实体类型和状态效果在两种语言里都有名称。
 */
public final class SparkDlcBootTest implements FabricGameTest {
    /**
     * Entity types that have no {@code entity.sparkdlc.<path>} lang key today (projectiles and helper entities that
     * are never named on screen; unchanged by the rename). Pinned exactly: a new entity type has to get a name or
     * be added here deliberately, and an entry has to leave when its name is added.
     * 目前没有 {@code entity.sparkdlc.<path>} 语言键的实体类型（从不在界面上显示名称的投射物和辅助实体；改名前后一致）。
     * 精确固定：新的实体类型必须有名称或被有意加入此处，补上名称的条目必须移除。
     */
    private static final Set<String> ENTITY_TYPES_WITHOUT_A_NAME = Set.of(
            "sparkdlc:capsule", "sparkdlc:capture_device", "sparkdlc:holy_flash", "sparkdlc:hunter_trap",
            "sparkdlc:magician_playback", "sparkdlc:ninja_shuriken", "sparkdlc:shock_device");

    /**
     * {@code audit()} force-loads every mixin target that has not been loaded yet, so a mixin that cannot apply
     * fails here instead of in the middle of somebody's round.
     * {@code audit()} 会强制加载所有尚未加载的 mixin 目标类，因此无法应用的 mixin 会在这里失败，而不是在某局游戏中途。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void everyCommonMixinApplies(TestContext context) {
        MixinEnvironment.getCurrentEnvironment().audit();
        context.complete();
    }

    /**
     * Owner 2026-11-20, "这个排一下序吧": the dormant Fiend's kill immunity runs before the Bodyguard gear (Democracy
     * Shield, vest) at the HEAD of Wathe's 5-argument {@code killPlayer}, because gear is only spent on a kill that no
     * immunity stops (owner 2026-10-22). Read from the class Mixin really produces: the pre-mixin bytes of
     * {@code GameFunctions} go through the running Mixin transformer again, and the handler calls in {@code killPlayer}
     * must come in the order of their priorities (Rope 1030, Otherworld 1040, ricochet 1050, Fiend 1090, gear 1100,
     * pardon 1200). Fails if the Fiend and the gear swap back.
     * 所有者 2026-11-20「这个排一下序吧」：在 Wathe 五参 {@code killPlayer} 的 HEAD，休眠魔人的免死先于保镖装备（民主盾牌、防弹衣）执行，
     * 因为只有没有任何免死挡下的击杀才消耗装备（所有者 2026-10-22）。读取的是 Mixin 实际生成的类：把 {@code GameFunctions}
     * 的 mixin 前字节再交给运行中的 Mixin 转换器一次，{@code killPlayer} 中各处理器的调用顺序必须与其优先级一致
     * （绳索 1030、里世界 1040、跳弹 1050、魔人 1090、装备 1100、宽恕 1200）。魔人与装备若换回原顺序即失败。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void dormantFiendImmunityRunsBeforeTheBodyguardGear(TestContext context) throws IOException {
        String name = "dev.doctor4t.wathe.game.GameFunctions";
        byte[] preMixin = FabricLauncherBase.getLauncher().getClassByteArray(name, true);
        byte[] applied = ((IMixinTransformer) MixinEnvironment.getCurrentEnvironment().getActiveTransformer())
                .transformClassBytes(name, name, preMixin);
        ClassNode node = new ClassNode();
        new ClassReader(applied).accept(node, 0);
        // The @WrapMethod wrappers move the original body, HEAD callbacks included, into a synthetic method, so the
        // callbacks are looked up in the one method of the class that calls the Fiend handler.
        // @WrapMethod 包装会把原方法体（含 HEAD 回调）移入一个合成方法，因此在类中唯一调用魔人处理器的方法里查找各回调。
        List<List<String>> callers = new ArrayList<>();
        for (MethodNode method : node.methods) {
            List<String> calls = new ArrayList<>();
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(node.name)
                        && call.name.contains("$sparkdlc$")) {
                    calls.add(call.name.substring(call.name.indexOf("$sparkdlc$") + 1));
                }
            }
            if (calls.contains("sparkdlc$protectDormantFiend")) {
                callers.add(calls);
            }
        }
        if (callers.size() != 1) {
            throw new GameTestException("expected one killPlayer body calling the Fiend handler, found " + callers);
        }
        List<String> handlers = callers.getFirst();
        List<String> expected = List.of("sparkdlc$interruptRopeStrangle", "sparkdlc$blockOtherworldKillOnInvalidTarget",
                "sparkdlc$rysTRicochet", "sparkdlc$protectDormantFiend", "sparkdlc$bodyguardGearBlocksKill",
                "sparkdlc$holdCharonKill");
        List<Integer> positions = new ArrayList<>();
        for (String handler : expected) {
            int first = handlers.indexOf(handler);
            if (first < 0 || first != handlers.lastIndexOf(handler)) {
                throw new GameTestException(handler + " is not called exactly once in the killPlayer body: "
                        + handlers);
            }
            positions.add(first);
        }
        SparkDLC.LOGGER.info("killPlayer handler order: {}", handlers);
        List<String> tracked = handlers.stream().filter(expected::contains).toList();
        for (int i = 1; i < positions.size(); i++) {
            if (positions.get(i - 1) > positions.get(i)) {
                throw new GameTestException(expected.get(i - 1) + " must run before " + expected.get(i)
                        + " at the HEAD of killPlayer; found " + tracked);
            }
        }
        context.complete();
    }

    /**
     * Other mods, packs and a few data rows of our own still name the five legacy mod ids
     * ({@code isModLoaded("sparkwitch")} and friends); the single {@code sparkdlc} container has to answer for each
     * of them through {@code provides}.
     * 其他模组、资源包以及我们自己的少数数据行仍会使用五个旧 mod id（{@code isModLoaded("sparkwitch")} 之类）；唯一的
     * {@code sparkdlc} 容器必须通过 {@code provides} 应答每一个。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void legacyModIdsResolveToSparkDlc(TestContext context) {
        FabricLoader loader = FabricLoader.getInstance();
        for (SparkDlcModule module : SparkDlcModule.values()) {
            Optional<ModContainer> container = loader.getModContainer(module.namespace());
            if (!loader.isModLoaded(module.namespace()) || container.isEmpty()) {
                throw new GameTestException("legacy mod id is not provided: " + module.namespace());
            }
            if (!SparkDLC.MOD_ID.equals(container.get().getMetadata().getId())) {
                throw new GameTestException(module.namespace() + " resolves to "
                        + container.get().getMetadata().getId() + ", not sparkdlc");
            }
        }
        context.complete();
    }

    /**
     * Fails when the namespace rename is incomplete: a registry entry or tag, packet type, login channel, component
     * key, role, faction, trait, skill or command of this mod that is still in one of the five legacy namespaces.
     * Each kind also has to be present under {@code sparkdlc}, so the check cannot pass by looking at nothing.
     * 改名不完整时失败：本模组的注册表条目或标签、数据包类型、登录频道、组件键、职业、阵营、天赋、技能或命令仍位于五个
     * 旧命名空间之一。每个种类还必须在 {@code sparkdlc} 下有内容，因此本检查不会因为什么都没看而通过。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void nothingIsLeftInALegacyNamespace(TestContext context) throws ReflectiveOperationException {
        Set<String> legacyNamespaces = new TreeSet<>();
        for (SparkDlcModule module : SparkDlcModule.values()) {
            legacyNamespaces.add(module.namespace());
        }
        Map<String, Integer> own = new LinkedHashMap<>();
        Set<String> left = new TreeSet<>();
        BiConsumer<String, Identifier> see = (kind, id) -> {
            if (legacyNamespaces.contains(id.getNamespace())) {
                left.add(kind + " " + id);
            } else if (SparkDLC.MOD_ID.equals(id.getNamespace())) {
                own.merge(kind, 1, Integer::sum);
            }
        };
        for (Registry<?> registry : Registries.REGISTRIES) {
            String kind = registry.getKey().getValue().getPath();
            registry.getIds().forEach(id -> see.accept("registry " + kind, id));
            registry.streamTags().forEach(tag -> see.accept("tag " + kind, tag.id()));
        }
        // Fabric has no public listing of the registered payload types. / Fabric 没有公开的已注册数据包类型列表。
        Class<?> payloadTypes = Class.forName("net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl");
        Field packetTypes = payloadTypes.getDeclaredField("packetTypes");
        packetTypes.setAccessible(true);
        for (String side : List.of("PLAY_C2S", "PLAY_S2C", "CONFIGURATION_C2S", "CONFIGURATION_S2C")) {
            for (Object id : ((Map<?, ?>) packetTypes.get(payloadTypes.getField(side).get(null))).keySet()) {
                see.accept("payload", (Identifier) id);
            }
        }
        see.accept("login channel", SparkDlcVersionHandshake.VERSION_CHECK_ID);
        ComponentRegistry.stream().forEach(key -> see.accept("component", key.getId()));
        WatheRoles.ROLES.forEach(role -> see.accept("role", role.identifier()));
        FactionCatalog.getCustomFactions().forEach(faction -> see.accept("faction", faction.id()));
        TraitRegistry.values().forEach(trait -> see.accept("trait", trait.id()));
        WitchSkillRegistry.values().forEach(skill -> see.accept("skill", skill.id()));
        // Command roots are `namespace:camelCaseName`, which is not a valid Identifier. The old command names of the
        // legacy table (with the three misspelt roots the role-kit commands always had) are there on purpose: they
        // are the aliases the compatibility layer adds (SparkDlcCommandTest pins them); any other root in a legacy
        // namespace is a leftover.
        // 命令根节点形如 `namespace:camelCaseName`，不是合法的 Identifier。对照表中的旧命令名（包括职业套件命令一直保留的三个
        // 拼写别名）有意存在：它们是兼容层添加的别名（由 SparkDlcCommandTest 固定）；旧命名空间里的其他根节点才是遗漏。
        Set<String> commandAliases = LegacyCommandAliases.names().keySet();
        context.getWorld().getServer().getCommandManager().getDispatcher().getRoot().getChildren().forEach(node -> {
            int colon = node.getName().indexOf(':');
            if (colon > 0 && !commandAliases.contains(node.getName())) {
                String namespace = node.getName().substring(0, colon);
                if (legacyNamespaces.contains(namespace)) {
                    left.add("command " + node.getName());
                } else if (SparkDLC.MOD_ID.equals(namespace)) {
                    own.merge("command", 1, Integer::sum);
                }
            }
        });

        if (!left.isEmpty()) {
            throw new GameTestException(left.size() + " ids are still in a legacy namespace: " + left);
        }
        for (String kind : List.of("registry item", "registry block", "registry entity_type", "registry sound_event",
                "registry mob_effect", "registry particle_type", "registry data_component_type",
                "registry creative_mode_tab", "tag item", "payload", "login channel", "component", "role", "faction",
                "trait", "skill", "command")) {
            if (own.getOrDefault(kind, 0) == 0) {
                throw new GameTestException("the scan saw no sparkdlc id of kind '" + kind + "': " + own);
            }
        }
        if (own.get("login channel") != 1) {
            throw new GameTestException("the mod asks one version question at login, on one sparkdlc channel");
        }
        context.complete();
    }

    /**
     * The name of a registered thing is looked up by a key built from its id, so a lang key that was not renamed
     * together with the id shows up as a raw key in game and nowhere else. Every item, block, entity type and status
     * effect of this mod must have its translation key in both lang files.
     * 已注册内容的名称由其 id 拼出的键查找，因此没有随 id 一起改名的语言键只会在游戏里显示为原始键，别处都发现不了。
     * 本模组的每个物品、方块、实体类型和状态效果都必须在两个语言文件里有对应的翻译键。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void registeredThingsHaveTheirNameInBothLocales(TestContext context) throws IOException {
        Map<String, JsonObject> locales = new LinkedHashMap<>();
        for (String locale : List.of("en_us", "zh_cn")) {
            locales.put(locale, readLang(locale));
        }
        Map<String, String> keys = new LinkedHashMap<>();
        Registries.ITEM.forEach(item -> own(Registries.ITEM.getId(item), "item", item.getTranslationKey(), keys));
        Registries.BLOCK.forEach(block -> own(Registries.BLOCK.getId(block), "block", block.getTranslationKey(), keys));
        Registries.ENTITY_TYPE.forEach(type ->
                own(Registries.ENTITY_TYPE.getId(type), "entity_type", type.getTranslationKey(), keys));
        Registries.STATUS_EFFECT.forEach(effect ->
                own(Registries.STATUS_EFFECT.getId(effect), "mob_effect", effect.getTranslationKey(), keys));
        if (keys.size() < 100) {
            throw new GameTestException("expected the items, blocks, entity types and effects of sparkdlc, found "
                    + keys.size());
        }
        Set<String> missing = new TreeSet<>();
        Set<String> unnamedEntityTypes = new TreeSet<>();
        keys.forEach((thing, key) -> locales.forEach((locale, lang) -> {
            if (!lang.has(key)) {
                if (thing.startsWith("entity_type ")) {
                    unnamedEntityTypes.add(thing.substring("entity_type ".length()));
                } else {
                    missing.add(thing + " (" + key + ", " + locale + ")");
                }
            }
        }));
        if (!missing.isEmpty()) {
            throw new GameTestException(missing.size() + " registered names have no lang key: " + missing);
        }
        if (!unnamedEntityTypes.equals(new TreeSet<>(ENTITY_TYPES_WITHOUT_A_NAME))) {
            throw new GameTestException("entity types without a lang key changed: expected "
                    + new TreeSet<>(ENTITY_TYPES_WITHOUT_A_NAME) + ", found " + unnamedEntityTypes);
        }
        context.complete();
    }

    private static void own(Identifier id, String kind, String translationKey, Map<String, String> keys) {
        if (SparkDLC.MOD_ID.equals(id.getNamespace())) {
            keys.put(kind + " " + id, translationKey);
        }
    }

    private static JsonObject readLang(String locale) throws IOException {
        String path = "assets/" + SparkDLC.MOD_ID + "/lang/" + locale + ".json";
        Path file = FabricLoader.getInstance().getModContainer(SparkDLC.MOD_ID).orElseThrow().findPath(path)
                .orElseThrow(() -> new GameTestException("the mod has no " + path));
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
