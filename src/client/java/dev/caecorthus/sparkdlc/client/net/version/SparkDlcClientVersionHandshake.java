package dev.caecorthus.sparkdlc.client.net.version;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.SparkDlcRoles;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConfirmS2CPacket;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.caecorthus.sparkdlc.net.version.SparkDlcVersionCheck;
import dev.caecorthus.sparkdlc.net.version.SparkDlcVersionHandshake;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

/**
 * Client side of the one SparkDLC handshake ({@link SparkDlcVersionHandshake}): answers the login query with the
 * client's version, and takes the play confirmation. Either one confirms the server in
 * {@link SparkDlcServerConnection} when the versions match; a play confirmation with another version disconnects.
 * SparkDLC 唯一握手（{@link SparkDlcVersionHandshake}）的客户端部分：以客户端版本应答登录查询，并接收 play 确认。
 * 两者中任何一个在版本一致时都会在 {@link SparkDlcServerConnection} 中确认服务端；play 确认的版本不同时断开连接。
 */
public final class SparkDlcClientVersionHandshake {
    private static boolean clientRegistered;

    private SparkDlcClientVersionHandshake() {
    }

    public static synchronized void registerClient() {
        if (clientRegistered) {
            SparkDLC.LOGGER.info("SparkDLC client login version receiver is already registered.");
            return;
        }
        clientRegistered = true;

        boolean registered = ClientLoginNetworking.registerGlobalReceiver(SparkDlcVersionHandshake.VERSION_CHECK_ID,
                (client, handler, buf, callbacks) -> {
                    String serverVersion = SparkDlcVersionHandshake.readVersion(buf);
                    String clientVersion = SparkDlcVersionHandshake.localVersion();
                    boolean confirmed = confirmServer(serverVersion, clientVersion, true);
                    SparkDLC.LOGGER.info(
                            "Answering SparkDLC login version query: server={}, client={}, confirmed={}.",
                            serverVersion,
                            clientVersion,
                            confirmed
                    );
                    return CompletableFuture.completedFuture(
                            SparkDlcVersionHandshake.writeVersion(clientVersion));
                });
        if (registered) {
            // The server treats an unregistered receiver as a missing client-side mod.
            // 服务端会把未注册的接收器判定为客户端缺少该模组。
            SparkDLC.LOGGER.info(
                    "Registered SparkDLC client login version receiver on channel {}.",
                    SparkDlcVersionHandshake.VERSION_CHECK_ID
            );
        } else {
            SparkDLC.LOGGER.warn(
                    "SparkDLC client login version receiver already existed on channel {}.",
                    SparkDlcVersionHandshake.VERSION_CHECK_ID
            );
        }

        ClientPlayNetworking.registerGlobalReceiver(SparkDlcServerConfirmS2CPacket.ID, (payload, context) -> {
            String clientVersion = SparkDlcVersionHandshake.localVersion();
            if (!confirmServer(payload.serverVersion(), clientVersion, false)) {
                SparkDLC.LOGGER.warn(
                        "Disconnecting from SparkDLC play confirmation mismatch: server={}, client={}.",
                        payload.serverVersion(),
                        clientVersion
                );
                context.responseSender().disconnect(Text.literal(
                        SparkDlcVersionCheck.mismatchMessage(payload.serverVersion(), clientVersion)
                ));
            }
        });
    }

    /**
     * Confirms only a version-compatible server and clears a stale confirmation otherwise.
     * 只确认版本兼容的服务端；否则清除残留的确认。
     */
    private static boolean confirmServer(String serverVersion, String clientVersion, boolean login) {
        String stage = login ? "login" : "play";
        if (!SparkDlcVersionCheck.isCompatible(serverVersion, clientVersion)) {
            SparkDlcServerConnection.reset();
            SparkDLC.LOGGER.warn(
                    "Ignoring SparkDLC {} confirmation mismatch: server={}, client={}.",
                    stage,
                    serverVersion,
                    clientVersion
            );
            return false;
        }
        if (login) {
            // A login confirmation belongs to the connection being established and must survive its JOIN reset.
            // 登录阶段的确认属于正在建立的连接，必须保留到其 JOIN 重置之后。
            SparkDlcServerConnection.confirmServerAtLogin();
        } else {
            SparkDlcServerConnection.confirmServer();
        }
        // The Assassin's guess list is ordered once the server is known to carry the mod's roles.
        // 确知服务端带有本模组的职业后，排定刺客的猜测列表顺序。
        SparkDlcRoles.refreshAssassinGuessRoleOrder();
        SparkDLC.LOGGER.info(
                "Confirmed SparkDLC server through {} channel: server={}, client={}.",
                stage,
                serverVersion,
                clientVersion
        );
        return true;
    }
}
