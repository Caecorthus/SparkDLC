package dev.caecorthus.sparkdlc.mixin.role.civilian.demonhunter;

import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterPistolService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Predicate;

/**
 * The second half of {@code DemonHunterGunPickupMixin}: while a Demon Hunter's ground gun pickup is being decided,
 * a {@code contains(Predicate)} scan of that hunter's inventory does not see the pistol, so Wathe's "already has a
 * gun" answers for the other guns only. At any other time, and for every other inventory, the predicate is passed
 * on untouched ({@code DemonHunterPistolService#heldGunTest}).
 * {@code DemonHunterGunPickupMixin} 的另一半：在判定猎魔人地面捡枪期间，对该猎魔人背包的 {@code contains(Predicate)} 扫描看不到
 * 猎魔枪，因此 Wathe 的“已经有枪”只针对其他枪作答。其他任何时候、其他任何背包，判定都原样传递
 * （{@code DemonHunterPistolService#heldGunTest}）。
 */
@Mixin(PlayerInventory.class)
public abstract class DemonHunterHeldGunMixin {
    @Shadow
    @Final
    public PlayerEntity player;

    @ModifyVariable(method = "contains(Ljava/util/function/Predicate;)Z", at = @At("HEAD"), argsOnly = true)
    private Predicate<ItemStack> sparkdlc$pistolIsNoHeldGun(Predicate<ItemStack> predicate) {
        return DemonHunterPistolService.heldGunTest(this.player, predicate);
    }
}
