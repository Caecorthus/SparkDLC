package dev.caecorthus.sparkdlc.mixin.legacy;

import dev.caecorthus.sparkdlc.legacy.LegacySavedData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.internal.base.AbstractComponentContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gap: Cardinal Components reads only the keys a container knows and drops the rest, so after the rename to
 * {@code sparkdlc:} every setting, ledger and round state saved under an old component key would be lost on the first
 * load (logged once, never written back). {@code AbstractComponentContainer#fromTag} is the one method every
 * container reads through (entities and players, the world component file, the scoreboard and its teams: the
 * generated container classes do not override it, checked against CCA 6.1.1), so this single hook rewrites the
 * component map before CCA looks at it: old keys move to the current keys, and old ids inside the listed values
 * (ours and Wathe's {@code DisabledRoles}, role history and round-end rows) become current ids.
 *
 * <p>The target is a CCA internal class. {@code require = 1} turns an upstream change into a start-up failure (the
 * headless game test audits every mixin) instead of silent data loss; the fallback would be three vanilla hooks
 * ({@code Entity#readNbt}, {@code ScoreboardState#readNbt} and the world persistent-state load). Remove this mixin
 * together with {@link LegacySavedData} once no world saved before the single namespace has to load any more.</p>
 *
 * 缺口：Cardinal Components 只读取容器认识的键，其余一律丢弃，所以改名到 {@code sparkdlc:} 之后，保存在旧组件键下的所有设置、
 * 台账和对局状态都会在第一次读取时丢失（只记一次日志，不会写回）。{@code AbstractComponentContainer#fromTag} 是所有容器读取
 * 的唯一入口（实体与玩家、世界组件文件、计分板及其队伍：生成的容器类不重写它，已对照 CCA 6.1.1 核实），因此这一个钩子在
 * CCA 查看组件表之前改写它：旧键移到当前键，列出的值内部的旧 id（我们自己的以及 Wathe 的 {@code DisabledRoles}、职业历史、
 * 结算行）变为当前 id。
 *
 * <p>目标是 CCA 的内部类。{@code require = 1} 让上游改动变成启动失败（无界面游戏测试会审计每个 mixin），而不是悄悄丢数据；
 * 备选方案是三个原版钩子（{@code Entity#readNbt}、{@code ScoreboardState#readNbt} 与世界持久化状态的读取）。当不再需要读取
 * 统一命名空间之前保存的世界时，连同 {@link LegacySavedData} 一起删除本 mixin。</p>
 */
@Mixin(value = AbstractComponentContainer.class, remap = false)
abstract class ComponentContainerLegacyDataMixin {
    @Inject(method = "fromTag", at = @At("HEAD"), require = 1)
    private void sparkdlc$migrateLegacySavedData(
            NbtCompound tag,
            RegistryWrapper.WrapperLookup registryLookup,
            CallbackInfo ci
    ) {
        LegacySavedData.migrateContainerTag(tag);
    }
}
