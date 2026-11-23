package dev.caecorthus.sparkdlc.round.modevote;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.GameTimeComponent;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Scoreboard-bound vote that decides which game mode the <b>next</b> round uses. It runs after the map has already
 * been picked and all players have been teleported, so the order is: pick map, then pick mode.
 * <p>
 * 绑定在记分板上的投票，决定<b>下一局</b>使用哪种模式。它在地图已经选完、玩家都已传送之后才跑，所以顺序是：先选地图，
 * 再选模式。
 */
public class ModeVotingComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<ModeVotingComponent> KEY =
            ComponentRegistry.getOrCreate(SparkDLC.id("mode_voting"), ModeVotingComponent.class);

    /** 10 seconds, as requested. / 10 秒，按要求设定。 */
    public static final int VOTING_TICKS = 10 * 20;

    public static final Identifier DUAL_ROLE_MODE_ID = SparkDLC.id("dual_role_murder");

    /** The two choices shown to players, index-aligned. / 展示给玩家的两个选项，下标即投票值。 */
    private static final String[] MODE_NAMES = {"classic", "dual_role"};

    private final Scoreboard scoreboard;
    @Nullable
    private MinecraftServer server;

    private boolean votingActive = false;
    private int ticksRemaining = 0;
    private final int[] votes = new int[MODE_NAMES.length];
    private final Map<UUID, Integer> playerVotes = new HashMap<>();
    @Nullable
    private Identifier lastWinner = null;

    public ModeVotingComponent(Scoreboard scoreboard, @Nullable MinecraftServer server) {
        this.scoreboard = scoreboard;
        this.server = server;
    }

    public void setServer(@Nullable MinecraftServer server) {
        this.server = server;
    }

    public void sync() {
        KEY.sync(this.scoreboard);
    }

    public boolean isVotingActive() {
        return votingActive;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public int getVoteCount(int index) {
        return index >= 0 && index < votes.length ? votes[index] : 0;
    }

    public int getPlayerVoteCount() {
        return playerVotes.size();
    }

    public int getVotedIndex(UUID playerId) {
        return playerVotes.getOrDefault(playerId, -1);
    }

    public int getModeCount() {
        return MODE_NAMES.length;
    }

    @Nullable
    public Identifier getLastWinner() {
        return lastWinner;
    }

    /** Starts the 10 second vote. Called right after the map teleport finished. / 开始 10 秒投票，在地图传送完成后调用。 */
    public void startVoting() {
        if (server == null) {
            return;
        }
        int online = 0;
        for (ServerWorld world : server.getWorlds()) {
            online += world.getPlayers().size();
        }
        if (online <= 0) {
            return;
        }
        this.votingActive = true;
        this.ticksRemaining = VOTING_TICKS;
        this.playerVotes.clear();
        for (int i = 0; i < votes.length; i++) {
            votes[i] = 0;
        }
        this.sync();
    }

    public void castVote(UUID playerId, int index) {
        if (!votingActive || index < 0 || index >= votes.length) {
            return;
        }
        Integer old = playerVotes.get(playerId);
        if (old != null && old >= 0 && old < votes.length) {
            votes[old] = Math.max(0, votes[old] - 1);
        }
        playerVotes.put(playerId, index);
        votes[index]++;
        if (server != null) {
            int online = 0;
            for (ServerWorld world : server.getWorlds()) {
                online += world.getPlayers().size();
            }
            if (playerVotes.size() >= online && ticksRemaining > 5 * 20) {
                ticksRemaining = 5 * 20;
            }
        }
        this.sync();
    }

    private void finishVoting() {
        this.votingActive = false;
        int best = 0;
        for (int i = 1; i < votes.length; i++) {
            if (votes[i] > votes[best]) {
                best = i;
            }
        }
        boolean tie = false;
        for (int i = 0; i < votes.length; i++) {
            if (i != best && votes[i] == votes[best]) {
                tie = true;
            }
        }
        // On a tie nothing changes: the current mode stays. / 平票则维持当前模式。
        if (tie || votes[best] == 0) {
            this.lastWinner = null;
            this.sync();
            return;
        }
        GameMode dual = best == 1 ? WatheGameModes.GAME_MODES.get(DUAL_ROLE_MODE_ID) : null;
        GameMode chosen = dual != null ? dual : WatheGameModes.MURDER;
        this.lastWinner = chosen.identifier;
        applyMode(chosen);
        this.sync();
    }

    private void applyMode(GameMode mode) {
        if (server == null || mode == null) {
            return;
        }
        for (ServerWorld world : server.getWorlds()) {
            GameWorldComponent game = GameWorldComponent.KEY.get(world);
            if (game.getGameMode() != mode) {
                game.setGameMode(mode);
                game.sync();
            }
        }
    }

    public void reset() {
        this.votingActive = false;
        this.ticksRemaining = 0;
        this.playerVotes.clear();
        for (int i = 0; i < votes.length; i++) {
            votes[i] = 0;
        }
        this.sync();
    }

    @Override
    public void serverTick() {
        if (!votingActive) {
            return;
        }
        if (--ticksRemaining <= 0) {
            ticksRemaining = 0;
            finishVoting();
            return;
        }
        if (ticksRemaining % 20 == 0) {
            this.sync();
        }
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeBoolean(votingActive);
        buf.writeVarInt(ticksRemaining);
        for (int i = 0; i < votes.length; i++) {
            buf.writeVarInt(votes[i]);
        }
        buf.writeVarInt(getVotedIndex(recipient.getUuid()));
        buf.writeVarInt(MODE_NAMES.length);
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        this.votingActive = buf.readBoolean();
        this.ticksRemaining = buf.readVarInt();
        for (int i = 0; i < votes.length; i++) {
            votes[i] = buf.readVarInt();
        }
        buf.readVarInt();
        buf.readVarInt();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        tag.putBoolean("ModeVotingActive", votingActive);
        tag.putInt("ModeVotingTicks", ticksRemaining);
        tag.putString("ModeVotingWinner", lastWinner == null ? "" : lastWinner.toString());
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        this.votingActive = tag.getBoolean("ModeVotingActive");
        this.ticksRemaining = tag.getInt("ModeVotingTicks");
        String winner = tag.getString("ModeVotingWinner");
        this.lastWinner = winner.isEmpty() ? null : Identifier.tryParse(winner);
    }
}
