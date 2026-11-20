package dev.caecorthus.sparkdlc.mixin.role.civilian.bodyguard;

import dev.caecorthus.sparkdlc.role.civilian.bodyguard.BodyguardProtectionService;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Democracy Shield and vest at HEAD of the one killPlayer overload every Wathe kill funnels into. Here the force
 * flag is known, and the gear is spent before any KillPlayer.BEFORE listener (NoellesRoles' Iron Man / whiskey) or
 * Wathe's psycho armour. The AXMC may pierce the vest; the kill then goes on past HEAD instead of being
 * cancelled (BodyguardProtectionService).
 *
 * <p>Priority 1100: HEAD injectors run in apply order (priority, then entry order in
 * {@code sparkdlc.mixins.json}), so this one runs after every
 * default-priority immunity guard at the same HEAD: the Jester's fake-death immunity, the stage-2 Undercover's
 * assassination immunity, the Saint's protection and the faction core's affect veto, and after the dormant Fiend's
 * immunity (1090). Gear is only spent on a kill that no immunity stops (owner 2026-10-22); the Fiend was made
 * explicit on 2026-11-20 ("这个排一下序吧"): both used to be 1100 and the config order ran the gear first.</p>
 * 民主盾牌与防弹衣，挂在所有 Wathe 击杀汇总的 killPlayer 重载的 HEAD。此处能看到 force 标记，并且装备先于任何
 * KillPlayer.BEFORE 监听（NoellesRoles 铁人/威士忌）与 Wathe 疯魔护甲消耗。AXMC 可以击穿防弹衣，此时击杀
 * 不在 HEAD 取消而是继续（BodyguardProtectionService）。
 *
 * <p>优先级 1100：同一 HEAD 的注入按应用顺序执行（先优先级，再按 {@code sparkdlc.mixins.json} 中的条目顺序），因此本钩子排在所有默认优先级的免死判断之后：
 * 小丑假死免死、二阶段卧底免刺杀、圣徒保护与阵营核心的影响否决，并排在休眠魔人免疫（1090）之后。只有没有任何免死挡下的击杀
 * 才会消耗装备（所有者 2026-10-22）；魔人的顺序于 2026-11-20 明确（「这个排一下序吧」）：两者原同为 1100，由配置顺序让
 * 装备先执行。</p>
 */
@Mixin(value = GameFunctions.class, priority = 1100)
public abstract class BodyguardGearKillGuardMixin {
    @Inject(
            method = "killPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;ZLnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/Identifier;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void sparkdlc$bodyguardGearBlocksKill(
            ServerPlayerEntity victim,
            boolean spawnBody,
            @Nullable ServerPlayerEntity killer,
            Identifier deathReason,
            boolean force,
            CallbackInfo ci
    ) {
        if (BodyguardProtectionService.tryBlockKill(victim, killer, deathReason, force)) {
            ci.cancel();
        }
    }
}
