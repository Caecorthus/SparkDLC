package dev.caecorthus.sparkdlc;

import net.minecraft.util.Identifier;

/**
 * Transitional holder: {@code MOD_ID}, {@code LOGGER} and {@code id(...)} for the code that still names this class.
 * It starts nothing; the start-up of the mod is {@link SparkDLC}. A later scripted pass points the call sites at
 * {@link SparkDLC} and removes this class.
 * 过渡期的持有类：为仍然引用本类的代码保留 {@code MOD_ID}、{@code LOGGER} 与 {@code id(...)}。它不启动任何东西；
 * 本模组的启动流程在 {@link SparkDLC}。之后的脚本化改名会把调用点指向 {@link SparkDLC} 并删除本类。
 */
public final class SparkAssist {
    public static final String MOD_ID = "sparkdlc";

    private SparkAssist() {
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
