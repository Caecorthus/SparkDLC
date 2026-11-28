package dev.caecorthus.sparkdlc.role.civilian.saint;

import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorForcedShotComponent;
import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorForcedShotGuns;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerCarry;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.tag.WatheItemTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * The Saint can throw away the gun she carries (owner 2026-11-28), with the drop key on the selected hotbar slot only.
 * Wathe refuses every drop of a living player while the train HUD is on, on the server (its {@code onPlayerAction}
 * wrapper) and on the client (the drop key is suppressed), so both halves ask {@link #mayThrowSelectedGun}: the server
 * through {@code mixin/role/civilian/saint/ServerPlayNetworkHandlerSaintGunDropMixin}, the client through Wathe's
 * {@code ShouldAllowSuppressedKey}. The gun leaves her hand through the vanilla selected-slot drop, so the item entity
 * keeps the stack's components and names her as its thrower, and Wathe's pickup rule then keeps her from taking that
 * gun back. Dropping from the inventory screen (Q over a slot, dragging out) stays refused by Wathe.
 * 圣徒可以丢出自己携带的枪（所有者 2026-11-28），只能对当前选中的快捷栏位按丢弃键。列车 HUD 开启时，Wathe 在服务端（其
 * {@code onPlayerAction} 包装）与客户端（丢弃键被压制）都拒绝存活玩家的一切丢弃，因此两端都询问
 * {@link #mayThrowSelectedGun}：服务端经由 {@code mixin/role/civilian/saint/ServerPlayNetworkHandlerSaintGunDropMixin}，
 * 客户端经由 Wathe 的 {@code ShouldAllowSuppressedKey}。枪通过原版的选中栏位丢弃离手，因此掉落物保留物品堆的组件并把她记为
 * 投掷者，Wathe 的拾取规则随即使她无法捡回这把枪。在物品栏界面中丢弃（对栏位按 Q、拖出界面）仍被 Wathe 拒绝。
 */
public final class SaintGunDropService {
    private SaintGunDropService() {
    }

    /**
     * Both sides: a living Saint (raw role) in a running round whose selected stack is in {@code wathe:guns}. The
     * Dominator's marked forced-shot revolver is never hers to throw.
     * 双端：进行中的对局里存活的圣徒（真实职业），且当前选中的物品堆属于 {@code wathe:guns}。支配者带标记的强制开枪左轮
     * 不属于她，不能丢出。
     */
    public static boolean mayThrowSelectedGun(PlayerEntity player) {
        if (player.isSpectator() || !GameFunctions.isPlayerPlayingAndAlive(player)
                || !SaintGunShotService.isSaint(player)) {
            return false;
        }
        ItemStack selected = player.getMainHandStack();
        return selected.isIn(WatheItemTags.GUNS) && !DominatorForcedShotGuns.isForcedShotGun(selected);
    }

    /**
     * Server thread, in place of the vanilla drop action Wathe would refuse. A drop the client predicted but the
     * server will not perform (the Dominator's sequence runs, the Partygoer carries her) is corrected by resyncing the
     * inventory; the guards on {@code ServerPlayerEntity#dropSelectedItem} still run and resync by themselves.
     * 服务端线程，取代会被 Wathe 拒绝的原版丢弃动作。客户端已预测但服务端不会执行的丢弃（支配者序列进行中、被派对客扛着）
     * 通过重新同步物品栏纠正；{@code ServerPlayerEntity#dropSelectedItem} 上的各项守卫照常运行，并自行重新同步。
     */
    public static void throwSelectedGun(ServerPlayerEntity player, boolean entireStack) {
        player.updateLastActionTime();
        if (DominatorForcedShotComponent.isSequenceActive(player) || PartygoerCarry.isCarried(player)) {
            player.currentScreenHandler.syncState();
            return;
        }
        player.dropSelectedItem(entireStack);
    }
}
