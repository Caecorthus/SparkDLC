package dev.caecorthus.sparkdlc.round.dualselect;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.faction.api.PoliceRoles;
import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.doctor4t.wathe.api.Faction;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.api.event.RoleAssigned;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import dev.caecorthus.sparkdlc.SparkDlcFactions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * The ten second window at round start where everyone whose role has a same-faction alternative is offered a second
 * role. The round is frozen while the choice is open, and players who do not answer keep the role they were first
 * dealt.
 * <p>
 * 开局那十秒的「二选一」：同阵营还有别的身份可选的人会收到第二个身份供其挑；选择期间整局冻结，不答的人保留开局
 * 自动分配的那个身份。
 */
public class DualRoleSelectionComponent implements AutoSyncedComponent {
    public static final ComponentKey<DualRoleSelectionComponent> KEY = ComponentRegistry.getOrCreate(
            SparkDLC.id("dual_role_selection"),
            DualRoleSelectionComponent.class
    );

    public enum Phase { INACTIVE, SELECTING, COMPLETE }

    /** Roles that only appear through death, revival or a non-murder game mode are never offered. */
    /** 只在死亡、复活或非谋杀模式里出现的身份不能作为备选。 */
    /**
     * 不参与「正常抽取」的身份。名单不是猜的：把 SparkDLC 里每一处 {@code appearanceCondition(context -> false)}
     * 与 {@code new Role(..., context -> false)} 全部列出来核对过（2026-11-23）。
     * <p>
     * 依据（文件:行）：
     * SparkWitchRoleRegistry 751 GRAND_WITCH_ID、760 ACCOMPLICE_ID、776 POTION_GUNNER_ID、1221 LOST_SOUL_ID、
     * 1349 SPHINX_HAO_ID、1365 SNIPER_ID、1396 WOVENER_ID、1411 INSIDER_ID；
     * GuardianAngelRole:18、BewitchedRole:25、AbyssListenerRole:23、RiftwalkerRole:23、VendettaRole:20、
     * WindSpiritRole:21、SaboteurRole:20、CurserRole、WraithRole:12（{@code context -> false}）。
     * <p>
     * 注意 {@code sparkwitch:specter}（幽魂）**不在这里** —— 它是杀手的正常刷新职业（用户 2026-11-23 指正）。
     * {@code sparkwitch:wraith}（冤魂）才是死后出现的那个。
     */
    private static final Set<String> NOT_FROM_NORMAL_DRAW = Set.of(
            "sparkwitch:grand_witch",      // 大魔女
            "sparkwitch:accomplice",       // 共犯
            "sparkwitch:potion_gunner",    // 药炮手
            "sparkwitch:lost_soul",        // 迷失的灵魂（卡戎复活）
            "sparkwitch:sphinx_hao",       // 狮身豪
            "sparkwitch:sniper",           // 狙击手
            "sparkwitch:wovener",          // 被织者
            "sparkwitch:insider",          // 内应
            "sparkwitch:guardian_angel",   // 守护天使
            "sparkwitch:bewitched",        // 魔化使
            "sparkwitch:abyss_listener",   // 聆渊者
            "sparkwitch:riftwalker",       // 隙行者
            "sparkwitch:vendetta",         // 仇杀客
            "sparkwitch:wind_spirit",      // 风精灵
            "sparkwitch:saboteur",         // 破坏者
            "sparkwitch:curser",           // 诅咒者
            "sparkwitch:wraith"            // 冤魂（死后出现）
    );

    private static final Set<Role> BASE_ROLES = Set.of(
            WatheRoles.KILLER, WatheRoles.VIGILANTE, WatheRoles.CIVILIAN, WatheRoles.LOOSE_END
    );
    public static final int SELECT_TICKS = GameConstants.FADE_TIME + GameConstants.FADE_PAUSE + 10 * 20;
    /** How long the two buttons stay clickable; after that the sheet shows the locked-in role. */
    /** 选项可以点多久；之后告示页显示最终身份。 */
    public static final int CHOOSE_TICKS = GameConstants.FADE_TIME + GameConstants.FADE_PAUSE + 7 * 20;
    private static boolean FREEZING = false;

