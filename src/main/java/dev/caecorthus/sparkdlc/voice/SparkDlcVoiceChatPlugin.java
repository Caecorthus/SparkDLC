package dev.caecorthus.sparkdlc.voice;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.LocationalSoundPacketEvent;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PacketEvent;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.api.events.StaticSoundPacketEvent;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.api.packets.Packet;
import de.maxhenkel.voicechat.api.packets.SoundPacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelRules;
import dev.caecorthus.sparkdlc.role.civilian.reporter.ReporterCommunicationComponent;
import dev.caecorthus.sparkdlc.role.civilian.reporter.ReporterCommunicationManager;
import dev.caecorthus.sparkdlc.role.civilian.reporter.ReporterCommunicationService;
import dev.caecorthus.sparkdlc.role.killer.blackraven.crow.BlackRavenCrowForm;
import dev.caecorthus.sparkdlc.role.killer.hiro.returning.HiroVictimFeignService;
import dev.caecorthus.sparkdlc.role.killer.kidnapper.KidnapperControlComponent;
import dev.caecorthus.sparkdlc.role.killer.morphling.MorphMarkPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.morphling.MorphlingService;
import dev.caecorthus.sparkdlc.role.killer.saboteur.SaboteurRules;
import dev.caecorthus.sparkdlc.role.neutral.cat.SchrodingersCatService;
import dev.caecorthus.sparkdlc.role.neutral.ferryman.FerrymanFeignService;
import dev.caecorthus.sparkdlc.role.neutral.ferryman.FerrymanMomentService;
import dev.caecorthus.sparkdlc.role.neutral.fiend.FiendFeignService;
import dev.caecorthus.sparkdlc.role.neutral.jester.JesterMomentService;
import dev.caecorthus.sparkdlc.role.neutral.pathogen.PathogenVisibility;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithCommunicationPolicy;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithStateService;
import dev.caecorthus.sparkdlc.role.witch.WitchFactionRules;
import dev.caecorthus.sparkdlc.role.witch.curser.CurserFeatureService;
import dev.caecorthus.sparkdlc.role.witch.riftwalker.session.RiftSessionService;
import dev.caecorthus.sparkdlc.trait.civilian.depression.DepressionTraitService;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.compat.TrainVoicePlugin;
import dev.doctor4t.wathe.game.GameFunctions;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * The one Simple Voice Chat plugin of the mod ({@code voicechat} in {@code fabric.mod.json}). It replaces three
 * plugins, and {@link #registerEvents} registers their handlers in the order and with the priorities they had:
 * Simple Voice Chat sorts the handlers of an event by priority with a stable sort, so handlers of equal priority run
 * in registration order, which used to be plugin order (role kits, traits, the mod's own roles) and is now the order
 * of the calls below. The plugin id is only printed in Simple Voice Chat's log; nothing is saved under it.
 *
 * <p>Role kits: the Morphling serum voice disguise, the Reporter's relays, and the mutes of the Jester's moment and
 * of a T-Virus-revived Pathogen. Traits: the two-way mute of a Depression psycho. The mod's own roles: the Wraith
 * life-cycle voice rules, the mutes of players who play dead or are hidden, the Saboteur's faction voice, the Blind's
 * lowest-priority voice perception, and on a physical client the Holy Flash and BEAR hearing filters
 * ({@link ClientVoiceReceivers}).</p>
 *
 * 本模组唯一的 Simple Voice Chat 插件（{@code fabric.mod.json} 的 {@code voicechat}）。它取代了三个插件，
 * {@link #registerEvents} 按它们原有的顺序与优先级注册各处理器：Simple Voice Chat 用稳定排序按优先级排列同一事件的处理器，
 * 因此同优先级的处理器按注册顺序执行；该顺序原先是插件顺序（职业套件、词条、本模组自有职业），现在就是下面调用的顺序。
 * 插件 id 只出现在 Simple Voice Chat 的日志里，没有任何数据以它为名保存。
 *
 * <p>职业套件：变形试剂的语音伪装、记者的转发，以及小丑时刻与被 T病毒复活的病原体的静音。词条：抑郁疯魔期间的双向静音。
 * 本模组自有职业：冤魂生命周期各阶段的语音规则、装死或隐藏玩家的静音、破坏者的阵营语音、盲人最低优先级的语音感知，
 * 以及物理客户端上的圣光弹与 BEAR 听力滤波（{@link ClientVoiceReceivers}）。</p>
 *
 * <p>变形试剂的语音伪装规则分两段：
 * 1. 正在试剂变形成别人的玩家，自己的麦克风包直接取消，避免“外观像 B、声音却从 A 本体发出”。
 * 2. 被采样目标 B 说话时，复制一份同样的语音数据，但把声源实体改成所有正在变成 B 的玩家 A。
 * 这样 B 的原始语音仍然保留，同时 A 附近的玩家也会听见“从 A 身上传来的 B 声音”。</p>
 *
 * <p>小丑时刻：被触发时刻的小丑从中枪起到时刻结束既不能说话也听不到（见 {@link JesterMomentService#isVoiceBlocked}）。</p>
 *
 * <p>病原体：被 T病毒复活的存活病原体不能说话，但仍能听见（见 {@link PathogenVisibility#isMuted}）。</p>
 */
public final class SparkDlcVoiceChatPlugin implements VoicechatPlugin {
    /** Server API, kept so gameplay code can drop a revived player out of its group. / 服务端 API，供玩法代码把复活的玩家移出语音分组。 */
    private static volatile de.maxhenkel.voicechat.api.VoicechatApi api;

    @Override
    public void initialize(de.maxhenkel.voicechat.api.VoicechatApi api) {
        SparkDlcVoiceChatPlugin.api = api;
        FerrymanMomentService.installVoiceGroupReset(SparkDlcVoiceChatPlugin::leaveAllGroups);
        VoicechatPlugin.super.initialize(api);
    }

    /**
     * Removes the player from every voice group (a revived Lost Soul must not keep the dead channel). Fails soft: an
     * absent connection or an older API just leaves the group state alone.
     * 把玩家移出所有语音分组（复活的迷失灵魂不能继续待在死者频道）。失败即降级：连接不存在或旧版 API 时不改动分组状态。
     */
    private static void leaveAllGroups(net.minecraft.server.network.ServerPlayerEntity player) {
        if (!(api instanceof de.maxhenkel.voicechat.api.VoicechatServerApi serverApi)) {
            return;
        }
        de.maxhenkel.voicechat.api.VoicechatConnection connection = serverApi.getConnectionOf(player.getUuid());
        if (connection == null || !connection.isInGroup()) {
            return;
        }
        connection.setGroup(null);
    }

    @Override
    public String getPluginId() {
        return SparkDLC.MOD_ID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registerRoleKitVoiceRules(registration);
        registerTraitVoiceRules(registration);
        registerOwnRoleVoiceRules(registration);
        VoicechatPlugin.super.registerEvents(registration);
    }

    /** Jester moment, muted Pathogen, Morphling disguise and Reporter relays. / 小丑时刻、病原体静音、变形伪装与记者转发。 */
    private void registerRoleKitVoiceRules(EventRegistration registration) {
        // Highest priority: Simple Voice Chat stops dispatching once an event is cancelled, so a muted Jester's voice
        // never reaches Wathe's walkie-talkie relay, the Noisemaker broadcast or the morph relay below.
        // 最高优先级：事件被取消后 Simple Voice Chat 不再分发，因此静音小丑的声音不会进入 Wathe 对讲机、喧哗者广播或下方的伪装转发。
        registration.registerEvent(MicrophonePacketEvent.class, this::blockJesterMomentSpeaker, Integer.MAX_VALUE);
        // A living Pathogen revived by a T-Virus never speaks; it still hears. / T病毒复活的存活病原体不能说话，但仍能听见。
        registration.registerEvent(MicrophonePacketEvent.class, this::blockMutedPathogenSpeaker, Integer.MAX_VALUE);
        // Every server-to-client voice packet, including relays other plugins build, passes these per-listener events.
        // 每个服务端发往客户端的语音包（包括其他插件构造的转发）都会经过这些逐接收者事件。
        registration.registerEvent(EntitySoundPacketEvent.class, this::blockJesterMomentListener, Integer.MAX_VALUE);
        registration.registerEvent(LocationalSoundPacketEvent.class, this::blockJesterMomentListener, Integer.MAX_VALUE);
        registration.registerEvent(StaticSoundPacketEvent.class, this::blockJesterMomentListener, Integer.MAX_VALUE);
        registration.registerEvent(MicrophonePacketEvent.class, this::handleMicrophonePacket);
    }

    /** Depression psycho silence. / 抑郁疯魔期间的静音。 */
    private void registerTraitVoiceRules(EventRegistration registration) {
        // Cancel before Wathe can relay walkie-talkie audio manually.
        // 必须先于 Wathe 取消事件，避免对讲机绕过抑郁疯魔的静音。
        registration.registerEvent(
                MicrophonePacketEvent.class,
                this::blockDepressionSpeaker,
                Integer.MAX_VALUE
        );
        registration.registerEvent(EntitySoundPacketEvent.class, this::blockDepressionListener);
        registration.registerEvent(LocationalSoundPacketEvent.class, this::blockDepressionListener);
    }

    /** Wraith rules, play-dead mutes, Saboteur voice, Blind perception, client filters. / 冤魂规则、装死静音、破坏者语音、盲人感知与客户端滤波。 */
    private void registerOwnRoleVoiceRules(EventRegistration registration) {
        registration.registerEvent(
                MicrophonePacketEvent.class,
                this::blockWraithSpeaker,
                Integer.MAX_VALUE
        );
        /*
         * 必须在声音包阶段过滤，而不能在这里直接取消已晋升身份的麦克风事件：
         * Wind Spirit、Vendetta、Curser 和 Saboteur 的对讲机功能依赖 Wathe
         * TrainVoicePlugin 继续收到 MicrophonePacketEvent。
         *
         * 三类声音包都要过滤，因为原生近距离语音、实体语音和插件转发的对讲机
         * 可能走不同的包类型。
         */
        registration.registerEvent(EntitySoundPacketEvent.class, this::blockRestrictedRecipient, Integer.MAX_VALUE);
        registration.registerEvent(LocationalSoundPacketEvent.class, this::blockRestrictedRecipient, Integer.MAX_VALUE);
        registration.registerEvent(StaticSoundPacketEvent.class, this::blockRestrictedRecipient, Integer.MAX_VALUE);
        // Lowest priority: the Blind only perceives speakers that no listener muted. / 最低优先级：盲人只感知未被静音的说话者。
        registration.registerEvent(
                MicrophonePacketEvent.class,
                BlindVoicePerceptionListener::onMicrophonePacket,
                Integer.MIN_VALUE
        );
        // Physical client only: Holy Flash muffles incoming voice; a no-op on dedicated servers.
        // 仅物理客户端：圣光弹压低传入语音；专用服务器上不做任何事。
        ClientVoiceReceivers.registerHolyFlashOnClient(registration);
        // Physical client only, after Holy Flash so both compose: BEAR hearing scales incoming voice (H7).
        // 仅物理客户端，排在圣光弹之后以便二者相乘：BEAR 听力缩放传入语音（H7）。
        ClientVoiceReceivers.registerBearHearingOnClient(registration);
    }


    private void blockJesterMomentSpeaker(MicrophonePacketEvent event) {
        if (JesterMomentService.isVoiceBlocked(resolveServerPlayer(event.getSenderConnection()))) {
            event.cancel();
        }
    }

    private void blockMutedPathogenSpeaker(MicrophonePacketEvent event) {
        if (PathogenVisibility.isMuted(resolveServerPlayer(event.getSenderConnection()))) {
            event.cancel();
        }
    }

    private void blockJesterMomentListener(PacketEvent<?> event) {
        if (JesterMomentService.isVoiceBlocked(resolveServerPlayer(event.getReceiverConnection()))) {
            event.cancel();
        }
    }

    private void handleMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatServerApi api = event.getVoicechat();
        ServerPlayerEntity sender = resolveServerPlayer(event.getSenderConnection());
        if (sender == null) {
            return;
        }

        MorphMarkPlayerComponent senderMark = MorphMarkPlayerComponent.KEY.get(sender);
        if (senderMark.isActive()) {
            // 伪装者自己的声音不应该按真实身份播出；真正的“伪装声音”由被采样目标说话时转发。
            event.cancel();
            return;
        }

        /*
         * 记者通讯是“额外复制一份语音”的能力，不能被变形怪的伪装列表早退挡住。
         * 因此先无条件尝试记者转发，再继续处理可能存在的变形伪装转发。
         */
        relayReporterCommunicationVoice(api, event, sender);

        List<ServerPlayerEntity> disguisedPlayers = MorphlingService.findActivePlayersDisguisedAs(sender);
        for (ServerPlayerEntity disguisedPlayer : disguisedPlayers) {
            relayVoiceAsDisguisedPlayer(api, event, sender, disguisedPlayer);
        }
    }

    private void relayVoiceAsDisguisedPlayer(
            VoicechatServerApi api,
            MicrophonePacketEvent event,
            ServerPlayerEntity originalSpeaker,
            ServerPlayerEntity disguisedPlayer
    ) {
        EntitySoundPacket.Builder<?> builder = event.getPacket()
                .entitySoundPacketBuilder()
                .entityUuid(disguisedPlayer.getUuid())
                .whispering(event.getPacket().isWhispering())
                .distance((float) api.getVoiceChatDistance());

        /*
         * simple-voice-chat 2.6.x 的公开 entityUuid(...) 在部分路径里不会真正改到底层 sender。
         * 参考 NoellesRoles 自改版的处理，这里在 build 前同步修正 builder 内部 sender/channelId，
         * 确保客户端看到的声源实体、说话图标和空间位置都落到伪装者 A 身上。
         */
        if (!retargetEntitySoundBuilder(
                builder,
                getMorphVoiceChannelId(originalSpeaker, disguisedPlayer),
                disguisedPlayer.getUuid()
        )) {
            return;
        }

        EntitySoundPacket redirectedPacket = builder.build();
        for (ServerPlayerEntity recipient : originalSpeaker.getServer().getPlayerManager().getPlayerList()) {
            VoicechatConnection connection = api.getConnectionOf(recipient.getUuid());
            if (connection != null) {
                // 不跳过原说话者 B：如果 B 在 A 的声音范围内，也能听见自己被 A “复读”的那份伪装语音。
                api.sendEntitySoundPacketTo(connection, redirectedPacket);
            }
        }
    }

    private UUID getMorphVoiceChannelId(ServerPlayerEntity originalSpeaker, ServerPlayerEntity disguisedPlayer) {
        return UUID.nameUUIDFromBytes(
                (SparkDLC.MOD_ID + ":morph_voice:" + originalSpeaker.getUuid() + ":" + disguisedPlayer.getUuid())
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    private void relayReporterCommunicationVoice(
            VoicechatServerApi api,
            MicrophonePacketEvent event,
            ServerPlayerEntity sender
    ) {
        if (!ReporterCommunicationService.isCommunicableEndpoint(sender)) {
            return;
        }

        /*
         * 记者通讯的语音要求是“远距离也能直接听到”，并且需要让饕餮肚子里的人
         * 在被接线/广播时越过胃部语音隔离。StaticSoundPacket 不绑定世界位置，
         * 手动发给目标连接即可，正好复刻自改版接线员的无距离衰减效果。
         */
        StaticSoundPacket redirectedPacket = event.getPacket().staticSoundPacketBuilder().build();
        relayReporterConnectionVoice(api, sender, redirectedPacket);
        relayReporterBroadcastVoice(api, sender, redirectedPacket);
    }

    private void relayReporterConnectionVoice(
            VoicechatServerApi api,
            ServerPlayerEntity sender,
            StaticSoundPacket redirectedPacket
    ) {
        for (ServerPlayerEntity possibleReporter : sender.getServer().getPlayerManager().getPlayerList()) {
            ReporterCommunicationComponent component = ReporterCommunicationComponent.KEY.get(possibleReporter);
            ReporterCommunicationManager.UUIDPair pair = ReporterCommunicationManager.getConnectionPair(component);
            if (pair == null) {
                continue;
            }

            UUID recipientUuid = null;
            if (sender.getUuid().equals(pair.first())) {
                recipientUuid = pair.second();
            } else if (sender.getUuid().equals(pair.second())) {
                recipientUuid = pair.first();
            }

            if (recipientUuid == null) {
                continue;
            }

            ServerPlayerEntity recipient = sender.getServer().getPlayerManager().getPlayer(recipientUuid);
            if (!ReporterCommunicationService.isCommunicableEndpoint(recipient)) {
                continue;
            }

            VoicechatConnection connection = api.getConnectionOf(recipientUuid);
            if (connection != null) {
                api.sendStaticSoundPacketTo(connection, redirectedPacket);
            }
        }
    }

    private void relayReporterBroadcastVoice(
            VoicechatServerApi api,
            ServerPlayerEntity sender,
            StaticSoundPacket redirectedPacket
    ) {
        for (ServerPlayerEntity possibleReporter : sender.getServer().getPlayerManager().getPlayerList()) {
            ReporterCommunicationComponent component = ReporterCommunicationComponent.KEY.get(possibleReporter);
            if (!component.hasActiveBroadcast() || component.getBroadcastTarget() == null) {
                continue;
            }
            if (!sender.getUuid().equals(component.getBroadcastTarget())) {
                continue;
            }

            for (ServerPlayerEntity recipient : sender.getServer().getPlayerManager().getPlayerList()) {
                if (recipient.getUuid().equals(sender.getUuid())) {
                    continue;
                }
                // 广播采访按需求覆盖所有本局在线玩家，不按存活状态过滤。
                if (!ReporterCommunicationService.isInGameOnlinePlayer(sender, recipient)) {
                    continue;
                }
                VoicechatConnection connection = api.getConnectionOf(recipient.getUuid());
                if (connection != null) {
                    api.sendStaticSoundPacketTo(connection, redirectedPacket);
                }
            }
        }
    }

    private boolean retargetEntitySoundBuilder(
            EntitySoundPacket.Builder<?> builder,
            UUID channelId,
            UUID senderUuid
    ) {
        try {
            setFieldRecursively(builder, "sender", senderUuid);
            setFieldRecursively(builder, "channelId", channelId);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private void setFieldRecursively(Object instance, String fieldName, Object value) throws ReflectiveOperationException {
        Class<?> type = instance.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(instance, value);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private @Nullable ServerPlayerEntity resolveServerPlayer(@Nullable VoicechatConnection connection) {
        if (connection == null || connection.getPlayer() == null || connection.getPlayer().getPlayer() == null) {
            return null;
        }

        Object rawPlayer = connection.getPlayer().getPlayer();
        return rawPlayer instanceof ServerPlayerEntity serverPlayer ? serverPlayer : null;
    }


    private void blockDepressionSpeaker(MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null
                || event.getSenderConnection().getPlayer() == null
                || event.getSenderConnection().getPlayer().getPlayer() == null) {
            return;
        }
        ServerPlayerEntity speaker = (ServerPlayerEntity) event.getSenderConnection().getPlayer().getPlayer();
        if (DepressionTraitService.shouldMuteVoice(speaker)) {
            event.cancel();
        }
    }

    private <T extends Packet> void blockDepressionListener(SoundPacketEvent<T> event) {
        if (event.getReceiverConnection() == null
                || event.getReceiverConnection().getPlayer() == null
                || event.getReceiverConnection().getPlayer().getPlayer() == null) {
            return;
        }
        ServerPlayerEntity listener = (ServerPlayerEntity) event.getReceiverConnection().getPlayer().getPlayer();
        if (DepressionTraitService.shouldMuteVoice(listener)) {
            event.cancel();
        }
    }


    private void blockRestrictedRecipient(de.maxhenkel.voicechat.api.events.PacketEvent<?> event) {
        if (SaboteurVoiceRules.shouldBlockPacket(event) || shouldBlockWraithRecipient(event)) {
            event.cancel();
        }
    }

    private boolean shouldBlockWraithRecipient(de.maxhenkel.voicechat.api.events.PacketEvent<?> event) {
        ServerPlayerEntity recipient = player(event.getReceiverConnection());
        if (recipient == null) {
            return false;
        }

        Role role = GameWorldComponent.KEY.get(recipient.getServerWorld()).getRole(recipient);
        boolean activeWraith = WraithStateService.isActive(recipient);
        boolean promotedWraith = WraithStateService.isPromoted(recipient);
        boolean guardianAngel = GuardianAngelRules.isGuardianAngel(role);

        /*
         * 未晋升冤魂继续保持完全静音；已晋升身份不再被普通 Wraith 接收限制
         * 拦截。Saboteur 的阵营语音限制已经由 SaboteurVoiceRules 双向处理。
         */
        if (WraithCommunicationPolicy.shouldBlockCommunication(
                activeWraith,
                promotedWraith,
                guardianAngel,
                recipient.isCreative()
        )) {
            return true;
        }

        ServerPlayerEntity speaker = soundPacketSpeaker(event, recipient);
        if (speaker == null) {
            return false;
        }

        Role speakerRole = GameWorldComponent.KEY.get(speaker.getServerWorld()).getRole(speaker);
        boolean speakerActiveWraith = WraithStateService.isActive(speaker);
        boolean speakerPromotedWraith = WraithStateService.isPromoted(speaker);
        boolean speakerGuardianAngel = GuardianAngelRules.isGuardianAngel(speakerRole);
        boolean speakerSaboteur = SaboteurRules.isActivePromotedSaboteur(speaker);
        boolean walkieTalkiePacket = isWalkieTalkiePacket(event);
        boolean recipientPlayingAndAlive = GameFunctions.isPlayerPlayingAndAlive(recipient);
        boolean livingWitchFactionRecipient = WitchFactionRules.isGrandWitch(role)
                || WitchFactionRules.isAccompliceLike(role);

        /*
         * 诅咒者是转身后需要继续和存活魔女阵营协作的特殊身份。
         * 这里只给大魔女 / 共犯补一条直通例外，其他存活玩家仍然交给下面的
         * 通用冤魂语音规则处理。
         */
        if (WraithCommunicationPolicy.shouldAllowPromotedCurserVoiceToLivingWitchFaction(
                speakerActiveWraith,
                speakerPromotedWraith,
                CurserFeatureService.isActivePromotedCurser(speaker),
                speaker.isCreative(),
                recipientPlayingAndAlive,
                livingWitchFactionRecipient
        )) {
            return false;
        }

        /*
         * 守护天使保留 Wathe 隐藏死者组。它可以向死者组说话，也可以从正常
         * 存活玩家处接收近距离语音，但任何发往正常存活玩家的声音包都必须取消。
         * 这里包含对讲机包，解决 Wathe 手动复制语音绕过分组的问题。
         */
        if (WraithCommunicationPolicy.shouldBlockGuardianAngelVoiceToLiving(
                speakerActiveWraith,
                speakerPromotedWraith,
                speakerGuardianAngel,
                recipientPlayingAndAlive
        )) {
            return true;
        }

        /*
         * 风精灵、仇杀客和诅咒者可以继续使用对讲机，但普通近距离语音不能
         * 被正常存活玩家听到。不能取消麦克风事件，否则 Wathe 的对讲机转发
         * 也会一起失效，所以只在最终声音包阶段屏蔽。
         */
        return WraithCommunicationPolicy.shouldBlockPromotedCivilianVoiceToLiving(
                speakerActiveWraith,
                speakerPromotedWraith,
                speakerGuardianAngel,
                speakerSaboteur,
                speaker.isCreative(),
                walkieTalkiePacket,
                recipientPlayingAndAlive
        );
    }

    private void blockWraithSpeaker(MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null
                || event.getSenderConnection().getPlayer() == null
                || event.getSenderConnection().getPlayer().getPlayer() == null) {
            return;
        }
        ServerPlayerEntity speaker = (ServerPlayerEntity) event.getSenderConnection().getPlayer().getPlayer();
        if (KidnapperControlComponent.KEY.get(speaker).isControlled()
                && GameFunctions.isPlayerAliveAndSurvival(speaker)) {
            // 迷药控制期间目标黑屏且无法主动行动；语音也必须在同一入口静音，避免报点破坏劫持效果。
            event.cancel();
            return;
        }
        if (FiendFeignService.isFeigning(speaker)) {
            // A feigning Fiend plays dead: muted but still hears, and never joins the dead players' group.
            // 假死的魔人在装死：被静音但仍能听见，也从不加入死者语音组。
            event.cancel();
            return;
        }
        if (FerrymanFeignService.isFeigning(speaker)) {
            // A player feigning before the Charon plays dead too: muted but still hears, like a feigning Fiend.
            // 在卡戎面前假死的玩家同样在装死：被静音但仍能听见，与假死的魔人相同。
            event.cancel();
            return;
        }
        if (HiroVictimFeignService.isFeigned(speaker)) {
            // A fake-dead victim of 二阶堂希罗 lies dead to everyone: muted but still hears, and it joins the dead
            // players' group only when the death becomes real.
            // 二阶堂希罗的假死受害者在所有人眼中已经死亡：被静默但仍能听见，只有死亡成真时才加入死者语音组。
            event.cancel();
            return;
        }
        if (SchrodingersCatService.isSpectre(speaker)) {
            // A Schrödinger's Cat in its 3 s spectre is a living spectator: muted but still hears, like a feigning
            // Fiend. / 处于 3 秒幽魂阶段的薛定谔之猫是活着的旁观者：被静音但仍能听见，与假死的魔人相同。
            event.cancel();
            return;
        }
        if (RiftSessionService.isInside(speaker)) {
            // Rift Gate occupants are muted but still hear (plan §6.6): no voice may leak from a gate, and the NR
            // Paranoid would otherwise hear them as non-swallowed spectators.
            // 裂隙门内的玩家被静音但仍能听见（plan §6.6）：门口不得传出人声，否则 NR 偏执杀手会把他们当作未被吞的旁观者听到。
            event.cancel();
            return;
        }
        if (BlackRavenCrowForm.isInCrowForm(speaker)) {
            // A Black Raven in Raven Form is muted but still hears (owner Q6), like a Rift Gate occupant: no voice may
            // leave the hidden body. / 化鸦中的黑羽鸦被静音但仍能听见（所有者 Q6），与裂隙门内玩家相同：隐藏的本体不得传出人声。
            event.cancel();
            return;
        }
        Role role = GameWorldComponent.KEY.get(speaker.getServerWorld()).getRole(speaker);
        if (WraithCommunicationPolicy.shouldBlockCommunication(
                WraithStateService.isActive(speaker),
                WraithStateService.isPromoted(speaker),
                GuardianAngelRules.isGuardianAngel(role),
                speaker.isCreative()
        )) {
            // 只有未晋升的普通冤魂在麦克风入口静音；已晋升身份必须放行，
            // 否则 Wathe 无法收到它们的对讲机语音。
            event.cancel();
        }
    }

    private boolean isWalkieTalkiePacket(de.maxhenkel.voicechat.api.events.PacketEvent<?> event) {
        return event instanceof SoundPacketEvent<?> soundPacketEvent
                && TrainVoicePlugin.WALKIE_TALKIE_CATEGORY.equals(
                soundPacket(soundPacketEvent).getCategory()
        );
    }

    /**
     * 优先使用事件提供的发送者连接；Wathe 通过 VoicechatServerApi 手动发送
     * 对讲机包时，部分版本的事件可能没有 senderConnection，因此再从声音包
     * 自带的 sender UUID 解析说话者。
     */
    private ServerPlayerEntity soundPacketSpeaker(
            de.maxhenkel.voicechat.api.events.PacketEvent<?> event,
            ServerPlayerEntity recipient
    ) {
        ServerPlayerEntity speaker = player(event.getSenderConnection());
        if (speaker != null || !(event instanceof SoundPacketEvent<?> soundPacketEvent)
                || recipient.getServer() == null
                || soundPacket(soundPacketEvent).getSender() == null) {
            return speaker;
        }
        return recipient.getServer().getPlayerManager().getPlayer(
                soundPacket(soundPacketEvent).getSender()
        );
    }

    private SoundPacket soundPacket(SoundPacketEvent<?> event) {
        /*
         * voicechat-api 的 SoundPacketEvent 泛型上界是基础 Packet；
         * 三类服务端声音事件的运行时包统一实现 SoundPacket。
         */
        return (SoundPacket) event.getPacket();
    }

    private ServerPlayerEntity player(de.maxhenkel.voicechat.api.VoicechatConnection connection) {
        if (connection == null || connection.getPlayer() == null
                || !(connection.getPlayer().getPlayer() instanceof ServerPlayerEntity player)) {
            return null;
        }
        return player;
    }
}
