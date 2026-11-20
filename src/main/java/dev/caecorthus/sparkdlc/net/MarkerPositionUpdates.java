package dev.caecorthus.sparkdlc.net;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The shared body of the private "marker" updates: an S2C packet that tells only the clients allowed to see them
 * where hidden traps are ({@code reset}, then {@code removed}, then {@code added} block positions). Two payload types
 * carry it, each with its own packet id, recipients and client store: the Bomber's timed-bomb traps
 * ({@code TimedBombMarkersPayload}) and the Conscience Poisoner's blue poison ({@code BluePoisonMarkersPayload}). They
 * used to be two copies of the same bound, splitter and codec; the wire format of both is unchanged (one boolean, then
 * two var-int counted position lists, added before removed).
 * 私有“标记”更新的共用主体：一种只发给有权看到的客户端的 S2C 数据包，告知隐藏陷阱的位置（{@code reset}，然后是 {@code removed}
 * 与 {@code added} 方块位置）。有两个数据包类型承载它，各有自己的数据包 id、接收者与客户端存储：炸弹客的定时炸弹陷阱
 * （{@code TimedBombMarkersPayload}）与善良毒师的蓝毒（{@code BluePoisonMarkersPayload}）。此前它们是同一套上限、拆包与编解码
 * 的两份副本；两者的线上格式都不变（一个布尔值，随后是两个以 var-int 计数的位置列表，added 在 removed 之前）。
 */
public final class MarkerPositionUpdates {
    /** Upper bound of each position list in one packet; larger updates are split by {@link #split}. / 单个数据包中每个位置列表的上限。 */
    public static final int MAX_POSITIONS = 1024;

    /** Builds the payload record of one marker kind. / 构造某一种标记的数据包记录。 */
    @FunctionalInterface
    public interface Factory<T> {
        T create(boolean reset, List<BlockPos> added, List<BlockPos> removed);
    }

    private MarkerPositionUpdates() {
    }

    /**
     * Refuses a packet whose lists exceed the bound; {@code label} names the marker kind in the message ("Timed bomb").
     * 拒绝列表超出上限的数据包；{@code label} 在消息中指明标记种类。
     */
    public static void requireBounded(String label, List<BlockPos> added, List<BlockPos> removed) {
        if (added.size() > MAX_POSITIONS || removed.size() > MAX_POSITIONS) {
            throw new IllegalArgumentException(label + " marker update exceeds " + MAX_POSITIONS + " positions");
        }
    }

    /**
     * Packets carrying one update without exceeding the per-packet bound. Only the first one resets, removals travel
     * before additions, and an update with nothing to say (no reset, no positions) yields no packet.
     * 在不超过单包上限的前提下承载一次更新的数据包列表。只有第一个包携带重置，移除先于新增发送；没有任何内容的更新
     * （不重置且没有位置）不产生数据包。
     */
    public static <T> List<T> split(boolean reset, List<BlockPos> added, List<BlockPos> removed, Factory<T> factory) {
        List<T> packets = new ArrayList<>();
        boolean pendingReset = reset;
        for (int from = 0; from < removed.size(); from += MAX_POSITIONS) {
            packets.add(factory.create(
                    pendingReset, List.of(), removed.subList(from, Math.min(removed.size(), from + MAX_POSITIONS))));
            pendingReset = false;
        }
        for (int from = 0; from < added.size(); from += MAX_POSITIONS) {
            packets.add(factory.create(
                    pendingReset, added.subList(from, Math.min(added.size(), from + MAX_POSITIONS)), List.of()));
            pendingReset = false;
        }
        if (pendingReset) {
            packets.add(factory.create(true, List.of(), List.of()));
        }
        return packets;
    }

    public static void write(PacketByteBuf buf, boolean reset, List<BlockPos> added, List<BlockPos> removed) {
        buf.writeBoolean(reset);
        writePositions(buf, added);
        writePositions(buf, removed);
    }

    public static <T> T read(PacketByteBuf buf, String label, Factory<T> factory) {
        boolean reset = buf.readBoolean();
        List<BlockPos> added = readPositions(buf, label);
        List<BlockPos> removed = readPositions(buf, label);
        return factory.create(reset, added, removed);
    }

    private static void writePositions(PacketByteBuf buf, List<BlockPos> positions) {
        buf.writeVarInt(positions.size());
        for (BlockPos pos : positions) {
            buf.writeBlockPos(pos);
        }
    }

    /** Bounded: a count outside 0..MAX_POSITIONS is refused before anything is allocated. / 有界：数量超出范围时在分配前拒绝。 */
    private static List<BlockPos> readPositions(PacketByteBuf buf, String label) {
        int size = buf.readVarInt();
        if (size < 0 || size > MAX_POSITIONS) {
            throw new IllegalArgumentException(
                    "Invalid " + label.toLowerCase(Locale.ROOT) + " marker count " + size);
        }
        List<BlockPos> positions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            positions.add(buf.readBlockPos());
        }
        return positions;
    }
}
