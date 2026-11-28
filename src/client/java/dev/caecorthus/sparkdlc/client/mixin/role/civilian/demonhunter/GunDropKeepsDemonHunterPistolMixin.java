package dev.caecorthus.sparkdlc.client.mixin.role.civilian.demonhunter;

import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterPistolService;
import dev.doctor4t.wathe.util.GunDropPayload;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

/**
 * Wathe's client half of "you dropped your gun for shooting an innocent" removes the first {@code wathe:guns} stack of
 * the local inventory, while the server (Wathe's punishment and the mod's two copies of it) removes and drops the
 * revolver only. Since 2026-11-28 a Demon Hunter can carry the Demon Hunter pistol (in that tag, handed out at the
 * round start, so usually in an earlier slot) next to a revolver picked up from the ground: the client would then
 * erase the pistol locally, and the server, whose pistol slot never changed, would not send it again until its
 * bullet count next changes. The pistol is never the gun that is dropped, so the client-side removal skips it.
 * Class-level {@code remap = false} (Wathe/Fabric members); the vanilla target says {@code remap = true}.
 * Wathe 中“误伤无辜而丢枪”的客户端部分会移除本地背包里第一个 {@code wathe:guns} 物品堆，而服务端（Wathe 的惩罚以及本模组的两份
 * 同款实现）只移除并丢下左轮。2026-11-28 起猎魔人可以同时带着猎魔枪（在该标签内，开局发放，所以通常在更靠前的格子）和从地上
 * 捡来的左轮：客户端会在本地抹掉猎魔枪，而服务端的猎魔枪格子没有变化，直到子弹数下一次改变才会重新下发。被丢下的枪从来不是
 * 猎魔枪，因此客户端的移除跳过它。类级 {@code remap = false}（Wathe/Fabric 成员）；原版目标显式声明 {@code remap = true}。
 */
@Mixin(value = GunDropPayload.Receiver.class, remap = false)
public abstract class GunDropKeepsDemonHunterPistolMixin {
    @ModifyArg(
            method = "receive(Ldev/doctor4t/wathe/util/GunDropPayload;Lnet/fabricmc/fabric/api/client/networking/v1/ClientPlayNetworking$Context;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerInventory;remove(Ljava/util/function/Predicate;ILnet/minecraft/inventory/Inventory;)I",
                    remap = true),
            index = 0
    )
    private Predicate<ItemStack> sparkdlc$dropTheGunNotThePistol(Predicate<ItemStack> droppedGun) {
        return DemonHunterPistolService.exceptPistol(droppedGun);
    }
}
