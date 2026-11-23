package dev.caecorthus.sparkdlc.client.block;

import dev.caecorthus.sparkdlc.block.NeonBlinkSettings;
import dev.caecorthus.sparkdlc.net.ApplyNeonPillarSettingsC2SPacket;
import dev.caecorthus.sparkdlc.net.OpenNeonPillarSettingsS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * Blink settings of one shining neon pillar column. The builder types how long the lamp stays lit and how long it
 * stays dark, in whole seconds; random mode ignores both and draws every phase from a fixed range. Turning blinking
 * off leaves the column steady and lit. The server decides everything again when Done is pressed.
 * <p>
 * 一根闪烁霓虹灯柱的闪烁设置。建造者以整秒输入灯亮多久、灭多久；随机模式忽略这两个值，每段相位都从固定区间里取值。关闭闪烁
 * 让灯柱保持常亮。点击完成后由服务端重新决定一切。
 */
public final class ShiningNeonPillarScreen extends Screen {
    private static final String KEY = "screen.sparkdlc.shining_neon_pillar.";
    private static final int PANEL_WIDTH = 200;

    private final BlockPos pos;
    private final NeonBlinkSettings initial;
    private boolean blinking;
    private boolean random;
    /** What the fields hold, kept across {@code init()} so a window resize does not lose it. / 输入框的内容，跨 {@code init()} 保留，窗口缩放时不会丢失。 */
    private String onText;
    private String offText;
    private TextFieldWidget onField;
    private TextFieldWidget offField;

    public ShiningNeonPillarScreen(OpenNeonPillarSettingsS2CPacket payload) {
        super(Text.translatable(KEY + "title"));
        this.pos = payload.pos();
        this.initial = payload.settings();
        this.blinking = payload.blinking();
        this.random = payload.settings().random();
        this.onText = Integer.toString(toSeconds(initial.onTicks()));
        this.offText = Integer.toString(toSeconds(initial.offTicks()));
    }

    @Override
    protected void init() {
        int left = this.width / 2 - PANEL_WIDTH / 2;
        int top = this.height / 2 - 60;
        this.onField = addDrawableChild(secondsField(left, top, KEY + "on_seconds", onText));
        this.onField.setChangedListener(text -> onText = text);
        this.offField = addDrawableChild(secondsField(left, top + 26, KEY + "off_seconds", offText));
        this.offField.setChangedListener(text -> offText = text);
        addDrawableChild(CyclingButtonWidget.onOffBuilder(blinking)
                .build(left, top + 52, PANEL_WIDTH, 20, Text.translatable(KEY + "blinking"),
                        (button, value) -> blinking = value));
        addDrawableChild(CyclingButtonWidget.onOffBuilder(random)
                .build(left, top + 78, PANEL_WIDTH, 20, Text.translatable(KEY + "random"),
                        (button, value) -> {
                            random = value;
                            updateFields();
                        }));
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> done())
                .dimensions(left, top + 110, PANEL_WIDTH / 2 - 4, 20).build());
        addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> close())
                .dimensions(left + PANEL_WIDTH / 2 + 4, top + 110, PANEL_WIDTH / 2 - 4, 20).build());
        updateFields();
    }

    private TextFieldWidget secondsField(int left, int y, String labelKey, String text) {
        TextFieldWidget field = new TextFieldWidget(this.textRenderer, left + PANEL_WIDTH - 80, y, 80, 20,
                Text.translatable(labelKey));
        field.setMaxLength(Integer.toString(NeonBlinkSettings.MAX_SECONDS).length());
        field.setTextPredicate(typed -> typed.chars().allMatch(c -> c >= '0' && c <= '9'));
        field.setText(text);
        return field;
    }

    /** Random mode ignores the durations, so their fields are read-only while it is on. / 随机模式忽略时长，开启时两个输入框只读。 */
    private void updateFields() {
        this.onField.setEditable(!random);
        this.offField.setEditable(!random);
    }

    private void done() {
        int on = toTicks(onText, initial.onTicks());
        int off = toTicks(offText, initial.offTicks());
        ClientPlayNetworking.send(new ApplyNeonPillarSettingsC2SPacket(pos, blinking,
                new NeonBlinkSettings(on, off, random)));
        close();
    }

    /** Seconds typed in the field, as ticks; an empty field keeps the value it opened with. / 输入框中的秒数换算为 tick；空输入框保留打开时的值。 */
    private static int toTicks(String text, int fallbackTicks) {
        if (text.isEmpty()) {
            return fallbackTicks;
        }
        int seconds = MathHelper.clamp(Integer.parseInt(text), NeonBlinkSettings.MIN_SECONDS, NeonBlinkSettings.MAX_SECONDS);
        return seconds * NeonBlinkSettings.TICKS_PER_SECOND;
    }

    private static int toSeconds(int ticks) {
        return MathHelper.clamp(Math.round(ticks / (float) NeonBlinkSettings.TICKS_PER_SECOND),
                NeonBlinkSettings.MIN_SECONDS, NeonBlinkSettings.MAX_SECONDS);
    }

    /** Labels are drawn after {@code super.render}, which draws the blurred background first. / 标签在 {@code super.render} 之后绘制，后者会先绘制模糊背景。 */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int left = this.width / 2 - PANEL_WIDTH / 2;
        int top = this.height / 2 - 60;
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, top - 22, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.translatable(KEY + "on_seconds"), left, top + 6, 0xE0E0E0);
        context.drawTextWithShadow(this.textRenderer, Text.translatable(KEY + "off_seconds"), left, top + 32, 0xE0E0E0);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable(KEY + "hint",
                NeonBlinkSettings.MIN_SECONDS, NeonBlinkSettings.MAX_SECONDS), this.width / 2, top + 138, 0x9A9A9A);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable(KEY + "random_hint",
                NeonBlinkSettings.RANDOM_MIN_SECONDS, NeonBlinkSettings.RANDOM_MAX_SECONDS), this.width / 2, top + 150,
                0x9A9A9A);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
