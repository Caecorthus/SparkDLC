package dev.caecorthus.sparkdlc.trait.civilian;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.trait.api.TraitAudience;
import dev.caecorthus.sparkdlc.trait.api.TraitDefinition;
import dev.caecorthus.sparkdlc.trait.api.TraitRegistry;
import net.minecraft.util.Identifier;
import dev.caecorthus.sparkdlc.trait.civilian.depression.DepressionTrait;
import dev.caecorthus.sparkdlc.trait.global.GlobalTraitService;
import dev.caecorthus.sparkdlc.trait.civilian.impostor.ImpostorTrait;

/**
 * Registers traits reserved for original civilian-side roles.
 * 注册仅限原始平民阵营使用的天赋；运行时仍会排除内鬼天赋。
 */
public final class CivilianTraits {
    public static final Identifier EXTROVERTED = SparkDLC.id("extroverted");
    public static final Identifier INTROVERTED = SparkDLC.id("introverted");
    public static final Identifier MONEY_TREE = SparkDLC.id("money_tree");
    public static final Identifier FOCUS = SparkDLC.id("focus");
    public static final Identifier DEPRESSION = SparkDLC.id("depression");

    private CivilianTraits() {
    }

    public static void register() {
        TraitRegistry.register(base(EXTROVERTED, CivilianTraitService.EXTROVERTED_COLOR)
                .predicate(context -> CivilianTraitService.canSelectNonUndercoverCivilianTrait(
                        context.role(),
                        context.selectedTraitIds()
                ))
                .incompatibleWith(INTROVERTED)
                .build());
        TraitRegistry.register(base(INTROVERTED, CivilianTraitService.INTROVERTED_COLOR)
                .predicate(context -> CivilianTraitService.canSelectNonUndercoverCivilianTrait(
                        context.role(),
                        context.selectedTraitIds()
                ))
                .incompatibleWith(EXTROVERTED)
                .build());
        TraitRegistry.register(base(MONEY_TREE, CivilianTraitService.MONEY_TREE_COLOR)
                .predicate(context -> CivilianTraitService.canSelectMoneyTree(
                        context.role(),
                        context.selectedTraitIds(),
                        context.player() != null
                                && context.gameComponent() != null
                                && GlobalTraitService.canSeeMoneyForTrait(
                                        context.player(),
                                        context.gameComponent(),
                                        context.role()
                                )
                ))
                .build());
        TraitRegistry.register(base(FOCUS, CivilianTraitService.FOCUS_COLOR)
                .predicate(context -> CivilianTraitService.canSelectFocus(
                        context.role(),
                        context.selectedTraitIds()
                ))
                .build());
        TraitRegistry.register(new DepressionTrait());
        TraitRegistry.register(new AnxietyTrait());
        TraitRegistry.register(new MinimalismTrait());
        TraitRegistry.register(new DopamineTrait());
        TraitRegistry.register(new SelfEsteemTrait());
    }

    private static TraitDefinition.Builder base(Identifier id, int color) {
        return TraitDefinition.builder(id, color)
                .audience(TraitAudience.INNOCENT_ONLY)
                .incompatibleWith(ImpostorTrait.ID)
                .predicate(context -> CivilianTraitService.canSelectCivilianTrait(
                        context.role(),
                        context.selectedTraitIds()
                ));
    }
}
