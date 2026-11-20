package dev.caecorthus.sparkdlc.trait.civilian;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.trait.api.Trait;
import dev.caecorthus.sparkdlc.trait.api.TraitAudience;
import dev.caecorthus.sparkdlc.trait.api.TraitSelectionContext;
import dev.caecorthus.sparkdlc.trait.civilian.impostor.ImpostorTrait;
import dev.caecorthus.sparkdlc.trait.global.GlobalTraitService;
import net.minecraft.util.Identifier;

import java.util.Set;

/**
 * 焦虑症 / Anxiety disorder: tasks refresh 80% faster, and finishing one restores a quarter of the mood bar.
 * Passenger-only, incompatible with 多巴胺 (both ride the same task loop).
 * 焦虑症：任务刷新频率提升 80%，每完成一个任务恢复 25% 理智。乘客阵营专属，与多巴胺冲突（两者都改任务节奏）。
 */
public final class AnxietyTrait implements Trait {
    public static final Identifier ID = SparkDLC.id("anxiety");
    public static final int COLOR = 0xB07BE8;
    /** The next-task countdown runs this many times faster (+80% frequency). 下一个任务倒计时的加速倍率（频率提升 80%）。 */
    public static final float REFRESH_MULTIPLIER = 1.8F;
    /** Mood restored per completed task, as a fraction of the holder's own ceiling. 每完成一个任务恢复的理智比例。 */
    public static final float TASK_MOOD_GAIN = 0.25F;

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public int color() {
        return COLOR;
    }

    @Override
    public TraitAudience audience() {
        return TraitAudience.INNOCENT_ONLY;
    }

    @Override
    public Set<Identifier> incompatibleTraits() {
        return Set.of(ImpostorTrait.ID, DopamineTrait.ID);
    }

    /**
     * Same gate as the other civilian traits, plus a real mood bar: without one there are no real tasks to pace.
     * 与其他平民词条相同的门槛，并要求真理智：没有真理智就没有真正的任务可言。
     */
    @Override
    public boolean canApply(TraitSelectionContext context) {
        return Trait.super.canApply(context)
                && CivilianTraitService.canSelectCivilianTrait(context.role(), context.selectedTraitIds())
                && GlobalTraitService.hasEffectiveRealMood(context.role(), context.selectedTraitIds());
    }
}
