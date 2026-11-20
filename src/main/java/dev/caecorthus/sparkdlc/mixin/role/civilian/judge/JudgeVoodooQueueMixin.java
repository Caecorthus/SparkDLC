package dev.caecorthus.sparkdlc.mixin.role.civilian.judge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.caecorthus.sparkdlc.role.civilian.judge.JudgeVoodooCause;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.voodoo.VoodooPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Bind the dead Voodoo player, never their killer or the selected victim. / 责任人为死亡的巫毒师，不是凶手或受咒者。 */
@Mixin(Noellesroles.class)
public abstract class JudgeVoodooQueueMixin {
    @WrapOperation(method = "lambda$registerEvents$16", at = @At(value = "INVOKE",
            target = "Lorg/agmas/noellesroles/voodoo/VoodooPlayerComponent;startPendingDeath(I)V"))
    private static void sparkdlc$bindVoodooCause(VoodooPlayerComponent pending, int ticks, Operation<Void> original,
                                                  ServerPlayerEntity deadVoodoo, ServerPlayerEntity killer, Identifier reason) {
        original.call(pending, ticks);
        ((JudgeVoodooCause) pending).sparkdlc$setPendingVoodooActor(deadVoodoo.getUuid());
    }
}
