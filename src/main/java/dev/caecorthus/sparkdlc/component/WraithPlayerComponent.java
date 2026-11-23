package dev.caecorthus.sparkdlc.component;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

/**
 * Data-only owner of synchronized and persistent Wraith lifecycle state.
 * 仅负责保存与同步冤魂生命周期状态，不承载玩法逻辑。
 */
public final class WraithPlayerComponent implements AutoSyncedComponent {
    public static final ComponentKey<WraithPlayerComponent> KEY = ComponentRegistry.getOrCreate(
            SparkDLC.id("wraith_player"),
            WraithPlayerComponent.class
    );

    private final @Nullable Object provider;
    private final WraithPlayerState state = new WraithPlayerState();

    public WraithPlayerComponent(@Nullable Object provider) {
        this.provider = provider;
    }

    public boolean isActive() {
        return state.isActive();
    }

    public boolean isRestricted() {
        return state.isRestricted();
    }

    public boolean isPromoted() {
        return state.isPromoted();
    }

    public int getCompletedTasks() {
        return state.getCompletedTasks();
    }

    public WraithState.Alignment getAlignment() {
        return state.getAlignment();
    }

    public boolean isPromotionPending() {
        return state.isPromotionPending();
    }

    public void activate(WraithState.Alignment alignment) {
        state.activate(alignment);
        sync();
    }

    public int recordTaskCompletion() {
        if (!state.isActive()) {
            return state.getCompletedTasks();
        }
        int completedTasks = state.recordTaskCompletion();
        // Only the owner's task count changed; non-owners read only active/restricted, which are unchanged.
        // 只有本人的任务数改变；非本人只读取在场/受限标记，这两项没有变化。
        if (provider instanceof ServerPlayerEntity owner && owner.networkHandler != null) {
            KEY.syncWith(owner, (ComponentProvider) owner);
        } else {
            sync();
        }
        return completedTasks;
    }

    public void setPromotionPending(boolean pending) {
        if (state.setPromotionPending(pending)) {
            // The pending flag is written for the owner only, like the task count above: non-owners read only
            // active/restricted, which are unchanged, so they get no packet that only tells them a promotion is
            // being held. / 待晋升标记只写给本人，与上方任务数相同：非本人只读取在场/受限标记，这两项没有变化，
            // 因此他们不会收到仅能说明“晋升正被暂缓”的数据包。
            if (provider instanceof ServerPlayerEntity owner && owner.networkHandler != null) {
                KEY.syncWith(owner, (ComponentProvider) owner);
            } else {
                sync();
            }
        }
    }

    public void promote() {
        if (state.promote()) {
            sync();
        }
    }

    public void clear() {
        boolean changed = state.clear();
        if (changed) {
            sync();
        }
    }

    public void sync() {
        if (provider instanceof PlayerEntity player) {
            KEY.sync(player);
        }
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity recipient) {
        return true;
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        boolean owner = recipient == provider;
        buf.writeBoolean(state.isActive());
        buf.writeBoolean(state.isRestricted());
        buf.writeVarInt(owner ? state.getCompletedTasks() : 0);
        buf.writeVarInt(owner && state.getAlignment() != null ? state.getAlignment().ordinal() : -1);
        buf.writeBoolean(owner && state.isPromotionPending());
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        boolean syncedActive = buf.readBoolean();
        boolean syncedRestricted = buf.readBoolean();
        int syncedCompletedTasks = buf.readVarInt();
        int alignmentOrdinal = buf.readVarInt();
        boolean syncedPromotionPending = buf.readBoolean();

        WraithState.Alignment syncedAlignment = syncedActive
                && alignmentOrdinal >= 0
                && alignmentOrdinal < WraithState.Alignment.values().length
                ? WraithState.Alignment.values()[alignmentOrdinal]
                : null;
        state.restore(
                syncedActive,
                syncedRestricted,
                syncedCompletedTasks,
                syncedAlignment,
                syncedPromotionPending
        );
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (!state.isActive()) {
            return;
        }
        tag.putBoolean("WraithActive", true);
        tag.putBoolean("WraithRestricted", state.isRestricted());
        tag.putInt("WraithCompletedTasks", state.getCompletedTasks());
        if (state.getAlignment() != null) {
            tag.putString("WraithAlignment", state.getAlignment().name());
        }
        if (state.isPromotionPending()) {
            tag.putBoolean("WraithPromotionPending", true);
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        readState(tag);
    }

    private void readState(NbtCompound tag) {
        boolean active = tag.getBoolean("WraithActive");
        WraithState.Alignment alignment = active
                ? WraithState.Alignment.fromSerializedName(tag.getString("WraithAlignment"))
                : null;
        state.restore(
                active,
                tag.getBoolean("WraithRestricted"),
                tag.getInt("WraithCompletedTasks"),
                alignment,
                tag.getBoolean("WraithPromotionPending")
        );
    }
}
