package dev.caecorthus.sparkdlc.client.role.civilian.emma;

import dev.caecorthus.sparkdlc.client.role.witch.grandwitch.factor.WitchFactorOutlineRules;
import dev.caecorthus.sparkdlc.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.net.EmmaFactorC2SPacket;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.caecorthus.sparkdlc.role.civilian.emma.EmmaRules;
import dev.caecorthus.sparkdlc.util.TickTime;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

/** Role-owned presentation/dispatch only; the server owns target eligibility and spending. / 职业自有展示与派发；目标资格及消耗由服务端决定。 */
public final class EmmaClientModule {
    private EmmaClientModule() {
    }

    public static boolean isEmma(PlayerEntity player) {
        var role = GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
        return role != null && EmmaRules.ROLE_ID.equals(role.identifier());
    }

    /** Called only by the existing shared-key bridge, never a second wasPressed consumer. / 只由已有共用技能键桥调用，不另读按键队列。 */
    public static void use(ClientPlayerEntity player) {
        if (!SparkDlcServerConnection.isConfirmedServer() || !isEmma(player)
                || !GameFunctions.isPlayerPlayingAndAlive(player)
                || !ClientPlayNetworking.canSend(EmmaFactorC2SPacket.ID)) {
            return;
        }
        PlayerEntity target = EmmaClientTargeting.findTarget(player);
        ClientPlayNetworking.send(new EmmaFactorC2SPacket(target == null ? null : target.getUuid()));
    }

    /** Bottom-right role HUD, intentionally outside the witch-only inventory skill panel. / 右下角职业 HUD，刻意不接入魔女专属背包技能面板。 */
    public static void renderHud(DrawContext context, ClientPlayerEntity player) {
        WitchPlayerComponent state = WitchPlayerComponent.KEY.get(player);
        PlayerEntity target = EmmaClientTargeting.findTarget(player);
        Text line = switch (EmmaHudRules.state(state.getCooldownTicks(), state.getMana(),
                EmmaRules.MANA_COST, target != null)) {
            case COOLDOWN -> Text.translatable("hud.sparkdlc.emma.factor.cooldown",
                    TickTime.ceilSecondsUnclamped(state.getCooldownTicks()));
            case NEED_MANA -> Text.translatable("hud.sparkdlc.emma.factor.need_mana", EmmaRules.MANA_COST);
            case NO_TARGET -> Text.translatable("hud.sparkdlc.emma.factor.no_target");
            case TRANSFER -> Text.translatable("hud.sparkdlc.emma.factor.transfer", EmmaVisibleName.of(target));
        };
        var renderer = MinecraftClient.getInstance().textRenderer;
        context.drawTextWithShadow(renderer, line,
                context.getScaledWindowWidth() - 5 - renderer.getWidth(line),
                context.getScaledWindowHeight() - 5 - renderer.fontHeight, WitchFactorOutlineRules.EMMA_COLOR);
    }
}
