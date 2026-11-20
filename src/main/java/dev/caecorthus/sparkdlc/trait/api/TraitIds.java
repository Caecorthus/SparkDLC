package dev.caecorthus.sparkdlc.trait.api;

import dev.caecorthus.sparkdlc.trait.civilian.impostor.ImpostorTrait;
import dev.caecorthus.sparkdlc.trait.civilian.police.PoliceTraits;
import dev.caecorthus.sparkdlc.trait.killer.KillerTraits;
import dev.caecorthus.sparkdlc.trait.killer.conscience.ConscienceTrait;
import net.minecraft.util.Identifier;

/**
 * Ids of the traits that roles and HUDs ask {@link SparkTraitsApi#hasActiveTrait} about by name. Each constant is the
 * owning trait's own id, so a trait id is defined once; code outside the trait feature names a trait through this
 * class instead of spelling the id again or reaching into the trait's package.
 * 职业与 HUD 通过 {@link SparkTraitsApi#hasActiveTrait} 按名称查询的天赋 id。每个常量就是所属天赋自身的 id，因此一个天赋 id
 * 只定义一次；天赋功能之外的代码通过本类点名天赋，而不是再次拼写 id 或深入天赋的包。
 *
 * <p>"Does this player hold trait X" has one spelling outside the trait feature:
 * {@code SparkTraitsApi.hasActiveTrait(player, TraitIds.X)}. The per-trait members the facade briefly carried
 * ({@code hasImpostor}, {@code hasConscience}, {@code hasTeamFirst}, {@code hasMasterSaboteur}) were exactly that call
 * and are folded into it. The trait feature's own {@code EffectiveTraitService.hasImpostor / hasConscience} are a
 * different read (they require the trait component and take a trait collection too) and stay inside the feature.
 * “该玩家是否持有天赋 X”在天赋功能之外只有一种写法：{@code SparkTraitsApi.hasActiveTrait(player, TraitIds.X)}。门面上曾短暂存在的
 * 按天赋成员（{@code hasImpostor}、{@code hasConscience}、{@code hasTeamFirst}、{@code hasMasterSaboteur}）恰好就是这一调用，
 * 已并入其中。天赋功能自身的 {@code EffectiveTraitService.hasImpostor / hasConscience} 是另一种读取（要求天赋组件存在，
 * 并可接受天赋集合），仍留在该功能内部。</p>
 */
public final class TraitIds {
    /** Impostor (内鬼): a civilian whose effective alignment is the killers'. Owner-synced, so the query also answers
     *  for the local player on a client.
     *  内鬼：有效阵营为杀手的平民。该词条同步给本人，因此客户端也能回答本地玩家。 */
    public static final Identifier IMPOSTOR = ImpostorTrait.ID;
    /** Conscience (良知): a killer whose effective alignment is the passengers'. Owner-synced like Impostor, so client
     *  shop listings and server validation agree.
     *  良知：有效阵营为乘客的杀手。与内鬼一样同步给本人，保证客户端商店列表与服务端校验一致。 */
    public static final Identifier CONSCIENCE = ConscienceTrait.ID;
    /** Team First (团队至上): the killer team purse asks on every income. / 团队至上：杀手团队钱包每次入账时查询。 */
    public static final Identifier TEAM_FIRST = KillerTraits.TEAM_FIRST;
    /** Master Saboteur (破坏大师): the sabotage system asks when an outage or a junction-box channel starts.
     *  破坏大师：破坏系统在停电或接线盒引导开始时查询。 */
    public static final Identifier MASTER_SABOTEUR = KillerTraits.MASTER_SABOTEUR;
    /** Fast Reload (快速装填), a police gun trait. / 快速装填，警用枪械天赋。 */
    public static final Identifier FAST_RELOAD = PoliceTraits.FAST_RELOAD;

    private TraitIds() {
    }
}
