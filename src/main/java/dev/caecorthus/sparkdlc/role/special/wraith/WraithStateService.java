package dev.caecorthus.sparkdlc.role.special.wraith;

import dev.caecorthus.sparkdlc.component.WraithPlayerComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/** Read-only access to synchronized Wraith runtime state. */
public final class WraithStateService {
    private WraithStateService() {
    }

    // Hot (collision, movement and door-shape mixins): a direct get, without the Optional and method reference of
    // maybeGet. The component is registered for every player, so it is null only for a null player.
    // 热路径（碰撞、移动与门形状 mixin）：直接 get，免去 maybeGet 的 Optional 与方法引用。该组件为每名玩家注册，
    // 因此只有玩家为 null 时才为 null。
    public static boolean isActive(@Nullable PlayerEntity player) {
        WraithPlayerComponent component = component(player);
        return component != null && component.isActive();
    }

    public static boolean isRestricted(@Nullable PlayerEntity player) {
        WraithPlayerComponent component = component(player);
        return component != null && component.isRestricted();
    }

    public static boolean isPromoted(@Nullable PlayerEntity player) {
        WraithPlayerComponent component = component(player);
        return component != null && component.isPromoted();
    }

    private static @Nullable WraithPlayerComponent component(@Nullable PlayerEntity player) {
        return player == null ? null : WraithPlayerComponent.KEY.get(player);
    }
}
