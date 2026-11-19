package dev.caecorthus.sparkdlc.client.settings;

/**
 * Frozen public API that sibling modules call (directly since the SparkDLC fusion), so they can stop showing settings
 * this screen now hosts.
 * SparkWitch's {@code SparkDlcSettingsBridge} calls {@link #hostsScopeSettings()} on every Accessibility screen init.
 * Keep the class name, the method name and the primitive {@code boolean} return.
 * 供兄弟模块调用的冻结公开 API（融合进 SparkDLC 后为直接调用），让它们不再重复显示本界面已收纳的设置。SparkWitch 的
 * {@code SparkDlcSettingsBridge} 在每次初始化辅助功能界面时调用 {@link #hostsScopeSettings()}。类名、方法名与基本类型
 * {@code boolean} 返回值都不得更改。
 */
public final class SparkDlcSettingsApi {
    private SparkDlcSettingsApi() {
    }

    /** True when the SparkDLC settings screen shows SparkWitch's scope page. SparkDLC 设置界面是否显示瞄准镜页。 */
    public static boolean hostsScopeSettings() {
        return SparkWitchScopeBridge.available();
    }
}
