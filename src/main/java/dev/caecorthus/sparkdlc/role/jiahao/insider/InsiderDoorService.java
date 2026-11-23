package dev.caecorthus.sparkdlc.role.jiahao.insider;

import dev.doctor4t.wathe.api.event.DoorInteraction;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.ModItems;

/**
 * The Insider's own neutral master key doors (D6). It never conflicts with NoellesRoles' key listener or the Corrupt
 * Cop kit's ({@link InsiderDoorRules}): NoellesRoles returns PASS for the Insider (its key role set is a private
 * immutable set), except DENY while the key cools down, and the kit's any-neutral rule answers the same as this rule on
 * train doors and PASS on key-locked room doors. Runs on both logical sides like those two listeners: Wathe raises
 * DoorInteraction from {@code SmallDoorBlock#onUse} on the client too, so the client predicts the same ALLOW/DENY and
 * key cooldown, and the server's cooldown write is synced back to the owner.
 * 内应自有的中立万能钥匙开门逻辑（D6）。与 NoellesRoles 及黑警职业强化的钥匙监听从不冲突（见
 * {@link InsiderDoorRules}）：NoellesRoles 对内应返回 PASS（其钥匙身份集合是私有的不可变集合），仅在钥匙冷却时返回
 * DENY；黑警职业强化的“任意中立”规则对列车门的结果与本规则相同，对上锁的房间门返回 PASS。与这两个监听一样在两个逻辑端
 * 运行：Wathe 在客户端的 {@code SmallDoorBlock#onUse} 中同样触发 DoorInteraction，因此客户端会预测相同的 ALLOW/DENY 与
 * 钥匙冷却，服务端写入的冷却再同步给持有者。
 */
public final class InsiderDoorService {
    private static boolean registered;

    private InsiderDoorService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        DoorInteraction.EVENT.register(InsiderDoorService::doorInteraction);
    }

    private static DoorInteraction.DoorInteractionResult doorInteraction(DoorInteraction.DoorInteractionContext context) {
        PlayerEntity player = context.getPlayer();
        DoorInteraction.DoorInteractionResult result = InsiderDoorRules.masterKeyDoorResult(
                // handItem is Wathe's copy of the main-hand stack. / handItem 是 Wathe 复制的主手物品栈。
                context.getHandItem().isOf(ModItems.NEUTRAL_MASTER_KEY),
                InsiderParticipation.isInsider(player),
                context.getDoorType(),
                context.isBlasted(),
                context.isJammed(),
                context.isOpen(),
                context.requiresKey(),
                player.getItemCooldownManager().isCoolingDown(ModItems.NEUTRAL_MASTER_KEY)
        );
        if (result == DoorInteraction.DoorInteractionResult.ALLOW) {
            player.getItemCooldownManager().set(ModItems.NEUTRAL_MASTER_KEY, InsiderRules.MASTER_KEY_COOLDOWN_TICKS);
        }
        return result;
    }
}
