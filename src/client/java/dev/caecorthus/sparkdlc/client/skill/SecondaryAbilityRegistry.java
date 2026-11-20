package dev.caecorthus.sparkdlc.client.skill;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Client-only role dispatcher; concrete roles own every secondary-key behavior. / 仅客户端角色分发器；第二技能键行为全部由具体角色拥有。 */
public final class SecondaryAbilityRegistry {
    private static final Map<Identifier, SecondaryAbilityHandler> HANDLERS = new LinkedHashMap<>();

    private SecondaryAbilityRegistry() {
    }

    public static synchronized void register(Identifier roleId, SecondaryAbilityHandler handler) {
        if (!tryRegister(roleId, handler)) {
            throw new IllegalStateException("Secondary ability handler already registered for " + roleId);
        }
    }

    /**
     * Puts a plain action on the Role Skill 2 key (default N) for {@code roleId}, for a role whose feature keeps no
     * per-tick or reset state on the key; {@code onPressed} runs on the client thread. A press reaches it only while
     * the local player's raw Wathe role id equals {@code roleId} on a confirmed server; it is a client-side trigger
     * only, and the caller's server must validate whatever the action sends. Returns false, without throwing, when
     * the role already has a handler (the first one stays). Null arguments throw {@link NullPointerException}.
     * 为 {@code roleId} 把一个简单动作挂到“职业技能 2”键（默认 N），供不在该键上保留每 tick 或重置状态的职业功能使用；
     * {@code onPressed} 在客户端线程运行。仅当本地玩家的原始 Wathe 职业 id 等于 {@code roleId} 且服务器已确认时按键才会派发；
     * 这只是客户端触发，动作发出的任何请求都必须由调用方的服务端校验。该职业已有处理器时返回 false 且不抛异常（保留先注册者）；
     * 参数为 null 时抛出 {@link NullPointerException}。
     */
    public static boolean tryRegisterAction(Identifier roleId, Runnable onPressed) {
        Objects.requireNonNull(roleId, "roleId");
        Objects.requireNonNull(onPressed, "onPressed");
        return tryRegister(roleId, client -> onPressed.run());
    }

    /**
     * Non-throwing form of {@link #register}: false keeps the first handler.
     * {@link #register} 的不抛异常形式：返回 false 时保留先注册的处理器。
     */
    public static synchronized boolean tryRegister(Identifier roleId, SecondaryAbilityHandler handler) {
        Objects.requireNonNull(roleId, "roleId");
        Objects.requireNonNull(handler, "handler");
        return HANDLERS.putIfAbsent(roleId, handler) == null;
    }

    public static synchronized @Nullable SecondaryAbilityHandler get(Identifier roleId) {
        return HANDLERS.get(roleId);
    }

    static synchronized void resetAll() {
        HANDLERS.values().forEach(SecondaryAbilityHandler::reset);
    }
}
