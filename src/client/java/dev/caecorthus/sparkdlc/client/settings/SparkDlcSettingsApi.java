package dev.caecorthus.sparkdlc.client.settings;

/**
 * What the SparkDLC settings screen hosts, so other screens can stop showing the same settings. The scope feature's
 * {@code ScopeAccessibilityOptionsMixin} calls {@link #hostsScopeSettings()} on every Accessibility screen init.
 * Keep the class name, the method name and the primitive {@code boolean} return.
 * SparkDLC 设置界面收纳了哪些设置，让其他界面不再重复显示。开镜功能的 {@code ScopeAccessibilityOptionsMixin} 在每次初始化
 * 辅助功能界面时调用 {@link #hostsScopeSettings()}。类名、方法名与基本类型 {@code boolean} 返回值都不得更改。
 */
public final class SparkDlcSettingsApi {
    private SparkDlcSettingsApi() {
    }

    /** True when the SparkDLC settings screen shows SparkWitch's scope page. SparkDLC 设置界面是否显示瞄准镜页。 */
    public static boolean hostsScopeSettings() {
        return true;
    }
}
