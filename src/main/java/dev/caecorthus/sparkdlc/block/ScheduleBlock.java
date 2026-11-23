package dev.caecorthus.sparkdlc.block;

import com.mojang.serialization.MapCodec;
import dev.caecorthus.sparkdlc.SparkDlcBlocks;
import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Locale;

/**
 * Station timetable for map makers. Right-clicking it tells that player alone, on the action bar, how much time the
 * current round has left; outside an active round (lobby, start-up fade, round end) it says no game is in progress.
 * The pocket watch ({@link dev.caecorthus.sparkdlc.item.ScheduleWatchItem}) gives the same reading.
 * <p>
 * The reading is Wathe's server-side round timer ({@code GameTimeComponent}), the value that ends the round at zero,
 * so kill bonuses, the Time Stealer's stolen and added minutes and a frozen timer while the engine is wrecked all
 * show as they are. Wathe itself draws the timer only for roles with {@code canSeeTime} and for spectators: a placed
 * timetable or a handed-out watch tells anyone who uses it.
 * <p>
 * One reading every 25 seconds per player, shared by the timetable and the watch. The cooldown is the watch's own item
 * cooldown, so it shows on the watch, keeps no state of its own and lasts no longer than the player's session.
 * <p>
 * 供地图作者使用的车站时刻表。右键后只向该玩家的动作栏报出本局剩余时间；不在进行中的对局里（大厅、开局过渡、对局结束）
 * 时回复「未开始游戏」。怀表（{@link dev.caecorthus.sparkdlc.item.ScheduleWatchItem}）给出同样的读数。
 * <p>
 * 读数取自 Wathe 服务端的回合计时器（{@code GameTimeComponent}），即归零时结束对局的那个值，因此击杀加时、窃时者偷走或
 * 增加的时间、动力损毁期间冻结的计时都会如实显示。Wathe 自己只给 {@code canSeeTime} 的职业和旁观者显示计时器：
 * 放置的时刻表或发放的怀表会告诉任何使用它的人。
 * <p>
 * 每名玩家每 25 秒只能读一次，时刻表与怀表共用。冷却就是怀表自身的物品冷却，所以会显示在怀表上，不另存状态，
 * 也不会比玩家本次在线更久。
 */
public class ScheduleBlock extends Block {
    public static final MapCodec<ScheduleBlock> CODEC = createCodec(ScheduleBlock::new);

    /** Minimum gap between two readings, in ticks: 25 seconds. / 两次读数之间的最小间隔，tick：25 秒。 */
    public static final int READ_COOLDOWN_TICKS = 25 * 20;

    public ScheduleBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            showRemainingTime(serverPlayer);
        }
        return ActionResult.success(world.isClient);
    }

    /**
     * Answers one reading of the timetable or the watch, on the player's action bar. Server side only.
     * 响应一次时刻表或怀表的读数，显示在该玩家的动作栏。仅服务端。
     */
    public static void showRemainingTime(ServerPlayerEntity player) {
        World world = player.getWorld();
        if (GameWorldComponent.KEY.get(world).getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) {
            player.sendMessage(Text.translatable("block.sparkdlc.schedule.not_started").formatted(Formatting.GRAY), true);
            return;
        }
        Item watch = SparkDlcBlocks.scheduleWatch();
        ItemCooldownManager cooldowns = player.getItemCooldownManager();
        if (cooldowns.isCoolingDown(watch)) {
            player.sendMessage(Text.translatable("block.sparkdlc.schedule.wait").formatted(Formatting.GRAY), true);
            return;
        }
        cooldowns.set(watch, READ_COOLDOWN_TICKS);
        int ticks = GameTimeComponent.KEY.get(world).getTime();
        player.sendMessage(
                Text.translatable("block.sparkdlc.schedule.remaining", formatTicks(ticks)).formatted(Formatting.GOLD),
                true
        );
        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.BLOCKS, 0.4F, 1.4F);
    }

    /**
     * Turns ticks into {@code m:ss}, whole seconds rounded down, never negative; minutes are not capped at 59.
     * 把 tick 变成 {@code m:ss}：按整秒向下取整，不为负；分钟数不封顶于 59。
     */
    public static String formatTicks(int ticks) {
        int totalSeconds = Math.max(0, ticks) / 20;
        return String.format(Locale.ROOT, "%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
