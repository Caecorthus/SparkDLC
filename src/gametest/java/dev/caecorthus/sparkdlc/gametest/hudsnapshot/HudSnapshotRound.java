package dev.caecorthus.sparkdlc.gametest.hudsnapshot;

import dev.caecorthus.sparkdlc.health.BodyHealthComponent;
import dev.caecorthus.sparkdlc.health.BodyPart;
import dev.caecorthus.sparkdlc.health.HealthModeComponent;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.api.WatheMapEffects;
import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.cca.TrainWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/**
 * Server half of {@code ./gradlew runHudSnapshot}: puts the one player of the integrated server into a running murder
 * round without a map, by writing Wathe's own server-side state, which then reaches the client through Wathe's and
 * Cardinal Components' normal sync. Every method runs on the server thread.
 *
 * <p>What a real round start would do and this does not: no teleport to a map position, no train reset, no role
 * assignment, no items, no welcome packet. The player is only stood on the harness's own little stage. What would
 * end the fake round at once, and how it is kept from doing so, all inside this setup:</p>
 * <ul>
 * <li>Win checks ({@code MurderGameMode.tickServerGameLoop}: one civilian alone is a passenger win, one killer alone a
 * killer win, and SparkDLC's own mixins sit in the same method). The world's game mode is {@link #MAPLESS_MURDER}, an
 * unregistered game mode with the id {@code wathe:murder} and an empty server loop. The component syncs the id only,
 * so the client resolves it to Wathe's real murder game mode, which is what its HUD asks for.</li>
 * <li>"Fell out of the train" ({@code GameWorldComponent.serverTick} kills below the play area, whose default floor
 * is y 118): the play area is set to a box around the stage that reaches below the world.</li>
 * <li>Time-of-day forcing ({@code TrainWorldComponent.serverTick} for the Harpy Express map effects): the map effect
 * is the generic one.</li>
 * <li>Mood drain, new tasks and task completion ({@code PlayerMoodComponent.serverTick}): {@link #pin} rewrites the
 * mood before every tick so that the tick's own drain lands on the case's value, keeps the next-task timer far away,
 * and the tasks are ones that cannot complete here. A mood at or below -1 would be a mental-breakdown death; cases
 * stay above it.</li>
 * <li>A player without a role in a running round is made a spectator by {@code GameWorldComponent.serverTick}: the
 * round is switched on and the first role is given in the same server task.</li>
 * </ul>
 *
 * {@code ./gradlew runHudSnapshot} 的服务端部分：在没有地图的情况下，把内置服务器上唯一的玩家置入一局进行中的谋杀对局，做法是写入
 * Wathe 自己的服务端状态，再由 Wathe 与 Cardinal Components 的正常同步到达客户端。所有方法都在服务器线程上运行。
 *
 * <p>真实开局会做而这里不做的事：不传送到地图位置、不重置列车、不分配身份、不发物品、不发欢迎包。玩家只是被放到本工具
 * 自己搭的小台子上。会让假对局立刻结束的因素，以及在本布置内部如何阻止它们：</p>
 * <ul>
 * <li>胜负判定（{@code MurderGameMode.tickServerGameLoop}：只有一名平民即乘客胜利，只有一名杀手即杀手胜利，且 SparkDLC 自己的
 * mixin 也在同一方法里）。世界的游戏模式被设为 {@link #MAPLESS_MURDER}：一个未注册、id 为 {@code wathe:murder}、服务端循环为空的
 * 游戏模式。该组件只同步 id，因此客户端把它解析为 Wathe 真正的谋杀模式，而这正是其 HUD 所要求的。</li>
 * <li>“跌出列车”（{@code GameWorldComponent.serverTick} 会杀死低于游玩区域的玩家，其默认底面为 y 118）：游玩区域被设为围绕台子、
 * 一直延伸到世界底部以下的盒子。</li>
 * <li>强制昼夜时间（{@code TrainWorldComponent.serverTick} 对 Harpy Express 地图效果生效）：地图效果设为通用效果。</li>
 * <li>心情流失、新任务与任务完成（{@code PlayerMoodComponent.serverTick}）：{@link #pin} 在每个 tick 之前重写心情，使该 tick 自身的
 * 流失恰好落在用例的数值上，并让下一个任务的计时器保持在很远的未来；所用任务在这里无法完成。心情到达或低于 -1 会触发精神崩溃
 * 死亡；各用例都保持在其之上。</li>
 * <li>进行中的对局里没有身份的玩家会被 {@code GameWorldComponent.serverTick} 设为旁观者：因此在同一个服务器任务里开启对局并赋予
 * 第一个身份。</li>
 * </ul>
 */
final class HudSnapshotRound {
    /** Where the player stands: high in the void, far from anything generated. / 玩家站立处：虚空高处，远离任何生成物。 */
    static final BlockPos STAGE = new BlockPos(0, 100, 0);

