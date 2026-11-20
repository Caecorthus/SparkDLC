package dev.caecorthus.sparkdlc.witch.prop;

import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Text plumbing for the station timetable: the countdown, its per-player read cooldown and the boring bits of turning
 * ticks into something readable.
 * <p>
 * 车站时刻表的文本管线：倒计时、按玩家计的查看冷却，以及把 tick 变成可读文本的琐事。
 */
public final class PropText {
    /** Player -> world time before which the timetable stays quiet. / 玩家 -> 在此之前时刻表保持沉默的世界时间。 */
    private static final Map<UUID, Long> NEXT_READ = new ConcurrentHashMap<>();

    private PropText() {
    }

    /**
     * Registers the two server tick listeners this package needs (countdown formatting is stateless, so there is only
     * the cooldown sweep).
     * 注册本包需要的服务端监听：倒计时格式化是无状态的，因此只有冷却清扫。
     */
    public static void register() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = server.getOverworld().getTime();
            NEXT_READ.entrySet().removeIf(entry -> now > entry.getValue() + 20L * 60L);
        });
    }

    /**
     * Answers a timetable click: either "the game has not started" or the remaining round time, at most once every
     * 25 seconds per player.
     * 响应时刻表点击：要么「未开始游戏」，要么本局剩余时间；每名玩家最多 25 秒一次。
     */
    public static void showSchedule(ServerPlayerEntity player, World world) {
        long now = world.getTime();
        Long next = NEXT_READ.get(player.getUuid());
        if (next != null && now < next) {
            player.sendMessage(Text.translatable("block.sparkwitch.schedule.wait").formatted(Formatting.GRAY), true);
            return;
        }
        NEXT_READ.put(player.getUuid(), now + 25L * 20L);
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        if (!game.isRunning()) {
            player.sendMessage(Text.translatable("block.sparkwitch.schedule.not_started").formatted(Formatting.GRAY), true);
            return;
        }
        GameTimeComponent time = GameTimeComponent.KEY.get(world);
        int ticks = Math.max(0, time.getTime());
        player.sendMessage(
                Text.translatable("block.sparkwitch.schedule.remaining", formatTicks(ticks)).formatted(Formatting.GOLD),
                true
        );
        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.BLOCKS, 0.4F, 1.4F);
    }

    /** Turns ticks into {@code m:ss}. / 把 tick 变成 {@code m:ss}。 */
    public static String formatTicks(int ticks) {
        int totalSeconds = ticks / 20;
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
