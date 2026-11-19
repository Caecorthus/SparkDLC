package dev.caecorthus.sparkdlc.witch.compat;

import dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Bridge to the four public SparkTraits Wraith lifecycle methods. SparkTraits is fused into SparkDLC, so the calls
 * are direct (compiled) instead of reflective.
 * 通往 SparkTraits 四个公共冤魂生命周期方法的桥接。SparkTraits 已融合进 SparkDLC，因此为直接（编译期）调用而非反射。
 */
public final class SparkTraitsWraithBridge {
    private SparkTraitsWraithBridge() {
    }

    public static NbtCompound capture(@Nullable PlayerEntity player) {
        return SparkTraitsApi.captureWraithTraitSnapshot(player).copy();
    }

    public static void restore(@Nullable PlayerEntity player, @Nullable NbtCompound snapshot) {
        if (player == null || snapshot == null) {
            return;
        }
        SparkTraitsApi.restoreWraithTraitSnapshot(player, snapshot.copy());
    }

    public static void clear(@Nullable PlayerEntity player, boolean gameEnd) {
        if (player == null) {
            return;
        }
        SparkTraitsApi.clearWraithTraits(player, gameEnd);
    }

    public static boolean hasLastStandTriggered(@Nullable ServerWorld world, @Nullable UUID playerUuid) {
        return world != null && playerUuid != null
                && SparkTraitsApi.hasLastStandTriggeredThisRound(world, playerUuid);
    }

    public static boolean didLastStandTriggerSince(@Nullable PlayerEntity player, boolean triggeredBefore) {
        return player != null
                && didLastStandTrigger(
                        triggeredBefore,
                        hasLastStandTriggered((ServerWorld) player.getWorld(), player.getUuid())
                );
    }

    static boolean didLastStandTrigger(boolean triggeredBefore, boolean triggeredAfter) {
        return !triggeredBefore && triggeredAfter;
    }
}
