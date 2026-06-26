package dev.caecorthus.sparkdlc.factionapi.api;

@FunctionalInterface
public interface FactionWinCondition {
    FactionWinResult checkWin(FactionWinContext context);

    static FactionWinCondition none() {
        return context -> FactionWinResult.none();
    }
}
