package dev.caecorthus.sparkdlc.faction.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * A rule that lets an action through whatever the policies say. {@link PlayerAffectPolicy} is deny-wins, so no policy
 * can state "this may not be vetoed"; the feature that owns such a rule registers it with
 * {@link SparkFactionApi#registerPlayerAffectExemption} from its own registration code. Exemptions are asked by
 * {@link SparkFactionApi#allowsPlayerAction} only, in registration order, before its missing-player and self shortcut
 * and before any policy, so either player may be null.
 * 无论策略如何都放行某个行为的规则。{@link PlayerAffectPolicy} 是“拒绝优先”的，没有任何策略能表达“这不可被否决”；拥有这类规则的
 * 功能在自己的注册代码中通过 {@link SparkFactionApi#registerPlayerAffectExemption} 注册。豁免只由
 * {@link SparkFactionApi#allowsPlayerAction} 询问，按注册顺序，早于其缺失玩家与自身捷径，也早于任何策略，因此两名玩家都可能为 null。
 */
@FunctionalInterface
public interface PlayerAffectExemption {
    boolean exempts(@Nullable PlayerEntity actor, @Nullable PlayerEntity target, Identifier actionId);
}
