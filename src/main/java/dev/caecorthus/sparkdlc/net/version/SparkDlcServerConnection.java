package dev.caecorthus.sparkdlc.net.version;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * The one answer to "does the server of the current connection run SparkDLC in this version": true once the login
 * query or the play confirmation of the handshake confirmed it. Client features that predict or draw what only a
 * SparkDLC server provides ask this before they act, so that the mod stays dark on any other server.
 *
 * <p>Until stage 3 the former SparkTraits and SparkWitch handshakes each kept such a flag. This class keeps the
 * lifecycle of the SparkTraits one, which is the stricter of the two: besides every connection edge it also drops a
 * confirmation at a JOIN that this connection's own login did not earn (a proxy backend switch), whereas the
 * SparkWitch flag survived every JOIN. The edges are driven from the session listeners of
 * {@code client.SparkDLCClient}.</p>
 *
 * “当前连接的服务端是否运行着同版本的 SparkDLC”的唯一答案：握手的登录查询或 play 确认通过后为 true。预测或绘制只有 SparkDLC
 * 服务端才提供的内容的客户端功能，在动作前都先询问它，使本模组在其他服务端上保持静默。
 *
 * <p>第三阶段之前，原 SparkTraits 与 SparkWitch 握手各自维护一个这样的标记。本类沿用 SparkTraits 标记的生命周期，它是两者中更严格的：
 * 除了每个连接节点之外，它还会在 JOIN 时丢弃并非由本连接自己的登录取得的确认（代理切换后端），而 SparkWitch 标记会保留到每次 JOIN
 * 之后。各节点由 {@code client.SparkDLCClient} 的会话监听器驱动。</p>
 */
public final class SparkDlcServerConnection {
    private static volatile boolean confirmedServer;
    /*
     * True from a login-phase confirmation until that connection's play session starts. A login query always precedes
     * the JOIN of its own connection, so JOIN keeps a confirmation that arrived this way and drops any other one.
     * 从登录阶段确认起，到该连接的 play 会话开始为止为 true。登录查询必然早于本连接的 JOIN，
     * 因此 JOIN 保留这样得到的确认，并丢弃其他来源的旧确认。
     */
    private static volatile boolean loginConfirmationAwaitingJoin;

    private SparkDlcServerConnection() {
    }

    /** Play-phase confirmation of the running session. / 当前会话在 play 阶段的确认。 */
    public static void confirmServer() {
        confirmedServer = true;
    }

    /**
     * Login-phase confirmation of the connection that is being established; it survives that connection's JOIN.
     * 正在建立的连接在登录阶段的确认；它会保留到该连接的 JOIN 之后。
     */
    public static void confirmServerAtLogin() {
        loginConfirmationAwaitingJoin = true;
        confirmedServer = true;
    }

    /**
     * Client JOIN: a proxy backend switch starts a new play session without a DISCONNECT, so a confirmation from the
     * previous backend must not carry over. Only this connection's own login confirmation is kept; otherwise the new
     * backend's play confirmation packet (sent on every server-side JOIN) confirms again.
     * 客户端 JOIN：代理切换后端会在没有 DISCONNECT 的情况下开始新的 play 会话，上一个后端的确认不能沿用。
     * 只保留本连接自己的登录确认；否则由新后端的 play 确认包（服务端每次 JOIN 都会发送）重新确认。
     */
    public static void beginPlaySession() {
        if (!loginConfirmationAwaitingJoin) {
            confirmedServer = false;
        }
        loginConfirmationAwaitingJoin = false;
    }

    public static void reset() {
        confirmedServer = false;
        loginConfirmationAwaitingJoin = false;
    }

    public static boolean isConfirmedServer() {
        return confirmedServer;
    }

    public static boolean isUnconfirmedClientWorld(World world) {
        return world != null && world.isClient() && !confirmedServer;
    }

    public static boolean isUnconfirmedClientEntity(Entity entity) {
        return entity != null && isUnconfirmedClientWorld(entity.getWorld());
    }
}
