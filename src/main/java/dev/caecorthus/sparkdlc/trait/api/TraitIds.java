package dev.caecorthus.sparkdlc.trait.api;

import dev.caecorthus.sparkdlc.trait.civilian.impostor.ImpostorTrait;
import dev.caecorthus.sparkdlc.trait.civilian.police.PoliceTraits;
import dev.caecorthus.sparkdlc.trait.killer.conscience.ConscienceTrait;
import net.minecraft.util.Identifier;

/**
 * Ids of the traits that roles and HUDs ask {@link SparkTraitsApi#hasActiveTrait} about by name. Each constant is the
 * owning trait's own id, so a trait id is defined once; code outside the trait feature names a trait through this
 * class instead of spelling the id again or reaching into the trait's package.
 * 职业与 HUD 通过 {@link SparkTraitsApi#hasActiveTrait} 按名称查询的天赋 id。每个常量就是所属天赋自身的 id，因此一个天赋 id
 * 只定义一次；天赋功能之外的代码通过本类点名天赋，而不是再次拼写 id 或深入天赋的包。
 */
public final class TraitIds {
    /** Impostor (内鬼): a civilian whose effective alignment is the killers'. / 内鬼：有效阵营为杀手的平民。 */
    public static final Identifier IMPOSTOR = ImpostorTrait.ID;
    /** Conscience (良知): a killer whose effective alignment is the passengers'. / 良知：有效阵营为乘客的杀手。 */
    public static final Identifier CONSCIENCE = ConscienceTrait.ID;
    /** Fast Reload (快速装填), a police gun trait. / 快速装填，警用枪械天赋。 */
    public static final Identifier FAST_RELOAD = PoliceTraits.FAST_RELOAD;

    private TraitIds() {
    }
}
