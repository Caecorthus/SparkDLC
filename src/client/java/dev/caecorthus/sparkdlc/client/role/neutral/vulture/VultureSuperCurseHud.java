package dev.caecorthus.sparkdlc.client.role.neutral.vulture;

import dev.caecorthus.sparkdlc.client.skill.SecondaryAbilityController;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSuperCursePlayerComponent;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSuperCurseC2SPacket;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSkateboardRules;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSuperCurseRules;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;


/**
 * 秃鹫“超级骂”状态行，右对齐画在右下角第三行：第一行是 NoellesRoles 的秃鹫提示，第二行是滑板状态
 * （开局即发放，通常常驻），因此固定占第三行，滑板行出现或消失时本行不跳动。
 * The Vulture's Super Curse line, right-aligned on the third bottom-right row: row one is NoellesRoles' Vulture tip,
 * row two the skateboard line (granted at round start, so usually present). A fixed row keeps this line still when the
 * board line comes and goes.
 *
 * <p>只渲染服务端同步的冷却；服务端不收此包时整行隐藏。
 * Renders only the server-synced cooldown; hidden entirely when the server does not take the request.</p>
 */
public final class VultureSuperCurseHud {
    /** Rows counted from the bottom: NoellesRoles tip, skateboard, this. / 自底向上：NoellesRoles 提示、滑板、本行。 */
    private static final int ROW_FROM_BOTTOM = 3;

    private VultureSuperCurseHud() {
    }

    public static void render(TextRenderer renderer, ClientPlayerEntity player, DrawContext context) {
        if (!ClientPlayNetworking.canSend(VultureSuperCurseC2SPacket.ID)
                || !GameFunctions.isPlayerPlayingAndAlive(player)) {
            return;
        }
        Role role = GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
        if (!VultureSuperCurseRules.isVulture(role)) {
            return;
        }

        // Same clock and rounding as the skateboard line, so the two 60 s openings count down together.
        // 与滑板行使用同一时钟和取整方式，两个 60 秒的开局冷却同步倒数。
        int cooldownTicks = VultureSuperCurseRules.cooldownRemaining(
                VultureSuperCursePlayerComponent.KEY.get(player).readyAtTick(), VultureClock.now(player.getWorld()));
        Text text = cooldownTicks > 0
                ? Text.translatable("hud.sparkdlc.vulture_super_curse.cooldown",
                        VultureSkateboardRules.displaySeconds(cooldownTicks))
                : Text.translatable("hud.sparkdlc.vulture_super_curse.ready", keyText());
        int x = context.getScaledWindowWidth() - renderer.getWidth(text);
        int y = context.getScaledWindowHeight() - renderer.fontHeight * ROW_FROM_BOTTOM;
        context.drawTextWithShadow(renderer, text, x, y, role.color());
    }

    private static Text keyText() {
        return SecondaryAbilityController.secondaryKeyText();
    }
}
