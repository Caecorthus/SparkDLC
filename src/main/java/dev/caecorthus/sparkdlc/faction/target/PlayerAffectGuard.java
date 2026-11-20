package dev.caecorthus.sparkdlc.faction.target;

import dev.caecorthus.sparkdlc.faction.api.PlayerAffectExemption;
import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Shared guard of the player-affect adapters: the mixins into Wathe, vanilla and NoellesRoles ask it directly, and the
 * mod's own services ask it through {@code SparkFactionApi.allowsPlayerAction} at the head of their entry points. It
 * stays outside the Mixin-owned package because it is loaded directly, and it names no feature: a rule that must not
 * be vetoed is registered as a {@link PlayerAffectExemption}.
 * 玩家影响适配器共用的守卫：注入 Wathe、原版与 NoellesRoles 的 mixin 直接询问它，模组自有服务则在入口方法开头经
 * {@code SparkFactionApi.allowsPlayerAction} 询问。它被直接加载，因此不放进 Mixin 所有的包；它不点名任何功能：不可被否决的规则
 * 以 {@link PlayerAffectExemption} 注册。
 */
public final class PlayerAffectGuard {
    // Asked in registration order, first true wins. Copy-on-write like the policy list: a check iterates a snapshot.
    // 按注册顺序询问，第一个 true 生效。与策略列表一样写时复制：检查遍历快照。
    private static final List<PlayerAffectExemption> EXEMPTIONS = new CopyOnWriteArrayList<>();

    private PlayerAffectGuard() {
    }

    /**
     * Registers an exemption for every check that goes through this guard ({@code SparkFactionApi.canAffectPlayer}
     * asked directly does not see it).
     * 为所有经过本守卫的检查注册一条豁免（直接询问 {@code SparkFactionApi.canAffectPlayer} 时看不到它）。
     */
    public static void registerExemption(PlayerAffectExemption exemption) {
        EXEMPTIONS.add(Objects.requireNonNull(exemption, "exemption"));
    }

    public static boolean allows(PlayerEntity actor, PlayerEntity target, Identifier actionId) {
        // Exemptions answer true before any policy is asked. / 豁免在询问任何策略之前回答 true。
        if (isExempt(EXEMPTIONS, actor, target, actionId)) {
            return true;
        }
        if (actor == null || target == null || actor == target) {
            return true;
        }
        return SparkFactionApi.canAffectPlayer(
                actor,
                target,
                actionId,
                GameWorldComponent.KEY.get(actor.getWorld())
        );
    }

    /** True when one of {@code exemptions}, asked in order, exempts the action. / 按顺序询问，任一豁免放行即为 true。 */
    static boolean isExempt(
            Iterable<? extends PlayerAffectExemption> exemptions,
            PlayerEntity actor,
            PlayerEntity target,
            Identifier actionId
    ) {
        for (PlayerAffectExemption exemption : exemptions) {
            if (exemption.exempts(actor, target, actionId)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static ServerPlayerEntity onlineTarget(ServerPlayerEntity actor, UUID targetUuid) {
        return targetUuid == null || actor.getServer() == null
                ? null
                : actor.getServer().getPlayerManager().getPlayer(targetUuid);
    }

    @Nullable
    public static ServerPlayerEntity entityTarget(ServerPlayerEntity actor, Integer entityId) {
        if (entityId == null) {
            return null;
        }
        return actor.getServerWorld().getEntityById(entityId) instanceof ServerPlayerEntity target
                ? target
                : null;
    }

    @Nullable
    public static UUID uuidAccessor(Object payload, String accessor) {
        Object value = invokeAccessor(payload, accessor);
        return value instanceof UUID uuid ? uuid : null;
    }

    @Nullable
    public static Integer intAccessor(Object payload, String accessor) {
        Object value = invokeAccessor(payload, accessor);
        return value instanceof Integer integer ? integer : null;
    }

    @Nullable
    private static Object invokeAccessor(Object payload, String accessor) {
        if (payload == null) {
            return null;
        }
        try {
            Method method = payload.getClass().getMethod(accessor);
            return method.invoke(payload);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
