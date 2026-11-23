package dev.caecorthus.sparkdlc.mixin.round.modevote;

import dev.caecorthus.sparkdlc.round.modevote.ModeVoteService;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs the mode vote after Wathe finished teleporting everyone to the newly chosen map.
 * <p>
 * 在地图已选定、玩家已传送到新地图之后启动模式投票，所以顺序是「先选地图，再选模式」。
 */
@Mixin(dev.doctor4t.wathe.game.GameFunctions.class)
public abstract class FinalizeVotingModeVoteMixin {

    @Inject(method = "finalizeVoting", at = @At("TAIL"))
    private static void sparkdlc$startModeVoting(ServerWorld currentWorld, Identifier targetDimensionId, CallbackInfo ci) {
        try {
            ModeVoteService.startVoteAfterTeleport(currentWorld);
        } catch (Throwable ignored) {
            // never break the map voting flow because of the mode vote
        }
    }
}
