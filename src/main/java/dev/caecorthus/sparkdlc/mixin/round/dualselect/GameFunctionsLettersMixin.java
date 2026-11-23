package dev.caecorthus.sparkdlc.mixin.round.dualselect;

import dev.caecorthus.sparkdlc.round.dualselect.DualRoleSelectionComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Defers the opening letters in dual-select rounds: Wathe hands them out before ON_FINISH_INITIALIZE, i.e. before the
 * player has chosen, so the letter would describe the role they may swap away from. In dual-select rounds we skip that
 * handout and send the letters again after the choices are applied.
 * <p>
 * 双选局里把开局信件押后：Wathe 在 ON_FINISH_INITIALIZE 之前就把信发了，那时玩家还没选，信里写的身份可能被换掉。
 * 双选模式下跳过这次发放，等选择落地后再补发。
 */
@Mixin(GameFunctions.class)
public abstract class GameFunctionsLettersMixin {

    @Invoker("giveLettersToPlayers")
    public static void sparkdlc$invokeGiveLetters(ServerWorld world, GameWorldComponent gameComponent,
                                           List<ServerPlayerEntity> players, Map<UUID, Integer> rooms) {
        throw new AssertionError();
    }

    @Inject(method = "giveLettersToPlayers", at = @At("HEAD"), cancellable = true)
    private static void sparkdlc$deferLetters(ServerWorld world, GameWorldComponent gameComponent,
                                              List<ServerPlayerEntity> players, Map<UUID, Integer> rooms,
                                              CallbackInfo ci) {
        if (DualRoleSelectionComponent.isDualSelectRound(gameComponent)) {
            ci.cancel();
        }
    }
}
