package dev.caecorthus.sparkdlc;

/**
 * The five mod ids SparkDLC answers for besides its own: the ids of the five mods that were fused into it before
 * 2.0.0. They are legacy names only: every registry id, packet, component, asset, lang key and config file of the
 * mod uses the one namespace {@code sparkdlc}, and nothing shown to a player names them. They stay loader-visible
 * because {@code fabric.mod.json} lists them under {@code provides}, so {@code FabricLoader#isModLoaded} and
 * {@code #getModContainer} still answer for them (other mods and packs ask), and the boot game test checks that
 * each of them resolves to the SparkDLC container and owns no id any more.
 * SparkDLC 除自身 id 之外还应答的五个 mod id：2.0.0 之前融合进来的五个 mod 的 id。它们只是旧名字：本模组的全部
 * 注册 id、数据包、组件、资源、语言键和配置文件都使用唯一的命名空间 {@code sparkdlc}，展示给玩家的任何文字也不再提到
 * 它们。它们仍对加载器可见，因为 {@code fabric.mod.json} 的 {@code provides} 列出了它们，所以
 * {@code FabricLoader#isModLoaded} 与 {@code #getModContainer} 仍然认得它们（其他模组与资源包会询问）；启动游戏测试
 * 检查它们每一个都解析到 SparkDLC 容器，且名下不再有任何 id。
 */
public enum SparkDlcModule {
    FACTION_API("sparkfactionapi"),
    STRENGTH("sparkstrength"),
    TRAITS("sparktraits"),
    WITCH("sparkwitch"),
    ASSIST("sparkassist");

    private final String namespace;

    SparkDlcModule(String namespace) {
        this.namespace = namespace;
    }

    /**
     * The mod id the module had as a separate mod (a {@code provides} alias today, no longer the namespace of its
     * ids). 模块作为独立 mod 时的 mod id（现在只是 {@code provides} 别名，不再是其 id 的命名空间）。
     */
    public String namespace() {
        return this.namespace;
    }
}
