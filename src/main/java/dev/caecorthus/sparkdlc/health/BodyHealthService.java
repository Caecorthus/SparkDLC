package dev.caecorthus.sparkdlc.health;

import dev.doctor4t.wathe.api.event.ResetPlayer;

/**
 * Round edges of the body-health pool, registered once from {@code SparkDLC#onInitialize}. Wathe fires
 * {@code ResetPlayer} when it initialises a round, when it finalises one and while it resets players in the lobby,
 * never on a death, so every player starts and leaves a round with full body health and nothing restores it in
 * between. Server only; the restore is unconditional, whatever {@link HealthModeComponent} says.
 * 部位血量池的回合节点，由 {@code SparkDLC#onInitialize} 注册一次。Wathe 在初始化对局、对局收尾以及在大厅重置玩家时触发
 * {@code ResetPlayer}，死亡时从不触发，因此每名玩家都以满部位血量开始并离开一局，局中不会被它恢复。仅服务端；
 * 无论 {@link HealthModeComponent} 是否开启都会恢复。
 */
public final class BodyHealthService {
    private static boolean registered;

    private BodyHealthService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ResetPlayer.EVENT.register(player -> BodyHealthComponent.KEY.get(player).restoreAll());
    }
}
