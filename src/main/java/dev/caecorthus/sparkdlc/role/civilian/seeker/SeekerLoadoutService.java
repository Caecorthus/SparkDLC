package dev.caecorthus.sparkdlc.role.civilian.seeker;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerCarItem;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerCooldowns;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Round-start loadout at ON_FINISH_INITIALIZE: revolver + Search Car + one Security Camera, exact 60 s car cooldown,
 * exact 60 s camera cooldown, starting balance.
 * The grant is driven only by {@code SeekerLifecycleService} (WP-02), after the Conscience trait's compensation and
 * Wathe's per-Item revolver start cooldown, while the round is still STARTING; this module registers no round-start
 * listener of its own, so the loadout can never be granted twice.
 * ON_FINISH_INITIALIZE 时的开局装备：左轮 + 搜寻小车 + 一个摄像头、精确 60 秒小车冷却、精确 60 秒摄像头冷却、开局余额。发放只由
 * {@code SeekerLifecycleService}（WP-02）驱动，时机在良心词条补偿与 Wathe 按物品写入的左轮开局冷却之后、
 * 对局仍处于 STARTING 时；本模块不注册任何开局监听，因此装备不会被发放两次。
 */
public final class SeekerLoadoutService {
    /** Cameras in the loadout (owner spec 2026-11-28); more come from the shop. / 装备中的摄像头数量（所有者规格 2026-11-28）；更多的从商店购买。 */
    static final int STARTING_CAMERAS = 1;

    private SeekerLoadoutService() {
    }

    /** Registers no round-start listener: the grant is driven by SeekerLifecycleService. / 不注册开局监听：发放由生命周期服务驱动。 */
    public static void register() {
        // Intentionally empty: see the class Javadoc. / 刻意留空：见类注释。
    }

    /**
     * Called exactly once per final Seeker by {@code SeekerLifecycleService} at ON_FINISH_INITIALIZE, after
     * {@code bindMatch}: revolver + car item + one camera (camera cooldown 60 s),
     * {@code component.apply(state.grant())} (INITIAL cooldown written by {@code apply}) and the starting balance.
     * Mid-round only through {@code SeekerLifecycleService.grantLateLoadout}.
     * 由 {@code SeekerLifecycleService} 在 ON_FINISH_INITIALIZE、{@code bindMatch} 之后对每名最终搜寻者恰好调用一次：
     * 发放左轮、小车物品与一个摄像头（摄像头冷却 60 秒）、{@code component.apply(state.grant())}（INITIAL 冷却由 {@code apply} 写入）以及开局余额。对局中途仅经由
     * {@code SeekerLifecycleService.grantLateLoadout} 调用。
     */
    public static void grant(ServerPlayerEntity player) {
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
        // Defensive final-role check; the round is still STARTING, so no running-state helper is used here.
        // 防御性的最终身份检查；此时对局仍处于 STARTING，因此不使用运行态判定。
        if (!receivesGrant(game.hasAnyRole(player), game.isPlayerDead(player.getUuid()), game.getRole(player))) {
            return;
        }
        PlayerInventory inventory = player.getInventory();
        // The revolver shares Wathe's per-Item start cooldown. The Impostor trait refuses free guns to its holder at
        // this point; that trait rule is deliberately not bypassed.
        // 左轮沿用 Wathe 按物品写入的开局冷却。内鬼词条此时会拒绝向其持有者发放免费枪械；刻意不绕过该词条规则。
        inventory.insertStack(new ItemStack(WatheItems.REVOLVER));
        if (needsCarItem(holdsCarItem(inventory))) {
            inventory.insertStack(new ItemStack(SparkDlcItems.seekerCar()));
        }
        // Owner spec 2026-11-28: one starting camera, with the camera item on its 60 s opening cooldown (exact + max;
        // the cooldown is per item, so it also covers cameras bought during it). The mid-round path grants the same.
        // 所有者规格 2026-11-28：开局发放一个摄像头，摄像头物品进入 60 秒开局冷却（精确 + 取大；冷却按物品计，因此期间买到的
        // 摄像头同样受其限制）。对局中途的路径发放相同内容。
        inventory.insertStack(new ItemStack(SparkDlcItems.seekerCamera(), STARTING_CAMERAS));
        SeekerCooldowns.writeCameraFloorExact(player, SeekerRules.CAMERA_INITIAL_COOLDOWN_TICKS);
        // READY + INITIAL: apply() writes the car cooldown a transition starts (exact + max, 60 s).
        // READY + INITIAL：apply() 写入状态转移开始的小车冷却（精确 + 取大，60 秒）。
        SeekerStatusComponent component = SeekerStatusComponent.KEY.get(player);
        component.apply(component.state().grant());
        SeekerEconomyService.initialize(player);
        inventory.markDirty();
        player.currentScreenHandler.sendContentUpdates();
    }

    /** Only a living final Seeker with a role receives the loadout. / 只有拥有身份且存活的最终搜寻者获得装备。 */
    static boolean receivesGrant(boolean hasRole, boolean dead, @Nullable Role finalRole) {
        return hasRole && !dead && SeekerRules.isSeeker(finalRole);
    }

    /** One car item per Seeker, ever. / 每名搜寻者始终只有一个小车物品。 */
    static boolean needsCarItem(boolean alreadyHoldsCar) {
        return !alreadyHoldsCar;
    }

    private static boolean holdsCarItem(PlayerInventory inventory) {
        return inventory.contains(stack -> stack.getItem() instanceof SeekerCarItem);
    }
}
