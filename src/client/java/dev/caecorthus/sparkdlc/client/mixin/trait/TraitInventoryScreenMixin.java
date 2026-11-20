package dev.caecorthus.sparkdlc.client.mixin.trait;

import dev.caecorthus.sparkdlc.client.gui.OwnerInventoryPresenter;
import dev.caecorthus.sparkdlc.client.gui.card.InventoryInfoCard;
import dev.caecorthus.sparkdlc.client.trait.gui.OwnerInventoryClientAdapter;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(LimitedInventoryScreen.class)
public abstract class TraitInventoryScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow @Final public ClientPlayerEntity player;
    public TraitInventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    // Named selector: Loom remaps it to Screen's intermediary method_25394 in the packaged jar, and dev runs keep the
    // named render, so both environments resolve Wathe's override.
    // 使用具名选择器：Loom 在打包 jar 中将其重映射为 Screen 的中间名 method_25394，开发环境则保留具名 render，两端都能命中 Wathe 的重写。
    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("TAIL"))
    private void sparkdlc$renderOwnerTraits(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // The owner card shows the traits itself when it committed this render call; this card is its fallback.
        // 本人卡片在本次渲染调用中已提交并显示天赋时由它显示；本卡片是其回退。
        if (OwnerInventoryPresenter.ownsTraits() || OwnerInventoryClientAdapter.nativeCardDisabled()) return;
        // A failure must not escape Wathe's render (it would crash the client): the card is disabled for this
        // connection and logged once. 失败不得抛出 Wathe 的渲染方法（会导致客户端崩溃）：本连接内停用卡片并只记录一次。
        try {
            var entries = OwnerInventoryClientAdapter.collect(player);
            var card = OwnerInventoryClientAdapter.card();
            // The role-skill card then anchors to this slot, and the native card fills the slot to sit flush beside it.
            // 此时职业技能卡锚定在本列左侧，原生卡占满整列以紧贴排列。
            var snapshot = card.prepareNative(this, textRenderer,
                    List.of(new InventoryInfoCard.Section(Text.translatable("gui.sparkdlc.traits"), entries)));
            card.draw(context, textRenderer, snapshot, mouseX, mouseY, width, height);
        } catch (RuntimeException | LinkageError failure) {
            OwnerInventoryClientAdapter.disableNativeCard(failure);
        }
    }
}
