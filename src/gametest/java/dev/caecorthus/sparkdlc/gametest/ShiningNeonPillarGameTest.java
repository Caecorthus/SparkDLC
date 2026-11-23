package dev.caecorthus.sparkdlc.gametest;

import dev.caecorthus.sparkdlc.SparkDlcBlocks;
import dev.caecorthus.sparkdlc.block.NeonBlinkSettings;
import dev.caecorthus.sparkdlc.block.ShiningNeonPillarBlock;
import dev.caecorthus.sparkdlc.block.ShiningNeonPillarBlockEntity;
import dev.caecorthus.sparkdlc.block.ShiningNeonPillarService;
import dev.caecorthus.sparkdlc.net.ApplyNeonPillarSettingsC2SPacket;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import java.util.List;

/**
 * The shining neon pillar on a real server: saved settings are clamped, a column blinks as one block (its driver
 * first makes a mixed column whole, then flips all of it), and a player who is not a creative-mode operator cannot
 * change the settings even with a hand-made packet.
 * 在真实服务器上检查闪烁霓虹灯柱：保存的设置会被夹到范围内；一根灯柱作为一个整体闪烁（驱动方块先让亮灭不一的灯柱一致，再整体
 * 翻转）；不是创造模式管理员的玩家即使伪造数据包也无法修改设置。
 */
public final class ShiningNeonPillarGameTest implements FabricGameTest {
    private static final List<BlockPos> COLUMN = List.of(new BlockPos(1, 1, 1), new BlockPos(1, 2, 1), new BlockPos(1, 3, 1));

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void aColumnBlinksAsOne(TestContext context) {
        RegistryWrapper.WrapperLookup registries = context.getWorld().getRegistryManager();
        BlockState lit = SparkDlcBlocks.shiningNeonPillar().getDefaultState();
        COLUMN.forEach(pos -> context.setBlockState(pos, lit));
        context.setBlockState(COLUMN.get(0), lit.with(ShiningNeonPillarBlock.LIT, false));
        ShiningNeonPillarBlockEntity driver = pillar(context, COLUMN.get(1));
        NbtCompound saved = driver.createNbt(registries);
        saved.putInt("blink_on_ticks", 20);
        saved.putInt("blink_off_ticks", 0);
        saved.putBoolean("blink_random", false);
        driver.read(saved, registries);
        NeonBlinkSettings blink = driver.blink();
        if (blink == null || blink.onTicks() != 20 || blink.offTicks() != NeonBlinkSettings.MIN_TICKS) {
            throw new GameTestException("saved settings were not read and clamped: " + blink);
        }
        if (driver.createNbt(registries).getInt("blink_off_ticks") != NeonBlinkSettings.MIN_TICKS) {
            throw new GameTestException("clamped settings were not saved");
        }
        context.runAtTick(5, () -> requireColumn(context, true, "the fresh phase makes the column show the driver"));
        context.runAtTick(30, () -> {
            requireColumn(context, false, "after the lit phase the whole column is dark");
            context.complete();
        });
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void onlyACreativeOperatorChangesTheSettings(TestContext context) {
        BlockPos pos = COLUMN.get(0);
        context.setBlockState(pos, SparkDlcBlocks.shiningNeonPillar().getDefaultState());
        FakePlayer player = FakePlayer.get(context.getWorld());
        player.changeGameMode(GameMode.CREATIVE);
        if (ShiningNeonPillarService.mayConfigure(player)) {
            throw new GameTestException("a creative player without operator rights may configure the pillar");
        }
        ShiningNeonPillarService.applySettings(player, new ApplyNeonPillarSettingsC2SPacket(
                context.getAbsolutePos(pos), true, new NeonBlinkSettings(40, 40, false)));
        if (pillar(context, pos).blink() != null) {
            throw new GameTestException("settings from a player who may not configure were applied");
        }
        context.complete();
    }

    private static ShiningNeonPillarBlockEntity pillar(TestContext context, BlockPos pos) {
        if (context.getBlockEntity(pos) instanceof ShiningNeonPillarBlockEntity blockEntity) {
            return blockEntity;
        }
        throw new GameTestException("no shining neon pillar block entity at " + pos);
    }

    private static void requireColumn(TestContext context, boolean lit, String what) {
        for (BlockPos pos : COLUMN) {
            if (context.getBlockState(pos).get(ShiningNeonPillarBlock.LIT) != lit) {
                throw new GameTestException(what + ": " + pos + " is " + (lit ? "dark" : "lit"));
            }
        }
    }
}
