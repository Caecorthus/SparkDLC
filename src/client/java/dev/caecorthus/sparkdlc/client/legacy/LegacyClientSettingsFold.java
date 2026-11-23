package dev.caecorthus.sparkdlc.client.legacy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.caecorthus.sparkdlc.legacy.LegacyFiles;
import dev.caecorthus.sparkdlc.legacy.LegacyIds;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Folds the second client settings file of earlier builds into the one settings file. Until stage 3 the scope
 * settings and the Mini Wither skin choice had a file of their own, {@code config/sparkdlc-client.json} (SparkWitch's
 * {@code config/sparkwitch-client.json} before the rename); now every client setting is in {@code config/sparkdlc.json}
 * ({@code SparkDlcConfigManager}). The rows are the {@code file_fold} kind of {@link LegacyIds} (old file, the file it
 * folds into), in the order they are tried. The rule, for each target:
 * <ul>
 *     <li>the first row whose old file exists is the source; a later row is not read (SparkWitch's file only counts
 *     when no earlier SparkDLC build wrote its own, as with the plain file carry-over before);</li>
 *     <li>every key of the source is copied into the target unless the target already has it: the target is the file
 *     the running build writes, so a key there is never older than the same key in a file only older builds
 *     wrote (the two files never shared a key: a conflict means a hand edit or a key of a newer version);</li>
 *     <li>the target then carries {@value #MARKER_KEY} with the old path: a target that has it is never folded into
 *     again, so the fold runs once, and a setting the player changes or removes afterwards stays as it is. (An earlier
 *     build that is started in between rewrites the target without that key and without the scope keys, and writes
 *     its own file; the next start of this build then folds that newer state in.)</li>
 *     <li>the old file is never changed or deleted: it stays as the backup of the last state the old jars saw, like
 *     the other carry-overs ({@link LegacyFiles}).</li>
 * </ul>
 * A source whose content is not a JSON object (broken, blank) has nothing to carry: the target is still marked, the
 * old file stays as it is, and a broken one is named in the log, so the old readers' rule (an unreadable file means
 * the defaults) holds. A source or target that cannot be read at all, or a target whose content is broken, is left
 * alone and tried again on the next start; {@code SparkDlcConfigManager} then deals with the broken target as it does
 * with any broken settings file. Must run after {@link LegacyFiles#carryOverAll} (which may create the target from
 * SparkAssist's file) and before the target's first reader.
 *
 * 把早期构建的第二个客户端设置文件并入唯一的设置文件。第三阶段之前，开镜设置与小凋灵皮肤选择有自己的文件
 * {@code config/sparkdlc-client.json}（改名之前是 SparkWitch 的 {@code config/sparkwitch-client.json}）；现在所有客户端设置都在
 * {@code config/sparkdlc.json} 中（{@code SparkDlcConfigManager}）。数据行是 {@link LegacyIds} 的 {@code file_fold} 种类（旧文件、
 * 它并入的文件），按尝试顺序排列。对每个目标文件的规则：第一个旧文件存在的行就是来源，之后的行不读取（与之前普通的文件迁移一样，
 * 只有在没有早期 SparkDLC 构建写下的文件时才使用 SparkWitch 的文件）；来源中的每个键都复制到目标中，除非目标已有该键：目标是正在运行的
 * 构建所写的文件，其中的键绝不会比只有旧构建才写的文件中的同名键更旧（两个文件从未共用过键：出现冲突说明是手工修改或更新版本的键）；
 * 随后目标带上 {@value #MARKER_KEY}，值为旧路径：带有该键的目标绝不会再被并入，因此并入只执行一次，玩家之后修改或删除的设置保持原样
 * （若中途启动了早期构建，它会去掉该键与开镜键重写目标，并写自己的文件；本构建下次启动时再把这一较新的状态并入）；旧文件绝不修改或
 * 删除，与其他迁移（{@link LegacyFiles}）一样留作旧 jar 最后状态的备份。内容不是 JSON 对象（损坏、空白）的来源没有可带的内容：
 * 目标仍会被标记，旧文件保持原样，损坏的会在日志中指出，因此旧读取方的规则（无法读取的文件即默认值）不变。完全无法读取的来源或
 * 目标、内容损坏的目标保持不动，下次启动时再试；随后由 {@code SparkDlcConfigManager} 像处理任何损坏的设置文件一样处理损坏的目标。
 * 必须在 {@link LegacyFiles#carryOverAll}（它可能由 SparkAssist 的文件创建目标）之后、目标的第一个读取方之前执行。
 */
public final class LegacyClientSettingsFold {
    /** The table kind this class works from. / 本类使用的对照表种类。 */
    public static final String KIND = "file_fold";
    /**
     * The key a target gets once a file was folded into it; its value is the old file's table path.
     * 并入完成后目标获得的键；值为旧文件在对照表中的路径。
     */
    public static final String MARKER_KEY = "carriedOverFrom";

    private static final Logger LOGGER = LoggerFactory.getLogger("SparkDLC/Legacy");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_PREFIX = "config/";

    private LegacyClientSettingsFold() {
    }

    /**
     * Folds what is due under {@code configDirectory}; returns the table paths of the files folded in this call.
     * Never throws: a failure is logged and tried again on the next start.
     * 在 {@code configDirectory} 下执行到期的并入；返回本次并入的文件在对照表中的路径。绝不抛出异常：失败只记录日志，下次启动时再试。
     */
    public static List<String> carryOver(Path configDirectory) {
        return carryOver(configDirectory, LegacyIds.renamed(KIND));
    }

    static List<String> carryOver(Path configDirectory, Map<String, String> rows) {
        Map<String, List<String>> sourcesByTarget = new LinkedHashMap<>();
        rows.forEach((oldPath, target) -> sourcesByTarget.computeIfAbsent(target, path -> new ArrayList<>()).add(oldPath));
        List<String> folded = new ArrayList<>();
        sourcesByTarget.forEach((target, sources) -> {
            try {
                String source = fold(configDirectory, target, sources);
                if (source != null) {
                    folded.add(source);
                }
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("Could not fold {} into {}; it is tried again on the next start", sources, target,
                        exception);
            }
        });
        return folded;
    }

    @Nullable
    private static String fold(Path configDirectory, String target, List<String> sources) throws IOException {
        String source = null;
        Path sourceFile = null;
        for (String candidate : sources) {
            Path file = configDirectory.resolve(insideConfig(candidate));
            if (Files.isRegularFile(file)) {
                source = candidate;
                sourceFile = file;
                break;
            }
        }
        if (source == null) {
            return null;
        }
        Path targetFile = configDirectory.resolve(insideConfig(target));
        JsonObject merged = new JsonObject();
        if (Files.exists(targetFile, LinkOption.NOFOLLOW_LINKS)) {
            JsonObject existing = parseObject(Files.readString(targetFile, StandardCharsets.UTF_8));
            if (existing == null) {
                LOGGER.warn("{} is not a readable settings object; {} is folded into it on a later start", targetFile,
                        sourceFile);
                return null;
            }
            if (existing.has(MARKER_KEY)) {
                return null;
            }
            merged = existing;
        }
        String text = Files.readString(sourceFile, StandardCharsets.UTF_8);
        JsonObject old = parseObject(text);
        int carried = 0;
        if (old != null) {
            for (Map.Entry<String, JsonElement> entry : old.entrySet()) {
                if (!merged.has(entry.getKey()) && !MARKER_KEY.equals(entry.getKey())) {
                    merged.add(entry.getKey(), entry.getValue());
                    carried++;
                }
            }
        } else if (!text.isBlank()) {
            LOGGER.warn("{} is not a JSON object; none of its settings are carried over and it is kept as it is",
                    sourceFile);
        }
        merged.addProperty(MARKER_KEY, source);
        LegacyFiles.writeAtomically(targetFile, GSON.toJson(merged).getBytes(StandardCharsets.UTF_8));
        LOGGER.info("Folded {} setting(s) of {} into {}; the old file is kept as a backup", carried, sourceFile,
                targetFile);
        return source;
    }

    /** The object {@code text} holds, or null for anything else (broken, blank, an array ...). / 不是对象时为 null。 */
    @Nullable
    private static JsonObject parseObject(String text) {
        try {
            JsonElement element = JsonParser.parseString(text);
            return element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (RuntimeException broken) {
            return null;
        }
    }

    private static String insideConfig(String tablePath) {
        if (!tablePath.startsWith(CONFIG_PREFIX) || tablePath.contains("..") || tablePath.endsWith("/")) {
            throw new IllegalStateException("legacy-ids.tsv: a file_fold row names one file inside config/: "
                    + tablePath);
        }
        return tablePath.substring(CONFIG_PREFIX.length());
    }
}
