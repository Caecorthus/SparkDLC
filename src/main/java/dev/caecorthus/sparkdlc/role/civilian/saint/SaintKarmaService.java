package dev.caecorthus.sparkdlc.role.civilian.saint;

import dev.caecorthus.sparkdlc.SparkDlcSounds;
import dev.caecorthus.sparkdlc.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.component.WitchWorldComponent;
import dev.caecorthus.sparkdlc.util.TickTime;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;

/**
 * Burns the Saint's killer with Hellfire. "Karma" is the name the code keeps for that lock (its state, runtime, cooldown
 * writer and the owner's mirror): for its whole duration every item the player carries is kept on cooldown. It is
 * inflicted once, when the Saint dies, and nothing re-triggers it (until the 2026-11-28 rebalance it was a round-long
 * mark that every later kill, poison-needle hit or bomb pass of its bearer triggered again).
 * 以业火灼烧击杀圣徒的玩家。“业障”是代码中对这把锁（其状态、运行时、冷却写入与本人镜像）沿用的名称：在整个持续时间内，
 * 该玩家携带的每件物品都保持在冷却中。它只在圣徒死亡时施加一次，此后不会被再次触发（2026-11-28 重做之前，它是整局有效的
 * 标记，背负者之后的每次击杀、毒针命中或炸弹传递都会再次触发）。
 */
public final class SaintKarmaService {
    private SaintKarmaService() {
    }

    /**
     * Burns {@code player} for {@code durationTicks} (a longer Hellfire already running is kept), rings the bell and
     * tells them the seconds; returns whether they burn now ({@code false} only for an immune role).
     * 让 {@code player} 承受 {@code durationTicks} 刻的业火（已有更长的业火时保留更长的），敲响钟声并告知秒数；
     * 返回其此刻是否正在燃烧（仅免疫职业返回 {@code false}）。
     */
    public static boolean inflict(ServerPlayerEntity player, int durationTicks) {
        if (SaintRules.isKarmaImmune(currentRole(player))) {
            clear(player);
            return false;
        }
        WitchWorldComponent worldComponent = WitchWorldComponent.KEY.get(player.getServerWorld());
        int remainingTicks = worldComponent.inflictSaintKarma(player.getUuid(), durationTicks);
        if (remainingTicks <= 0) {
            return false;
        }
        SaintKarmaCooldownService.apply(player, remainingTicks);
        updatePlayerMirror(player, true, remainingTicks);
        sendBell(player, 1.0F);
        sendBell(player, 0.97F);
        player.sendMessage(
                Text.translatable("message.sparkdlc.saint.hellfire.received",
                        TickTime.ceilSecondsAtLeastOne(remainingTicks)),
                true
        );
        return true;
    }

    public static void clear(ServerPlayerEntity player) {
        WitchWorldComponent worldComponent = WitchWorldComponent.KEY.get(player.getServerWorld());
        worldComponent.clearSaintKarma(player.getUuid());
        updatePlayerMirror(player, false, 0);
    }

    private static Role currentRole(ServerPlayerEntity player) {
        return GameWorldComponent.KEY.get(player.getServerWorld()).getRole(player);
    }

    private static void sendBell(ServerPlayerEntity player, float pitch) {
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(
                RegistryEntry.of(SparkDlcSounds.SAINT_BELL),
                SoundCategory.PLAYERS,
                player.getX(),
                player.getY(),
                player.getZ(),
                5.0F,
                pitch,
                player.getWorld().random.nextLong()
        ));
    }

    static void updatePlayerMirror(ServerPlayerEntity player, boolean marked, int remainingTicks) {
        WitchPlayerComponent component = WitchPlayerComponent.KEY.get(player);
        if (component.getSaintState().updateKarma(marked, remainingTicks)) {
            component.sync();
        }
    }
}
