package dev.caecorthus.sparkdlc.compat.noellesroles;

import dev.caecorthus.sparkdlc.SparkWitch;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Exact read-boundary aliases for the legacy ids of the six Wraith-family roles: {@code sparktraits:<role>} (when
 * SparkTraits hosted them) and {@code sparkwitch:<role>} (before the single {@code sparkdlc} namespace) both lead to
 * the role's current id.
 * 冤魂系六个角色旧 id 的精确读取别名：{@code sparktraits:<role>}（由 SparkTraits 托管时）与 {@code sparkwitch:<role>}
 * （统一为 {@code sparkdlc} 命名空间之前）都指向角色当前的 id。
 */
public final class WraithLegacyRoleIds {
    /**
     * The namespaces these six roles were saved under before their current id, most recent first: when a save holds
     * a role under several of them, the most recent one wins. Literals on purpose: they are the names in old saves.
     * 这六个角色在使用当前 id 之前保存时用过的命名空间，按从新到旧排列：存档里同一角色出现在多个旧命名空间下时，以最新的为准。
     * 有意写死：它们是旧存档里的名字。
     */
    private static final List<String> LEGACY_NAMESPACES = List.of("sparkwitch", "sparktraits");
    private static final List<String> ROLE_PATHS =
            List.of("wraith", "wind_spirit", "guardian_angel", "vendetta", "saboteur", "curser");
    /** Legacy id to the role's current id, in {@link #LEGACY_NAMESPACES} order. / 旧 id 到当前角色 id，按上述顺序。 */
    private static final Map<Identifier, Identifier> LEGACY_TO_CANONICAL = aliases();
    private static final Map<String, String> LEGACY_STRINGS = stringAliases();

    private WraithLegacyRoleIds() {
    }

    /** Copies exact old role lists only when absent and canonicalizes exact disabled-role strings. / 仅补齐缺失的精确旧角色列表，并规范化精确禁用角色字符串。 */
    public static void migrateGameWorldNbt(NbtCompound nbt) {
        for (Map.Entry<Identifier, Identifier> alias : LEGACY_TO_CANONICAL.entrySet()) {
            String legacy = alias.getKey().toString();
            String canonical = alias.getValue().toString();
            if (!nbt.contains(canonical) && nbt.contains(legacy)) {
                NbtElement legacyValue = nbt.get(legacy);
                if (legacyValue != null) {
                    nbt.put(canonical, legacyValue.copy());
                }
            }
        }
        canonicalizeStringList(nbt, "DisabledRoles");
    }

    /** Canonicalizes only exact saved RoleId values inside Wathe history. / 仅规范化 Wathe 历史中的精确 RoleId 值。 */
    public static void migrateRoleHistoryNbt(NbtCompound nbt) {
        NbtList players = nbt.getList("History", NbtElement.COMPOUND_TYPE);
        for (int playerIndex = 0; playerIndex < players.size(); playerIndex++) {
            NbtList entries = players.getCompound(playerIndex).getList("Entries", NbtElement.COMPOUND_TYPE);
            for (int entryIndex = 0; entryIndex < entries.size(); entryIndex++) {
                NbtCompound entry = entries.getCompound(entryIndex);
                String roleId = entry.getString("RoleId");
                String canonical = LEGACY_STRINGS.get(roleId);
                if (canonical != null) {
                    entry.putString("RoleId", canonical);
                }
            }
        }
    }

    private static void canonicalizeStringList(NbtCompound nbt, String key) {
        if (!nbt.contains(key, NbtElement.LIST_TYPE)) {
            return;
        }
        NbtList original = nbt.getList(key, NbtElement.STRING_TYPE);
        NbtList canonicalized = new NbtList();
        for (int index = 0; index < original.size(); index++) {
            String stored = original.getString(index);
            canonicalized.add(NbtString.of(LEGACY_STRINGS.getOrDefault(stored, stored)));
        }
        nbt.put(key, canonicalized);
    }

    private static Map<Identifier, Identifier> aliases() {
        Map<Identifier, Identifier> aliases = new LinkedHashMap<>();
        for (String namespace : LEGACY_NAMESPACES) {
            for (String path : ROLE_PATHS) {
                // The target is the role's live id, whatever namespace the mod uses now. / 目标是角色当前的 id。
                aliases.put(Identifier.of(namespace, path), SparkWitch.id(path));
            }
        }
        return Collections.unmodifiableMap(aliases);
    }

    private static Map<String, String> stringAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        LEGACY_TO_CANONICAL.forEach((legacy, canonical) -> aliases.put(legacy.toString(), canonical.toString()));
        return Collections.unmodifiableMap(aliases);
    }
}
