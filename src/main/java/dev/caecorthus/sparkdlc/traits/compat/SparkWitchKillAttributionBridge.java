package dev.caecorthus.sparkdlc.traits.compat;

import dev.caecorthus.sparkdlc.witch.api.SparkWitchApi;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

/**
 * Public-facade bridge preserving delayed killers even when their player is offline. The modules are fused in
 * SparkDLC, so this calls {@link SparkWitchApi#runWithKillAttribution} directly.
 * 公共门面桥接：即使玩家离线，仍保留延迟击杀的责任人。各模块已融合进 SparkDLC，因此这里直接调用
 * {@link SparkWitchApi#runWithKillAttribution}。
 */
public final class SparkWitchKillAttributionBridge {
    private SparkWitchKillAttributionBridge() {
    }

    /**
     * The action runs exactly once, inside SparkWitch's attribution scope; whatever it throws propagates unchanged.
     * 动作在 SparkWitch 的责任归属范围内恰好执行一次；其抛出的异常原样向上传播。
     */
    public static void runWithKillAttribution(ServerWorld world, UUID responsiblePlayer, Runnable action) {
        SparkWitchApi.runWithKillAttribution(world, responsiblePlayer, action);
    }
}
