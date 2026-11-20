package dev.caecorthus.sparkdlc.trait.killer.conscience;

import java.util.UUID;

public interface ConscienceScorpionBed {
    boolean sparkdlc$hasConscienceScorpion();

    UUID sparkdlc$getConscienceScorpionPoisoner();

    default UUID sparkdlc$getConsciencePoisoner() {
        return sparkdlc$getConscienceScorpionPoisoner();
    }

    void sparkdlc$setConscienceScorpion(boolean hasScorpion, UUID poisoner);
}
