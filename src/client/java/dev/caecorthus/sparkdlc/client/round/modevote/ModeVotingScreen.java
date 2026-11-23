package dev.caecorthus.sparkdlc.client.round.modevote;

import dev.caecorthus.sparkdlc.round.modevote.ModeVoteC2SPayload;
import dev.caecorthus.sparkdlc.round.modevote.ModeVotingComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

/**
 * Mode vote drawn with Wathe's map-vote kit: same vignette background, same art-deco brass rules, same ticket cards
 * (paper gradient, double border, corner rivets, description block, weighted bar, "VOTED" stamp).
 * <p>
 * 模式投票直接套用 Wathe 地图投票那套画法：同一套暗底晕影、同样的黄铜装饰线、同样的车票卡片（纸色渐变、双边框、
 * 四角铆钉、描述区、加权票条、"VOTED" 印章）。
 */
public class ModeVotingScreen extends Screen {
    // 以下配色与尺寸逐条照抄 MapVotingScreen。
    private static final int BG_TOP = 0xEE1A0505;
    private static final int BG_BOTTOM = 0xEE0F0202;
    private static final int BRASS_COLOR = 0xFFD4AF37;
    private static final int BRASS_DIM = 0xFF8B735B;
    private static final int TICKET_BG = 0xFFFDF5E6;
    private static final int TICKET_BG_DARK = 0xFFE0D5C0;
    private static final int TICKET_BG_SELECTED = 0xFFFFF8F0;
    private static final int TEXT_INK = 0xFF2F1B1B;
    private static final int TEXT_DIM = 0xFF6B5A5A;
    private static final int TEXT_RED = 0xFFA00000;
    private static final int BAR_BG = 0xFF4A3520;
    private static final int BAR_FILL = 0xFFA00000;

    private static final int CARD_W = 140;
    private static final int CARD_H = 125;
    private static final int CARD_GAP = 12;

    private final ModeVotingComponent component;

    public ModeVotingScreen(ModeVotingComponent component) {
        super(Text.translatable("gui.sparkdlc.mode_voting.title"));
        this.component = component;
    }

    private int cardLeft(int index) {
        int total = CARD_W * 2 + CARD_GAP;
        int left = (this.width - total) / 2;
        return left + index * (CARD_W + CARD_GAP);
    }

    private int cardTop() {
        return this.height / 2 - CARD_H / 2 + 8;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, BG_TOP, BG_BOTTOM);
        renderDecoBars(context);

        drawTitle(context, Text.translatable("gui.sparkdlc.mode_voting.title"), 16, 1.2f);

        int ticksLeft = component.getTicksRemaining();
        String timeStr = String.format("%d", Math.max(0, ticksLeft / 20));
        int timerColor = ticksLeft < 100 ? 0xFFFF5555 : BRASS_COLOR;
        drawCenteredText(context, Text.literal(timeStr), this.width / 2, 30, timerColor);

        int mine = this.client != null && this.client.player != null
                ? component.getVotedIndex(this.client.player.getUuid())
                : -1;

        int votes0 = component.getVoteCount(0);
        int votes1 = component.getVoteCount(1);
        int totalVotes = votes0 + votes1;
        boolean anyVote = totalVotes > 0;

