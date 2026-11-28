package dev.caecorthus.sparkdlc.gametest.hudsnapshot;

import dev.caecorthus.sparkdlc.health.BodyHealthComponent;
import dev.caecorthus.sparkdlc.health.BodyPart;
import dev.caecorthus.sparkdlc.health.HealthModeComponent;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.MoodRenderer;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.FlatLevelGeneratorPresets;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.level.LevelInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client half of {@code ./gradlew runHudSnapshot}, a dev-only way to look at the top-left in-round HUD (body health
 * figure, Wathe's mood flag and task list) in the real game. That HUD only draws for a player who is playing and
 * alive in a murder round on a SparkDLC server, and no dev save has a Wathe map, so this entrypoint does the whole
 * trip without input: wait for the title screen, create a throwaway void world, let {@link HudSnapshotRound} fake the
 * round on the integrated server, and for each {@link HudSnapshotCase} wait until this client has really received the
 * state, give the HUD's animations time to settle, and save the pictures ({@link HudSnapshotImages}). Then it closes
 * the game: exit code 0 when every case was saved, 1 with a {@code FAILED} log line otherwise, 2 from the watchdog
 * when the client hangs. A plain client entrypoint like {@code SparkDlcClientAudit}, and just as inert: it does
 * nothing unless the run sets {@code -Dsparkdlc.hudSnapshot=true}.
 *
 * <p>The readiness check reads Wathe's and the health feature's public state only, never the HUD classes: those are
 * what the pictures are for, and they may be redrawn without this file noticing.</p>
 *
 * {@code ./gradlew runHudSnapshot} 的客户端部分：仅供开发使用，用来在真实游戏里查看对局左上角的 HUD（身体健康示意图、Wathe 的心情
 * 旗帜与任务清单）。该 HUD 只在 SparkDLC 服务器的谋杀对局中、对参与且存活的玩家绘制，而开发环境没有带 Wathe 地图的存档，因此这个
 * 入口在无人操作的情况下走完全程：等待标题界面，创建一次性虚空世界，由 {@link HudSnapshotRound} 在内置服务器上伪造对局，然后对每个
 * {@link HudSnapshotCase} 等到本客户端确实收到该状态、给 HUD 动画留出稳定时间，再保存图片（{@link HudSnapshotImages}）。之后它自行
 * 关闭游戏：全部用例都已保存时退出码为 0，否则为 1 并带有一行 {@code FAILED} 日志；客户端卡死时由看门狗以 2 退出。它与
 * {@code SparkDlcClientAudit} 一样是普通的客户端入口，也同样静默：只有运行配置设置了 {@code -Dsparkdlc.hudSnapshot=true} 时才会动作。
 *
 * <p>就绪检查只读取 Wathe 与健康功能的公开状态，从不读取 HUD 类：图片正是用来看它们的，它们重画时本文件不应受影响。</p>
 */
public final class HudSnapshotClient implements ClientModInitializer {
    /** {@code -PhudSnapshotCases=a,b}: a subset of {@link HudSnapshotCase#ALL}. / {@link HudSnapshotCase#ALL} 的子集。 */
    static final String CASES_PROPERTY = "sparkdlc.hudSnapshot.cases";

    private static final Logger LOGGER = LoggerFactory.getLogger("sparkdlc_gametest");
    private static final String TAG = "[SparkDLC HUD snapshot] ";
    /** What the Gradle run asks the window for; a different size only earns a warning. / Gradle 运行配置要求的窗口尺寸；不同时仅警告。 */
    private static final int WINDOW_WIDTH = 1280, WINDOW_HEIGHT = 720;
    /**
     * North and level, straight at the backdrop wall (or at the horizon of the open sky). The camera is put back
     * every tick, so a mouse over the window cannot turn it.
     * 朝北平视，正对背景墙（或露天天空的地平线）。每个 tick 都会把镜头摆回原位，因此鼠标划过窗口也无法转动它。
     */
    private static final float CAMERA_YAW = 180.0f, CAMERA_PITCH = 0.0f;

    private static final long TITLE_PATIENCE_MS = 5_000;
    private static final long JOIN_TIMEOUT_MS = 30_000;
    /**
     * Between building the stage and moving the player onto it. The chunk packet a client gets on arrival carries
     * the light as it is at that moment; sent right after the blocks were placed, the wall arrived unlit (sky light
     * 0 on the client, 15 on the server) and stayed black.
     * 搭好台子到把玩家移上去之间的间隔。客户端到达时收到的区块包携带的是当时的光照；方块刚放下就发送的话，墙到达时没有光照
     * （客户端天空光为 0，服务端为 15），并且一直是黑的。
     */
    private static final long STAGE_LIGHTING_MS = 1_500;
    /** The stage's chunks get drawn before the first case. / 第一个用例之前，先让台子所在的区块画出来。 */
    private static final long WARMUP_MS = 1_500;
    private static final long WARMUP_TIMEOUT_MS = 15_000;
    private static final long CASE_TIMEOUT_MS = 15_000;
    /**
     * How many frames the synced state must have been drawn for before the picture. Wathe eases its mood value, flag
     * alpha, threshold arrow and row offsets once per frame, by the frame's partial tick over 8 to 32: a row offset
     * closes about 1/64 of its gap per frame whatever the frame rate, so a third row needs some 400 frames to come
     * within a twentieth of a GUI pixel, and the arrow that marks a crossed mood threshold needs about 270 to fade.
     * The run directory's options lift the frame limit, which makes this a second or so; at 60 frames a second it
     * would be ten.
     * 截图前同步状态必须已经被绘制多少帧。Wathe 每帧缓动一次心情数值、旗帜透明度、阈值箭头与各行偏移，系数为该帧的 partial tick 除以
     * 8 到 32：无论帧率多少，行偏移每帧大约只缩小差距的 1/64，因此第三行需要约 400 帧才能进入二十分之一个 GUI 像素以内，标记心情
     * 越过阈值的箭头需要约 270 帧才能淡出。运行目录的选项解除了帧率上限，于是这大约只需一秒；在每秒 60 帧下则需要十秒。
     */
    private static final int SETTLE_FRAMES = 600;
    /**
     * A case starts without tasks and gets them one at a time, last one first, each after the client has drawn the
     * ones before. Wathe gives a task its row when the task first shows up in a frame and puts that row on top; rows
     * that show up in the same frame are ordered by the identity hash of the task kinds, which differs from one JVM
     * to the next, and the rows of the case before keep their place for the 80 or so frames they take to fade.
     * Waiting these frames makes the rows of every picture stand, top to bottom, in the order the case lists them, on
     * every run and for every subset of cases.
     * 用例开始时没有任务，之后从最后一个开始逐个添加，每个都在客户端画过之前的任务之后才给。Wathe 在某个任务首次出现在一帧里时为它
     * 分配行，并把该行放在最上面；同一帧里出现的多行按任务种类的 identity hash 排序，而它在每个 JVM 里都不同；上一个用例的行在淡出
     * 所需的约 80 帧里也仍占着位置。等过这些帧之后，每张图片的各行自上而下都按用例列出的顺序排列，每次运行、任意用例子集都是如此。
     */
    private static final int OLD_ROWS_FRAMES = 200, NEXT_ROW_FRAMES = 8;
    /** At least this long as well, so a tick-driven change is not missed. / 同时至少等待这么久，以免漏掉由 tick 驱动的变化。 */
    private static final long SETTLE_MS = 750;
    /** Extra frames for {@link MoodRenderer#moodRender} before shooting anyway. / 仍未稳定时额外等待的帧数，之后照常截图。 */
    private static final int EASING_GRACE_FRAMES = 600;
    /**
     * How far below the server's held mood this client may read: its own tick drains its copy between two syncs
     * (three tasks take 0.0006 a tick, and other features can scale that).
     * 本客户端读到的心情可以比服务端维持的值低多少：它自己的 tick 会在两次同步之间扣减副本（三个任务每 tick 扣 0.0006，
     * 其他功能还可能缩放这一数值）。
     */
    private static final float MOOD_SYNC_TOLERANCE = 0.003f;
    private static final float MOOD_EASING_TOLERANCE = 0.001f;
    /** A hung client is killed; it must never stay on the owner's screen. / 卡死的客户端会被杀掉；它绝不能留在屏幕上。 */
    private static final long WATCHDOG_SECONDS = 300;

    private enum Phase { TITLE, JOINING, STAGE, WARMUP, CASES, DONE }

    private volatile Phase phase = Phase.TITLE;
    private long phaseStartedAt = System.nanoTime();
    private List<HudSnapshotCase> cases = List.of();
    private HudSnapshotBackdrop backdrop = HudSnapshotBackdrop.MID;
    private int caseIndex;
    private long caseStartedAt;
    /**
     * Since when the client has shown everything it was given of the case so far, or -1.
     * 客户端从何时起呈现了该用例目前已给出的全部内容；尚未呈现则为 -1。
     */
    private long readySince = -1;
    /** {@link #frames} at {@link #readySince}. / {@link #readySince} 时刻的 {@link #frames}。 */
    private long readySinceFrame;
    /** World frames rendered so far; render thread only. / 迄今渲染的世界帧数；仅渲染线程访问。 */
    private long frames;
    /** How many of the case's tasks the server has been asked to add. / 已请求服务端添加的用例任务数量。 */
    private int tasksGiven;
    private boolean closedAScreen;
    private final List<String> saved = new ArrayList<>();
    private final List<String> failures = new ArrayList<>();
    /** Set on the server thread when a server-side step threw. / 服务端步骤抛出异常时由服务器线程写入。 */
    private volatile String serverFailure;
    /** Read by the shutdown hook. / 由关闭钩子读取。 */
    private volatile boolean failed;

    @Override
    public void onInitializeClient() {
        if (!HudSnapshotRunDirectory.enabled()) {
            return;
        }
        try {
            cases = HudSnapshotCase.select(System.getProperty(CASES_PROPERTY));
            backdrop = HudSnapshotBackdrop.select(System.getProperty(HudSnapshotBackdrop.PROPERTY));
        } catch (IllegalArgumentException exception) {
            failures.add(exception.getMessage());
        }
        startWatchdog();
        // Vanilla's own stop always exits with 0. A failed run stops the same clean way, and this hook, which runs
        // inside that exit, turns the code into 1 after the other hooks have had time to flush the log.
        // 原版自身的停止流程总是以 0 退出。失败的运行以同样干净的方式停止，由这个在退出过程中运行的钩子把退出码改为 1；
        // 它会先等其他钩子把日志写完。
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (failed) {
                try {
                    Thread.sleep(750);
                } catch (InterruptedException ignored) {
                    // Exit with the failure code right away. / 立即以失败码退出。
                }
                Runtime.getRuntime().halt(1);
            }
        }, "SparkDLC HUD snapshot exit code"));
        // The integrated server runs in this JVM, so its tick event is registered from here as well.
        // 内置服务器运行在同一个 JVM 里，因此它的 tick 事件也从这里注册。
        ServerTickEvents.END_SERVER_TICK.register(HudSnapshotRound::pin);
        WorldRenderEvents.END.register(context -> frames++);
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient client) {
        if (phase == Phase.DONE) {
            return;
        }
        try {
            step(client);
        } catch (Throwable throwable) {
            LOGGER.error(TAG + "unexpected failure in phase {}", phase, throwable);
            failures.add("unexpected failure in phase " + phase + ": " + throwable);
            finish(client);
        }
    }

    private void step(MinecraftClient client) throws Exception {
        switch (phase) {
            case TITLE -> {
                // The loading overlay stays up until the first resource reload is over. / 首次资源重载结束前加载遮罩一直存在。
                if (client.getOverlay() != null) {
                    phaseStartedAt = System.nanoTime();
                    return;
                }
                if (!failures.isEmpty()) {
                    finish(client);
                    return;
                }
                if (!(client.currentScreen instanceof TitleScreen) && phaseMillis() < TITLE_PATIENCE_MS) {
                    return;
                }
                enter(Phase.JOINING);
                createWorld(client);
            }
            case JOINING -> {
                String missing = notJoined(client);
                if (missing == null) {
                    reportWindow(client);
                    enter(Phase.STAGE);
                    onServer(client, player -> HudSnapshotRound.buildStage(player.getServerWorld(), backdrop));
                } else if (phaseMillis() > JOIN_TIMEOUT_MS) {
                    failures.add("the throwaway world was not joined: " + missing);
                    finish(client);
                }
            }
            case STAGE -> {
                if (serverFailure != null) {
                    failures.add(serverFailure);
                    finish(client);
                } else if (phaseMillis() >= STAGE_LIGHTING_MS) {
                    enter(Phase.WARMUP);
                    HudSnapshotCase first = cases.get(0);
                    onServer(client, player -> LOGGER.info(TAG + "{}",
                            HudSnapshotRound.begin(player, backdrop, first, CAMERA_YAW, CAMERA_PITCH)));
                }
            }
            case WARMUP -> {
                holdCamera(client);
                if (serverFailure != null) {
                    failures.add(serverFailure);
                    finish(client);
                } else if (phaseMillis() >= WARMUP_MS && onStage(client)) {
                    enter(Phase.CASES);
                    caseIndex = 0;
                    startCase(client);
                } else if (phaseMillis() > WARMUP_TIMEOUT_MS) {
                    failures.add("the player never arrived on the stage at " + HudSnapshotRound.STAGE + ": it is at "
                            + (client.player == null ? null : client.player.getBlockPos()));
                    finish(client);
                }
            }
            case CASES -> caseTick(client);
            case DONE -> {
            }
        }
    }

    private void enter(Phase next) {
        phase = next;
        phaseStartedAt = System.nanoTime();
    }

    private long phaseMillis() {
        return millisSince(phaseStartedAt);
    }

    private static long millisSince(long nanoTime) {
        return (System.nanoTime() - nanoTime) / 1_000_000L;
    }

    /**
     * A void superflat world: sky everywhere, no mobs, no structures, noon for ever. Created through the same call
     * the title screen's demo button uses, so no screen is clicked.
     * 虚空超平坦世界：到处都是天空，没有生物、没有结构，永远是正午。通过标题界面试玩按钮所用的同一个调用创建，因此不需要点击任何界面。
     */
    private static void createWorld(MinecraftClient client) {
        GameRules rules = new GameRules();
        rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
        rules.get(GameRules.DO_WEATHER_CYCLE).set(false, null);
        rules.get(GameRules.DO_MOB_SPAWNING).set(false, null);
        LevelInfo level = new LevelInfo(HudSnapshotRunDirectory.WORLD_NAME, GameMode.ADVENTURE, false,
                Difficulty.PEACEFUL, true, rules, DataConfiguration.SAFE_MODE);
        LOGGER.info(TAG + "creating the throwaway world '{}'", HudSnapshotRunDirectory.WORLD_NAME);
        client.createIntegratedServerLoader().createAndStart(HudSnapshotRunDirectory.WORLD_NAME, level,
                new GeneratorOptions(0L, false, false), HudSnapshotClient::voidDimensions, client.currentScreen);
    }

    private static DimensionOptionsRegistryHolder voidDimensions(DynamicRegistryManager registries) {
        FlatChunkGeneratorConfig theVoid = registries.get(RegistryKeys.FLAT_LEVEL_GENERATOR_PRESET)
                .entryOf(FlatLevelGeneratorPresets.THE_VOID).value().settings();
        return registries.get(RegistryKeys.WORLD_PRESET).entryOf(WorldPresets.FLAT).value()
                .createDimensionsRegistryHolder().with(registries, new FlatChunkGenerator(theVoid));
    }

    /** What still stands between the client and the joined world, or null. / 客户端距离进入世界还差什么；已进入则为 null。 */
    private static String notJoined(MinecraftClient client) {
        if (client.player == null || client.world == null || client.getServer() == null) {
            return "no player yet (screen " + screenName(client) + ")";
        }
        if (client.getOverlay() != null || client.currentScreen != null) {
            return "screen " + screenName(client) + " is still up";
        }
        if (!SparkDlcServerConnection.isConfirmedServer()) {
            return "the integrated server was not confirmed as a SparkDLC server by the handshake";
        }
        return null;
    }

    private static void reportWindow(MinecraftClient client) {
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        double scale = client.getWindow().getScaleFactor();
        if (width != WINDOW_WIDTH || height != WINDOW_HEIGHT || scale != HudSnapshotRunDirectory.GUI_SCALE) {
            LOGGER.warn(TAG + "the window is {}x{} at GUI scale {}, not {}x{} at {}: the pictures will differ from "
                    + "other runs", width, height, scale, WINDOW_WIDTH, WINDOW_HEIGHT, HudSnapshotRunDirectory.GUI_SCALE);
        } else {
            LOGGER.info(TAG + "window {}x{} at GUI scale {}, language {}", width, height, scale,
                    client.getLanguageManager().getLanguage());
        }
    }

    private static boolean onStage(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        return player != null && client.currentScreen == null && client.getOverlay() == null
                && player.getBlockPos().isWithinDistance(HudSnapshotRound.STAGE, 1.5);
    }

    private void startCase(MinecraftClient client) {
        HudSnapshotCase snapshotCase = cases.get(caseIndex);
        caseStartedAt = System.nanoTime();
        readySince = -1;
        tasksGiven = 0;
        LOGGER.info(TAG + "case '{}' ({}/{})", snapshotCase.name(), caseIndex + 1, cases.size());
        onServer(client, player -> HudSnapshotRound.apply(player, snapshotCase));
    }

    private void caseTick(MinecraftClient client) throws Exception {
        HudSnapshotCase snapshotCase = cases.get(caseIndex);
        holdCamera(client);
        if (client.currentScreen != null) {
            // Nothing in this run opens one; whatever did (a key press on the window, another mod) is closed again.
            // 本次运行不会打开任何界面；无论是什么打开的（对窗口的按键、其他模组），都把它关掉。
            if (!closedAScreen) {
                LOGGER.warn(TAG + "closing unexpected screen {}", screenName(client));
                closedAScreen = true;
            }
            client.setScreen(null);
        }
        String missing = serverFailure != null ? serverFailure : notInState(client, snapshotCase, tasksGiven);
        if (missing == null) {
            if (readySince < 0) {
                readySince = System.nanoTime();
                readySinceFrame = frames;
            }
            long drawn = frames - readySinceFrame;
            if (tasksGiven < snapshotCase.tasks().size()) {
                // The next task, once the rows before it (or the last case's fading rows) have been drawn.
                // 等之前的行（或上一个用例正在淡出的行）画过之后，再给下一个任务。
                if (drawn >= (tasksGiven == 0 ? OLD_ROWS_FRAMES : NEXT_ROW_FRAMES)) {
                    tasksGiven++;
                    PlayerMoodComponent.Task task = snapshotCase.tasks().get(snapshotCase.tasks().size() - tasksGiven);
                    readySince = -1;
                    onServer(client, player -> HudSnapshotRound.addTask(player, task));
                }
                return;
            }
            boolean eased = moodEased(client, snapshotCase);
            if (millisSince(readySince) >= SETTLE_MS && drawn >= SETTLE_FRAMES
                    && (eased || drawn >= SETTLE_FRAMES + EASING_GRACE_FRAMES)) {
                if (!eased) {
                    LOGGER.warn(TAG + "case '{}': Wathe's eased mood value is still {}; shooting anyway",
                            snapshotCase.name(), MoodRenderer.moodRender);
                }
                Path directory = client.runDirectory.toPath().resolve(HudSnapshotRunDirectory.SNAPSHOT_DIRECTORY);
                HudSnapshotImages.save(client, directory, snapshotCase.name());
                saved.add(snapshotCase.name());
                LOGGER.info(TAG + "saved {} and {} ({} frames in {} ms after the state arrived)",
                        directory.resolve(snapshotCase.name() + ".png").toAbsolutePath(),
                        snapshotCase.name() + "-topleft.png", drawn, millisSince(readySince));
                nextCase(client);
            }
            return;
        }
        readySince = -1;
        String trouble = HudSnapshotRound.trouble();
        if (serverFailure != null || trouble != null) {
            // The round is gone for good: the remaining cases cannot be shot either. / 对局已不可恢复：剩余用例同样无法截图。
            failures.add("case '" + snapshotCase.name() + "' and every later one: " + (trouble != null ? trouble : missing));
            finish(client);
        } else if (millisSince(caseStartedAt) > CASE_TIMEOUT_MS) {
            failures.add("case '" + snapshotCase.name() + "': the client never showed the state (" + missing + ")");
            nextCase(client);
        }
    }

    private void nextCase(MinecraftClient client) {
        caseIndex++;
        if (caseIndex >= cases.size()) {
            finish(client);
        } else {
            startCase(client);
        }
    }

    /**
     * What this client has not yet received of the case, or null when everything the top-left HUD draws from is in
     * place: the gates of the HUD (confirmed server, round running for this player, murder game mode, train HUD) and
     * the case's values (role, health switch and parts, mood, psycho, and the last {@code tasksGiven} tasks).
     * 本客户端尚未收到该用例的哪些内容；左上角 HUD 绘制所依据的一切都已到位时返回 null：HUD 的门槛（已确认的服务器、对局对该玩家
     * 处于进行中、谋杀模式、列车 HUD）以及用例的数值（身份、健康开关与各部位、心情、狂暴，以及后 {@code tasksGiven} 个任务）。
     */
    private static String notInState(MinecraftClient client, HudSnapshotCase snapshotCase, int tasksGiven) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null) {
            return "no player";
        }
        List<String> missing = new ArrayList<>();
        if (client.getOverlay() != null || client.currentScreen != null) {
            missing.add("screen " + screenName(client) + " is up");
        }
        if (client.options.hudHidden) {
            missing.add("the HUD is hidden (F1)");
        }
        if (!SparkDlcServerConnection.isConfirmedServer()) {
            missing.add("server not confirmed as SparkDLC");
        }
        if (client.interactionManager.getCurrentGameMode() != GameMode.ADVENTURE) {
            missing.add("player game mode is " + client.interactionManager.getCurrentGameMode());
        }
        GameWorldComponent game = GameWorldComponent.KEY.get(client.world);
        if (!(game.getGameMode() instanceof MurderGameMode)) {
            missing.add("game mode is " + (game.getGameMode() == null ? null : game.getGameMode().identifier));
        }
        if (!GameFunctions.isPlayerPlayingAndAlive(player)) {
            missing.add("not playing and alive (status " + game.getGameStatus() + ", has role " + game.hasAnyRole(player)
                    + ", dead " + game.isPlayerDead(player.getUuid()) + ")");
        }
        Role role = game.getRole(player);
        if (role != snapshotCase.role()) {
            missing.add("role is " + (role == null ? null : role.identifier()));
        }
        if (WatheClient.trainComponent == null || !WatheClient.trainComponent.hasHud()) {
            missing.add("train HUD is off");
        }
        if (HealthModeComponent.isEnabled(client.world) != snapshotCase.healthMode()) {
            missing.add("health mode is " + !snapshotCase.healthMode());
        }
        BodyHealthComponent body = BodyHealthComponent.KEY.get(player);
        for (BodyPart part : BodyPart.values()) {
            if (body.health(part) != snapshotCase.health(part)) {
                missing.add(part.id() + " is " + body.health(part));
            }
        }
        PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
        List<PlayerMoodComponent.Task> tasks = snapshotCase.tasks();
        Set<PlayerMoodComponent.Task> given = Set.copyOf(tasks.subList(tasks.size() - tasksGiven, tasks.size()));
        if (!mood.tasks.keySet().equals(given)) {
            missing.add("tasks are " + mood.tasks.keySet() + ", not " + given);
        }
        float held = HudSnapshotRound.heldMood(snapshotCase);
        if (mood.getMood() > held + 0.00001f || mood.getMood() < held - MOOD_SYNC_TOLERANCE) {
            missing.add("mood is " + mood.getMood() + ", not just under " + held);
        }
        if (PlayerPsychoComponent.KEY.get(player).getPsychoTicks() > 0 != snapshotCase.psycho()) {
            missing.add("psycho is " + !snapshotCase.psycho());
        }
        return missing.isEmpty() ? null : String.join("; ", missing);
    }

    /**
     * Whether Wathe's per-frame eased copy of the mood has arrived. Psycho mode draws no mood, so there is nothing to
     * wait for.
     * Wathe 逐帧缓动的心情副本是否已经到位。狂暴模式不绘制心情，因此无需等待。
     */
    private static boolean moodEased(MinecraftClient client, HudSnapshotCase snapshotCase) {
        return snapshotCase.psycho() || client.player == null || Math.abs(MoodRenderer.moodRender
                - PlayerMoodComponent.KEY.get(client.player).getMood()) <= MOOD_EASING_TOLERANCE;
    }

    private static void holdCamera(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }
        player.setYaw(CAMERA_YAW);
        player.setPitch(CAMERA_PITCH);
        player.prevYaw = CAMERA_YAW;
        player.prevPitch = CAMERA_PITCH;
        player.setHeadYaw(CAMERA_YAW);
    }

    /** Runs a step on the server thread; a failure there is reported by the client. / 在服务器线程上执行一步；其失败由客户端报告。 */
    private void onServer(MinecraftClient client, Consumer<ServerPlayerEntity> step) {
        IntegratedServer server = client.getServer();
        if (server == null) {
            serverFailure = "the integrated server is gone";
            return;
        }
        server.execute(() -> {
            try {
                if (server.getPlayerManager().getPlayerList().isEmpty()) {
                    serverFailure = "the integrated server has no player";
                    return;
                }
                step.accept(server.getPlayerManager().getPlayerList().get(0));
            } catch (Throwable throwable) {
                LOGGER.error(TAG + "server-side step failed", throwable);
                serverFailure = "server-side step failed: " + throwable;
            }
        });
    }

    private static String screenName(MinecraftClient client) {
        if (client.getOverlay() != null) {
            return client.getOverlay().getClass().getSimpleName();
        }
        return client.currentScreen == null ? "none" : client.currentScreen.getClass().getName();
    }

    /**
     * Reports and closes the game through vanilla's own stop, which leaves the world and stops the integrated server;
     * the shutdown hook of {@link #onInitializeClient} makes the exit code 1 when anything failed.
     * 输出结果，并通过原版自身的停止流程关闭游戏（它会离开世界并停止内置服务器）；只要有失败，{@link #onInitializeClient}
     * 中的关闭钩子就把退出码改为 1。
     */
    private void finish(MinecraftClient client) {
        phase = Phase.DONE;
        Path directory = client.runDirectory.toPath().resolve(HudSnapshotRunDirectory.SNAPSHOT_DIRECTORY).toAbsolutePath();
        if (failures.isEmpty() && !cases.isEmpty() && saved.size() == cases.size()) {
            LOGGER.info(TAG + "OK: saved {} case(s) {} to {}", saved.size(), saved, directory);
        } else {
            failed = true;
            for (String failure : failures) {
                LOGGER.error(TAG + "FAILED: {}", failure);
            }
            LOGGER.error(TAG + "FAILED: saved {} of {} case(s) {} to {}; exiting with code 1", saved.size(),
                    cases.size(), saved, directory);
        }
        client.scheduleStop();
    }

    private void startWatchdog() {
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(WATCHDOG_SECONDS * 1000L);
            } catch (InterruptedException interrupted) {
                return;
            }
            String message = TAG + "FAILED: the client was still running after " + WATCHDOG_SECONDS + " s (phase "
                    + phase + ", " + saved.size() + " case(s) saved); killing it with exit code 2";
            LOGGER.error(message);
            System.err.println(message);
            System.err.flush();
            Runtime.getRuntime().halt(2);
        }, "SparkDLC HUD snapshot watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }
}
