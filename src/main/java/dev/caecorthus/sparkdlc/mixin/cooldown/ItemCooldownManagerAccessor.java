package dev.caecorthus.sparkdlc.mixin.cooldown;

import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * The mod's one accessor on vanilla {@link ItemCooldownManager}: the cooldown entries and the manager's own tick
 * counter, which the public API only lets callers set or remove as a whole. Readers: forced cooldowns (written exactly,
 * bypassing duration modifiers that hook {@code ItemCooldownManager.set}), the exact server-clock locks of
 * {@code util.ItemCooldownSync}, the Toxicologist's antidote discount (shortens the running cooldown by the number of
 * poisoned players and keeps the rest), Saint Karma (extends but never shortens an existing cooldown), the traits'
 * exact cooldown writes, and the client HUDs that draw the remaining ticks. Names carry the {@code sparkdlc$} prefix
 * because NoellesRoles declares accessors on the same class.
 * 本 mod 在原版 {@link ItemCooldownManager} 上唯一的访问器：冷却条目与管理器自身的 tick 计数；公开 API 只能整体设置或移除。
 * 读取方：强制冷却（越过挂在 {@code ItemCooldownManager.set} 上的时长倍率精确写入）、{@code util.ItemCooldownSync} 按服务端
 * 时钟维护的精确锁、毒理学家的解毒剂冷却抵扣（按中毒人数缩短当前冷却并保留剩余抵扣）、圣徒业障（只延长、不缩短已有冷却）、
 * 特质的精确冷却写入，以及绘制剩余 tick 的客户端 HUD。方法名带 {@code sparkdlc$} 前缀，因为 NoellesRoles 也在同一类上声明了
 * 访问器。
 *
 * <p>Until the helper de-duplication this was four identical accessors, one per former mod.
 * 在辅助类去重之前，这里是四个完全相同的访问器，每个旧 mod 一个。</p>
 */
@Mixin(ItemCooldownManager.class)
public interface ItemCooldownManagerAccessor {
    /** Values are {@code ItemCooldownManager$Entry}; cast them to {@link ItemCooldownEntryAccessor}. / 值为 Entry，需转为条目访问器。 */
    @Accessor("entries")
    Map<Item, Object> sparkdlc$getEntries();

    @Accessor("tick")
    int sparkdlc$getTick();
}