    private final Scoreboard scoreboard;
    private final Map<UUID, List<Identifier>> options = new LinkedHashMap<>();
    private final Map<UUID, Vec3d> anchors = new LinkedHashMap<>();
    private final Map<UUID, Integer> choices = new LinkedHashMap<>();
    private Phase phase = Phase.INACTIVE;
    private int ticksRemaining = 0;
    private List<Identifier> localOptions = List.of();
    private int localChoice = -1;

    public DualRoleSelectionComponent(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public DualRoleSelectionComponent(Scoreboard scoreboard, @Nullable MinecraftServer server) {
        this(scoreboard);
    }

    public static boolean isFreezing() {
        return FREEZING;
    }

    public void sync() {
        KEY.sync(scoreboard);
    }

    public Phase phase() {
        return phase;
    }

    public int ticksRemaining() {
        return ticksRemaining;
    }

    public List<Identifier> localOptions() {
        return localOptions;
    }

    public int localChoice() {
        return localChoice;
    }

    public void startVote(ServerWorld world, GameWorldComponent gameWorldComponent) {
        begin(world, gameWorldComponent);
    }

    private void begin(ServerWorld world, GameWorldComponent gameWorldComponent) {
        options.clear();
        choices.clear();
        List<ServerPlayerEntity> players = world.getPlayers();
        Set<Identifier> used = new HashSet<>();
        for (ServerPlayerEntity player : players) {
            Role role = gameWorldComponent.getRole(player);
            if (role != null) {
                used.add(role.identifier());
            }
        }
        Map<FactionGroup, List<UUID>> byFaction = new LinkedHashMap<>();
        Map<UUID, Role> roles = new LinkedHashMap<>();
        for (ServerPlayerEntity player : players) {
            Role role = gameWorldComponent.getRole(player);
            if (!participates(role)) {
                continue;
            }
            byFaction.computeIfAbsent(groupOf(role), k -> new ArrayList<>()).add(player.getUuid());
            roles.put(player.getUuid(), role);
        }
        boolean any = false;
        for (Map.Entry<FactionGroup, List<UUID>> entry : byFaction.entrySet()) {
            List<Role> pool = candidates(gameWorldComponent, entry.getKey(), used);
            if (pool.size() < entry.getValue().size()) {
                continue;
            }
            any = true;
            Collections.shuffle(pool, new Random(world.getRandom().nextLong()));
            for (int i = 0; i < entry.getValue().size(); i++) {
                UUID id = entry.getValue().get(i);
                Role alternate = pool.get(i);
                used.add(alternate.identifier());
                options.put(id, List.of(roles.get(id).identifier(), alternate.identifier()));
                choices.put(id, 0);
            }
        }
        // 没被抽中二选一的人同样吃黑屏告示页，这样开局节奏一致。
        // Everyone who cannot choose still gets the notice sheet, so the round starts in lockstep.
        if (any) {
            for (ServerPlayerEntity player : players) {
                Role role = gameWorldComponent.getRole(player);
                if (role == null || options.containsKey(player.getUuid())) {
                    continue;
                }
                options.put(player.getUuid(), List.of(role.identifier()));
            }
        }
        if (!any) {
            phase = Phase.INACTIVE;
            FREEZING = false;
            sync();
            return;
        }
        phase = Phase.SELECTING;
        ticksRemaining = SELECT_TICKS;
        FREEZING = true;
        anchors.clear();
        for (ServerPlayerEntity player : players) {
            if (options.containsKey(player.getUuid())) {
                anchors.put(player.getUuid(), player.getPos());
            }
        }
        sync();
    }

    public void choose(UUID playerId, int index) {
        if (phase != Phase.SELECTING) {
            return;
        }
        List<Identifier> playerOptions = options.get(playerId);
        if (playerOptions == null || index < 0 || index >= playerOptions.size()) {
            return;
        }
        choices.put(playerId, index);
        sync();
    }

    public void tick(ServerWorld world, GameWorldComponent gameWorldComponent) {
        if (phase != Phase.SELECTING) {
            return;
        }
        holdPlayers(world);
        if (--ticksRemaining <= 0) {
            applyResults(world, gameWorldComponent);
        } else if (ticksRemaining % 20 == 0) {
            sync();
        }
    }

    /**
     * Keeps every participant standing where the freeze started. Vanilla movement packets still arrive, so the
     * server snaps anyone who walked away back to their anchor every tick.
     * <p>
     * 把每个参与者按在冻结开始时站的位置上。客户端仍在发移动包，所以每 tick 把走开的人拉回锚点。
     */
    private void holdPlayers(ServerWorld world) {
        if (anchors.isEmpty()) {
            return;
        }
        for (ServerPlayerEntity player : world.getPlayers()) {
            Vec3d anchor = anchors.get(player.getUuid());
            if (anchor == null) {
                continue;
            }
            if (player.getPos().squaredDistanceTo(anchor) > 0.01D) {
                player.requestTeleport(anchor.x, anchor.y, anchor.z);
                player.setVelocity(Vec3d.ZERO);
                player.velocityModified = true;
            }
        }
    }

    public List<Identifier> getLocalOptions() {
        return List.copyOf(localOptions);
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public boolean isSelecting() {
        return phase == Phase.SELECTING;
    }

    public boolean canChoose() {
        return localOptions.size() >= 2;
    }

    public int getLocalChoice() {
        return localChoice;
    }

    private void applyResults(ServerWorld world, GameWorldComponent gameWorldComponent) {
        phase = Phase.COMPLETE;
        FREEZING = false;
        List<ServerPlayerEntity> players = world.getPlayers();
        int killers = 0;
        for (ServerPlayerEntity player : players) {
            Role role = gameWorldComponent.getRole(player);
            if (role != null && role.canUseKiller()) {
                killers++;
            }
        }
        for (ServerPlayerEntity player : players) {
            int index = choices.getOrDefault(player.getUuid(), 0);
            List<Identifier> playerOptions = options.get(player.getUuid());
            if (index <= 0 || playerOptions == null || index >= playerOptions.size()) {
                continue;
            }
            Identifier targetId = playerOptions.get(index);
            Role target = WatheRoles.ROLES.stream()
                    .filter(r -> r.identifier().equals(targetId))
                    .findFirst().orElse(null);
            if (target == null) {
                continue;
            }
            gameWorldComponent.addRole(player, target);
            try {
                RoleAssigned.EVENT.invoker().assignRole(player, target);
            } catch (RuntimeException exception) {
                // 一个监听器抛错不能让后面的人继续用旧身份。
                // One listener blowing up must not leave the remaining players on their first role.
                SparkDLC.LOGGER.error("[dualmode] role listener failed for {}", player.getName().getString(), exception);
            }
        }
        gameWorldComponent.sync();
        // 换完身份才发信件：Wathe 默认在 ON_FINISH_INITIALIZE 之前就按旧身份把信塞进背包，
        // 那样信里写的角色和玩家实际拿到的会对不上。这里补发一次，旧信件由 mixin 跳过。
        try {
            dev.caecorthus.sparkdlc.mixin.round.dualselect.GameFunctionsLettersMixin
                    .sparkdlc$invokeGiveLetters(world, gameWorldComponent, players, java.util.Map.of());
        } catch (Throwable throwable) {
            SparkDLC.LOGGER.warn("[dualmode] letter refresh failed", throwable);
        }
        for (ServerPlayerEntity player : players) {
            Role role = gameWorldComponent.getRole(player);
            if (role == null) {
                continue;
            }
            ServerPlayNetworking.send(player, new AnnounceWelcomePayload(
                    role.identifier().toString(), killers, players.size() - killers));
        }
        options.clear();
        choices.clear();
        anchors.clear();
        phase = Phase.INACTIVE;
        sync();
    }

    private enum FactionGroup { CIVILIAN, KILLER, NEUTRAL }

    /**
     * 三层划分（用户 2026-11-23 的口径）：
     * <ul>
     *   <li>{@link Category#NOT_IN_MODE} 不会在双选模式里出现：探索模式平民、空身份、只在生还模式抽的亡命徒、
     *       以及一切「不是靠正常抽取拿到」的身份（死亡后出现的冤魂与其晋升链、卡戎复活的迷失之魂）。</li>
     *   <li>{@link Category#KEEP_ASSIGNED} 会出现、但玩家不参与选择：警职、嘉豪阵营、魔女阵营、以及基础身份。
     *       他们照常按系统分配的身份开局，只是不弹二选一。</li>
     *   <li>{@link Category#PICKABLE} 其余乘客 / 杀手 / 中立常规身份，进候选池。</li>
     * </ul>
     * Three buckets, matching the spec: never appears in this mode / appears but does not get to choose / picks.
     */
    public enum Category { NOT_IN_MODE, KEEP_ASSIGNED, PICKABLE }

    public static Category classify(Role role) {
        if (role == null) {
            return Category.KEEP_ASSIGNED;
        }
        if (role == WatheRoles.NO_ROLE || role == WatheRoles.DISCOVERY_CIVILIAN) {
            return Category.NOT_IN_MODE;
        }
        String id = role.identifier().toString();
        if (NOT_FROM_NORMAL_DRAW.contains(id)) {
            return Category.NOT_IN_MODE;
        }
        if (BASE_ROLES.contains(role) || PoliceRoles.contains(role)) {
            return Category.KEEP_ASSIGNED;
        }
        Identifier faction = SparkFactionApi.resolveEffectiveFaction(role);
        if (faction != null
                && (SparkDlcFactions.JIAHAO.equals(faction)
                    || SparkDlcFactions.WITCH.equals(faction)
                    || SparkDlcFactions.MURDEROUS_WITCH.equals(faction))) {
            return Category.KEEP_ASSIGNED;
        }
        return groupOf(role) == null ? Category.KEEP_ASSIGNED : Category.PICKABLE;
    }

    /** 本局是不是双选模式（按 GameWorldComponent 里持久化的模式判断）。 */
    public static boolean isDualSelectRound(GameWorldComponent gameWorldComponent) {
        if (gameWorldComponent == null) return false;
        var mode = gameWorldComponent.getGameMode();
        return mode != null
                && DualSelectService.MODE_ID.toString().equals(mode.identifier.toString());
    }

    private static boolean participates(Role role) {
        return classify(role) == Category.PICKABLE;
    }

    private static FactionGroup groupOf(Role role) {
        if (role.isInnocent()) {
            return FactionGroup.CIVILIAN;
        }
        return role.getFaction() == Faction.KILLER ? FactionGroup.KILLER : FactionGroup.NEUTRAL;
    }

    private static List<Role> candidates(GameWorldComponent gameWorldComponent, FactionGroup group, Set<Identifier> used) {
        List<Role> pool = new ArrayList<>();
        for (Role candidate : WatheRoles.ROLES) {
            if (BASE_ROLES.contains(candidate) || used.contains(candidate.identifier())) {
                continue;
            }
            if (classify(candidate) != Category.PICKABLE || groupOf(candidate) != group) {
                continue;
            }
            if (!gameWorldComponent.isRoleEnabled(candidate)) {
                continue;
            }
            pool.add(candidate);
        }
        return pool;
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeEnumConstant(phase);
        buf.writeVarInt(ticksRemaining);
        List<Identifier> mine = options.getOrDefault(recipient.getUuid(), List.of());
        buf.writeVarInt(mine.size());
        for (Identifier id : mine) {
            buf.writeIdentifier(id);
        }
        buf.writeVarInt(choices.getOrDefault(recipient.getUuid(), -1));
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        this.phase = buf.readEnumConstant(Phase.class);
        this.ticksRemaining = buf.readVarInt();
        int size = buf.readVarInt();
        List<Identifier> read = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            read.add(buf.readIdentifier());
        }
        this.localOptions = read;
        this.localChoice = buf.readVarInt();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        tag.putString("Phase", phase.name());
        tag.putInt("Ticks", ticksRemaining);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        try {
            phase = Phase.valueOf(tag.getString("Phase"));
        } catch (IllegalArgumentException ignored) {
            phase = Phase.INACTIVE;
        }
        ticksRemaining = tag.getInt("Ticks");
    }
}
