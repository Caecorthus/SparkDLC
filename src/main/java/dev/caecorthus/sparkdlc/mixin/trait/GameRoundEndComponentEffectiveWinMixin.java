package dev.caecorthus.sparkdlc.mixin.trait;

import dev.caecorthus.sparkdlc.trait.component.TraitWorldComponent;
import dev.caecorthus.sparkdlc.trait.effective.EffectiveWinService;
import dev.caecorthus.sparkdlc.trait.lifecycle.RoundEndTraitResolver;
import dev.caecorthus.sparkdlc.trait.civilian.laststand.LastStandFinalMomentService;
import dev.caecorthus.sparkdlc.round.end.RoundEndRows;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.jester.JesterPlayerComponent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;
import dev.caecorthus.sparkdlc.SparkDLC;

/**
 * Applies the traits' effective-team winners to Wathe's round-end data.
 * 将天赋的有效阵营胜负关系写入 Wathe 回合结束数据。
 *
 * <p>Runs only when the core's {@code GameRoundEndComponentCustomWinMixin} (same priority, listed earlier in the
 * config, same two HEADs) did not cancel, i.e. when there is no custom faction win.
 * 仅当核心的 {@code GameRoundEndComponentCustomWinMixin}（优先级相同、配置中排在前面、注入同样的两个 HEAD）没有取消时运行，
 * 即没有自定义阵营胜利时。
 */
@Mixin(value = GameRoundEndComponent.class, remap = false)
public abstract class GameRoundEndComponentEffectiveWinMixin {
    @Shadow
    @Final
    private List<GameRoundEndComponent.RoundEndData> players;

    @Shadow
    private GameFunctions.WinStatus winStatus;

    @Shadow
    private @Nullable Identifier gameMode;

    @Shadow
    public abstract void sync();

    @Inject(method = "setRoundEndData(Lnet/minecraft/server/world/ServerWorld;Ldev/doctor4t/wathe/game/GameFunctions$WinStatus;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void sparkdlc$setEffectiveRoundEndData(
            ServerWorld serverWorld,
            GameFunctions.WinStatus winStatus,
            CallbackInfo ci
    ) {
        this.players.clear();
        GameWorldComponent game = GameWorldComponent.KEY.get(serverWorld);
        TraitWorldComponent traitWorld = TraitWorldComponent.KEY.get(serverWorld);
        this.gameMode = RoundEndRows.gameModeId(game);

        RoundEndRows.appendRows(this.players, serverWorld, game, (uuid, role) ->
                LastStandFinalMomentService.didFinalMomentPlayerWin(
                        winStatus,
                        role,
                        traitWorld.isFinalMomentLooseEnd(uuid)
                ) || isWinningJester(serverWorld, uuid, role)
                        || EffectiveWinService.didEffectiveTeamWin(
                        winStatus,
                        role,
                        RoundEndTraitResolver.resolve(serverWorld, traitWorld, uuid)
                ));

        this.winStatus = winStatus;
        this.sync();
        ci.cancel();
    }

    @Inject(method = "didWin", at = @At("HEAD"), cancellable = true)
    private void sparkdlc$didWinFromRoundEndData(UUID uuid, CallbackInfoReturnable<Boolean> cir) {
        if (GameFunctions.WinStatus.NONE == this.winStatus) {
            cir.setReturnValue(false);
            return;
        }
        for (GameRoundEndComponent.RoundEndData detail : this.players) {
            if (detail.player().getId().equals(uuid)) {
                cir.setReturnValue(detail.isWinner());
                return;
            }
        }
        cir.setReturnValue(false);
    }

    /**
     * A triggered hidden Jester is the real NoellesRoles Jester role; NoellesRoles marks the win on its component,
     * and that flag must survive the traits' replacement of the round-end data so the player gets the Jester screen.
     * 触发后的隐藏小丑就是真正的 NoellesRoles 小丑；NoellesRoles 会把胜利写在组件上，天赋替换回合结束数据时必须
     * 保留该标志，玩家才能看到小丑结算界面。
     */
    private static boolean isWinningJester(ServerWorld world, UUID uuid, @Nullable Role role) {
        if (role == null || !Noellesroles.JESTER_ID.equals(role.identifier())) {
            return false;
        }
        ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
        return player != null && JesterPlayerComponent.KEY.get(player).won;
    }
}
