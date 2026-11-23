package dev.caecorthus.sparkdlc.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

import java.util.function.Predicate;

/**
 * The one implementation of the "interaction lock" three states share: the Control Expert stun, the Engineer capture
 * stun (and the Jester Moment freeze, which reuses its component) and the Taotie head daze. Each of them used to carry
 * its own copy of these five listeners. Server-authoritative; it also runs on the client, where FAIL just means no
 * packet is sent. The locked player's own client already reads every key as released; the listeners refuse the vanilla
 * packets a modified client could still send: item use, block use, entity use and attacks. They sit in the caller's
 * dedicated phase ordered before {@link Event#DEFAULT_PHASE}, so they decide before every existing listener (including
 * ones that kill inside the callback: sword, poker, Mighty Force); a player who is not locked always gets PASS, so the
 * relative order of all other listeners is unchanged.
 * 三种状态共用的“交互锁”的唯一实现：控场专家眩晕、工程师捕捉定身（以及复用其组件的小丑时刻定身）与饕餮头槌眩晕。此前它们各带
 * 一份这五个监听器。服务端权威；客户端同样运行，FAIL 只表示不发送数据包。被锁定者的客户端已把所有按键读作未按下；这些监听器
 * 拒绝修改过的客户端仍可能发送的原版数据包：使用物品、使用方块、使用实体与攻击。监听器位于调用方的专用阶段（排在
 * {@link Event#DEFAULT_PHASE} 之前），先于所有现有监听器（包括在回调内直接击杀的：仪式剑、火钳、蛮力）做出决定；未被锁定的
 * 玩家始终得到 PASS，其他监听器之间的相对顺序不变。
 *
 * <p>What differs between the states is explicit: the phase (each keeps its own id, other features order themselves
 * against it), the predicate, and {@link BareHandBlockUse}. Each state's own guard class still decides when to
 * register, so the registration order of the listeners is the one it always was.
 * 各状态之间的差异都是显式参数：阶段（各自保留自己的 id，其他功能据此排序）、判定条件，以及 {@link BareHandBlockUse}。何时注册
 * 仍由各状态自己的守卫类决定，因此监听器的注册顺序与以往完全相同。</p>
 */
public final class InteractionLockGuards {
    /**
     * Whether a locked player may still use a block with an empty hand. / 被锁定的玩家能否空手使用方块。
     */
    public enum BareHandBlockUse {
        /** A stunned player cannot press any key. / 被定身者按不了任何键。 */
        REFUSED,
        /** A dazed player keeps walking, opening doors and pressing buttons. / 被眩晕者仍可走动、开门、按按钮。 */
        ALLOWED
    }

    private InteractionLockGuards() {
    }

    /**
     * Registers the five listeners in {@code phase}, in the fixed order item use, block use, entity use, entity attack,
     * block attack. The caller guards against a second registration.
     * 在 {@code phase} 阶段按固定顺序注册五个监听器：使用物品、使用方块、使用实体、攻击实体、攻击方块。重复注册由调用方防止。
     */
    public static void register(Identifier phase, Predicate<PlayerEntity> locked, BareHandBlockUse bareHandBlockUse) {
        UseItemCallback.EVENT.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
        UseItemCallback.EVENT.register(phase, (player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            return locked.test(player) ? TypedActionResult.fail(stack) : TypedActionResult.pass(stack);
        });
        UseBlockCallback.EVENT.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
        UseBlockCallback.EVENT.register(phase, (player, world, hand, hit) ->
                bareHandBlockUse == BareHandBlockUse.ALLOWED && player.getStackInHand(hand).isEmpty()
                        ? ActionResult.PASS : verdict(locked, player));
        UseEntityCallback.EVENT.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
        UseEntityCallback.EVENT.register(phase, (player, world, hand, entity, hit) -> verdict(locked, player));
        AttackEntityCallback.EVENT.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
        AttackEntityCallback.EVENT.register(phase, (player, world, hand, entity, hit) -> verdict(locked, player));
        AttackBlockCallback.EVENT.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
        AttackBlockCallback.EVENT.register(phase, (player, world, hand, pos, direction) -> verdict(locked, player));
    }

    private static ActionResult verdict(Predicate<PlayerEntity> locked, PlayerEntity player) {
        return locked.test(player) ? ActionResult.FAIL : ActionResult.PASS;
    }
}
