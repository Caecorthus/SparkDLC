package dev.caecorthus.sparkdlc.client.role.neutral.vulture;

import dev.caecorthus.sparkdlc.client.skill.SecondaryAbilityRegistry;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSuperCurseC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.agmas.noellesroles.Noellesroles;

/**
 * 秃鹫“超级骂”的客户端按键挂钩：挂到共用的“职业技能 2”键（默认 N）。
 * Client key hook for the Vulture's Super Curse on the shared Role Skill 2 key (default N).
 *
 * <p>客户端只发送空请求包；职业、存活、冷却与幼稚词条全部由服务端判定，冷却通过组件同步回来供 HUD 显示。
 * 按键注册表（{@code SecondaryAbilityRegistry}）为直接调用；秃鹫的职业 id 只在这里注册一次，因此按键总是可用。
 * The client only sends an empty request; the server decides role, liveness, cooldown and the Childish variant, and
 * syncs the cooldown back for the HUD. The key registry ({@code SecondaryAbilityRegistry}) is called directly; the
 * Vulture's role id is registered there only here, so the key is always available.</p>
 */
public final class VultureSuperCurseClientHooks {
    private static boolean registered;

    private VultureSuperCurseClientHooks() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        // The registry dispatches by the raw Wathe role id. / 注册表按原始 Wathe 职业 id 分发。
        SecondaryAbilityRegistry.register(Noellesroles.VULTURE.identifier(), client -> requestSuperCurse());
    }

    private static void requestSuperCurse() {
        // A server without the receiver never gets the packet. / 服务端未注册接收器时不发送。
        if (ClientPlayNetworking.canSend(VultureSuperCurseC2SPacket.ID)) {
            ClientPlayNetworking.send(new VultureSuperCurseC2SPacket());
        }
    }
}
