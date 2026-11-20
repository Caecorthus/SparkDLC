package dev.caecorthus.sparkdlc.client.mixin.role.civilian.coroner;

import dev.caecorthus.sparkdlc.client.gui.widget.PlayerPageLayout;
import dev.caecorthus.sparkdlc.client.gui.widget.PlayerPageSwitchWidget;
import dev.caecorthus.sparkdlc.client.gui.widget.PlayerSelectionPageState;
import dev.caecorthus.sparkdlc.client.role.civilian.coroner.CoronerPlayerWidget;
import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerRules;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 给 Wathe 受限背包界面追加验尸官“尸体身份变形”头像栏。
 *
 * <p>和召集者保持同一交互模型：自己头像永远存在，刚开局只能点自己；
 * 每用采尸袋收走一具尸体，就会多一个尸体原主头像按钮。</p>
 */
@Mixin(LimitedInventoryScreen.class)
public abstract class CoronerInventoryScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow @Final public ClientPlayerEntity player;

    @Unique private final List<CoronerPlayerWidget> sparkdlc$coronerPlayerWidgets = new ArrayList<>();
    @Unique private PlayerPageSwitchWidget sparkdlc$coronerPreviousPageWidget;
    @Unique private PlayerPageSwitchWidget sparkdlc$coronerNextPageWidget;
    @Unique private int sparkdlc$coronerCurrentPage;

    public CoronerInventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void sparkdlc$coronerAddMorphButtons(CallbackInfo ci) {
        if (player == null || player.getWorld() == null) {
            return;
        }

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        if (!CoronerRules.isCoroner(gameWorld.getRole(player))) {
            return;
        }

        sparkdlc$coronerAddPlayerSelectionUI();
    }

    @Unique
    private void sparkdlc$coronerAddPlayerSelectionUI() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.networkHandler == null) {
            return;
        }

        List<UUID> targetUuids = sparkdlc$coronerCollectTargets(player);
        int y = PlayerPageLayout.getPlayerRowY(this.height);
        sparkdlc$coronerPlayerWidgets.clear();
        sparkdlc$coronerCurrentPage = PlayerSelectionPageState.getCoronerPage();

        for (UUID targetUuid : targetUuids) {
            PlayerListEntry playerListEntry = client.player.networkHandler.getPlayerListEntry(targetUuid);
            CoronerPlayerWidget child = new CoronerPlayerWidget(
                    0,
                    y,
                    targetUuid,
                    targetUuid.equals(player.getUuid()),
                    playerListEntry
            );
            sparkdlc$coronerPlayerWidgets.add(child);
            addDrawableChild(child);
        }

        sparkdlc$coronerPreviousPageWidget = addDrawableChild(new PlayerPageSwitchWidget(
                0,
                y,
                Items.PURPLE_DYE.getDefaultStack(),
                Text.translatable("ui.sparkdlc.pagination.previous"),
                button -> {
                    sparkdlc$coronerCurrentPage--;
                    sparkdlc$coronerRefreshPage();
                }
        ));
        sparkdlc$coronerNextPageWidget = addDrawableChild(new PlayerPageSwitchWidget(
                0,
                y,
                Items.LIME_DYE.getDefaultStack(),
                Text.translatable("ui.sparkdlc.pagination.next"),
                button -> {
                    sparkdlc$coronerCurrentPage++;
                    sparkdlc$coronerRefreshPage();
                }
        ));

        sparkdlc$coronerRefreshPage();
    }

    @Unique
    private List<UUID> sparkdlc$coronerCollectTargets(ClientPlayerEntity player) {
        List<UUID> targetUuids = new ArrayList<>();
        targetUuids.add(player.getUuid());
        targetUuids.addAll(CoronerPlayerComponent.KEY.get(player).unlockedBodyUuids());
        return targetUuids;
    }

    @Unique
    private void sparkdlc$coronerRefreshPage() {
        int totalPages = PlayerPageLayout.getTotalPageCount(sparkdlc$coronerPlayerWidgets.size());
        if (sparkdlc$coronerCurrentPage < 0) {
            sparkdlc$coronerCurrentPage = 0;
        }
        if (sparkdlc$coronerCurrentPage >= totalPages) {
            sparkdlc$coronerCurrentPage = totalPages - 1;
        }
        PlayerSelectionPageState.setCoronerPage(sparkdlc$coronerCurrentPage);

        int startIndex = sparkdlc$coronerCurrentPage * PlayerPageLayout.PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PlayerPageLayout.PLAYERS_PER_PAGE, sparkdlc$coronerPlayerWidgets.size());
        int visibleCount = endIndex - startIndex;
        int y = PlayerPageLayout.getPlayerRowY(this.height);
        boolean showPrevious = sparkdlc$coronerCurrentPage > 0;
        boolean showNext = sparkdlc$coronerCurrentPage < totalPages - 1;
        int groupStartX = PlayerPageLayout.getCenteredGroupStartX(this.width, visibleCount, showPrevious, showNext);
        int playerStartX = groupStartX + (showPrevious ? PlayerPageLayout.SLOT_APART : 0);

        for (int i = 0; i < sparkdlc$coronerPlayerWidgets.size(); i++) {
            CoronerPlayerWidget widget = sparkdlc$coronerPlayerWidgets.get(i);
            boolean visible = i >= startIndex && i < endIndex;
            widget.visible = visible;
            widget.active = visible;
            if (visible) {
                int visibleIndex = i - startIndex;
                widget.setX(playerStartX + visibleIndex * PlayerPageLayout.SLOT_APART);
                widget.setY(y);
            }
        }

        if (sparkdlc$coronerPreviousPageWidget != null) {
            sparkdlc$coronerPreviousPageWidget.visible = showPrevious;
            sparkdlc$coronerPreviousPageWidget.active = showPrevious;
            sparkdlc$coronerPreviousPageWidget.setX(groupStartX);
            sparkdlc$coronerPreviousPageWidget.setY(y);
        }
        if (sparkdlc$coronerNextPageWidget != null) {
            sparkdlc$coronerNextPageWidget.visible = showNext;
            sparkdlc$coronerNextPageWidget.active = showNext;
            sparkdlc$coronerNextPageWidget.setX(playerStartX + visibleCount * PlayerPageLayout.SLOT_APART);
            sparkdlc$coronerNextPageWidget.setY(y);
        }
    }
}
