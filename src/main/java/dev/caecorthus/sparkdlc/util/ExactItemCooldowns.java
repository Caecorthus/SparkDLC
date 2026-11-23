package dev.caecorthus.sparkdlc.util;

import dev.caecorthus.sparkdlc.mixin.cooldown.ItemCooldownEntryAccessor;
import dev.caecorthus.sparkdlc.mixin.cooldown.ItemCooldownManagerAccessor;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * The one reader and writer of exact vanilla item cooldowns. Duration modifiers hook {@code ItemCooldownManager.set}
 * at HEAD (the Fast Hands trait x0.9, NoellesRoles Stimulation x0.8, ...), so a length written through a plain
 * {@code set} shrinks. Every writer here installs the exact length instead, in one of three modes that differ in what
 * those HEAD modifiers (ours and other mods') see and in which packet the client gets; each caller keeps its mode.
 * 唯一的原版物品冷却精确读写。时长倍率挂在 {@code ItemCooldownManager.set} 的 HEAD（快手词条 ×0.9、NoellesRoles
 * 亢奋 ×0.8 等），经普通 {@code set} 写入的时长会被缩短。这里的写入方都改为装入精确时长，分三种模式，区别在于那些 HEAD
 * 倍率（本模组的与其他模组的）能看到什么、客户端收到哪个数据包；每个调用方保留它原来的模式。
 *
 * <ul>
 * <li>{@link #raiseInPlace} / {@link #keepRaisedInPlace}: monotonic, in place. The server-clock locks of the role
 * kits (the Vulture skateboard's opening lock and ready tick, the M67 and drone round openings, a re-asserted
 * Bodyguard shield cooldown): their clock is the real gate, the vanilla entry only shows it. A per-tick re-sync
 * through {@code set} never reached the lock and re-sent every tick; each {@code CooldownUpdateS2CPacket} restarts the
 * client's sweep, so the slot stayed full until the last ~2 s. Never shortens; sends its own packet only when it
 * rewrote the entry. Cooldowns the vanilla entry itself gates (M67 throws, drone returns, ...) keep using {@code set},
 * so those modifiers still apply there.
 * 单调、原地：职业套件按服务端时钟维护的锁（秃鹫滑板的开局锁与可用时刻、M67 与无人机的开局锁、补回的保镖盾冷却），
 * 真正的判定是该时钟，原版条目只负责显示。经 {@code set} 的逐刻重同步永远达不到锁的时长、每刻重发；每个
 * {@code CooldownUpdateS2CPacket} 都让客户端冷却动画从头开始，栏位一直是满的、直到最后约 2 秒。绝不缩短；只在改写了条目时
 * 发送自己的数据包。由原版条目本身判定的冷却（M67 投掷、无人机归还等）仍使用 {@code set}，这些倍率在那里照常生效。</li>
 * <li>{@link #overwriteInPlace}: unconditional, in place, for {@code ForcedCooldowns} (its caller has already made the
 * value monotonic). Always sends its own exact packet; the modifiers are server-gated, so the client's {@code set}
 * applies the packet duration unchanged.
 * 无条件、原地，供 {@code ForcedCooldowns} 使用（调用方已保证单调）。总是发送自己的精确数据包：这些倍率仅在服务端
 * 生效，客户端的 {@code set} 会原样采用包内时长。</li>
 * <li>{@link #setPastModifiers}: through {@code set}, for {@code SparkTraitsApi.setExactItemCooldownRemaining} and the
 * traits. Every HEAD modifier runs and sees the call; {@code ExactItemCooldownMixin} then restores the exact length at
 * the {@code Entry} construction (keeping the forced-melee floor) and vanilla's own {@code onCooldownUpdate} sends the
 * packet.
 * 经由 {@code set}，供 {@code SparkTraitsApi.setExactItemCooldownRemaining} 与词条使用。每个 HEAD 倍率都会运行并看到这次
 * 调用；随后 {@code ExactItemCooldownMixin} 在构造 {@code Entry} 时恢复精确时长（保留强制近战下限），由原版自身的
 * {@code onCooldownUpdate} 发送数据包。</li>
 * </ul>
 *
 * <p>Why the modes stay apart: the in-place writers call {@code set} only to create a missing entry, so HEAD
 * modifiers, the TAIL hooks (the Depression fake-kill record) and the forced-melee floor see one call at most and the
 * {@code Entry} object keeps its identity ({@code ForcedItemLedger} keys on it); {@link #setPastModifiers} runs all of
 * them on every write and replaces the entry. Making either the writer of all callers would change what those hooks
 * observe.
 * 为何不合成一种：原地写入方只在条目缺失时借 {@code set} 创建，因此 HEAD 倍率、TAIL 钩子（抑郁假死记录）与强制近战下限至多看到
 * 一次调用，{@code Entry} 对象也保持不变（{@code ForcedItemLedger} 以其为键）；{@link #setPastModifiers} 每次写入都会运行它们
 * 并替换条目。让任一种成为所有调用方的写入方，都会改变这些钩子观察到的内容。</p>
 */
public final class ExactItemCooldowns {
    /** {@link #exactWriteTicks} when no exact write is running. / 没有精确写入进行时 {@link #exactWriteTicks} 的返回值。 */
    public static final int NO_EXACT_WRITE = -1;

    private static final ThreadLocal<Write> EXACT_WRITE = new ThreadLocal<>();

    private ExactItemCooldowns() {
    }

    /** Remaining ticks of the vanilla entry, 0 when none. / 原版条目的剩余刻数，无条目时为 0。 */
    public static int remainingTicks(ItemCooldownManager manager, Item item) {
        ItemCooldownManagerAccessor access = (ItemCooldownManagerAccessor) manager;
        Object entry = access.sparkdlc$getEntries().get(item);
        return entry instanceof ItemCooldownEntryAccessor bounds
                ? ExactItemCooldownRules.remaining(bounds.sparkdlc$getEndTick(), access.sparkdlc$getTick())
                : 0;
    }

    /** As above on the player's own manager; 0 for a null player or item. / 同上，读玩家自身的管理器；玩家或物品为空时为 0。 */
    public static int remainingTicks(@Nullable PlayerEntity player, @Nullable Item item) {
        return player == null || item == null ? 0 : remainingTicks(player.getItemCooldownManager(), item);
    }

    /**
     * The live vanilla entry for the item, or {@code null}. Its identity changes on every {@code set} and
     * {@code remove}; {@code ForcedItemLedger} keys on it.
     * 物品当前的原版条目，无则为 {@code null}。每次 {@code set} 与 {@code remove} 都会换成新对象；ForcedItemLedger 以其为键。
     */
    public static @Nullable ItemCooldownEntryAccessor liveEntry(ItemCooldownManager manager, Item item) {
        Object entry = ((ItemCooldownManagerAccessor) manager).sparkdlc$getEntries().get(item);
        return entry instanceof ItemCooldownEntryAccessor bounds ? bounds : null;
    }

    /** The manager's own tick counter (the time base of its entries). / 冷却管理器自身的刻计数（其条目的时间基准）。 */
    public static int tick(ItemCooldownManager manager) {
        return ((ItemCooldownManagerAccessor) manager).sparkdlc$getTick();
    }

    /**
     * Per-tick upkeep of a server-clock lock: raises the entry to exactly {@code lockTicks} when it is short by more
     * than {@link ExactItemCooldownRules#PHASE_TOLERANCE_TICKS}; never shortens. Returns true when it wrote.
     * 服务端时钟锁的逐刻维护：条目比 {@code lockTicks} 短超过容差时，精确提高到 {@code lockTicks}；绝不缩短。写入时返回 true。
     */
    public static boolean keepRaisedInPlace(ServerPlayerEntity player, Item item, int lockTicks) {
        return ExactItemCooldownRules.needsRaise(remainingTicks(player.getItemCooldownManager(), item), lockTicks)
                && raiseInPlace(player, item, lockTicks);
    }

    /**
     * Makes the entry hold exactly {@code ticks} remaining when it held less; never shortens (a longer entry, e.g. a
     * trait's forced floor a {@code set} hook kept, is left alone, and then nothing is sent). Returns false for a
     * non-positive length, or when another mod cancelled {@code set} and no entry exists.
     * 条目剩余不足 {@code ticks} 时精确写为 {@code ticks}；绝不缩短（更长的条目，例如 {@code set} 钩子保留的词条
     * 强制下限，保持不变，此时也不发送）。时长非正，或其他模组取消了 {@code set} 导致无条目时返回 false。
     */
    public static boolean raiseInPlace(ServerPlayerEntity player, Item item, int ticks) {
        if (ticks <= 0) {
            return false;
        }
        ItemCooldownManager manager = player.getItemCooldownManager();
        ItemCooldownEntryAccessor bounds = entryCreatedThroughSet(manager, item, ticks);
        if (bounds == null) {
            return false;
        }
        int tick = tick(manager);
        int end = ExactItemCooldownRules.endTick(tick, ticks);
        if (bounds.sparkdlc$getEndTick() >= end) {
            // Already long enough: set wrote it unmodified (and sent it), or a hook lengthened it.
            // 已足够长：set 以原值写入（并已发送），或被钩子延长。
            return true;
        }
        rewrite(player, item, bounds, tick, end);
        return true;
    }

    /**
     * Installs exactly {@code ticks} remaining (start = now, end = now + ticks), whatever the entry held, and always
     * sends the exact packet. Callers pass a positive, already-monotonic value. Returns false only when another mod
     * cancelled {@code set} and no entry could be created.
     * 精确写入 {@code ticks} 剩余（起点 = 当前，终点 = 当前 + ticks），不论条目原值，并总是发送精确数据包。调用方传入已满足
     * 单调性的正数。仅当其他模组取消了 {@code set} 导致无法创建条目时返回 false。
     */
    public static boolean overwriteInPlace(ServerPlayerEntity player, Item item, int ticks) {
        ItemCooldownManager manager = player.getItemCooldownManager();
        ItemCooldownEntryAccessor bounds = entryCreatedThroughSet(manager, item, ticks);
        if (bounds == null) {
            return false;
        }
        int tick = tick(manager);
        rewrite(player, item, bounds, tick, ExactItemCooldownRules.endTick(tick, ticks));
        return true;
    }

    /**
     * Writes exactly {@code ticks} (negative counts as 0) through {@code set}: the HEAD modifiers run, then
     * {@code ExactItemCooldownMixin} restores this duration at the {@code Entry} construction, AFTER them, and keeps the
     * polymorphic {@code onCooldownUpdate(Item,int)} call that sends the vanilla S2C packet. Re-entrant: a nested write
     * restores the outer one.
     * 经 {@code set} 写入精确的 {@code ticks}（负数按 0）：HEAD 倍率照常运行，随后 {@code ExactItemCooldownMixin} 在构造
     * {@code Entry} 时（在它们之后）恢复该时长，并保留发送原版 S2C 数据包的多态 {@code onCooldownUpdate(Item,int)} 调用。
     * 可重入：嵌套写入结束后恢复外层写入。
     */
    public static void setPastModifiers(ServerPlayerEntity player, Item item, int ticks) {
        ItemCooldownManager manager = player.getItemCooldownManager();
        Write previous = EXACT_WRITE.get();
        EXACT_WRITE.set(new Write(manager, item, Math.max(0, ticks)));
        try {
            // Keep the polymorphic onCooldownUpdate(Item,int) call: it sends the vanilla S2C packet.
            // ExactItemCooldownMixin restores this duration at Entry construction, AFTER modifiers.
            manager.set(item, Math.max(0, ticks));
        } finally {
            if (previous == null) EXACT_WRITE.remove();
            else EXACT_WRITE.set(previous);
        }
    }

    /** True while {@link #setPastModifiers} is installing this exact duration. 仅在 setPastModifiers 写入该精确时长期间为 true。 */
    public static boolean isExactWrite(ItemCooldownManager manager, Item item) {
        return exactWriteTicks(manager, item) != NO_EXACT_WRITE;
    }

    /**
     * The exact duration {@link #setPastModifiers} is installing on this manager and item, or {@link #NO_EXACT_WRITE}.
     * {@link #setPastModifiers} 正在为该管理器与物品写入的精确时长；没有时为 {@link #NO_EXACT_WRITE}。
     */
    public static int exactWriteTicks(ItemCooldownManager manager, Item item) {
        Write write = EXACT_WRITE.get();
        return write != null && write.manager == manager && write.item == item ? write.ticks : NO_EXACT_WRITE;
    }

    /**
     * The live entry; when none exists, the vanilla path only creates it (and runs the set hooks), its possibly
     * modified length is corrected by the caller. Null when another mod cancelled {@code set}.
     * 当前条目；没有时仅借原版路径创建条目（并触发 set 钩子），其可能被倍率修改的时长由调用方校正。其他模组取消了
     * {@code set} 时为 null。
     */
    private static @Nullable ItemCooldownEntryAccessor entryCreatedThroughSet(ItemCooldownManager manager, Item item,
                                                                             int ticks) {
        Object entry = ((ItemCooldownManagerAccessor) manager).sparkdlc$getEntries().get(item);
        if (entry == null) {
            manager.set(item, ticks);
            entry = ((ItemCooldownManagerAccessor) manager).sparkdlc$getEntries().get(item);
        }
        return entry instanceof ItemCooldownEntryAccessor bounds ? bounds : null;
    }

    private static void rewrite(ServerPlayerEntity player, Item item, ItemCooldownEntryAccessor bounds, int tick,
                                int end) {
        bounds.sparkdlc$setStartTick(tick);
        bounds.sparkdlc$setEndTick(end);
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(new CooldownUpdateS2CPacket(item, ExactItemCooldownRules.remaining(end, tick)));
        }
    }

    private record Write(ItemCooldownManager manager, Item item, int ticks) {
    }
}
