package dev.caecorthus.sparkdlc.strength.role.engineer;

import dev.caecorthus.sparkdlc.strength.SparkStrengthEntities;
import dev.caecorthus.sparkdlc.strength.component.engineer.EngineerStunnedPlayerComponent;
import dev.caecorthus.sparkdlc.strength.entity.CaptureDeviceEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Round-state cleanup entrypoint for capture devices.
 * 捕捉装置的局状态清理入口。
 */
public final class EngineerCaptureDeviceService {
    private EngineerCaptureDeviceService() {
    }

    public static void clearPlayer(ServerPlayerEntity player) {
        EngineerStunnedPlayerComponent.KEY.get(player).clear();
    }

    public static void clearRoundState(ServerWorld world) {
        for (CaptureDeviceEntity entity : world.getEntitiesByType(
                SparkStrengthEntities.captureDevice(),
                captureDevice -> true
        )) {
            entity.discard();
        }
        for (ServerPlayerEntity player : world.getPlayers()) {
            clearPlayer(player);
        }
    }
}
