package dev.caecorthus.sparkdlc.assist.client.guidebook;

import dev.caecorthus.sparkdlc.assist.client.guidebook.data.GuidebookResourceLoader;
import dev.caecorthus.sparkdlc.assist.guidebook.GuidebookCatalog;
import dev.caecorthus.sparkdlc.assist.guidebook.GuidebookDiscoveryRules;
import dev.caecorthus.sparkdlc.assist.guidebook.GuidebookEntry;
import dev.caecorthus.sparkdlc.assist.guidebook.GuidebookTab;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composes authored resource entries with read-only entries discovered from installed registries.
 * 将资源文件中的正文条目与已安装模组注册表中的只读条目合并。
 */
public final class GuidebookRuntimeCatalog {
    private static final Logger LOGGER = LoggerFactory.getLogger("SparkAssist/Guidebook");

    private GuidebookRuntimeCatalog() {
    }

    public static GuidebookCatalog load(MinecraftClient client) {
        GuidebookCatalog authored = GuidebookResourceLoader.load(client.getResourceManager());
        Set<String> authoredIds = new HashSet<>();
        authored.entries().forEach(entry -> authoredIds.add(entry.id()));

        List<GuidebookEntry> discovered = new ArrayList<>();
        discoverRoles(discovered, authoredIds);
        discoverOptionalRegistry(
                discovered,
                authoredIds,
                "sparktraits",
                "dev.caecorthus.sparkdlc.traits.api.TraitRegistry",
                GuidebookTab.TRAIT,
                "trait"
        );
        discoverOptionalRegistry(
                discovered,
                authoredIds,
                "sparkwitch",
                "dev.caecorthus.sparkdlc.witch.api.WitchSkillRegistry",
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

    private static void discoverOptionalRegistry(
            List<GuidebookEntry> entries,
            Set<String> authoredIds,
            String modId,
            String registryClassName,
            GuidebookTab tab,
            String translationPrefix
    ) {
        if (!FabricLoader.getInstance().isModLoaded(modId)) {
            return;
        }
        try {
            Class<?> registryClass = Class.forName(registryClassName);
            Method valuesMethod = registryClass.getMethod("values");
            Collection<?> values = (Collection<?>) valuesMethod.invoke(null);
            int order = 1_000;
            for (Object value : values) {
                Identifier id = (Identifier) value.getClass().getMethod("id").invoke(value);
                // Witch skills are fail-closed to the supported role-owned set.
                // 魔女技能目录仅收录受支持的职业专属技能，未知技能默认排除。
                if (authoredIds.contains(id.toString())
                        || !GuidebookDiscoveryRules.includes(tab, id.toString())) {
                    continue;
                }
                String path = id.getPath().replace('/', '.');
                String baseKey = translationPrefix + "." + id.getNamespace() + "." + path;
                int color = ((Number) value.getClass().getMethod("color").invoke(value)).intValue() & 0xFFFFFF;
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
        } catch (ClassNotFoundException exception) {
            LOGGER.debug("Optional guidebook registry {} is unavailable", registryClassName);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassCastException exception) {
            LOGGER.warn("Could not read optional guidebook registry {}", registryClassName, exception);
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
