package dev.caecorthus.sparkdlc.client;

import dev.caecorthus.sparkdlc.client.SparkAssistClient;
import dev.caecorthus.sparkdlc.client.SparkFactionApiClient;
import dev.caecorthus.sparkdlc.client.SparkStrengthClient;
import dev.caecorthus.sparkdlc.client.SparkTraitsClient;
import dev.caecorthus.sparkdlc.client.SparkWitchClient;
import dev.caecorthus.sparkdlc.client.legacy.LegacyClientNames;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client entrypoint of SparkDLC. Same rule as {@link dev.caecorthus.sparkdlc.SparkDLC}: the modules start in the
 * alphabetical mod-id order they had as separate mods, with SparkAssist first.
 * SparkDLC 的客户端入口。与 {@link dev.caecorthus.sparkdlc.SparkDLC} 同理：各模块按独立 mod 时的 mod id 字母序启动，
 * SparkAssist 最先。
 */
public final class SparkDLCClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Files and key bindings saved under the old names, before any module reads the new ones.
        // 以旧名字保存的文件与按键绑定，要在任何模块读取新名字之前带过来。
        LegacyClientNames.carryOver();
        new SparkAssistClient().onInitializeClient();
        new SparkFactionApiClient().onInitializeClient();
        new SparkStrengthClient().onInitializeClient();
        new SparkTraitsClient().onInitializeClient();
        new SparkWitchClient().onInitializeClient();
    }
}
