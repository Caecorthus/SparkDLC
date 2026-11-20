package dev.caecorthus.sparkdlc.gametest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets the client audit's run directory up as a player's disk looked before the fusion, so the real client start
 * proves what unit tests cannot: that the old names are carried over BEFORE their first reader runs (SparkDLC's
 * settings, vanilla's {@code GameOptions}). It runs as a pre-launch entrypoint, i.e. before any mod initializer, and
 * only for {@code ./gradlew runClientAudit} ({@code -Dsparkdlc.clientAudit=true}, run directory
 * {@code build/run/clientAudit}): it removes that directory's files of the new names and writes them under the old
 * names with values that differ from the defaults. {@link LegacyClientNamesCheck} then looks at what the running
 * client ended up with. No Minecraft class is touched here (none may be loaded this early).
 *
 * <p>The game's own config folder gets what a player upgrading from the five mods has: SparkAssist's settings and
 * SparkWitch's second client file (folded into {@code config/sparkdlc.json} since stage 3). The other upgrade paths of
 * that fold need a config folder each, so they are planted under {@code legacy-fold-cases/<case>/config} of the run
 * directory ({@link #FOLD_CASES}) and the check runs the same carry-over on them once the client is up.</p>
 *
 * 把客户端检查的运行目录布置成融合之前玩家磁盘的样子，让真实的客户端启动证明单元测试证明不了的事：旧名字是在第一个读取方
 * （SparkDLC 的设置、原版的 {@code GameOptions}）运行“之前”被带过来的。它作为 pre-launch 入口运行，即早于任何模组初始化，
 * 且只在 {@code ./gradlew runClientAudit}（{@code -Dsparkdlc.clientAudit=true}，运行目录 {@code build/run/clientAudit}）中动作：
 * 删除该目录中新名字的文件，并以旧名字写入与默认值不同的内容。之后由 {@link LegacyClientNamesCheck} 检查运行中的客户端最终
 * 得到的结果。这里不接触任何 Minecraft 类（这么早的阶段不允许加载）。
 *
 * <p>游戏自己的配置文件夹布置成从五个旧模组升级的玩家的样子：SparkAssist 的设置与 SparkWitch 的第二个客户端文件（自第三阶段起
 * 并入 {@code config/sparkdlc.json}）。该并入的其他升级路径各需要一个配置文件夹，因此布置在运行目录的
 * {@code legacy-fold-cases/<case>/config} 下（{@link #FOLD_CASES}），客户端启动后由检查对它们执行同样的迁移。</p>
 */
public final class LegacyClientNamesFixture implements PreLaunchEntrypoint {
    static final String OLD_SKILL_KEY = "key.sparkwitch.secondary_skill";
    static final String NEW_SKILL_KEY = "key.sparkdlc.role_skill_2";
    static final String SKILL_BOUND_TO = "key.keyboard.x";
    static final String OLD_REPLAY_KEY = "key.sparkfactionapi.open_replay";
    static final String NEW_REPLAY_KEY = "key.sparkdlc.open_replay";
    static final String REPLAY_BOUND_TO = "key.keyboard.f8";
    static final int SCOPE_SENSITIVITY = 55;
    /** A skin id that is not installed: the skin manager reads it and draws vanilla. / 未安装的皮肤 id：读取后按原版绘制。 */
    static final String MINI_WITHER_SKIN = "client-audit-fixture";
    /** A key no build knows, which must survive the fold and every save. / 任何构建都不认识的键，必须在并入与保存后保留。 */
    static final String UNKNOWN_KEY = "clientAuditFutureKey";
    static final String WITCH_CLIENT_FILE = "{\"scopedSensitivityPercent\":" + SCOPE_SENSITIVITY
            + ",\"miniWitherSkin\":\"" + MINI_WITHER_SKIN + "\",\"" + UNKNOWN_KEY + "\":7}";
    static final String FOLD_CASES_DIRECTORY = "legacy-fold-cases";
    /**
     * The other upgrade paths of the client settings fold: case name to the files of its config folder. What each
     * must end up as is in {@link LegacyClientNamesCheck}.
     * 客户端设置并入的其他升级路径：用例名到其配置文件夹中的文件。各自的预期结果见 {@link LegacyClientNamesCheck}。
     */
    static final Map<String, Map<String, String>> FOLD_CASES = foldCases();
    static final String NEW_ACHIEVEMENT_SAVE = "sparkdlc/achievements/client-audit-fixture.json";

    private static final Logger LOGGER = LoggerFactory.getLogger("sparkdlc_gametest");
    /** The data version of 1.21.1; without a version line vanilla would run every options fixer since 1.0. */
    private static final String VERSION_LINE = "version:3955";
    private static boolean planted;

    /** Whether this start runs on the fixture, so the check has something to look at. / 本次启动是否使用了该布置。 */
    static boolean planted() {
        return planted;
    }

    @Override
    public void onPreLaunch() {
        if (!Boolean.getBoolean("sparkdlc.clientAudit")) {
            return;
        }
        FabricLoader loader = FabricLoader.getInstance();
        Path gameDirectory = loader.getGameDir().toAbsolutePath().normalize();
        // Files are deleted below: never anywhere but the audit's own throwaway directory.
        // 下面会删除文件：只允许发生在检查专用的一次性目录里。
        if (!gameDirectory.endsWith(Path.of("run", "clientAudit"))) {
            LOGGER.warn("[SparkDLC client audit] legacy names: not planting fixtures outside build/run/clientAudit ({})",
                    gameDirectory);
            return;
        }
        try {
            plantKeyLines(gameDirectory.resolve("options.txt"));
            Path config = loader.getConfigDir();
            plant(config, "sparkdlc.json", "sparkassist.json", "{\"instinctKeyMode\":\"toggle\"}");
            // An earlier audit run of an earlier build may have left the second file under its later name.
            // 早期构建的检查运行可能留下了改名后的第二个文件。
            plant(config, "sparkdlc-client.json", "sparkwitch-client.json", WITCH_CLIENT_FILE);
            plantFoldCases(gameDirectory.resolve(FOLD_CASES_DIRECTORY));
            plant(config, NEW_ACHIEVEMENT_SAVE, "sparkassist/achievements/client-audit-fixture.json", "{\"version\":1}");
            planted = true;
            LOGGER.info("[SparkDLC client audit] legacy names: fixtures planted under the old names");
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("[SparkDLC client audit] legacy names: could not plant the fixtures", exception);
        }
    }

    private static Map<String, Map<String, String>> foldCases() {
        Map<String, Map<String, String>> cases = new LinkedHashMap<>();
        cases.put("earlier-build", Map.of(
                "sparkdlc.json", "{\"instinctKeyMode\":\"toggle\",\"guidebookFoliage\":false}",
                "sparkdlc-client.json", "{\"scopeMode\":\"picture_in_picture\",\"scopedSensitivityPercent\":60,"
                        + "\"scopeLensResolutionPercent\":75,\"miniWitherSkin\":\"cirno\"}"));
        cases.put("five-mods-then-earlier-build", Map.of(
                "sparkassist.json", "{\"instinctKeyMode\":\"toggle\"}",
                "sparkwitch-client.json", "{\"scopedSensitivityPercent\":150,\"miniWitherSkin\":\"ghast\"}",
                "sparkdlc-client.json", "{\"scopedSensitivityPercent\":60}"));
        cases.put("both-files-disagree", Map.of(
                "sparkdlc.json", "{\"scopedSensitivityPercent\":80}",
                "sparkdlc-client.json", "{\"scopedSensitivityPercent\":60,\"miniWitherSkin\":\"cirno\"}"));
        cases.put("unreadable-old-file", Map.of(
                "sparkdlc.json", "{\"instinctKeyMode\":\"toggle\"}",
                "sparkdlc-client.json", "{ \"scopeMode\": \"picture_in_picture\", oops"));
        cases.put("already-folded", Map.of(
                "sparkdlc.json", "{\"scopedSensitivityPercent\":70,\"carriedOverFrom\":\"config/sparkdlc-client.json\"}",
                "sparkdlc-client.json", "{\"scopedSensitivityPercent\":60}"));
        return cases;
    }

    /** Replaces the case folders of an earlier run. / 替换之前运行留下的用例文件夹。 */
    private static void plantFoldCases(Path root) throws IOException {
        if (Files.exists(root)) {
            try (Stream<Path> tree = Files.walk(root)) {
                for (Path path : tree.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
        for (Map.Entry<String, Map<String, String>> foldCase : FOLD_CASES.entrySet()) {
            Path config = root.resolve(foldCase.getKey()).resolve("config");
            Files.createDirectories(config);
            for (Map.Entry<String, String> file : foldCase.getValue().entrySet()) {
                Files.writeString(config.resolve(file.getKey()), file.getValue(), StandardCharsets.UTF_8);
            }
        }
    }

    private static void plant(Path config, String newName, String oldName, String content) throws IOException {
        Files.deleteIfExists(config.resolve(newName));
        Path oldFile = config.resolve(oldName);
        Files.createDirectories(oldFile.getParent());
        Files.writeString(oldFile, content, StandardCharsets.UTF_8);
    }

    /** Leaves every other option of an earlier audit run as it is. / 之前的检查运行留下的其他选项保持原样。 */
    private static void plantKeyLines(Path options) throws IOException {
        List<String> lines = new ArrayList<>();
        boolean versioned = false;
        if (Files.isRegularFile(options)) {
            for (String line : Files.readAllLines(options, StandardCharsets.ISO_8859_1)) {
                String name = line.contains(":") ? line.substring(0, line.indexOf(':')) : line;
                if (name.equals("key_" + OLD_SKILL_KEY) || name.equals("key_" + NEW_SKILL_KEY)
                        || name.equals("key_" + OLD_REPLAY_KEY) || name.equals("key_" + NEW_REPLAY_KEY)) {
                    continue;
                }
                versioned |= name.equals("version");
                lines.add(line);
            }
        }
        if (!versioned) {
            lines.add(0, VERSION_LINE);
        }
        lines.add("key_" + OLD_SKILL_KEY + ":" + SKILL_BOUND_TO);
        lines.add("key_" + OLD_REPLAY_KEY + ":" + REPLAY_BOUND_TO);
        Files.write(options, lines, StandardCharsets.ISO_8859_1);
    }
}
