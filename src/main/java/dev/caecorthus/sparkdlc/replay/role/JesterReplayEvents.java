package dev.caecorthus.sparkdlc.replay.role;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.doctor4t.wathe.record.replay.ReplayRegistry;
import net.minecraft.util.Identifier;

/**
 * Replay lines for the Jester's transformation and fake death.
 * 小丑转变与假死的回放文本。
 */
public final class JesterReplayEvents {
    public static final Identifier JESTER_POSITIONS_SHUFFLED = SparkDLC.id("jester_positions_shuffled");
    public static final Identifier JESTER_FAKE_DEATH_KILL_BLOCKED = SparkDLC.id("jester_fake_death_kill_blocked");

    private JesterReplayEvents() {
    }

    static void register() {
        // Actor = the Jester whose transformation shuffled everyone. / actor 为开始转变、打乱所有人位置的小丑。
        ReplayRegistry.registerGlobalEventFormatter(JESTER_POSITIONS_SHUFFLED,
                (event, match, world) -> ReplayText.onePlayerEvent(
                        event.data(),
                        match,
                        "replay.global.sparkdlc.jester_positions_shuffled"
                ));
        // Actor = the Jester whose fake corpse shrugged off a kill. / actor 为假尸体挡下一次击杀的小丑。
        ReplayRegistry.registerGlobalEventFormatter(JESTER_FAKE_DEATH_KILL_BLOCKED,
                (event, match, world) -> ReplayText.onePlayerEvent(
                        event.data(),
                        match,
                        "replay.global.sparkdlc.jester_fake_death_kill_blocked"
                ));
    }
}
