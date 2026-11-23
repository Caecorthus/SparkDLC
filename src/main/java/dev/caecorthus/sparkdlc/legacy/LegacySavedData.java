package dev.caecorthus.sparkdlc.legacy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

/**
 * Migration of saved data that is not a vanilla registry id: everything a world or a player file written by the five
 * separate mods holds under an old name. Pure NBT rewriting on load, driven by {@link LegacyIds}; the next save writes
 * the current names, so each file is migrated once and the code is idempotent on migrated data.
 *
 * <p>Three things are rewritten:</p>
 * <ol>
 * <li><b>Cardinal Components keys</b> ({@link #migrateComponentMap}). A container drops the data stored under a key
 * it does not know, so every old key is moved to its current key first. The current key always wins when it is
 * already saved; among several old keys of one component the table kind decides ({@code component} before
 * {@code component_fallback}).</li>
 * <li><b>Ids inside component values</b> ({@link #VALUE_RULES}): only at the listed NBT paths and only as the listed
 * kind of id. There is no walk over "every string": a chat line or a note that happens to read
 * {@code sparkwitch:prophet} is text, and {@code sparktraits:wraith} is a retired trait in a trait list but an old
 * role id in a role position.</li>
 * <li><b>Attribute modifier ids</b> in saved entity attributes ({@link #migrateAttributes}).</li>
 * </ol>
 *
 * 非原版注册表 id 的存档数据迁移：五个独立模组时期写下的世界或玩家文件中，凡是保存在旧名字下的内容。读取时纯粹改写 NBT，
 * 由 {@link LegacyIds} 驱动；下一次保存写出当前名字，因此每个文件只迁移一次，对已迁移的数据重复执行不产生变化。
 *
 * <p>改写三类内容：（一）Cardinal Components 的键：容器会丢弃未知键下的数据，所以先把每个旧键移到当前键；当前键已有数据时
 * 始终以它为准，同一组件的多个旧键之间由对照表的种类决定先后（{@code component} 先于 {@code component_fallback}）。
 * （二）组件值内部的 id（{@link #VALUE_RULES}）：仅限列出的 NBT 路径、仅按列出的 id 种类解释，绝不遍历“所有字符串”——
 * 恰好写成 {@code sparkwitch:prophet} 的聊天内容或笔记是文本，而 {@code sparktraits:wraith} 在天赋列表里是已退役的天赋，
 * 在职业位置上才是旧职业 id。（三）实体属性存档中的属性修饰符 id。</p>
 */
public final class LegacySavedData {
    /** The compound every Cardinal Components container saves into. / Cardinal Components 容器保存所用的复合标签。 */
    public static final String COMPONENTS_KEY = "cardinal_components";

    /**
     * Table kinds that hold old component keys, in precedence order: a {@code component_fallback} row is an even
     * older key of the same component and is used only when neither the current key nor the {@code component} row's
     * key is saved (the two Wraith components were {@code sparktraits:} before they were {@code sparkwitch:}).
     * 保存旧组件键的对照表种类，按优先级排列：{@code component_fallback} 行是同一组件更早的键，只有当前键和
     * {@code component} 行的键都没有数据时才使用（两个冤魂组件在 {@code sparkwitch:} 之前曾是 {@code sparktraits:}）。
     */
    static final String[] COMPONENT_KEY_KINDS = {"component", "component_fallback"};

    /**
     * A role id as old saves hold it: the {@code sparkwitch:} id every role had, then the {@code sparktraits:} id
     * the six Wraith-family roles had before that. The order matters only where ids are compound keys (the newer
     * namespace wins). Never used for a trait list.
     * 旧存档中的职业 id：先是所有职业都用过的 {@code sparkwitch:} id，再是冤魂系六个职业更早的 {@code sparktraits:} id。
     * 顺序只在 id 作为复合标签的键时有意义（较新的命名空间优先）。绝不用于天赋列表。
     */
    private static final String[] ROLE = {"role", "legacy_role_alias"};
    /**
     * A trait id. {@code sparktraits:wraith}, {@code arrogant_asf} and {@code chameleon} are retired traits: the
     * table gives them no new id, so they stay as they are and the trait codec keeps dropping them.
     * 天赋 id。{@code sparktraits:wraith}、{@code arrogant_asf}、{@code chameleon} 是已退役的天赋：对照表不给它们新 id，
     * 因此保持原样，天赋编解码器会继续丢弃它们。
     */
    private static final String[] TRAIT = {"trait"};
    private static final String[] SKILL = {"skill"};
    private static final String[] FACTION = {"faction"};

