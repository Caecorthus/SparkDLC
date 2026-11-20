package dev.caecorthus.sparkdlc.gametest;

import dev.caecorthus.sparkdlc.client.SparkDLCClient;
import dev.caecorthus.sparkdlc.client.scope.ScopeSettingsStore;
import dev.caecorthus.sparkdlc.config.SparkDlcClientConfig;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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
            if (!Files.isRegularFile(FabricLoader.getInstance().getConfigDir()
                    .resolve(LegacyClientNamesFixture.NEW_ACHIEVEMENT_SAVE))) {
                missing.add("config/sparkassist/achievements/client-audit-fixture.json");
            }
            // The key a registered binding ended up with is what vanilla's GameOptions#load read from options.txt.
            // 已注册的绑定最终得到的键，就是原版 GameOptions#load 从 options.txt 读到的内容。
            expectBoundKey(client, LegacyClientNamesFixture.NEW_SKILL_KEY, LegacyClientNamesFixture.SKILL_BOUND_TO, missing);
            expectBoundKey(client, LegacyClientNamesFixture.NEW_REPLAY_KEY, LegacyClientNamesFixture.REPLAY_BOUND_TO, missing);
        } catch (RuntimeException | LinkageError failure) {
            LOGGER.error("[SparkDLC client audit] legacy names: the check itself failed", failure);
            return;
        }
        if (missing.isEmpty()) {
            LOGGER.info("[SparkDLC client audit] legacy names: settings, scope settings, achievement save and both key"
                    + " bindings were carried over from their old names");
        } else {
            LOGGER.error("[SparkDLC client audit] legacy names: NOT carried over: {}", missing);
        }
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
