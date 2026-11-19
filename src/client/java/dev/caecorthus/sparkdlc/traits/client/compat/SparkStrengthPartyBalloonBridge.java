package dev.caecorthus.sparkdlc.traits.client.compat;

import dev.caecorthus.sparkdlc.strength.component.partyanimal.PartyBalloonMarkComponent;
import dev.caecorthus.sparkdlc.strength.role.partyanimal.PartyBalloonRules;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Bridge to SparkStrength's Party Balloon mark. The mark only syncs to Party Animals, and the yellow outline it draws
 * must survive SparkTraits' Conscience branch, which answers before Wathe's event. The modules are fused in SparkDLC,
 * so this reads {@link PartyBalloonMarkComponent} and {@link PartyBalloonRules} directly.
 * 到 SparkStrength 派对气球标记的桥接。该标记只同步给派对狂，其黄色描边需要在 SparkTraits 先于 Wathe 事件作答的
 * 善良分支中保留。各模块已融合进 SparkDLC，因此这里直接读取 {@link PartyBalloonMarkComponent} 与 {@link PartyBalloonRules}。
 */
public final class SparkStrengthPartyBalloonBridge {
    // Boxed once: the instinct hook asks per visible player each frame. / 只装箱一次：本能钩子每帧为每个可见玩家查询。
    private static final Integer HIGHLIGHT_COLOR = PartyBalloonRules.HIGHLIGHT_COLOR;

    private SparkStrengthPartyBalloonBridge() {
    }

    /**
     * SparkStrength's balloon outline colour when {@code target} carries a balloon, or {@code null} when it does not.
     * 目标带着气球时返回 SparkStrength 的气球描边颜色；未带气球时返回 null。
     */
    public static @Nullable Integer highlightColor(PlayerEntity target) {
        return PartyBalloonMarkComponent.isMarked(target) ? HIGHLIGHT_COLOR : null;
    }
}
