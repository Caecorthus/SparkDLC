package dev.caecorthus.sparkdlc.mixin.role.civilian.demonhunter;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterPistolRules;
import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterPistolService;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.agmas.noellesroles.demonhunter.DemonHunterShootC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Server Demon Hunter pistol receiver (NoellesRoles), the shot rules of the 2026-11-28 rework
 * ({@code DemonHunterPistolRules}); they apply to whoever fires the pistol. Five narrow hooks, in the order NoellesRoles
 * reaches them, share two method-local flags ({@code @Share}):
 * <ol>
 *     <li>{@code recordItemUse} (an accepted shot with a bullet, before any kill): notes whether anyone is frenzied.
 *     It is read before the kill on purpose: the shot that kills the last frenzied player was fired during a
 *     frenzy.</li>
 *     <li>NoellesRoles' single {@code isRole(target, JESTER)} read (reached only with a valid player target): notes
 *     whether the hit gives its bullet back, i.e. the target is frenzied and not a Jester (NoellesRoles' Jester
 *     branch refunds a Jester it kills). The read itself is returned untouched.</li>
 *     <li>{@code PlayerInventory#removeOne} (the last bullet was fired): a Demon Hunter's pistol, and any pistol whose
 *     bullet is about to be refunded, stays with 0 bullets; otherwise NoellesRoles removes the stack as before.</li>
 *     <li>{@code killPlayer} (always reached for a refunded hit, before the kill): pays the bullet back into the
 *     main-hand pistol. Before the kill, because the kill can end the frenzy and take a Coroner's loaned pistol
 *     away; nothing is ever created for a shooter who no longer holds it.</li>
 *     <li>The final {@code ItemCooldownManager#set} (10 s): skipped when the shot was fired during a frenzy.</li>
 * </ol>
 * None of them cancels; the HEAD guards, the target-lookup wrappers (Seeker, drone, puppet) and the TAIL Jester-puppet
 * refund are untouched. Class-level {@code remap = false} (NoellesRoles/Wathe/Fabric members); the two vanilla
 * targets say {@code remap = true}.
 * 服务端猎魔枪接收器（NoellesRoles）：2026-11-28 重做的射击规则（{@code DemonHunterPistolRules}），对任何开枪者生效。五个窄钩子
 * 按 NoellesRoles 执行到它们的顺序排列，共用两个方法内的标记（{@code @Share}）：
 * <ol>
 *     <li>{@code recordItemUse}（已接受且有子弹的一枪，位于任何击杀之前）：记下此刻是否有人疯魔。特意在击杀前读取：打死最后
 *     一名疯魔者的那一枪是在疯魔期间开的。</li>
 *     <li>NoellesRoles 唯一一次 {@code isRole(target, JESTER)} 读取（只有存在有效玩家目标时才会到达）：记下这次命中是否返还
 *     子弹，即目标处于疯魔且不是小丑（击杀小丑的返还由 NoellesRoles 的小丑分支负责）。读取结果本身原样返回。</li>
 *     <li>{@code PlayerInventory#removeOne}（打出了最后一发）：猎魔人的枪以及即将返还子弹的枪以 0 发保留；否则 NoellesRoles
 *     照旧移除物品堆。</li>
 *     <li>{@code killPlayer}（返还的命中必然到达，位于击杀之前）：把子弹还进主手的猎魔枪。放在击杀之前，是因为击杀可能结束疯魔
 *     并收回验尸官借来的枪；已经不再持枪的射手不会凭空得到任何东西。</li>
 *     <li>最后的 {@code ItemCooldownManager#set}（10 秒）：这一枪在疯魔期间开出时跳过。</li>
 * </ol>
 * 它们都不取消；HEAD 守卫、目标查找处的包装（搜寻者、无人机、皮套）以及 TAIL 的小丑皮套返还均不受影响。类级
 * {@code remap = false}（NoellesRoles/Wathe/Fabric 成员）；两个原版目标显式声明 {@code remap = true}。
 */
@Mixin(value = DemonHunterShootC2SPacket.Receiver.class, remap = false)
public abstract class DemonHunterPistolShotMixin {
    @Inject(method = "receive(Lorg/agmas/noellesroles/demonhunter/DemonHunterShootC2SPacket;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
            at = @At(value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/record/GameRecordManager;recordItemUse(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/Identifier;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/nbt/NbtCompound;)V"))
    private void sparkdlc$noteFrenzyAtShot(DemonHunterShootC2SPacket payload, ServerPlayNetworking.Context context,
                                           CallbackInfo ci,
                                           @Share("frenzyAtShot") LocalBooleanRef frenzyAtShot) {
        frenzyAtShot.set(DemonHunterPistolService.isAnyoneFrenzied(context.player().getServerWorld()));
    }

    @WrapOperation(method = "receive(Lorg/agmas/noellesroles/demonhunter/DemonHunterShootC2SPacket;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
            at = @At(value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;isRole(Lnet/minecraft/entity/player/PlayerEntity;Ldev/doctor4t/wathe/api/Role;)Z"),
            require = 1, allow = 1)
    private boolean sparkdlc$noteFrenziedHit(GameWorldComponent game, PlayerEntity target, Role jesterRole,
                                             Operation<Boolean> original,
                                             @Share("frenzyAtShot") LocalBooleanRef frenzyAtShot,
                                             @Share("bulletRefunded") LocalBooleanRef bulletRefunded) {
        boolean jester = original.call(game, target, jesterRole);
        boolean frenzied = DemonHunterPistolService.isFrenzied(target);
        if (frenzied) {
            // The target NoellesRoles is about to judge counts even where the world scan above would skip it.
            // NoellesRoles 即将判定的目标一定算数，即使上面的全图扫描会跳过它。
            frenzyAtShot.set(true);
        }
        bulletRefunded.set(DemonHunterPistolRules.refundsBullet(frenzied, jester));
        return jester;
    }

    @WrapOperation(method = "receive(Lorg/agmas/noellesroles/demonhunter/DemonHunterShootC2SPacket;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerInventory;removeOne(Lnet/minecraft/item/ItemStack;)V",
                    remap = true),
            require = 1, allow = 1)
    private void sparkdlc$keepEmptiedPistol(PlayerInventory inventory, ItemStack pistol, Operation<Void> original,
                                            DemonHunterShootC2SPacket payload,
                                            ServerPlayNetworking.Context context,
                                            @Share("bulletRefunded") LocalBooleanRef bulletRefunded) {
        if (DemonHunterPistolRules.keepsEmptiedPistol(DemonHunterPistolService.keepsPistol(context.player()),
                bulletRefunded.get())) {
            DemonHunterPistolService.emptyPistol(pistol);
        } else {
            original.call(inventory, pistol);
        }
    }

    @Inject(method = "receive(Lorg/agmas/noellesroles/demonhunter/DemonHunterShootC2SPacket;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
            at = @At(value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/game/GameFunctions;killPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;ZLnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/Identifier;)V"))
    private void sparkdlc$refundFrenziedHit(DemonHunterShootC2SPacket payload, ServerPlayNetworking.Context context,
                                            CallbackInfo ci,
                                            @Share("bulletRefunded") LocalBooleanRef bulletRefunded) {
        if (bulletRefunded.get()) {
            DemonHunterPistolService.refundBullet(context.player());
        }
    }

    @WrapWithCondition(method = "receive(Lorg/agmas/noellesroles/demonhunter/DemonHunterShootC2SPacket;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V",
                    remap = true),
            require = 1, allow = 1)
    private boolean sparkdlc$noCooldownDuringFrenzy(ItemCooldownManager cooldowns, Item pistol, int ticks,
                                                    @Share("frenzyAtShot") LocalBooleanRef frenzyAtShot) {
        return DemonHunterPistolRules.coolsDown(frenzyAtShot.get());
    }
}
