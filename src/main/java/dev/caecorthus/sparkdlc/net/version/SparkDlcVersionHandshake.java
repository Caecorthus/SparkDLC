package dev.caecorthus.sparkdlc.net.version;

import dev.caecorthus.sparkdlc.SparkDLC;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * The one version handshake of SparkDLC, server side, and the wire helpers both sides share.
 *
 * <ol>
 * <li><b>Login query</b> on {@link #VERSION_CHECK_ID}: the server sends its version, the client answers with its own,
 * and {@link SparkDlcVersionCheck#decideLogin} decides. An answered mismatch is refused; an unanswered query is let
 * through, because a proxy answers the backend's login queries itself.</li>
 * <li><b>Play confirmation</b> ({@link SparkDlcServerConfirmS2CPacket}), sent when the player has joined: the only
 * confirmation that reaches a client behind a proxy. A client that finds another version there disconnects itself.</li>
 * </ol>
 *
 * <p>Until stage 3 there were three login queries ({@code sparkdlc:faction_version_check},
 * {@code sparkdlc:trait_version_check}, {@code sparkdlc:witch_version_check}) and two play confirmations. Those ids are
 * retired: nothing listens on them, and a jar from before this change cannot complete the handshake with this one
 * (it never could across a version change: the version string is the whole check).</p>
 *
 * SparkDLC 唯一的版本握手（服务端部分），以及两端共用的线格式辅助方法。
 *
 * <ol>
 * <li><b>登录查询</b>（频道 {@link #VERSION_CHECK_ID}）：服务端发送自己的版本，客户端应答自己的版本，由
 * {@link SparkDlcVersionCheck#decideLogin} 判定。已应答但不一致时拒绝；未应答时放行，因为代理会自行应答后端的登录查询。</li>
 * <li><b>play 确认</b>（{@link SparkDlcServerConfirmS2CPacket}），在玩家加入后发送：代理之后的客户端只能收到这一个确认。
 * 客户端在其中发现版本不同时会自行断开。</li>
 * </ol>
 *
 * <p>第三阶段之前有三个登录查询与两个 play 确认。这些 id 已退役：没有任何代码再监听它们，此改动之前的 jar 无法与本版本完成握手
 * （跨版本本来也不行：版本字符串就是检查的全部内容）。</p>
 */
public final class SparkDlcVersionHandshake {
    public static final Identifier VERSION_CHECK_ID = SparkDLC.id("version_check");

    private static boolean serverRegistered;

    private SparkDlcVersionHandshake() {
    }

    /**
     * Registers the login query and the play confirmation. The confirmation is sent from a player-join listener, and
     * its place among the other join listeners of the mod is behaviour: see the step that calls this in
     * {@link SparkDLC}. 注册登录查询与 play 确认。确认由玩家加入监听器发送，它在本模组其他加入监听器之间的位置属于行为：
     * 见 {@link SparkDLC} 中调用本方法的步骤。
     */
    public static synchronized void registerServer() {
        if (serverRegistered) {
            return;
        }
        serverRegistered = true;
        SparkDLC.LOGGER.info("Registering SparkDLC login version check on channel {}.", VERSION_CHECK_ID);

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            String serverVersion = localVersion();
            SparkDLC.LOGGER.info(
                    "Sending SparkDLC login version query {} to {} with server version {}.",
                    VERSION_CHECK_ID,
                    handler.getConnectionInfo(),
                    serverVersion
            );
            ServerLoginNetworking.registerReceiver(handler, VERSION_CHECK_ID,
                    (minecraftServer, networkHandler, understood, buf, loginSynchronizer, responseSender) ->
                            handleResponse(networkHandler, understood, buf, serverVersion));
            sender.sendPacket(VERSION_CHECK_ID, writeVersion(serverVersion));
        });

        PayloadTypeRegistry.playS2C().register(
                SparkDlcServerConfirmS2CPacket.ID,
                SparkDlcServerConfirmS2CPacket.CODEC
        );
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (!ServerPlayNetworking.canSend(player, SparkDlcServerConfirmS2CPacket.ID)) {
                SparkDLC.LOGGER.warn(
                        "SparkDLC play confirmation channel {} is not available for {}.",
                        SparkDlcServerConfirmS2CPacket.PAYLOAD_ID,
                        player.getGameProfile().getName()
                );
                return;
            }

            // Reconfirm the SparkDLC server after play starts, because proxies can drop login queries.
            // 进入 play 阶段后再次确认 SparkDLC 服务端，因为代理可能吞掉登录查询。
            sender.sendPacket(new SparkDlcServerConfirmS2CPacket(localVersion()));
        });
    }

    /** The version of this jar, which is the whole content of the check. / 本 jar 的版本，即检查的全部内容。 */
    public static String localVersion() {
        return FabricLoader.getInstance()
                .getModContainer(SparkDLC.MOD_ID)
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();
    }

    public static PacketByteBuf writeVersion(String version) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(version);
        return buf;
    }

    public static String readVersion(PacketByteBuf buf) {
        return buf.readString(SparkDlcVersionCheck.MAX_VERSION_LENGTH);
    }

    private static void handleResponse(
            ServerLoginNetworkHandler handler,
            boolean understood,
            PacketByteBuf buf,
            String serverVersion
    ) {
        String clientVersion = understood ? readVersion(buf) : null;
        SparkDlcVersionCheck.LoginDecision decision =
                SparkDlcVersionCheck.decideLogin(understood, serverVersion, clientVersion);
        switch (decision) {
            case ACCEPT -> SparkDLC.LOGGER.info(
                    "Received SparkDLC login version response from {}: client={}, server={}.",
                    handler.getConnectionInfo(),
                    clientVersion,
                    serverVersion
            );
            case ACCEPT_UNANSWERED -> {
                SparkDLC.LOGGER.warn(
                        "SparkDLC login version query {} was not understood by {}. Expected client version {}.",
                        VERSION_CHECK_ID,
                        handler.getConnectionInfo(),
                        serverVersion
                );
                SparkDLC.LOGGER.warn(
                        "Allowing {} to continue because proxies can drop Fabric login-query responses.",
                        handler.getConnectionInfo()
                );
            }
            case REJECT_MISSING_CLIENT -> SparkDLC.LOGGER.warn(
                    "SparkDLC login version query {} was not understood by {}. Expected client version {}.",
                    VERSION_CHECK_ID,
                    handler.getConnectionInfo(),
                    serverVersion
            );
            case REJECT_MISMATCH -> SparkDLC.LOGGER.warn(
                    "Rejecting SparkDLC version mismatch for {}: client={}, server={}.",
                    handler.getConnectionInfo(),
                    clientVersion,
                    serverVersion
            );
        }
        if (decision.rejected()) {
            handler.disconnect(Text.literal(SparkDlcVersionCheck.kickMessage(decision, serverVersion, clientVersion)));
        }
    }
}
