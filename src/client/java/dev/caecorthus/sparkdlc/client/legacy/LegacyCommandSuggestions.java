package dev.caecorthus.sparkdlc.client.legacy;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.caecorthus.sparkdlc.legacy.LegacyCommandAliases;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Keeps the old command names out of tab completion. The server still sends them in the command tree, so a typed
 * {@code /sparkwitch:setMana} is recognised (not shown as an unknown command), its arguments are suggested like
 * those of {@code /sparkdlc:setMana}, and it runs; only the old root names themselves are no longer offered, so
 * {@code /spark} + Tab lists the current names once instead of every command twice.
 *
 * <p>Brigadier has no "hidden" flag and the command tree packet has none either: a literal that is in the client's
 * tree is always suggested, and one that is not in the tree gets no argument suggestions. The only place where both
 * can be had is the client's suggestion list, which is what this filters.</p>
 *
 * 让旧命令名不出现在 Tab 补全中。服务器仍会在命令树里发送它们，因此输入的 {@code /sparkwitch:setMana} 能被识别（不会显示为
 * 未知命令），参数补全与 {@code /sparkdlc:setMana} 相同，并且可以执行；只是不再提示旧的根命令名，这样 {@code /spark} + Tab
 * 只列出一遍当前名字，而不是每条命令出现两次。
 *
 * <p>Brigadier 没有“隐藏”标记，命令树数据包也没有：位于客户端命令树中的字面量一定会被提示，不在树中的字面量则得不到参数补全。
 * 唯一能两者兼得的位置是客户端的补全列表，本类过滤的正是它。</p>
 */
public final class LegacyCommandSuggestions {
    private LegacyCommandSuggestions() {
    }

    /** The suggestions without the old command names. / 去掉旧命令名之后的补全结果。 */
    public static Suggestions withoutLegacyNames(Suggestions suggestions) {
        return without(suggestions, LegacyCommandAliases.names().keySet());
    }

    static Suggestions without(Suggestions suggestions, Set<String> hidden) {
        List<Suggestion> kept = new ArrayList<>(suggestions.getList().size());
        for (Suggestion suggestion : suggestions.getList()) {
            if (!hidden.contains(suggestion.getText())) {
                kept.add(suggestion);
            }
        }
        return kept.size() == suggestions.getList().size() ? suggestions : new Suggestions(suggestions.getRange(), kept);
    }
}
