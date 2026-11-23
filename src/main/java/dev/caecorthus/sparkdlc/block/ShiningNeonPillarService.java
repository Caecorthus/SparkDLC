package dev.caecorthus.sparkdlc.block;

import dev.caecorthus.sparkdlc.SparkDlcBlocks;
import dev.caecorthus.sparkdlc.net.ApplyNeonPillarSettingsC2SPacket;
import dev.caecorthus.sparkdlc.net.OpenNeonPillarSettingsS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The blink settings of shining neon pillar columns: who may change them, the screen round trip and the flip itself.
 * A column is a run of shining neon pillars with the same axis, as for Wathe's right-click toggle; it is walked at
 * most {@link #MAX_REACH} blocks down and 2 * {@code MAX_REACH} + 1 blocks in all, and never into an unloaded chunk,
 * so a longer run blinks as separate windows.
 * <p>
 * 闪烁霓虹灯柱的闪烁设置：谁可以修改、界面往返以及翻转本身。一根灯柱是一串轴向相同的闪烁霓虹灯柱，与 Wathe 的右键切换一致；
 * 向下最多走 {@link #MAX_REACH} 格、总长最多 2 * {@code MAX_REACH} + 1 格，且从不进入未加载的区块，因此更长的一串会分成几段各自闪烁。
 */
public final class ShiningNeonPillarService {
    /** How far a column is walked from a block, each way. / 从一个方块出发沿每个方向最多走多远。 */
    public static final int MAX_REACH = 64;
    /** Extra reach allowed past the player's block interaction range, as vanilla's own checks. / 在玩家方块交互距离之外额外允许的距离，与原版检查一致。 */
    private static final double REACH_SLACK = 1.0;

    private ShiningNeonPillarService() {
    }

    /**
     * Only a creative-mode operator (permission level 2, the rule of command and structure blocks) may change how a
     * pillar blinks. During a round the players are not, so they can neither open the screen nor send settings.
     * <p>
     * 只有创造模式的管理员（权限等级 2，与命令方块、结构方块的规则相同）可以修改灯柱的闪烁方式。对局中的玩家不是，
     * 因此既打不开界面，也无法发送设置。
     */
    public static boolean mayConfigure(PlayerEntity player) {
        return player.isCreativeLevelTwoOp();
    }

    /** Sends the settings of the column holding {@code pos} to the player's screen. / 把 {@code pos} 所在灯柱的设置发到玩家的界面。 */
    static void openSettings(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!isPillar(state)) {
            return;
        }
        NeonBlinkSettings current = null;
        for (BlockPos member : column(world, pos, state.get(PillarBlock.AXIS))) {
            ShiningNeonPillarBlockEntity blockEntity = blockEntity(world, member);
            if (blockEntity != null && blockEntity.blink() != null) {
                current = blockEntity.blink();
                break;
            }
        }
        ServerPlayNetworking.send(player, new OpenNeonPillarSettingsS2CPacket(
                pos.toImmutable(), current != null, current != null ? current : NeonBlinkSettings.DEFAULT));
    }

    /**
     * Applies settings sent back by the screen. Runs on the server thread (Fabric calls play payload receivers
     * there). Every fact of the packet is checked again: the sender may configure, the position is loaded and within
     * reach, and it holds a shining neon pillar; the durations were already clamped when the packet was decoded.
     * Enabling makes the column's lowest block its only driver; disabling leaves the column steady and lit.
     * <p>
     * 应用界面回传的设置。在服务端线程上运行（Fabric 在该线程调用 play 数据包接收器）。数据包的每一项都重新校验：发送者有权修改、
     * 坐标已加载且在交互距离内、该处是闪烁霓虹灯柱；时长在解码时已被夹到范围内。开启时让灯柱最低的方块成为唯一的驱动方块；
     * 关闭时让灯柱保持常亮。
     */
    public static void applySettings(ServerPlayerEntity player, ApplyNeonPillarSettingsC2SPacket payload) {
        if (!mayConfigure(player)) {
            return;
        }
        ServerWorld world = player.getServerWorld();
        BlockPos pos = payload.pos();
        if (!world.isChunkLoaded(pos) || !player.canInteractWithBlockAt(pos, REACH_SLACK)) {
            return;
        }
        BlockState state = world.getBlockState(pos);
        if (!isPillar(state)) {
            return;
        }
        List<BlockPos> column = column(world, pos, state.get(PillarBlock.AXIS));
        for (BlockPos member : column) {
            ShiningNeonPillarBlockEntity blockEntity = blockEntity(world, member);
            if (blockEntity != null) {
                blockEntity.setBlink(null);
            }
        }
        if (payload.blinking()) {
            ShiningNeonPillarBlockEntity driver = blockEntity(world, column.get(0));
            if (driver != null) {
                driver.setBlink(payload.settings());
            }
            return;
        }
        for (BlockPos member : column) {
            BlockState memberState = world.getBlockState(member);
            if (!memberState.get(ShiningNeonPillarBlock.LIT)) {
                world.setBlockState(member, memberState.with(ShiningNeonPillarBlock.LIT, true), Block.NOTIFY_ALL);
            }
        }
    }

    /**
     * Flips the column the driver at {@code pos} belongs to, with a quiet lever click.
     * 翻转 {@code pos} 处驱动方块所属的灯柱，并发出轻微的拉杆声。
     */
    static void flipColumn(World world, BlockPos pos, BlockState state, ShiningNeonPillarBlockEntity driver) {
        boolean lit = !state.get(ShiningNeonPillarBlock.LIT);
        showOnColumn(world, pos, state, driver, lit);
        world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.25F, lit ? 1.4F : 0.7F);
    }

    /**
     * Sets every block of the driver's column to {@code lit}, so a column that a manual toggle or a restore left mixed
     * is whole again. Should two columns have been joined, the first driver to get here keeps the column and the
     * other one's settings are dropped.
     * <p>
     * 把驱动方块所在灯柱的每个方块设为 {@code lit}，使被手动切换或恢复弄乱的灯柱重新一致。若两根灯柱被接在了一起，先执行到这里的
     * 驱动方块保留这根灯柱，另一个的设置被丢弃。
     */
    static void showOnColumn(World world, BlockPos pos, BlockState state, ShiningNeonPillarBlockEntity driver, boolean lit) {
        for (BlockPos member : column(world, pos, state.get(PillarBlock.AXIS))) {
            ShiningNeonPillarBlockEntity blockEntity = blockEntity(world, member);
            if (blockEntity != null && blockEntity != driver && blockEntity.blink() != null) {
                blockEntity.setBlink(null);
            }
            BlockState memberState = world.getBlockState(member);
            if (memberState.get(ShiningNeonPillarBlock.LIT) != lit) {
                world.setBlockState(member, memberState.with(ShiningNeonPillarBlock.LIT, lit), Block.NOTIFY_ALL);
            }
        }
    }

    /**
     * The column through {@code pos}, from its lowest block along {@code axis} to its highest.
     * 经过 {@code pos} 的灯柱，沿 {@code axis} 从最低的方块到最高的方块。
     */
    private static List<BlockPos> column(World world, BlockPos pos, Direction.Axis axis) {
        Direction up = Direction.from(axis, Direction.AxisDirection.POSITIVE);
        BlockPos lowest = pos.toImmutable();
        for (int i = 0; i < MAX_REACH && isMember(world, lowest.offset(up.getOpposite()), axis); i++) {
            lowest = lowest.offset(up.getOpposite());
        }
        List<BlockPos> column = new ArrayList<>();
        BlockPos member = lowest;
        column.add(member);
        while (column.size() <= 2 * MAX_REACH && isMember(world, member.offset(up), axis)) {
            member = member.offset(up);
            column.add(member);
        }
        return column;
    }

    private static boolean isMember(World world, BlockPos pos, Direction.Axis axis) {
        if (!world.isChunkLoaded(pos)) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        return isPillar(state) && state.get(PillarBlock.AXIS) == axis;
    }

    private static boolean isPillar(BlockState state) {
        return state.isOf(SparkDlcBlocks.shiningNeonPillar());
    }

    @Nullable
    private static ShiningNeonPillarBlockEntity blockEntity(World world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof ShiningNeonPillarBlockEntity blockEntity ? blockEntity : null;
    }
}
