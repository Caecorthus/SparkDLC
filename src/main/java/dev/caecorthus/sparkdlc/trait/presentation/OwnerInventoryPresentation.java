package dev.caecorthus.sparkdlc.trait.presentation;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/** Common-safe indirection: dedicated servers never resolve the client implementation. */
public final class OwnerInventoryPresentation {
    public interface Adapter {
        void visit(PlayerEntity player, BiConsumer<Text, List<Text>> visitor);
    }
    private static Adapter adapter;
    private OwnerInventoryPresentation() {}

    public static void install(Adapter clientAdapter) {
        adapter = Objects.requireNonNull(clientAdapter);
    }

    public static void visit(PlayerEntity player, BiConsumer<Text, List<Text>> visitor) {
        if (adapter != null && player != null && visitor != null) adapter.visit(player, visitor);
    }
}
