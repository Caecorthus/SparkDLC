package dev.caecorthus.sparkdlc.mixin.role.civilian.demonhunter;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterPistolRules;
import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterPistolService;
import dev.doctor4t.wathe.api.event.PsychoType;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * NoellesRoles' two psycho listeners for the Demon Hunter, inside the pinned 1.7.6 jar (javac-numbered lambdas of
 * {@code Noellesroles#registerEvents}; a rebuilt jar that renumbers them fails at load, {@code require = 1}, and in
 * {@code verifyUpstreamMixinSelectors}). Both loop over the living players whose raw role is the Demon Hunter.
 * <ul>
 *     <li>{@code lambda$registerEvents$10} ({@code ON_PSYCHO_START}): its only {@code ItemCooldownManager#set} is the
 *     5 s it puts on a pistol it has just handed to a hunter who carried none. A frenzy is starting, so the pistol
 *     does not go on cooldown (2026-11-28). The +2 bullets and the hand-out itself are untouched.</li>
 *     <li>{@code lambda$registerEvents$12} ({@code ON_PSYCHO_END}): its {@code removePistol} took the pistol back
 *     once no public psycho was left. A Demon Hunter now keeps it, with its leftover bullets, until the round reset
 *     (NoellesRoles' {@code ResetPlayer} listener, a different call site, still removes it). The frenzy list
 *     bookkeeping before it is untouched.</li>
 * </ul>
 * An own listener could not do either: it cannot tell NoellesRoles' 5 s from the 10 s of an earlier shot, and a pistol
 * removed with its bullets cannot be given back. Class-level {@code remap = false} (NoellesRoles members); the vanilla
 * target says {@code remap = true}.
 * NoellesRoles 为猎魔人注册的两个疯魔监听器，位于固定的 1.7.6 jar 内（{@code Noellesroles#registerEvents} 中由 javac 编号的
 * lambda；重编后若编号变化，会在加载时失败（{@code require = 1}），{@code verifyUpstreamMixinSelectors} 也会失败）。两者都遍历
 * 原始职业为猎魔人的存活玩家。
 * <ul>
 *     <li>{@code lambda$registerEvents$10}（{@code ON_PSYCHO_START}）：其中唯一的 {@code ItemCooldownManager#set} 是给刚发到
 *     无枪猎魔人手里的枪加的 5 秒。此时正有人进入疯魔，所以枪不进冷却（2026-11-28）。+2 发子弹与发枪本身不变。</li>
 *     <li>{@code lambda$registerEvents$12}（{@code ON_PSYCHO_END}）：其中的 {@code removePistol} 原本在没有公开疯魔后收回枪。
 *     现在猎魔人保留它和剩余子弹直到回合重置（NoellesRoles 的 {@code ResetPlayer} 监听器是另一个调用点，仍会移除）。它之前的
 *     疯魔名单维护不变。</li>
 * </ul>
 * 自有监听器两件事都做不到：它分不清 NoellesRoles 的 5 秒与先前开枪留下的 10 秒，连同子弹一起被移除的枪也无法还原。类级
 * {@code remap = false}（NoellesRoles 成员）；原版目标显式声明 {@code remap = true}。
 */
@Mixin(value = Noellesroles.class, remap = false)
public abstract class DemonHunterFrenzyListenerMixin {
    @WrapWithCondition(
            method = "lambda$registerEvents$10",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V",
                    remap = true),
            require = 1,
            allow = 1
    )
    private static boolean sparkdlc$noCooldownAtFrenzyStart(ItemCooldownManager cooldowns, Item pistol, int ticks,
                                                            ServerPlayerEntity frenziedPlayer, PsychoType type) {
        return DemonHunterPistolRules.coolsDown(DemonHunterPistolService.isAnyoneFrenziedWith(frenziedPlayer));
    }

    @WrapWithCondition(
            method = "lambda$registerEvents$12",
            at = @At(value = "INVOKE",
                    target = "Lorg/agmas/noellesroles/demonhunter/DemonHunterPistolItem;removePistol(Lnet/minecraft/entity/player/PlayerEntity;)V"),
            require = 1,
            allow = 1
    )
    private static boolean sparkdlc$keepPistolAfterFrenzy(PlayerEntity hunter) {
        return !DemonHunterPistolService.keepsPistol(hunter);
    }
}
