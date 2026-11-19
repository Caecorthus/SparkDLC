package dev.caecorthus.sparkdlc;

import java.util.Optional;

/**
 * The five modules fused into SparkDLC. Each was a separate mod before 2.0.0 and keeps its old mod id as the
 * namespace of its registry ids, packets, components, assets, lang keys and config files, so worlds, resource packs
 * and saved settings made for the separate mods keep working. {@code fabric.mod.json} also lists the ids under
 * {@code provides}, so {@code FabricLoader#isModLoaded} and {@code #getModContainer} still answer for them.
 * 融合进 SparkDLC 的五个模块。它们在 2.0.0 之前各是独立 mod，旧 mod id 继续作为各自注册 id、数据包、组件、资源、
 * 语言键和配置文件的命名空间，因此为独立 mod 制作的存档、资源包和设置都继续有效。{@code fabric.mod.json} 的
 * {@code provides} 也列出了这些 id，所以 {@code FabricLoader#isModLoaded} 与 {@code #getModContainer} 仍然认得它们。
 */
public enum SparkDlcModule {
    FACTION_API("sparkfactionapi", "SparkFactionAPI"),
    STRENGTH("sparkstrength", "SparkStrength"),
    TRAITS("sparktraits", "SparkTraits"),
    WITCH("sparkwitch", "SparkWitch"),
    ASSIST("sparkassist", "SparkAssist");

    private final String namespace;
    private final String displayName;

    SparkDlcModule(String namespace, String displayName) {
        this.namespace = namespace;
        this.displayName = displayName;
    }

    /** The module's namespace, which is also the mod id it had as a separate mod. 模块的命名空间，也是它作为独立 mod 时的 id。 */
    public String namespace() {
        return this.namespace;
    }

    /** The name players know the module by. 玩家熟悉的模块名称。 */
    public String displayName() {
        return this.displayName;
    }

    /** The module that owns {@code namespace}, if it is one of the five. 拥有该命名空间的模块（若属于这五个之一）。 */
    public static Optional<SparkDlcModule> byNamespace(String namespace) {
        for (SparkDlcModule module : values()) {
            if (module.namespace.equals(namespace)) {
                return Optional.of(module);
            }
        }
        return Optional.empty();
    }
}
