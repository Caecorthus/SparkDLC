package dev.caecorthus.sparkdlc.client.bridge.witch;

import dev.caecorthus.sparkdlc.client.settings.SparkDlcSettingsApi;

/**
 * Asks SparkAssist whether its SparkDLC settings screen shows SparkWitch's scope settings (through
 * {@code api.client.ScopeSettingsApi}), so Options → Accessibility leaves its own scope rows out. The modules are fused
 * in SparkDLC, so this calls SparkAssist's public {@link SparkDlcSettingsApi#hostsScopeSettings()} directly (no
 * reflection, no {@code sparkassist} gate); it is asked at every screen init. When the answer is false,
 * Accessibility shows the rows exactly as before. Precedent: SparkFactionAPI's
 * {@link dev.caecorthus.sparkdlc.client.gui.card.GuideDecorBridge GuideDecorBridge}.
 * 询问 SparkAssist 的 SparkDLC 设置界面是否（通过 {@code api.client.ScopeSettingsApi}）显示 SparkWitch 的开镜设置，若是，
 * 「选项 → 辅助功能」就不再加入自己的开镜选项行。各模块已融合进 SparkDLC，因此直接调用 SparkAssist 公开的
 * {@link SparkDlcSettingsApi#hostsScopeSettings()}（不再反射，也不再检查 {@code sparkassist} 是否加载），每次界面初始化都会询问。
 * 答案为 false 时，辅助功能界面照旧显示这些选项行。参照 SparkFactionAPI 的
 * {@link dev.caecorthus.sparkdlc.client.gui.card.GuideDecorBridge GuideDecorBridge}。
 */
public final class SparkDlcSettingsBridge {
    private SparkDlcSettingsBridge() {
    }

    /**
     * Whether SparkAssist's settings screen hosts the scope settings.
     * SparkAssist 的设置界面是否承载开镜设置。
     */
    public static boolean hostsScopeSettings() {
        return SparkDlcSettingsApi.hostsScopeSettings();
    }
}
