package dev.caecorthus.sparkdlc.assist.client.settings;

import dev.caecorthus.sparkdlc.witch.api.client.ScopeSettingsApi;
import java.util.Arrays;
import java.util.List;

/**
 * Reads and writes SparkWitch's scope settings through its public client API, {@link ScopeSettingsApi}. The modules
 * are fused in SparkDLC, so these are direct calls and the scope page is always available (the bridge used to look
 * the API up by name and hid the page when SparkWitch was absent or a call failed).
 * 经由 SparkWitch 的公开客户端 API {@link ScopeSettingsApi} 读写瞄准镜设置。各模块已融合进 SparkDLC，因此这里是直接调用，
 * 瞄准镜页始终可用（原先按名称查找该 API，SparkWitch 不在或调用失败时隐藏该页）。
 */
final class SparkWitchScopeBridge {
    static final String PICTURE_IN_PICTURE = "picture_in_picture";

    private SparkWitchScopeBridge() {
    }

    /** Always true: SparkWitch is a module of this mod. 恒为 true：SparkWitch 是本模组的一个模块。 */
    static boolean available() {
        return true;
    }

    static String scopeMode() {
        return ScopeSettingsApi.scopeMode();
    }

    static void setScopeMode(String mode) {
        ScopeSettingsApi.setScopeMode(mode);
    }

    static List<String> scopeModes() {
        return List.copyOf(ScopeSettingsApi.scopeModes());
    }

    static String defaultScopeMode() {
        return ScopeSettingsApi.defaultScopeMode();
    }

    static boolean pictureInPictureSuppressed() {
        return ScopeSettingsApi.pictureInPictureSuppressed();
    }

    static int lensResolutionPercent() {
        return ScopeSettingsApi.lensResolutionPercent();
    }

    static void setLensResolutionPercent(int percent) {
        ScopeSettingsApi.setLensResolutionPercent(percent);
    }

    static List<Integer> lensResolutionChoices() {
        return Arrays.stream(ScopeSettingsApi.lensResolutionChoices()).boxed().toList();
    }

    static int defaultLensResolutionPercent() {
        return ScopeSettingsApi.defaultLensResolutionPercent();
    }

    static int scopedSensitivityPercent() {
        return ScopeSettingsApi.scopedSensitivityPercent();
    }

    static void setScopedSensitivityPercent(int percent) {
        ScopeSettingsApi.setScopedSensitivityPercent(percent);
    }

    static int scopedSensitivityMin() {
        return ScopeSettingsApi.scopedSensitivityMin();
    }

    static int scopedSensitivityMax() {
        return ScopeSettingsApi.scopedSensitivityMax();
    }

    static int scopedSensitivityStep() {
        return ScopeSettingsApi.scopedSensitivityStep();
    }

    static int defaultScopedSensitivityPercent() {
        return ScopeSettingsApi.defaultScopedSensitivityPercent();
    }

    static void save() {
        ScopeSettingsApi.save();
    }
}
