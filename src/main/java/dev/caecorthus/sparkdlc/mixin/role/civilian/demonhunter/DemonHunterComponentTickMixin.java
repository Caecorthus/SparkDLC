package dev.caecorthus.sparkdlc.mixin.role.civilian.demonhunter;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterPistolService;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.demonhunter.DemonHunterPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * NoellesRoles' Demon Hunter component tick: once nobody on its frenzy list has psycho ticks left it clears the list,
 * removes the pistol and syncs. A Demon Hunter now keeps the pistol for the whole round (2026-11-28), so only the
 * removal is skipped; the list is still cleared and synced (it drives the through-wall highlight). The tick acts on
 * Demon Hunters only; the condition still reads the raw role, so that anyone else it may ever reach is handled as
 * before. Class-level {@code remap = false}: NoellesRoles members.
 * NoellesRoles 猎魔人组件的 tick：疯魔名单上已无人还有疯魔 tick 时，它清空名单、移除猎魔枪并同步。现在猎魔人整局保留猎魔枪
 * （2026-11-28），因此只跳过移除；名单照常清空并同步（它驱动隔墙高亮）。该 tick 只作用于猎魔人；条件仍读取原始职业，
 * 这样它将来可能触及的其他人都按原样处理。类级 {@code remap = false}：NoellesRoles 成员。
 */
@Mixin(value = DemonHunterPlayerComponent.class, remap = false)
public abstract class DemonHunterComponentTickMixin {
    @WrapWithCondition(
            method = "serverTick()V",
            at = @At(value = "INVOKE",
                    target = "Lorg/agmas/noellesroles/demonhunter/DemonHunterPistolItem;removePistol(Lnet/minecraft/entity/player/PlayerEntity;)V"),
            require = 1,
            allow = 1
    )
    private boolean sparkdlc$keepPistolAfterFrenzy(PlayerEntity hunter) {
        return !DemonHunterPistolService.keepsPistol(hunter);
    }
}
