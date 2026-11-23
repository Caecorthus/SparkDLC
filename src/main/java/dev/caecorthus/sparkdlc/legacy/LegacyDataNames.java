package dev.caecorthus.sparkdlc.legacy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

/**
 * The private names the mod stores inside worlds, players and stacks that are not ids: NBT keys of block entities,
 * entities and a NoellesRoles component, item {@code minecraft:custom_data} keys, a match-record field, an entity
 * command tag and two scoreboard teams. Until 2026-11-20 they still carried the name of the former mod that wrote
 * them ({@code sparkwitch:stored_coins}, {@code SparkStrengthCapsule}, {@code sparktraitsConscienceGas},
 * {@code sparkwitch_duel_knight}, ...); the owner then decided "就数据可以改" (the saved-data names may change). The
 * feature code knows only the current name; the old one is the {@code private_data_name} row of {@link LegacyIds}
 * (old name, current name) and is reached only through this class: read the current name first, then the old one;
 * write only the current name.
 *
 * <p>Item custom data needs no reader of its own: every {@code NbtComponent} renames its old top-level keys when it is
 * built ({@link #withCurrentKeys}, {@code mixin.legacy.NbtComponentLegacyDataNamesMixin}), so a stack loaded from a
 * world, a player file, a structure, a packet or a command carries the current names from then on.</p>
 *
 * 本模组存放在世界、玩家与物品堆里的、不属于 id 的私有名字：方块实体、实体与一个 NoellesRoles 组件的 NBT 键，物品
 * {@code minecraft:custom_data} 的键，一个对局记录字段，一个实体命令标签和两个计分板队伍。直到 2026-11-20 它们仍带着写入它们的
 * 旧模组名（{@code sparkwitch:stored_coins}、{@code SparkStrengthCapsule}、{@code sparktraitsConscienceGas}、
 * {@code sparkwitch_duel_knight} 等）；随后所有者决定“就数据可以改”。功能代码只认识当前名字；旧名字是 {@link LegacyIds} 的
 * {@code private_data_name} 行（旧名字、当前名字），只能经由本类访问：先读当前名字，再读旧名字；只写当前名字。
 *
 * <p>物品自定义数据不需要单独的读取方：每个 {@code NbtComponent} 构建时都会把旧的顶层键改为当前名字（{@link #withCurrentKeys}、
 * {@code mixin.legacy.NbtComponentLegacyDataNamesMixin}），因此从世界、玩家文件、结构、数据包或命令载入的物品堆此后都带当前名字。</p>
 */
public final class LegacyDataNames {
    /** The table kind this class works from. / 本类使用的对照表种类。 */
    public static final String KIND = "private_data_name";

    private static final Map<String, String> RENAMED = LegacyIds.renamed(KIND);
    private static final Map<String, String> OLD_BY_CURRENT = oldByCurrent(RENAMED);

    private LegacyDataNames() {
    }

    /**
     * The name {@code current} had before 2026-11-20, or null when it has no row.
     * {@code current} 在 2026-11-20 之前的名字；没有对应行时为 null。
     */
    public static @Nullable String oldName(String current) {
        return OLD_BY_CURRENT.get(current);
    }

    /**
     * The key {@code nbt} holds the value of {@code current} under: the current key when present, else its old key
     * when present, else the current key (so a reader that finds nothing behaves exactly as before).
     * {@code nbt} 保存 {@code current} 值所用的键：有当前键时为当前键，否则有旧键时为旧键，都没有时为当前键（读取方什么都找不到时
     * 行为与以前完全相同）。
     */
    public static String keyIn(NbtCompound nbt, String current) {
        if (nbt.contains(current)) {
            return current;
        }
        String old = OLD_BY_CURRENT.get(current);
        return old != null && nbt.contains(old) ? old : current;
    }

    /**
     * Whether {@code names} (command tags, ...) holds {@code current} or its old name.
     * {@code names}（命令标签等）是否含有 {@code current} 或其旧名字。
     */
    public static boolean containsEither(Collection<String> names, String current) {
        if (names.contains(current)) {
            return true;
        }
        String old = OLD_BY_CURRENT.get(current);
        return old != null && names.contains(old);
    }

    /**
     * {@code nbt} with every old top-level key renamed to its current name: {@code nbt} itself when it has none (the
     * common case, nothing is copied), otherwise a copy. A current key that is already present wins and the old entry
     * is dropped, as {@link LegacySavedData} does for component keys.
     * 把每个旧顶层键改为当前名字后的 {@code nbt}：没有旧键时（常见情况）原样返回、不复制，否则返回副本。当前键已存在时以它为准，
     * 旧条目被丢弃，与 {@link LegacySavedData} 处理组件键相同。
     */
    public static NbtCompound withCurrentKeys(NbtCompound nbt) {
        if (nbt.isEmpty()) {
            return nbt;
        }
        NbtCompound renamed = null;
        for (Map.Entry<String, String> row : RENAMED.entrySet()) {
            if (!nbt.contains(row.getKey())) {
                continue;
            }
            if (renamed == null) {
                renamed = nbt.copy();
            }
            NbtElement value = renamed.get(row.getKey());
            renamed.remove(row.getKey());
            if (value != null && !renamed.contains(row.getValue())) {
                renamed.put(row.getValue(), value);
            }
        }
        return renamed == null ? nbt : renamed;
    }

    /**
     * Removes the teams the given current team names had before 2026-11-20 from {@code scoreboard} (with their
     * members), so a scoreboard saved by an older build, or a round that started on one, keeps no member on a team
     * the mod no longer resets.
     * 从 {@code scoreboard} 中移除给定当前队伍名在 2026-11-20 之前的队伍（连同其成员），使旧版本保存的计分板或在旧版本上开始的
     * 对局不会把成员留在本模组不再重置的队伍里。
     */
    public static void removeOldTeams(Scoreboard scoreboard, String... currentNames) {
        for (String current : currentNames) {
            String old = OLD_BY_CURRENT.get(current);
            Team team = old == null ? null : scoreboard.getTeam(old);
            if (team != null) {
                scoreboard.removeTeam(team);
            }
        }
    }

    private static Map<String, String> oldByCurrent(Map<String, String> renamed) {
        Map<String, String> reversed = new LinkedHashMap<>();
        renamed.forEach((old, current) -> {
            String previous = reversed.put(current, old);
            if (previous != null) {
                throw new IllegalStateException(KIND + " " + current + " has two old names: " + previous + ", " + old);
            }
        });
        return Collections.unmodifiableMap(reversed);
    }
}
