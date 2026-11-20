package dev.caecorthus.sparkdlc.witch.block.props;

import dev.caecorthus.sparkdlc.witch.prop.PropText;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Station timetable. Right-clicking it reports how much time is left in the round on the action bar. It refuses to
 * answer more than once every 25 seconds per player, and outside a round it says the game has not started.
 * <p>
 * 车站时刻表。右键会通过动作栏报出本局剩余时间。同一玩家 25 秒内只回应一次；不在对局中时回复「未开始游戏」。
 */
public class ScheduleBlock extends Block {
    /** Minimum gap between two reads, in ticks: 25 seconds. / 两次查看之间的最小间隔，tick：25 秒。 */
    public static final int READ_COOLDOWN_TICKS = 25 * 20;

    public ScheduleBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (player instanceof ServerPlayerEntity serverPlayer) {
            PropText.showSchedule(serverPlayer, world);
        }
        return ActionResult.SUCCESS;
    }
}
