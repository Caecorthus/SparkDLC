package dev.caecorthus.sparkdlc.witch.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * V1 owner-inventory handshake with SparkTraits. The two facade calls ({@code registerExternalInventoryPresenterV1}
 * and {@code visitOwnerInventoryTraits}) are handed in as method references, so they are direct, compiled calls
 * (SparkTraits is fused into SparkDLC) while this class names no Traits type. {@code P} is the player type, {@code E}
 * the text type of a trait name and of its tooltip lines.
 * 与 SparkTraits 的 V1 本人物品栏握手。两个门面调用（{@code registerExternalInventoryPresenterV1} 与
 * {@code visitOwnerInventoryTraits}）以方法引用传入，因此是直接的编译期调用（SparkTraits 已融合进 SparkDLC），而本类不引用
 * 任何 Traits 类型。{@code P} 为玩家类型，{@code E} 为词条名及其提示行的文本类型。
 */
public final class TraitsInventoryBridge<P, E> {
    private final Predicate<BooleanSupplier> register;
    private final BiConsumer<P, BiConsumer<E, List<E>>> visit;
    private final BooleanSupplier ownership;
    private boolean accepted;
    private boolean disabled;

    public TraitsInventoryBridge(Predicate<BooleanSupplier> register, BiConsumer<P, BiConsumer<E, List<E>>> visit,
                                 BooleanSupplier ownership) {
        this.register = register;
        this.visit = visit;
        this.ownership = ownership;
    }

    /** Empty Optional means failure/unavailable; Optional.of(empty list) is a successful empty visit. */
    public <T> Optional<List<T>> collect(P player, BiFunction<E, List<E>, T> copyEntry) {
        if (disabled) return Optional.empty();
        try {
            // A false response may simply mean Traits' client initializer has not run yet.
            if (!accepted) accepted = register.test(ownership);
            if (!accepted) return Optional.empty();
            List<T> entries = new ArrayList<>();
            visit.accept(player, (tag, tooltip) -> entries.add(copyEntry.apply(tag, tooltip)));
            return Optional.of(List.copyOf(entries));
        } catch (RuntimeException failure) {
            // Not a lookup guard: a failing collection or visitor must leave no partial snapshot, so Traits keeps its
            // native card for this frame. / 并非查找保护：收集或访问失败时不得留下部分快照，本帧由 Traits 保留原生卡片。
            return Optional.empty();
        }
    }

    public void disable() { disabled = true; }
    public void reset() { disabled = false; }
}
