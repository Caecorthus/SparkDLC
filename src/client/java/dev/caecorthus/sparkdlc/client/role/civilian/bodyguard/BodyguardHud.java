package dev.caecorthus.sparkdlc.client.role.civilian.bodyguard;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.role.civilian.bodyguard.BodyguardRules;
import dev.caecorthus.sparkdlc.role.civilian.bodyguard.BodyguardVestService;
import dev.caecorthus.sparkdlc.util.ExactItemCooldowns;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * The Bodyguard's bottom-right status lines in NoellesRoles' role-line style and the Bodyguard colour: the shield
 * (ready / raised seconds left / cooldown) and the vest (worn, or bought but not yet put on).
 * 保镖右下角状态行，沿用 NoellesRoles 职业提示的样式与保镖颜色：盾（就绪 / 举盾剩余秒数 / 冷却），以及防弹衣（已穿戴，或已买未穿）。
 */
public final class BodyguardHud {
    /** NoellesRoles' Bodyguard role colour. / NoellesRoles 保镖职业颜色。 */
    private static final int BODYGUARD_COLOR = 0x4682FA;
    private static final int EDGE_MARGIN = 2;

    private BodyguardHud() {
    }

    public static void render(TextRenderer renderer, ClientPlayerEntity player, DrawContext context) {
        if (!GameFunctions.isPlayerPlayingAndAlive(player)
                || !BodyguardRules.isBodyguard(GameWorldComponent.KEY.get(player.getWorld()).getRole(player))) {
            return;
        }
        List<Text> lines = new ArrayList<>();
        Item shield = SparkDlcItems.democracyShield();
        if (player.isUsingItem() && player.getActiveItem().isOf(shield)) {
            int left = BodyguardRules.MAX_RAISE_TICKS - player.getItemUseTime();
            lines.add(Text.translatable("tip.sparkdlc.bodyguard.hud.shield_raised", Math.max(1, seconds(left))));
        } else if (player.getInventory().contains(stack -> stack.isOf(shield))) {
            int cooldown = ExactItemCooldowns.remainingTicks(player.getItemCooldownManager(), shield);
            lines.add(cooldown > 0
                    ? Text.translatable("tip.sparkdlc.bodyguard.hud.shield_cooldown", seconds(cooldown))
                    : Text.translatable("tip.sparkdlc.bodyguard.hud.shield_ready"));
        }
        if (BodyguardVestService.isWorn(player)) {
            lines.add(Text.translatable("tip.sparkdlc.bodyguard.hud.vest"));
        } else if (player.getInventory().contains(stack -> stack.isOf(SparkDlcItems.bodyguardVest()))) {
            // Bought but not on: it only protects from the chest slot. / 已买未穿：只有穿在胸甲栏才生效。
            lines.add(Text.translatable("tip.sparkdlc.bodyguard.hud.vest_unworn"));
        }
        // A small margin keeps the last glyph and its shadow off the screen edge. / 留出少量边距，避免最后一个字及其阴影贴边被裁。
        int y = context.getScaledWindowHeight() - EDGE_MARGIN;
        for (Text line : lines) {
            y -= renderer.fontHeight;
            context.drawTextWithShadow(renderer, line,
                    context.getScaledWindowWidth() - renderer.getWidth(line) - EDGE_MARGIN, y, BODYGUARD_COLOR);
        }
    }

    private static int seconds(int ticks) {
        return Math.ceilDiv(Math.max(0, ticks), 20);
    }
}
