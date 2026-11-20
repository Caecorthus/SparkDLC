package dev.caecorthus.sparkdlc.client.mixin.role.killer.witchmaiden;

import dev.caecorthus.sparkdlc.client.role.killer.witchmaiden.WitchMaidenClientModule;
import dev.caecorthus.sparkdlc.component.WitchPlayerComponent;
import net.minecraft.network.RegistryByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forwards the decoded local-owner sync to Witch Maiden state. / 将解码后的本地所有者同步转交给巫女状态。 */
@Mixin(WitchPlayerComponent.class)
public abstract class FocusedFootstepsOwnerSyncMixin {
    @Inject(method = "applySyncPacket", at = @At("TAIL"))
    private void sparkdlc$acknowledgeFocusedFootstepsSync(RegistryByteBuf buf, CallbackInfo ci) {
        WitchMaidenClientModule.onOwnerSync((WitchPlayerComponent) (Object) this);
    }
}
