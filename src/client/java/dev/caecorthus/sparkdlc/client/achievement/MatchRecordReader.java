package dev.caecorthus.sparkdlc.client.achievement;

import dev.caecorthus.sparkdlc.achievement.MatchEvent;
import dev.caecorthus.sparkdlc.achievement.RecordFlattener;
import dev.caecorthus.sparkdlc.client.record.SparkMatchRecordClient;
import dev.caecorthus.sparkdlc.record.net.MatchRecordSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads the server's match record that clients receive when a round ends ({@code SparkMatchRecordClient.latest()})
 * as achievement events. A server that sends no record simply gives none, and achievements that need one cannot be
 * earned that round.
 * 把回合结束时客户端收到的服务端对局记录（{@code SparkMatchRecordClient.latest()}）读成成就事件。不发送记录的服务器不会
 * 提供记录，需要记录的成就在那一局里无法达成。
 */
final class MatchRecordReader {
    private static final Logger LOGGER = LoggerFactory.getLogger("SparkAssist/Achievements");
    // Set once a record could not be read; the record is ignored for the rest of the session.
    // 一旦某份记录读取失败即置位；本次会话余下时间里不再理会记录。
    private static boolean disabled;

    private MatchRecordReader() {
    }

    /**
     * The latest record the client holds, as an opaque token; compare identities to tell whether a new one arrived.
     * 客户端持有的最新记录，作为不透明的标记使用；比较引用即可判断是否收到了新记录。
     */
    static Object latest() {
        return disabled ? null : SparkMatchRecordClient.latest();
    }

    /**
     * The record's events in record order, each with its NBT flattened by {@link RecordFlattener}.
     * 记录中的事件，按记录顺序排列，每条的 NBT 都由 {@link RecordFlattener} 展开。
     */
    static List<MatchEvent> events(Object record) {
        if (record == null || disabled) {
            return List.of();
        }
        // This try covers reading the server-sent event data (UUIDs, RecordFlattener). A record that cannot be read
        // turns records off for the session.
        // 这个 try 覆盖对服务端发来的事件数据的读取（UUID、RecordFlattener）。读取失败的记录会让本次会话不再使用记录。
        try {
            List<MatchEvent> list = new ArrayList<>();
            for (MatchRecordSnapshot.Event event : ((MatchRecordSnapshot) record).events()) {
                NbtCompound data = event.data();
                list.add(new MatchEvent(
                        event.type(),
                        event.tick(),
                        uuid(data, "actor"),
                        uuid(data, "target"),
                        RecordFlattener.flatten(data)));
            }
            return List.copyOf(list);
        } catch (RuntimeException exception) {
            disable(exception);
            return List.of();
        }
    }

    private static UUID uuid(NbtCompound data, String key) {
        return data.containsUuid(key) ? data.getUuid(key) : null;
    }

    private static void disable(Exception exception) {
        LOGGER.warn("Reading the SparkFactionAPI match record failed; ignoring it from now on", exception);
        disabled = true;
    }
}
