package dev.caecorthus.sparkdlc.client;

import dev.caecorthus.sparkdlc.client.config.SparkAssistConfigManager;

/**
 * Transitional holder: forwards {@code configManager()} to {@link SparkDLCClient} for the code that still names this class. It
 * starts nothing; the client start-up of the mod is {@link SparkDLCClient}. A later scripted pass points the call
 * sites there and removes this class.
 * 过渡期的持有类：为仍然引用本类的代码把 {@code configManager()} 转发给 {@link SparkDLCClient}。它不启动任何东西；本模组的客户端启动流程在
 * {@link SparkDLCClient}。之后的脚本化改名会把调用点指向那里并删除本类。
 */
public final class SparkAssistClient {
    private SparkAssistClient() {
    }

    public static SparkAssistConfigManager configManager() {
        return SparkDLCClient.configManager();
    }
}
