package dev.caecorthus.sparkdlc.mixin.legacy;

import dev.caecorthus.sparkdlc.legacy.LegacyDataNames;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gap: the markers and compounds the mod keeps in an item's {@code minecraft:custom_data} were renamed on 2026-11-20
 * (owner: "就数据可以改"), and a stack saved before that (a chest, a player file, a structure, a command block) still
 * holds them under the old name. The private constructor of {@code NbtComponent} is the one place every such value
 * is built through ({@code CODEC} and {@code PACKET_CODEC} decoding, {@code of}, {@code apply}, {@code set}), so this
 * hook renames the old top-level keys there ({@link LegacyDataNames#withCurrentKeys}): the readers only know the
 * current names, and the stack carries them from then on. Nothing is copied when no old key is present; a compound
 * that has one is replaced by a renamed copy, so the caller's compound is never changed. Remove together with the
 * {@code private_data_name} rows once no world saved before 2026-11-20 has to load any more.
 *
 * 缺口：本模组保存在物品 {@code minecraft:custom_data} 中的标记与复合标签于 2026-11-20 改名（所有者：“就数据可以改”），而此前保存的
 * 物品堆（箱子、玩家文件、结构、命令方块）仍以旧名字保存它们。{@code NbtComponent} 的私有构造器是构建这类值的唯一入口
 * （{@code CODEC} 与 {@code PACKET_CODEC} 解码、{@code of}、{@code apply}、{@code set}），因此本钩子在这里把旧的顶层键改为当前名字
 * （{@link LegacyDataNames#withCurrentKeys}）：读取方只认识当前名字，物品堆此后也带着当前名字。没有旧键时不复制；有旧键时用改名后的
 * 副本替换，调用方的复合标签绝不被修改。当不再需要读取 2026-11-20 之前保存的世界时，连同 {@code private_data_name} 行一起删除。
 */
@Mixin(NbtComponent.class)
abstract class NbtComponentLegacyDataNamesMixin {
    @Shadow
    @Final
    @Mutable
    private NbtCompound nbt;

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void sparkdlc$renameLegacyDataNames(NbtCompound nbt, CallbackInfo ci) {
        this.nbt = LegacyDataNames.withCurrentKeys(this.nbt);
    }
}
