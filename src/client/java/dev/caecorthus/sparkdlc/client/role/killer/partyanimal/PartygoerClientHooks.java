package dev.caecorthus.sparkdlc.client.role.killer.partyanimal;

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
 * Client side of the Partygoer skill: the Role Skill 2 key (the shared key, default N) asks the server for the
 * room picker, the server's answer opens it, and the Partygoer gets its renderer.
 * 派对客技能的客户端部分：“职业技能 2”键（共享键，默认 N）向服务端请求选房界面，服务端回复后打开界面；并注册派对客渲染器。
 *
 * <p>Like the Vulture's Super Curse, the key comes from {@code SecondaryAbilityRegistry}, called directly; the Party
 * Animal's role id is registered there only here.
 * 与秃鹫超级骂一样，按键来自 {@code SecondaryAbilityRegistry}（直接调用）；派对狂的职业 id 只在这里注册。</p>
 */
public final class PartygoerClientHooks {
    private static boolean registered;

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
        SecondaryAbilityRegistry.register(Noellesroles.PARTY_ANIMAL.identifier(), client -> requestMenu());
    }

    private static void requestMenu() {
        if (ClientPlayNetworking.canSend(PartygoerMenuC2SPacket.ID)) {
            ClientPlayNetworking.send(new PartygoerMenuC2SPacket());
        }
    }
}
