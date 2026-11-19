package dev.caecorthus.sparkdlc.gametest;

import dev.caecorthus.sparkdlc.SparkDlcModule;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.Optional;

/**
 * Boots a real dedicated server with every module loaded (`./gradlew runGameTest`, no window, no EULA) and checks
 * what the unit tests cannot: that all common-side mixins of all five modules apply against the real Wathe,
 * NoellesRoles and Minecraft classes, and that the fused mod still answers for the five legacy mod ids.
 * 启动一个加载了全部模块的真实专用服务器（`./gradlew runGameTest`，无窗口、无需 EULA），检查单元测试覆盖不到的内容：
 * 五个模块的全部通用端 mixin 都能应用到真实的 Wathe、NoellesRoles 与 Minecraft 类上，并且融合后的 mod 仍然能以
 * 五个旧 mod id 被识别。
 */
public final class SparkDlcBootTest implements FabricGameTest {
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
     * Sibling checks all over the code base ask {@code isModLoaded("sparkdlc")} and friends; the single
     * {@code sparkdlc} container has to answer for each legacy id through {@code provides}.
     * 代码中到处用 {@code isModLoaded("sparkdlc")} 之类检查兄弟模块；唯一的 {@code sparkdlc} 容器必须通过
     * {@code provides} 应答每个旧 id。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void legacyModIdsResolveToSparkDlc(TestContext context) {
        FabricLoader loader = FabricLoader.getInstance();
        for (SparkDlcModule module : SparkDlcModule.values()) {
            Optional<ModContainer> container = loader.getModContainer(module.namespace());
            if (!loader.isModLoaded(module.namespace()) || container.isEmpty()) {
                throw new GameTestException("legacy mod id is not provided: " + module.namespace());
            }
            if (!"sparkdlc".equals(container.get().getMetadata().getId())) {
                throw new GameTestException(module.namespace() + " resolves to "
                        + container.get().getMetadata().getId() + ", not sparkdlc");
            }
        }
        context.complete();
    }

    /**
     * One item per item-owning module, under its unchanged legacy namespace: registry ids must survive the fusion or
     * existing worlds, maps and resource packs break.
     * 每个拥有物品的模块各取一个物品，命名空间保持旧值：注册 id 必须原样保留，否则已有存档、地图和资源包都会失效。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void legacyNamespacesKeepTheirItems(TestContext context) {
        for (String id : new String[]{"sparkdlc:m67", "sparkdlc:ninja_knife"}) {
            if (!Registries.ITEM.containsId(Identifier.of(id))) {
                throw new GameTestException("item is not registered: " + id);
            }
        }
        context.complete();
    }
}
