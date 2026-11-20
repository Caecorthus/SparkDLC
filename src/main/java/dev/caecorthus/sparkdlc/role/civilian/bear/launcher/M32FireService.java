package dev.caecorthus.sparkdlc.role.civilian.bear.launcher;

import dev.caecorthus.sparkdlc.SparkDlcSounds;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.mixin.cooldown.ItemCooldownEntryAccessor;
import dev.caecorthus.sparkdlc.mixin.cooldown.ItemCooldownManagerAccessor;
import dev.caecorthus.sparkdlc.SparkDlcDataComponents;
import dev.caecorthus.sparkdlc.role.civilian.bear.BearItems;
import dev.caecorthus.sparkdlc.role.civilian.bear.BearRules;
import dev.caecorthus.sparkdlc.role.civilian.bear.net.FireM32A1C2SPayload;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.ControlExpertStun;
import dev.caecorthus.sparkdlc.role.civilian.seeker.remote.SeekerRemoteSessionService;
import dev.caecorthus.sparkdlc.role.killer.kidnapper.KidnapperControlComponent;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithStateService;
import dev.caecorthus.sparkdlc.util.OffMatchUse;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/**
 * Server-authoritative M32A1 fire (WP1). The client sends only {@code sparkdlc:fire_m32a1} with its click-time aim;
 * this service resolves the {@link OffMatchUse} mode and validates the held launcher, the stun/session/Kidnapper/Wraith
 * locks, the traits' weapon blocks, the cooldown and a loaded chamber ({@link M32FireRules}) before it spawns a
 * {@link SmokeRoundEntity} from its OWN eye position. Only a spawned round advances the cylinder ({@link
 * M32Cylinder#fire()}, D8: the next loaded chamber in rotation), starts the 1 s cooldown through the exact floor writer
 * (bypasses Fast Hands, never shortens a longer lock) and plays the public shot (the sound already contains the
 * cylinder clack). An empty cylinder answers with a dry click only the shooter hears, and no cooldown. Fabric runs play
 * receivers on the server thread (the stun and Seeker payload guards wrap that hand-off), so nothing else is needed.
 * 服务端权威的 M32A1 开火（WP1）。客户端只发送带点击瞬间朝向的 {@code sparkdlc:fire_m32a1}；本服务判定 {@link OffMatchUse}
 * 模式，并复核手持发射器、眩晕/会话/绑匪/冤魂锁定、词条的武器封锁、冷却与已装填弹膛（{@link M32FireRules}）后，才从服务端
 * 自身的眼部位置生成 {@link SmokeRoundEntity}。只有成功生成的榴弹才会转动转轮（{@link M32Cylinder#fire()}，D8：按转动顺序的下一
 * 个已装填弹膛）、经精确取大写入开始 1 秒冷却（绕过快手，绝不缩短更长的锁定），并播放公开的射击声（该声音已包含转轮咔哒声）。
 * 空转轮只给射手一声空响，不开始冷却。Fabric 在服务端线程执行游戏数据包接收器（眩晕与搜寻者拦截包装了该切换），因此无需其他处理。
 */
public final class M32FireService {
    static final String EMPTY_KEY = "message.sparkdlc.bear.m32.empty";
    /** Shot volume 3 = a 48-block hearing radius (sound README). / 射击音量 3 = 48 格听觉半径（音效说明）。 */
    static final float FIRE_VOLUME = 3.0F;
    static final float DRY_CLICK_VOLUME = 1.0F;

    private static boolean registered;

