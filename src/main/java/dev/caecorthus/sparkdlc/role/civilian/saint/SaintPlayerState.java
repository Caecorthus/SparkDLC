package dev.caecorthus.sparkdlc.role.civilian.saint;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.RegistryByteBuf;

/**
 * The client-visible mirror of the Hellfire a player received for killing the Saint. The lock itself is owned by the
 * world component, keyed by UUID, and is still called Karma in code ({@link SaintKarmaState}); this mirror only feeds
 * the owner's HUD. Since the 2026-11-28 rebalance the Saint herself has no state here: her Hellfire is passive, so the
 * former ability timers (and their NBT keys {@code SaintHellfireCooldownTicks} / {@code SaintHellfireActiveTicks}, which
 * were round-scoped) are gone.
 * 玩家因击杀圣徒而承受的业火在客户端可见的镜像。锁本身由世界组件按 UUID 持有，代码中仍称为业障（{@link SaintKarmaState}）；
 * 此镜像只供本人的 HUD 使用。自 2026-11-28 重做起圣徒自身在这里没有状态：她的业火是被动，原先的技能计时（及其仅限本局的
 * NBT 键 {@code SaintHellfireCooldownTicks} / {@code SaintHellfireActiveTicks}）已移除。
 */
public final class SaintPlayerState {
    private boolean karmaMarked;
    private int karmaCooldownTicks;

    public boolean isKarmaMarked() {
        return karmaMarked;
    }

    public int karmaCooldownTicks() {
        return karmaCooldownTicks;
    }

    public boolean updateKarma(boolean marked, int remainingTicks) {
        int normalizedTicks = Math.max(0, remainingTicks);
        if (karmaMarked == marked && karmaCooldownTicks == normalizedTicks) {
            return false;
        }
        boolean shouldSync = karmaMarked != marked
                || normalizedTicks == 0
                || normalizedTicks % 20 == 0
                || Math.abs(karmaCooldownTicks - normalizedTicks) > 1;
        karmaMarked = marked;
        karmaCooldownTicks = normalizedTicks;
        return shouldSync;
    }

    public boolean isEmpty() {
        return !karmaMarked && karmaCooldownTicks == 0;
    }

    public void clear() {
        karmaMarked = false;
        karmaCooldownTicks = 0;
    }

    /**
     * Keeps the established Saint NBT keys stable while the in-memory owner is split out.
     * 拆分内存状态时仍保持既有圣徒 NBT 键不变。
     */
    public void writeNbt(NbtCompound tag) {
        if (karmaMarked) {
            tag.putBoolean("SaintKarmaMarked", true);
            if (karmaCooldownTicks > 0) {
                tag.putInt("SaintKarmaCooldownTicks", karmaCooldownTicks);
            }
        }
    }

    public void readNbt(NbtCompound tag) {
        karmaMarked = tag.getBoolean("SaintKarmaMarked");
        karmaCooldownTicks = karmaMarked && tag.contains("SaintKarmaCooldownTicks", NbtElement.NUMBER_TYPE)
                ? Math.max(0, tag.getInt("SaintKarmaCooldownTicks"))
                : 0;
    }

    /**
     * These four fields remain one contiguous packet segment in their original order. The first two were the Saint's
     * Hellfire cooldown and burn timers; the active skill is gone (2026-11-28), so they are written as 0 and skipped on
     * read, which keeps the layout of the shared player sync packet unchanged.
     * 这四个字段继续按原顺序占据同步包中的同一连续区段。前两个原为圣徒的业火冷却与燃烧计时；主动技能已移除（2026-11-28），
     * 因此写入 0、读取时跳过，共享玩家同步包的布局保持不变。
     */
    public void writeSync(RegistryByteBuf buf, boolean ownerVisible) {
        buf.writeVarInt(0);
        buf.writeVarInt(0);
        buf.writeBoolean(ownerVisible && karmaMarked);
        buf.writeVarInt(ownerVisible ? karmaCooldownTicks : 0);
    }

    public void readSync(RegistryByteBuf buf) {
        buf.readVarInt();
        buf.readVarInt();
        karmaMarked = buf.readBoolean();
        karmaCooldownTicks = Math.max(0, buf.readVarInt());
    }
}
