package dev.caecorthus.sparkdlc.cooldown.api;

import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The admin clear of one item cooldown ({@code /sparkdlc:clearCooldown}), as the features see it that keep a floor,
 * a lock or a timer of their own behind an item's vanilla cooldown. The command reads whether the item was cooling
 * down, runs the {@link BeforeRemove} listeners, removes the vanilla cooldown and runs the {@link AfterRemove}
 * listeners. Listeners register during mod initialization and run in registration order, which is observable: a
 * later {@link AfterRemove} listener sees what an earlier one did to the cooldown. A listener that throws stops the
 * clear for that player and the exception reaches the command. Server thread only. Role mechanics that remove a
 * cooldown (refreshes, kill resets) never come through here.
 * 管理员对单个物品冷却的清除（{@code /sparkdlc:clearCooldown}），从那些在物品原版冷却背后另有下限、锁或计时的功能的角度看。
 * 命令先读取物品是否处于冷却，运行 {@link BeforeRemove} 监听器，移除原版冷却，再运行 {@link AfterRemove} 监听器。监听器在
 * 模组初始化时注册，按注册顺序运行，且顺序可观察：靠后的 {@link AfterRemove} 监听器看到的是靠前者处理之后的冷却状态。监听器
 * 抛出异常会中止对该玩家的清除，异常会到达命令。仅限服务端线程。移除冷却的职业机制（刷新、击杀重置）不会经过这里。
 */
public final class AdminCooldownClear {
    private static final List<BeforeRemove> BEFORE_REMOVE = new CopyOnWriteArrayList<>();
    private static final List<AfterRemove> AFTER_REMOVE = new CopyOnWriteArrayList<>();

    private AdminCooldownClear() {
    }

    /**
     * Runs right before the command removes {@code item}'s vanilla cooldown for {@code player}: for a feature whose
     * own state would otherwise cancel or undo that remove.
     * 在命令为 {@code player} 移除 {@code item} 的原版冷却之前运行：供自身状态否则会取消或撤销该移除的功能使用。
     */
    @FunctionalInterface
    public interface BeforeRemove {
        void beforeRemove(ServerPlayerEntity player, Item item);
    }

    /**
     * Runs right after the remove. {@code wasCooling} is whether the item was cooling down before the
     * {@link BeforeRemove} listeners ran; whether the clear took effect is the listener's own question at that
     * moment ({@link #tookEffect}).
     * 在移除之后运行。{@code wasCooling} 表示在 {@link BeforeRemove} 监听器运行之前该物品是否处于冷却；清除是否生效由监听器
     * 当时自行询问（{@link #tookEffect}）。
     */
    @FunctionalInterface
    public interface AfterRemove {
        void afterRemove(ServerPlayerEntity player, Item item, boolean wasCooling);
    }

    public static void registerBeforeRemove(BeforeRemove listener) {
        BEFORE_REMOVE.add(Objects.requireNonNull(listener, "listener"));
    }

    public static void registerAfterRemove(AfterRemove listener) {
        AFTER_REMOVE.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * Whether the clear took effect: {@code item} is not cooling down for {@code player} right now. For
     * {@link AfterRemove} listeners; each one asks at its own turn, so it sees what the listeners before it did.
     * 清除是否生效：此刻 {@code item} 对 {@code player} 不处于冷却。供 {@link AfterRemove} 监听器使用；每个监听器在轮到自己时
     * 询问，因此看到的是排在它之前的监听器处理之后的状态。
     */
    public static boolean tookEffect(ServerPlayerEntity player, Item item) {
        return !player.getItemCooldownManager().isCoolingDown(item);
    }

    public static void beforeRemove(ServerPlayerEntity player, Item item) {
        beforeRemove(BEFORE_REMOVE, player, item);
    }

    public static void afterRemove(ServerPlayerEntity player, Item item, boolean wasCooling) {
        afterRemove(AFTER_REMOVE, player, item, wasCooling);
    }

    public static void beforeRemove(Iterable<? extends BeforeRemove> listeners, ServerPlayerEntity player, Item item) {
        for (BeforeRemove listener : listeners) {
            listener.beforeRemove(player, item);
        }
    }

    public static void afterRemove(
            Iterable<? extends AfterRemove> listeners,
            ServerPlayerEntity player,
            Item item,
            boolean wasCooling
    ) {
        for (AfterRemove listener : listeners) {
            listener.afterRemove(player, item, wasCooling);
        }
    }
}
