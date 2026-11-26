package dev.caecorthus.sparkdlc.health;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

/**
 * Stable CCA contract {@code sparkdlc:body_health} (NEVER_COPY): the health of each {@link BodyPart} of its owner,
 * {@code 0..part.maxHealth()}, every part full by default. Server-authoritative and synced only to its owner, whose
 * HUD draws it; the client never predicts it and clamps what it receives. NBT keeps the values across a mid-round
 * relog (one PascalCase key per part, written only while the part is damaged; a missing key reads as full health);
 * Wathe's {@code ResetPlayer} restores every part ({@link BodyHealthService}). Storage only: no damage, kill or hit
 * logic lives here (which part a hit lands on is answered by {@code health.hitbox}), and the values are kept whether
 * or not {@link HealthModeComponent} is on.
 *
 * <p>Sync packet: one VarInt per part, in {@link BodyPart} declaration order.</p>
 *
 * 稳定 CCA 契约 {@code sparkdlc:body_health}（NEVER_COPY）：拥有者每个 {@link BodyPart} 的血量，范围
 * {@code 0..part.maxHealth()}，默认全部满血。服务端权威，只同步给本人，由本人 HUD 绘制；客户端从不预测，并对收到的值限幅。
 * NBT 使数值在局中重新登录后保留（每个部位一个 PascalCase 键，仅在该部位受损时写出；缺失的键按满血读取）；Wathe 的
 * {@code ResetPlayer} 会恢复全部部位（{@link BodyHealthService}）。这里只负责存储：不含伤害、击杀或命中逻辑（命中落在哪个部位由 {@code health.hitbox} 回答），且无论
 * {@link HealthModeComponent} 是否开启都保留数值。
 *
 * <p>同步包：按 {@link BodyPart} 的声明顺序，每个部位一个 VarInt。</p>
 */
public final class BodyHealthComponent implements AutoSyncedComponent {
    public static final ComponentKey<BodyHealthComponent> KEY = ComponentRegistry.getOrCreate(
            SparkDLC.id("body_health"), BodyHealthComponent.class);
    static final String HEAD_KEY = "Head";
    static final String CHEST_KEY = "Chest";
    static final String ABDOMEN_KEY = "Abdomen";
    static final String LEFT_ARM_KEY = "LeftArm";
    static final String RIGHT_ARM_KEY = "RightArm";
    static final String LEFT_LEG_KEY = "LeftLeg";
    static final String RIGHT_LEG_KEY = "RightLeg";

    private static final BodyPart[] PARTS = BodyPart.values();

    private final PlayerEntity player;
    /** Indexed by {@link BodyPart#ordinal()}. / 以 {@link BodyPart#ordinal()} 为下标。 */
    private final int[] health = new int[PARTS.length];

    public BodyHealthComponent(PlayerEntity player) {
        this.player = player;
        for (BodyPart part : PARTS) {
            health[part.ordinal()] = part.maxHealth();
        }
    }

    public int health(BodyPart part) {
        return health[part.ordinal()];
    }

    /** Sum of every part's current health, {@code 0..}{@link BodyHealthRules#TOTAL_MAX}. / 所有部位当前血量之和。 */
    public int totalHealth() {
        int total = 0;
        for (int value : health) {
            total += value;
        }
        return total;
    }

    /**
     * Server only: sets one part (clamped to its range) and syncs a change.
     * 仅服务端：设置一个部位的血量（限制在其范围内），有变化时同步。
     */
    public void setHealth(BodyPart part, int value) {
        int next = BodyHealthRules.clamp(part, value);
        if (next != health[part.ordinal()]) {
            health[part.ordinal()] = next;
            sync();
        }
    }

    /**
     * Server only: every part back to its maximum, with one sync if anything changed.
     * 仅服务端：所有部位恢复到上限，有变化时只同步一次。
     */
    public void restoreAll() {
        boolean changed = false;
        for (BodyPart part : PARTS) {
            if (health[part.ordinal()] != part.maxHealth()) {
                health[part.ordinal()] = part.maxHealth();
                changed = true;
            }
        }
        if (changed) {
            sync();
        }
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
        for (BodyPart part : PARTS) {
            buf.writeVarInt(health[part.ordinal()]);
        }
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        for (BodyPart part : PARTS) {
            health[part.ordinal()] = BodyHealthRules.clamp(part, buf.readVarInt());
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        for (BodyPart part : PARTS) {
            String key = nbtKey(part);
            health[part.ordinal()] = tag.contains(key, NbtElement.NUMBER_TYPE)
                    ? BodyHealthRules.clamp(part, tag.getInt(key))
                    : part.maxHealth();
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        for (BodyPart part : PARTS) {
            if (health[part.ordinal()] != part.maxHealth()) {
                tag.putInt(nbtKey(part), health[part.ordinal()]);
            }
        }
    }

    /** The saved key of a part; these are saved data and must not change. / 部位的存档键；属于存档数据，不可更改。 */
    static String nbtKey(BodyPart part) {
        return switch (part) {
            case HEAD -> HEAD_KEY;
            case CHEST -> CHEST_KEY;
            case ABDOMEN -> ABDOMEN_KEY;
            case LEFT_ARM -> LEFT_ARM_KEY;
            case RIGHT_ARM -> RIGHT_ARM_KEY;
            case LEFT_LEG -> LEFT_LEG_KEY;
            case RIGHT_LEG -> RIGHT_LEG_KEY;
        };
    }
}
