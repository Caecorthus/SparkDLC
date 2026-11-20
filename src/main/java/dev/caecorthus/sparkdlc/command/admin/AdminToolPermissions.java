package dev.caecorthus.sparkdlc.command.admin;

import dev.caecorthus.sparkdlc.legacy.LegacyPermissions;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

/**
 * Permission nodes for SparkFactionAPI administrator tools.
 * SparkFactionAPI 管理工具使用的权限节点。
 */
public final class AdminToolPermissions {
    public static final int DEFAULT_COMMAND_LEVEL = 2;
    public static final String COMMAND_ADMIN = "sparkdlc.command.admin";

    private AdminToolPermissions() {
    }

    /**
     * Requirement shared by every administrator command: the admin node, then the node's legacy name
     * ({@link LegacyPermissions}), falling back to op level 2 when no permission provider decides.
     * 所有管理命令共用的权限要求：管理节点，其次是该节点的旧名字（{@link LegacyPermissions}）；没有权限提供者作出判断时回落到
     * 2 级 OP。
     */
    public static Predicate<ServerCommandSource> admin() {
        return LegacyPermissions.require(COMMAND_ADMIN, DEFAULT_COMMAND_LEVEL);
    }
}
