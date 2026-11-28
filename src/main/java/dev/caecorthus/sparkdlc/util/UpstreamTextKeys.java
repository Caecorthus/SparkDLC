package dev.caecorthus.sparkdlc.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * The mod's own lang keys for upstream (Wathe / NoellesRoles) text it rewrites. When two mods define
 * one key in different namespaces, the value that wins is decided by {@code TranslationStorage.load}, which walks the
 * client's namespaces in {@code HashMap} order; that order changes with the installed mod set. So the mod never
 * redefines an upstream key: these call sites are pointed at the mod's own keys in code instead (as
 * {@code BartenderRules.effectTextKey} does for the bartender's ingredient lines).
 * 本模组改写上游（Wathe / NoellesRoles）文字时使用的自有 lang 键。两个模组在不同命名空间里定义同一个键时，
 * 生效的值由 {@code TranslationStorage.load} 决定，它按客户端命名空间 {@code HashMap} 的顺序遍历，这个顺序会随安装的模组
 * 变化。所以本模组不重新定义上游的键，而是在代码里把这些调用点指向自己的键（同 {@code BartenderRules.effectTextKey}
 * 对酒保调料说明的做法）。
 */
public final class UpstreamTextKeys {
    private static final String NOELLESROLES = "noellesroles";
    /** NoellesRoles roles whose welcome goal line the mod rewrites. / 开局目标句由本模组改写的 NoellesRoles 身份。 */
    private static final Set<String> RETUNED_GOAL_ROLES =
            Set.of("assassin", "bodyguard", "demon_hunter", "pathogen", "undercover");
    /** Upstream key to the mod's key: the reworked Bodyguard's letter line. / 上游键到本模组的键：重做后保镖的信件附言。 */
    private static final Map<String, String> RETUNED_KEYS = Map.of(
            "tip.letter.bodyguard.tooltip2", "tip.letter.sparkdlc.bodyguard.tooltip2"
    );

    private UpstreamTextKeys() {
    }

    /** Whether the mod owns this role's welcome goal line. / 该身份的开局目标句是否由本模组提供。 */
    public static boolean retunesGoal(String namespace, String path) {
        return NOELLESROLES.equals(namespace) && RETUNED_GOAL_ROLES.contains(path);
    }

    /**
     * Wathe's goal key shape ({@code goal} when exactly one non-killer boarded, else {@code goals}) under
     * the mod's namespace.
     * 沿用 Wathe 的目标句键形（全车恰好一个非杀手时用 {@code goal}，否则 {@code goals}），放在本模组的命名空间下。
     */
    public static String goalKey(String path, int targets) {
        return (targets == 1 ? "announcement.goal.sparkdlc." : "announcement.goals.sparkdlc.") + path;
    }

    /** Wathe's goal line with the mod's key, same colour. / 换成本模组键的 Wathe 目标行，颜色不变。 */
    public static Function<Integer, Text> goalText(String path, int colour) {
        return targets -> Text.translatable(goalKey(path, targets), targets).withColor(colour);
    }

    /** The mod's key for an upstream key, or the key itself. / 上游键对应的本模组键；没有则原样返回。 */
    public static String textKey(String key) {
        return RETUNED_KEYS.getOrDefault(key, key);
    }

    /**
     * The lines with every retuned translatable line pointed at the mod's key (same arguments, style and
     * siblings), or null when none matched.
     * 把其中可改写的翻译行指向本模组的键（参数、样式与子文本不变）；没有匹配时返回 null。
     */
    public static @Nullable List<Text> retargetLines(List<Text> lines) {
        List<Text> result = null;
        for (int index = 0; index < lines.size(); index++) {
            Text line = lines.get(index);
            if (!(line.getContent() instanceof TranslatableTextContent content)) {
                continue;
            }
            String key = textKey(content.getKey());
            if (key.equals(content.getKey())) {
                continue;
            }
            MutableText retargeted = Text.translatable(key, content.getArgs()).setStyle(line.getStyle());
            line.getSiblings().forEach(retargeted::append);
            if (result == null) {
                result = new ArrayList<>(lines);
            }
            result.set(index, retargeted);
        }
        return result;
    }
}
