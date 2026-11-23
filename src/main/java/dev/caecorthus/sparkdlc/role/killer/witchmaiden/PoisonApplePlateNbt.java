package dev.caecorthus.sparkdlc.role.killer.witchmaiden;

import dev.caecorthus.sparkdlc.legacy.LegacyDataNames;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

/** Separates server-authoritative Poison Apple data from the single client-visible armed bit. */
public final class PoisonApplePlateNbt {
    /**
     * Renamed from its former-mod name on 2026-11-20 (owner: "就数据可以改"); data saved under the old name is still
     * read (current name first) and written back under this one (legacy.LegacyDataNames).
     * 2026-11-20 起不再使用旧模组名（所有者：“就数据可以改”）；旧名字下保存的数据仍可读取（先读当前名字），并以本名字写回
     * （legacy.LegacyDataNames）。
     */
    public static final String STATE_KEY = "sparkdlc:poison_apple";
    public static final String ARMED_KEY = "sparkdlc:poison_apple_armed";
    private static final String PLACER_KEY = "Placer";
    private static final String TAKE_COUNT_KEY = "SuccessfulTakes";
    private static final String MATCH_KEY = "Match";

    private PoisonApplePlateNbt() {
    }

    public static void writePersistent(NbtCompound root, @Nullable PoisonApplePlateState state) {
        root.remove(STATE_KEY);
        if (state == null) {
            return;
        }
        NbtCompound stateNbt = new NbtCompound();
        stateNbt.putUuid(PLACER_KEY, state.placerUuid());
        stateNbt.putInt(TAKE_COUNT_KEY, state.successfulTakeCount());
        stateNbt.putUuid(MATCH_KEY, state.matchUuid());
        root.put(STATE_KEY, stateNbt);
    }

    public static @Nullable PoisonApplePlateState readPersistent(NbtCompound root) {
        String stateKey = LegacyDataNames.keyIn(root, STATE_KEY);
        if (!root.contains(stateKey, NbtElement.COMPOUND_TYPE)) {
            return null;
        }
        NbtCompound stateNbt = root.getCompound(stateKey);
        if (!stateNbt.containsUuid(PLACER_KEY) || !stateNbt.containsUuid(MATCH_KEY)) {
            return null;
        }
        return new PoisonApplePlateState(
                stateNbt.getUuid(PLACER_KEY),
                stateNbt.getInt(TAKE_COUNT_KEY),
                stateNbt.getUuid(MATCH_KEY)
        );
    }

    /** The client-visible armed bit (the current key first, then the old one). / 客户端可见的已布置标记（先当前键再旧键）。 */
    public static boolean readArmed(NbtCompound root) {
        return root.getBoolean(LegacyDataNames.keyIn(root, ARMED_KEY));
    }

    public static void stripForClient(NbtCompound root, boolean armed) {
        root.remove(STATE_KEY);
        root.putBoolean(ARMED_KEY, armed);
    }
}
