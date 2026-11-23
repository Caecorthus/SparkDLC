package dev.caecorthus.sparkdlc.mixin.role.killer.hunter;

import dev.caecorthus.sparkdlc.role.HunterOrthopedistPairingService;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MurderGameMode.class)
public abstract class HunterOrthopedistPairingMixin {
    @Inject(
            method = "assignRolesAndGetKillerCount",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/cca/ScoreboardRoleSelectorComponent;assignCivilians(Lnet/minecraft/server/world/ServerWorld;Ldev/doctor4t/wathe/cca/GameWorldComponent;Ljava/util/List;)I"
            )
    )
    private static void sparkdlc$pairHunterOrthopedistBeforeCivilians(
            @NotNull ServerWorld world,
            @NotNull List<ServerPlayerEntity> players,
            GameWorldComponent gameComponent,
            CallbackInfoReturnable<Integer> cir
    ) {
        HunterOrthopedistPairingService.ensurePairBeforeCivilians(world, gameComponent, players);
        // Witch and Team Jiahao members are no longer dealt here: the third-faction draw deals them before Wathe's
        // neutral pass from the Witch and Jiahao lineups (owner 2026-11-16, see SparkDlcRoleRegistry).
        // 魔女与嘉豪阵营成员不再在此发放：由第三方阵营抽取在 Wathe 中立分配之前按魔女与嘉豪阵容发放
        //（所有者 2026-11-16，见 SparkDlcRoleRegistry）。
    }
}
