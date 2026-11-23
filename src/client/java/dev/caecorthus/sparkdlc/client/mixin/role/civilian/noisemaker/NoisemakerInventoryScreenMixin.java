package dev.caecorthus.sparkdlc.client.mixin.role.civilian.noisemaker;

import dev.caecorthus.sparkdlc.client.role.civilian.noisemaker.NoisemakerGlowPlayerWidget;
import dev.caecorthus.sparkdlc.client.gui.widget.PlayerPageLayout;
import dev.caecorthus.sparkdlc.client.gui.widget.PlayerPageSwitchWidget;
import dev.caecorthus.sparkdlc.client.gui.widget.PlayerSelectionPageState;
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
import org.agmas.noellesroles.Noellesroles;
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
 * 给 Wathe 的受限背包界面追加大嗓门“点亮玩家”头像栏。
 *
 * <p>这里不修改 NoellesRolesspark 本体，而是在本模组里单独混入
 * {@link LimitedInventoryScreen}。所有字段和方法都使用 {@code sparkdlc$noisemaker}
 * 前缀，避免和其它扩展 mod 也混同一个背包界面时发生名字碰撞。</p>
 */
@Mixin(LimitedInventoryScreen.class)
public abstract class NoisemakerInventoryScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow @Final public ClientPlayerEntity player;

    @Unique private final List<NoisemakerGlowPlayerWidget> sparkdlc$noisemakerPlayerWidgets = new ArrayList<>();
    @Unique private PlayerPageSwitchWidget sparkdlc$noisemakerPreviousPageWidget;
    @Unique private PlayerPageSwitchWidget sparkdlc$noisemakerNextPageWidget;
    @Unique private int sparkdlc$noisemakerCurrentPage;

    public NoisemakerInventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void sparkdlc$noisemakerAddGlowButtons(CallbackInfo ci) {
        if (player == null || player.getWorld() == null) {
            return;
        }

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        if (!gameWorld.isRole(player, Noellesroles.NOISEMAKER)) {
            return;
        }

        sparkdlc$noisemakerAddPlayerSelectionUI();
    }

    @Unique
    private void sparkdlc$noisemakerAddPlayerSelectionUI() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.networkHandler == null) {
            return;
        }

        // 使用 Tab/网络列表里的 UUID，而不是只读取世界实体。
        // 这样玩家死亡变旁观后头像仍然存在，点击死亡玩家也会被服务端扣冷却。
        List<UUID> playerUuids = new ArrayList<>(client.player.networkHandler.getPlayerUuids());
        int y = PlayerPageLayout.getPlayerRowY(this.height);
        sparkdlc$noisemakerPlayerWidgets.clear();
        sparkdlc$noisemakerCurrentPage = PlayerSelectionPageState.getNoisemakerPage();

        for (UUID targetUuid : playerUuids) {
            PlayerListEntry playerListEntry = client.player.networkHandler.getPlayerListEntry(targetUuid);
            NoisemakerGlowPlayerWidget child = new NoisemakerGlowPlayerWidget(
                    0,
                    y,
                    targetUuid,
                    playerListEntry
            );
            sparkdlc$noisemakerPlayerWidgets.add(child);
            addDrawableChild(child);
        }

        sparkdlc$noisemakerPreviousPageWidget = addDrawableChild(new PlayerPageSwitchWidget(
                0,
                y,
                Items.PURPLE_DYE.getDefaultStack(),
                Text.translatable("ui.sparkdlc.pagination.previous"),
                button -> {
                    sparkdlc$noisemakerCurrentPage--;
                    sparkdlc$noisemakerRefreshPage();
                }
        ));
        sparkdlc$noisemakerNextPageWidget = addDrawableChild(new PlayerPageSwitchWidget(
                0,
                y,
                Items.LIME_DYE.getDefaultStack(),
                Text.translatable("ui.sparkdlc.pagination.next"),
                button -> {
                    sparkdlc$noisemakerCurrentPage++;
                    sparkdlc$noisemakerRefreshPage();
                }
        ));

        sparkdlc$noisemakerRefreshPage();
    }

    @Unique
    private void sparkdlc$noisemakerRefreshPage() {
        int totalPages = PlayerPageLayout.getTotalPageCount(sparkdlc$noisemakerPlayerWidgets.size());
        if (sparkdlc$noisemakerCurrentPage < 0) {
            sparkdlc$noisemakerCurrentPage = 0;
        }
        if (sparkdlc$noisemakerCurrentPage >= totalPages) {
            sparkdlc$noisemakerCurrentPage = totalPages - 1;
        }
        PlayerSelectionPageState.setNoisemakerPage(sparkdlc$noisemakerCurrentPage);

        int startIndex = sparkdlc$noisemakerCurrentPage * PlayerPageLayout.PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PlayerPageLayout.PLAYERS_PER_PAGE, sparkdlc$noisemakerPlayerWidgets.size());
        int visibleCount = endIndex - startIndex;
        int y = PlayerPageLayout.getPlayerRowY(this.height);
        boolean showPrevious = sparkdlc$noisemakerCurrentPage > 0;
        boolean showNext = sparkdlc$noisemakerCurrentPage < totalPages - 1;
        int groupStartX = PlayerPageLayout.getCenteredGroupStartX(this.width, visibleCount, showPrevious, showNext);
        int playerStartX = groupStartX + (showPrevious ? PlayerPageLayout.SLOT_APART : 0);

        for (int i = 0; i < sparkdlc$noisemakerPlayerWidgets.size(); i++) {
            NoisemakerGlowPlayerWidget widget = sparkdlc$noisemakerPlayerWidgets.get(i);
            boolean visible = i >= startIndex && i < endIndex;
            widget.visible = visible;
            widget.active = visible;
            if (visible) {
                int visibleIndex = i - startIndex;
                widget.setX(playerStartX + visibleIndex * PlayerPageLayout.SLOT_APART);
                widget.setY(y);
            }
        }

        if (sparkdlc$noisemakerPreviousPageWidget != null) {
            sparkdlc$noisemakerPreviousPageWidget.visible = showPrevious;
            sparkdlc$noisemakerPreviousPageWidget.active = showPrevious;
            sparkdlc$noisemakerPreviousPageWidget.setX(groupStartX);
            sparkdlc$noisemakerPreviousPageWidget.setY(y);
        }
        if (sparkdlc$noisemakerNextPageWidget != null) {
            sparkdlc$noisemakerNextPageWidget.visible = showNext;
            sparkdlc$noisemakerNextPageWidget.active = showNext;
            sparkdlc$noisemakerNextPageWidget.setX(playerStartX + visibleCount * PlayerPageLayout.SLOT_APART);
            sparkdlc$noisemakerNextPageWidget.setY(y);
        }
    }
}
