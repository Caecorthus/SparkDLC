package dev.caecorthus.sparkdlc.role.civilian.demonhunter;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.tag.WatheItemTags;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.demonhunter.DemonHunterPistolItem;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Server side of the Demon Hunter pistol rework (rules in {@link DemonHunterPistolRules}): the pistol handed out with
 * the role, and the facts the mixins on NoellesRoles' pistol code ask for ({@code mixin.role.civilian.demonhunter}).
 * NoellesRoles still owns the item, the shot, the 2 bullets per frenzy start and the removal at the round reset.
 * 猎魔枪重做的服务端部分（规则见 {@link DemonHunterPistolRules}）：随职业发放的猎魔枪，以及挂在 NoellesRoles 猎魔枪代码上的
 * mixin（{@code mixin.role.civilian.demonhunter}）所询问的事实。物品、射击、每次疯魔开始的 2 发子弹以及回合重置时的移除仍归
 * NoellesRoles。
 */
public final class DemonHunterPistolService {
    /**
     * Players the round's role map made a Demon Hunter, so that a later change to another role takes the pistol back
     * from exactly them (and from nobody who merely holds one). Server thread only; emptied per player by the round
     * reset. A leftover entry (server stopped mid-round) only costs one no-op look at that player's inventory.
     * 被本局职业表定为猎魔人的玩家；之后改成其他职业时只从他们身上收回猎魔枪（不动其他只是拿着枪的人）。仅服务端线程访问；
     * 回合重置时按玩家清除。残留条目（服务器在对局中途停止）只会多看一次该玩家的背包，不产生任何效果。
     */
    private static final Set<UUID> ASSIGNED = new HashSet<>();

    /**
     * The Demon Hunter whose ground gun pickup is being decided right now, else null: written by the server thread
     * around {@code ItemEntity#onPlayerCollision}, compared by identity (a client-side player object never matches).
     * 此刻正在判定地面捡枪的猎魔人，否则为 null：由服务端线程在 {@code ItemEntity#onPlayerCollision} 前后写入，按对象身份比较
     * （客户端的玩家对象永远不会相等）。
     */
    private static @Nullable PlayerEntity gunPickupBy;

    private DemonHunterPistolService() {
    }

    /**
     * {@code RoleAssigned}, at the round start and mid-round alike (Wathe clears the inventory before any round-start
     * assignment, so the pistol is not wiped): a new Demon Hunter gets an empty pistol unless one is already carried;
     * a player the role map moves away from the Demon Hunter (a round-start re-deal, a mid-round role change) loses
     * it. A frenzy that is already running adds no bullets.
     * {@code RoleAssigned}，开局与对局中途相同（Wathe 在任何开局分配之前清空背包，所以枪不会被抹掉）：新的猎魔人拿到一把空枪，
     * 除非身上已有；被职业表从猎魔人改成其他职业的玩家（开局重新发牌、对局中途变更）失去它。已经在进行的疯魔不会补发子弹。
     */
    public static void assignForRole(ServerPlayerEntity player, Role role) {
        if (DemonHunterPistolRules.keepsPistol(role)) {
            ASSIGNED.add(player.getUuid());
            issuePistol(player);
        } else if (ASSIGNED.remove(player.getUuid())) {
            takePistol(player);
        }
    }

    /** Round reset: NoellesRoles' {@code ResetPlayer} listener removes the pistol itself. / 回合重置：猎魔枪由 NoellesRoles 的 {@code ResetPlayer} 监听器自行移除。 */
    public static void clearPlayer(ServerPlayerEntity player) {
        ASSIGNED.remove(player.getUuid());
    }

    /**
     * Whether the player keeps the pistol all round: the raw role, as NoellesRoles' frenzy listeners read it
     * ({@code getAllWithRole}); a disguise is not a Demon Hunter.
     * 该玩家是否整局保留猎魔枪：按原始职业判断，与 NoellesRoles 疯魔监听器的读取方式一致（{@code getAllWithRole}）；
     * 伪装不算猎魔人。
     */
    public static boolean keepsPistol(PlayerEntity player) {
        return DemonHunterPistolRules.keepsPistol(GameWorldComponent.KEY.get(player.getWorld()).getRole(player));
    }

    /** Psycho ticks left, of any psycho kind. / 还有疯魔 tick（任何一种疯魔）。 */
    public static boolean isFrenzied(PlayerEntity player) {
        return PlayerPsychoComponent.KEY.get(player).getPsychoTicks() > 0;
    }

