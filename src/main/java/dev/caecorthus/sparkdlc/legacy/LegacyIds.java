package dev.caecorthus.sparkdlc.legacy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Identifier;

/**
 * The one table of names SparkDLC had while it was five separate mods. Every id used to live in one of the legacy
 * namespaces ({@link #LEGACY_NAMESPACES}); since the fusion everything is {@code sparkdlc:}. Anything that can still
 * hand the mod an OLD name (a saved world, a config file, a command typed by an admin, a permission node) goes through
 * this class instead of guessing, because a few ids did not simply change their namespace (two creative tabs became
 * one, {@code sparktraits:world} became {@code sparkdlc:trait_world}, ...) and the same old string can mean different
 * things in different kinds ({@code sparktraits:wraith} is a retired trait AND an old role alias).
 *
 * <p>The table is {@code tools/unify/legacy-ids.tsv} (kind, old id, new id), bundled into the jar as
 * {@code /sparkdlc/legacy-ids.tsv}. Parsing uses the JDK only, so plain unit tests can read it.</p>
 *
 * 这是 SparkDLC 还是五个独立模组时所用名称的唯一对照表。当时每个 id 都属于某个旧命名空间
 * （{@link #LEGACY_NAMESPACES}），融合后全部为 {@code sparkdlc:}。任何仍可能把旧名称交给本模组的来源（旧存档、配置文件、
 * 管理员输入的指令、权限节点）都应通过本类转换而不是猜测：有些 id 并非只换了命名空间（两个创造物品栏合成一个、
 * {@code sparktraits:world} 变为 {@code sparkdlc:trait_world} 等），同一个旧字符串在不同种类中含义也可能不同
 * （{@code sparktraits:wraith} 既是已退役的词条，也是旧的职业别名）。
 *
 * <p>对照表即 {@code tools/unify/legacy-ids.tsv}（种类、旧 id、新 id），打包进 jar 后位于
 * {@code /sparkdlc/legacy-ids.tsv}。解析只用 JDK，普通单元测试也能读取。</p>
 */
public final class LegacyIds {
    /** The current namespace of everything. / 现在所有内容使用的命名空间。 */
    public static final String NAMESPACE = "sparkdlc";
    /** The five former mod ids, still listed under {@code provides}. / 五个旧模组 id，仍列在 {@code provides} 中。 */
    public static final List<String> LEGACY_NAMESPACES =
            List.of("sparkfactionapi", "sparkstrength", "sparktraits", "sparkwitch", "sparkassist");

    private static final String RESOURCE = "/sparkdlc/legacy-ids.tsv";
    private static final Map<String, Map<String, String>> RENAMED_BY_KIND = load();

    private LegacyIds() {
    }

    /**
     * Old id to new id for one kind of the table (for example {@code "item"}, {@code "component"}, {@code "role"},
     * {@code "command"}), in table order. Rows whose id did not change are left out; an unknown kind is empty.
     * 对照表中某一种类（如 {@code "item"}、{@code "component"}、{@code "role"}、{@code "command"}）的旧 id 到新 id 映射，
     * 保持表内顺序。未改名的行不包含在内；未知种类返回空映射。
     */
    public static Map<String, String> renamed(String kind) {
        return RENAMED_BY_KIND.getOrDefault(kind, Map.of());
    }

    /**
     * The current id for {@code id} read as one of the given kinds: the first kind that lists it as an old id wins;
     * anything else (already current, another mod's id, null) is returned unchanged. It never guesses by swapping the
     * namespace: an old id that is not in the table stays as it is.
     * 把 {@code id} 按给定种类解释后得到的当前 id：第一个把它列为旧 id 的种类生效；其他情况（已是当前 id、其他模组的 id、
     * null）原样返回。绝不靠替换命名空间来猜测：不在表中的旧 id 保持不变。
     */
    public static String canonical(String id, String... kinds) {
        if (id == null) {
            return null;
        }
        for (String kind : kinds) {
            String renamed = renamed(kind).get(id);
            if (renamed != null) {
                return renamed;
            }
        }
        return id;
    }

    /** {@link #canonical(String, String...)} for an {@link Identifier}. / {@link Identifier} 版本。 */
    public static Identifier canonical(Identifier id, String... kinds) {
        if (id == null || !isLegacyNamespace(id.getNamespace())) {
            return id;
        }
        String current = canonical(id.toString(), kinds);
        return current.equals(id.toString()) ? id : Identifier.of(current);
    }

    /** Whether {@code namespace} is one of the five former mod ids. / {@code namespace} 是否为五个旧模组 id 之一。 */
    public static boolean isLegacyNamespace(String namespace) {
        return LEGACY_NAMESPACES.contains(namespace);
    }

    private static Map<String, Map<String, String>> load() {
        Map<String, Map<String, String>> byKind = new LinkedHashMap<>();
        try (InputStream in = LegacyIds.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + RESOURCE + " (bundled from tools/unify/legacy-ids.tsv)");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = reader.readLine(); // header: kind, old_id, new_id, note
            while ((line = reader.readLine()) != null) {
                String[] cells = line.split("\t", -1);
                if (cells.length < 3 || cells[1].equals(cells[2])) {
                    continue;
                }
                String previous = byKind.computeIfAbsent(cells[0], kind -> new LinkedHashMap<>())
                        .put(cells[1], cells[2]);
                if (previous != null && !previous.equals(cells[2])) {
                    throw new IllegalStateException(cells[0] + " " + cells[1] + " maps to both " + previous
                            + " and " + cells[2]);
                }
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        byKind.replaceAll((kind, ids) -> Collections.unmodifiableMap(ids));
        return Collections.unmodifiableMap(byKind);
    }
}
