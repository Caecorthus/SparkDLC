package dev.caecorthus.sparkdlc.role.civilian.saint;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Stores, by player UUID, the Hellfire each killer of the Saint is burning with (called Karma in code): one entry per
 * burning player, holding the ticks left; the entry is dropped when they run out. Round-scoped; Grand Witch assignment
 * explicitly removes the entry. Until the 2026-11-28 rebalance an entry was a round-long mark that later kills
 * re-triggered; a mark without time read from older data is ignored.
 * 按玩家 UUID 保存击杀圣徒者正在承受的业火（代码中称为业障）：每名燃烧中的玩家一个条目，记录剩余刻数，耗尽即移除。
 * 仅限本局；成为大魔女时会显式移除对应条目。2026-11-28 重做之前，条目是整局有效、会被后续击杀再次触发的标记；
 * 从旧数据读到的无剩余时间的标记会被忽略。
 */
public final class SaintKarmaState {
    private final LinkedHashMap<UUID, Integer> remainingTicks = new LinkedHashMap<>();
    /**
     * Items an admin cleared during the running Karma ({@code /sparkdlc:clearCooldown}); transient, never saved.
     * Dropped when the Karma ends, is inflicted again (a new penalty covers every item), or is removed.
     * 业障进行中被管理员清除冷却的物品（{@code /sparkdlc:clearCooldown}）；仅在内存中，从不保存。业障结束、再次施加
     * （新的惩罚覆盖所有物品）或被移除时丢弃。
     */
    private final Map<UUID, Set<Identifier>> adminClearedItems = new HashMap<>();

    public boolean isMarked(UUID playerUuid) {
        return remainingTicks.containsKey(playerUuid);
    }

    public int remainingTicks(UUID playerUuid) {
        return remainingTicks.getOrDefault(playerUuid, 0);
    }

    /**
     * Starts the lock for {@code durationTicks}, or keeps a longer one already running; returns the ticks now left
     * (0 when nothing runs, i.e. a non-positive duration on a player without a lock).
     * 开始持续 {@code durationTicks} 的锁，已有更长的锁时保留更长的；返回此刻的剩余刻数（没有任何锁在运行时为 0，
     * 即对未被锁定的玩家传入非正时长）。
     */
    public int inflict(UUID playerUuid, int durationTicks) {
        int merged = SaintRules.mergeCooldownTicks(remainingTicks(playerUuid), durationTicks);
        if (merged <= 0) {
            return 0;
        }
        remainingTicks.put(playerUuid, merged);
        adminClearedItems.remove(playerUuid);
        return merged;
    }

    /** Only while Karma is running; returns whether the item was newly exempted. / 仅在业障进行中；返回是否新豁免。 */
    public boolean exemptAdminCleared(UUID playerUuid, Identifier itemId) {
        if (remainingTicks(playerUuid) <= 0) {
            return false;
        }
        return adminClearedItems.computeIfAbsent(playerUuid, ignored -> new HashSet<>()).add(itemId);
    }

    public boolean isAdminCleared(UUID playerUuid, Identifier itemId) {
        Set<Identifier> items = adminClearedItems.get(playerUuid);
        return items != null && items.contains(itemId);
    }

    public boolean unmark(UUID playerUuid) {
        adminClearedItems.remove(playerUuid);
        return remainingTicks.remove(playerUuid) != null;
    }

    public void tick() {
        if (remainingTicks.isEmpty()) {
            return;
        }
        remainingTicks.replaceAll((playerUuid, ticks) -> ticks - 1);
        // A lock that ran out leaves no entry behind (and with it no admin exemption).
        // 耗尽的锁不留下条目（管理员豁免也随之丢弃）。
        remainingTicks.values().removeIf(ticks -> ticks <= 0);
        // Skips the capturing lambda and iterator when there is nothing to drop. / 无可丢弃时跳过捕获 lambda 与迭代器。
        if (!adminClearedItems.isEmpty()) {
            adminClearedItems.keySet().removeIf(playerUuid -> remainingTicks(playerUuid) <= 0);
        }
    }

    public void restore(UUID playerUuid, int ticks) {
        if (ticks > 0) {
            remainingTicks.put(playerUuid, ticks);
        }
    }

    public List<Entry> entries() {
        List<Entry> entries = new ArrayList<>(remainingTicks.size());
        for (Map.Entry<UUID, Integer> entry : remainingTicks.entrySet()) {
            entries.add(new Entry(entry.getKey(), entry.getValue()));
        }
        return List.copyOf(entries);
    }

    public void clear() {
        remainingTicks.clear();
        adminClearedItems.clear();
    }

    public record Entry(UUID playerUuid, int remainingTicks) {
    }
}
