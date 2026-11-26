package dev.caecorthus.sparkdlc.health;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

/**
 * Stable CCA contract {@code sparkdlc:health_mode}: the server-wide switch of the body-health system, on by default.
 * Saved with the scoreboard (key {@code Enabled}; a missing key reads as on) and synced to every client, so both
 * sides answer {@link #isEnabled(World)} the same way. Changed by {@code /sparkdlc:healthMode on|off}. The switch
 * only says whether the system is shown and, later, applied; it never clears {@link BodyHealthComponent} values.
 * 稳定 CCA 契约 {@code sparkdlc:health_mode}：部位血量系统的全服开关，默认开启。随计分板保存（键 {@code Enabled}；
 * 缺失的键按开启读取）并同步给所有客户端，因此两端对 {@link #isEnabled(World)} 的回答一致。由
 * {@code /sparkdlc:healthMode on|off} 修改。开关只决定该系统是否显示以及日后是否生效；它从不清除
 * {@link BodyHealthComponent} 的数值。
 */
public final class HealthModeComponent implements AutoSyncedComponent {
    public static final ComponentKey<HealthModeComponent> KEY = ComponentRegistry.getOrCreate(
            SparkDLC.id("health_mode"),
            HealthModeComponent.class
    );

    private static final String ENABLED_KEY = "Enabled";

    private final Scoreboard scoreboard;
    private boolean enabled = true;

    public HealthModeComponent(Scoreboard scoreboard, @Nullable MinecraftServer server) {
        this.scoreboard = scoreboard;
    }

    /**
     * Side-agnostic: whether the body-health system is on, read from the scoreboard of {@code world}. A client
     * answers from its synced copy, which is on until the first sync arrives.
     * 两端通用：部位血量系统是否开启，读自 {@code world} 的计分板。客户端依据同步副本作答，首次同步到达前视为开启。
     */
    public static boolean isEnabled(World world) {
        return KEY.get(world.getScoreboard()).isEnabled();
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Server only: stores the switch and syncs a change. / 仅服务端：保存开关，有变化时同步。 */
    public void setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            this.enabled = enabled;
            KEY.sync(scoreboard);
        }
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeBoolean(enabled);
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        this.enabled = buf.readBoolean();
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.enabled = !tag.contains(ENABLED_KEY, NbtElement.NUMBER_TYPE) || tag.getBoolean(ENABLED_KEY);
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean(ENABLED_KEY, enabled);
    }
}
