package dev.caecorthus.sparkdlc.client.bridge.strength;

import dev.caecorthus.sparkdlc.client.api.WraithViewerApi;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.BiPredicate;

/**
 * SparkWitch 冤魂观察者闸门的客户端桥。
 * Client-only bridge to SparkWitch's Wraith viewer gate.
 *
 * <p>各模块已融合进 SparkDLC，因此这里直接调用其公开门面 {@link WraithViewerApi#addViewerGate(BiPredicate)}，
 * 不再按名称反射，也不再检测 sparkwitch 是否加载。
 * The modules are fused in SparkDLC, so this calls that public facade directly, with no lookup by name and no
 * mod-loaded gate.</p>
 */
public final class SparkWitchWraithViewerCompat {
    private SparkWitchWraithViewerCompat() {
    }

    /**
     * Asks SparkWitch to reveal active Wraiths' bodies to viewers {@code gate} accepts ({@code (viewer, wraith)}, render
     * thread). Returns whether the gate was installed: always true since the SparkDLC fusion.
     * 请 SparkWitch 向 {@code gate} 接受的观察者显示活跃冤魂的身体（参数为 (观察者, 冤魂)，渲染线程）。返回是否安装成功：
     * 融合进 SparkDLC 后恒为 true。
     */
    public static boolean addViewerGate(BiPredicate<PlayerEntity, PlayerEntity> gate) {
        WraithViewerApi.addViewerGate(gate);
        return true;
    }
}
