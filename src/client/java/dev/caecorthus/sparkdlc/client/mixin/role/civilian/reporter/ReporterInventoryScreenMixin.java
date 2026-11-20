package dev.caecorthus.sparkdlc.client.mixin.role.civilian.reporter;

import dev.caecorthus.sparkdlc.client.gui.widget.PlayerPageLayout;
import dev.caecorthus.sparkdlc.client.gui.widget.PlayerPageSwitchWidget;
import dev.caecorthus.sparkdlc.client.gui.widget.PlayerSelectionPageState;
import dev.caecorthus.sparkdlc.client.role.civilian.reporter.ReporterCommunicationPlayerWidget;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 给 Wathe 受限背包界面追加记者“接线 / 广播采访”头像栏。
 *
 * <p>这里不改 NoellesRolesspark 的记者本体，所有按钮和页码状态都放在 SparkStrength 内；
 * 服务端收到两次点击后再判断接线或广播，这样旧的 G 键标记透视逻辑和冷却完全独立。</p>
 */
@Mixin(LimitedInventoryScreen.class)
public abstract class ReporterInventoryScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow @Final public ClientPlayerEntity player;

    @Unique private final List<ReporterCommunicationPlayerWidget> sparkdlc$reporterPlayerWidgets = new ArrayList<>();
    @Unique private PlayerPageSwitchWidget sparkdlc$reporterPreviousPageWidget;
    @Unique private PlayerPageSwitchWidget sparkdlc$reporterNextPageWidget;
    @Unique private int sparkdlc$reporterCurrentPage;
    @Unique private boolean sparkdlc$reporterUiActive;

    public ReporterInventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void sparkdlc$reporterAddCommunicationButtons(CallbackInfo ci) {
        sparkdlc$reporterUiActive = false;
        ReporterCommunicationPlayerWidget.firstChoice = null;
        if (player == null || player.getWorld() == null) {
            return;
        }

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        if (!gameWorld.isRole(player, Noellesroles.REPORTER)) {
            return;
        }

        sparkdlc$reporterAddPlayerSelectionUI();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void sparkdlc$reporterRenderSelectionHint(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!sparkdlc$reporterUiActive) {
            return;
        }

        Text text = ReporterCommunicationPlayerWidget.firstChoice == null
                ? Text.translatable("ui.sparkdlc.reporter.first_player_selection")
                : Text.translatable("ui.sparkdlc.reporter.second_player_selection");
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int x = this.width / 2 - textRenderer.getWidth(text) / 2;
        int y = (this.height - 32) / 2 + 40;
        context.drawTextWithShadow(textRenderer, text, x, y, Noellesroles.REPORTER.color());
    }

    @Unique
    private void sparkdlc$reporterAddPlayerSelectionUI() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.networkHandler == null) {
            return;
        }

        /*
         * 复刻接线员的选择范围：使用在线网络列表并额外确保自己在列表中。
         * 目标是否死亡、是否被饕餮吞噬不在客户端过滤，避免 UI 泄露隐藏状态；
         * 服务端会在发包后统一决定成功、失败冷却和回放。
         */
        Set<UUID> targetUuids = new LinkedHashSet<>(client.player.networkHandler.getPlayerUuids());
        targetUuids.add(client.player.getUuid());

        int y = PlayerPageLayout.getPlayerRowY(this.height);
        sparkdlc$reporterPlayerWidgets.clear();
        sparkdlc$reporterCurrentPage = PlayerSelectionPageState.getReporterPage();
        sparkdlc$reporterUiActive = true;

        for (UUID targetUuid : targetUuids) {
            PlayerListEntry playerListEntry = client.player.networkHandler.getPlayerListEntry(targetUuid);
            ReporterCommunicationPlayerWidget child = new ReporterCommunicationPlayerWidget(
                    0,
                    y,
                    targetUuid,
                    playerListEntry
            );
            sparkdlc$reporterPlayerWidgets.add(child);
            addDrawableChild(child);
        }

        sparkdlc$reporterPreviousPageWidget = addDrawableChild(new PlayerPageSwitchWidget(
                0,
                y,
                Items.PURPLE_DYE.getDefaultStack(),
                Text.translatable("ui.sparkdlc.pagination.previous"),
                button -> {
                    sparkdlc$reporterCurrentPage--;
                    sparkdlc$reporterRefreshPage();
                }
        ));
        sparkdlc$reporterNextPageWidget = addDrawableChild(new PlayerPageSwitchWidget(
                0,
                y,
                Items.LIME_DYE.getDefaultStack(),
                Text.translatable("ui.sparkdlc.pagination.next"),
                button -> {
                    sparkdlc$reporterCurrentPage++;
                    sparkdlc$reporterRefreshPage();
                }
        ));

        sparkdlc$reporterRefreshPage();
    }

    @Unique
    private void sparkdlc$reporterRefreshPage() {
        int totalPages = PlayerPageLayout.getTotalPageCount(sparkdlc$reporterPlayerWidgets.size());
        if (sparkdlc$reporterCurrentPage < 0) {
            sparkdlc$reporterCurrentPage = 0;
        }
        if (sparkdlc$reporterCurrentPage >= totalPages) {
            sparkdlc$reporterCurrentPage = totalPages - 1;
        }
        PlayerSelectionPageState.setReporterPage(sparkdlc$reporterCurrentPage);

        int startIndex = sparkdlc$reporterCurrentPage * PlayerPageLayout.PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PlayerPageLayout.PLAYERS_PER_PAGE, sparkdlc$reporterPlayerWidgets.size());
        int visibleCount = endIndex - startIndex;
        int y = PlayerPageLayout.getPlayerRowY(this.height);
        boolean showPrevious = sparkdlc$reporterCurrentPage > 0;
        boolean showNext = sparkdlc$reporterCurrentPage < totalPages - 1;
        int groupStartX = PlayerPageLayout.getCenteredGroupStartX(this.width, visibleCount, showPrevious, showNext);
        int playerStartX = groupStartX + (showPrevious ? PlayerPageLayout.SLOT_APART : 0);

        for (int i = 0; i < sparkdlc$reporterPlayerWidgets.size(); i++) {
            ReporterCommunicationPlayerWidget widget = sparkdlc$reporterPlayerWidgets.get(i);
            boolean visible = i >= startIndex && i < endIndex;
            widget.visible = visible;
            widget.active = visible;
            if (visible) {
                int visibleIndex = i - startIndex;
                widget.setX(playerStartX + visibleIndex * PlayerPageLayout.SLOT_APART);
                widget.setY(y);
            }
        }

        if (sparkdlc$reporterPreviousPageWidget != null) {
            sparkdlc$reporterPreviousPageWidget.visible = showPrevious;
            sparkdlc$reporterPreviousPageWidget.active = showPrevious;
            sparkdlc$reporterPreviousPageWidget.setX(groupStartX);
            sparkdlc$reporterPreviousPageWidget.setY(y);
        }
        if (sparkdlc$reporterNextPageWidget != null) {
            sparkdlc$reporterNextPageWidget.visible = showNext;
            sparkdlc$reporterNextPageWidget.active = showNext;
            sparkdlc$reporterNextPageWidget.setX(playerStartX + visibleCount * PlayerPageLayout.SLOT_APART);
            sparkdlc$reporterNextPageWidget.setY(y);
        }
    }
}
