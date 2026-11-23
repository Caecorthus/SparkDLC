package dev.caecorthus.sparkdlc.command;

import dev.caecorthus.sparkdlc.legacy.LegacyPermissions;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

/**
 * Every permission node of the mod, in one table (the administrator tools and the role commands used to keep one
 * table each). The check itself is {@link LegacyPermissions}: the node, then the name it had before the mod was
 * fused, then the default op level when no permission provider decides. The node strings are what server owners
 * grant in their permission plugin and must not change.
 * 本模组的全部权限节点，集中在一张表里（管理工具与职业命令原先各有一张表）。检查本身由 {@link LegacyPermissions} 完成：
 * 先看节点，再看它在模组融合之前的名字，没有权限提供者作出判断时回落到默认 OP 等级。节点字符串是服主在权限插件中授予的
 * 名字，不可更改。
 */
public final class SparkDlcPermissions {
    public static final int DEFAULT_COMMAND_LEVEL = 2;
    /**
     * The administrator tools: {@code /sparkdlc:clearCooldown}, the task and sanity commands, anti-fast-round.
     * 管理工具：{@code /sparkdlc:clearCooldown}、任务与理智命令、防速通。
     */
    public static final String COMMAND_ADMIN = "sparkdlc.command.admin";
    public static final String COMMAND_SET_MANA = "sparkdlc.command.setmana";
    public static final String COMMAND_FORCE_ABILITY = "sparkdlc.command.forceability";
    public static final String COMMAND_FORCE_PROMOTION = "sparkdlc.command.forcepromotion";
    /** {@code /sparkdlc:forceAccompliceRole} (D4). / 锁定魔化使晋升身份的命令（D4）。 */
    public static final String COMMAND_FORCE_ACCOMPLICE_ROLE = "sparkdlc.command.forceaccomplicerole";
    public static final String COMMAND_GHOST_CHANCE = "sparkdlc.command.ghostchance";
    public static final String COMMAND_GHOST_MIN_REQUIREMENT = "sparkdlc.command.ghostminrequirement";
    /**
     * {@code /sparkdlc:thirdFactionMinRequirement}: the third-faction player minimum.
     * 设置第三阵营最低人数的命令。
     */
    public static final String COMMAND_THIRD_FACTION_MIN_REQUIREMENT = "sparkdlc.command.thirdfactionminrequirement";
    /** {@code /sparkdlc:witchFactor}. / 魔女因子设置命令。 */
    public static final String COMMAND_WITCH_FACTOR = "sparkdlc.command.witch_factor";
    /** Using the Rift Gate Remover (传送门清除工具). / 使用传送门清除工具。 */
    public static final String ITEM_RIFT_GATE_REMOVER = "sparkdlc.item.riftgateremover";

    private SparkDlcPermissions() {
    }

    /**
     * Requirement shared by every administrator tool command: the admin node, then the node's legacy name
     * ({@link LegacyPermissions}), falling back to op level 2 when no permission provider decides.
     * 所有管理工具命令共用的权限要求：管理节点，其次是该节点的旧名字（{@link LegacyPermissions}）；没有权限提供者作出判断时
     * 回落到 2 级 OP。
     */
    public static Predicate<ServerCommandSource> admin() {
        return LegacyPermissions.require(COMMAND_ADMIN, DEFAULT_COMMAND_LEVEL);
    }
}
