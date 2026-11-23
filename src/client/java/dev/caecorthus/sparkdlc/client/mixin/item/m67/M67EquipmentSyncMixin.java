package dev.caecorthus.sparkdlc.client.mixin.item.m67;

import dev.caecorthus.sparkdlc.client.item.M67Client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class M67EquipmentSyncMixin {
    @Unique
    private M67Client.EquipmentSync sparkdlc$m67BeforeSync;

    @Inject(method = {"onScreenHandlerSlotUpdate", "onInventory", "onUpdateSelectedSlot"}, at = @At("HEAD"))
    private void sparkdlc$m67EquipmentBefore(CallbackInfo ci) {
        // Network-thread invocations are rescheduled by vanilla; only snapshot the applied call.
        // 原版会重新调度网络线程调用；仅记录主线程实际应用前的状态。
        if (MinecraftClient.getInstance().isOnThread()) {
            sparkdlc$m67BeforeSync = M67Client.beforeEquipmentSync();
        }
    }

    @Inject(method = {"onScreenHandlerSlotUpdate", "onInventory", "onUpdateSelectedSlot"}, at = @At("RETURN"))
    private void sparkdlc$m67EquipmentApplied(CallbackInfo ci) {
        M67Client.afterEquipmentSync(sparkdlc$m67BeforeSync);
        sparkdlc$m67BeforeSync = null;
    }
}
