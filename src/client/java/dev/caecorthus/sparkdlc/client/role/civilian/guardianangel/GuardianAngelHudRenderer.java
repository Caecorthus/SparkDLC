package dev.caecorthus.sparkdlc.client.role.civilian.guardianangel;

import dev.caecorthus.sparkdlc.client.SparkDLCClient;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelHudRules;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelRules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

/** Renders the Guardian Angel's owner-private targeting and cooldown line. / 渲染守护天使仅本人可见的瞄准与冷却提示。 */
public final class GuardianAngelHudRenderer {
    private static final int RIGHT_PADDING = 5;
    private static final int BOTTOM_PADDING = 5;

    private GuardianAngelHudRenderer() {
    }

    public static void render(DrawContext context, ClientPlayerEntity player) {
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        Text line = stateText(player);
        int x = context.getScaledWindowWidth() - RIGHT_PADDING - renderer.getWidth(line);
        int y = context.getScaledWindowHeight() - BOTTOM_PADDING - renderer.fontHeight;
        context.drawTextWithShadow(renderer, line, x, y, GuardianAngelRules.COLOR);
    }

    static Text stateText(ClientPlayerEntity player) {
        GuardianAngelPlayerComponent component = GuardianAngelPlayerComponent.KEY.get(player);
        PlayerEntity target = GuardianAngelTargetingPreview.aimedPlayer(player);
        return switch (GuardianAngelHudRules.state(component.getCooldownTicks(), target != null)) {
            case COOLDOWN -> Text.translatable(
                    "hud.sparkdlc.guardian_angel.cooldown",
                    GuardianAngelHudRules.cooldownSeconds(component.getCooldownTicks())
            );
            case AIM_AT_PLAYER -> Text.translatable("hud.sparkdlc.guardian_angel.no_target");
            case READY -> Text.translatable(
                    "hud.sparkdlc.guardian_angel.ready",
                    SparkDLCClient.abilityKeyText()
            );
        };
    }
}
