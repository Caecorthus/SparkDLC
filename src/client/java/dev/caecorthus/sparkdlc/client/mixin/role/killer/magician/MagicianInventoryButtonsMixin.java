package dev.caecorthus.sparkdlc.client.mixin.role.killer.magician;

import dev.caecorthus.sparkdlc.client.role.killer.magician.MagicianTargetWidget;
import dev.caecorthus.sparkdlc.client.role.killer.magician.MagicianTargetSelectionApi;
import dev.caecorthus.sparkdlc.client.role.killer.magician.MagicianPageSwitchWidget;
import dev.caecorthus.sparkdlc.client.role.killer.magician.MagicianActionWidget;
import dev.caecorthus.sparkdlc.role.killer.magician.UseMagicianAbilityC2SPacket;
import dev.caecorthus.sparkdlc.client.inventory.InventoryButtonLayout;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import dev.caecorthus.sparkdlc.role.killer.magician.MagicianPlayerComponent;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/** Adds the Magician's paged disguise-target row to Wathe's limited inventory. */
@Mixin(LimitedInventoryScreen.class)
public abstract class MagicianInventoryButtonsMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Unique
    private final List<MagicianTargetWidget> sparkdlc$magicianTargets = new ArrayList<>();
    @Unique
    private MagicianPageSwitchWidget sparkdlc$magicianPrevious;
    @Unique
    private MagicianPageSwitchWidget sparkdlc$magicianNext;
    @Unique
    private final List<MagicianActionWidget> sparkdlc$magicianActions = new ArrayList<>();
    @Unique
    private int sparkdlc$magicianPage;

    protected MagicianInventoryButtonsMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }
    @Shadow @Final public ClientPlayerEntity player;

    // Named selector (remapped at build time): the intermediary name crashed the dev client. / 使用具名选择器（构建时重映射）：中间名会让开发客户端崩溃。
    @Inject(method = "init()V", at = @At("TAIL"))
    private void sparkdlc$addMagicianTargets(CallbackInfo ci) {
        if (player == null || player.networkHandler == null) return;
        var role = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
        if (role == null || !dev.caecorthus.sparkdlc.SparkDlcRoles.MAGICIAN_ID.equals(role.identifier())) return;
        sparkdlc$magicianTargets.clear();
        sparkdlc$magicianPage = 0;
        int y = InventoryButtonLayout.getPlayerRowY(this.height);
        // The fixed round roster (owner decision 2026-10-07 D7), not the live online list. / 本局固定名单（所有者 2026-10-07 决定 D7），而非实时在线列表。
        for (MagicianPlayerComponent.RosterEntry entry : MagicianTargetSelectionApi.roster(player)) {
            sparkdlc$magicianTargets.add(this.addDrawableChild(new MagicianTargetWidget(0, y, entry)));
        }

        sparkdlc$magicianPrevious = this.addDrawableChild(new MagicianPageSwitchWidget(
                0,
                y,
                Items.PURPLE_DYE.getDefaultStack(),
                Text.translatable("ui.sparkdlc.magician.pagination.previous"),
                button -> {
                    sparkdlc$magicianPage--;
                    sparkdlc$refreshMagicianPage();
                }
        ));
        sparkdlc$magicianNext = this.addDrawableChild(new MagicianPageSwitchWidget(
                0,
                y,
                Items.LIME_DYE.getDefaultStack(),
                Text.translatable("ui.sparkdlc.magician.pagination.next"),
                button -> {
                    sparkdlc$magicianPage++;
                    sparkdlc$refreshMagicianPage();
                }
        ));
        sparkdlc$refreshMagicianPage();
        sparkdlc$addMagicianActions();
    }

    @Unique
    private void sparkdlc$addMagicianActions() {
        int y = InventoryButtonLayout.getPlayerRowY(this.height) + InventoryButtonLayout.SLOT_APART;
        ItemStack[] icons = {
                Items.GREEN_DYE.getDefaultStack(),
                Items.RED_DYE.getDefaultStack(),
                Items.BLUE_DYE.getDefaultStack(),
                Items.PURPLE_DYE.getDefaultStack()
        };
        int[] actions = {
                UseMagicianAbilityC2SPacket.START_RECORDING,
                UseMagicianAbilityC2SPacket.STOP_RECORDING,
                UseMagicianAbilityC2SPacket.START_PLAYBACK,
                UseMagicianAbilityC2SPacket.STOP_PLAYBACK
        };
        String[] labels = {
                "ui.sparkdlc.magician.action.start_recording",
                "ui.sparkdlc.magician.action.stop_recording",
                "ui.sparkdlc.magician.action.start_playback",
                "ui.sparkdlc.magician.action.stop_playback"
        };
        int startX = this.width / 2 - (icons.length * InventoryButtonLayout.SLOT_APART) / 2
                + InventoryButtonLayout.SLOT_X_OFFSET;
        for (int i = 0; i < icons.length; i++) {
            sparkdlc$magicianActions.add(this.addDrawableChild(new MagicianActionWidget(
                    startX + i * InventoryButtonLayout.SLOT_APART,
                    y,
                    icons[i],
                    Text.translatable(labels[i]),
                    actions[i]
            )));
        }
    }

    @Unique
    private void sparkdlc$refreshMagicianPage() {
        int totalPages = InventoryButtonLayout.getTotalPageCount(sparkdlc$magicianTargets.size());
        sparkdlc$magicianPage = Math.max(0, Math.min(sparkdlc$magicianPage, totalPages - 1));

        int startIndex = sparkdlc$magicianPage * InventoryButtonLayout.PLAYERS_PER_PAGE;
        int endIndex = Math.min(
                startIndex + InventoryButtonLayout.PLAYERS_PER_PAGE,
                sparkdlc$magicianTargets.size()
        );
        int visibleCount = Math.max(0, endIndex - startIndex);
        boolean showPrevious = sparkdlc$magicianPage > 0;
        boolean showNext = sparkdlc$magicianPage < totalPages - 1;
        int groupStartX = InventoryButtonLayout.getCenteredGroupStartX(
                this.width,
                visibleCount,
                showPrevious,
                showNext
        );
        int playerStartX = groupStartX + (showPrevious ? InventoryButtonLayout.SLOT_APART : 0);
        int y = InventoryButtonLayout.getPlayerRowY(this.height);

        for (int index = 0; index < sparkdlc$magicianTargets.size(); index++) {
            MagicianTargetWidget widget = sparkdlc$magicianTargets.get(index);
            boolean visible = index >= startIndex && index < endIndex;
            widget.visible = visible;
            widget.active = visible;
            if (visible) {
                widget.setX(playerStartX + (index - startIndex) * InventoryButtonLayout.SLOT_APART);
                widget.setY(y);
            }
        }

        if (sparkdlc$magicianPrevious != null) {
            sparkdlc$magicianPrevious.visible = showPrevious;
            sparkdlc$magicianPrevious.active = showPrevious;
            sparkdlc$magicianPrevious.setX(groupStartX);
            sparkdlc$magicianPrevious.setY(y);
        }
        if (sparkdlc$magicianNext != null) {
            sparkdlc$magicianNext.visible = showNext;
            sparkdlc$magicianNext.active = showNext;
            sparkdlc$magicianNext.setX(playerStartX + visibleCount * InventoryButtonLayout.SLOT_APART);
            sparkdlc$magicianNext.setY(y);
        }
    }
}
