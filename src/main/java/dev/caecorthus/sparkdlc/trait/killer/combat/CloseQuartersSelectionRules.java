package dev.caecorthus.sparkdlc.trait.killer.combat;

import java.util.function.Supplier;
import java.util.stream.Stream;

/** Shop-only selection, using the registered item's identity rather than its class or inventory access. */
final class CloseQuartersSelectionRules {
    private CloseQuartersSelectionRules() {
    }

    static <T> boolean canSelect(boolean eligibleRole, T knife, Supplier<Stream<T>> shopItems) {
        return eligibleRole && shopItems.get().anyMatch(item -> item == knife);
    }
}
