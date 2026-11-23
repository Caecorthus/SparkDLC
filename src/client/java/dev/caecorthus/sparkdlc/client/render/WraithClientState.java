package dev.caecorthus.sparkdlc.client.render;

import dev.caecorthus.sparkdlc.skill.api.SparkRoleApi;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Reads only confirmed, synchronized Wraith state on the client.
 * 客户端仅在服务端确认后读取已同步的冤魂状态。
 */
public final class WraithClientState {
    private WraithClientState() {
    }

    public static boolean isActive(PlayerEntity player) {
        return SparkDlcServerConnection.isConfirmedServer()
                && player != null
                && SparkRoleApi.isWraithActive(player);
    }

    public static boolean isRestricted(PlayerEntity player) {
        return isActive(player) && SparkRoleApi.isWraithRestricted(player);
    }

    public static boolean isPromoted(PlayerEntity player) {
        return SparkDlcServerConnection.isConfirmedServer()
                && player != null
                && SparkRoleApi.isWraithPromoted(player);
    }
}
