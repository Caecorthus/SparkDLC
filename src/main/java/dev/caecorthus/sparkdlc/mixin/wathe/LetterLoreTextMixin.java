package dev.caecorthus.sparkdlc.mixin.wathe;

import dev.caecorthus.sparkdlc.faction.text.WatheTextRepairs;
import dev.caecorthus.sparkdlc.util.UpstreamTextKeys;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Rewrites the text of the finished round-letter lore at the tail of {@code applyLetterLore}, in two steps:
 * <ol>
 *   <li>the role name translates on each client (see {@link WatheTextRepairs#translatableLetterRoleName});</li>
 *   <li>the retuned lines (the reworked Bodyguard's postscript) point at this mod's keys. Wathe still picks each line
 *   by checking that the upstream key exists, which NoellesRoles guarantees; only the shown text moves to this mod's
 *   key ({@link UpstreamTextKeys#retargetLines}).</li>
 * </ol>
 * Works on the finished lore: Wathe builds the lines inside a lambda whose number differs between the two Wathe 1.5.6
 * builds, and other features (the Dominator letter, a later TAIL injector) also edit the finished lore. The two steps
 * were two TAIL injectors applied in this order; each step reads the lore the previous one left.
 * 在 {@code applyLetterLore} 末尾改写成品开局信件 lore 的文字，分两步：
 * <ol>
 *   <li>让身份名在每个客户端各自翻译（见 {@link WatheTextRepairs#translatableLetterRoleName}）；</li>
 *   <li>把需要改写的行（重做后保镖的附言）指向本模组的键。Wathe 仍按上游键是否存在来决定加哪一行（NoellesRoles 保证存在），
 *   只有显示的文字换成本模组的键（{@link UpstreamTextKeys#retargetLines}）。</li>
 * </ol>
 * 在成品 lore 上改：Wathe 在一个 lambda 里构造这些行，两个 Wathe 1.5.6 构建的 lambda 编号不同；其他功能（支配者信件，
 * 一个更晚的 TAIL 注入）也是改成品 lore。这两步原本是按此顺序应用的两个 TAIL 注入；每一步读取上一步留下的 lore。
 */
@Mixin(GameFunctions.class)
public abstract class LetterLoreTextMixin {
    @Inject(method = "applyLetterLore", at = @At("TAIL"))
    private static void sparkdlc$rewriteLetterLoreText(
            ItemStack letter,
            ServerPlayerEntity player,
            Role role,
            String roleName,
            String factionName,
            int letterColor,
            int roleColor,
            CallbackInfo ci
    ) {
        // Step 1: translatable role name. / 第一步：可翻译的身份名。
        LoreComponent lore = letter.get(DataComponentTypes.LORE);
        if (lore != null) {
            List<Text> lines = WatheTextRepairs.translatableLetterRoleName(lore.lines(), roleName);
            if (lines != null) {
                letter.set(DataComponentTypes.LORE, new LoreComponent(lines));
            }
        }
        // Step 2: retuned lines on this mod's keys, read from the lore step 1 left.
        // 第二步：改写的行换成本模组的键，读取第一步留下的 lore。
        lore = letter.get(DataComponentTypes.LORE);
        if (lore != null) {
            List<Text> lines = UpstreamTextKeys.retargetLines(lore.lines());
            if (lines != null) {
                letter.set(DataComponentTypes.LORE, new LoreComponent(lines));
            }
        }
    }
}
