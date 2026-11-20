package dev.caecorthus.sparkdlc.client.role.killer.witchmaiden.skin;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.caecorthus.sparkdlc.client.SparkDLCClient;
import dev.caecorthus.sparkdlc.client.config.SparkDlcConfigManager;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * The selected skin id, key {@value #KEY} in the one client settings file {@code config/sparkdlc.json}
 * ({@link SparkDlcConfigManager}, which keeps it beside the other settings and writes them all on every save, the
 * scope keys and any unknown key included). A missing key or a non-string value means the default (vanilla). A file
 * that exists but could not be read when the game started is never written for the rest of the run (that would wipe
 * the other settings): the choice stays live for this session. Until stage 3 the key lived in
 * {@code config/sparkdlc-client.json}, which is folded into the one file on the first start
 * ({@code client.legacy.LegacyClientSettingsFold}).
 * 选中的皮肤 id，保存在唯一的客户端设置文件 {@code config/sparkdlc.json} 的 {@value #KEY} 键（{@link SparkDlcConfigManager} 把它与
 * 其他设置放在一起，每次保存都写入全部设置，包括开镜键与任何未知键）。键缺失或值不是字符串时视为默认（原版）。启动时文件存在但
 * 无法读取的，本次运行中绝不写入（否则会抹掉其他设置）：选择只在本次会话中生效。第三阶段之前该键位于
 * {@code config/sparkdlc-client.json}，首次启动时并入这一个文件（{@code client.legacy.LegacyClientSettingsFold}）。
 */
final class MiniWitherSkinSettings {
    static final String KEY = "miniWitherSkin";
    /** Test seam ({@link #useStore}); null means the client's settings file. / 测试接缝；null 表示客户端的设置文件。 */
    @Nullable
    private static SparkDlcConfigManager store;

    private MiniWitherSkinSettings() {
    }

    /** The stored id, trimmed and lower-cased; {@code ""} for the default. / 已保存的 id（去空白、转小写）。 */
    static synchronized String read() {
        JsonElement value = store().otherKeys().get(KEY);
        if (value instanceof JsonPrimitive primitive && primitive.isString()) {
            return normalize(primitive.getAsString());
        }
        return MiniWitherSkinManager.DEFAULT_ID;
    }

    /**
     * Sets {@code id} and saves the settings file; false when nothing was written (an unreadable file, or a failed
     * write), in which case the choice still holds for this session.
     * 设置 id 并保存设置文件；未写入时（文件无法读取或写入失败）返回 false，此时选择仍在本次会话中有效。
     */
    static synchronized boolean write(String id) {
        SparkDlcConfigManager settings = store();
        settings.editOtherKeys(json -> json.addProperty(KEY, normalize(id)));
        return settings.save();
    }

    static String normalize(@Nullable String id) {
        return id == null ? MiniWitherSkinManager.DEFAULT_ID : id.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Test seam: reads and writes {@code testStore} instead (null: the client's settings file again).
     * 测试接缝：改为读写 {@code testStore}（null：恢复客户端的设置文件）。
     */
    static synchronized void useStore(@Nullable SparkDlcConfigManager testStore) {
        store = testStore;
    }

    private static SparkDlcConfigManager store() {
        SparkDlcConfigManager override = store;
        return override != null ? override : SparkDLCClient.configManager();
    }
}
