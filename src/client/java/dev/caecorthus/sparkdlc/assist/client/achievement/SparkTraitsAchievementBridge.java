package dev.caecorthus.sparkdlc.assist.client.achievement;

import dev.caecorthus.sparkdlc.traits.component.TraitPlayerComponent;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Reads SparkTraits state that its public API does not expose but that the server syncs to the player it belongs
 * to: who sent the local player into Depression psycho ({@code TraitPlayerComponent#getDepressionPsychoAttacker},
 * synced to its owner only). The modules are fused in SparkDLC, so the component is read directly.
 * 读取 SparkTraits 公开 API 未提供、但服务端只同步给本人的状态：是谁让本地玩家进入了抑郁疯魔
 * （{@code TraitPlayerComponent#getDepressionPsychoAttacker}，仅同步给本人）。各模块已融合进 SparkDLC，因此直接读取该组件。
 */
final class SparkTraitsAchievementBridge {
    private SparkTraitsAchievementBridge() {
    }

    /**
     * The player who sent {@code player} into Depression psycho, or null when they are not in it. Only meaningful for
     * the local player: the server never sends this to anyone else.
     * 让 {@code player} 进入抑郁疯魔的玩家；不在抑郁疯魔中时为 null。只对本地玩家有意义：服务端不会把它发给其他人。
     */
    static UUID depressionPsychoAttacker(PlayerEntity player) {
        if (player == null) {
            return null;
        }
        TraitPlayerComponent component = TraitPlayerComponent.KEY.getNullable(player);
        return component == null ? null : component.getDepressionPsychoAttacker();
    }
}
