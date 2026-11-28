package dev.caecorthus.sparkdlc.mixin.role.civilian.demonhunter;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterPistolService;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * A Demon Hunter who carries the pistol can still pick a normal gun up from the ground (2026-11-28). Wathe's own
 * {@code ItemEntityMixin} (HEAD of {@code onPlayerCollision}) refuses a {@code wathe:guns} stack to a player who
 * "already has a gun", and the pistol is in that tag. Wathe's handler is merged into {@code ItemEntity} under a
 * generated name and cannot be targeted, so this wraps the whole pickup instead: MixinExtras applies
 * {@code @WrapMethod} after every {@code @Inject} of every mod, so Wathe's handler and all the mod's own HEAD vetoes
 * (Wraith, Impostor / Depression, Recruit, Savior, Undercover, USEC) run inside the window it opens. Inside it,
 * {@code DemonHunterHeldGunMixin} leaves the hunter's pistols out of the {@code PlayerInventory#contains(Predicate)}
 * scan Wathe asks. Nothing else changes: every other Wathe condition (innocent, not the dropper, no knife, no
 * pickup ban), a real second gun, a pistol held on the cursor and every veto still refuse as before, and nobody but
 * a Demon Hunter touching a gun other than the pistol opens the window.
 * 带着猎魔枪的猎魔人仍可从地上捡起普通的枪（2026-11-28）。Wathe 自己的 {@code ItemEntityMixin}（{@code onPlayerCollision} 的
 * HEAD）拒绝把 {@code wathe:guns} 物品交给“已经有枪”的玩家，而猎魔枪在这个标签里。Wathe 的处理方法以生成的名字合并进
 * {@code ItemEntity}，无法作为目标，所以这里改为包住整个拾取：MixinExtras 在所有模组的全部 {@code @Inject} 之后才应用
 * {@code @WrapMethod}，因此 Wathe 的处理方法和本模组自己的全部 HEAD 否决（冤魂、内鬼 / 抑郁、新兵、救世主、卧底、USEC）都在它
 * 打开的窗口内执行。窗口内，{@code DemonHunterHeldGunMixin} 让 Wathe 所查询的 {@code PlayerInventory#contains(Predicate)}
 * 扫描看不到猎魔人的猎魔枪。其余一概不变：Wathe 的其他条件（好人、不是丢枪者、没有刀、未被禁止捡枪）、真正的第二把枪、
 * 光标上拿着的猎魔枪以及所有否决都照旧拒绝；只有猎魔人碰到猎魔枪以外的枪时才会打开窗口。
 */
@Mixin(ItemEntity.class)
public abstract class DemonHunterGunPickupMixin {
    @Shadow
    public abstract ItemStack getStack();

    @WrapMethod(method = "onPlayerCollision(Lnet/minecraft/entity/player/PlayerEntity;)V")
    private void sparkdlc$pistolIsNoHeldGun(PlayerEntity player, Operation<Void> original) {
        if (!DemonHunterPistolService.beginGunPickup(player, this.getStack())) {
            original.call(player);
            return;
        }
        try {
            original.call(player);
        } finally {
            DemonHunterPistolService.endGunPickup();
        }
    }
}
