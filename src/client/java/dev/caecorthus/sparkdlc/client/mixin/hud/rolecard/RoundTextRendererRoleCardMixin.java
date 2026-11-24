package dev.caecorthus.sparkdlc.client.mixin.hud.rolecard;

import dev.caecorthus.sparkdlc.client.hud.rolecard.RoleCardState;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.client.gui.RoundTextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wathe seam: every welcome ({@code AnnounceWelcomePayload} -> {@code RoundTextRenderer.startWelcome}) starts the
 * round-start role card's timer, so the card follows Wathe's own welcome timing and restarts when a welcome is sent
 * again. Observes only; the welcome itself is untouched.
 * Wathe 接缝：每次欢迎词（{@code AnnounceWelcomePayload} -> {@code RoundTextRenderer.startWelcome}）都会启动开局身份卡的
 * 计时器，使卡片跟随 Wathe 自己的欢迎节奏，并在欢迎词再次发送时重新开始。只观察，不改动欢迎词本身。
 */
@Mixin(value = RoundTextRenderer.class, remap = false)
public abstract class RoundTextRendererRoleCardMixin {
    @Inject(method = "startWelcome", at = @At("TAIL"))
    private static void sparkdlc$startRoleCard(RoleAnnouncementTexts.RoleAnnouncementText role, int killers,
                                               int targets, CallbackInfo ci) {
        RoleCardState.onWelcome();
    }
}
