package dev.caecorthus.sparkdlc.trait.effective.alignment;

import dev.caecorthus.sparkdlc.trait.killer.conscience.ConscienceTrait;
import dev.caecorthus.sparkdlc.trait.civilian.impostor.ImpostorTrait;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.util.Identifier;

import java.util.Collection;

/** Pure effective-alignment rules for Conscience and Impostor.
 *  善良与内鬼的纯有效阵营规则。 */
public final class EffectiveAlignment {
    private EffectiveAlignment() {
    }

    public static boolean hasConscience(Collection<Identifier> traits) {
        return traits.contains(ConscienceTrait.ID);
    }

    public static boolean hasImpostor(Collection<Identifier> traits) {
        return traits.contains(ImpostorTrait.ID);
    }

    public static boolean isOriginalKiller(Role role) {
        return role != null && role.canUseKiller();
    }

    public static boolean isOriginalCivilian(Role role) {
        return role != null && role.isInnocent();
    }

    /** Resolves killer-team membership after the traits' alignment flips.
     *  在天赋阵营翻转后判断是否属于杀手阵营。 */
    public static boolean isEffectiveKiller(Role role, Collection<Identifier> traits) {
        if (hasImpostor(traits)) {
            return true;
        }
        if (hasConscience(traits)) {
            return false;
        }
        return isOriginalKiller(role);
    }

    /** Resolves civilian-team membership after the traits' alignment flips.
     *  在天赋阵营翻转后判断是否属于好人阵营。 */
    public static boolean isEffectiveCivilian(Role role, Collection<Identifier> traits) {
        if (hasConscience(traits)) {
            return true;
        }
        if (hasImpostor(traits)) {
            return false;
        }
        return isOriginalCivilian(role);
    }
}
