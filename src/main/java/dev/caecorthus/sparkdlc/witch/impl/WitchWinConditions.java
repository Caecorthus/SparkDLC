package dev.caecorthus.sparkdlc.witch.impl;

import dev.caecorthus.sparkdlc.factionapi.api.FactionWinContext;
import dev.caecorthus.sparkdlc.factionapi.api.FactionWinResult;

public final class WitchWinConditions {
    private WitchWinConditions() {
    }

    public static FactionWinResult checkWin(FactionWinContext context) {
        return FactionWinResult.none();
    }
}
