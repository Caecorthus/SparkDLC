package dev.caecorthus.sparkdlc.component;

import dev.caecorthus.sparkdlc.SparkWitch;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithRoundSettingsSnapshot;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithSettings;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithSettingsNbtCodec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Stores only the round-scoped Wraith conversion budget and consumed player slots.
 * 只保存本局冤魂转化上限与已消耗的玩家名额。
 */
public final class WraithRoundComponent implements Component {
    public static final ComponentKey<WraithRoundComponent> KEY = ComponentRegistry.getOrCreate(
            SparkWitch.id("wraith_round"),
            WraithRoundComponent.class
    );

    private static final String STARTING_PLAYER_COUNT_KEY = "StartingPlayerCount";
    private static final String CONSUMED_PLAYERS_KEY = "ConsumedPlayers";

    private final WraithRoundQuota quota = new WraithRoundQuota();
    private WraithRoundSettingsSnapshot settingsSnapshot = new WraithRoundSettingsSnapshot(
            0, WraithSettings.DEFAULT_CHANCE, WraithSettings.DEFAULT_MINIMUM, WraithSettings.DEFAULT_DIVIDEND);

    public WraithRoundComponent(@Nullable Object provider) {
    }

    public void beginRound(int startingPlayerCount) {
        beginRound(startingPlayerCount, WraithSettings.DEFAULT);
    }

    public void beginRound(int startingPlayerCount, WraithSettings settings) {
        settingsSnapshot = new WraithRoundSettingsSnapshot(
                startingPlayerCount, settings.chance(), settings.minimum(), settings.dividend());
        quota.beginRound(settingsSnapshot.startingPlayers(), settingsSnapshot.minimum(), settingsSnapshot.dividend());
    }

    public WraithRoundSettingsSnapshot getSettingsSnapshot() {
        return settingsSnapshot;
    }

    public int getStartingPlayerCount() {
        return quota.getStartingPlayerCount();
    }

    public int getCap() {
        return quota.getCap();
    }

    public int getConsumedCount() {
        return quota.getConsumedCount();
    }

    public Set<UUID> getConsumedPlayers() {
        return quota.getConsumedPlayers();
    }

    public boolean hasCapacity() {
        return quota.hasCapacity();
    }

    public boolean tryConsume(UUID playerUuid) {
        return quota.tryConsume(playerUuid);
    }

    public void clearRound() {
        settingsSnapshot = new WraithRoundSettingsSnapshot(
                0, WraithSettings.DEFAULT_CHANCE, WraithSettings.DEFAULT_MINIMUM, WraithSettings.DEFAULT_DIVIDEND);
        quota.clearRound();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt(STARTING_PLAYER_COUNT_KEY, settingsSnapshot.startingPlayers());
        WraithSettingsNbtCodec.writeRound(tag, settingsSnapshot);
        NbtList consumed = new NbtList();
        for (UUID playerUuid : quota.getConsumedPlayers()) {
            consumed.add(NbtString.of(playerUuid.toString()));
        }
        tag.put(CONSUMED_PLAYERS_KEY, consumed);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        settingsSnapshot = WraithSettingsNbtCodec.readRound(tag, tag.getInt(STARTING_PLAYER_COUNT_KEY));
        quota.restore(settingsSnapshot.startingPlayers(), settingsSnapshot.minimum(), settingsSnapshot.dividend(),
                readConsumedPlayers(tag));
    }

    private static Set<UUID> readConsumedPlayers(NbtCompound tag) {
        LinkedHashSet<UUID> restoredConsumedPlayers = new LinkedHashSet<>();
        NbtList consumed = tag.getList(CONSUMED_PLAYERS_KEY, NbtElement.STRING_TYPE);
        for (int index = 0; index < consumed.size(); index++) {
            try {
                restoredConsumedPlayers.add(UUID.fromString(consumed.getString(index)));
            } catch (IllegalArgumentException ignored) {
                // Ignore a malformed entry without losing the remaining round snapshot.
                // 忽略损坏条目，同时保留其余本局快照。
            }
        }
        return restoredConsumedPlayers;
    }
}
