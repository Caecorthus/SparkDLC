package dev.caecorthus.sparkdlc.mixin.role.civilian.saint;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.caecorthus.sparkdlc.role.civilian.saint.SaintGunDropService;
import dev.caecorthus.sparkdlc.role.civilian.saint.SaintGunShotService;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Server half of the Saint's gun drop (owner 2026-11-28; vanilla target, default remap). Wathe wraps
 * {@code onPlayerAction} ({@code ServerPlayNetworkHandlerMixin#wathe$restrictPlayerActions}, a default-priority
 * {@code @WrapMethod}) and returns without calling the vanilla handler for {@code DROP_ITEM} and
 * {@code DROP_ALL_ITEMS} of a living player while the train HUD is on. A {@code @WrapMethod} of a mixin applied later
 * is the outer one, so priority 1100 puts this wrapper outside Wathe's and inside the Last Escape click lock
 * (priority 2000), which deliberately lets drops through. For a living Saint whose selected stack is a gun it performs
 * the selected-slot drop itself ({@link SaintGunDropService#throwSelectedGun}) instead of calling on; every other
 * action and every other player goes on to Wathe's wrapper unchanged. The two drop actions are first moved to the
 * server thread, as the vanilla handler itself does on entry: Wathe's wrapper answers before that hand-off.
 * 圣徒丢枪的服务端部分（所有者 2026-11-28；原版目标，默认重映射）。Wathe 包裹了 {@code onPlayerAction}
 * （{@code ServerPlayNetworkHandlerMixin#wathe$restrictPlayerActions}，默认优先级的 {@code @WrapMethod}），列车 HUD 开启时
 * 对存活玩家的 {@code DROP_ITEM} 与 {@code DROP_ALL_ITEMS} 直接返回，不调用原版处理。后应用的 mixin 的 {@code @WrapMethod}
 * 位于外层，因此优先级 1100 使本包装处于 Wathe 的包装之外、绝境逃生点击锁（优先级 2000，有意放行丢弃）之内。对选中物品堆为
 * 枪械的存活圣徒，它自行执行选中栏位的丢弃（{@link SaintGunDropService#throwSelectedGun}）而不再向内调用；其他动作与其他玩家
 * 原样进入 Wathe 的包装。两种丢弃动作会先被移交到服务端线程，与原版处理在入口处所做的一致：Wathe 的包装在该移交之前就已作答。
 */
@Mixin(value = ServerPlayNetworkHandler.class, priority = 1100)
public abstract class ServerPlayNetworkHandlerSaintGunDropMixin {
    @Shadow
    public ServerPlayerEntity player;

    @WrapMethod(method = "onPlayerAction")
    private void sparkdlc$saintThrowsSelectedGun(PlayerActionC2SPacket packet, Operation<Void> original) {
        PlayerActionC2SPacket.Action action = packet.getAction();
        if (action != PlayerActionC2SPacket.Action.DROP_ITEM
                && action != PlayerActionC2SPacket.Action.DROP_ALL_ITEMS) {
            original.call(packet);
            return;
        }
        // No-op on the server thread; from the network thread it re-queues the packet and leaves, like vanilla.
        // 在服务端线程上是空操作；在网络线程上则重新排入该数据包并退出，与原版一致。
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayNetworkHandler) (Object) this, player.getServerWorld());
        if (SaintGunDropService.mayThrowSelectedGun(player)) {
            SaintGunDropService.throwSelectedGun(player, action == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS);
            return;
        }
        if (SaintGunShotService.isSaint(player)) {
            // Her client predicts the drop (SaintGunDropClient). When the server declines one (the state changed in
            // this tick, or the Dominator / Partygoer cases), Wathe's wrapper below answers nothing, so the stack
            // would stay erased on her screen until the next slot sync.
            // 她的客户端会预测丢弃（SaintGunDropClient）。服务端拒绝时（状态在本 tick 内变化，或支配者 / 派对客的情形），
            // 下面 Wathe 的包装不作任何应答，物品堆会在她的画面上一直消失到下一次栏位同步，因此在这里补一次同步。
            player.currentScreenHandler.syncState();
        }
        original.call(packet);
    }
}
