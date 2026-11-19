package dev.caecorthus.sparkdlc.client.mixin.legacy;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.caecorthus.sparkdlc.client.legacy.LegacyCommandSuggestions;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Compatibility layer for old names: drops the old command roots ({@code sparkwitch:setMana}, ...) from the
 * suggestions of the chat box and of command block screens, which both complete through this class. Closes the gap
 * that neither Brigadier nor the command tree packet can mark a literal as hidden (see
 * {@link LegacyCommandSuggestions}). Display only: parsing and execution are untouched, so the old names keep
 * working if this mixin is ever removed, they merely show up in tab completion again. Remove it together with the
 * command aliases.
 *
 * 旧名称兼容层：从聊天框与命令方块界面的补全结果里去掉旧的命令根节点（{@code sparkwitch:setMana} 等），两者都通过该类补全。
 * 弥补的缺口是 Brigadier 和命令树数据包都无法把字面量标记为隐藏（见 {@link LegacyCommandSuggestions}）。只影响显示：解析与执行
 * 不受影响，因此即使移除本 mixin，旧名字仍然可用，只是会重新出现在 Tab 补全中。应与命令别名一起移除。
 */
@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorLegacyCommandMixin {
    @ModifyExpressionValue(
            method = "refresh",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/CommandDispatcher;getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;I)Ljava/util/concurrent/CompletableFuture;",
                    remap = false
            ),
            require = 1
    )
    private CompletableFuture<Suggestions> sparkdlc$hideLegacyCommandNames(CompletableFuture<Suggestions> suggestions) {
        return suggestions.thenApply(LegacyCommandSuggestions::withoutLegacyNames);
    }
}
