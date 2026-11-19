package dev.caecorthus.sparkdlc.client.bridge.strength;

import dev.caecorthus.sparkdlc.client.api.BlindViewApi;
import net.minecraft.util.math.Box;

import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

/**
 * SparkWitch 盲人视图透视描边的客户端桥。
 * Client-only bridge to SparkWitch's Blind see-through outlines.
 *
 * <p>SparkWitch 的盲人回声画面在全部世界渲染之后覆盖屏幕，{@code WorldRenderEvents.LAST} 画的描边到不了盲人眼前。
 * 各模块已融合进 SparkDLC，因此这里直接调用其冻结的公开门面 {@link BlindViewApi#addSeeThroughOutlines(Consumer)}，
 * 不再按名称反射，也不再检测 sparkwitch 是否加载。
 * SparkWitch's Blind echo view paints over the screen after all world rendering, so outlines drawn at
 * {@code WorldRenderEvents.LAST} never reach a Blind. The modules are fused in SparkDLC, so this calls that frozen
 * public facade directly, with no lookup by name and no mod-loaded gate.</p>
 */
public final class SparkWitchBlindViewCompat {
    private SparkWitchBlindViewCompat() {
    }

    /**
     * Hands {@code source} to SparkWitch: on the render thread, once per frame and only while the local Blind's echo
     * view is shown, it writes world-space boxes as {@code (box, 0xAARRGGBB)} into the sink and must not render.
     * Returns whether SparkWitch took it: true for every non-null source.
     * 把 {@code source} 交给 SparkWitch：仅在本地盲人的回声画面显示时，于渲染线程每帧调用一次，以
     * {@code (box, 0xAARRGGBB)} 向接收器写入世界坐标的包围盒，不得自行渲染。返回 SparkWitch 是否接收：非 null 的来源恒为 true。
     */
    public static boolean addSeeThroughOutlines(Consumer<ObjIntConsumer<Box>> source) {
        if (source == null) {
            return false;
        }
        BlindViewApi.addSeeThroughOutlines(source);
        return true;
    }
}
