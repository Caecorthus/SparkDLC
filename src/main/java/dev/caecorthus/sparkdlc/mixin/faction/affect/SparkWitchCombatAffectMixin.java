package dev.caecorthus.sparkdlc.mixin.faction.affect;

import dev.caecorthus.sparkdlc.faction.target.PlayerAffectMixinGuard;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Guards SparkWitch custom melee effects before their attack paths run.
 * 在 SparkWitch 自定义近战攻击路径运行前拦截其效果。
 */
@Pseudo
@Mixin(targets = {
        "dev.caecorthus.sparkdlc.item.firepoker.FirePokerCombatService",
        "dev.caecorthus.sparkdlc.item.ceremonialsword.CeremonialSwordCombatService",
        "dev.caecorthus.sparkdlc.role.civilian.apprentice.abilities.mightyforce.MightyForceCombatService"
}, remap = false)
public abstract class SparkWitchCombatAffectMixin {
    private static final Identifier CUSTOM_MELEE_ACTION = Identifier.of("sparkdlc", "custom_melee");
    private static final Identifier CEREMONIAL_BLADE_ACTION = Identifier.of("sparkdlc", "ceremonial_blade");

    @Inject(method = "tryHandleAttack", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void sparkfactionapi$guardCustomMelee(
            Entity attacker,
            World world,
            Hand hand,
            Entity target,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        if (attacker instanceof ServerPlayerEntity actor
                && target instanceof ServerPlayerEntity playerTarget
                && !PlayerAffectMixinGuard.allows(actor, playerTarget, CUSTOM_MELEE_ACTION)) {
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Inject(method = "killWithCeremonialSword", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void sparkfactionapi$guardCeremonialDashKill(
            ServerPlayerEntity actor,
            ServerPlayerEntity target,
            CallbackInfo ci
    ) {
        if (!PlayerAffectMixinGuard.allows(actor, target, CEREMONIAL_BLADE_ACTION)) {
            ci.cancel();
        }
    }
}
