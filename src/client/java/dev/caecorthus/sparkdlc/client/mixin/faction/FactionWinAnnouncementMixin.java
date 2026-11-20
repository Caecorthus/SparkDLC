package dev.caecorthus.sparkdlc.client.mixin.faction;

import dev.caecorthus.sparkdlc.faction.api.FactionDefinition;
import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.component.FactionRoundEndComponent;
import dev.caecorthus.sparkdlc.faction.text.FactionRoundEndTextRules;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.client.gui.RoundTextRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

/**
 * Shows custom faction wins with faction text instead of the first winning role's text.
 * 自定义阵营胜利时显示阵营公告，而不是第一个胜者角色的公告。
 */
@Mixin(RoundTextRenderer.class)
public abstract class FactionWinAnnouncementMixin {
    @Redirect(
            method = "renderHud",
            at = @At(
                    value = "FIELD",
                    target = "Ldev/doctor4t/wathe/client/gui/RoleAnnouncementTexts$RoleAnnouncementText;winText:Lnet/minecraft/text/Text;"
            )
    )
    private static Text sparkdlc$customFactionWinTitle(
            RoleAnnouncementTexts.RoleAnnouncementText roleText,
            TextRenderer renderer,
            ClientPlayerEntity player,
            DrawContext context
    ) {
        return customWinningFaction(player)
                .map(FactionRoundEndTextRules::winTitle)
                .map(Text.class::cast)
                .orElse(roleText.winText);
    }

    @Redirect(
            method = "renderHud",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Identifier;getPath()Ljava/lang/String;"
            )
    )
    private static String sparkdlc$customFactionWinPhrasePath(
            Identifier roleId,
            TextRenderer renderer,
            ClientPlayerEntity player,
            DrawContext context
    ) {
        return customWinningFaction(player)
                .map(FactionDefinition::id)
                .map(FactionRoundEndTextRules::winPhrasePath)
                .orElseGet(roleId::getPath);
    }

    private static Optional<FactionDefinition> customWinningFaction(ClientPlayerEntity player) {
        if (player == null) {
            return Optional.empty();
        }
        FactionRoundEndComponent roundEnd = FactionRoundEndComponent.KEY.get(player.getScoreboard());
        Identifier winningFaction = roundEnd.getWinningFaction();
        if (winningFaction == null) {
            return Optional.empty();
        }
        return SparkFactionApi.getFaction(winningFaction);
    }
}
