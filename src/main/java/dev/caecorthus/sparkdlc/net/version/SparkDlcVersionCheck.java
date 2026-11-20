package dev.caecorthus.sparkdlc.net.version;

/**
 * The version decision of the one SparkDLC handshake, as pure logic: no Minecraft or Fabric type is touched, so it
 * runs in a plain unit test. {@link SparkDlcVersionHandshake} asks it what to do with a login answer, and the client
 * asks it whether the server it met may be confirmed.
 *
 * <p>Until stage 3 the mod carried three copies of this rule (the former SparkFactionAPI, SparkTraits and SparkWitch
 * handshakes), each with its own channel and its own kick message. They always compared the same string, the version
 * of the one jar, so one check decides exactly what the three decided together.</p>
 *
 * SparkDLC 唯一握手的版本判定，纯逻辑实现：不接触任何 Minecraft 或 Fabric 类型，可在普通单元测试中运行。
 * {@link SparkDlcVersionHandshake} 用它决定如何处理登录应答，客户端用它判断遇到的服务端能否被确认。
 *
 * <p>第三阶段之前，本模组带着这条规则的三份副本（原 SparkFactionAPI、SparkTraits、SparkWitch 握手），各有自己的频道与踢出提示。
 * 它们比较的始终是同一个字符串，即同一个 jar 的版本，因此一次检查的结论与原先三次检查合起来的结论完全一致。</p>
 */
public final class SparkDlcVersionCheck {
    /**
     * Longest version string accepted from the other side. The value is supplied by the peer and reaches the log and
     * the disconnect text, so it is not read at the default 32767-character limit. Writing is unchanged.
     * 接受对端发来的最长版本字符串。该值由对端提供，会进入日志与断开提示，因此不按默认的 32767 字符上限读取。写入不变。
     */
    public static final int MAX_VERSION_LENGTH = 128;

    /** The name players see in the kick messages. / 踢出提示中玩家看到的名字。 */
    private static final String MOD_NAME = "SparkDLC";

    /** What the server does with the answer to its login query. / 服务端对登录查询应答的处理结果。 */
    public enum LoginDecision {
        /** The client answered with the server's version. / 客户端应答的版本与服务端一致。 */
        ACCEPT(false),
        /**
         * The query was not answered (no SparkDLC on the client, or a proxy dropped it) and that is tolerated: the
         * play-phase confirmation settles it. 查询未被应答（客户端没有 SparkDLC，或被代理吞掉）且予以放行：由 play 阶段的确认来定。
         */
        ACCEPT_UNANSWERED(false),
        /** The query was not answered and the server insists on the mod. / 查询未被应答，且服务端坚持要求安装本模组。 */
        REJECT_MISSING_CLIENT(true),
        /** The client answered with another version. / 客户端应答了不同的版本。 */
        REJECT_MISMATCH(true);

        private final boolean rejected;

        LoginDecision(boolean rejected) {
            this.rejected = rejected;
        }

        /** Whether the login is refused. / 登录是否被拒绝。 */
        public boolean rejected() {
            return rejected;
        }
    }

    private SparkDlcVersionCheck() {
    }

    /**
     * The decision for one login answer. {@code understood} is false when the client did not answer the query;
     * {@code clientVersion} is only read when it did.
     * 对一次登录应答的判定。客户端没有应答查询时 {@code understood} 为 false；只有应答了才读取 {@code clientVersion}。
     */
    public static LoginDecision decideLogin(boolean understood, String serverVersion, String clientVersion) {
        if (!understood) {
            // Reject answered mismatches early, but tolerate unanswered login queries behind proxies.
            // 已回应但版本不一致时尽早拒绝；代理后的未回应登录查询则允许继续。
            return shouldRejectUnansweredLoginQuery()
                    ? LoginDecision.REJECT_MISSING_CLIENT
                    : LoginDecision.ACCEPT_UNANSWERED;
        }
        return isCompatible(serverVersion, clientVersion) ? LoginDecision.ACCEPT : LoginDecision.REJECT_MISMATCH;
    }

    /**
     * The text a refused login is disconnected with, or null when the decision lets the player in.
     * 被拒绝的登录断开时显示的文本；判定为放行时返回 null。
     */
    public static String kickMessage(LoginDecision decision, String serverVersion, String clientVersion) {
        return switch (decision) {
            case REJECT_MISSING_CLIENT -> missingClientMessage(serverVersion);
            case REJECT_MISMATCH -> mismatchMessage(serverVersion, clientVersion);
            case ACCEPT, ACCEPT_UNANSWERED -> null;
        };
    }

    public static boolean isCompatible(String serverVersion, String clientVersion) {
        return !isBlank(serverVersion) && !isBlank(clientVersion) && serverVersion.equals(clientVersion);
    }

    public static boolean shouldRejectUnansweredLoginQuery() {
        // Proxy transfers can hide Fabric login-query support from a correctly modded client.
        // 代理转服可能让正确安装模组的客户端在登录查询阶段显示为未理解。
        return false;
    }

    public static String missingClientMessage(String serverVersion) {
        return MOD_NAME + " is required on the client with version " + serverVersion + ".";
    }

    public static String mismatchMessage(String serverVersion, String clientVersion) {
        return MOD_NAME + " version mismatch: server=" + serverVersion
                + ", client=" + clientVersion
                + ". Please install the same " + MOD_NAME + " version as the server.";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
