package dev.caecorthus.sparkdlc.input;

/**
 * Pure routing rules for the mod's Wathe instinct key Adapter.
 * 本模组的 Wathe 本能键 Adapter 使用的纯路由规则。
 */
public final class InstinctKeyRules {
    private static final String WATHE_INSTINCT_TRANSLATION_KEY = "key.wathe.instinct";

    private InstinctKeyRules() {
    }

    public static boolean shouldHandle(String translationKey, boolean configAvailable) {
        return configAvailable && isWatheInstinctKey(translationKey);
    }

    public static boolean isWatheInstinctKey(String translationKey) {
        return WATHE_INSTINCT_TRANSLATION_KEY.equals(translationKey);
    }
}
