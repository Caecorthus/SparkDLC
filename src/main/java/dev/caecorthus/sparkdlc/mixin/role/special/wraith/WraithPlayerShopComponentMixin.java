package dev.caecorthus.sparkdlc.mixin.role.special.wraith;

import dev.caecorthus.sparkdlc.role.civilian.windspirit.WindSpiritRules;
import dev.caecorthus.sparkdlc.role.killer.saboteur.SaboteurRules;
import dev.caecorthus.sparkdlc.role.witch.curser.CurserFeatureService;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithStateService;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lets only approved promoted Wraith identities cross Wathe's recorded-dead purchase gate.
 * 仅允许获准的冤魂晋升身份穿过 Wathe 的死亡记录购买门禁。
 */
@Mixin(value = PlayerShopComponent.class, remap = false)
public abstract class WraithPlayerShopComponentMixin {
    @Shadow @Final private PlayerEntity player;

    @Redirect(
            method = "tryBuy",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/game/GameFunctions;isPlayerPlayingAndAlive(Lnet/minecraft/entity/player/PlayerEntity;)Z"
            )
    )
    private boolean sparkdlc$allowPromotedWraithPurchase(PlayerEntity checkedPlayer) {
        boolean windSpiritAllowed = WindSpiritRules.canPassShopAliveGate(
                GameFunctions.isPlayerPlayingAndAlive(checkedPlayer),
                WindSpiritRules.isWindSpirit(player),
                WraithStateService.isActive(player),
                WraithStateService.isRestricted(player)
        );
        boolean saboteurAllowed = SaboteurRules.canPassShopAliveGate(
                windSpiritAllowed,
                SaboteurRules.isActivePromotedSaboteur(player)
        );
        return saboteurAllowed || CurserFeatureService.isActivePromotedCurser(player);
    }
}
