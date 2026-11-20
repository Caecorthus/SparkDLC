package dev.caecorthus.sparkdlc.client.mixin.scope;

import dev.caecorthus.sparkdlc.client.settings.SparkDlcSettingsApi;
import dev.caecorthus.sparkdlc.client.scope.ScopeOptions;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds "Scope View", "Lens Resolution" and "Scoped Sensitivity" at the end of vanilla Options → Accessibility (owner
 * decision Q3; Sodium replaces Video Settings). It appends rows to the option list at {@code addOptions} TAIL rather
 * than changing {@code getOptions}' return: a second cancellable or return-modifying hook on that return (the
 * settings feature once had a cancellable RETURN {@code @Inject} there) could be skipped, depending on apply order.
 * The rows are left out while the SparkDLC settings screen hosts these settings
 * ({@link SparkDlcSettingsApi#hostsScopeSettings()}, asked at each screen init; that screen edits them through
 * {@code client.scope.ScopeSettingsEditor}); it always does, so adding them is unreachable since the fusion.
 * Client-only; the options are never written to {@code options.txt}.
 * 在原版「选项 → 辅助功能」末尾加入「开镜画面」「镜内分辨率」与「开镜灵敏度」（所有者决定 Q3；Sodium 会替换视频设置）。
 * 在 {@code addOptions} 的 TAIL 向选项列表追加选项行，而不修改 {@code getOptions} 的返回值：同一返回点上的第二个可取消或
 * 改返回值的钩子（设置功能曾在那里有一个可取消的 RETURN {@code @Inject}）可能因应用顺序被跳过。当 SparkDLC 设置界面
 * 承载这些设置时（{@link SparkDlcSettingsApi#hostsScopeSettings()}，每次界面初始化时询问；该界面通过
 * {@code client.scope.ScopeSettingsEditor} 编辑它们）不加入这些行；它恒承载这些设置，因此加入这些行在融合后不可达。
 * 纯客户端；这些选项从不写入 {@code options.txt}。
 */
@Mixin(AccessibilityOptionsScreen.class)
public abstract class ScopeAccessibilityOptionsMixin extends GameOptionsScreen {
    private ScopeAccessibilityOptionsMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void sparkdlc$appendScopeOptions(CallbackInfo ci) {
        if (this.body != null && !SparkDlcSettingsApi.hostsScopeSettings()) {
            ScopeOptions.addTo(this.body);
        }
    }
}
