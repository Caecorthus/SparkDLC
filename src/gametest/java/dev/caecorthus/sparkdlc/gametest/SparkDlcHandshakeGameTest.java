package dev.caecorthus.sparkdlc.gametest;

import dev.caecorthus.sparkdlc.net.version.SparkDlcVersionCheck;
import dev.caecorthus.sparkdlc.net.version.SparkDlcVersionHandshake;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.SharedConstants;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * The version handshake over a real connection. The test server listens on a loopback port for this test, and a bare
 * socket plays the client through the vanilla login protocol: handshake, login start, then the login queries the
 * server asks. Three clients log in: one with the server's version, one with another version, one that does not know
 * the query (no SparkDLC, or a proxy in between). Nothing of the mod is called directly, so what is checked is what a
 * player's client would meet: one question, on one channel, and the kick text.
 *
 * <p>The socket runs on its own thread, because the login only advances while the server thread ticks.</p>
 *
 * 通过真实连接检查版本握手。测试服务器为本测试监听一个回环端口，由裸 socket 按原版登录协议扮演客户端：握手、开始登录，然后应答
 * 服务端提出的登录查询。共登录三个客户端：一个与服务端版本相同，一个版本不同，一个不认识该查询（没有 SparkDLC，或中间隔着代理）。
 * 不直接调用本模组的任何代码，因此检查的正是玩家客户端会遇到的情况：一个询问、一个频道，以及踢出文本。
 *
 * <p>socket 在自己的线程上运行，因为登录流程只有在服务器线程 tick 时才会推进。</p>
 */
public final class SparkDlcHandshakeGameTest implements FabricGameTest {
    /** Login-state packet ids of the vanilla protocol. / 原版协议登录状态的数据包 id。 */
    private static final int S2C_DISCONNECT = 0x00;
    private static final int S2C_LOGIN_FINISHED = 0x02;
    private static final int S2C_COMPRESSION = 0x03;
    private static final int S2C_CUSTOM_QUERY = 0x04;
    private static final int C2S_HELLO = 0x00;
    private static final int C2S_CUSTOM_QUERY_ANSWER = 0x02;
    private static final int SOCKET_TIMEOUT_MILLIS = 15_000;

    /** What one scripted client saw. / 一个脚本客户端看到的内容。 */
    private record Login(List<String> ownChannels, String serverVersion, int firstPacketAfterQueries, String kick) {
    }

