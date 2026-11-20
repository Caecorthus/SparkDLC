package dev.caecorthus.sparkdlc.compat.noellesroles;

import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import net.minecraft.entity.LivingEntity;
import org.agmas.noellesroles.ModEffects;

/** Bridges NoellesRoles' status-effect capability into the shared bilateral collision contract. */
public final class NoellesCollisionCompatibility {
    private static boolean registered;

    private NoellesCollisionCompatibility() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        SparkFactionApi.registerEntityCollisionExemption(entity -> {
            if (!(entity instanceof LivingEntity livingEntity)) {
                return false;
            }
            return livingEntity.hasStatusEffect(ModEffects.NO_COLLISION);
        });
    }
}
