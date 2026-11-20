package dev.caecorthus.sparkdlc.client.settings;

/**
 * What the SparkDLC settings screen hosts, so other screens can stop showing the same settings. The scope feature's
 * {@code ScopeAccessibilityOptionsMixin} calls {@link #hostsScopeSettings()} on every Accessibility screen init.
 * SparkDLC 设置界面收纳了哪些设置，让其他界面不再重复显示。开镜功能的 {@code ScopeAccessibilityOptionsMixin} 在每次初始化
 * 辅助功能界面时调用 {@link #hostsScopeSettings()}。
 */
public final class SparkDlcSettingsApi {
    private SparkDlcSettingsApi() {
    }

    /**
     * Whether the SparkDLC settings screen shows the scope page: always, so the Accessibility rows it replaces are
     * unreachable since the fusion. / SparkDLC 设置界面是否显示瞄准镜页：恒为是，因此它所取代的辅助功能界面行在融合后不可达。
     */
    public static boolean hostsScopeSettings() {
        return true;
    }
}