    @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 600)
    public void oneLoginQueryDecidesWhoGetsIn(TestContext context) throws IOException {
        String serverVersion = SparkDlcVersionHandshake.localVersion();
        int port;
        try (ServerSocket probe = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            port = probe.getLocalPort();
        }
        context.getWorld().getServer().getNetworkIo().bind(InetAddress.getLoopbackAddress(), port);

        CompletableFuture<List<Login>> logins = CompletableFuture.supplyAsync(() -> {
            try {
                return List.of(
                        login(port, "SameVersion", version -> version),
                        login(port, "OtherVersion", version -> version + "-other"),
                        login(port, "NoSparkDlc", version -> null));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });

        context.addInstantFinalTask(() -> {
            if (!logins.isDone()) {
                throw new GameTestException("the scripted logins are still running");
            }
            List<Login> seen;
            try {
                seen = logins.join();
            } catch (RuntimeException e) {
                throw new GameTestException("a scripted login failed: " + e);
            }
            for (Login login : seen) {
                // One question, whatever the answer is going to be. / 无论将如何应答，都只有一个询问。
                if (!login.ownChannels().equals(List.of(SparkDlcVersionHandshake.VERSION_CHECK_ID.toString()))) {
                    throw new GameTestException("expected one login query of sparkdlc on "
                            + SparkDlcVersionHandshake.VERSION_CHECK_ID + ", saw " + login.ownChannels()
                            + " and then packet " + login.firstPacketAfterQueries() + " " + login.kick());
                }
                if (!serverVersion.equals(login.serverVersion())) {
                    throw new GameTestException("the query must carry the server version " + serverVersion
                            + ", carried " + login.serverVersion());
                }
            }
            requireLetIn("the same version", seen.get(0));
            Login mismatch = seen.get(1);
            String expectedKick = SparkDlcVersionCheck.mismatchMessage(serverVersion, serverVersion + "-other");
            if (mismatch.firstPacketAfterQueries() != S2C_DISCONNECT || mismatch.kick() == null
                    || !mismatch.kick().contains(expectedKick)) {
                throw new GameTestException("another version must be kicked with '" + expectedKick + "', saw packet "
                        + mismatch.firstPacketAfterQueries() + " " + mismatch.kick());
            }
            requireLetIn("no answer to the query", seen.get(2));
        });
    }

    private static void requireLetIn(String what, Login login) {
        if (login.firstPacketAfterQueries() != S2C_LOGIN_FINISHED) {
            throw new GameTestException("a client with " + what + " must be let in, saw packet "
                    + login.firstPacketAfterQueries() + " " + login.kick());
        }
    }

    /**
     * One login up to the first packet that follows the queries. {@code answer} maps the server version to the
     * version this client claims, or to null for a client that does not understand the query. Queries of other mods
     * are always answered as not understood.
     * 一次登录，进行到查询之后的第一个数据包为止。{@code answer} 把服务端版本映射为该客户端声称的版本；返回 null 表示客户端不认识
     * 该查询。其他模组的查询一律按“不认识”应答。
     */
    private static Login login(int port, String name, Function<String, String> answer) throws IOException {
        try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), port)) {
            socket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
            OutputStream out = socket.getOutputStream();
            DataInputStream in = new DataInputStream(socket.getInputStream());

            PacketByteBuf handshake = packet(0x00);
            handshake.writeVarInt(SharedConstants.getGameVersion().getProtocolVersion());
            handshake.writeString("localhost");
            handshake.writeShort(port);
            handshake.writeVarInt(2); // next state: login / 下一状态：登录
            send(out, handshake, false);
            PacketByteBuf hello = packet(C2S_HELLO);
            hello.writeString(name);
            hello.writeUuid(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes()));
            send(out, hello, false);

            List<String> ownChannels = new ArrayList<>();
            String serverVersion = null;
            // Fabric switches compression on before it sends the queries, so that they travel compressed.
            // Fabric 在发送查询之前就开启压缩，使查询以压缩格式传输。
            boolean compressed = false;
            while (true) {
                PacketByteBuf received = receive(in, compressed);
                int id = received.readVarInt();
                if (id == S2C_COMPRESSION && !compressed) {
                    compressed = true;
                    continue;
                }
                if (id != S2C_CUSTOM_QUERY) {
                    String kick = id == S2C_DISCONNECT ? received.readString(262144) : null;
                    return new Login(ownChannels, serverVersion, id, kick);
                }
                int queryId = received.readVarInt();
                String channel = received.readString();
                PacketByteBuf response = packet(C2S_CUSTOM_QUERY_ANSWER);
                response.writeVarInt(queryId);
                String claimed = null;
                if (channel.startsWith("sparkdlc:")) {
                    ownChannels.add(channel);
                    serverVersion = received.readString();
                    claimed = answer.apply(serverVersion);
                }
                response.writeBoolean(claimed != null);
                if (claimed != null) {
                    response.writeString(claimed);
                }
                send(out, response, compressed);
            }
        }
    }

    private static PacketByteBuf packet(int id) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(id);
        return buf;
    }

    /**
     * Frames one packet. Once compression is on, a frame carries the uncompressed size first, and 0 there means
     * "sent as it is": every packet of this test stays far below the threshold.
     * 封装一个数据包。开启压缩后，帧内先写未压缩大小，0 表示“原样发送”：本测试的所有数据包都远低于压缩阈值。
     */
    private static void send(OutputStream out, PacketByteBuf body, boolean compressed) throws IOException {
        PacketByteBuf frame = new PacketByteBuf(Unpooled.buffer());
        frame.writeVarInt(body.readableBytes() + (compressed ? 1 : 0));
        if (compressed) {
            frame.writeVarInt(0);
        }
        frame.writeBytes(body);
        byte[] bytes = new byte[frame.readableBytes()];
        frame.readBytes(bytes);
        out.write(bytes);
        out.flush();
    }

    private static PacketByteBuf receive(DataInputStream in, boolean compressed) throws IOException {
        int length = 0;
        for (int shift = 0; ; shift += 7) {
            int read = in.read();
            if (read < 0) {
                throw new EOFException("the server closed the connection without a packet");
            }
            length |= (read & 0x7F) << shift;
            if ((read & 0x80) == 0) {
                break;
            }
            if (shift > 28) {
                throw new IOException("frame length is not a var int");
            }
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        PacketByteBuf frame = new PacketByteBuf(Unpooled.wrappedBuffer(bytes));
        if (!compressed) {
            return frame;
        }
        int uncompressedSize = frame.readVarInt();
        if (uncompressedSize == 0) {
            return frame;
        }
        byte[] deflated = new byte[frame.readableBytes()];
        frame.readBytes(deflated);
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(deflated);
            byte[] inflated = new byte[uncompressedSize];
            if (inflater.inflate(inflated) != uncompressedSize) {
                throw new IOException("a compressed frame is shorter than it says");
            }
            return new PacketByteBuf(Unpooled.wrappedBuffer(inflated));
        } catch (DataFormatException e) {
            throw new IOException(e);
        } finally {
            inflater.end();
        }
    }
}
