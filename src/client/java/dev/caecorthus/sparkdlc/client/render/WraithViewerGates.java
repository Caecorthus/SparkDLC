package dev.caecorthus.sparkdlc.client.render;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;

/**
 * Gates other features install to reveal active Wraiths' bodies to chosen viewers (today the living Spiritualist).
 * A gate is asked {@code (viewer, wraith)} on the render thread, only for an active Wraith (promoted ones included)
 * and never for the Wraith itself; any gate answering true draws that Wraith's body translucent for that viewer, as
 * vanilla does for a spectator. It reveals the body only: held items, outlines, instinct colour, name tags and aim
 * pass-through keep their ordinary-viewer rules.
 * 其他功能安装的闸门：向指定观察者（目前为存活的灵界行者）显示活跃冤魂（含晋升形态）的身体。闸门在渲染线程以
 * {@code (观察者, 冤魂)} 调用，只针对活跃冤魂且从不针对冤魂本人；任一闸门返回 true，该观察者就会像原版旁观者那样看到
 * 半透明的冤魂身体。只显示身体：手持物、描边、本能颜色、名字与准星穿透仍按普通观察者规则处理。
 */
public final class WraithViewerGates {
    private static final List<BiPredicate<PlayerEntity, PlayerEntity>> GATES = new CopyOnWriteArrayList<>();

    private WraithViewerGates() {
    }

    /**
     * Adds {@code gate}; gates are OR'd and never removed, except one that throws, which is dropped after a warning.
     * A null gate throws {@link NullPointerException}.
     * 添加 {@code gate}；多个闸门按“或”合并且不会被移除，抛出异常的闸门在记录警告后被丢弃。参数为 null 时抛出
     * {@link NullPointerException}。
     */
    public static void add(BiPredicate<PlayerEntity, PlayerEntity> gate) {
        GATES.add(Objects.requireNonNull(gate, "gate"));
    }

    /**
     * Render thread: whether an installed gate reveals active Wraith {@code target}'s body to {@code viewer}. Never
     * true for the Wraith itself or a target that is not an active Wraith. A gate that throws is dropped.
     * 渲染线程：已安装的闸门是否向 {@code viewer} 显示活跃冤魂 {@code target} 的身体。冤魂本人或非活跃冤魂的目标
     * 永远为 false。抛出异常的闸门会被丢弃。
     */
    public static boolean revealsBody(PlayerEntity viewer, PlayerEntity target) {
        if (GATES.isEmpty() || viewer == null || target == null || viewer == target
                || !WraithClientState.isActive(target)) {
            return false;
        }
        for (BiPredicate<PlayerEntity, PlayerEntity> gate : GATES) {
            try {
                if (gate.test(viewer, target)) {
                    return true;
                }
            } catch (RuntimeException | LinkageError failure) {
                GATES.remove(gate);
                SparkDLC.LOGGER.warn("Dropping a Wraith viewer gate that threw", failure);
            }
        }
        return false;
    }
}
