package dev.caecorthus.sparkdlc.achievement;

import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.doctor4t.wathe.api.Faction;
import java.util.Locale;
import java.util.function.Function;

/**
 * Faction ids as {@code SparkFactionApi} names them, and the two sides achievements talk about: "好人" is the civilian
 * faction; "非好人" is every other real faction (killers, neutrals, witches, …). Achievements judge a player by their
 * <em>effective</em> faction, so a Conscience or Impostor flip counts.
 * {@code SparkFactionApi} 使用的阵营 id，以及成就里说的两方：“好人”即平民阵营；“非好人”是其他所有真实阵营（杀手、中立、魔女……）。
 * 成就按<em>实际</em>阵营判断玩家，良知、内鬼等词条造成的阵营翻转同样算数。
 */
public final class Factions {
    public static final String CIVILIAN = "wathe:civilian";
    public static final String KILLER = "wathe:killer";
    public static final String NEUTRAL = "wathe:neutral";
    /** No faction: no role, or a role outside every faction. 无阵营：没有角色，或角色不属于任何阵营。 */
    public static final String NONE = "wathe:none";
    public static final String WITCH = "sparkdlc:witch";
    public static final String MURDEROUS_WITCH = "sparkdlc:murderous_witch";
    /**
     * Team Jiahao (嘉豪阵营): the Insider and Noelle's Roles' Corrupt Cop, a native Wathe faction ({@code JIAHAO})
     * since 2026-11-04; before that both read as {@link #NEUTRAL}.
     * 嘉豪阵营：内应与 Noelle's Roles 的黑警，自 2026-11-04 起是 Wathe 原生阵营（{@code JIAHAO}）；此前两者都读作 {@link #NEUTRAL}。
     */
    public static final String JIAHAO = "sparkdlc:jiahao";

    private Factions() {
    }

    /** "好人": the civilian faction. “好人”：平民阵营。 */
    public static boolean isCivilian(String factionId) {
        return CIVILIAN.equals(factionId);
    }

    /**
     * "非好人": any known faction other than civilian; unknown (null) and {@link #NONE} are neither side.
     * “非好人”：平民以外的任何已知阵营；未知（null）和 {@link #NONE} 不属于任何一方。
     */
    public static boolean isNonCivilian(String factionId) {
        return factionId != null && !factionId.isBlank() && !CIVILIAN.equals(factionId) && !NONE.equals(factionId);
    }

    /**
     * The side a faction stood on before Team Jiahao became its own Wathe faction: {@link #JIAHAO} folds back into
     * {@link #NEUTRAL}, every other id (null too) is itself. Only for rules whose meaning was fixed while the Insider
     * and the Corrupt Cop read as neutral, so who counts as an "opponent" there does not change.
     * 嘉豪阵营独立之前的阵营归属：{@link #JIAHAO} 折回 {@link #NEUTRAL}，其他 id（包括 null）不变。只用于含义在内应与黑警
     * 仍读作中立时就已确定的规则，使这些规则里“对手”的范围保持不变。
     */
    public static String legacySide(String factionId) {
        return JIAHAO.equals(factionId) ? NEUTRAL : factionId;
    }

    /**
     * The id for a Wathe {@code Faction} constant name as written in {@code role_assigned} ({@code CIVILIAN} →
     * {@code wathe:civilian}), or null. The faction core appends native custom constants ({@code WITCH},
     * {@code JIAHAO}) and names their ids through {@code factionIdOf(Faction.valueOf(name))}; when it has no id for
     * the name (a constant this side lacks) the name keeps the {@code wathe:} form.
     * {@code role_assigned} 中 Wathe {@code Faction} 常量名对应的 id（{@code CIVILIAN} → {@code wathe:civilian}），没有则为
     * null。阵营核心会追加原生自定义常量（{@code WITCH}、{@code JIAHAO}），并通过
     * {@code factionIdOf(Faction.valueOf(name))} 给出其 id；API 对该名称没有 id（本端没有这个常量）时沿用
     * {@code wathe:} 形式。
     */
    public static String fromWatheName(String name) {
        return fromWatheName(name, Factions::sparkFactionId);
    }

    /**
     * {@link #fromWatheName(String)} with the {@code SparkFactionApi} lookup given: it returns the id, or null (or
     * blank) to fall back for this name only. The real lookup is a direct call, so a lookup that throws is not
     * swallowed here (it was while the lookup was reflective).
     * 指定 {@code SparkFactionApi} 查询的 {@link #fromWatheName(String)}：查询返回 id；返回 null（或空白）时只对这一个名称
     * 回退。真正的查询是直接调用，因此这里不吞掉查询抛出的异常（原先查询走反射时会吞掉）。
     */
    static String fromWatheName(String name, Function<String, String> sparkFactionLookup) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String constant = name.trim();
        String id = sparkFactionLookup.apply(constant);
        if (id != null && !id.isBlank()) {
            return id;
        }
        return "wathe:" + constant.toLowerCase(Locale.ROOT);
    }

    /**
     * {@code SparkFactionApi.factionIdOf(Faction)} for a constant name, or null when this side's {@code Faction}
     * has no such constant or the API has no id for it. A direct call (it used to be a reflective lookup that failed
     * closed while the faction core was a separate mod).
     * 按常量名调用 {@code SparkFactionApi.factionIdOf(Faction)}；本端的 {@code Faction} 没有该常量，或 API 没有对应 id 时
     * 返回 null。这里是直接调用（阵营核心还是独立模组时是反射查找，失败关闭）。
     */
    private static String sparkFactionId(String constant) {
        Faction faction;
        try {
            faction = Faction.valueOf(constant);
        } catch (IllegalArgumentException unknown) {
            return null;
        }
        return SparkFactionApi.factionIdOf(faction).map(Object::toString).orElse(null);
    }
}
