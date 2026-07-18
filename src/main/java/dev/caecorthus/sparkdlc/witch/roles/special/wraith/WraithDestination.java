package dev.caecorthus.sparkdlc.witch.roles.special.wraith;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/** A verified server-side teleport destination. */
record WraithDestination(ServerWorld world, Vec3d position, float yaw, float pitch) {
}
