package dev.caecorthus.sparkdlc.legacy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts the ids inside an achievement save written before the fusion. The save's {@code stats} object
 * ({@code LifetimeStats}) keys its lifetime counts and sets by role, faction, item, trait, death-reason and
 * role-change-cause id, and a round played with the fused jar arrives under the {@code sparkdlc:} ids. Without this
 * every such statistic would split in two: "wins as the Grand Witch" would start again from zero, while the
 * rules that count distinct keys ("roles played", "ways to die", "items eaten") would count the same thing twice.
 *
 * <p>{@link #withCurrentIds} folds each old id into its current id by what the value means:</p>
 * <ul>
 *     <li>count maps: the old key's count is ADDED to the current key's (the reader itself would let the later key
 *     overwrite the earlier one);</li>
 *     <li>maps of id sets and the death-reason list: the union, without duplicates.</li>
 * </ul>
 * It is a function of the file content, so loading the same file twice gives the same result, and once the save
 * has been written back no old id is left and it changes nothing. Each field is read as the kind of id it holds:
 * {@code sparktraits:wraith} in a role field is the old alias of {@code sparkdlc:wraith}, in a trait field it is the
 * retired Wraith trait and stays as it is. Fields without ids (food / drink kinds, catch names, kill groups, win
 * streak scopes), the unlock rows (plain achievement ids) and fields a newer version wrote are left alone.
 *
 * <p>The field list below is the save format as it was when the ids changed. A statistic added later can only
 * ever hold current ids, so it does not belong here.</p>
 *
 * 转换融合前写出的成就存档里的 id。存档的 {@code stats} 对象（{@code LifetimeStats}）用身份、阵营、物品、词条、死因和身份变化原因的 id
 * 作为累计计数与集合的键，而用融合后的 jar 打的对局会以 {@code sparkdlc:} 的 id 记入。不做转换的话，这类统计都会一分为二：
 * “以大魔女获胜”的次数从零重新开始，而按不同键的个数判定的规则（“玩过的身份”“死法”“吃过的物品”）又会把同一样东西算两次。
 *
 * <p>{@link #withCurrentIds} 按值的含义把每个旧 id 并入其当前 id：计数表把旧键的计数“加”到当前键上（读取方自己会让后出现的键
 * 覆盖先出现的）；id 集合表与死因列表取并集并去重。结果只取决于文件内容，同一文件读两次结果相同；存档写回之后不再有旧 id，
 * 再执行也不会改变任何内容。每个字段按其保存的 id 种类解释：{@code sparktraits:wraith} 在身份字段里是 {@code sparkdlc:wraith}
 * 的旧别名，在词条字段里则是已退役的冤魂词条，保持原样。不含 id 的字段（食物/饮品类别、渔获名、击杀分组、连胜范围）、解锁记录
 * （不带命名空间的成就 id）以及更新版本写入的字段都不改动。</p>
 *
 * <p>下面的字段表是 id 改名那一刻的存档格式。之后新增的统计只会保存当前 id，因此不属于这里。</p>
 */
public final class LegacyAchievementSave {
    private static final String[] ROLE = {"role", "legacy_role_alias"};
    private static final String[] FACTION = {"faction"};
    private static final String[] ITEM = {"item"};
    private static final String[] TRAIT = {"trait"};
    private static final String[] DEATH_REASON = {"death_reason"};
    private static final String[] ROLE_CHANGE_CAUSE = {"role_change_cause"};
    /** Keys that are not ids: the kill groups of {@code RoleGroups}. / 不是 id 的键：{@code RoleGroups} 的击杀分组。 */
    private static final String[] NOT_AN_ID = {};

    /** Id to count. / id 到计数。 */
    private static final Map<String, String[]> COUNTS = Map.ofEntries(
            Map.entry("roleRounds", ROLE),
            Map.entry("roleWins", ROLE),
            Map.entry("heldRoleWins", ROLE),
            Map.entry("rivalKills", ROLE),
            Map.entry("factionRounds", FACTION),
            Map.entry("factionWins", FACTION),
            Map.entry("saves", ITEM),
            Map.entry("consumedItems", ITEM),
            Map.entry("foodWithTraits", TRAIT),
            Map.entry("nonCivilianKills", DEATH_REASON),
            Map.entry("blackoutNonCivilianKills", DEATH_REASON));

    /** Key to a set of ids; the kinds of the key and of the set's members. / 键到 id 集合；键与集合成员各自的种类。 */
    private record SetMap(String[] key, String[] member) {
    }

    private static final Map<String, SetMap> SET_MAPS = Map.of(
            "killedRoles", new SetMap(NOT_AN_ID, ROLE),
            "streakKilledRoles", new SetMap(NOT_AN_ID, ROLE),
            "rivalKillTraits", new SetMap(ROLE, TRAIT),
            "rolesTaken", new SetMap(ROLE, ROLE_CHANGE_CAUSE));

    /** A plain list of ids. / 单纯的 id 列表。 */
    private static final Map<String, String[]> LISTS = Map.of("deathReasons", DEATH_REASON);

    private LegacyAchievementSave() {
    }

    /** The {@code stats} fields this class converts. / 本类会转换的 {@code stats} 字段。 */
    public static Set<String> convertedFields() {
        Set<String> fields = new LinkedHashSet<>(COUNTS.keySet());
        fields.addAll(SET_MAPS.keySet());
        fields.addAll(LISTS.keySet());
        return Set.copyOf(fields);
    }

    /**
     * {@code stats} with every old id folded into its current id. The argument is never modified: it is returned
     * itself when it holds no old id (always the case for a save written by the fused mod), otherwise a copy.
     * 把每个旧 id 并入其当前 id 后的 {@code stats}。绝不修改参数：其中没有旧 id 时（融合后写出的存档总是如此）原样返回，
     * 否则返回副本。
     */
    public static JsonObject withCurrentIds(JsonObject stats) {
        if (stats == null) {
            return null;
        }
        JsonObject result = stats;
        for (String field : List.copyOf(stats.keySet())) {
            JsonElement value = stats.get(field);
            JsonElement converted = value;
            if (COUNTS.containsKey(field)) {
                converted = counts(value, COUNTS.get(field));
            } else if (SET_MAPS.containsKey(field)) {
                converted = setMap(value, SET_MAPS.get(field));
            } else if (LISTS.containsKey(field)) {
                converted = ids(value, LISTS.get(field));
            }
            if (converted != value) {
                if (result == stats) {
                    result = stats.deepCopy();
                }
                result.add(field, converted);
            }
        }
        return result;
    }

    /** The same element when no key is an old id. / 没有旧 id 的键时返回同一个元素。 */
    private static JsonElement counts(JsonElement element, String[] kinds) {
        if (!(element instanceof JsonObject map) || !hasOldKey(map, kinds)) {
            return element;
        }
        JsonObject merged = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
            String id = LegacyIds.canonical(entry.getKey(), kinds);
            JsonElement earlier = merged.get(id);
            // A count that meets no other is carried over as it stands. 没有遇到另一个计数时原样带过去。
            merged.add(id, earlier == null ? entry.getValue() : new JsonPrimitive(sum(earlier, entry.getValue())));
        }
        return merged;
    }

    private static JsonElement setMap(JsonElement element, SetMap kinds) {
        if (!(element instanceof JsonObject map)) {
            return element;
        }
        boolean changed = hasOldKey(map, kinds.key());
        JsonObject merged = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
            String id = LegacyIds.canonical(entry.getKey(), kinds.key());
            JsonElement members = ids(entry.getValue(), kinds.member());
            changed |= members != entry.getValue();
            JsonElement earlier = merged.get(id);
            merged.add(id, earlier == null ? members : union(earlier, members));
        }
        return changed ? merged : element;
    }

    /** The same element when no member is an old id. / 没有旧 id 的成员时返回同一个元素。 */
    private static JsonElement ids(JsonElement element, String[] kinds) {
        if (!(element instanceof JsonArray array)) {
            return element;
        }
        boolean changed = false;
        JsonArray converted = new JsonArray();
        Set<String> seen = new LinkedHashSet<>();
        for (JsonElement member : array) {
            if (!isString(member)) {
                converted.add(member);
                continue;
            }
            String id = LegacyIds.canonical(member.getAsString(), kinds);
            changed |= !id.equals(member.getAsString());
            if (seen.add(id)) {
                converted.add(id);
            }
        }
        return changed ? converted : element;
    }

    /** Both sets' members; the reader skips a value that is not an array, so the array wins. / 两个集合的全部成员。 */
    private static JsonElement union(JsonElement first, JsonElement second) {
        if (!(first instanceof JsonArray firstArray)) {
            return second;
        }
        if (!(second instanceof JsonArray secondArray)) {
            return first;
        }
        JsonArray both = new JsonArray();
        Set<String> seen = new LinkedHashSet<>();
        for (JsonArray array : List.of(firstArray, secondArray)) {
            for (JsonElement member : array) {
                if (!isString(member) || seen.add(member.getAsString())) {
                    both.add(member);
                }
            }
        }
        return both;
    }

    private static boolean hasOldKey(JsonObject map, String[] kinds) {
        for (String key : map.keySet()) {
            if (!LegacyIds.canonical(key, kinds).equals(key)) {
                return true;
            }
        }
        return false;
    }

    private static int sum(JsonElement first, JsonElement second) {
        return (int) Math.min(Integer.MAX_VALUE, (long) count(first) + count(second));
    }

    /** As {@code LifetimeStats} reads a count: a number, never negative; anything else is zero. / 与读取方相同。 */
    private static int count(JsonElement element) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            return 0;
        }
        return Math.max(0, element.getAsInt());
    }

    private static boolean isString(JsonElement element) {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }
}
