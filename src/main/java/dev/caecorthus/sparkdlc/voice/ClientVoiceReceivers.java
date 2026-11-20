package dev.caecorthus.sparkdlc.voice;

import de.maxhenkel.voicechat.api.events.EventRegistration;
import dev.caecorthus.sparkdlc.SparkWitch;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.util.function.Consumer;

/**
 * The client-only incoming-voice receivers (Holy Flash muffle, BEAR hearing scaling), seen from common code. Simple
 * Voice Chat loads every {@code voicechat} entrypoint on both sides and gathers plugin events while it initialises as
 * a common mod, that is before any client initializer runs, so the client cannot register itself into a common hook in
 * time. The client implementation is therefore declared as the Fabric entrypoint {@value #ENTRYPOINT} in
 * {@code fabric.mod.json} and resolved by the loader on the physical client; on a dedicated server nothing is looked
 * up. Each receiver fails closed on its own (a warning, voice keeps its normal volume). Without SVC the plugin, and
 * this type, never load.
 * 从通用代码看到的仅客户端传入语音接收器（圣光弹压音、BEAR 听力缩放）。Simple Voice Chat 会在两端加载所有
 * {@code voicechat} 入口，并在它作为通用模组初始化时（早于任何客户端初始化器）收集插件事件，因此客户端来不及把自己注册进
 * 通用钩子。客户端实现因此在 {@code fabric.mod.json} 中声明为 Fabric 入口 {@value #ENTRYPOINT}，在物理客户端上由加载器
 * 解析；专用服务器上不做任何查找。每个接收器各自按失败关闭处理（记录警告，语音保持正常音量）。未安装 SVC 时插件及本类型都
 * 不会被加载。
 */
public interface ClientVoiceReceivers {
    /** The entrypoint key in {@code fabric.mod.json}. / {@code fabric.mod.json} 中的入口键。 */
    String ENTRYPOINT = "sparkdlc_client_voice";

    /** Muffles incoming voice on a flashed player's own client. / 在被闪玩家自己的客户端上压低传入语音。 */
    void registerHolyFlash(EventRegistration registration);

    /** Scales incoming voice by the wearer's BEAR hearing factor (H7). / 按佩戴者的 BEAR 听力倍数缩放传入语音（H7）。 */
    void registerBearHearing(EventRegistration registration);

    /** Physical client only; a no-op on dedicated servers. / 仅物理客户端；专用服务器上不做任何事。 */
    static void registerHolyFlashOnClient(EventRegistration registration) {
        onClient("Holy Flash voice muffle", receivers -> receivers.registerHolyFlash(registration));
    }

    /**
     * Physical client only; a no-op on dedicated servers. Call after {@link #registerHolyFlashOnClient} so the flash
     * muffle and the hearing factor multiply.
     * 仅物理客户端；专用服务器上不做任何事。须在 {@link #registerHolyFlashOnClient} 之后调用，使闪光压音与听力倍数相乘。
     */
    static void registerBearHearingOnClient(EventRegistration registration) {
        onClient("BEAR hearing voice scaling", receivers -> receivers.registerBearHearing(registration));
    }

    private static void onClient(String effect, Consumer<ClientVoiceReceivers> register) {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            return;
        }
        try {
            FabricLoader.getInstance().getEntrypoints(ENTRYPOINT, ClientVoiceReceivers.class).forEach(register);
        } catch (RuntimeException | LinkageError exception) {
            SparkWitch.LOGGER.warn(effect + " unavailable; voice stays at normal volume.", exception);
        }
    }
}
