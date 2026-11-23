package dev.caecorthus.sparkdlc.client.voice;

import de.maxhenkel.voicechat.api.events.EventRegistration;
import dev.caecorthus.sparkdlc.client.role.civilian.bear.hearing.BearHearingVoiceReceiver;
import dev.caecorthus.sparkdlc.client.role.civilian.saint.HolyFlashVoiceReceiver;
import dev.caecorthus.sparkdlc.voice.ClientVoiceReceivers;

/**
 * The client side of {@link ClientVoiceReceivers}: hands Simple Voice Chat's event registration to the two client-only
 * receivers. Instantiated by the Fabric loader through the {@code sparkdlc_client_voice} entrypoint, only on the
 * physical client and only when Simple Voice Chat is installed.
 * {@link ClientVoiceReceivers} 的客户端一侧：把 Simple Voice Chat 的事件注册交给两个仅客户端的接收器。由 Fabric 加载器经
 * {@code sparkdlc_client_voice} 入口实例化，仅在物理客户端且安装了 Simple Voice Chat 时发生。
 */
public final class IncomingVoiceEffects implements ClientVoiceReceivers {
    @Override
    public void registerHolyFlash(EventRegistration registration) {
        HolyFlashVoiceReceiver.register(registration);
    }

    @Override
    public void registerBearHearing(EventRegistration registration) {
        BearHearingVoiceReceiver.register(registration);
    }
}
