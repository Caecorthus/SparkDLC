package dev.caecorthus.sparkdlc.traits.client.render;

/** Pure selection of the winning effect; a weaker effect contributes neither spread nor brightness. */
public final class LastEscapeVisionRules {
    private LastEscapeVisionRules() {
    }

    public static float[] compose(float desaturation, float spread, float brightness) {
        return desaturation > 0.5f ? new float[] {desaturation, spread, brightness}
                : new float[] {0.5f, 0.0f, 1.0f};
    }

    /**
     * Whether [desaturation, spread, brightness] may be handed to the Witch compositor as an escape composition:
     * all finite, desaturation within [0.5, 1], spread and brightness not negative. Anything else reads as "no escape".
     * [灰阶、扩散、亮度] 是否可作为脱险参数交给 Witch 合成器：全部为有限值，灰阶在 [0.5, 1] 内，扩散与亮度不为负。
     * 其余情况一律视为“无脱险”。
     */
    public static boolean isValid(float desaturation, float spread, float brightness) {
        return Float.isFinite(desaturation) && Float.isFinite(spread) && Float.isFinite(brightness)
                && desaturation >= 0.5f && desaturation <= 1.0f && spread >= 0.0f && brightness >= 0.0f;
    }
}
