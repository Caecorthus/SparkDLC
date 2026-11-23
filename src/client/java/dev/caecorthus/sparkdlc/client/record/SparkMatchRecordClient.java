package dev.caecorthus.sparkdlc.client.record;

import dev.caecorthus.sparkdlc.client.record.MatchRecordClient;
import dev.caecorthus.sparkdlc.record.net.MatchRecordSnapshot;
import org.jetbrains.annotations.Nullable;

/**
 * Client read access to the record of the last match that ended on the current server. The server sends it once per
 * match, before the round turns INACTIVE on the client, so it is already here when the round-end screen appears.
 * It is null before the first finished match of a connection and is cleared on disconnect.
 * <p>
 * The achievements read the record through this class ({@code achievement.MatchRecordReader}, a direct call; it
 * used to be a reflective one from another jar): the method {@code latest()}, and the
 * {@link MatchRecordSnapshot} accessors {@code matchId()}, {@code events()}, {@code seq()}, {@code type()},
 * {@code tick()} and {@code data()}.
 * <p>
 * 客户端读取当前服务器上最近一局已结束对局记录的入口。服务端每局发送一次，且早于客户端上回合变为 INACTIVE，
 * 因此出现局末画面时记录已经到达。本次连接中尚无已结束对局时为 null，断开连接时清除。
 * <p>
 * 成就功能经由本类读取记录（{@code achievement.MatchRecordReader}，直接调用；原先是来自另一个 jar 的反射调用）：
 * 方法 {@code latest()}，以及
 * {@link MatchRecordSnapshot} 的访问器 {@code matchId()}、{@code events()}、{@code seq()}、{@code type()}、
 * {@code tick()}、{@code data()}。
 */
public final class SparkMatchRecordClient {
    private SparkMatchRecordClient() {
    }

    /**
     * The latest received match record, or null. Call it on the client thread; treat event NBT as read-only.
     * 最近收到的对局记录，没有时为 null。请在客户端线程调用；事件 NBT 请视为只读。
     */
    public static @Nullable MatchRecordSnapshot latest() {
        return MatchRecordClient.latest();
    }
}
