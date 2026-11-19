package dev.caecorthus.sparkdlc.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * What the start-up of the mod registered, checked on the running server.
 * 在运行中的服务器上检查模组启动时注册的内容。
 */
public final class SparkDlcStartupTest implements FabricGameTest {
    private static final Logger LOGGER = LoggerFactory.getLogger("sparkdlc_gametest");

    /**
     * Cardinal Components only creates a key that {@code fabric.mod.json} declares under
     * {@code custom.cardinal-components}, but nothing checks the other direction, or that a declared key gets a
     * factory at all. The declared list and the factories the mod's entrypoint registers must be the same set, each
     * id once.
     * Cardinal Components 只会创建 {@code fabric.mod.json} 在 {@code custom.cardinal-components} 下声明过的键，但没有任何东西
     * 检查反方向，也不检查声明过的键是否真的注册了工厂。声明列表与本模组入口注册的工厂必须是同一个集合，且每个 id 只出现一次。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void declaredComponentIdsAreExactlyTheRegisteredFactories(TestContext context) {
        List<String> declared = new ArrayList<>();
        CustomValue list = FabricLoader.getInstance().getModContainer("sparkdlc").orElseThrow().getMetadata()
                .getCustomValue("cardinal-components");
        for (CustomValue id : list.getAsArray()) {
            declared.add(id.getAsString());
        }
        List<String> registered = StartupOrderDump.componentIds(StartupOrderDump.componentFactories().values());
        if (registered.size() < 100) {
            throw new GameTestException("expected the component factories of sparkdlc, recorded " + registered.size());
        }
        requireNoDuplicate("declared in fabric.mod.json", declared);
        requireNoDuplicate("registered by the cardinal-components entrypoint", registered);
        Set<String> undeclared = new TreeSet<>(registered);
        declared.forEach(undeclared::remove);
        Set<String> unregistered = new TreeSet<>(declared);
        registered.forEach(unregistered::remove);
        if (!undeclared.isEmpty() || !unregistered.isEmpty()) {
            throw new GameTestException("component ids differ: registered but not declared " + undeclared
                    + ", declared but not registered " + unregistered);
        }
        context.complete();
    }

    /**
     * Not a check: writes the start-up order dump when the run asked for one ({@link StartupOrderDump}).
     * 不是检查：运行要求时写出启动顺序导出文件（{@link StartupOrderDump}）。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void startupOrderDumpIsWrittenOnRequest(TestContext context)
            throws ReflectiveOperationException, IOException {
        Path file = StartupOrderDump.writeIfRequested();
        if (file != null) {
            LOGGER.info("[SparkDLC start-up order] wrote {}", file.toAbsolutePath());
        }
        context.complete();
    }

    private static void requireNoDuplicate(String what, List<String> ids) {
        Set<String> seen = new TreeSet<>();
        Set<String> twice = new TreeSet<>();
        for (String id : ids) {
            if (!seen.add(id)) {
                twice.add(id);
            }
        }
        if (!twice.isEmpty()) {
            throw new GameTestException("component ids " + what + " more than once: " + twice);
        }
    }
}
