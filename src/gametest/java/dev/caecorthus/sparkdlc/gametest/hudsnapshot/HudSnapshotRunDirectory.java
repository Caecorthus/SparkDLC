package dev.caecorthus.sparkdlc.gametest.hudsnapshot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prepares the run directory of {@code ./gradlew runHudSnapshot} before the game reads it: a fixed
 * {@code options.txt} (GUI scale 3, Simplified Chinese, no pause on lost focus, no tutorial, no clouds, no frame
 * limit, silent), no
 * world of an earlier run and no pictures of an earlier run. It is a pre-launch entrypoint because vanilla reads the
 * language and the GUI scale before any client initializer runs. Inert unless the run sets
 * {@code -Dsparkdlc.hudSnapshot=true}, and it refuses to write anywhere but {@code build/run/hudSnapshot}. No
 * Minecraft class is touched here (none may be loaded this early).
 * 在游戏读取之前布置 {@code ./gradlew runHudSnapshot} 的运行目录：固定的 {@code options.txt}（GUI 缩放 3、简体中文、失去焦点不暂停、
 * 无教程、无云、不限帧率、静音），清除上一次运行留下的世界与图片。它是 pre-launch 入口，因为原版在任何客户端初始化器运行之前就读取语言与
 * GUI 缩放。只有运行配置设置了 {@code -Dsparkdlc.hudSnapshot=true} 时才会动作，并且拒绝写入 {@code build/run/hudSnapshot}
 * 以外的任何位置。这里不接触任何 Minecraft 类（这么早的阶段不允许加载）。
 */
public final class HudSnapshotRunDirectory implements PreLaunchEntrypoint {
    /** The switch of the whole harness. / 整个截图工具的开关。 */
    static final String PROPERTY = "sparkdlc.hudSnapshot";
    /** Save folder of the throwaway world. / 一次性世界的存档文件夹。 */
    static final String WORLD_NAME = "hud_snapshot";
    /** Where the pictures go, under the run directory. / 图片的输出位置（运行目录下）。 */
    static final String SNAPSHOT_DIRECTORY = "snapshots";
    static final int GUI_SCALE = 3;

    private static final Logger LOGGER = LoggerFactory.getLogger("sparkdlc_gametest");
    /**
     * The whole file is rewritten on every run, so a run never inherits what an earlier one (or a person clicking
     * around in the window) saved. {@code version} is the data version of 1.21.1; without it vanilla would run every
     * options fixer since 1.0.
     * 每次运行都会重写整个文件，因此不会继承上一次运行（或有人在窗口里点来点去）保存的内容。{@code version} 是 1.21.1 的数据版本；
     * 没有它，原版会执行自 1.0 以来的所有选项修复器。
     */
    private static final List<String> OPTIONS = List.of(
            "version:3955",
            "lang:zh_cn",
            "guiScale:" + GUI_SCALE,
            "fullscreen:false",
            "pauseOnLostFocus:false",
            "tutorialStep:none",
            "onboardAccessibility:false",
            "skipMultiplayerWarning:true",
            "joinedFirstServer:true",
            "narrator:0",
            "renderClouds:\"false\"",
            "bobView:false",
            "autoJump:false",
            "renderDistance:4",
            "simulationDistance:5",
            // 260 is vanilla's "unlimited": the HUD's easing advances per frame (see HudSnapshotClient.SETTLE_FRAMES).
            // 260 即原版的“无限制”：HUD 的缓动按帧推进（见 HudSnapshotClient.SETTLE_FRAMES）。
            "maxFps:260",
            "enableVsync:false",
            "soundCategory_master:0.0");

    static boolean enabled() {
        return Boolean.getBoolean(PROPERTY);
    }

    @Override
    public void onPreLaunch() {
        if (!enabled()) {
            return;
        }
        Path gameDirectory = FabricLoader.getInstance().getGameDir().toAbsolutePath().normalize();
        // Files are deleted below: never anywhere but the harness's own throwaway directory.
        // 下面会删除文件：只允许发生在本工具专用的一次性目录里。
        if (!gameDirectory.endsWith(Path.of("run", "hudSnapshot"))) {
            LOGGER.warn("[SparkDLC HUD snapshot] not preparing a run directory outside build/run/hudSnapshot ({})",
                    gameDirectory);
            return;
        }
        try {
            Files.createDirectories(gameDirectory);
            Files.write(gameDirectory.resolve("options.txt"), OPTIONS, StandardCharsets.UTF_8);
            deleteTree(gameDirectory.resolve("saves").resolve(WORLD_NAME));
            deleteTree(gameDirectory.resolve(SNAPSHOT_DIRECTORY));
            LOGGER.info("[SparkDLC HUD snapshot] run directory prepared: {}", gameDirectory);
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("[SparkDLC HUD snapshot] could not prepare the run directory", exception);
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> tree = Files.walk(root)) {
            for (Path path : tree.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
