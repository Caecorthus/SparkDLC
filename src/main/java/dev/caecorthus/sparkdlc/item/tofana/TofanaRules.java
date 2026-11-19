package dev.caecorthus.sparkdlc.item.tofana;

import dev.caecorthus.sparkdlc.SparkWitch;
import net.minecraft.util.Identifier;

public final class TofanaRules {
    public static final Identifier DEATH_REASON_ID = SparkWitch.id("tofana_elixir");

    private TofanaRules() {
    }

    public static boolean canProtect(
            boolean forced,
            boolean distinctPlayerKiller,
            boolean killerPlayingAndAlive
    ) {
        return !forced && distinctPlayerKiller && killerPlayingAndAlive;
    }
}
