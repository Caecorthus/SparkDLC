package dev.caecorthus.sparkdlc.mixin.role.civilian.saint;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.caecorthus.sparkdlc.role.civilian.saint.SaintGunShotService;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Saint's gunshot never kills (owner 2026-11-28): two seams in Wathe's 5-arg {@code killPlayer}, the one overload
 * every gun's kill ends in (Wathe member). Ordering is the contract:
 * <ul>
 *     <li>On the alive branch's {@code changeGameMode(SPECTATOR)} call itself (no shift), {@code order = 1050}: after
 *     every {@code @WrapMethod} pre-call, HEAD veto, {@code KillPlayer.BEFORE} listener, psycho armour and the Specter
 *     shield, after Tofana (one instruction earlier) and the Last Escape trait (default order) on the same call, so
 *     whatever would have stopped the kill stops the shot too and pays what it always pays; and before the
 *     Schrödinger's Cat takeover (order 1100) and Hiro's fake death (order 1200), which act only on a death that
 *     really commits, and this one never does. Cancelling there skips the death mark, kill reward, corpse, drops and
 *     {@code KillPlayer.AFTER}, like Tofana and the Cat; the victim is rooted and glows instead.</li>
 *     <li>Around both {@code stopPsycho()} calls, which come before that point: a victim in psycho mode keeps it,
 *     because the shot that reaches them does not kill.</li>
 * </ul>
 * Every kill that is not a Saint's gunshot passes through both untouched. Server only.
 * 圣徒的枪击永不致死（所有者 2026-11-28）：Wathe 五参 {@code killPlayer}（所有枪械击杀最终都落到的重载，Wathe 成员）中的
 * 两个接缝，顺序即契约。存活分支的 {@code changeGameMode(SPECTATOR)} 调用本身（不偏移，{@code order = 1050}）：位于所有
 * {@code @WrapMethod} 调用前逻辑、HEAD 否决、{@code KillPlayer.BEFORE} 监听器、疯魔护甲与幽魂护盾之后，也位于同一调用上的
 * 托法娜（前一条指令）与绝境逃生天赋（默认 order）之后，因此原本能挡下击杀的一切同样挡下这一枪，并照常付出代价；同时位于
 * 薛定谔之猫接管（order 1100）与希罗假死（order 1200）之前，它们只对真正落定的死亡生效，而这一枪永远不会落定。在此取消会
 * 跳过死亡标记、击杀奖励、尸体、掉落与 {@code KillPlayer.AFTER}，与托法娜和猫一致；受害者改为被禁锢并发光。
 * 包裹该点之前的两处 {@code stopPsycho()} 调用：处于疯魔状态的受害者保留疯魔，因为命中他们的这一枪并不致死。
 * 不是圣徒枪击的击杀在两处都原样通过。仅服务端。
 */
@Mixin(GameFunctions.class)
public abstract class GameFunctionsSaintGunShotMixin {
    @Inject(
            method = "killPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;ZLnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/Identifier;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;changeGameMode(Lnet/minecraft/world/GameMode;)Z"
            ),
            cancellable = true,
            require = 1,
            allow = 1,
            order = 1050
    )
    private static void sparkdlc$saintGunShotRootsInsteadOfKilling(
            ServerPlayerEntity victim,
            boolean spawnBody,
            @Nullable ServerPlayerEntity killer,
            Identifier deathReason,
            boolean force,
            CallbackInfo ci
    ) {
        if (SaintGunShotService.isNonLethalShot(victim, killer, deathReason)) {
            ci.cancel();
            SaintGunShotService.applyHit(victim, killer);
        }
    }

    @WrapOperation(
            method = "killPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;ZLnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/Identifier;Z)V",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/PlayerPsychoComponent;stopPsycho()V")
    )
    private static void sparkdlc$saintGunShotKeepsPsycho(
            PlayerPsychoComponent psycho,
            Operation<Void> original,
            ServerPlayerEntity victim,
            boolean spawnBody,
            @Nullable ServerPlayerEntity killer,
            Identifier deathReason,
            boolean force
    ) {
        if (!SaintGunShotService.isNonLethalShot(victim, killer, deathReason)) {
            original.call(psycho);
        }
    }
}
