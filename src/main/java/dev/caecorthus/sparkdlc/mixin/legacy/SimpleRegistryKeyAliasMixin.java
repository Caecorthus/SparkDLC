package dev.caecorthus.sparkdlc.mixin.legacy;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.Optional;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Closes a gap in Fabric API's registry aliases on 1.21.1 (verified on registry-sync 5.3.1 of Fabric API 0.116.7;
 * the upstream 1.21.1 branch still has the same target, so updating Fabric API does not help). Fabric's
 * {@code SimpleRegistryMixin} redirects the lookups by {@link Identifier} through its alias table, but for the
 * lookup by {@link RegistryKey} it targets {@code getOptional(RegistryKey)}, the 1.21.2+ name: on 1.21.1 that
 * method is {@code getEntry(RegistryKey)}, the target matches nothing and nothing fails, because the other targets
 * of the same injector satisfy its {@code require}. Everything that resolves an id through a registry wrapper
 * therefore misses the aliases of {@code LegacyRegistryAliases}: {@code /give}, {@code /summon},
 * {@code /setblock}, {@code /effect}, {@code /particle}, command blocks and functions, block states in structure
 * files, and {@code RegistryFixedCodec} / {@code RegistryElementCodec} references such as {@code can_break}.
 *
 * <p>When the key lookup finds nothing, this asks the {@link Identifier} lookup of the same registry, which Fabric
 * does alias. A key that exists never gets here, an id that is neither registered nor an alias is still empty, and
 * a key of another registry is left alone. If Fabric fixes its own target the key arrives already translated, the
 * vanilla lookup succeeds and this does nothing. It serves every alias of every registry, not only SparkDLC's:
 * the gap is Fabric's, and Wathe's own {@code trainmurdermystery:} aliases get the same repair.</p>
 *
 * <p>Remove it when Fabric API aliases {@code SimpleRegistry#getEntry(RegistryKey)} on 1.21.1, when the mod moves to
 * 1.21.2+ (where Fabric's target exists; {@code require = 1} fails start-up there because this target is gone), or
 * together with the whole legacy package.</p>
 *
 * 修补 Fabric API 注册表别名在 1.21.1 上的缺口（已在 Fabric API 0.116.7 的 registry-sync 5.3.1 上核实；上游 1.21.1 分支的
 * 目标仍然相同，升级 Fabric API 无济于事）。Fabric 的 {@code SimpleRegistryMixin} 会让按 {@link Identifier} 的查找经过别名表，
 * 但按 {@link RegistryKey} 的查找它瞄准的是 1.21.2+ 才有的方法名 {@code getOptional(RegistryKey)}：在 1.21.1 上该方法叫
 * {@code getEntry(RegistryKey)}，目标匹配不到任何方法，却不会报错，因为同一注入器的其他目标已满足 {@code require}。
 * 于是所有经注册表包装器解析 id 的地方都用不上 {@code LegacyRegistryAliases} 的别名：{@code /give}、{@code /summon}、
 * {@code /setblock}、{@code /effect}、{@code /particle}、命令方块与函数、结构文件里的方块状态，以及 {@code can_break} 这类
 * {@code RegistryFixedCodec} / {@code RegistryElementCodec} 引用。
 *
 * <p>按键查找不到时，改问同一注册表按 {@link Identifier} 的查找（Fabric 已为它接入别名）。存在的键不会走到这里；既未注册也不是
 * 别名的 id 仍返回空；属于其他注册表的键不处理。若 Fabric 修好了自己的目标，键在进入时已被转换，原版查找成功，此处不做任何事。
 * 它对所有注册表的所有别名生效，不限于 SparkDLC：缺口在 Fabric，Wathe 自己的 {@code trainmurdermystery:} 别名也一并修复。</p>
 *
 * <p>移除时机：Fabric API 在 1.21.1 上为 {@code SimpleRegistry#getEntry(RegistryKey)} 接入别名时；模组迁到 1.21.2+ 时
 * （那里 Fabric 的目标存在；本目标消失，{@code require = 1} 会让启动失败）；或与整个 legacy 包一起移除。</p>
 */
@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryKeyAliasMixin<T> {
    @Shadow
    public abstract RegistryKey<? extends Registry<T>> getKey();

    @Shadow
    public abstract Optional<RegistryEntry.Reference<T>> getEntry(Identifier id);

    @ModifyReturnValue(
            method = "getEntry(Lnet/minecraft/registry/RegistryKey;)Ljava/util/Optional;",
            at = @At("RETURN"),
            require = 1
    )
    private Optional<RegistryEntry.Reference<T>> sparkdlc$resolveAliasedKey(
            Optional<RegistryEntry.Reference<T>> original, RegistryKey<T> key) {
        if (original.isPresent() || key == null || !key.isOf(this.getKey())) {
            return original;
        }
        return this.getEntry(key.getValue());
    }
}
