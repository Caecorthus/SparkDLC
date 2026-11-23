package dev.caecorthus.sparkdlc.role.civilian.medium;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Payload types and server receivers of the Medium. Registered once, from
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}; ids and codecs are those of the payload classes.
 * 通灵师的数据包类型与服务端接收器。由 {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} 注册一次；
 * id 与编解码器取自各数据包类。
 */
public final class MediumPackets {
    private MediumPackets() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(UseMediumSkillC2SPacket.ID, UseMediumSkillC2SPacket.CODEC);
        // Medium 幽灵的加护 (Role Skill 2 key): paid on receipt, no menu. It took the place of the removed
        // use_medium_second_skill; 招魂 is now picked in the inventory screen.
        // 通灵师幽灵的加护（职业技能 2 键）：收到即扣费，没有菜单。它取代了已移除的 use_medium_second_skill；
        // 招魂改为在背包界面中选择。
        PayloadTypeRegistry.playC2S().register(
                UseMediumBlessingC2SPacket.ID,
                UseMediumBlessingC2SPacket.CODEC
        );
        // Medium: the paid submits and the 通灵 question box. / 通灵师：付费提交与通灵提问框。
        PayloadTypeRegistry.playC2S().register(
                SubmitMediumSeanceC2SPacket.ID,
                SubmitMediumSeanceC2SPacket.CODEC
        );
        PayloadTypeRegistry.playC2S().register(
                SubmitMediumHauntTargetC2SPacket.ID,
                SubmitMediumHauntTargetC2SPacket.CODEC
        );
        // The dead answer a 通灵 or pick a 加护 from their vote screen; never a chat message.
        // 死者在投票界面回答通灵或选择加护；绝不走聊天消息。
        PayloadTypeRegistry.playC2S().register(
                SubmitMediumVoteC2SPacket.ID,
                SubmitMediumVoteC2SPacket.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                OpenMediumSeanceInputS2CPacket.ID,
                OpenMediumSeanceInputS2CPacket.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                MediumSeanceResultS2CPacket.ID,
                MediumSeanceResultS2CPacket.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(UseMediumSkillC2SPacket.ID,
                (payload, context) -> MediumSkillService.use(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(UseMediumBlessingC2SPacket.ID,
                (payload, context) -> context.server().execute(() ->
                        MediumBlessingService.use(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(SubmitMediumSeanceC2SPacket.ID,
                (payload, context) -> context.server().execute(() ->
                        MediumSkillService.submit(context.player(), payload.question())));
        ServerPlayNetworking.registerGlobalReceiver(SubmitMediumHauntTargetC2SPacket.ID,
                (payload, context) -> context.server().execute(() ->
                        MediumSecondSkillService.submit(context.player(), payload.target())));
        ServerPlayNetworking.registerGlobalReceiver(SubmitMediumVoteC2SPacket.ID,
                (payload, context) -> context.server().execute(() ->
                        MediumSkillService.vote(context.player(), payload.windowId(), payload.choice())));
    }
}
