package dev.caecorthus.sparkdlc.voice;

import dev.caecorthus.sparkdlc.SparkDLC;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Wathe's voice-group reset for the revives the mod performs itself (the Pathogen's T-Virus revive and the Last Stand
 * trait): {@code TrainVoicePlugin.resetPlayer} takes the player out of the hidden dead group Wathe moved it into on
 * death. Reached by reflection because {@code TrainVoicePlugin} implements Simple Voice Chat's plugin interface and
 * Simple Voice Chat is optional at run time. Any failure is logged at debug level with the caller's message and
 * changes nothing else, as each revive did with its own copy of this call.
 * Wathe 的语音分组重置，供本模组自行执行的复活使用（病原体的 T病毒复活与背水一战词条）：{@code TrainVoicePlugin.resetPlayer}
 * 把玩家移出 Wathe 在其死亡时放入的隐藏死者分组。通过反射访问，因为 {@code TrainVoicePlugin} 实现了 Simple Voice Chat 的插件
 * 接口，而 Simple Voice Chat 在运行时是可选的。任何失败都以调用方给出的信息记录为 debug 日志，不改变其他任何状态，与两处复活
 * 原先各自持有的这段调用一致。
 */
public final class WatheVoiceGroups {
    private WatheVoiceGroups() {
    }

    /**
     * Resets the player's voice group; {@code failureMessage} is the debug line logged when that is not possible.
     * The {@code LinkageError} half only ever caught a {@code TrainVoicePlugin} that failed to link; Wathe calls that
     * class directly for every participant when a round starts, so no revive can reach it with the class unlinked.
     * 重置玩家的语音分组；无法重置时以 {@code failureMessage} 记录 debug 日志。{@code LinkageError} 部分只会捕获无法链接的
     * {@code TrainVoicePlugin}；Wathe 在对局开始时已为每名参与者直接调用该类，因此任何复活都不会在该类未链接时到达这里。
     */
    public static void resetPlayer(UUID uuid, String failureMessage) {
        try {
            Class<?> plugin = Class.forName("dev.doctor4t.wathe.compat.TrainVoicePlugin");
            Method resetPlayer = plugin.getMethod("resetPlayer", UUID.class);
            resetPlayer.invoke(null, uuid);
        } catch (ReflectiveOperationException | LinkageError exception) {
            SparkDLC.LOGGER.debug(failureMessage, exception);
        }
    }
}
