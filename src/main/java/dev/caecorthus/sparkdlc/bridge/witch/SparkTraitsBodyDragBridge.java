package dev.caecorthus.sparkdlc.bridge.witch;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Body query through SparkTraits' public API only. SparkTraits is fused into SparkDLC, so the call is direct.
 * 仅通过 SparkTraits 公共 API 查询尸体。SparkTraits 已融合进 SparkDLC，因此为直接调用。
 */
public final class SparkTraitsBodyDragBridge {
    private SparkTraitsBodyDragBridge() {
    }

    /** Drags any body SparkTraits does not report as a fake-death body.
     * 可拖动 SparkTraits 未报告为假死尸体的任意尸体。 */
    public static boolean canDragBody(Entity body) {
        // SparkTraits always ships in this jar: "loaded" is constant. / SparkTraits 始终随本 jar 发布：“已加载”恒为真。
        return canDragFromQuery(true, SparkTraitsApi.isFakeDeathBody(body));
    }

    /**
     * True only when SparkTraits positively reports a Depression fake-death body, so Death Sense treats every other
     * body as real.
     * 仅当 SparkTraits 明确报告为抑郁假死尸体时返回 true，死亡感知把其他尸体一律视为真尸体。
     */
    public static boolean isConfirmedFakeDeathBody(Entity body) {
        return SparkTraitsApi.isFakeDeathBody(body);
    }

    static boolean canDragFromQuery(boolean loaded, @Nullable Boolean fakeDeathBody) {
        return !loaded || Boolean.FALSE.equals(fakeDeathBody);
    }
}
