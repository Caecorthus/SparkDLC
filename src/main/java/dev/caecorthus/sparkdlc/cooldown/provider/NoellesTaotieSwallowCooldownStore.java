package dev.caecorthus.sparkdlc.cooldown.provider;

import dev.caecorthus.sparkdlc.cooldown.api.RoleSkillCooldownStore;
import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesRoleIds;
import dev.caecorthus.sparkdlc.mixin.compat.noellesroles.TaotiePlayerComponentAccessor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.taotie.TaotiePlayerComponent;

import java.util.OptionalInt;
import java.util.Set;

/**
 * NoellesRoles Taotie swallow cooldown (pinned 1.7.6: {@code getSwallowCooldown}, and {@code setSwallowCooldown},
 * which writes exactly and syncs). The nominal is the private, per-round {@code calculatedSwallowCooldown} that every
 * swallow writes, read through {@code TaotiePlayerComponentAccessor}: {@code initializeForGame} sets it, and
 * before that it holds the public {@code SWALLOW_COOLDOWN} default, which a swallow would also write. Non-positive
 * means unknown.
 * NoellesRoles 饕餮吞噬冷却（锁定 1.7.6：getSwallowCooldown 与精确写入并同步的 setSwallowCooldown）。标准冷却是每次吞噬
 * 写入的私有按回合值 calculatedSwallowCooldown，经 TaotiePlayerComponentAccessor 读取：由 initializeForGame 设置，
 * 在此之前为公开默认值 SWALLOW_COOLDOWN，此时吞噬同样写入该值。非正数视为未知。
 */
final class NoellesTaotieSwallowCooldownStore implements RoleSkillCooldownStore {
    static final Identifier ID = Identifier.of(NoellesRoleIds.NAMESPACE, "taotie_swallow");
    static final Identifier ROLE_ID = Identifier.of(NoellesRoleIds.NAMESPACE, "taotie");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public boolean appliesTo(ServerPlayerEntity player) {
        return ForcedCooldownRoles.selfRoleIn(player, Set.of(ROLE_ID)) != null;
    }

    @Override
    public int remainingTicks(ServerPlayerEntity player) {
        return TaotiePlayerComponent.KEY.get(player).getSwallowCooldown();
    }

    @Override
    public OptionalInt nominalTicks(ServerPlayerEntity player) {
        int calculated = ((TaotiePlayerComponentAccessor) (Object) TaotiePlayerComponent.KEY.get(player))
                .sparkdlc$getCalculatedSwallowCooldown();
        return calculated > 0 ? OptionalInt.of(calculated) : OptionalInt.empty();
    }

    @Override
    public void setRemainingTicks(ServerPlayerEntity player, int ticks) {
        TaotiePlayerComponent.KEY.get(player).setSwallowCooldown(Math.max(0, ticks));
    }
}
