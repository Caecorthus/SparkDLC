package dev.caecorthus.sparkdlc.client.health;

import dev.caecorthus.sparkdlc.health.BodyHealthComponent;
import dev.caecorthus.sparkdlc.health.BodyPart;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Draws the body health figure ({@link BodyHealthHudRules}): seven hollow cells, no textures and no numbers, each
 * a frame coloured by the health its part has left. Called only by the {@code RoleHudDispatcher.renderBodyHealth} layer of
 * the main-HUD TAIL seam; every gate is {@link BodyHealthHudRules#visible}. Display only: it reads the local
 * player's synced {@link BodyHealthComponent} and predicts nothing.
 * 绘制身体健康示意图（{@link BodyHealthHudRules}）：七个不使用贴图、不带数字的空心格子，边框各自按其部位的剩余血量着色。
 * 只由主 HUD TAIL 接缝的 {@code RoleHudDispatcher.renderBodyHealth} 层调用；全部门槛在
 * {@link BodyHealthHudRules#visible} 中。仅用于展示：读取本地玩家同步的 {@link BodyHealthComponent}，不做任何预测。
 */
public final class BodyHealthHud {
    // values() clones its array on every call; the part order is fixed. / values() 每次调用都会复制数组；部位顺序固定。
    private static final BodyPart[] PARTS = BodyPart.values();

    private BodyHealthHud() {
    }

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!BodyHealthHudRules.visible(client)) {
            return;
        }
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }
        BodyHealthComponent health = BodyHealthComponent.KEY.get(player);
        for (BodyPart part : PARTS) {
            BodyHealthHudRules.Cell cell = BodyHealthHudRules.cell(part);
            int x = BodyHealthHudRules.FIGURE_X + cell.x();
            int y = BodyHealthHudRules.FIGURE_Y + cell.y();
            int color = BodyHealthHudRules.frameColor(health.health(part), part.maxHealth());
            // Frame only: the inside of a cell is never filled. / 只画边框：格子内部从不填充。
            for (int inset = 0; inset < BodyHealthHudRules.FRAME_THICKNESS; inset++) {
                context.drawBorder(x + inset, y + inset, cell.width() - 2 * inset, cell.height() - 2 * inset, color);
            }
        }
    }
}
