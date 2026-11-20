package dev.caecorthus.sparkdlc.faction.api;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

/**
 * A veto for one action: {@code action} from any actor is denied on the targets {@code protectedTarget} accepts, and
 * everything else passes. Register it with {@link SparkFactionApi#registerPlayerAffectPolicy}. A plain class, so
 * equality is object identity: every instance is its own policy.
 * 针对单个动作的否决：任何人对 {@code protectedTarget} 认可的目标执行 {@code action} 都会被拒绝，其余一律放行。通过
 * {@link SparkFactionApi#registerPlayerAffectPolicy} 注册。使用普通类，相等性即对象身份：每个实例都是独立的策略。
 */
public final class DenyActionPolicy implements PlayerAffectPolicy {
    private final Identifier action;
    private final Predicate<PlayerEntity> protectedTarget;

    public DenyActionPolicy(Identifier action, Predicate<PlayerEntity> protectedTarget) {
        this.action = action;
        this.protectedTarget = protectedTarget;
    }

    @Override
    public boolean canAffect(PlayerEntity actor, PlayerEntity target, Identifier actionId,
                             GameWorldComponent gameComponent) {
        return !(action.equals(actionId) && target != null && protectedTarget.test(target));
    }

    /** The text is kept from before the fusion, when only the former SparkStrength registered such vetoes.
     *  文本沿用融合之前的写法，当时只有原 SparkStrength 注册这种否决。 */
    @Override
    public String toString() {
        return "SparkStrength deny " + action;
    }
}
