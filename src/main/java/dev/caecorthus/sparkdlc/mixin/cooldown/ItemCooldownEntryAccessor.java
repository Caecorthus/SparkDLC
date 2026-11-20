package dev.caecorthus.sparkdlc.mixin.cooldown;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * The mod's one accessor on a vanilla item cooldown entry: reads its tick bounds and (via {@link Mutable}) rewrites
 * the final ones. Writers are few and exact: a forced cooldown corrected in place after any {@code set} modifiers ran
 * (server only), and the exact server-clock locks of {@code util.ItemCooldownSync} (on the client, the Vulture
 * skateboard's slot sweep, drawn from its synced lock window). Every other cooldown (the Toxicologist's antidote
 * discount and Saint Karma included) only reads here and still writes through {@code ItemCooldownManager.set}.
 * 本 mod 在单个原版物品冷却条目上唯一的访问器：读取其起止 tick，并（借助 {@link Mutable}）改写这两个 final 字段。写入方很少且
 * 精确：强制冷却在 {@code set} 的倍率执行后原地校正（仅服务端），以及 {@code util.ItemCooldownSync} 按服务端时钟维护的精确锁
 * （客户端则是按同步的锁区间绘制的秃鹫滑板栏位冷却条）。其他冷却（包括毒理学家的解毒剂冷却抵扣与圣徒业障）在这里只读取，
 * 写入仍走 {@code ItemCooldownManager.set}。
 *
 * <p>Until the helper de-duplication this was four accessors, one per former mod; this is the union of their members
 * (both fields were already made mutable by two of the four).
 * 在辅助类去重之前，这里是四个访问器，每个旧 mod 一个；此处是它们成员的并集（四个中的两个本就把这两个字段设为可写）。</p>
 */
@Mixin(targets = "net.minecraft.entity.player.ItemCooldownManager$Entry")
public interface ItemCooldownEntryAccessor {
    @Accessor("startTick")
    int sparkdlc$getStartTick();

    @Accessor("endTick")
    int sparkdlc$getEndTick();

    @Mutable
    @Accessor("startTick")
    void sparkdlc$setStartTick(int startTick);

    @Mutable
    @Accessor("endTick")
    void sparkdlc$setEndTick(int endTick);
}
