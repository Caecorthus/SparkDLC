package dev.caecorthus.sparkdlc.role.killer.bomber;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.net.MarkerPositionUpdates;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Private S2C update of the planted timed-bomb traps (bed and tray): the positions of the bed head or tray block
 * entities holding one. Sent only to players who may see the traps ({@code BomberTrapService#seesTimedBombTraps});
 * every other client receives no packet at all, so neither the positions nor the moment a trap is planted or sprung
 * reach it. The block entities' client NBT no longer carries the trap owner. {@code reset} empties the client store
 * before {@code removed} and {@code added} are applied, in that order.
 * 已埋设定时炸弹陷阱（床与托盘）的私有 S2C 更新：持有陷阱的床头或托盘方块实体的位置。只发送给可以看到陷阱的玩家
 * （BomberTrapService#seesTimedBombTraps）；其他客户端完全收不到任何数据包，因此既得不到位置，也得不到陷阱被埋设或触发的时机。
 * 方块实体发给客户端的 NBT 不再携带埋设者。reset 会先清空客户端存储，然后依次应用 removed 与 added。
 *
 * <p>The bound, the splitter and the wire format are the shared {@link MarkerPositionUpdates}; this record is the
 * packet id and the type of one marker kind.
 * 上限、拆包与线上格式来自共用的 {@link MarkerPositionUpdates}；本记录只是其中一种标记的数据包 id 与类型。</p>
 */
public record TimedBombMarkersPayload(boolean reset, List<BlockPos> added, List<BlockPos> removed) implements CustomPayload {
    /** Upper bound of each position list in one packet; larger updates are split by {@link #split}. / 单个数据包中每个位置列表的上限。 */
    public static final int MAX_POSITIONS = MarkerPositionUpdates.MAX_POSITIONS;
    public static final Id<TimedBombMarkersPayload> ID = new Id<>(SparkDLC.id("timed_bomb_markers"));
    public static final PacketCodec<RegistryByteBuf, TimedBombMarkersPayload> CODEC =
            PacketCodec.of(TimedBombMarkersPayload::write, TimedBombMarkersPayload::read);
    /** Names this marker kind in the bound and decode errors. / 在上限与解码错误消息中指明本标记种类。 */
    private static final String LABEL = "Timed bomb";

    public TimedBombMarkersPayload {
        added = List.copyOf(added);
        removed = List.copyOf(removed);
        MarkerPositionUpdates.requireBounded(LABEL, added, removed);
    }

    /**
     * Packets carrying one update without exceeding the per-packet bound. Only the first one resets, removals travel
     * before additions, and an update with nothing to say (no reset, no positions) yields no packet.
     * 在不超过单包上限的前提下承载一次更新的数据包列表。只有第一个包携带重置，移除先于新增发送；
     * 没有任何内容的更新（不重置且没有位置）不产生数据包。
     */
    public static List<TimedBombMarkersPayload> split(boolean reset, List<BlockPos> added, List<BlockPos> removed) {
        return MarkerPositionUpdates.split(reset, added, removed, TimedBombMarkersPayload::new);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    void write(PacketByteBuf buf) {
        MarkerPositionUpdates.write(buf, reset, added, removed);
    }

    static TimedBombMarkersPayload read(PacketByteBuf buf) {
        return MarkerPositionUpdates.read(buf, LABEL, TimedBombMarkersPayload::new);
    }
}
