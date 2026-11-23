package dev.caecorthus.sparkdlc.mixin.legacy;

import dev.caecorthus.sparkdlc.legacy.LegacySavedData;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gap: a persistent attribute modifier is saved in the entity's {@code attributes} under its id, and attribute
 * modifier ids are not registry entries, so no registry alias reaches them. The Hunter's fracture slow
 * ({@code sparkwitch:fracture_speed}) is such a modifier: after the rename the mod removes and replaces it by its
 * new id only, and a player saved with the old id would stay slowed for ever. This hook renames the old ids in the
 * list that is about to be read ({@code AttributeContainer#readNbt} is the only reader of saved attributes), so the
 * modifier comes back under the id the mod removes. Remove together with {@link LegacySavedData}.
 *
 * 缺口：持久属性修饰符以其 id 保存在实体的 {@code attributes} 中，而属性修饰符 id 不是注册表条目，注册表别名管不到它们。
 * 猎人的骨折减速（{@code sparkwitch:fracture_speed}）就是这样的修饰符：改名后模组只按新 id 移除和替换它，带着旧 id 存档的玩家
 * 会永远被减速。本钩子在即将读取的列表里把旧 id 改成新 id（{@code AttributeContainer#readNbt} 是存档属性的唯一读取入口），
 * 修饰符因此以模组会移除的 id 恢复。与 {@link LegacySavedData} 一同删除。
 */
@Mixin(AttributeContainer.class)
abstract class AttributeContainerLegacyModifierIdsMixin {
    @Inject(method = "readNbt", at = @At("HEAD"), require = 1)
    private void sparkdlc$migrateLegacyModifierIds(NbtList nbt, CallbackInfo ci) {
        LegacySavedData.migrateAttributes(nbt);
    }
}
