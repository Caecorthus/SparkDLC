package dev.caecorthus.sparkdlc.mixin.role.civilian.demonhunter;

import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterPistolRules;
import org.agmas.noellesroles.demonhunter.DemonHunterPistolItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Points the Demon Hunter pistol's tooltip lines the 2026-11-28 rework made wrong at the mod's own keys (see
 * {@link DemonHunterPistolRules#tooltipKey}; the Bartender's ingredient tooltip precedent). Every other line keeps
 * NoellesRoles' key.
 * 把 2026-11-28 重做后不再正确的猎魔枪提示行指向本模组自己的键（见 {@link DemonHunterPistolRules#tooltipKey}；沿用酒保调剂提示
 * 的先例）。其余各行保留 NoellesRoles 的键。
 */
@Mixin(DemonHunterPistolItem.class)
public abstract class DemonHunterPistolTooltipMixin {
    @ModifyArg(
            method = "appendTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;")
    )
    private String sparkdlc$retunedPistolTip(String key) {
        return DemonHunterPistolRules.tooltipKey(key);
    }
}
