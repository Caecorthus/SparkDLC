package dev.caecorthus.sparkdlc.client.mixin.role.civilian.professor;

import dev.caecorthus.sparkdlc.client.gui.widget.PlayerPageLayout;
import dev.caecorthus.sparkdlc.client.gui.widget.PlayerPageSwitchWidget;
import dev.caecorthus.sparkdlc.client.gui.widget.PlayerSelectionPageState;
import dev.caecorthus.sparkdlc.client.role.civilian.professor.ProfessorRemoteFeedPlayerWidget;
import dev.caecorthus.sparkdlc.client.role.civilian.professor.ProfessorSerumButtonWidget;
import dev.caecorthus.sparkdlc.role.civilian.professor.ProfessorSerumRules;
import dev.caecorthus.sparkdlc.role.civilian.professor.ProfessorSerumType;
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
 * 给教授背包添加“远程投喂”两级按钮。
 *
 * <p>第一页显示所有在线玩家头像；点击头像后切到第二页，显示四种试剂按钮。
 * 这套 UI 只负责采集选择，最终是否成功由服务器再次检查，避免客户端靠改包绕过限制。</p>
 */
@Mixin(LimitedInventoryScreen.class)
public abstract class ProfessorInventoryScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow @Final public ClientPlayerEntity player;

    @Unique private final List<ProfessorRemoteFeedPlayerWidget> sparkdlc$professorPlayerWidgets = new ArrayList<>();
    @Unique private final List<ProfessorSerumButtonWidget> sparkdlc$professorSerumWidgets = new ArrayList<>();
    @Unique private PlayerPageSwitchWidget sparkdlc$professorPreviousPageWidget;
    @Unique private PlayerPageSwitchWidget sparkdlc$professorNextPageWidget;
    @Unique private PlayerPageSwitchWidget sparkdlc$professorBackWidget;
    @Unique private int sparkdlc$professorCurrentPage;
    @Unique private UUID sparkdlc$professorSelectedTarget;
    @Unique private boolean sparkdlc$professorSelectingSerum;

    public ProfessorInventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void sparkdlc$professorAddRemoteFeedButtons(CallbackInfo ci) {
        if (player == null || player.getWorld() == null) {
            return;
        }

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        if (!ProfessorSerumRules.isProfessor(gameWorld.getRole(player))) {
            return;
        }

        sparkdlc$professorAddPlayerSelectionUI();
    }

    @Unique
    private void sparkdlc$professorAddPlayerSelectionUI() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.networkHandler == null) {
            return;
        }

        List<UUID> playerUuids = new ArrayList<>(client.player.networkHandler.getPlayerUuids());
        int y = PlayerPageLayout.getPlayerRowY(this.height);
        sparkdlc$professorPlayerWidgets.clear();
        sparkdlc$professorSerumWidgets.clear();
        sparkdlc$professorCurrentPage = PlayerSelectionPageState.getProfessorPage();
        sparkdlc$professorSelectingSerum = false;
        sparkdlc$professorSelectedTarget = null;

        for (UUID targetUuid : playerUuids) {
            PlayerListEntry playerListEntry = client.player.networkHandler.getPlayerListEntry(targetUuid);
            ProfessorRemoteFeedPlayerWidget child = new ProfessorRemoteFeedPlayerWidget(
                    0,
                    y,
                    targetUuid,
                    playerListEntry,
                    button -> sparkdlc$professorOpenSerumSelection(targetUuid)
            );
            sparkdlc$professorPlayerWidgets.add(child);
            addDrawableChild(child);
        }

        sparkdlc$professorPreviousPageWidget = addDrawableChild(new PlayerPageSwitchWidget(
                0,
                y,
                Items.PURPLE_DYE.getDefaultStack(),
                Text.translatable("ui.sparkdlc.pagination.previous"),
                button -> {
                    sparkdlc$professorCurrentPage--;
                    sparkdlc$professorRefreshPage();
                }
        ));
        sparkdlc$professorNextPageWidget = addDrawableChild(new PlayerPageSwitchWidget(
                0,
                y,
                Items.LIME_DYE.getDefaultStack(),
                Text.translatable("ui.sparkdlc.pagination.next"),
                button -> {
                    sparkdlc$professorCurrentPage++;
                    sparkdlc$professorRefreshPage();
                }
        ));
        sparkdlc$professorBackWidget = addDrawableChild(new PlayerPageSwitchWidget(
                0,
                y,
                Items.ARROW.getDefaultStack(),
                Text.translatable("ui.sparkdlc.professor.back_to_players"),
                button -> sparkdlc$professorOpenPlayerSelection()
        ));

        int serumStartX = this.width / 2 - (ProfessorSerumType.BACKPACK_ORDER.size() * PlayerPageLayout.SLOT_APART) / 2
                + PlayerPageLayout.SLOT_X_OFFSET;
        for (int i = 0; i < ProfessorSerumType.BACKPACK_ORDER.size(); i++) {
            ProfessorSerumButtonWidget widget = new ProfessorSerumButtonWidget(
                    serumStartX + i * PlayerPageLayout.SLOT_APART,
                    y,
                    player.getUuid(),
                    ProfessorSerumType.BACKPACK_ORDER.get(i)
            );
            sparkdlc$professorSerumWidgets.add(widget);
            addDrawableChild(widget);
        }

        sparkdlc$professorRefreshPage();
    }

    @Unique
    private void sparkdlc$professorOpenSerumSelection(UUID targetUuid) {
        sparkdlc$professorSelectedTarget = targetUuid;
        sparkdlc$professorSelectingSerum = true;
        sparkdlc$professorRefreshPage();
    }

    @Unique
    private void sparkdlc$professorOpenPlayerSelection() {
        sparkdlc$professorSelectedTarget = null;
        sparkdlc$professorSelectingSerum = false;
        sparkdlc$professorRefreshPage();
    }

    @Unique
    private void sparkdlc$professorRefreshPage() {
        if (sparkdlc$professorSelectingSerum) {
            sparkdlc$professorRefreshSerumSelection();
            return;
        }

        int totalPages = PlayerPageLayout.getTotalPageCount(sparkdlc$professorPlayerWidgets.size());
        if (sparkdlc$professorCurrentPage < 0) {
            sparkdlc$professorCurrentPage = 0;
        }
        if (sparkdlc$professorCurrentPage >= totalPages) {
            sparkdlc$professorCurrentPage = totalPages - 1;
        }
        PlayerSelectionPageState.setProfessorPage(sparkdlc$professorCurrentPage);

        int startIndex = sparkdlc$professorCurrentPage * PlayerPageLayout.PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PlayerPageLayout.PLAYERS_PER_PAGE, sparkdlc$professorPlayerWidgets.size());
        int visibleCount = endIndex - startIndex;
        int y = PlayerPageLayout.getPlayerRowY(this.height);
        boolean showPrevious = sparkdlc$professorCurrentPage > 0;
        boolean showNext = sparkdlc$professorCurrentPage < totalPages - 1;
        int groupStartX = PlayerPageLayout.getCenteredGroupStartX(this.width, visibleCount, showPrevious, showNext);
        int playerStartX = groupStartX + (showPrevious ? PlayerPageLayout.SLOT_APART : 0);

        for (int i = 0; i < sparkdlc$professorPlayerWidgets.size(); i++) {
            ProfessorRemoteFeedPlayerWidget widget = sparkdlc$professorPlayerWidgets.get(i);
            boolean visible = i >= startIndex && i < endIndex;
            widget.visible = visible;
            widget.active = visible;
            if (visible) {
                int visibleIndex = i - startIndex;
                widget.setX(playerStartX + visibleIndex * PlayerPageLayout.SLOT_APART);
                widget.setY(y);
            }
        }

        for (ProfessorSerumButtonWidget widget : sparkdlc$professorSerumWidgets) {
            widget.visible = false;
            widget.active = false;
        }
        if (sparkdlc$professorBackWidget != null) {
            sparkdlc$professorBackWidget.visible = false;
            sparkdlc$professorBackWidget.active = false;
        }
        if (sparkdlc$professorPreviousPageWidget != null) {
            sparkdlc$professorPreviousPageWidget.visible = showPrevious;
            sparkdlc$professorPreviousPageWidget.active = showPrevious;
            sparkdlc$professorPreviousPageWidget.setX(groupStartX);
            sparkdlc$professorPreviousPageWidget.setY(y);
        }
        if (sparkdlc$professorNextPageWidget != null) {
            sparkdlc$professorNextPageWidget.visible = showNext;
            sparkdlc$professorNextPageWidget.active = showNext;
            sparkdlc$professorNextPageWidget.setX(playerStartX + visibleCount * PlayerPageLayout.SLOT_APART);
            sparkdlc$professorNextPageWidget.setY(y);
        }
    }

    @Unique
    private void sparkdlc$professorRefreshSerumSelection() {
        int y = PlayerPageLayout.getPlayerRowY(this.height);
        int totalSlots = ProfessorSerumType.BACKPACK_ORDER.size() + 1;
        int groupStartX = this.width / 2 - totalSlots * PlayerPageLayout.SLOT_APART / 2
                + PlayerPageLayout.SLOT_X_OFFSET;
        UUID targetUuid = sparkdlc$professorSelectedTarget;

        for (ProfessorRemoteFeedPlayerWidget widget : sparkdlc$professorPlayerWidgets) {
            widget.visible = false;
            widget.active = false;
        }
        if (sparkdlc$professorPreviousPageWidget != null) {
            sparkdlc$professorPreviousPageWidget.visible = false;
            sparkdlc$professorPreviousPageWidget.active = false;
        }
        if (sparkdlc$professorNextPageWidget != null) {
            sparkdlc$professorNextPageWidget.visible = false;
            sparkdlc$professorNextPageWidget.active = false;
        }
        if (sparkdlc$professorBackWidget != null) {
            sparkdlc$professorBackWidget.visible = true;
            sparkdlc$professorBackWidget.active = true;
            sparkdlc$professorBackWidget.setX(groupStartX);
            sparkdlc$professorBackWidget.setY(y);
        }

        for (int i = 0; i < sparkdlc$professorSerumWidgets.size(); i++) {
            ProfessorSerumButtonWidget widget = sparkdlc$professorSerumWidgets.get(i);
            if (targetUuid == null) {
                widget.visible = false;
                widget.active = false;
                continue;
            }

            widget.setTargetUuid(targetUuid);
            widget.visible = true;
            widget.active = true;
            widget.setX(groupStartX + (i + 1) * PlayerPageLayout.SLOT_APART);
            widget.setY(y);
        }
    }
}
