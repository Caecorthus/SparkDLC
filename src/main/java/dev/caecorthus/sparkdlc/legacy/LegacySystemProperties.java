package dev.caecorthus.sparkdlc.legacy;

import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * The developer switches set with {@code -D} at launch ({@code -Dsparkdlc.debugCautiousSound=true}, ...). They were
 * named after the former mod that read them ({@code sparktraits.debugCautiousSound}, {@code sparkassist.decorDebug})
 * until stage 3; a launch script that still sets an old name keeps working. The rows are the
 * {@code system_property} kind of {@link LegacyIds} (old name, current name).
 *
 * 启动时以 {@code -D} 设置的开发开关（{@code -Dsparkdlc.debugCautiousSound=true} 等）。在第三阶段之前它们以读取它们的旧模组命名
 * （{@code sparktraits.debugCautiousSound}、{@code sparkassist.decorDebug}）；仍然设置旧名字的启动脚本继续有效。数据行是
 * {@link LegacyIds} 的 {@code system_property} 种类（旧名字、当前名字）。
 */
public final class LegacySystemProperties {
    /** The table kind this class works from. / 本类使用的对照表种类。 */
    public static final String KIND = "system_property";

    private LegacySystemProperties() {
    }

    /**
     * {@link Boolean#getBoolean(String)} of the current name; when that is not set at all, of its old name instead.
     * A current name that is set wins, even when it says {@code false}.
     * 当前名字的 {@link Boolean#getBoolean(String)}；当前名字完全没有设置时改用其旧名字。已设置的当前名字优先，即使其值为
     * {@code false}。
     */
    public static boolean getBoolean(String current) {
        return getBoolean(current, LegacySystemProperties::property, LegacyIds.renamed(KIND));
    }

    static boolean getBoolean(String current, UnaryOperator<String> properties, Map<String, String> renamed) {
        String value = properties.apply(current);
        if (value == null) {
            for (Map.Entry<String, String> row : renamed.entrySet()) {
                if (row.getValue().equals(current)) {
                    value = properties.apply(row.getKey());
                    if (value != null) {
                        break;
                    }
                }
            }
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * {@link System#getProperty(String)} that answers null where it would throw, as {@link Boolean#getBoolean} does.
     * 与 {@link Boolean#getBoolean} 一样，在会抛出异常时返回 null 的 {@link System#getProperty(String)}。
     */
    private static String property(String name) {
        try {
            return System.getProperty(name);
        } catch (IllegalArgumentException | NullPointerException | SecurityException refused) {
            return null;
        }
    }
}
