package dev.caecorthus.sparkdlc.bridge.strength;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

/**
 * Bridge to SparkTraits' public facade for the Vulture's death scream. The modules are fused in SparkDLC, so this
 * calls {@link SparkTraitsApi#getRoundEndTraitIds} directly.
 * 秃鹫死亡惨叫使用的 SparkTraits 公开门面桥接。各模块已融合进 SparkDLC，因此这里直接调用
 * {@link SparkTraitsApi#getRoundEndTraitIds}。
 */
public final class SparkTraitsVultureCompat {
    private SparkTraitsVultureCompat() {
    }

    /**
     * Traits the player holds or died with: live traits, else the death snapshot, else the round snapshot. Safe inside
     * Wathe's {@code KillPlayer.AFTER} whichever side of SparkTraits' own AFTER listener (which snapshots, then wipes,
     * the victim's traits) this runs on. Server-only; never null.
     * 玩家当前或死亡时持有的词条：在线词条，否则死亡快照，再否则本局快照。无论本调用在 SparkTraits 自己的
     * {@code KillPlayer.AFTER}（先快照、再清空受害者词条）之前还是之后执行，结果都正确。仅限服务端；永不返回 null。
     */
    public static List<Identifier> getRoundEndTraitIds(ServerWorld world, UUID playerUuid) {
        if (world == null || playerUuid == null) {
            return List.of();
        }
        return List.copyOf(SparkTraitsApi.getRoundEndTraitIds(world, playerUuid));
    }
}
