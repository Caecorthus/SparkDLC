package dev.caecorthus.sparkdlc.client.role.killer.partyanimal;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.SparkDlcEntities;
import dev.caecorthus.sparkdlc.client.skill.SecondaryAbilityRegistry;
import dev.caecorthus.sparkdlc.client.render.entity.PartygoerEntityRenderer;
import dev.caecorthus.sparkdlc.client.render.entity.PartygoerModel;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerMenuC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerMenuS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import org.agmas.noellesroles.Noellesroles;

/**
 * Client side of the Partygoer skill: the Role Skill 2 key (SparkWitch's shared key, default N) asks the server for the
 * room picker, the server's answer opens it, and the Partygoer gets its renderer.
 * 派对客技能的客户端部分：“职业技能 2”键（SparkWitch 共享键，默认 N）向服务端请求选房界面，服务端回复后打开界面；并注册派对客渲染器。
 *
 * <p>Like the Vulture's Super Curse, the key comes from SparkWitch's public key facade, called directly since the
 * SparkDLC fusion; the skill has no key (and no HUD line) only when another handler already took the role.
 * 与秃鹫超级骂一样，按键来自 SparkWitch 公开按键门面（融合进 SparkDLC 后为直接调用）；只有该职业已被其他处理器占用时，
 * 技能才没有按键，也不显示 HUD。</p>
 */
public final class PartygoerClientHooks {
    private static boolean registered;
    private static boolean keyAvailable;

    private PartygoerClientHooks() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        EntityModelLayerRegistry.registerModelLayer(PartygoerModel.LAYER, PartygoerModel::getTexturedModelData);
        EntityRendererRegistry.register(SparkDlcEntities.partygoer(), PartygoerEntityRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(PartygoerMenuS2CPacket.ID,
                (payload, context) -> context.client().setScreen(new PartygoerRoomScreen(payload.rooms())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(PartygoerCorpseClient::reset));
        keyAvailable = SecondaryAbilityRegistry.tryRegisterAction(Noellesroles.PARTY_ANIMAL.identifier(),
                PartygoerClientHooks::requestMenu);
        if (!keyAvailable) {
            SparkDLC.LOGGER.warn("Partygoer has no key: the Role Skill 2 hook is unavailable or taken.");
        }
    }

    /** Whether Role Skill 2 reaches the Partygoer skill. / “职业技能 2”是否能触发派对客技能。 */
    public static boolean isKeyAvailable() {
        return keyAvailable;
    }

    private static void requestMenu() {
        if (ClientPlayNetworking.canSend(PartygoerMenuC2SPacket.ID)) {
            ClientPlayNetworking.send(new PartygoerMenuC2SPacket());
        }
    }
}
