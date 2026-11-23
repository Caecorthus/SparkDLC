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
import java.util.Map;
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
     * Two places where the start-up reaches the player, pinned so that a change fails here and not in somebody's
     * round. A login is asked exactly one version question by the mod: a second listener would mean a second query
     * and a second kick message again, as while the mod still carried the three handshakes of its former parts. A
     * chat message is offered to the Pathogen's and the Reporter's gates before the Wraith gate, which the start-up
     * registers itself after every role hub.
     * 启动流程会影响到玩家的两处，在此固定，使改动在这里失败，而不是在某局游戏中。本模组对一次登录只提出一个版本询问：
     * 出现第二个监听器就意味着又有了第二个查询和第二条踢出提示，如同本模组仍带着原先三个部分各自的握手时那样。
     * 聊天消息先交给病原体与记者的限制，再交给冤魂的限制；后者由启动流程自身在所有职业注册中心之后注册。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void orderSensitiveListenersKeepTheirOrder(TestContext context) throws ReflectiveOperationException {
        Map<String, List<List<String>>> events = StartupOrderDump.eventHandlerHosts();
        requireOwnHosts(events, "net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents$QueryStart", List.of(
                "dev.caecorthus.sparkdlc.net.version.SparkDlcVersionHandshake"));
        requireOwnHosts(events, "net.fabricmc.fabric.api.message.v1.ServerMessageEvents$AllowChatMessage", List.of(
                "dev.caecorthus.sparkdlc.role.neutral.pathogen.PathogenFeatureService",
                "dev.caecorthus.sparkdlc.role.civilian.reporter.ReporterCommunicationManager",
                "dev.caecorthus.sparkdlc.SparkDLC"));
        context.complete();
    }

    /** The mod's own handlers of the one event with this callback, in dispatch order. / 使用该回调的唯一事件中本模组处理器的分发顺序。 */
    private static void requireOwnHosts(Map<String, List<List<String>>> events, String callback, List<String> expected) {
        List<List<String>> lists = events.getOrDefault(callback, List.of());
        if (lists.size() != 1) {
            throw new GameTestException("expected one event with handlers of sparkdlc for " + callback + ", found "
                    + lists.size());
        }
        List<String> own = new ArrayList<>();
        for (String host : lists.get(0)) {
            if (host.startsWith("dev.caecorthus.sparkdlc.")) {
                own.add(host.replace(" (lambda)", ""));
            }
        }
        if (!own.equals(expected)) {
            throw new GameTestException("listener order of " + callback + " changed: expected " + expected
                    + ", found " + own);
        }
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
