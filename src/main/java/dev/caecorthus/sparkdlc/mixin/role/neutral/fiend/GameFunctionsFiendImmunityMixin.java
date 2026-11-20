package dev.caecorthus.sparkdlc.mixin.role.neutral.fiend;

import dev.caecorthus.sparkdlc.role.neutral.fiend.FiendImmunityService;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dormant Fiend immunity (D3) on Wathe's 5-arg {@code killPlayer} (Wathe member, {@code remap = false}); it ignores
 * {@code force}. Order: HEAD callbacks run in ascending mixin priority (Mixin 0.8.7 resolves every HEAD node before any
 * injection, then inserts each lower-priority callback first). Priority 1090 therefore runs after the default-1000
 * HEAD guards: the faction core's affect veto (a vetoed attack never reaches this callback, so it earns no reaction),
 * the Saint guard (Saint victims only, consumes nothing), Vendetta and Wraith capture; after the Rope's interrupt
 * observer (1030), the Otherworld void (1040) and the Rys-T ricochet (1050); and before the Bodyguard's gear guard
 * (Democracy Shield and vest, 1100). Owner 2026-11-20, "这个排一下序吧": the immunity runs before the gear, by the
 * owner's rule of 2026-10-22 that gear is only spent on a kill no immunity stops; until then both were 1100 and the
 * config entry order ran the gear first. It still precedes every {@code KillPlayer.BEFORE} listener, psycho armour
 * and Tofana, so no shield or protection pays for an ignored hit. The traits' and Judge's {@code @WrapMethod}s stay
 * outermost.
 * 休眠魔人免疫（D3），挂在 Wathe 五参 {@code killPlayer} 上（Wathe 成员，{@code remap = false}），不看 {@code force}。
 * 顺序：HEAD 回调按 mixin 优先级升序执行（Mixin 0.8.7 先解析所有 HEAD 节点再注入，优先级低者先插入）。因此优先级 1090
 * 排在默认 1000 的 HEAD 守卫之后：阵营核心的影响否决（被否决的攻击到不了这里，不产生反应）、圣徒守卫（只针对
 * 圣徒受害者，不消耗任何东西）、仇杀与冤魂捕获；排在绳索打断观察者（1030）、里世界作废（1040）与 Rys-T 跳弹（1050）之后；
 * 并先于保镖的装备守卫（民主盾牌与防弹衣，1100）。所有者 2026-11-20「这个排一下序吧」：免疫先于装备，依据所有者
 * 2026-10-22 的规则——只有没有任何免死挡下的击杀才消耗装备；此前两者同为 1100，由配置条目顺序让装备先执行。它仍先于所有
 * {@code KillPlayer.BEFORE} 监听、疯魔护甲与托法娜，因此被忽略的攻击不会消耗任何护盾或保护。天赋与法官的
 * {@code @WrapMethod} 仍在最外层。
 */
@Mixin(value = GameFunctions.class, remap = false, priority = 1090)
public abstract class GameFunctionsFiendImmunityMixin {
    @Inject(
            method = "killPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;ZLnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/Identifier;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void sparkdlc$protectDormantFiend(
            ServerPlayerEntity victim,
            boolean spawnBody,
            @Nullable ServerPlayerEntity killer,
            Identifier deathReason,
            boolean force,
            CallbackInfo ci
    ) {
        if (FiendImmunityService.interceptKill(victim, killer, deathReason, force)) {
            ci.cancel();
        }
    }
}
