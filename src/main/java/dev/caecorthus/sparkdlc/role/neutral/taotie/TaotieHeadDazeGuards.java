package dev.caecorthus.sparkdlc.role.neutral.taotie;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.event.InteractionLockGuards;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;

/**
 * Interaction lock of the daze, modelled on the Control Expert stun guards. Server-authoritative; it also runs
 * on the client, where FAIL means no packet is sent. The listeners sit in a dedicated phase ordered before
 * {@link Event#DEFAULT_PHASE}, so they decide before every existing listener (including ones that kill inside the
 * callback); undazed players always get PASS, so the relative order of all other listeners is unchanged. Bare-hand
 * block use stays allowed so a dazed player can still open doors and press buttons.
 * 眩晕的交互锁，写法与控场专家眩晕一致。服务端权威；客户端同样运行，FAIL 时不会发送数据包。监听器位于排在
 * {@link Event#DEFAULT_PHASE} 之前的专用阶段，先于所有现有监听器（包括在回调内直接击杀的）做出决定；未被眩晕的玩家
 * 始终得到 PASS，其他监听器之间的相对顺序不变。空手使用方块仍然放行，被眩晕者仍可开门、按按钮。
 */
public final class TaotieHeadDazeGuards {
    public static final Identifier DAZE_PHASE = SparkDLC.id("taotie_head_daze");
    private static boolean registered;

    private TaotieHeadDazeGuards() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        // The five listeners are the shared InteractionLockGuards; what is this state's own is the phase, the
        // predicate and that bare-hand block use stays allowed.
        // 五个监听器来自共用的 InteractionLockGuards；本状态自有的是阶段、判定条件以及“空手使用方块仍然放行”。
        InteractionLockGuards.register(DAZE_PHASE, TaotieHeadDaze::isDazed,
                InteractionLockGuards.BareHandBlockUse.ALLOWED);
    }
}
