package dev.caecorthus.sparkdlc.assist.client.guidebook;

import dev.caecorthus.sparkdlc.assist.client.guidebook.data.GuidebookResourceLoader;
import dev.caecorthus.sparkdlc.assist.guidebook.GuidebookCatalog;
import dev.caecorthus.sparkdlc.assist.guidebook.GuidebookDiscoveryRules;
import dev.caecorthus.sparkdlc.assist.guidebook.GuidebookEntry;
import dev.caecorthus.sparkdlc.assist.guidebook.GuidebookTab;
import dev.caecorthus.sparkdlc.traits.api.Trait;
import dev.caecorthus.sparkdlc.traits.api.TraitRegistry;
import dev.caecorthus.sparkdlc.witch.api.WitchSkillDefinition;
import dev.caecorthus.sparkdlc.witch.api.WitchSkillRegistry;
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
                "sparktraits",
                TraitRegistry.values(),
                Trait::id,
                Trait::color,
                GuidebookTab.TRAIT,
                "trait"
        );
        discoverRegistry(
                discovered,
                authoredIds,
                "sparkwitch",
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
                    "guidebook.sparkassist.content.role.overview",
                    List.of(
                            "guidebook.sparkassist.content.role.overview",
                            "guidebook.sparkassist.content.role.abilities",
                            "guidebook.sparkassist.content.role.items"
                    ),
                    List.of(),
                    List.of(id.getNamespace()),
                    role.color() & 0xFFFFFF,
                    order++
            ));
        }
    }

    /**
     * Adds an entry for every value of a sibling module's registry that has no authored page. {@code modId} is the
     * module's namespace, recorded as the entry's owner and required mod. The modules are fused in SparkDLC, so the
     * registry values are read directly (they used to be looked up by class name behind an isModLoaded gate).
     * 为兄弟模块注册表中每个没有人工页面的值添加条目。{@code modId} 是该模块的命名空间，记录为条目的所属模组与所需模组。
     * 各模块已融合进 SparkDLC，因此直接读取注册表的值（原先在 isModLoaded 判断之后按类名查找）。
     */
    private static <T> void discoverRegistry(
            List<GuidebookEntry> entries,
            Set<String> authoredIds,
            String modId,
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
                    modId,
                    baseKey + ".name",
                    baseKey + ".description",
                    List.of(baseKey + ".description"),
                    GuidebookDiscoveryRules.ownerRoleIds(id.toString()),
                    List.of(modId),
                    color,
                    order++
            ));
        }
    }

    /**
     * Ids a guide page may name as its required mods. A container also answers for the ids it {@code provides}:
     * SparkDLC is one container that provides {@code sparkwitch}, {@code sparktraits}, {@code sparkstrength},
     * {@code sparkfactionapi} and {@code sparkassist}, and the guide pages still name those module ids.
     * 指南页面可声明为“所需模组”的 id。容器同时代表它 {@code provides} 的 id：SparkDLC 是一个容器，提供
     * {@code sparkwitch}、{@code sparktraits}、{@code sparkstrength}、{@code sparkfactionapi} 与
     * {@code sparkassist}，而指南页面仍然按这些模块 id 声明。
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
