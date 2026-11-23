package dev.caecorthus.sparkdlc.role.killer.hunter.net;

import dev.caecorthus.sparkdlc.role.killer.hunter.HunterShotgunAimClaims;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Stable packet contract: registers every Hunter payload type and receiver (the only place that does), from
 * {@code SparkDlcPackets.register()}. The aim claim is on no deny-list: it only stores where the shooter aimed, and
 * the shot itself stays on the guarded vanilla use path. Its receiver must store synchronously (no
 * {@code server.execute}), so the claim is in place before the use packet queued right after it.
 * 稳定数据包契约：注册所有猎人数据包类型与接收器（唯一注册处），由 {@code SparkDlcPackets.register()} 调用。
 * 瞄准声明不在任何拦截名单上：它只记录射手的瞄准，射击本身仍走受拦截的原版使用路径。其接收器必须同步存入
 * （不得再 {@code server.execute}），以保证声明先于紧随其后排队的使用数据包就位。
 */
public final class HunterNetworking {
    /** Every Hunter C2S payload id, in registration order. / 所有猎人 C2S 数据包 id，按注册顺序。 */
    public static final List<Identifier> C2S_IDS = List.of(DoubleBarrelAimC2SPacket.PAYLOAD_ID);

    private static boolean registered;

    private HunterNetworking() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        PayloadTypeRegistry.playC2S().register(DoubleBarrelAimC2SPacket.ID, DoubleBarrelAimC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DoubleBarrelAimC2SPacket.ID,
                (payload, context) -> HunterShotgunAimClaims.submit(context.player(), payload.rays()));
        // The claims' disconnect and server-stop cleanup are event listeners, so HunterShotgunAimClaims.register() is
        // called from the start-up step of the mod's own roles (SparkDLC), where listener order is kept, not from here.
        // 瞄准声明的断线与停服清理属于事件监听器，因此 HunterShotgunAimClaims.register() 由本模组自有职业的启动步骤
        // （SparkDLC）调用，那里保持监听器顺序；不在此处调用。
    }
}
