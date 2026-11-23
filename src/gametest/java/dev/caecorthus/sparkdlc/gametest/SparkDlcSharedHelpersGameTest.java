package dev.caecorthus.sparkdlc.gametest;

import dev.caecorthus.sparkdlc.mixin.compat.noellesroles.TaotiePlayerComponentAccessor;
import dev.caecorthus.sparkdlc.mixin.cooldown.ItemCooldownEntryAccessor;
import dev.caecorthus.sparkdlc.mixin.cooldown.ItemCooldownManagerAccessor;
import dev.caecorthus.sparkdlc.mixin.cooldown.ServerItemCooldownManagerAccessor;
import dev.caecorthus.sparkdlc.mixin.minecraft.PlayerManagerAccessor;
import dev.caecorthus.sparkdlc.mixin.wathe.GameWorldComponentAccessor;
import dev.caecorthus.sparkdlc.mixin.wathe.PlayerMoodComponentAccessor;
import dev.caecorthus.sparkdlc.role.civilian.engineer.EngineerStunGuards;
import dev.caecorthus.sparkdlc.role.civilian.engineer.EngineerStunnedPlayerComponent;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadDazeComponent;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadDazeGuards;
import dev.caecorthus.sparkdlc.util.RaycastShapeScope;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.index.WatheEntities;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerItemCooldownManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import org.agmas.noellesroles.taotie.TaotiePlayerComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * The helpers that were one copy per former mod and are one now, exercised on a real server against the real
 * Minecraft, Wathe and NoellesRoles classes: every member of every merged accessor resolves and reads or writes what it
 * names, the single ray wrapper scopes a real {@code RaycastContext#getBlockShape}, a Wathe body answers an armour list,
 * and the shared interaction lock decides the five vanilla interactions for the states that use it. (That the mixins
 * apply at all is {@link SparkDlcBootTest#everyCommonMixinApplies}; this checks what they do.)
 * 原先每个旧 mod 一份、现在只有一份的辅助类，在真实服务器上针对真实的 Minecraft、Wathe 与 NoellesRoles 类执行：合并后每个访问器的
 * 每个成员都能解析，并读写其指明的内容；唯一的射线包装为真实的 {@code RaycastContext#getBlockShape} 设置作用域；Wathe 尸体返回
 * 装备列表；共用的交互锁为使用它的状态裁决五种原版交互。（mixin 能否应用由 {@link SparkDlcBootTest#everyCommonMixinApplies}
 * 检查；这里检查它们做了什么。）
 */
public final class SparkDlcSharedHelpersGameTest implements FabricGameTest {
    /**
     * The one cooldown accessor pair serves every former reader and writer: entries and tick of the manager, both
     * bounds of an entry readable and (for the exact locks) writable, and the owner of a server manager.
     * 唯一的一对冷却访问器服务于所有原读取方与写入方：管理器的条目与 tick，条目的两个边界可读且（供精确锁）可写，以及服务端
     * 管理器的所属玩家。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void theCooldownAccessorsReadAndWriteVanillaCooldowns(TestContext context) {
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        ItemCooldownManager manager = player.getItemCooldownManager();
        manager.set(Items.STICK, 100);
        ItemCooldownManagerAccessor access = (ItemCooldownManagerAccessor) manager;
        Object raw = access.sparkdlc$getEntries().get(Items.STICK);
        if (!(raw instanceof ItemCooldownEntryAccessor entry)) {
            throw new GameTestException("the cooldown entry is not reachable through the accessor: " + raw);
        }
        int tick = access.sparkdlc$getTick();
        check(entry.sparkdlc$getStartTick() == tick, "an entry starts at the manager's tick");
        check(entry.sparkdlc$getEndTick() - entry.sparkdlc$getStartTick() == 100, "the entry spans the set duration");
        check(manager.getCooldownProgress(Items.STICK, 0.0F) == 1.0F, "a fresh cooldown is full");
        // The exact locks rewrite the bounds in place; vanilla reads the rewritten entry.
        // 精确锁原地改写边界；原版读取的是改写后的条目。
        entry.sparkdlc$setStartTick(tick - 60);
        check(Math.abs(manager.getCooldownProgress(Items.STICK, 0.0F) - 100.0F / 160.0F) < 1.0e-6F,
                "vanilla reads the rewritten start tick");
        entry.sparkdlc$setEndTick(tick);
        check(!manager.isCoolingDown(Items.STICK), "vanilla reads the rewritten end tick");

        ServerWorld world = context.getWorld();
        FakePlayer fake = FakePlayer.get(world);
        if (!(fake.getItemCooldownManager() instanceof ServerItemCooldownManager server)) {
            throw new GameTestException("a server player has a server cooldown manager");
        }
        check(((ServerItemCooldownManagerAccessor) server).sparkdlc$getPlayer() == fake,
                "the server manager names its owner");
        check(world.getServer().getPlayerManager() instanceof PlayerManagerAccessor, "the save invoker is applied");
        context.complete();
    }

    /**
     * The one accessor on Wathe's game component (it was four), the one on its mood component (two) and the one on
     * NoellesRoles' Taotie component (three): every member reads or writes the field it names.
     * Wathe 游戏组件上唯一的访问器（此前四个）、其心情组件上唯一的访问器（此前两个）以及 NoellesRoles 饕餮组件上唯一的访问器
     * （此前三个）：每个成员都读写其指明的字段。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void theComponentAccessorsReachTheFieldsTheyName(TestContext context) {
        ServerWorld world = context.getWorld();
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        GameWorldComponentAccessor access = (GameWorldComponentAccessor) game;
        check(access.sparkdlc$getWorld() == world, "the component names its world");
        HashSet<UUID> dead = access.sparkdlc$getDeadPlayers();
        UUID probe = UUID.randomUUID();
        check(dead != null && dead.add(probe) && access.sparkdlc$getDeadPlayers().remove(probe),
                "the dead set is the live, mutable one");
        access.sparkdlc$getTicksUntilNextResetAttempt();
        GameWorldComponent.GameStatus status = game.getGameStatus();
        access.sparkdlc$setGameStatusField(status);
        check(game.getGameStatus() == status, "the raw status write is the field getGameStatus reads");

        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        PlayerMoodComponentAccessor mood = (PlayerMoodComponentAccessor) (Object) PlayerMoodComponent.KEY.get(player);
        int timer = mood.sparkdlc$getNextTaskTimer();
        mood.sparkdlc$setNextTaskTimer(timer + 7);
        check(mood.sparkdlc$getNextTaskTimer() == timer + 7, "the task timer is read back as written");
        mood.sparkdlc$setNextTaskTimer(timer);

        int swallow = ((TaotiePlayerComponentAccessor) (Object) TaotiePlayerComponent.KEY.get(player))
                .sparkdlc$getCalculatedSwallowCooldown();
        check(swallow >= 0, "the swallow cooldown is readable: " + swallow);
        context.complete();
    }

    /**
     * One wrapper, one scope: {@link RaycastShapeScope#isRaycast()} is true exactly while a real
     * {@code RaycastContext#getBlockShape} runs, also when such queries nest, and false again afterwards. The probe is
     * a block whose outline asks the world for its block entity, so the question is asked from inside the vanilla call.
     * 一个包装、一个作用域：{@link RaycastShapeScope#isRaycast()} 恰好在真实的 {@code RaycastContext#getBlockShape} 执行期间为
     * true（这类查询嵌套时也是如此），之后恢复为 false。探针是一个轮廓形状会向世界询问方块实体的方块，因此这个问题是在原版调用
     * 内部提出的。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void theOneRayWrapperScopesARealBlockShapeQuery(TestContext context) {
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        RaycastContext ray = new RaycastContext(Vec3d.ZERO, new Vec3d(0.0, 0.0, 4.0), RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE, player);
        BlockState shulker = Blocks.SHULKER_BOX.getDefaultState();
        List<Boolean> seen = new ArrayList<>();
        BlockView inner = new ProbeView(() -> seen.add(RaycastShapeScope.isRaycast()));
        // A ray query made while another one runs (a door mixin that casts a ray of its own) keeps the scope open
        // after it returns. / 在另一条射线查询执行期间发起的查询（例如自己再投射射线的门 mixin）返回后，作用域仍保持打开。
        BlockView outer = new ProbeView(() -> {
            seen.add(RaycastShapeScope.isRaycast());
            ray.getBlockShape(shulker, inner, BlockPos.ORIGIN);
            seen.add(RaycastShapeScope.isRaycast());
        });
        check(!RaycastShapeScope.isRaycast(), "no ray is running before the query");
        ray.getBlockShape(shulker, outer, BlockPos.ORIGIN);
        check(seen.equals(List.of(true, true, true)), "inside the vanilla query, nested or not: " + seen);
        check(!RaycastShapeScope.isRaycast(), "the scope is left when the query returns");
        context.complete();
    }

    /**
     * A Wathe body answers an armour list, never {@code null} (one handler now; equipment scanners such as
     * LambDynamicLights iterate it).
     * Wathe 尸体返回装备列表而不是 {@code null}（现在只有一个处理器；LambDynamicLights 等装备扫描逻辑会遍历它）。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void aBodyAnswersAnArmourList(TestContext context) {
        PlayerBodyEntity body = WatheEntities.PLAYER_BODY.create(context.getWorld());
        if (body == null) {
            throw new GameTestException("no body entity");
        }
        Iterable<ItemStack> armour = body.getArmorItems();
        check(armour != null, "getArmorItems must not answer null");
        armour.forEach(stack -> { });
        context.complete();
    }

    /**
     * The shared interaction lock, through the listeners the Engineer stun and the Taotie daze register with it: a
     * locked player's item use, block use, entity use and attacks are refused before any other listener is asked, and
     * the one explicit difference holds (a stunned player cannot use a block bare-handed, a dazed one can: the guard
     * then answers exactly what it answers an unlocked player).
     * 共用的交互锁，经由工程师定身与饕餮眩晕向它注册的监听器检查：被锁定玩家的使用物品、使用方块、使用实体与攻击在询问其他
     * 监听器之前就被拒绝；唯一的显式差异成立（被定身者不能空手使用方块，被眩晕者可以：此时守卫给出的答案与未被锁定的玩家完全
     * 相同）。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void theSharedInteractionLockRefusesWhatALockedPlayerDoes(TestContext context) {
        EngineerStunGuards.register();
        TaotieHeadDazeGuards.register();
        ServerWorld world = context.getWorld();
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        BlockHitResult hit = new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false);
        ActionResult bareHandBlockUseWhenFree = UseBlockCallback.EVENT.invoker().interact(player, world, Hand.MAIN_HAND, hit);

        EngineerStunnedPlayerComponent stun = EngineerStunnedPlayerComponent.KEY.get(player);
        stun.stun(40);
        checkEveryInteractionRefused(player, world, hit, "Engineer stun");
        check(UseBlockCallback.EVENT.invoker().interact(player, world, Hand.MAIN_HAND, hit) == ActionResult.FAIL,
                "a stunned player cannot use a block bare-handed");
        stun.clear();

        TaotieHeadDazeComponent daze = TaotieHeadDazeComponent.KEY.get(player);
        daze.apply(40);
        check(UseBlockCallback.EVENT.invoker().interact(player, world, Hand.MAIN_HAND, hit) == bareHandBlockUseWhenFree,
                "a dazed player still uses blocks bare-handed, like a free one");
        player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.STICK));
        checkEveryInteractionRefused(player, world, hit, "Taotie daze");
        daze.clear();
        player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        context.complete();
    }

    private static void checkEveryInteractionRefused(PlayerEntity player, ServerWorld world, BlockHitResult hit,
                                                     String state) {
        check(UseItemCallback.EVENT.invoker().interact(player, world, Hand.MAIN_HAND).getResult() == ActionResult.FAIL,
                state + ": item use");
        if (!player.getMainHandStack().isEmpty()) {
            check(UseBlockCallback.EVENT.invoker().interact(player, world, Hand.MAIN_HAND, hit) == ActionResult.FAIL,
                    state + ": block use with an item in hand");
        }
        check(UseEntityCallback.EVENT.invoker().interact(player, world, Hand.MAIN_HAND, player, null)
                == ActionResult.FAIL, state + ": entity use");
        check(AttackEntityCallback.EVENT.invoker().interact(player, world, Hand.MAIN_HAND, player, null)
                == ActionResult.FAIL, state + ": entity attack");
        check(AttackBlockCallback.EVENT.invoker().interact(player, world, Hand.MAIN_HAND, BlockPos.ORIGIN, Direction.UP)
                == ActionResult.FAIL, state + ": block attack");
    }

    private static void check(boolean condition, String what) {
        if (!condition) {
            throw new GameTestException(what);
        }
    }

    /**
     * A block view that only reports when a shape asks it for a block entity. / 只在形状向它询问方块实体时上报的方块视图。
     */
    private record ProbeView(Runnable asked) implements BlockView {
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            asked.run();
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return Blocks.AIR.getDefaultState();
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return Fluids.EMPTY.getDefaultState();
        }

        @Override
        public int getHeight() {
            return 384;
        }

        @Override
        public int getBottomY() {
            return -64;
        }
    }
}
