package dev.caecorthus.sparkdlc.faction.target;

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
 * Shared guard of the player-affect adapters: the mixins into Wathe, vanilla and NoellesRoles, and the mod's own
 * services that ask it at the head of their entry points. It stays outside the Mixin-owned package because it is
 * loaded directly.
 * 玩家影响适配器共用的守卫：注入 Wathe、原版与 NoellesRoles 的 mixin，以及在入口方法开头询问它的模组自有服务。它被直接加载，
 * 因此不放进 Mixin 所有的包。
 */
public final class PlayerAffectMixinGuard {
    /**
     * A rule that lets an action through whatever the policies say. Player-affect policies are deny-wins, so no policy
     * can state "this may not be vetoed"; the feature that owns such a rule registers it here from its own registration
     * code, and the guard itself names no feature. Either player may be null: exemptions are asked before the guard's
     * own null and self shortcut.
     * 无论策略如何都放行某个行为的规则。玩家影响策略是“拒绝优先”的，没有任何策略能表达“这不可被否决”；拥有这类规则的功能在
     * 自己的注册代码中把它注册到这里，守卫自身不点名任何功能。两名玩家都可能为 null：豁免在守卫自身的 null 与自身捷径之前询问。
     */
    @FunctionalInterface
    public interface Exemption {
        boolean exempts(PlayerEntity actor, PlayerEntity target, Identifier actionId);
    }

    // Asked in registration order, first true wins. Copy-on-write like the policy list: a check iterates a snapshot.
    // 按注册顺序询问，第一个 true 生效。与策略列表一样写时复制：检查遍历快照。
    private static final List<Exemption> EXEMPTIONS = new CopyOnWriteArrayList<>();

    private PlayerAffectMixinGuard() {
    }

    /**
     * Registers an exemption for every check that goes through this guard ({@code SparkFactionApi.canAffectPlayer}
     * asked directly does not see it). Call it from the owning feature's registration, before a round can run.
     * 为所有经过本守卫的检查注册一条豁免（直接询问 {@code SparkFactionApi.canAffectPlayer} 时看不到它）。须在拥有该规则的功能
     * 注册时调用，早于任何对局运行。
     */
    public static void registerExemption(Exemption exemption) {
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
            Iterable<? extends Exemption> exemptions,
            PlayerEntity actor,
            PlayerEntity target,
            Identifier actionId
    ) {
        for (Exemption exemption : exemptions) {
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
