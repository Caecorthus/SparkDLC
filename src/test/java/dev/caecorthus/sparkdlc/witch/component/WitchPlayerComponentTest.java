package dev.caecorthus.sparkdlc.witch.component;

import dev.caecorthus.sparkdlc.witch.SparkWitch;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WitchPlayerComponentTest {
    @Test
    void forcedSkillPersistsThroughNbtWithoutClientRuntimeState() {
        NbtCompound tag = new NbtCompound();

        WitchForcedSkillState.writeToNbt(tag, SparkWitch.id("mighty_force"));

        assertEquals(SparkWitch.id("mighty_force"), WitchForcedSkillState.readFromNbt(tag));
    }
}