    /** Noon, with the daylight cycle off (see the world's game rules). / 正午，昼夜循环已关闭（见世界的游戏规则）。 */
    private static final long NOON = 6000L;
    /** Long enough that no task timer and no weather change comes due. / 足够长，任务计时器与天气变化都不会到期。 */
    private static final int FAR_AWAY = Integer.MAX_VALUE / 2;
    /**
     * Ten minutes on the round clock: a plausible reading for the roles that are shown it, and far from running out.
     * 回合时钟上的十分钟：对能看到时钟的身份而言是合理的读数，且远不会耗尽。
     */
    private static final int ROUND_TICKS = 10 * 60 * 20;
    /**
     * How far above a positive case mood the synced value is held. Wathe's client tick drains its own copy between
     * two syncs, so what the HUD reads is always a drain or two below the server's value; with this headroom a case
     * mood of 0.87 is read as 0.871 to 0.872 and shows as 87% whether the HUD rounds or truncates. A full mood cannot
     * get it (Wathe clamps at 1), so with a task the client reads just under 1, as it does in a real round.
     * 同步值比正的用例心情高出多少。Wathe 的客户端 tick 会在两次同步之间自行扣减它的副本，因此 HUD 读到的值总比服务端低一两次流失量；
     * 有了这点余量，0.87 的用例心情会被读成 0.871 到 0.872，无论 HUD 四舍五入还是截断都显示 87%。满心情无法加余量（Wathe 限制在 1
     * 以内），因此带任务时客户端读到的值略低于 1，与真实对局一致。
     */
    private static final float MOOD_HEADROOM = 0.002f;

    /**
     * Stands in for Wathe's murder game mode on the server only: same id, nothing in the loop. It is never
     * registered, so {@code WatheGameModes.GAME_MODES} still maps the id to the real one on both sides.
     * 仅在服务端替代 Wathe 的谋杀模式：id 相同，循环为空。它从不注册，因此两端的 {@code WatheGameModes.GAME_MODES} 仍把该 id
     * 映射到真正的模式。
     */
    private static final GameMode MAPLESS_MURDER = new GameMode(WatheGameModes.MURDER_ID,
            WatheGameModes.MURDER.defaultStartTime, 1) {
        @Override
        public void tickServerGameLoop(ServerWorld world, GameWorldComponent game) {
        }

        @Override
        public void initializeGame(ServerWorld world, GameWorldComponent game, List<ServerPlayerEntity> players) {
        }
    };

    /*
     * Wathe keeps these two private and offers no setter that fits: setMood clamps to 1 (the pin needs one tick of
     * drain above the target) and nothing sets the task timer.
     * Wathe 把这两个字段设为私有，且没有合用的 setter：setMood 会限制在 1 以内（pin 需要比目标值高出一个 tick 的流失量），
     * 也没有任何方法能设置任务计时器。
     */
    private static final Field MOOD = privateField("mood");
    private static final Field NEXT_TASK_TIMER = privateField("nextTaskTimer");

    /** The case {@link #pin} holds in place; written and read on the server thread. / {@link #pin} 维持的用例；仅在服务器线程读写。 */
    private static HudSnapshotCase active;
    /** What {@link #pinMood} stored before the running tick, or NaN. / {@link #pinMood} 在当前 tick 之前写入的值；没有则为 NaN。 */
    private static float pinnedMood = Float.NaN;
    /** The mood one tick takes away in the active case, as last measured. / 当前用例中一个 tick 扣除的心情，取最近一次实测值。 */
    private static float drainPerTick;
    /** Why the fake round is no longer what it should be, for the client's report. / 假对局偏离预期的原因，供客户端报告。 */
    private static volatile String trouble;

    private HudSnapshotRound() {
    }

    static String trouble() {
        return trouble;
    }

