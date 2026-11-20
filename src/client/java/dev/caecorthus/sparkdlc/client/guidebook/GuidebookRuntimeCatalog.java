package dev.caecorthus.sparkdlc.client.guidebook;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.client.guidebook.data.GuidebookResourceLoader;
import dev.caecorthus.sparkdlc.guidebook.GuidebookCatalog;
import dev.caecorthus.sparkdlc.guidebook.GuidebookDiscoveryRules;
import dev.caecorthus.sparkdlc.guidebook.GuidebookEntry;
import dev.caecorthus.sparkdlc.guidebook.GuidebookTab;
import dev.caecorthus.sparkdlc.trait.api.Trait;
import dev.caecorthus.sparkdlc.trait.api.TraitRegistry;
import dev.caecorthus.sparkdlc.skill.api.WitchSkillDefinition;
import dev.caecorthus.sparkdlc.skill.api.WitchSkillRegistry;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

/**
 * Composes authored resource entries with read-only entries discovered from installed registries.
 * 将资源文件中的正文条目与已安装模组注册表中的只读条目合并。
 */
public final class GuidebookRuntimeCatalog {
    private GuidebookRuntimeCatalog() {
    }

    public static GuidebookCatalog load(MinecraftClient client) {
        GuidebookCatalog authored = GuidebookResourceLoader.load(client.getResourceManager());
        Set<String> authoredIds = new HashSet<>();
        authored.entries().forEach(entry -> authoredIds.add(entry.id()));

        List<GuidebookEntry> discovered = new ArrayList<>();
        discoverRoles(discovered, authoredIds);
        discoverRegistry(
                discovered,
                authoredIds,
                TraitRegistry.values(),
                Trait::id,
                Trait::color,
                GuidebookTab.TRAIT,
                "trait"
        );
        discoverRegistry(
                discovered,
                authoredIds,
                WitchSkillRegistry.values(),
                WitchSkillDefinition::id,
                WitchSkillDefinition::color,
                GuidebookTab.SKILL,
                "skill"
        );

        return GuidebookCatalog.mergeAuthoredWithAvailableDiscoveries(
                authored,
                GuidebookCatalog.of(discovered),
                loadedModIds()
        );
    }

    private static void discoverRoles(List<GuidebookEntry> entries, Set<String> authoredIds) {
        int order = 1_000;
        for (Role role : WatheRoles.ROLES) {
            Identifier id = role.identifier();
            if (role == WatheRoles.NO_ROLE
                    || role == WatheRoles.DISCOVERY_CIVILIAN
                    || authoredIds.contains(id.toString())) {
                continue;
            }
            entries.add(new GuidebookEntry(
                    id.toString(),
                    GuidebookTab.ROLE,
                    id.getNamespace(),
                    "announcement.role." + id.getPath(),
                    "guidebook.sparkdlc.content.role.overview",
                    List.of(
                            "guidebook.sparkdlc.content.role.overview",
                            "guidebook.sparkdlc.content.role.abilities",
                            "guidebook.sparkdlc.content.role.items"
                    ),
                    List.of(),
                    List.of(id.getNamespace()),
                    role.color() & 0xFFFFFF,
                    order++
            ));
        }
    }

    /**
     * Adds an entry for every value of one of the mod's own registries that has no authored page. The entry's owner is
     * this mod and it requires no other mod: the registries are part of this mod, which is always loaded (they used to
     * be looked up by class name behind an isModLoaded gate, and each entry named its former mod as required, while
     * the registries lived in other jars).
     * 为本模组某个注册表中每个没有人工页面的值添加条目。条目的所属模组是本模组，且不需要其他模组：这些注册表属于总是已加载的
     * 本模组（它们还在其他 jar 中时，是在 isModLoaded 判断之后按类名查找，每个条目也把原模组声明为所需模组）。
     */
    private static <T> void discoverRegistry(
            List<GuidebookEntry> entries,
            Set<String> authoredIds,
            Collection<T> values,
            Function<T, Identifier> idOf,
            ToIntFunction<T> colorOf,
            GuidebookTab tab,
            String translationPrefix
    ) {
        int order = 1_000;
        for (T value : values) {
            Identifier id = idOf.apply(value);
            // Witch skills are fail-closed to the supported role-owned set.
            // 魔女技能目录仅收录受支持的职业专属技能，未知技能默认排除。
            if (authoredIds.contains(id.toString())
                    || !GuidebookDiscoveryRules.includes(tab, id.toString())) {
                continue;
            }
            String path = id.getPath().replace('/', '.');
            String baseKey = translationPrefix + "." + id.getNamespace() + "." + path;
            int color = colorOf.applyAsInt(value) & 0xFFFFFF;
            entries.add(new GuidebookEntry(
                    id.toString(),
                    tab,
                    SparkDLC.MOD_ID,
                    baseKey + ".name",
                    baseKey + ".description",
                    List.of(baseKey + ".description"),
                    GuidebookDiscoveryRules.ownerRoleIds(id.toString()),
                    List.of(),
                    color,
                    order++
            ));
        }
    }

    /**
     * The loaded mod ids the discovered pages are checked against (authored pages, resource packs' included, are never
     * filtered). Only a discovered role page names a required mod: its role's namespace, so a role another mod
     * registers in Wathe's role list under a namespace that no loaded mod has or provides gets no page. A container
     * also answers for the ids it {@code provides}: SparkDLC provides {@code sparkwitch}, {@code sparktraits},
     * {@code sparkstrength}, {@code sparkfactionapi} and {@code sparkassist}.
     * 用来检查自动发现页面的已加载 mod id（人工编写的页面，包括资源包中的，从不筛选）。只有自动发现的职业页面声明所需模组：
     * 即该职业的命名空间，因此其他模组以任何已加载模组都没有、也不 provides 的命名空间注册到 Wathe 职业列表的职业不会有页面。
     * 容器同时代表它 {@code provides} 的 id：SparkDLC 提供 {@code sparkwitch}、{@code sparktraits}、
     * {@code sparkstrength}、{@code sparkfactionapi} 与 {@code sparkassist}。
     */
    private static Set<String> loadedModIds() {
        Set<String> ids = new HashSet<>();
        FabricLoader.getInstance().getAllMods().forEach(mod -> {
            ids.add(mod.getMetadata().getId());
            ids.addAll(mod.getMetadata().getProvides());
        });
        return Set.copyOf(ids);
    }
}
