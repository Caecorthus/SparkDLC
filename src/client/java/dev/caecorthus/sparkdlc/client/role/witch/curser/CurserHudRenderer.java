package dev.caecorthus.sparkdlc.client.role.witch.curser;

import dev.caecorthus.sparkdlc.client.SparkWitchClient;
import dev.caecorthus.sparkdlc.role.witch.curser.CurserPlayerComponent;
import dev.caecorthus.sparkdlc.util.TickTime;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

/** Curser-owned bottom-right cooldown HUD. / 诅咒师自有的右下角冷却 HUD。 */
public final class CurserHudRenderer {
    private CurserHudRenderer() {
    }

    public static void render(DrawContext context, ClientPlayerEntity player) {
        int cooldown = CurserPlayerComponent.KEY.get(player).getCooldownTicks();
        Text line = cooldown > 0
                ? Text.translatable("hud.sparkdlc.curser.cooldown", TickTime.ceilSecondsUnclamped(cooldown))
                : Text.translatable("hud.sparkdlc.curser.ready", SparkWitchClient.abilityKeyText());
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        context.drawTextWithShadow(
                renderer,
                line,
                context.getScaledWindowWidth() - 5 - renderer.getWidth(line),
                context.getScaledWindowHeight() - 5 - renderer.fontHeight,
                0xA968D5
        );
    }
}