    /**
     * The one definition of "someone is frenzied": a living player of the round with psycho ticks left. Wathe's
     * {@code isPsychoActive} counts the public psycho only, so the Jester Moment, the Pig God chase, the Depression
     * counterattack and the Silencer's silent psycho would be missed.
     * “有人处于疯魔”的唯一定义：本局一名存活玩家还有疯魔 tick。Wathe 的 {@code isPsychoActive} 只统计公开疯魔，会漏掉小丑时刻、
     * 皮革追杀、抑郁反击与静语者的静默疯魔。
     */
    public static boolean isAnyoneFrenzied(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (isFrenzied(player) && GameFunctions.isPlayerPlayingAndAlive(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@link #isAnyoneFrenzied} at a frenzy start; the player whose frenzy starts always counts.
     * 疯魔开始时的 {@link #isAnyoneFrenzied}；开启疯魔的玩家本人一定算在内。
     */
    public static boolean isAnyoneFrenziedWith(ServerPlayerEntity frenziedPlayer) {
        return isFrenzied(frenziedPlayer) || isAnyoneFrenzied(frenziedPlayer.getServerWorld());
    }

    /**
     * The pistol's last bullet was fired and the stack stays: NoellesRoles removes the stack without writing the
     * count, so the count is written here.
     * 猎魔枪打出了最后一发而物品堆保留：NoellesRoles 直接移除物品堆、不写回数量，因此在这里写回。
     */
    public static void emptyPistol(ItemStack pistol) {
        pistol.set(ModItems.BULLETS, 0);
    }

    /**
     * One bullet back into the pistol that just fired (still in the main hand: the shot is only accepted from
     * there, and {@link DemonHunterPistolRules#keepsEmptiedPistol} kept it).
     * 把一发子弹还给刚开过火的猎魔枪（仍在主手：只有主手的枪才能开火，且 {@link DemonHunterPistolRules#keepsEmptiedPistol}
     * 保住了它）。
     */
    public static void refundBullet(ServerPlayerEntity shooter) {
        ItemStack pistol = shooter.getMainHandStack();
        if (pistol.isOf(ModItems.DEMON_HUNTER_PISTOL)) {
            pistol.set(ModItems.BULLETS, pistol.getOrDefault(ModItems.BULLETS, 0) + 1);
        }
    }

    /**
     * Opens the window in which this player's pistols are not counted as a held gun, when the rule applies to this
     * ground pickup. True = opened; the caller must close it with {@link #endGunPickup}.
     * 规则适用于这次地面拾取时，打开“该玩家的猎魔枪不算持有的枪”的窗口。返回 true 表示已打开，调用方必须用
     * {@link #endGunPickup} 关闭。
     */
    public static boolean beginGunPickup(PlayerEntity player, ItemStack groundStack) {
        // The tag test goes first: every item a player touches comes through here. / 先判标签：玩家碰到的每个掉落物都会经过这里。
        if (player.getWorld().isClient || !groundStack.isIn(WatheItemTags.GUNS)) {
            return false;
        }
        if (!DemonHunterPistolRules.ignoresPistolForPickup(keepsPistol(player), true,
                groundStack.isOf(ModItems.DEMON_HUNTER_PISTOL))) {
            return false;
        }
        gunPickupBy = player;
        return true;
    }

    /** Closes the window of {@link #beginGunPickup}. / 关闭 {@link #beginGunPickup} 打开的窗口。 */
    public static void endGunPickup() {
        gunPickupBy = null;
    }

    /**
     * {@code PlayerInventory#contains(Predicate)} of {@code owner}: inside the window of {@link #beginGunPickup} the
     * test never sees a pistol, so Wathe's "already has a gun" (its {@code wathe:guns} inventory scan) answers for
     * the other guns only. Outside the window, and for every other player, the test is returned untouched.
     * {@code owner} 的 {@code PlayerInventory#contains(Predicate)}：在 {@link #beginGunPickup} 的窗口内，判定永远看不到猎魔枪，
     * 因此 Wathe 的“已经有枪”（它对 {@code wathe:guns} 的背包扫描）只针对其他枪作答。窗口之外以及其他任何玩家，判定原样返回。
     */
    public static Predicate<ItemStack> heldGunTest(PlayerEntity owner, Predicate<ItemStack> test) {
        if (owner == null || owner != gunPickupBy) {
            return test;
        }
        return exceptPistol(test);
    }

    /**
     * The test, never true for a Demon Hunter pistol. Also the client's "which gun did I just drop" test
     * ({@code GunDropKeepsDemonHunterPistolMixin}): the pistol is never the gun Wathe's punishment drops.
     * 同一个判定，但对猎魔枪永远为 false。也用于客户端“我刚丢下的是哪把枪”的判定
     * （{@code GunDropKeepsDemonHunterPistolMixin}）：Wathe 的惩罚丢下的枪从来不是猎魔枪。
     */
    public static Predicate<ItemStack> exceptPistol(Predicate<ItemStack> test) {
        return stack -> !stack.isOf(ModItems.DEMON_HUNTER_PISTOL) && test.test(stack);
    }

    private static void issuePistol(ServerPlayerEntity player) {
        if (DemonHunterPistolItem.findPistol(player) != null
                || player.currentScreenHandler.getCursorStack().isOf(ModItems.DEMON_HUNTER_PISTOL)) {
            return;
        }
        ItemStack pistol = new ItemStack(ModItems.DEMON_HUNTER_PISTOL);
        pistol.set(ModItems.BULLETS, DemonHunterPistolRules.ISSUED_BULLETS);
        // NoellesRoles' own way of handing the pistol out (first free hotbar slot). With no free slot nothing is
        // given, and NoellesRoles' frenzy-start branch for a hunter without a pistol applies.
        // 与 NoellesRoles 自己发枪的方式相同（第一个空的快捷栏格）。没有空格时不发，届时由 NoellesRoles 在疯魔开始时对无枪
        // 猎魔人的分支处理。
        ShopEntry.insertStackInFreeSlot(player, pistol);
    }

    private static void takePistol(ServerPlayerEntity player) {
        // Inventory and crafting grid, as NoellesRoles does at the round reset; the cursor is outside both.
        // 背包与合成格，与 NoellesRoles 在回合重置时的做法相同；光标不在两者之内。
        DemonHunterPistolItem.removePistol(player);
        ScreenHandler handler = player.currentScreenHandler;
        if (handler.getCursorStack().isOf(ModItems.DEMON_HUNTER_PISTOL)) {
            handler.setCursorStack(ItemStack.EMPTY);
        }
    }
}
