package dev.caecorthus.sparkdlc.bridge.witch;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Bridge into the public SparkTraits API. SparkTraits is fused into SparkDLC, so the calls are direct (compiled).
 * 读取 SparkTraits 公共 API 的桥接。SparkTraits 已融合进 SparkDLC，因此为直接（编译期）调用。
 */
public final class SparkTraitsLastStandBridge {
    private SparkTraitsLastStandBridge() {
    }

    /** Server only. / 仅服务端。 */
    public static boolean hasTriggeredThisRound(ServerWorld world, UUID playerUuid) {
        return world != null && playerUuid != null
                && SparkTraitsApi.hasLastStandTriggeredThisRound(world, playerUuid);
    }

    /**
     * Whether SparkTraits Last Stand currently holds this player's pending death transition; a null player reads as
     * false. Shared by the Seeker and Abyss Listener bridges. Both sides.
     * SparkTraits 背水一战当前是否持有该玩家的待决死亡转换；玩家为空时为 false。由搜寻者与聆渊者桥接共用。两端可用。
     */
    public static boolean isLastStandPending(@Nullable PlayerEntity player) {
        return player != null && SparkTraitsApi.isLastStandPending(player);
    }
}
