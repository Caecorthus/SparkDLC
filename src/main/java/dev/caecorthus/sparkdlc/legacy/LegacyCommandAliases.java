package dev.caecorthus.sparkdlc.legacy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps the command names of the five former mods working ({@code /sparkwitch:setMana} for
 * {@code /sparkdlc:setMana}, ...): every row of kind {@code command} in the legacy table gets an alias root.
 *
 * <p>An alias is a literal of its own that carries the current root's executor and requirement and shares the
 * current root's child nodes (the very same node objects). It is neither a Brigadier {@code redirect} nor a second
 * copy of the tree:</p>
 * <ul>
 * <li>a redirect never enters the node it points to, so the old name typed alone would not run a root that is itself
 * executable ({@code antifastround}, {@code witchFactor}, {@code listTraits}, ...) and would not ask the root's
 * requirement either;</li>
 * <li>a second copy would have to be built by calling every command class once per name, which is how side effects
 * get doubled and how the two trees drift apart.</li>
 * </ul>
 * <p>With shared children the old and the new name are the same command by construction: same executors, same
 * argument-level requirements, same suggestion providers, one node set to maintain. Brigadier nodes hold no parent
 * pointer, so a node may sit under two parents (vanilla does the same through redirects).</p>
 *
 * <p>Call {@link #register(CommandDispatcher)} after the current commands are registered. The three misspelt
 * SparkStrength aliases are older than the rename, are registered by {@code RoleKitCommands} and are not
 * touched here.</p>
 *
 * 让五个旧模组的命令名继续可用（{@code /sparkwitch:setMana} 等同于 {@code /sparkdlc:setMana} 等）：对照表中每一行
 * {@code command} 都会得到一个别名根节点。
 *
 * <p>别名是一个独立的字面量节点，带有当前根节点的执行器与权限要求，并与当前根节点共用子节点（同一批节点对象）。它既不是
 * Brigadier 的 {@code redirect}，也不是第二份命令树：</p>
 * <ul>
 * <li>redirect 不会进入它指向的节点，因此单独输入旧名时不会执行本身可执行的根节点（{@code antifastround}、
 * {@code witchFactor}、{@code listTraits} 等），也不会检查根节点的权限要求；</li>
 * <li>第二份命令树需要按名字把每个命令类各调用一次，副作用会因此重复，两棵树也会逐渐不一致。</li>
 * </ul>
 * <p>共用子节点后，新旧名字在结构上就是同一条命令：同样的执行器、同样的参数级权限要求、同样的补全来源，只需维护一份节点。
 * Brigadier 节点没有父指针，同一节点可以挂在两个父节点下（原版的 redirect 也是如此）。</p>
 *
 * <p>在当前命令注册完之后调用 {@link #register(CommandDispatcher)}。SparkStrength 的三个拼写别名早于改名，由
 * {@code RoleKitCommands} 注册，这里不处理。</p>
 */
public final class LegacyCommandAliases {
    private static final Logger LOGGER = LoggerFactory.getLogger("sparkdlc");
    private static final String KIND = "command";

    private LegacyCommandAliases() {
    }

    /**
     * Old root literal to current root literal, in table order. Command roots look like ids but are not
     * {@code Identifier}s (they are camel case).
     * 旧根字面量到当前根字面量的映射，保持表内顺序。命令根节点看起来像 id，但不是 {@code Identifier}（带驼峰大写）。
     */
    public static Map<String, String> names() {
        return LegacyIds.renamed(KIND);
    }

    /**
     * Adds every missing alias to the dispatcher and returns how many were added. Safe to call again: a name that
     * is already a root is left alone (never merged into), so nothing is registered twice.
     * 把尚未存在的别名加入分发器，返回新增数量。可重复调用：已是根节点的名字保持原样（不会合并进去），因此不会重复注册。
     */
    public static <S> int register(CommandDispatcher<S> dispatcher) {
        return register(dispatcher.getRoot(), names());
    }

    static <S> int register(CommandNode<S> root, Map<String, String> names) {
        int added = 0;
        for (Map.Entry<String, String> name : names.entrySet()) {
            CommandNode<S> current = root.getChild(name.getValue());
            if (current == null) {
                // A table row without its command: the alias cannot exist, the server must still start.
                // 表中有行但命令不存在：别名无法建立，但服务器仍须正常启动。
                LOGGER.error("No command '{}' to alias as '{}'; the old name will not work", name.getValue(),
                        name.getKey());
                continue;
            }
            if (root.getChild(name.getKey()) != null) {
                continue;
            }
            root.addChild(alias(name.getKey(), current));
            added++;
        }
        return added;
    }

    /**
     * A literal called {@code name} that behaves exactly like {@code current}: its executor, requirement and
     * redirect settings, and its children as shared node objects.
     * 名为 {@code name}、行为与 {@code current} 完全一致的字面量节点：带有其执行器、权限要求与 redirect 设置，子节点为共用的
     * 节点对象。
     */
    static <S> LiteralCommandNode<S> alias(String name, CommandNode<S> current) {
        LiteralCommandNode<S> alias = new LiteralCommandNode<>(name, current.getCommand(), current.getRequirement(),
                current.getRedirect(), current.getRedirectModifier(), current.isFork());
        for (CommandNode<S> child : current.getChildren()) {
            alias.addChild(child);
        }
        return alias;
    }
}
