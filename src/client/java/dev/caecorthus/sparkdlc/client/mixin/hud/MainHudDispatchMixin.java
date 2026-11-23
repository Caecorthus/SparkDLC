package dev.caecorthus.sparkdlc.client.mixin.hud;

import dev.caecorthus.sparkdlc.client.hud.RoleHudDispatcher;
import dev.caecorthus.sparkdlc.client.role.civilian.bodyguard.BodyguardHud;
import dev.caecorthus.sparkdlc.client.role.killer.bomber.DronePilotHud;
import dev.caecorthus.sparkdlc.client.role.jiahao.corruptcop.CorruptCopAbilityHud;
import dev.caecorthus.sparkdlc.client.role.civilian.demonhunter.DemonHunterSniffHudRenderer;
import dev.caecorthus.sparkdlc.client.role.civilian.detective.DetectiveMagnifierHudRenderer;
import dev.caecorthus.sparkdlc.client.role.killer.partyanimal.PartygoerHud;
import dev.caecorthus.sparkdlc.client.role.civilian.spiritualist.SpiritPossessionHud;
import dev.caecorthus.sparkdlc.client.role.neutral.taotie.TaotieHeadHudRenderer;
import dev.caecorthus.sparkdlc.client.role.neutral.vulture.SkateboardHud;
import dev.caecorthus.sparkdlc.client.role.neutral.vulture.VultureSuperCurseHud;
import dev.caecorthus.sparkdlc.client.trait.hud.DepressionHud;
import dev.caecorthus.sparkdlc.client.trait.hud.PreparefullHud;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Every line the mod appends to the main HUD pass, from one TAIL hook (gated by F1 like the rest of the main HUD): the
 * role-kit lines, the Depression and Preparefull trait lines, then the role HUD panels of
 * {@link RoleHudDispatcher#renderMainHudTail}. Each feature keeps its own checks; the order below is the order their
 * separate TAIL hooks ran in (same-point injectors of one config run in mixin config order, and no other entry of the
 * client config injects at this TAIL), so later lines still draw over earlier ones. Default priority 1000 in the JSON
 * slot of the first hook it replaced; Mixin orders a target's mixins by priority, then declaration order, and the
 * entries of one config are contiguous in that order, so the block keeps its place relative to other mods'
 * renderMainHud TAIL injects such as Wathe's own HUD.
 * 本模组追加到主 HUD 渲染流程的每一行，由一个 TAIL 钩子统一调用（与主 HUD 其他部分一样受 F1 控制）：职业套件各行、
 * 抑郁与养精蓄锐词条的行，然后是 {@link RoleHudDispatcher#renderMainHudTail} 的职业 HUD 面板。各功能保留自己的判断；
 * 下列顺序即原先各自 TAIL 钩子的执行顺序（同一配置中同一注入点的注入器按 mixin 配置顺序执行，且客户端配置中没有其他条目
 * 注入此 TAIL），因此后面的行仍然画在前面的行之上。默认优先级 1000，占用其所替换的第一个钩子的 JSON 位置；Mixin 先按
 * 优先级、再按声明顺序排列同一目标的 mixin，同一配置的条目在该顺序中相邻，因此整体相对其他模组的 renderMainHud TAIL
 * 注入（如 Wathe 自身 HUD）的位置不变。
 */
@Mixin(InGameHud.class)
public abstract class MainHudDispatchMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderMainHud", at = @At("TAIL"))
    private void sparkdlc$renderMainHudLines(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ClientPlayerEntity player = client.player;
        if (player != null) {
            BodyguardHud.render(client.textRenderer, player, context);
        }
        DronePilotHud.renderInstruments(context, tickCounter);
        if (player != null) {
            CorruptCopAbilityHud.render(client.textRenderer, player, context);
        }
        SpiritPossessionHud.render(context, player);
        if (player != null) {
            TaotieHeadHudRenderer.render(context, player);
            PartygoerHud.render(client.textRenderer, player, context);
            DetectiveMagnifierHudRenderer.render(context, player);
            DemonHunterSniffHudRenderer.render(context, player);
            SkateboardHud.render(client.textRenderer, player, context);
            VultureSuperCurseHud.render(client.textRenderer, player, context);
        }
        // Was trait.DepressionInGameHudMixin: Depression's private timer, on a confirmed server only.
        // 原 trait.DepressionInGameHudMixin：抑郁的私有倒计时，仅限已确认的服务器。
        if (SparkDlcServerConnection.isConfirmedServer() && client.player != null) {
            DepressionHud.render(client.textRenderer, client.player, context, tickCounter.getTickDelta(true));
        }
        // Was trait.PreparefullInGameHudMixin: the 养精蓄锐 charge percentage, on a confirmed server only.
        // 原 trait.PreparefullInGameHudMixin：养精蓄锐的蓄力百分比，仅限已确认的服务器。
        if (SparkDlcServerConnection.isConfirmedServer() && client.player != null) {
            PreparefullHud.render(client.textRenderer, client.player, context);
        }
        // Was faction.witch.RoleHudMainHudTailMixin: the role HUD panels hidden with F1, layers and gates in the
        // dispatcher. / 原 faction.witch.RoleHudMainHudTailMixin：F1 时隐藏的职业 HUD 面板，各层与门槛在分发器中。
        RoleHudDispatcher.renderMainHudTail(context);
    }
}
