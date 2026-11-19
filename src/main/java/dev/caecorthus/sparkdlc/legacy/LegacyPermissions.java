package dev.caecorthus.sparkdlc.legacy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;

/**
 * The one permission check of the mod (fabric-permissions-api). A node that was renamed with the mod
 * ({@code sparkwitch.command.setmana} to {@code sparkdlc.command.setmana}, rows of kind {@code permission} in the
 * legacy table) is answered in this order:
 * <ol>
 * <li>the current node, when a permission provider set it (allow or deny);</li>
 * <li>otherwise the legacy node, when a provider set it (allow or deny);</li>
 * <li>otherwise the vanilla op level.</li>
 * </ol>
 * So a LuckPerms setup that still grants or denies the old node keeps working, and the new node, once set, is
 * authoritative. The op level is asked once, after both nodes: {@code check(new) || check(old)} would be wrong,
 * because each half falls back to the op level on its own and an operator would walk through an explicit deny on
 * the old node. A node without a legacy name behaves exactly like {@code Permissions.check} / {@code require}.
 *
 * 本模组唯一的权限检查（fabric-permissions-api）。随模组改名的节点（{@code sparkwitch.command.setmana} 变为
 * {@code sparkdlc.command.setmana}，即对照表中的 {@code permission} 行）按以下顺序判定：
 * <ol>
 * <li>权限提供者设置了当前节点时，以它为准（允许或拒绝）；</li>
 * <li>否则权限提供者设置了旧节点时，以旧节点为准（允许或拒绝）；</li>
 * <li>否则使用原版 OP 等级。</li>
 * </ol>
 * 因此仍在授予或拒绝旧节点的 LuckPerms 配置继续有效，而新节点一旦设置就具有最终决定权。OP 等级只在两个节点之后询问一次：
 * {@code check(新) || check(旧)} 是错误的，因为两边各自都会回落到 OP 等级，管理员会绕过旧节点上的明确拒绝。没有旧名字的节点
 * 行为与 {@code Permissions.check} / {@code require} 完全相同。
 */
public final class LegacyPermissions {
    private static final String KIND = "permission";
    private static final Map<String, List<String>> LEGACY_NODES = legacyNodesByCurrentNode();

    private LegacyPermissions() {
    }

    /** {@code Permissions.require} with the legacy node as second choice. / 以旧节点为第二选择的 {@code Permissions.require}。 */
    public static Predicate<ServerCommandSource> require(String node, int defaultRequiredLevel) {
        return source -> check(source, node, defaultRequiredLevel);
    }

    /** {@code Permissions.check} with the legacy node as second choice. / 以旧节点为第二选择的 {@code Permissions.check}。 */
    public static boolean check(CommandSource source, String node, int defaultRequiredLevel) {
        return decide(node, legacyNodes(node), asked -> Permissions.getPermissionValue(source, asked),
                () -> source.hasPermissionLevel(defaultRequiredLevel));
    }

    /** The entity form, like {@code Permissions.check(Entity, ...)}. / 实体版本，与 {@code Permissions.check(Entity, ...)} 相同。 */
    public static boolean check(Entity entity, String node, int defaultRequiredLevel) {
        return check(entity.getCommandSource(), node, defaultRequiredLevel);
    }

    /** The names {@code node} had before the rename; empty for a node that was never renamed. / {@code node} 改名前的名字；从未改名的节点返回空列表。 */
    public static List<String> legacyNodes(String node) {
        return LEGACY_NODES.getOrDefault(node, List.of());
    }

    /**
     * The precedence itself, free of Minecraft types: the first node a provider answered wins, current node first;
     * {@code opLevel} is asked only when none answered.
     * 判定顺序本身，不依赖 Minecraft 类型：第一个得到权限提供者答复的节点生效，当前节点优先；都没有答复时才询问 {@code opLevel}。
     */
    static boolean decide(String node, List<String> legacyNodes, Function<String, TriState> provider,
            BooleanSupplier opLevel) {
        TriState state = provider.apply(node);
        for (String legacyNode : legacyNodes) {
            if (state != TriState.DEFAULT) {
                break;
            }
            state = provider.apply(legacyNode);
        }
        return state.orElseGet(opLevel);
    }

    private static Map<String, List<String>> legacyNodesByCurrentNode() {
        Map<String, List<String>> byCurrentNode = new LinkedHashMap<>();
        LegacyIds.renamed(KIND).forEach((legacyNode, node) ->
                byCurrentNode.computeIfAbsent(node, current -> new ArrayList<>()).add(legacyNode));
        byCurrentNode.replaceAll((node, legacyNodes) -> List.copyOf(legacyNodes));
        return Map.copyOf(byCurrentNode);
    }
}