    /**
     * Once per run, well before {@link #begin}: the stage. It is the harness's own and has nothing to do with a Wathe
     * map: an invisible floor at {@link #STAGE} and, unless the backdrop is the open sky, a plain wall that fills the
     * view to the north. The player is still far away (Wathe has moved the roleless newcomer into its default play
     * area), so the light engine finishes before any client is sent these chunks.
     * 每次运行一次，且远早于 {@link #begin}：搭台子。台子是本工具自己的，与 Wathe 地图无关：{@link #STAGE} 处一块不可见的地板，以及
     * （背景不是露天天空时）一面填满北向视野的纯色墙。此时玩家还在远处（Wathe 已把这个没有身份的新玩家移进它的默认游玩区域），
     * 因此光照引擎会在任何客户端收到这些区块之前完成计算。
     */
    static void buildStage(ServerWorld world, HudSnapshotBackdrop backdrop) {
        world.setTimeOfDay(NOON);
        world.setWeather(FAR_AWAY, 0, false, false);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.setBlockState(STAGE.add(dx, -1, dz), Blocks.BARRIER.getDefaultState());
            }
        }
        if (backdrop.wall() != null) {
            // 3.5 blocks in front of the eyes and wider than any field of view. / 位于眼前 3.5 格，比任何视野都宽。
            for (int dx = -10; dx <= 10; dx++) {
                for (int dy = -8; dy <= 11; dy++) {
                    world.setBlockState(STAGE.add(dx, dy, -4), backdrop.wall().getDefaultState());
                }
            }
        }
    }

    /**
     * Once per run: the player onto the stage in adventure mode (Wathe made it a spectator on arrival), then the
     * round and the first case. Returns a line for the log.
     * 每次运行一次：把玩家以冒险模式放到台子上（Wathe 在其加入时已将其设为旁观者），然后是对局与第一个用例。返回一行日志。
     */
    static String begin(ServerPlayerEntity player, HudSnapshotBackdrop backdrop, HudSnapshotCase first, float yaw,
            float pitch) {
        ServerWorld world = player.getServerWorld();
        player.changeGameMode(net.minecraft.world.GameMode.ADVENTURE);
        player.teleport(world, STAGE.getX() + 0.5, STAGE.getY(), STAGE.getZ() + 0.5, yaw, pitch);

        MapVariablesWorldComponent.KEY.get(world).setPlayArea(new Box(
                STAGE.getX() - 256.0, world.getBottomY() - 256.0, STAGE.getZ() - 256.0,
                STAGE.getX() + 256.0, world.getTopY() + 256.0, STAGE.getZ() + 256.0));

        TrainWorldComponent train = TrainWorldComponent.KEY.get(world);
        train.setSpeed(0);
        train.setSnow(false);
        train.setFog(false);
        train.setTimeOfDay(TrainWorldComponent.TimeOfDay.DAY);
        train.setHud(true);

        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        game.clearRoleMap();
        game.setMapEffect(WatheMapEffects.GENERIC);
        game.setGameMode(MAPLESS_MURDER);
        game.setFade(0);
        int airOnArrival = player.getAir();
        apply(player, first);
        game.setGameStatus(GameWorldComponent.GameStatus.ACTIVE);
        return "round faked in " + world.getRegistryKey().getValue() + " at " + player.getBlockPos() + ", backdrop "
                + backdrop.id() + " (air on arrival " + airOnArrival + "/" + player.getMaxAir() + ")";
    }

    /**
     * Puts the player into one case, without its tasks: those follow one at a time through {@link #addTask}, so that
     * their rows stand in the case's order (see {@code HudSnapshotClient}). Order matters here too: the role first,
     * because Wathe's mood tick asks for it.
     * 把玩家置入一个用例，但不含任务：任务随后经由 {@link #addTask} 逐个给出，使各行按用例的顺序出现（见
     * {@code HudSnapshotClient}）。这里的顺序同样有讲究：先设身份，因为 Wathe 的心情 tick 会查询它。
     */
    static void apply(ServerPlayerEntity player, HudSnapshotCase snapshotCase) {
        ServerWorld world = player.getServerWorld();
        MinecraftServer server = world.getServer();
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        game.getRoles().clear();
        game.addRole(player, snapshotCase.role());
        game.setPsychosActive(snapshotCase.psycho() ? 1 : 0);
        game.sync();
        // Every picture reads about the same on the round clock. / 每张图片上回合时钟的读数大致相同。
        GameTimeComponent.KEY.get(world).setTime(ROUND_TICKS);
        // Wathe draws its air meter whenever the air is below the maximum, and a round kills at zero.
        // 只要空气值低于上限，Wathe 就会绘制氧气条；对局中空气值归零即死亡。
        player.setAir(player.getMaxAir());

        HealthModeComponent.KEY.get(server.getScoreboard()).setEnabled(snapshotCase.healthMode());
        BodyHealthComponent body = BodyHealthComponent.KEY.get(player);
        for (BodyPart part : BodyPart.values()) {
            body.setHealth(part, snapshotCase.health(part));
        }

        PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
        mood.tasks.clear();
        active = snapshotCase;
        holdMoodFromScratch(mood, snapshotCase);

        // Only the two synced values, not startPsycho(): that would hand out a bat and fire the psycho events.
        // 只写两个被同步的数值，不调用 startPsycho()：后者会发放球棒并触发狂暴事件。
        PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(player);
        psycho.psychoTicks = snapshotCase.psycho() ? GameConstants.PSYCHO_TIMER : 0;
        psycho.armour = 0;
        psycho.sync();
    }

    /** Gives the player one more task of the active case. / 给玩家添加当前用例的又一个任务。 */
    static void addTask(ServerPlayerEntity player, PlayerMoodComponent.Task task) {
        if (active == null) {
            throw new IllegalStateException("no active case to add " + task + " to");
        }
        PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
        mood.tasks.put(task, neverFulfilled(task));
        holdMoodFromScratch(mood, active);
    }

    /**
     * After the task list changed: the drain is no longer the measured one, so the pin starts from Wathe's own figure
     * again, and the new list is synced at once (a FAKE role's mood tick has nothing dirty to sync).
     * 任务列表变化之后：流失量不再是之前测得的值，因此 pin 重新从 Wathe 自身的数值出发，并立即同步新的列表（FAKE 身份的心情 tick
     * 没有任何脏数据可同步）。
     */
    private static void holdMoodFromScratch(PlayerMoodComponent mood, HudSnapshotCase snapshotCase) {
        pinnedMood = Float.NaN;
        drainPerTick = mood.tasks.size() * GameConstants.MOOD_DRAIN;
        pinMood(mood, snapshotCase);
        mood.sync();
    }

    /**
     * End of every server tick: holds the active case against Wathe's own tick, and notes when the round is no longer
     * running for the player.
     * 每个服务器 tick 结束时：让当前用例顶住 Wathe 自身的 tick，并在对局对该玩家不再处于进行状态时记录下来。
     */
    static void pin(MinecraftServer server) {
        HudSnapshotCase snapshotCase = active;
        if (snapshotCase == null || server.getPlayerManager().getPlayerList().isEmpty()) {
            return;
        }
        ServerPlayerEntity player = server.getPlayerManager().getPlayerList().get(0);
        pinMood(PlayerMoodComponent.KEY.get(player), snapshotCase);
        if (trouble == null && (!GameFunctions.isPlayerPlayingAndAlive(player) || player.isSpectator())) {
            GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
            trouble = "the fake round stopped on the server during '" + snapshotCase.name() + "': status "
                    + game.getGameStatus() + ", has role " + game.hasAnyRole(player) + ", dead "
                    + game.isPlayerDead(player.getUuid()) + ", player game mode "
                    + player.interactionManager.getGameMode() + ", y " + player.getY();
        }
    }

    /**
     * Before a tick: a REAL role with tasks loses mood in the tick, so the stored value is that much above the case's
     * value and the tick's own sync sends the case's value. The loss is measured from the previous tick rather than
     * computed as Wathe's {@code tasks * MOOD_DRAIN}: SparkDLC and NoellesRoles features scale that drain.
     * 在 tick 之前：有任务的 REAL 身份会在该 tick 内损失心情，因此存储值比用例的数值高出这么多，该 tick 自身的同步发出的就是用例的
     * 数值。损失量取自上一个 tick 的实测，而不是按 Wathe 的 {@code 任务数 * MOOD_DRAIN} 计算：SparkDLC 与 NoellesRoles 的功能会
     * 缩放这项流失。
     */
    private static void pinMood(PlayerMoodComponent mood, HudSnapshotCase snapshotCase) {
        try {
            NEXT_TASK_TIMER.setInt(mood, FAR_AWAY);
            if (!snapshotCase.realMood()) {
                MOOD.setFloat(mood, 1.0f);
                return;
            }
            if (!Float.isNaN(pinnedMood)) {
                float drained = pinnedMood - MOOD.getFloat(mood);
                if (drained >= 0.0f && drained < 0.01f) {
                    drainPerTick = drained;
                }
            }
            float target = heldMood(snapshotCase);
            pinnedMood = target + drainPerTick;
            MOOD.setFloat(mood, pinnedMood);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /** The mood the server syncs for the case, see {@link #MOOD_HEADROOM}. / 服务端为该用例同步的心情，见 {@link #MOOD_HEADROOM}。 */
    static float heldMood(HudSnapshotCase snapshotCase) {
        if (!snapshotCase.realMood()) {
            return 1.0f;
        }
        float mood = snapshotCase.mood();
        return mood > 0.0f ? Math.min(1.0f, mood + MOOD_HEADROOM) : mood;
    }

    /** A task of the given kind that this world cannot complete. / 该类型中在这个世界里无法完成的任务。 */
    private static PlayerMoodComponent.TrainTask neverFulfilled(PlayerMoodComponent.Task task) {
        return switch (task) {
            case SLEEP -> new PlayerMoodComponent.SleepTask(FAR_AWAY);
            // Its timer runs under the open sky, one per tick. / 露天时它的计时器每 tick 减一。
            case OUTSIDE -> new PlayerMoodComponent.OutsideTask(FAR_AWAY);
            case EAT -> new PlayerMoodComponent.EatTask();
            case DRINK -> new PlayerMoodComponent.DrinkTask();
        };
    }

    private static Field privateField(String name) {
        try {
            Field field = PlayerMoodComponent.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Wathe's PlayerMoodComponent has no field '" + name + "'", exception);
        }
    }
}
