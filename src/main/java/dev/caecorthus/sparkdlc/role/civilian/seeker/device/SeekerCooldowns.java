package dev.caecorthus.sparkdlc.role.civilian.seeker.device;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerCooldownReason;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerRules;
import dev.caecorthus.sparkdlc.util.ExactItemCooldowns;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.IntConsumer;

/**
 * Frozen contract: the only writer of the {@code seeker_car} and {@code seeker_camera} item cooldowns. Every write is
 * the exact one ({@code SparkTraitsApi.setExactItemCooldownRemaining}), which runs past the trait cooldown modifiers
 * and performs the vanilla set itself (one write, one packet).
 * <ul>
 * <li>Car, "max + exact": a longer existing cooldown is kept. Only {@code SeekerStatusComponent#apply} calls
 * {@link #writeFloorExact}, for the cooldown a state transition starts.</li>
 * <li>Car, the one deliberate shortening: {@link #shortenExact}, called only by the shop's car service
 * ({@code SeekerShopService}). The car is exempt from forced cooldowns ({@code RoleForcedCooldowns.isExemptItem}), so
 * what it shortens is always a cooldown this class wrote.</li>
 * <li>Camera, "max + exact": {@link #writeCameraFloorExact}, called by the loadout grant (60 s) and by a successful
 * placement (45 s). The camera is an ordinary forced-cooldown slot (never exempt), so a longer forced lock is kept.</li>
 * </ul>
 * 冻结契约：{@code seeker_car} 与 {@code seeker_camera} 物品冷却的唯一写入方。每次写入都使用精确写入
 * （{@code SparkTraitsApi.setExactItemCooldownRemaining}），它越过词条冷却倍率并自行完成原版写入（只写一次、只发一个数据包）。
 * 小车“取大 + 精确”：保留更长的既有冷却；只有 {@code SeekerStatusComponent#apply} 调用 {@link #writeFloorExact}，写入状态转移
 * 开始的冷却。小车唯一有意的缩短：{@link #shortenExact}，仅由商店的小车整备（{@code SeekerShopService}）调用；小车不受强制冷却
 * 影响（{@code RoleForcedCooldowns.isExemptItem}），因此被缩短的始终是本类写入的冷却。摄像头“取大 + 精确”：
 * {@link #writeCameraFloorExact}，由装备发放（60 秒）与成功放置（45 秒）调用；摄像头是普通的强制冷却槽位（从不豁免），
 * 因此更长的强制锁定会被保留。
 */
public final class SeekerCooldowns {
    private SeekerCooldowns() {
    }

    /**
     * Server only. {@code reason} is informational: the component stores it for the HUD label; the item cooldown
     * itself is authoritative.
     * 仅服务端。{@code reason} 仅供参考：组件保存它用作 HUD 标签；物品冷却本身才是权威。
     *
     * @return true when a new value was written. / 写入了新值时返回 true。
     */
    public static boolean writeFloorExact(ServerPlayerEntity player, int ticks, SeekerCooldownReason reason) {
        if (player == null || ticks <= 0) {
            return false;
        }
        Item car = SparkDlcItems.seekerCar();
        // The exact write bypasses Fast Hands itself and performs the vanilla write.
        // 精确写入自行绕过快手并完成原版写入。
        return write(remainingTicks(player), ticks,
                merged -> SparkTraitsApi.setExactItemCooldownRemaining(player, car, merged));
    }