        int top = cardTop();
        for (int i = 0; i < 2; i++) {
            int x = cardLeft(i);
            boolean hovered = mouseX >= x && mouseX <= x + CARD_W && mouseY >= top && mouseY <= top + CARD_H;
            int votes = component.getVoteCount(i);
            float weight = anyVote ? (float) votes / totalVotes : 0.5f;
            drawTicketCard(context, x, top, i, true, mine == i, hovered, votes, totalVotes, weight);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    /** 与 MapVotingScreen#drawTicketCard 一一对应，只是把「地图名/人数」换成「模式名/结构说明」。 */
    private void drawTicketCard(DrawContext context, int x, int y, int index,
                                boolean available, boolean selected, boolean hovered,
                                int votes, int totalVotes, float probability) {
        int cw = CARD_W;
        int ch = CARD_H;

        int bgColor = selected ? TICKET_BG_SELECTED : (available ? TICKET_BG : 0xFF504040);
        int shadowColor = 0x66000000;

        context.fill(x + 4, y + 4, x + cw + 4, y + ch + 4, shadowColor);
        context.fillGradient(x, y, x + cw, y + ch, bgColor, available ? TICKET_BG_DARK : 0xFF302020);

        int borderColor = selected ? BRASS_COLOR : (hovered ? 0xFF8B4513 : TEXT_INK);
        context.drawBorder(x + 2, y + 2, cw - 4, ch - 4, borderColor);
        context.drawBorder(x + 5, y + 5, cw - 10, ch - 10, borderColor & 0x88FFFFFF);

        int rivetColor = BRASS_DIM;
        context.fill(x + 6, y + 6, x + 8, y + 8, rivetColor);
        context.fill(x + cw - 8, y + 6, x + cw - 6, y + 8, rivetColor);
        context.fill(x + 6, y + ch - 8, x + 8, y + ch - 6, rivetColor);
        context.fill(x + cw - 8, y + ch - 8, x + cw - 6, y + ch - 6, rivetColor);

        int titleColor = available ? TEXT_INK : 0xFFAAAAAA;

        Text name = Text.translatable(index == 0
                ? "gui.sparkdlc.mode_voting.classic"
                : "gui.sparkdlc.mode_voting.dual");
        drawCenteredText(context, name, x + cw / 2, y + 15, titleColor);

        if (available) {
            Text sub = Text.translatable(index == 0
                    ? "gui.sparkdlc.mode_voting.classic_tag"
                    : "gui.sparkdlc.mode_voting.dual_tag");
            drawCenteredText(context, sub, x + cw / 2, y + 30, TEXT_DIM);

            int barWidth = cw - 24;
            int barX = x + 12;
            int barY = y + ch - 25;

            int descStartY = y + 45;
            int lineHeight = 10;
            int descAvailableHeight = barY - descStartY - 16;
            int maxLines = Math.max(1, descAvailableHeight / lineHeight);

            Text blurb = Text.translatable(index == 0
                    ? "gui.sparkdlc.mode_voting.classic_desc"
                    : "gui.sparkdlc.mode_voting.dual_desc");
            List<OrderedText> descLines = this.textRenderer.wrapLines(StringVisitable.plain(blurb.getString()), cw - 20);
            int linesToRender = Math.min(descLines.size(), maxLines);
            for (int line = 0; line < linesToRender; line++) {
                OrderedText orderedText = descLines.get(line);
                int lineWidth = this.textRenderer.getWidth(orderedText);
                context.drawText(this.textRenderer, orderedText, x + (cw - lineWidth) / 2,
                        descStartY + line * lineHeight, TEXT_DIM, false);
            }

            int probY = descStartY + linesToRender * lineHeight + 2;
            String probStr = String.format("%.1f%%", probability * 100f);
            int probColor = probability >= 0.5f ? TEXT_RED : BRASS_DIM;
            drawCenteredText(context, Text.literal(probStr), x + cw / 2, probY, probColor);

            context.fill(barX, barY, barX + barWidth, barY + 4, BAR_BG);
            if (totalVotes > 0 && votes > 0) {
                int fillW = (int) (((float) votes / totalVotes) * barWidth);
                context.fill(barX, barY, barX + fillW, barY + 4, BAR_FILL);
            }

            Text voteText = Text.literal(votes + " Votes");
            context.getMatrices().push();
            context.getMatrices().translate(x + cw / 2f, barY + 8, 0);
            context.getMatrices().scale(0.8f, 0.8f, 1);
            drawCenteredText(context, voteText, 0, 0, TEXT_DIM);
            context.getMatrices().pop();

            if (selected) {
                context.drawBorder(x + cw - 30, y + ch - 30, 24, 24, 0xFFA00000);
                context.getMatrices().push();
                context.getMatrices().translate(x + cw - 18, y + ch - 18, 0);
                context.getMatrices().scale(0.6f, 0.6f, 1);
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-15));
                drawCenteredText(context, Text.literal("VOTED"), 0, 0, 0xFFA00000);
                context.getMatrices().pop();
            }
        }
    }

    private void drawTitle(DrawContext context, Text text, int y, float scale) {
        context.getMatrices().push();
        context.getMatrices().translate(width / 2f, y, 0);
        context.getMatrices().scale(scale, scale, 1);
        drawCenteredText(context, text, 0, 0, BRASS_COLOR);
        context.getMatrices().pop();
    }

    private void drawCenteredText(DrawContext context, Text text, int x, int y, int color) {
        int w = this.textRenderer.getWidth(text);
        context.drawText(this.textRenderer, text, x - w / 2, y, color, true);
    }

    private void renderDecoBars(DrawContext context) {
        int color = BRASS_COLOR;
        context.fill(0, 45, width, 46, color);
        context.fill(0, 48, width, 49, color);
        context.fill(0, height - 40, width, height - 39, color);
        context.fill(0, height - 43, width, height - 42, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int top = cardTop();
            for (int i = 0; i < 2; i++) {
                int x = cardLeft(i);
                if (mouseX >= x && mouseX <= x + CARD_W && mouseY >= top && mouseY <= top + CARD_H) {
                    ClientPlayNetworking.send(new ModeVoteC2SPayload(i));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
