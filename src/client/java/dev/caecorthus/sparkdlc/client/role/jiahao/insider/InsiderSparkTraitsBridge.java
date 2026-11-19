package dev.caecorthus.sparkdlc.client.role.jiahao.insider;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Cross-module seam (SparkTraits): the Insider's own client read of the public {@code SparkTraitsApi} facade,
 * {@code isInstinctHidden(viewer, target)}; never SparkTraits' implementation or component packages. SparkTraits is
 * fused into SparkDLC, so the call is direct; a null viewer or target changes nothing ("not hidden").
 * 跨模块接缝（SparkTraits）：内应自有的客户端读取，仅经由公共门面 {@code SparkTraitsApi} 的
 * {@code isInstinctHidden(viewer, target)}，从不触及 SparkTraits 的实现或组件包。SparkTraits 已融合进 SparkDLC，
 * 因此为直接调用；观察者或目标为空时不改变任何结果（“不隐藏”）。
 */
public final class InsiderSparkTraitsBridge {
    private InsiderSparkTraitsBridge() {
    }

    /** SparkTraits hides the target from this viewer's instinct. / SparkTraits 是否对该观察者的本能隐藏目标。 */
    public static boolean isInstinctHidden(@Nullable PlayerEntity viewer, @Nullable PlayerEntity target) {
        return viewer != null && target != null && SparkTraitsApi.isInstinctHidden(viewer, target);
    }
}
