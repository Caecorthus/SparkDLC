package dev.caecorthus.sparkdlc.role.civilian.controlexpert;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.event.InteractionLockGuards;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;

/**
 * Server-authoritative interaction lock (also runs on the client, where FAIL means no packet is sent). The listeners
 * sit in a dedicated phase ordered before {@link Event#DEFAULT_PHASE}, so they decide before every existing listener,
 * including callbacks that kill inside the callback (sword, poker, Mighty Force). Unstunned players always get PASS,
 * so the relative order of all other listeners is unchanged.
 * 服务端权威的交互锁（客户端同样运行，FAIL 时不会发送数据包）。监听器位于排在 {@link Event#DEFAULT_PHASE}
 * 之前的专用阶段，因此先于所有现有监听器做出决定，包括在回调内直接击杀的监听器（仪式剑、火钳、蛮力）。
 * 未被眩晕的玩家始终得到 PASS，其他监听器之间的相对顺序保持不变。
 */
public final class ControlExpertStunGuards {
    public static final Identifier STUN_PHASE = SparkDLC.id("control_expert_stun");
    private static boolean registered;

    private ControlExpertStunGuards() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        // The five listeners are the shared InteractionLockGuards; what is this state's own is the phase, the
        // predicate and that bare-hand block use is refused too.
        // 五个监听器来自共用的 InteractionLockGuards；本状态自有的是阶段、判定条件以及“空手使用方块同样拒绝”。
        InteractionLockGuards.register(STUN_PHASE, ControlExpertStun::isStunned,
                InteractionLockGuards.BareHandBlockUse.REFUSED);
    }
}
