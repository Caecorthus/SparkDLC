package dev.caecorthus.sparkdlc.mixin.role.civilian.saint;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.item.ceremonialsword.CeremonialSwordProtectionPolicy;
import dev.caecorthus.sparkdlc.role.civilian.saint.SaintFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorRules;
import dev.caecorthus.sparkdlc.role.killer.bellringer.BellRingerRules;
import dev.caecorthus.sparkdlc.role.killer.timestealer.TimeStealerRules;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Enforces the Saint's Voodoo immunity before Wathe's first-result-wins kill event can bypass later listeners (Wathe
 * member, HEAD of the 5-arg {@code killPlayer}, default priority and order). Since 2026-11-28 it is her only
 * immunity: kills by good players are no longer refused here.
 * 在 Wathe 的首个结果短路击杀事件绕过后续监听器之前，强制执行圣徒的巫毒免疫（Wathe 成员，五参 {@code killPlayer} 的 HEAD，
 * 默认优先级与 order）。自 2026-11-28 起这是她唯一的免疫：好人的击杀不再在此被拒绝。
 */
@Mixin(GameFunctions.class)
public abstract class GameFunctionsSaintProtectionMixin {
    @Inject(
            method = "killPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;ZLnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/Identifier;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void sparkdlc$protectSaint(
            ServerPlayerEntity victim,
            boolean spawnBody,
            @Nullable ServerPlayerEntity killer,
            Identifier deathReason,
            boolean force,
            CallbackInfo ci
    ) {
        // The Savior's terminal weapons kill every target except the Fiend. / 救世主终结武器除魔人外必杀。
        if (SaviorRules.isTerminalKill(deathReason)) {
            return;
        }
        // This HEAD guard ignores force, so the owner-approved forced kills (bell toll, Clock curse) opt out explicitly.
        // 此 HEAD 拦截不看 force，因此所有者批准的强制击杀（丧钟、时钟诅咒）需在此显式放行。
        if (BellRingerRules.piercesProtection(deathReason, force)
                || TimeStealerRules.piercesProtection(deathReason, force)) {
            return;
        }
        if (killer == null && dev.caecorthus.sparkdlc.role.civilian.emma.EmmaTerminalService.isBacklash(deathReason)) {
            return;
        }
        // The Last Escape trait owns active-phase immunity and its train/lifecycle exceptions before protection costs.
        // Last Escape 天赋在消耗保护前裁定脱险无敌及列车/生命周期例外。
        if (SparkTraitsApi.isLastEscapeActive(victim)) {
            return;
        }
        if (CeremonialSwordProtectionPolicy.cancelsDeath(
                SaintFeatureService.blocksKill(victim, deathReason), deathReason)) {
            ci.cancel();
        }
    }
}