    /**
     * Where a saved component value holds an id, by CURRENT component key (keys are migrated first). Path steps:
     * {@code Name} a child compound, or the string itself at the end; {@code Name[]} a list (of strings at the end,
     * of compounds otherwise); {@code Name{}} the KEYS of a child compound at the end, each compound value otherwise;
     * a bare {@code {}} the keys of the component's own compound.
     *
     * <p>Settings an admin made, which must survive the update: disabled and pending traits, disabled and forced
     * skills, the forced-role locks, and in Wathe's own components the disabled roles (plus its role history and
     * last round-end rows). The remaining rows are round state that a player file or the world can still carry
     * after a restart; they are listed so that such state reads exactly as it did before the rename.</p>
     *
     * <p>Left alone on purpose: the Hunter's {@code ActivePoisonSource} (an id of no single kind, gone when the
     * poison ends), the Black Raven disguise state (purged when its match id is not the live match), Wathe's shop
     * state keyed by shop-entry id (cleared at every round start), ids in entity NBT outside Cardinal Components (the
     * death role on a body, the owner role of a capture device: entities of one round; Wathe discards the bodies when
     * it finalises the round at server start), and all free text.</p>
     *
     * 存档中的组件值在何处保存 id，按“当前”组件键列出（键先迁移）。路径写法：{@code Name} 为子复合标签，位于末尾时为字符串
     * 本身；{@code Name[]} 为列表（末尾时为字符串列表，否则为复合标签列表）；{@code Name{}} 位于末尾时指子复合标签的“键”，
     * 否则指其中每个复合标签值；单独的 {@code {}} 指组件自身复合标签的键。
     *
     * <p>管理员做出的、更新后必须保留的设置：禁用与待发放的天赋、禁用与强制的技能、强制职业锁定，以及 Wathe 自身组件中的
     * 禁用职业（外加职业历史与上一局结算行）。其余各行是重启后玩家文件或世界里仍可能带着的对局状态；列出它们是为了让这些
     * 状态读出来与改名前完全一致。</p>
     *
     * <p>有意不处理：猎人的 {@code ActivePoisonSource}（不属于单一种类的 id，中毒结束即消失）、黑鸦伪装状态（对局 id 与
     * 当前对局不符时会被清除）、Wathe 以商店条目 id 为键的商店状态（每局开始时清空）、Cardinal Components 之外实体 NBT 中的 id
     * （尸体上的死亡职业、捕获装置的归属职业：都是单局实体；Wathe 在服务器启动结算对局时会清除尸体），以及所有自由文本。</p>
     */
    private static final Map<String, List<ValueRule>> VALUE_RULES = valueRules(
            // Settings. / 设置。
            new ValueRule("sparkdlc:trait_world", "DisabledTraits[]", TRAIT),
            new ValueRule("sparkdlc:traits", "PendingTraits[]", TRAIT),
            new ValueRule("sparkdlc:witch_world", "DisabledSkills[]", SKILL),
            new ValueRule("sparkdlc:witch_world", "ForcedWraithPromotions[].Role", ROLE),
            new ValueRule("sparkdlc:witch_world", "ForcedAccompliceRoles[].Role", ROLE),
            new ValueRule("sparkdlc:witch_world", "PendingForcedAccompliceRoles[].Role", ROLE),
            new ValueRule("sparkdlc:player", "ForcedSkill", SKILL),
            // Wathe's own components. Role lists in wathe:game are keyed by role id. / Wathe 自身的组件；wathe:game 中的
            // 职业名单以职业 id 为键。
            new ValueRule("wathe:game", "DisabledRoles[]", ROLE),
            new ValueRule("wathe:game", "{}", ROLE),
            new ValueRule("wathe:role_history", "History[].Entries[].RoleId", ROLE),
            new ValueRule("wathe:roundend", "players[].role", ROLE),
            // Last round's result and round state. / 上一局结果与对局状态。
            new ValueRule("sparkdlc:round_end", "WinningFaction", FACTION),
            new ValueRule("sparkdlc:traits", "ActiveTraits[]", TRAIT),
            new ValueRule("sparkdlc:traits", "RevealedTraits[]", TRAIT),
            new ValueRule("sparkdlc:traits", "SerialKillerMurdererRole", ROLE),
            new ValueRule("sparkdlc:player", "ActiveSkill", SKILL),
            new ValueRule("sparkdlc:accomplice_variant_round", "UsedVariants[]", ROLE),
            new ValueRule("sparkdlc:coroner_body_snapshot", "RoleId", ROLE),
            new ValueRule("sparkdlc:coroner_player", "ActiveDisguiseRole", ROLE),
            new ValueRule("sparkdlc:coroner_player", "UnlockedBodies[].RoleId", ROLE),
            new ValueRule("sparkdlc:detective_case_player", "DetectiveCases[].KillerGuess", ROLE));

