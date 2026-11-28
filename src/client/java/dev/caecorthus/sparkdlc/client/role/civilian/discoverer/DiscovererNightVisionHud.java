package dev.caecorthus.sparkdlc.client.role.civilian.discoverer;

import dev.caecorthus.sparkdlc.client.SparkDLCClient;
import dev.caecorthus.sparkdlc.client.hud.CachedHudText;
import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesTaotieSeekerBridge;
import dev.caecorthus.sparkdlc.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.discoverer.DiscovererComponent;
import dev.caecorthus.sparkdlc.role.civilian.discoverer.DiscovererRules;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

/**
 * Draws the Discoverer's owner-only Night Vision line ({@link DiscovererNightVisionHudRules}). Reads only the local
 * player's real synced role and its owner-only {@link DiscovererComponent} ready tick, counted down against the client
 * world clock for display; the server alone decides a request. Called by {@link DiscovererClientModule} only on a
 * confirmed SparkDLC server. Role-owned presentation: never the {@code gui.sparkdlc.skills} panel.
 * 绘制探索者仅本人可见的夜视行（{@link DiscovererNightVisionHudRules}）。只读取本地玩家同步的真实职业及其仅本人同步的
 * {@link DiscovererComponent} 就绪刻，并依据客户端世界时钟倒数用于展示；请求是否生效只由服务端决定。仅在已确认的 SparkDLC
 * 服务器上由 {@link DiscovererClientModule} 调用。属于职业自有展示：绝不进入 {@code gui.sparkdlc.skills} 面板。
 */
public final class DiscovererNightVisionHud {
    // Render-thread cache keyed on the line, the ability key text, the Language, the font renderer and the glyph
    // options (see CachedHudText). / 渲染线程缓存，键为该行、技能按键文本、Language、字体渲染器与字形选项（见 CachedHudText）。
    private static final CachedHudText<DiscovererNightVisionHudRules.Line> CACHE = new CachedHudText<>(
            DiscovererNightVisionHud::text, DiscovererNightVisionHudRules.Line::seconds,
            line -> DiscovererNightVisionHudRules.measuredSeconds(line.seconds()));

    private DiscovererNightVisionHud() {
    }

    static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return;
        }
        // Cheapest exit first: every other client leaves at the real-role check (getRole stays raw, so a Black Raven
        // acting as the Discoverer never sees this line).
        // 先做开销最小的判断：其他所有客户端都在真实职业检查处返回（getRole 始终是真实身份，扮演探索者的黑羽鸦看不到这一行）。
        GameWorldComponent game = GameWorldComponent.KEY.get(client.world);
        boolean discoverer = DiscovererRules.isDiscoverer(game.getRole(player));
        if (!discoverer) {
            return;
        }
        // HudRenderCallback fires even with F1, unlike Wathe's own HUD layer; follow Wathe's train HUD.
        // HudRenderCallback 在 F1 下仍会触发，与 Wathe 自身 HUD 层不同；跟随 Wathe 列车 HUD 的显示状态。
        boolean trainHudActive = WatheClient.trainComponent != null && WatheClient.trainComponent.hasHud();
        boolean gameActive = game.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE;
        if (!DiscovererNightVisionHudRules.showsHud(client.options.hudHidden, gameActive, game.getFade(),
                trainHudActive, discoverer, GameFunctions.isPlayerPlayingAndAlive(player),
                NoellesTaotieSeekerBridge.isSwallowed(player))) {
            return;
        }
        DiscovererNightVisionHudRules.Line line = DiscovererNightVisionHudRules.line(
                DiscovererComponent.KEY.get(player).nightVisionCooldownRemaining(client.world.getTime()));
        // The corner row is taken by the shared witch skill line (defensive: a Discoverer has no witch skill id) or by
        // the Saint's Karma line, which any killer of a Hellfire Saint can carry, drawn flush in the corner.
        // 右下角这一行可能被共享魔女技能行占用（防御性处理：探索者没有魔女技能 id），或被圣徒业报行占用（任何在地狱火
        // 期间击杀圣徒的人都可能带有，紧贴角落绘制）。
        WitchPlayerComponent witch = WitchPlayerComponent.KEY.get(player);
        boolean sharedLineOccupied = witch.getActiveSkillId() != null || witch.getSaintState().karmaCooldownTicks() > 0;
        TextRenderer renderer = client.textRenderer;
        CACHE.update(line, line.keyHint() ? SparkDLCClient.abilityKeyText() : null, renderer);
        Text text = CACHE.text();
        int measured = CACHE.measuredWidth();
        int width = context.getScaledWindowWidth();
        int x = DiscovererNightVisionHudRules.rowX(width, CACHE.width());
        int y = DiscovererNightVisionHudRules.rowY(width, context.getScaledWindowHeight(), measured,
                renderer.fontHeight, sharedLineOccupied);
        context.drawTextWithShadow(renderer, text, x, y, line.color());
    }

    private static Text text(DiscovererNightVisionHudRules.Line line, int seconds, Text keyText) {
        if (line.keyHint()) {
            return Text.translatable(line.key(), keyText);
        }
        return line.hasSeconds() ? Text.translatable(line.key(), seconds) : Text.translatable(line.key());
    }
}
