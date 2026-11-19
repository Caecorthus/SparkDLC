package dev.caecorthus.sparkdlc;

import java.util.Optional;

/**
 * The five modules fused into SparkDLC, with the mod id each had as a separate mod before 2.0.0. Those five ids
 * are legacy names now: every registry id, packet, component, asset, lang key and config file of the mod uses the
 * one namespace {@code sparkdlc}. They stay loader-visible because {@code fabric.mod.json} lists them under
 * {@code provides}, so {@code FabricLoader#isModLoaded} and {@code #getModContainer} still answer for them, and a
 * few data rows still label their module with them (the event volume rows of the settings screen).
 * 融合进 SparkDLC 的五个模块，以及它们在 2.0.0 之前作为独立 mod 时各自的 mod id。这五个 id 现在是旧名字：本模组的全部
 * 注册 id、数据包、组件、资源、语言键和配置文件都使用唯一的命名空间 {@code sparkdlc}。它们仍对加载器可见，因为
 * {@code fabric.mod.json} 的 {@code provides} 列出了它们，所以 {@code FabricLoader#isModLoaded} 与
 * {@code #getModContainer} 仍然认得它们；少数数据行（设置界面的事件音量行）仍用它们标注所属模块。
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

    /**
     * The mod id the module had as a separate mod (a {@code provides} alias today, no longer the namespace of its
     * ids). 模块作为独立 mod 时的 mod id（现在只是 {@code provides} 别名，不再是其 id 的命名空间）。
     */
    public String namespace() {
        return this.namespace;
    }

    /** The name players know the module by. 玩家熟悉的模块名称。 */
    public String displayName() {
        return this.displayName;
    }

    /** The module whose legacy mod id is {@code namespace}, if it is one of the five. 旧 mod id 为该值的模块（若属于这五个之一）。 */
    public static Optional<SparkDlcModule> byNamespace(String namespace) {
        for (SparkDlcModule module : values()) {
            if (module.namespace.equals(namespace)) {
                return Optional.of(module);
            }
        }
        return Optional.empty();
    }
}