    private static final String ATTRIBUTE_MODIFIERS_KEY = "modifiers";
    private static final String ATTRIBUTE_MODIFIER_ID_KEY = "id";
    private static final String[] ATTRIBUTE_MODIFIER = {"attribute_modifier"};

    private LegacySavedData() {
    }

    /**
     * Migrates the component map inside the NBT a Cardinal Components container is about to read (an entity's own
     * NBT, the world component file, the scoreboard file, a team). Nothing happens without a component map.
     * 迁移 Cardinal Components 容器即将读取的 NBT（实体自身的 NBT、世界组件文件、计分板文件、队伍）中的组件表。
     * 没有组件表时什么都不做。
     */
    public static void migrateContainerTag(NbtCompound containerTag) {
        if (containerTag != null && containerTag.contains(COMPONENTS_KEY, NbtElement.COMPOUND_TYPE)) {
            migrateComponentMap(containerTag.getCompound(COMPONENTS_KEY));
        }
    }

    /**
     * Migrates one component map in place: old component keys first, then the ids inside the values.
     * 就地迁移一张组件表：先迁移旧组件键，再迁移值内部的 id。
     */
    public static void migrateComponentMap(NbtCompound components) {
        moveKeys(components, COMPONENT_KEY_KINDS);
        // Few rules, many components per player: walk the rules, not the keys. / 规则少而每个玩家的组件多：按规则查找。
        for (Map.Entry<String, List<ValueRule>> rules : VALUE_RULES.entrySet()) {
            if (components.contains(rules.getKey(), NbtElement.COMPOUND_TYPE)) {
                NbtCompound component = components.getCompound(rules.getKey());
                for (ValueRule rule : rules.getValue()) {
                    rewrite(component, rule.path(), 0, rule.kinds());
                }
            }
        }
    }

    /**
     * Migrates the ids of attribute modifiers in a saved {@code attributes} list (entity NBT). Only a persistent
     * modifier is ever saved; after the rename the mod removes and replaces it by its new id only, so an old id left
     * in a player file would slow that player for ever. An old modifier becomes the new one, or is dropped when the
     * new one is saved next to it.
     * 迁移实体 NBT 中 {@code attributes} 列表里属性修饰符的 id。只有持久修饰符才会被保存；改名后模组只按新 id 移除和替换它，
     * 玩家文件里遗留的旧 id 会让该玩家永远减速。旧修饰符改为新 id；若新 id 的修饰符已同时存在，则丢弃旧的。
     */
    public static void migrateAttributes(NbtList attributes) {
        if (attributes == null) {
            return;
        }
        for (NbtElement attribute : attributes) {
            if (!(attribute instanceof NbtCompound instance)
                    || !instance.contains(ATTRIBUTE_MODIFIERS_KEY, NbtElement.LIST_TYPE)) {
                continue;
            }
            NbtList modifiers = instance.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
            for (int index = modifiers.size() - 1; index >= 0; index--) {
                NbtCompound modifier = modifiers.getCompound(index);
                String id = modifier.getString(ATTRIBUTE_MODIFIER_ID_KEY);
                String current = LegacyIds.canonical(id, ATTRIBUTE_MODIFIER);
                if (current.equals(id)) {
                    continue;
                }
                if (hasModifier(modifiers, current)) {
                    modifiers.remove(index);
                } else {
                    modifier.putString(ATTRIBUTE_MODIFIER_ID_KEY, current);
                }
            }
        }
    }

