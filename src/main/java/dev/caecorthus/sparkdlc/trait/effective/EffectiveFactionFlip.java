package dev.caecorthus.sparkdlc.trait.effective;

import dev.caecorthus.sparkdlc.faction.api.FactionIds;
import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;

/**
 * The trait alignment flips as an effective-faction resolver of the faction contract: Impostor turns
 * {@code wathe:civilian} into {@code wathe:killer}, Conscience the reverse; every other faction is left alone.
 * 以阵营契约的有效阵营解析器形式提供词条的阵营翻转：内鬼把 {@code wathe:civilian} 变为 {@code wathe:killer}，善良则相反；
 * 其他阵营保持不变。
 */
public final class EffectiveFactionFlip {
    private static boolean registered;

    private EffectiveFactionFlip() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        SparkFactionApi.registerEffectiveFactionResolver(EffectiveFactionFlip::resolveEffectiveFaction);
    }

    private static Identifier resolveEffectiveFaction(
            PlayerEntity player,
            GameWorldComponent gameComponent,
            Identifier currentFaction
    ) {
        return resolveEffectiveFaction(EffectiveTraitService.effectiveTraitIds(player, gameComponent), currentFaction);
    }

    /**
     * The flipped faction, or null when these traits do not flip {@code currentFaction} (the resolver contract's
     * "no opinion"); null traits or faction give null. Impostor wins over Conscience.
     * 翻转后的阵营；这些词条不翻转 {@code currentFaction} 时为 null（解析器契约中的“无意见”）；词条或阵营为 null 时返回 null。
     * 内鬼优先于善良。
     */
    public static Identifier resolveEffectiveFaction(Collection<Identifier> traits, Identifier currentFaction) {
        if (traits == null || currentFaction == null) {
            return null;
        }
        if (EffectiveTraitService.hasImpostor(traits)) {
            return FactionIds.CIVILIAN.equals(currentFaction) ? FactionIds.KILLER : null;
        }
        if (EffectiveTraitService.hasConscience(traits)) {
            return FactionIds.KILLER.equals(currentFaction) ? FactionIds.CIVILIAN : null;
        }
        return null;
    }

}
