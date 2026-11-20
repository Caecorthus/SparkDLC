package dev.caecorthus.sparkdlc.client.guidebook.ui.decor;

import dev.caecorthus.sparkdlc.client.config.SparkDlcClientSettings;
import dev.caecorthus.sparkdlc.config.SparkDlcClientConfig.GuidebookDecor;
import dev.caecorthus.sparkdlc.legacy.LegacySystemProperties;

/**
 * The decoration settings as the painters read them each frame, plus the developer overlay switch
 * ({@code -Dsparkdlc.decorDebug=true} paints the no-go rectangles in red; the name before stage 3 is still accepted).
 * 绘制时读取的点缀设置，以及开发用的禁区叠加开关（-Dsparkdlc.decorDebug=true 用红色画出禁区；第三阶段之前的名字仍然有效）。
 */
public final class DecorSettings {
    private static final boolean DEBUG_ZONES = LegacySystemProperties.getBoolean("sparkdlc.decorDebug");

    private DecorSettings() {
    }

    /** -1 off, 0 light, 1 medium, 2 full. 关为 -1，轻 0，中 1，满 2。 */
    public static int density() {
        return SparkDlcClientSettings.guidebookDecor().density();
    }

    public static boolean enabled() {
        return SparkDlcClientSettings.guidebookDecor() != GuidebookDecor.OFF;
    }

    public static boolean foliage() {
        return enabled() && SparkDlcClientSettings.guidebookFoliage();
    }

    public static boolean debugZones() {
        return DEBUG_ZONES;
    }
}
