package dev.caecorthus.sparkdlc.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.caecorthus.sparkdlc.util.DurableFiles;
import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.config.SparkDlcClientConfig;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The one client settings file, {@code config/sparkdlc.json}. It holds the keys of {@link SparkDlcClientConfig} and,
 * beside them, the keys other client features keep in the same file ({@link #otherKeys()}): the scope settings
 * ({@code ScopeSettingsStore}), the Mini Wither skin choice ({@code MiniWitherSkinSettings}), whatever the carry-over
 * of the second file of earlier builds brought in ({@code client.legacy.LegacyClientSettingsFold}) and the keys of a
 * newer version. Every save writes all of them, so no feature's key is lost when another one saves.
 * 唯一的客户端设置文件 {@code config/sparkdlc.json}。它保存 {@link SparkDlcClientConfig} 的键，以及其他客户端功能放在同一文件中的
 * 键（{@link #otherKeys()}）：开镜设置（{@code ScopeSettingsStore}）、小凋灵皮肤选择（{@code MiniWitherSkinSettings}）、把早期构建的
 * 第二个设置文件带过来时（{@code client.legacy.LegacyClientSettingsFold}）合并进来的键，以及更新版本写下的键。每次保存都写入全部
 * 这些键，因此某个功能保存时不会丢失其他功能的键。
 */
public final class SparkDlcConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("SparkDLC/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;
    private final SparkDlcClientConfig config;
    /** The file's keys that {@link SparkDlcClientConfig} does not own, in file order. / 不属于该模型的键，保持文件中的顺序。 */
    private final JsonObject otherKeys;
    // False when the file exists but could not be read (or its broken content could not be moved aside): this run
    // then starts from the defaults, keeps every change in memory only and never writes over the file.
    // 文件存在却读不出来（或其损坏的内容无法改名留存）时为 false：本次运行从默认值开始，所有改动只保存在内存中，
    // 绝不覆盖该文件。
    private final boolean writable;

    private SparkDlcConfigManager(Path path, SparkDlcClientConfig config, JsonObject otherKeys, boolean writable) {
        this.path = path;
        this.config = config;
        this.otherKeys = otherKeys;
        this.writable = writable;
    }

    public static SparkDlcConfigManager load() {
        return load(FabricLoader.getInstance().getConfigDir().resolve(SparkDLC.MOD_ID + ".json"),
                System.currentTimeMillis());
    }

    /**
     * Reads {@code path} and writes it straight back (adding keys a newer version knows). A missing file starts from
     * the defaults. A file whose content is broken is moved to {@code <name>.broken-<epochMillis>} first. One that
     * cannot be read (perhaps locked for a moment by another program) or moved aside is never written over for the
     * rest of this run; the next launch reads it again. The keys the model does not own are kept as they are.
     * 读取 {@code path} 并立即写回（补上新版本认识的键）。文件不存在时从默认值开始。内容损坏的文件会先被改名为
     * {@code <name>.broken-<epochMillis>}。读不出来（可能被其他程序暂时锁住）或无法改名留存的文件，本次运行中绝不覆盖，
     * 下次启动时再读取。不属于该模型的键原样保留。
     */
    public static SparkDlcConfigManager load(Path path, long epochMillis) {
        SparkDlcClientConfig config = SparkDlcClientConfig.defaults();
        JsonObject otherKeys = new JsonObject();
        boolean writable = true;
        if (!Files.notExists(path)) {
            try {
                JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                config = SparkDlcClientConfig.fromJson(json);
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if (!SparkDlcClientConfig.KEYS.contains(entry.getKey())) {
                        otherKeys.add(entry.getKey(), entry.getValue());
                    }
                }
            } catch (CharacterCodingException | RuntimeException broken) {
                Path aside = path.resolveSibling(path.getFileName() + ".broken-" + epochMillis);
                try {
                    Files.move(path, aside, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.warn("SparkDLC settings in {} could not be parsed and were moved to {}", path, aside);
                } catch (IOException moveFailure) {
                    writable = false;
                }
            } catch (IOException readFailure) {
                writable = false;
            }
            if (!writable) {
                LOGGER.error("SparkDLC settings in {} could not be read; defaults are used and the file is not"
                        + " written until the next launch", path);
            }
        }
        SparkDlcConfigManager manager = new SparkDlcConfigManager(path, config, otherKeys, writable);
        manager.save();
        return manager;
    }

    public SparkDlcClientConfig config() {
        return config;
    }

    /**
     * A copy of the file's keys that {@link SparkDlcClientConfig} does not own (see the class comment).
     * 文件中不属于 {@link SparkDlcClientConfig} 的键的副本（见类注释）。
     */
    public synchronized JsonObject otherKeys() {
        return otherKeys.deepCopy();
    }

    /**
     * Changes those keys in memory; like the setters of {@link SparkDlcClientConfig} it leaves the file alone until
     * the next {@link #save()}. {@code change} must not touch a key of the model.
     * 在内存中修改这些键；与 {@link SparkDlcClientConfig} 的 setter 一样，在下一次 {@link #save()} 之前不写文件。
     * {@code change} 不得改动模型的键。
     */
    public synchronized void editOtherKeys(Consumer<JsonObject> change) {
        change.accept(otherKeys);
        SparkDlcClientConfig.KEYS.forEach(otherKeys::remove);
    }

    /**
     * Writes every key, the model's first; false when nothing was written (an unreadable file, or a failed write).
     * 写入全部键，模型的键在前；未写入时（文件无法读取或写入失败）返回 false。
     */
    public synchronized boolean save() {
        if (!writable) {
            return false;
        }
        try {
            // Written to a sibling, forced to disk and then moved over the file (as the achievement save does), so a
            // crash or power cut mid-write keeps the previous settings instead of a cut-off file.
            // 先写到同目录的临时文件并刷到磁盘，再替换正式文件（与成就存档相同），写到一半崩溃或断电时保留上一次的设置，
            // 而不是留下被截断的文件。
            DurableFiles.write(path, () -> {
                JsonObject json = config.toJson();
                otherKeys.entrySet().forEach(entry -> json.add(entry.getKey(), entry.getValue().deepCopy()));
                return GSON.toJson(json);
            });
            return true;
        } catch (IOException failure) {
            // Config save failures should not break the client settings screen; the next save writes everything again.
            // 配置保存失败不应导致客户端设置界面崩溃；下一次保存会再次写入全部内容。
            LOGGER.warn("Unable to save the SparkDLC settings to {}", path, failure);
            return false;
        }
    }
}
