package dev.caecorthus.sparkdlc.replay.role;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.role.killer.bomber.drone.DroneKind;
import dev.doctor4t.wathe.record.replay.ReplayRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/**
 * Replay lines for the Bomber's drones.
 * 炸弹客无人机的回放文本。
 */
public final class DroneReplayEvents {
    public static final Identifier DRONE_PLACED = SparkDLC.id("drone_placed");
    public static final Identifier DRONE_GRENADE_DROPPED = SparkDLC.id("drone_grenade_dropped");
    public static final Identifier DRONE_DESTROYED = SparkDLC.id("drone_destroyed");
    public static final Identifier BOMB_DRONE_DETONATED = SparkDLC.id("bomb_drone_detonated");

    private DroneReplayEvents() {
    }

    static void register() {
        ReplayRegistry.registerGlobalEventFormatter(DRONE_PLACED, (event, match, world) -> {
            NbtCompound data = event.data();
            return ReplayText.onePlayerEvent(data, match, "replay.global.sparkdlc.drone_placed." + droneKindId(data));
        });
        ReplayRegistry.registerGlobalEventFormatter(DRONE_GRENADE_DROPPED,
                (event, match, world) -> ReplayText.onePlayerEvent(
                        event.data(),
                        match,
                        "replay.global.sparkdlc.drone_grenade_dropped"
                ));
        // Actor = who broke it (none when it crashed), target = the owner. / actor 为击毁者（坠毁时为空），target 为主人。
        ReplayRegistry.registerGlobalEventFormatter(DRONE_DESTROYED, (event, match, world) -> {
            NbtCompound data = event.data();
            return ReplayText.throwableHitEvent(data, match, "replay.global.sparkdlc.drone_destroyed." + droneKindId(data));
        });
        ReplayRegistry.registerGlobalEventFormatter(BOMB_DRONE_DETONATED,
                (event, match, world) -> ReplayText.onePlayerEvent(
                        event.data(),
                        match,
                        "replay.global.sparkdlc.bomb_drone_detonated"
                ));
    }

    /** Known drone kind id only, so a malformed event can never pick an arbitrary key. / 仅接受已知无人机型号 id。 */
    private static String droneKindId(NbtCompound data) {
        return DroneKind.BOMB.id().equals(data.getString("kind")) ? DroneKind.BOMB.id() : DroneKind.GRENADE.id();
    }
}