    /** Every current component key that has a value rule. / 有值规则的所有当前组件键。 */
    public static List<String> componentsWithValueRules() {
        return List.copyOf(VALUE_RULES.keySet());
    }

    private static boolean hasModifier(NbtList modifiers, String id) {
        for (int index = 0; index < modifiers.size(); index++) {
            if (id.equals(modifiers.getCompound(index).getString(ATTRIBUTE_MODIFIER_ID_KEY))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Moves every key that is an old id of one of the kinds to its current id, kind by kind. A key that is already
     * present under the current id is never overwritten: the old entry is dropped (the reader would ignore it and
     * the next save would not write it).
     * 把属于给定种类旧 id 的每个键移到当前 id，逐个种类处理。当前 id 下已有的键绝不覆盖：旧条目被丢弃（读取方本来就会忽略它，
     * 下一次保存也不会再写出）。
     */
    private static void moveKeys(NbtCompound compound, String[] kinds) {
        for (String kind : kinds) {
            Map<String, String> renamed = LegacyIds.renamed(kind);
            List<String> oldKeys = null;
            for (String key : compound.getKeys()) {
                if (renamed.containsKey(key)) {
                    if (oldKeys == null) {
                        oldKeys = new ArrayList<>();
                    }
                    oldKeys.add(key);
                }
            }
            if (oldKeys == null) {
                continue;
            }
            for (String oldKey : oldKeys) {
                NbtElement value = compound.get(oldKey);
                compound.remove(oldKey);
                String currentKey = renamed.get(oldKey);
                if (value != null && !compound.contains(currentKey)) {
                    compound.put(currentKey, value);
                }
            }
        }
    }

    private static void rewrite(NbtCompound at, String[] path, int index, String[] kinds) {
        String step = path[index];
        boolean last = index == path.length - 1;
        if (step.endsWith("[]")) {
            String key = step.substring(0, step.length() - 2);
            if (!(at.get(key) instanceof NbtList list)) {
                return;
            }
            for (int element = 0; element < list.size(); element++) {
                NbtElement value = list.get(element);
                if (!last) {
                    if (value instanceof NbtCompound child) {
                        rewrite(child, path, index + 1, kinds);
                    }
                } else if (value instanceof NbtString string) {
                    String current = LegacyIds.canonical(string.asString(), kinds);
                    if (!current.equals(string.asString())) {
                        list.set(element, NbtString.of(current));
                    }
                }
            }
        } else if (step.endsWith("{}")) {
            String key = step.substring(0, step.length() - 2);
            NbtCompound keyed = key.isEmpty() ? at : at.get(key) instanceof NbtCompound child ? child : null;
            if (keyed == null) {
                return;
            }
            if (last) {
                moveKeys(keyed, kinds);
                return;
            }
            for (String childKey : keyed.getKeys()) {
                if (keyed.get(childKey) instanceof NbtCompound child) {
                    rewrite(child, path, index + 1, kinds);
                }
            }
        } else if (!last) {
            if (at.get(step) instanceof NbtCompound child) {
                rewrite(child, path, index + 1, kinds);
            }
        } else if (at.get(step) instanceof NbtString string) {
            String current = LegacyIds.canonical(string.asString(), kinds);
            if (!current.equals(string.asString())) {
                at.putString(step, current);
            }
        }
    }

    private static Map<String, List<ValueRule>> valueRules(ValueRule... rules) {
        Map<String, List<ValueRule>> byComponent = new LinkedHashMap<>();
        for (ValueRule rule : rules) {
            byComponent.computeIfAbsent(rule.component(), key -> new ArrayList<>()).add(rule);
        }
        byComponent.replaceAll((component, list) -> List.copyOf(list));
        return byComponent;
    }

    private record ValueRule(String component, String[] path, String[] kinds) {
        ValueRule(String component, String path, String[] kinds) {
            this(component, path.split("\\."), kinds);
        }
    }
}
