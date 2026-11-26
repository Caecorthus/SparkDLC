package dev.caecorthus.sparkdlc.client.mixin.health;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.caecorthus.sparkdlc.client.hud.topleft.TopLeftHud;
import dev.caecorthus.sparkdlc.client.hud.topleft.TopLeftHudLayout;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.client.gui.MoodRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Re-lays the top-left corner of the in-round HUD around the body health figure ({@link TopLeftHudLayout},
 * {@link TopLeftHud}) while Wathe 1.5.6 {@code MoodRenderer} keeps drawing it: every handler only moves, hides or
 * follows one of Wathe's own draw calls, and on a server that is not a confirmed SparkDLC one each of them passes its
 * call through unchanged ({@link TopLeftHudLayout#WATHE}), so Wathe's block stays pixel-identical there.
 * <p>Pinned Wathe 1.5.6 seams, all in {@code watheClientMixinContracts}:
 * <ul>
 *     <li>{@code renderHud}: its only {@code drawTextWithShadow(TextRenderer, Text, III)} draws one task row at
 *     (22, 6) under the row's {@code translate(0, 10 * offset, 0)}; the wrap shifts the row, clips it under the
 *     header (a new row slides in from offset -1) and records its alpha. The row text is whatever the Planner and
 *     Bell Echo {@code TaskRenderer.tick} mixins left in the field. HEAD resets the rows' presence, TAIL (normal end
 *     only, as for the faction mood flags) draws the header. Its only
 *     {@code renderPsycho} call is wrapped in one translation (and, beside the figure, in a clip at the figure's
 *     right edge for the jittering flag trail), and {@code renderPsycho}'s only {@code enableScissor} is shifted
 *     with it (a scissor ignores the matrix in 1.21.1).</li>
 *     <li>{@code renderCivilian}: {@code MatrixStack.translate(FFF)} ordinal 1 is the flag block's
 *     {@code (0, 3 * moodOffset, 0)}, so the flag and the arrows drawn over it move together and keep the breakdown
 *     shake (ordinal 0); the wrap of the only icon {@code drawGuiTexture} adds the signed percentage after the flag,
 *     in the flag's matrix. Ordinal 5 is the bar block's {@code (0, 10 * moodOffset, 0)}; both {@code fill(IIIII)} calls are
 *     the mood bar, and the only {@code drawTextWithShadow} is the breakdown warning.</li>
 *     <li>{@code renderKiller}: {@code translate(FFF)} ordinal 0 is the flag block's slide, and the only
 *     {@code fill(IIIII)} is the fake mood bar (a FAKE mood always reads 1, so there is no value to show).</li>
 * </ul>
 * Composition: every hook on a call another mixin of the mod also hooks is a {@code @WrapOperation} (never a
 * {@code @Redirect}). This mixin is the last entry of the client config, so on the civilian icon call its wrap is the
 * outermost one around Noa's, Natsume's and the faction flags' sprite swaps, and the traits' {@code @ModifyArg}s on
 * the bar's {@code scale} and {@code hsvToRgb} still run before the hidden {@code fill}. Display only.
 * <p>围绕身体健康示意图重新排布对局 HUD 左上角（{@link TopLeftHudLayout}、{@link TopLeftHud}），而该角落仍由 Wathe 1.5.6
 * {@code MoodRenderer} 绘制：每个处理方法只移动、隐藏或跟随 Wathe 自身的一次绘制调用；在未确认为 SparkDLC 的服务器上，
 * 它们都把调用原样放行（{@link TopLeftHudLayout#WATHE}），因此 Wathe 的区块在那里保持逐像素一致。
 * <p>锁定的 Wathe 1.5.6 接缝，全部登记在 {@code watheClientMixinContracts} 中：
 * <ul>
 *     <li>{@code renderHud}：其中唯一的 {@code drawTextWithShadow(TextRenderer, Text, III)} 在该行的
 *     {@code translate(0, 10 * offset, 0)} 下于 (22, 6) 绘制一行任务；包装负责平移该行、在标题下方裁剪（新任务行从偏移 -1
 *     处滑入）并记录其透明度。行文本即规划大师与
 *     敲钟回响的 {@code TaskRenderer.tick} mixin 留在字段中的内容。HEAD 清零任务行存在度，TAIL（仅正常结尾，与阵营心情旗帜
 *     相同）绘制标题。唯一的 {@code renderPsycho} 调用被包在一次平移内（位于示意图旁边时，还包在一个以示意图右缘为界的裁剪内，
 *     用于抖动的旗帜残影），{@code renderPsycho} 中唯一的
 *     {@code enableScissor} 随之平移（1.21.1 的裁剪区域不受矩阵影响）。</li>
 *     <li>{@code renderCivilian}：{@code MatrixStack.translate(FFF)} 第 1 个是旗帜区块的
 *     {@code (0, 3 * moodOffset, 0)}，因此旗帜与叠画其上的箭头一起移动，并保留崩溃抖动（第 0 个）；唯一的图标
 *     {@code drawGuiTexture} 的包装在旗帜之后、于旗帜的矩阵中补上百分比。第 5 个是理智条区块的
 *     {@code (0, 10 * moodOffset, 0)}；两次 {@code fill(IIIII)} 都是理智条，唯一的 {@code drawTextWithShadow} 是崩溃警告。</li>
 *     <li>{@code renderKiller}：{@code translate(FFF)} 第 0 个是旗帜区块的下移，唯一的 {@code fill(IIIII)} 是伪装理智条
 *     （FAKE 理智恒为 1，没有可显示的数值）。</li>
 * </ul>
 * 共存方式：凡是本模组其他 mixin 也挂钩的调用，这里一律使用 {@code @WrapOperation}（绝不用 {@code @Redirect}）。本 mixin 是
 * 客户端配置的最后一项，因此在平民图标调用上它的包装位于诺亚、夏目安安与阵营旗帜的 sprite 替换之外的最外层；词条对理智条
 * {@code scale} 与 {@code hsvToRgb} 的 {@code @ModifyArg} 仍在被隐藏的 {@code fill} 之前执行。仅用于展示。
 */
@Mixin(MoodRenderer.class)
public abstract class TopLeftHudMoodRendererMixin {
    @Inject(method = "renderHud", at = @At("HEAD"), require = 1, allow = 1)
    private static void sparkdlc$beginTopLeftFrame(
            PlayerEntity player,
            TextRenderer textRenderer,
            DrawContext context,
            RenderTickCounter tickCounter,
            CallbackInfo ci
    ) {
        TopLeftHud.beginFrame();
    }

    /** One task row: shifted to the layout's row column, its alpha recorded. / 一行任务：平移到布局的任务列，并记录其透明度。 */
    @WrapOperation(
            method = "renderHud",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
            ),
            require = 1,
            allow = 1
    )
    private static int sparkdlc$moveTaskRow(
            DrawContext context,
            TextRenderer textRenderer,
            Text text,
            int x,
            int y,
            int color,
            Operation<Integer> original
    ) {
        TopLeftHudLayout layout = TopLeftHud.layout();
        TopLeftHud.rowShown(TopLeftHudLayout.rowAlpha(color));
        if (!layout.relaid()) {
            return original.call(context, textRenderer, text, x, y, color);
        }
        // A new row slides in from the header's line: clip it right under the header.
        // 新任务行从标题所在行滑入：在标题正下方裁剪。
        context.enableScissor(0, layout.rowClipTop(), context.getScaledWindowWidth(), context.getScaledWindowHeight());
        int width = original.call(context, textRenderer, text, x + layout.taskDx(), y + layout.taskDy(), color);
        context.disableScissor();
        return width;
    }

    /** Psycho mode: the whole block moves with the flag (its banner and bar are hidden below). / 疯魔模式：整个区块随旗帜移动（其横幅与计时条在下方被隐藏）。 */
    @WrapOperation(
            method = "renderHud",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/client/gui/MoodRenderer;renderPsycho(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/client/gui/DrawContext;Ldev/doctor4t/wathe/cca/PlayerPsychoComponent;Lnet/minecraft/client/render/RenderTickCounter;)V"
            ),
            require = 1,
            allow = 1
    )
    private static void sparkdlc$movePsychoBlock(
            PlayerEntity player,
            TextRenderer textRenderer,
            DrawContext context,
            PlayerPsychoComponent psycho,
            RenderTickCounter tickCounter,
            Operation<Void> original
    ) {
        TopLeftHudLayout layout = TopLeftHud.layout();
        if (layout.flagDx() == 0 && layout.flagDy() == 0) {
            original.call(player, textRenderer, context, psycho, tickCounter);
            return;
        }
        // The flag trail jitters around the flag by up to a dozen pixels: beside the figure it is clipped at the
        // figure's right edge. Wathe's own banner clip nests inside this one.
        // 旗帜残影会在旗帜周围抖动十余像素：位于示意图旁边时在示意图右缘处裁剪。Wathe 自身的横幅裁剪嵌套在其内。
        boolean besideFigure = layout.hasFigure();
        if (besideFigure) {
            context.enableScissor(layout.figureRight(), 0, context.getScaledWindowWidth(),
                    context.getScaledWindowHeight());
        }
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate((float) layout.flagDx(), (float) layout.flagDy(), 0.0F);
        original.call(player, textRenderer, context, psycho, tickCounter);
        matrices.pop();
        if (besideFigure) {
            context.disableScissor();
        }
    }

    /** The banner's clip rectangle is in screen pixels, so it is shifted by hand. / 横幅的裁剪矩形以屏幕像素计，因此单独平移。 */
    @WrapOperation(
            method = "renderPsycho",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;enableScissor(IIII)V"),
            require = 1,
            allow = 1
    )
    private static void sparkdlc$movePsychoScissor(
            DrawContext context,
            int x1,
            int y1,
            int x2,
            int y2,
            Operation<Void> original
    ) {
        TopLeftHudLayout layout = TopLeftHud.layout();
        original.call(context, x1 + layout.flagDx(), y1 + layout.flagDy(), x2 + layout.flagDx(), y2 + layout.flagDy());
    }

    /**
     * Psycho mode in the re-laid corner shows the flag and its trail only: the scrolling banner (renderPsycho's only
     * {@code drawTextWithShadow}) is not drawn, because on a narrow GUI it runs into the round clock at the top
     * centre. Wathe's own layout keeps it.
     * 重排后的左上角在疯魔模式下只显示旗帜及其残影：滚动横幅（renderPsycho 中唯一的 {@code drawTextWithShadow}）不再绘制，
     * 因为在较窄的 GUI 上它会压到顶部中央的回合时钟。Wathe 自身的布局保留横幅。
     */
    @WrapOperation(
            method = "renderPsycho",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
            ),
            require = 1,
            allow = 1
    )
    private static int sparkdlc$hidePsychoBanner(
            DrawContext context,
            TextRenderer textRenderer,
            Text text,
            int x,
            int y,
            int color,
            Operation<Integer> original
    ) {
        if (TopLeftHud.layout().relaid()) {
            return x;
        }
        return original.call(context, textRenderer, text, x, y, color);
    }

    /**
     * The psycho timer bar (renderPsycho's only {@code fill(IIIII)}) goes with the banner in the re-laid corner.
     * 疯魔计时条（renderPsycho 中唯一的 {@code fill(IIIII)}）在重排后的左上角与横幅一同不再绘制。
     */
    @WrapOperation(
            method = "renderPsycho",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"),
            require = 1,
            allow = 1
    )
    private static void sparkdlc$hidePsychoTimerBar(
            DrawContext context,
            int x1,
            int y1,
            int x2,
            int y2,
            int color,
            Operation<Void> original
    ) {
        if (!TopLeftHud.layout().relaid()) {
            original.call(context, x1, y1, x2, y2, color);
        }
    }

    @Inject(method = "renderHud", at = @At("TAIL"), require = 1, allow = 1)
    private static void sparkdlc$taskListHeader(
            PlayerEntity player,
            TextRenderer textRenderer,
            DrawContext context,
            RenderTickCounter tickCounter,
            CallbackInfo ci
    ) {
        TopLeftHud.renderHeader(context, textRenderer);
    }

    /** The civilian flag block (flag and arrows). / 平民旗帜区块（旗帜与箭头）。 */
    @WrapOperation(
            method = "renderCivilian",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
                    ordinal = 1
            ),
            require = 1,
            allow = 1
    )
    private static void sparkdlc$moveCivilianFlag(
            MatrixStack matrices,
            float x,
            float y,
            float z,
            Operation<Void> original
    ) {
        TopLeftHudLayout layout = TopLeftHud.layout();
        original.call(matrices, x + layout.flagDx(), layout.flagTranslateY(y), z);
    }

    /** The percentage, right after the flag and in its matrix. / 百分比：紧接旗帜之后，在旗帜的矩阵中绘制。 */
    @WrapOperation(
            method = "renderCivilian",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"
            ),
            require = 1,
            allow = 1
    )
    private static void sparkdlc$moodPercentBesideFlag(
            DrawContext context,
            Identifier sprite,
            int x,
            int y,
            int width,
            int height,
            Operation<Void> original
    ) {
        original.call(context, sprite, x, y, width, height);
        TopLeftHud.renderMoodPercent(context, x, y);
    }

    /**
     * The bar block no longer follows the rows by itself: the bar is hidden and the warning is placed below.
     * 理智条区块不再自行跟随任务行：理智条被隐藏，警告在下方另行定位。
     */
    @WrapOperation(
            method = "renderCivilian",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
                    ordinal = 5
            ),
            require = 1,
            allow = 1
    )
    private static void sparkdlc$releaseCivilianBarBlock(
            MatrixStack matrices,
            float x,
            float y,
            float z,
            Operation<Void> original
    ) {
        original.call(matrices, x, TopLeftHud.layout().relaid() ? 0.0F : y, z);
    }

    @WrapOperation(
            method = "renderCivilian",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"),
            require = 2,
            allow = 2
    )
    private static void sparkdlc$hideCivilianMoodBar(
            DrawContext context,
            int x1,
            int y1,
            int x2,
            int y2,
            int color,
            Operation<Void> original
    ) {
        if (!TopLeftHud.layout().relaid()) {
            original.call(context, x1, y1, x2, y2, color);
        }
    }

    /**
     * The breakdown warning, on the line under the task rows; the matrix still carries Wathe's shake.
     * 崩溃警告：位于任务行下方的那一行；矩阵中仍带有 Wathe 的抖动。
     */
    @WrapOperation(
            method = "renderCivilian",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
            ),
            require = 1,
            allow = 1
    )
    private static int sparkdlc$moveBreakdownWarning(
            DrawContext context,
            TextRenderer textRenderer,
            Text text,
            int x,
            int y,
            int color,
            Operation<Integer> original
    ) {
        TopLeftHudLayout layout = TopLeftHud.layout();
        if (!layout.relaid()) {
            return original.call(context, textRenderer, text, x, y, color);
        }
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(
                (float) (layout.taskX() - x),
                layout.belowRowsY(MoodRenderer.moodOffset, TopLeftHud.rowPresence()) - y,
                0.0F);
        int width = original.call(context, textRenderer, text, x, y, color);
        matrices.pop();
        return width;
    }

    /** The killer flag block. / 杀手旗帜区块。 */
    @WrapOperation(
            method = "renderKiller",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
                    ordinal = 0
            ),
            require = 1,
            allow = 1
    )
    private static void sparkdlc$moveKillerFlag(
            MatrixStack matrices,
            float x,
            float y,
            float z,
            Operation<Void> original
    ) {
        TopLeftHudLayout layout = TopLeftHud.layout();
        original.call(matrices, x + layout.flagDx(), layout.flagTranslateY(y), z);
    }

    @WrapOperation(
            method = "renderKiller",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"),
            require = 1,
            allow = 1
    )
    private static void sparkdlc$hideKillerMoodBar(
            DrawContext context,
            int x1,
            int y1,
            int x2,
            int y2,
            int color,
            Operation<Void> original
    ) {
        if (!TopLeftHud.layout().relaid()) {
            original.call(context, x1, y1, x2, y2, color);
        }
    }
}