    /**
     * Server only: shortens the car cooldown that is running right now by {@code byTicks} (to zero when less is left)
     * with one exact write. The HUD and the console read the remaining time from the synced vanilla cooldown, so they
     * follow at once; the synced cooldown reason is left as it is (it only labels a running cooldown).
     * 仅服务端：把小车当前正在进行的冷却缩短 {@code byTicks}（剩余不足时归零），只做一次精确写入。HUD 与控制台从已同步的原版冷却
     * 读取剩余时间，因此立即跟随；已同步的冷却原因保持不变（它只是进行中冷却的标签）。
     *
     * @return false, with nothing written, when the car is not cooling down. / 小车不在冷却中时不写入并返回 false。
     */
    public static boolean shortenExact(ServerPlayerEntity player, int byTicks) {
        if (player == null || byTicks <= 0) {
            return false;
        }
        Item car = SparkDlcItems.seekerCar();
        return shorten(remainingTicks(player), byTicks,
                shortened -> SparkTraitsApi.setExactItemCooldownRemaining(player, car, shortened));
    }

    /**
     * Server only: the camera item cooldown ({@code SeekerRules.CAMERA_INITIAL_COOLDOWN_TICKS} from the loadout grant,
     * {@code SeekerRules.CAMERA_USE_COOLDOWN_TICKS} after a successful placement). Vanilla keys a cooldown by item, so
     * it covers every camera stack the player holds or buys.
     * 仅服务端：摄像头物品冷却（装备发放起 {@code SeekerRules.CAMERA_INITIAL_COOLDOWN_TICKS}，成功放置后
     * {@code SeekerRules.CAMERA_USE_COOLDOWN_TICKS}）。原版冷却按物品计，因此覆盖玩家持有或购买的每一组摄像头。
     *
     * @return true when a new value was written. / 写入了新值时返回 true。
     */
    public static boolean writeCameraFloorExact(ServerPlayerEntity player, int ticks) {
        if (player == null || ticks <= 0) {
            return false;
        }
        Item camera = SparkDlcItems.seekerCamera();
        return write(cameraRemainingTicks(player), ticks,
                merged -> SparkTraitsApi.setExactItemCooldownRemaining(player, camera, merged));
    }

    /**
     * Pure write rule: nothing happens unless the merged value ({@link SeekerRules#mergeCooldownTicks}) is longer than
     * what already runs; then the exact writer runs once.
     * 纯写入规则：只有取大后的值长于当前剩余时间才写入；随后精确写入执行一次。
     */
    static boolean write(int existingTicks, int requestedTicks, IntConsumer exactWriter) {
        int existing = Math.max(0, existingTicks);
        int merged = SeekerRules.mergeCooldownTicks(existing, requestedTicks);
        if (merged <= existing) {
            return false;
        }
        exactWriter.accept(merged);
        return true;
    }

    /**
     * Pure shortening rule: nothing happens unless a cooldown is running; then the exact writer runs once with
     * {@link SeekerRules#shortenedCooldownTicks} (zero ends the cooldown).
     * 纯缩短规则：只有冷却正在进行时才写入；随后精确写入以 {@link SeekerRules#shortenedCooldownTicks} 执行一次（零即结束冷却）。
     */
    static boolean shorten(int existingTicks, int byTicks, IntConsumer exactWriter) {
        if (existingTicks <= 0 || byTicks <= 0) {
            return false;
        }
        exactWriter.accept(SeekerRules.shortenedCooldownTicks(existingTicks, byTicks));
        return true;
    }

    /**
     * Remaining ticks on the car item cooldown; 0 when none. Side-neutral: the client reads its synced vanilla cooldown
     * (HUD, console), the server its authoritative one.
     * 小车物品冷却的剩余刻数；无冷却时为 0。两端通用：客户端读取已同步的原版冷却（HUD、控制台），服务端读取权威冷却。
     */
    public static int remainingTicks(PlayerEntity player) {
        return ExactItemCooldowns.remainingTicks(player, SparkDlcItems.seekerCar());
    }

    /**
     * Remaining ticks on the camera item cooldown; 0 when none. Side-neutral like {@link #remainingTicks}.
     * 摄像头物品冷却的剩余刻数；无冷却时为 0。与 {@link #remainingTicks} 一样两端通用。
     */
    public static int cameraRemainingTicks(PlayerEntity player) {
        return ExactItemCooldowns.remainingTicks(player, SparkDlcItems.seekerCamera());
    }
}
