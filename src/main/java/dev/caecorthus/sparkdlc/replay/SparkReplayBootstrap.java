package dev.caecorthus.sparkdlc.replay;

import dev.caecorthus.sparkdlc.replay.net.ReplayNetworking;

/**
 * Single entry point that wires the replay module during mod initialization.
 * 回放模块在模组初始化时的统一接线入口。
 */
public final class SparkReplayBootstrap {
    private SparkReplayBootstrap() {
    }

    public static void register() {
        ReplayNetworking.registerCommon();
        RoleChangeReplay.register();
        ReplayPresentation.register();
    }
}
