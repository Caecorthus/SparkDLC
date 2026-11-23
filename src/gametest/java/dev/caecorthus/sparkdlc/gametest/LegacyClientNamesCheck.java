package dev.caecorthus.sparkdlc.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.caecorthus.sparkdlc.client.SparkDLCClient;
import dev.caecorthus.sparkdlc.client.config.SparkDlcConfigManager;
import dev.caecorthus.sparkdlc.client.legacy.LegacyClientNames;
import dev.caecorthus.sparkdlc.client.legacy.LegacyClientSettingsFold;
import dev.caecorthus.sparkdlc.client.role.killer.witchmaiden.skin.MiniWitherSkinManager;
import dev.caecorthus.sparkdlc.client.scope.ScopeMode;
import dev.caecorthus.sparkdlc.client.scope.ScopeSettings;
import dev.caecorthus.sparkdlc.client.scope.ScopeSettingsStore;
import dev.caecorthus.sparkdlc.config.SparkDlcClientConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The other half of {@link LegacyClientNamesFixture}: once the client is up, what it reads under the new names must
 * be what the fixture wrote under the old ones. One log line says so, or names what was not carried over; it never
 * throws, so a mistake here cannot hide the mixin audit's own result.
 * {@link LegacyClientNamesFixture} 的另一半：客户端启动完成后，它在新名字下读到的内容必须就是布置时写在旧名字下的内容。
 * 结果用一行日志给出，或指出哪一项没有被带过来；绝不抛出异常，因此这里的差错不会掩盖 mixin 检查本身的结果。
 */