    private M32FireService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerPlayNetworking.registerGlobalReceiver(FireM32A1C2SPayload.ID,
                (payload, context) -> fire(context.player(), payload));
    }

    public static void fire(ServerPlayerEntity player, FireM32A1C2SPayload payload) {
        if (player == null || payload == null) {
            return;
        }
        OffMatchUse.Mode mode = OffMatchUse.mode(player);
        ItemStack launcher = player.getMainHandStack();
        boolean holding = launcher.getItem() instanceof M32A1Item;
        M32Cylinder cylinder = holding ? M32A1Item.cylinder(launcher) : M32Cylinder.EMPTY;
        M32FireRules.Decision decision = M32FireRules.decide(new M32FireRules.Facts(
                mode,
                holding,
                player.isSpectator(),
                ControlExpertStun.isStunned(player),
                SeekerRemoteSessionService.isLocked(player),
                isKidnapped(player),
                WraithStateService.isActive(player),
                holding && SparkTraitsApi.blocksWeaponAction(player, launcher),
                holding && player.getItemCooldownManager().isCoolingDown(launcher.getItem()),
                !cylinder.isEmpty()));
        switch (decision) {
            case FIRE -> shoot(player, launcher, cylinder, payload, mode == OffMatchUse.Mode.PRESENTATION);
            case EMPTY -> dryClick(player);
            default -> {
            }
        }
    }

    private static void shoot(ServerPlayerEntity player, ItemStack launcher, M32Cylinder cylinder,
                              FireM32A1C2SPayload payload, boolean presentation) {
        M32Cylinder.Fired fired = cylinder.fire();
        if (fired == null) {
            return;
        }
        Vec3d direction = M32FireRules.aimDirection(payload.hasAim(), payload.yaw(), payload.pitch(),
                player.getYaw(), player.getPitch());
        if (!SmokeRoundEntity.launch(player, direction, presentation)) {
            return;
        }
        launcher.set(SparkDlcDataComponents.M32_CYLINDER, fired.after());
        writeFloorExact(player, BearRules.M32_COOLDOWN_TICKS);
        player.getServerWorld().playSound(null, player.getX(), player.getEyeY(), player.getZ(),
                SparkDlcSounds.BEAR_M32_FIRE, SoundCategory.PLAYERS, FIRE_VOLUME,
                0.97F + player.getRandom().nextFloat() * 0.06F);
    }

    /** Empty cylinder: a dry click only the shooter hears, plus an action-bar hint. / 空转轮：仅射手可闻的空响与动作栏提示。 */
    private static void dryClick(ServerPlayerEntity player) {
        player.playSoundToPlayer(SparkDlcSounds.BEAR_M32_EMPTY, SoundCategory.PLAYERS, DRY_CLICK_VOLUME, 1.0F);
        player.sendMessage(Text.translatable(EMPTY_KEY).withColor(BearRules.COLOR), true);
    }

    /**
     * Server only, the M32A1's single cooldown writer: {@code max(existing, ticks)} written once, the exact write
     * ({@code SparkTraitsApi.setExactItemCooldownRemaining}) first (it bypasses Fast Hands), the vanilla set only when
     * that answers false, which since the fusion happens only for a null player or item, so it is unreachable here; the
     * USEC writer rule. Returns true when a new value was written.
     * 仅服务端，M32A1 唯一的冷却写入方：写入一次 {@code max(既有, ticks)}，优先使用精确写入
     * （{@code SparkTraitsApi.setExactItemCooldownRemaining}，绕过快手），仅当其返回 false 时才执行原版写入；融合后只有玩家
     * 或物品为 null 时才返回 false，因此此处不可达；即 USEC 的写入规则。写入了新值时返回 true。
     */
    static boolean writeFloorExact(ServerPlayerEntity player, int ticks) {
        Item item = BearItems.M32A1;
        int merged = M32FireRules.floorWrite(remainingTicks(player, item), ticks);
        if (merged <= 0) {
            return false;
        }
        if (!SparkTraitsApi.setExactItemCooldownRemaining(player, item, merged)) {
            player.getItemCooldownManager().set(item, merged);
        }
        return true;
    }

    /** Remaining ticks on {@code item}'s cooldown on the player's own clock; 0 when none. / 剩余冷却刻数；无冷却时为 0。 */
    static int remainingTicks(PlayerEntity player, Item item) {
        if (!(player.getItemCooldownManager() instanceof ItemCooldownManagerAccessor accessor)) {
            return 0;
        }
        Object entry = accessor.sparkdlc$getEntries().get(item);
        if (!(entry instanceof ItemCooldownEntryAccessor cooldown)) {
            return 0;
        }
        return Math.max(0, cooldown.sparkdlc$getEndTick() - accessor.sparkdlc$getTick());
    }

    private static boolean isKidnapped(ServerPlayerEntity player) {
        return KidnapperControlComponent.KEY.maybeGet(player).map(KidnapperControlComponent::isControlled)
                .orElse(false);
    }
}
