package dev.caecorthus.sparkdlc.client.role.neutral.vulture;

import dev.caecorthus.sparkdlc.SparkStrength;
import dev.caecorthus.sparkdlc.client.skill.SecondaryAbilityRegistry;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSuperCurseC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.agmas.noellesroles.Noellesroles;

/**
 * 秃鹫“超级骂”的客户端按键挂钩：挂到 SparkWitch 的“职业技能 2”键（默认 N）。
 * Client key hook for the Vulture's Super Curse on SparkWitch's Role Skill 2 key (default N).
 *
 * <p>客户端只发送空请求包；职业、存活、冷却与幼稚词条全部由服务端判定，冷却通过组件同步回来供 HUD 显示。
 * 各模块已融合进 SparkDLC，按键门面为直接调用；只有该职业已被其他处理器占用时技能才没有按键，HUD 同样隐藏。
 * The client only sends an empty request; the server decides role, liveness, cooldown and the Childish variant, and
 * syncs the cooldown back for the HUD. The modules are fused in SparkDLC, so the key facade is called directly; the
 * skill has no key and no HUD only when another handler already took the role.</p>
 */
public final class VultureSuperCurseClientHooks {
    private static boolean registered;
    private static boolean keyAvailable;

    private VultureSuperCurseClientHooks() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        // SparkWitch dispatches by the raw Wathe role id. / SparkWitch 按原始 Wathe 职业 id 分发。
        keyAvailable = SecondaryAbilityRegistry.tryRegisterAction(
                Noellesroles.VULTURE.identifier(),
                VultureSuperCurseClientHooks::requestSuperCurse
        );
        if (!keyAvailable) {
            SparkStrength.LOGGER.warn("Vulture Super Curse has no key: the Role Skill 2 hook is unavailable or taken.");
        }
    }

    /** Whether Role Skill 2 reaches the Super Curse. / “职业技能 2”是否能触发超级骂。 */
    public static boolean isKeyAvailable() {
        return keyAvailable;
    }

    private static void requestSuperCurse() {
        // A server without SparkStrength's receiver never gets the packet. / 服务端未注册接收器时不发送。
        if (ClientPlayNetworking.canSend(VultureSuperCurseC2SPacket.ID)) {
            ClientPlayNetworking.send(new VultureSuperCurseC2SPacket());
        }
    }
}
