package dev.caecorthus.sparkdlc.role.killer.magician;

import dev.caecorthus.sparkdlc.SparkWitchRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of the Magician. Registered once, from
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of the payload classes.
 * 魔术师的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class MagicianPackets {
    private MagicianPackets() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(UseMagicianAbilityC2SPacket.ID, UseMagicianAbilityC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SelectMagicianTargetC2SPacket.ID, SelectMagicianTargetC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                UseMagicianAbilityC2SPacket.ID,
                (payload, context) -> context.server().execute(
                        () -> MagicianAbility.handle(context.player(), payload.action())
                )
        );
        // Any member of the round roster is accepted, alive or dead (owner decision 2026-10-07 D7), so a refused pick
        // can never reveal a death. / 接受本局名单内任何成员，无论生死（所有者 2026-10-07 决定 D7），拒绝选择永远不会暴露死亡。
        ServerPlayNetworking.registerGlobalReceiver(SelectMagicianTargetC2SPacket.ID, (payload, context) ->
                context.server().execute(() -> {
                    var game = GameWorldComponent.KEY.get(context.player().getWorld());
                    var role = game.getRole(context.player());
                    var component = MagicianPlayerComponent.KEY.get(context.player());
                    var entry = payload.target() == null ? null : component.rosterEntry(payload.target());
                    // Exactly ACTIVE, like the ability itself (owner 2026-11). / 必须恰为 ACTIVE，与技能本身一致（所有者 2026-11）。
                    if (entry != null && role != null
                            && SparkWitchRoles.MAGICIAN_ID.equals(role.identifier())
                            && game.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE) {
                        component.setSelectedTarget(entry.uuid(), entry.name());
                    }
                })
        );
    }
}