final class LegacyClientNamesCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger("sparkdlc_gametest");

    private LegacyClientNamesCheck() {
    }

    static void run(MinecraftClient client) {
        if (!LegacyClientNamesFixture.planted()) {
            return;
        }
        List<String> missing = new ArrayList<>();
        try {
            if (SparkDLCClient.configManager().config().instinctKeyMode() != SparkDlcClientConfig.InstinctKeyMode.TOGGLE) {
                missing.add("config/sparkassist.json (instinctKeyMode)");
            }
            if (ScopeSettingsStore.current().sensitivityPercent() != LegacyClientNamesFixture.SCOPE_SENSITIVITY) {
                missing.add("config/sparkwitch-client.json (scopedSensitivityPercent)");
            }
            checkOneSettingsFile(missing);
            checkFoldCases(missing);
            if (!Files.isRegularFile(FabricLoader.getInstance().getConfigDir()
                    .resolve(LegacyClientNamesFixture.NEW_ACHIEVEMENT_SAVE))) {
                missing.add("config/sparkassist/achievements/client-audit-fixture.json");
            }
            // The key a registered binding ended up with is what vanilla's GameOptions#load read from options.txt.
            // 已注册的绑定最终得到的键，就是原版 GameOptions#load 从 options.txt 读到的内容。
            expectBoundKey(client, LegacyClientNamesFixture.NEW_SKILL_KEY, LegacyClientNamesFixture.SKILL_BOUND_TO, missing);
            expectBoundKey(client, LegacyClientNamesFixture.NEW_REPLAY_KEY, LegacyClientNamesFixture.REPLAY_BOUND_TO, missing);
        } catch (IOException | RuntimeException | LinkageError failure) {
            LOGGER.error("[SparkDLC client audit] legacy names: the check itself failed", failure);
            return;
        }
        if (missing.isEmpty()) {
            LOGGER.info("[SparkDLC client audit] legacy names: settings, scope settings, Mini Wither skin, achievement"
                    + " save and both key bindings were carried over from their old names; the client settings fold"
                    + " passed its {} upgrade cases", LegacyClientNamesFixture.FOLD_CASES.size());
        } else {
            LOGGER.error("[SparkDLC client audit] legacy names: NOT carried over: {}", missing);
        }
    }

    /**
     * SparkWitch's second client file reached the one settings file: read by the real readers, every key kept on disk,
     * no second file created, the old file untouched as the backup.
     * SparkWitch 的第二个客户端文件到达了唯一的设置文件：真实读取方读到它，磁盘上每个键都在，没有创建第二个文件，旧文件原样留作备份。
     */
    private static void checkOneSettingsFile(List<String> missing) throws IOException {
        Path config = FabricLoader.getInstance().getConfigDir();
        String skin = MiniWitherSkinManager.selectedId();
        if (!LegacyClientNamesFixture.MINI_WITHER_SKIN.equals(skin)) {
            missing.add("config/sparkwitch-client.json (miniWitherSkin, read as \"" + skin + "\")");
        }
        JsonObject others = SparkDLCClient.configManager().otherKeys();
        if (!Objects.equals(others.get(LegacyClientNamesFixture.UNKNOWN_KEY), JsonParser.parseString("7"))) {
            missing.add("config/sparkwitch-client.json (" + LegacyClientNamesFixture.UNKNOWN_KEY + ")");
        }
        JsonObject onDisk = JsonParser.parseString(Files.readString(config.resolve("sparkdlc.json"))).getAsJsonObject();
        for (String key : List.of("instinctKeyMode", "scopedSensitivityPercent", "miniWitherSkin",
                LegacyClientNamesFixture.UNKNOWN_KEY, LegacyClientSettingsFold.MARKER_KEY)) {
            if (!onDisk.has(key)) {
                missing.add("config/sparkdlc.json on disk (" + key + ")");
            }
        }
        if (!"config/sparkwitch-client.json".equals(string(onDisk.get(LegacyClientSettingsFold.MARKER_KEY)))) {
            missing.add("config/sparkdlc.json (" + LegacyClientSettingsFold.MARKER_KEY + " = "
                    + onDisk.get(LegacyClientSettingsFold.MARKER_KEY) + ")");
        }
        if (Files.exists(config.resolve("sparkdlc-client.json"))) {
            missing.add("config/sparkdlc-client.json must not be created");
        }
        Path backup = config.resolve("sparkwitch-client.json");
        if (!Files.isRegularFile(backup) || !LegacyClientNamesFixture.WITCH_CLIENT_FILE.equals(
                Files.readString(backup, StandardCharsets.UTF_8))) {
            missing.add("config/sparkwitch-client.json kept unchanged as the backup");
        }
    }

    /**
     * The other upgrade paths, each in its own folder: the same carry-over the client entrypoint runs, then the same
     * settings manager, then what each must end up as.
     * 其他升级路径，各在自己的文件夹中：执行与客户端入口相同的迁移，再用相同的设置管理器读取，然后检查各自的预期结果。
     */
    private static void checkFoldCases(List<String> missing) throws IOException {
        Path root = FabricLoader.getInstance().getGameDir().resolve(LegacyClientNamesFixture.FOLD_CASES_DIRECTORY);
        for (Map.Entry<String, Map<String, String>> foldCase : LegacyClientNamesFixture.FOLD_CASES.entrySet()) {
            String name = foldCase.getKey();
            Path config = root.resolve(name).resolve("config");
            List<String> folded = LegacyClientNames.carryOverFiles(config);
            List<String> again = LegacyClientNames.carryOverFiles(config);
            SparkDlcConfigManager manager = SparkDlcConfigManager.load(config.resolve("sparkdlc.json"),
                    System.currentTimeMillis());
            JsonObject others = manager.otherKeys();
            ScopeSettings scope = ScopeSettings.fromJson(others);
            SparkDlcClientConfig settings = manager.config();
            boolean ok = switch (name) {
                case "earlier-build" -> folded.equals(List.of("config/sparkdlc-client.json"))
                        && settings.instinctKeyMode() == SparkDlcClientConfig.InstinctKeyMode.TOGGLE
                        && !settings.guidebookFoliage()
                        && scope.equals(new ScopeSettings(ScopeMode.PICTURE_IN_PICTURE, 60, 75))
                        && "cirno".equals(string(others.get("miniWitherSkin")));
                case "five-mods-then-earlier-build" -> folded.equals(List.of("config/sparkdlc-client.json"))
                        && settings.instinctKeyMode() == SparkDlcClientConfig.InstinctKeyMode.TOGGLE
                        && scope.sensitivityPercent() == 60 && !others.has("miniWitherSkin");
                case "both-files-disagree" -> folded.equals(List.of("config/sparkdlc-client.json"))
                        && scope.sensitivityPercent() == 80 && "cirno".equals(string(others.get("miniWitherSkin")));
                case "unreadable-old-file" -> folded.equals(List.of("config/sparkdlc-client.json"))
                        && settings.instinctKeyMode() == SparkDlcClientConfig.InstinctKeyMode.TOGGLE
                        && scope.equals(ScopeSettings.DEFAULTS);
                case "already-folded" -> folded.isEmpty() && scope.sensitivityPercent() == 70;
                default -> false;
            };
            // Every case: run once, the marker set, no second file created, every old file kept as planted.
            // 每个用例：只执行一次，带有标记，没有创建第二个文件，每个旧文件保持布置时的内容。
            ok &= again.isEmpty() && others.has(LegacyClientSettingsFold.MARKER_KEY);
            for (Map.Entry<String, String> file : foldCase.getValue().entrySet()) {
                if (!file.getKey().equals("sparkdlc.json")) {
                    ok &= file.getValue().equals(Files.readString(config.resolve(file.getKey()), StandardCharsets.UTF_8));
                }
            }
            ok &= foldCase.getValue().containsKey("sparkdlc-client.json") || !Files.exists(config.resolve("sparkdlc-client.json"));
            if (!ok) {
                missing.add("client settings fold case '" + name + "' (folded " + folded + ", then " + again
                        + "; settings " + others + ")");
            }
        }
    }

    private static String string(JsonElement element) {
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    private static void expectBoundKey(MinecraftClient client, String translationKey, String boundKey,
                                       List<String> missing) {
        for (KeyBinding binding : client.options.allKeys) {
            if (translationKey.equals(binding.getTranslationKey())) {
                if (!boundKey.equals(binding.getBoundKeyTranslationKey())) {
                    missing.add("key_" + translationKey + " (bound to " + binding.getBoundKeyTranslationKey()
                            + ", expected " + boundKey + ")");
                }
                return;
            }
        }
        missing.add("key_" + translationKey + " (no such binding is registered)");
    }
}
