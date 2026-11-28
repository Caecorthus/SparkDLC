package dev.caecorthus.sparkdlc.role.civilian.discoverer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

/**
 * Stable CCA contract {@code sparkdlc:discoverer_player} (NEVER_COPY): the absolute world tick at which the
 * Discoverer's Night Vision is ready again, synced only to its owner (whose HUD counts it down against the client
 * world clock; the client never predicts it), plus whether the skill was already used this round (server only; it
 * picks the 60 s or the 75 s cooldown). NBT keeps both across a mid-round relog and a server restart; role assignment
 * and Wathe's {@code ResetPlayer} clear both. The forced-cooldown store {@code sparkdlc:discoverer_night_vision} only
 * ever raises the ready tick.
 * 稳定 CCA 契约 {@code sparkdlc:discoverer_player}（NEVER_COPY）：探索者夜视再次就绪的绝对世界刻，只同步给本人（本人 HUD
 * 依据客户端世界时钟倒数；客户端从不预测），以及本局是否已用过该技能（仅服务端；用于选择 60 秒或 75 秒冷却）。NBT 使两者在
 * 局中重新登录与服务器重启后保留；身份分配与 Wathe 的 {@code ResetPlayer} 会清空两者。强制冷却存储
 * {@code sparkdlc:discoverer_night_vision} 只会把就绪刻推后。
 */
public final class DiscovererComponent implements AutoSyncedComponent {
    public static final ComponentKey<DiscovererComponent> KEY = ComponentRegistry.getOrCreate(
            DiscovererRules.ROLE_ID.withSuffixedPath("_player"), DiscovererComponent.class);
    static final String NIGHT_VISION_READY_TICK = "NightVisionReadyTick";
    static final String NIGHT_VISION_USED = "NightVisionUsed";

    private final PlayerEntity player;
    private long nightVisionReadyTick;
    private boolean nightVisionUsed;

    public DiscovererComponent(PlayerEntity player) {
        this.player = player;
    }

    public long nightVisionReadyTick() {
        return nightVisionReadyTick;
    }

    /** Ticks until Night Vision is ready at world time {@code now}; 0 when ready. / 在世界时间 now 时距夜视就绪的刻数，就绪为 0。 */
    public int nightVisionCooldownRemaining(long now) {
        return (int) Math.clamp(nightVisionReadyTick - now, 0L, Integer.MAX_VALUE);
    }

    /** Server only: moves the ready tick and syncs a change. / 仅服务端：设置就绪刻，有变化时同步。 */
    public void setNightVisionReadyTick(long tick) {
        long next = Math.max(0L, tick);
        if (next != nightVisionReadyTick) {
            nightVisionReadyTick = next;
            sync();
        }
    }

    /** Whether the skill was already used this round. / 本局是否已用过该技能。 */
    public boolean nightVisionUsed() {
        return nightVisionUsed;
    }

    /** Server only; never synced, the client has no use for it. / 仅服务端；不同步，客户端用不到。 */
    void markNightVisionUsed() {
        nightVisionUsed = true;
    }

    /** Server only: a fresh round, ready and unused. / 仅服务端：新的一局，就绪且未使用。 */
    public void clear() {
        nightVisionUsed = false;
        setNightVisionReadyTick(0L);
    }

    private void sync() {
        if (player instanceof ServerPlayerEntity) {
            KEY.sync(player);
        }
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity recipient) {
        return recipient == player;
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeVarLong(nightVisionReadyTick);
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        nightVisionReadyTick = Math.max(0L, buf.readVarLong());
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        nightVisionReadyTick = Math.max(0L, tag.getLong(NIGHT_VISION_READY_TICK));
        nightVisionUsed = tag.getBoolean(NIGHT_VISION_USED);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (nightVisionReadyTick > 0L) {
            tag.putLong(NIGHT_VISION_READY_TICK, nightVisionReadyTick);
        }
        if (nightVisionUsed) {
            tag.putBoolean(NIGHT_VISION_USED, true);
        }
    }
}
