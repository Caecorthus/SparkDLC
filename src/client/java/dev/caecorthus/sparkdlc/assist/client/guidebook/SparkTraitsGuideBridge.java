package dev.caecorthus.sparkdlc.assist.client.guidebook;

import dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.traits.api.TraitRegistry;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Reads only SparkTraits' public, owner-synced active-trait API. The modules are fused in SparkDLC, so these are
 * direct calls.
 * 仅读取 SparkTraits 公开且只同步给本人的生效天赋 API。各模块已融合进 SparkDLC，因此这里是直接调用。
 */
public final class SparkTraitsGuideBridge {
    private SparkTraitsGuideBridge() {
    }

    public static Set<String> ownerVisibleActiveTraitIds(PlayerEntity player) {
        if (player == null) {
            return Set.of();
        }

        Set<String> activeIds = new LinkedHashSet<>();
        // The guide polls every tick: read the player's few active ids rather than ask about every trait;
        // ids this client does not know are still skipped.
        // 指南每 tick 轮询：直接读取玩家仅有的几个生效天赋，不再逐个询问全部天赋；本客户端不认识的 id 仍然跳过。
        for (Identifier id : SparkTraitsApi.getActiveTraitIds(player)) {
            if (TraitRegistry.contains(id)) {
                activeIds.add(id.toString());
            }
        }
        return Set.copyOf(activeIds);
    }
}
