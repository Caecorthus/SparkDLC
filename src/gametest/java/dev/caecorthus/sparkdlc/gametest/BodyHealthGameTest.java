package dev.caecorthus.sparkdlc.gametest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.caecorthus.sparkdlc.health.BodyHealthComponent;
import dev.caecorthus.sparkdlc.health.BodyHealthRules;
import dev.caecorthus.sparkdlc.health.BodyPart;
import dev.caecorthus.sparkdlc.health.HealthModeComponent;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.world.GameMode;

import java.util.List;
import java.util.Set;

/**
 * The body-health pool on a running server: the stored values of a player (defaults, clamping, restore, saved names)
 * and the server-wide switch with its command. Nothing here deals damage; the mod has no such logic yet.
 * 运行中的服务器上的部位血量池：玩家的存储数值（默认值、限幅、恢复、存档键名），以及全服开关及其命令。这里不造成任何伤害；
 * 模组目前还没有这类逻辑。
 */
public final class BodyHealthGameTest implements FabricGameTest {
    /**
     * Every part starts full, a write is clamped to the part's range, {@code restoreAll} fills every part, and the
     * saved form holds one PascalCase key per damaged part only: a missing key is full health, an out-of-range saved
     * value is clamped on load.
     * 每个部位初始满血，写入会被限制在该部位的范围内，{@code restoreAll} 补满所有部位；存档形式只为受损部位各保存一个
     * PascalCase 键：缺失的键即满血，越界的存档值在读取时被限幅。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void aBodyHealthPoolStartsFullClampsAndRestores(TestContext context) {
        RegistryWrapper.WrapperLookup registries = context.getWorld().getRegistryManager();
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        BodyHealthComponent body = BodyHealthComponent.KEY.get(player);
        check(BodyHealthRules.TOTAL_MAX == 440, "total maximum: " + BodyHealthRules.TOTAL_MAX);
        for (BodyPart part : BodyPart.values()) {
            check(body.health(part) == part.maxHealth(), part.id() + " starts at " + body.health(part));
        }
        check(body.totalHealth() == BodyHealthRules.TOTAL_MAX, "a fresh pool totals " + body.totalHealth());
        NbtCompound full = new NbtCompound();
        body.writeToNbt(full, registries);
        check(full.isEmpty(), "a full pool saves nothing: " + full);

        body.setHealth(BodyPart.HEAD, -5);
        body.setHealth(BodyPart.CHEST, 999);
        body.setHealth(BodyPart.LEFT_ARM, 12);
        check(body.health(BodyPart.HEAD) == 0, "a negative head value is stored as " + body.health(BodyPart.HEAD));
        check(body.health(BodyPart.CHEST) == BodyPart.CHEST.maxHealth(),
                "a chest value above the maximum is stored as " + body.health(BodyPart.CHEST));
        check(body.health(BodyPart.LEFT_ARM) == 12, "left arm: " + body.health(BodyPart.LEFT_ARM));
        int expectedTotal = BodyHealthRules.TOTAL_MAX - BodyPart.HEAD.maxHealth() - (BodyPart.LEFT_ARM.maxHealth() - 12);
        check(body.totalHealth() == expectedTotal, "total after damage: " + body.totalHealth());

        NbtCompound saved = new NbtCompound();
        body.writeToNbt(saved, registries);
        check(saved.getKeys().equals(Set.of("Head", "LeftArm")) && saved.getInt("Head") == 0
                && saved.getInt("LeftArm") == 12, "a damaged pool saves its damaged parts only: " + saved);

        body.restoreAll();
        check(body.totalHealth() == BodyHealthRules.TOTAL_MAX, "total after restoreAll: " + body.totalHealth());
        body.readFromNbt(saved, registries);
        check(body.health(BodyPart.HEAD) == 0 && body.health(BodyPart.LEFT_ARM) == 12
                && body.health(BodyPart.CHEST) == BodyPart.CHEST.maxHealth(), "the saved pool loads back: " + saved);

        NbtCompound outOfRange = new NbtCompound();
        outOfRange.putInt("Head", 999);
        outOfRange.putInt("RightLeg", -3);
        body.readFromNbt(outOfRange, registries);
        check(body.health(BodyPart.HEAD) == BodyPart.HEAD.maxHealth() && body.health(BodyPart.RIGHT_LEG) == 0
                && body.health(BodyPart.LEFT_ARM) == BodyPart.LEFT_ARM.maxHealth(),
                "out-of-range saved values are clamped and missing keys are full");
        body.readFromNbt(new NbtCompound(), registries);
        check(body.totalHealth() == BodyHealthRules.TOTAL_MAX, "an empty save is a full pool: " + body.totalHealth());
        context.complete();
    }

    /**
     * The switch is read the same way through the world and through the server scoreboard, {@code on} / {@code off}
     * change it, and a save without the key reads as on. {@code set} checks the part and the part's own range before
     * it resolves the selector; with nobody online a valid {@code set} or {@code reset} stops at the empty selector.
     * 开关经由世界读取与经由服务器计分板读取的结果一致，{@code on} / {@code off} 会修改它，没有该键的存档按开启读取。
     * {@code set} 在解析选择器之前先校验部位以及该部位自身的范围；没有玩家在线时，合法的 {@code set} 或 {@code reset}
     * 会停在空选择器上。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void theHealthModeSwitchAndItsCommand(TestContext context) {
        ServerWorld world = context.getWorld();
        MinecraftServer server = world.getServer();
        RegistryWrapper.WrapperLookup registries = world.getRegistryManager();
        CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();
        ServerCommandSource source = server.getCommandSource();
        HealthModeComponent mode = HealthModeComponent.KEY.get(server.getScoreboard());
        boolean before = mode.isEnabled();
        try {
            check(failureOf(dispatcher, "sparkdlc:healthMode off", source) == null, "healthMode off runs");
            check(!mode.isEnabled() && !HealthModeComponent.isEnabled(world), "healthMode off turns the mode off");
            NbtCompound saved = new NbtCompound();
            mode.writeToNbt(saved, registries);
            check(saved.contains("Enabled") && !saved.getBoolean("Enabled"), "the switch is saved as Enabled: " + saved);
            check(failureOf(dispatcher, "sparkdlc:healthMode on", source) == null, "healthMode on runs");
            check(mode.isEnabled() && HealthModeComponent.isEnabled(world), "healthMode on turns the mode on");
            check(failureOf(dispatcher, "sparkdlc:healthMode", source) == null, "healthMode reports its state");
            mode.readFromNbt(saved, registries);
            check(!mode.isEnabled(), "a saved off switch loads as off");
            mode.readFromNbt(new NbtCompound(), registries);
            check(mode.isEnabled(), "a save without the key reads as on");
        } finally {
            mode.setEnabled(before);
        }

        for (String command : List.of(
                "sparkdlc:healthMode set @a head 0", "sparkdlc:healthMode set @a head 35",
                "sparkdlc:healthMode set @a chest 85", "sparkdlc:healthMode set @a abdomen 70",
                "sparkdlc:healthMode set @a left_arm 60", "sparkdlc:healthMode set @a right_arm 60",
                "sparkdlc:healthMode set @a left_leg 65", "sparkdlc:healthMode set @a right_leg 65",
                "sparkdlc:healthMode reset @a")) {
            CommandSyntaxException failure = failureOf(dispatcher, command, source);
            if (failure == null || failure.getType() != EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION) {
                throw new GameTestException("'" + command + "' should be valid and stop at the empty player selector,"
                        + " but: " + (failure == null ? "it ran" : failure.getMessage()));
            }
        }
        for (String command : List.of(
                "sparkdlc:healthMode set @a head 36", "sparkdlc:healthMode set @a chest 86",
                "sparkdlc:healthMode set @a left_arm 61", "sparkdlc:healthMode set @a head -1",
                "sparkdlc:healthMode set @a nose 1", "sparkdlc:healthMode set @a HEAD 1")) {
            CommandSyntaxException failure = failureOf(dispatcher, command, source);
            if (failure == null || failure.getType() == EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION) {
                throw new GameTestException("'" + command + "' names no part or a value outside the part's range and"
                        + " must be rejected before the selector");
            }
        }
        context.complete();
    }

    private static CommandSyntaxException failureOf(CommandDispatcher<ServerCommandSource> dispatcher, String command,
            ServerCommandSource source) {
        try {
            dispatcher.execute(command, source);
            return null;
        } catch (CommandSyntaxException failure) {
            return failure;
        }
    }

    private static void check(boolean condition, String what) {
        if (!condition) {
            throw new GameTestException(what);
        }
    }
}
