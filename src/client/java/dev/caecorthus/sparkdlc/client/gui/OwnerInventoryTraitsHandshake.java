package dev.caecorthus.sparkdlc.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * The owner inventory card's side of the V1 handshake with the trait feature: it offers itself as the presenter of
 * the owner's traits until the trait feature accepts (the first registrant wins, and a rejection may only mean that
 * the trait feature's client side is not initialised yet, so every frame retries), then collects the traits of each
 * frame as one complete snapshot or none at all. The two calls of the trait contract
 * ({@code SparkTraitsApi.registerExternalInventoryPresenterV1} and {@code visitOwnerInventoryTraits}) are handed in as
 * method references, which keeps this protocol testable without Minecraft types. {@code P} is the player type,
 * {@code E} the text type of a trait name and of its tooltip lines.
 * 本人物品栏卡片在与词条功能的 V1 握手中的一侧：它持续申请成为本人词条的展示方，直到词条功能接受（先注册者胜出；被拒绝可能
 * 只是词条功能的客户端尚未初始化，因此每帧重试），随后把每一帧的词条收集为一份完整快照，否则一份也不留。词条契约的两个调用
 * （{@code SparkTraitsApi.registerExternalInventoryPresenterV1} 与 {@code visitOwnerInventoryTraits}）以方法引用传入，
 * 使该协议无需 Minecraft 类型即可测试。{@code P} 为玩家类型，{@code E} 为词条名及其提示行的文本类型。
 */
public final class OwnerInventoryTraitsHandshake<P, E> {
    private final Predicate<BooleanSupplier> register;
    private final BiConsumer<P, BiConsumer<E, List<E>>> visit;
    private final BooleanSupplier ownership;
    private boolean accepted;
    private boolean disabled;

    public OwnerInventoryTraitsHandshake(Predicate<BooleanSupplier> register, BiConsumer<P, BiConsumer<E, List<E>>> visit,
                                         BooleanSupplier ownership) {
        this.register = register;
        this.visit = visit;
        this.ownership = ownership;
    }

    /** Empty Optional means failure/unavailable; Optional.of(empty list) is a successful empty visit. */
    public <T> Optional<List<T>> collect(P player, BiFunction<E, List<E>, T> copyEntry) {
        if (disabled) return Optional.empty();
        try {
            // A false response may simply mean the trait client step (registerTraitClient) has not run yet.
            if (!accepted) accepted = register.test(ownership);
            if (!accepted) return Optional.empty();
            List<T> entries = new ArrayList<>();
            visit.accept(player, (tag, tooltip) -> entries.add(copyEntry.apply(tag, tooltip)));
            return Optional.of(List.copyOf(entries));
        } catch (RuntimeException failure) {
            // Not a lookup guard: a failing collection or visitor must leave no partial snapshot, so the trait card
            // stays native for this frame. / 并非查找保护：收集或访问失败时不得留下部分快照，本帧天赋卡保留原生卡片。
            return Optional.empty();
        }
    }

    public void disable() { disabled = true; }
    public void reset() { disabled = false; }
}
