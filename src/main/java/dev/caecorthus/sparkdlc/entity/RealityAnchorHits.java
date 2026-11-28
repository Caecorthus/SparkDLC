package dev.caecorthus.sparkdlc.entity;

import dev.caecorthus.sparkdlc.role.civilian.seeker.hit.SeekerDamageRules;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Destruction seam for placed reality stabilization anchors: every break ends in {@link RealityAnchorEntity#destroy()}.
 * A left-click breaks one ({@link #onMelee}). Every firearm that fires a server-side ray (Wathe's revolver family, the
 * Bandit revolver, the serial killer's psycho pistols, the Savior pistol, the double barrel, the M1903A4 and the
 * M16A4) offers its line to {@link #gunShot} or {@link #destroyAlongRay}; Wathe grenades call {@link #onBlast}. The
 * anchor never
 * blocks the shot itself and nothing here touches a player: the weapon's own resolution runs unchanged. An anchor is
 * hit only when it is the first thing on the line, in front of the first colliding block and of what the weapon hit.
 * 现实稳定锚被摧毁的接缝：每次摧毁都汇入 {@link RealityAnchorEntity#destroy()}。左键可打掉它（{@link #onMelee}）。所有服务端
 * 射线的枪械（Wathe 左轮系、强盗左轮、连环杀手疯魔双枪、救世主手枪、双管猎枪、M1903A4 与 M16A4）都把弹道交给
 * {@link #gunShot} 或
 * {@link #destroyAlongRay}；Wathe 手雷调用 {@link #onBlast}。锚不会拦截子弹，这里也不触碰任何玩家：武器自身的判定照常执行。
 * 只有当锚是弹道上的第一个东西（位于第一个阻挡方块以及武器命中的目标之前）时才会被击中。
 */
public final class RealityAnchorHits {
    /** Extra margin around the anchor's own box, so a shot that grazes the machine still counts. / 锚包围盒的额外余量。 */
    private static final double HIT_MARGIN = 0.15D;
    /**
     * The gun receivers' plain target cap. A trait that widens it for one shot (Niko: 300 blocks) widens the
     * shooter's client pick range with it, so the bullet then flies that far.
     * 枪械接收器原本的目标上限。词条为某一枪放宽它时（Niko：300 格）也同时放宽了射手客户端的选靶距离，因此子弹会飞那么远。
     */
    public static final double GUN_TARGET_CAP = 65.0D;
    /** Client pick range of Wathe's {@code RevolverItem.getGunTarget}. / Wathe {@code RevolverItem.getGunTarget} 的客户端选靶距离。 */
    public static final double WATHE_REVOLVER_RANGE = 30.0D;
    private static boolean registered;

    private RealityAnchorHits() {
    }

    /**
     * {@link AttackEntityCallback} in the default phase, registered once from common code and therefore firing on both
     * sides. The stun, hypnosis, session-lock and duel guards of the mod sit in earlier phases and refuse first; every
     * target that is not an anchor passes untouched.
     * 默认阶段的 {@link AttackEntityCallback}，由通用代码注册一次，因此两端都会触发。本模组的眩晕、催眠、会话锁定与决斗拦截
     * 位于更早的阶段，会先行拒绝；所有不是锚的目标原样放行。
     */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        AttackEntityCallback.EVENT.register(RealityAnchorHits::onAttack);
    }

    /**
     * Left-click on an anchor. Fabric fires this at {@code ServerPlayerEntity#attack} HEAD, on the server thread and
     * before Wathe's player-only attack wrapper, which is why an adventure-mode punch reaches a non-player entity at
     * all. The client answers SUCCESS so Fabric sends the attack packet itself (a later listener, such as the
     * shuriken's "FAIL on any entity", would otherwise swallow it). Spectators pass (vanilla spectate).
     * 左键点击锚。Fabric 在 {@code ServerPlayerEntity#attack} HEAD 触发本回调，位于服务端线程上且早于 Wathe 只认玩家的攻击
     * 包装，冒险模式的一拳因此才打得到非玩家实体。客户端返回 SUCCESS，让 Fabric 自行发送攻击包（否则后续监听器如手里剑
     * “对任何实体返回 FAIL”会吞掉它）。旁观者放行（原版观战）。
     */
    static ActionResult onAttack(PlayerEntity player, World world, Hand hand, Entity entity,
                                 @Nullable EntityHitResult hitResult) {
        if (!(entity instanceof RealityAnchorEntity anchor) || player == null || player.isSpectator()) {
            return ActionResult.PASS;
        }
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        return player instanceof ServerPlayerEntity attacker && onMelee(attacker, anchor)
                ? ActionResult.SUCCESS : ActionResult.FAIL;
    }

    /**
     * Any non-spectating player's punch clears the anchor. Vanilla already checked reach
     * ({@code canInteractWithEntityIn}) but never line of sight, so a forged attack packet through a wall is refused.
     * 任何非旁观玩家的一拳都会清掉锚。原版已检查距离（{@code canInteractWithEntityIn}），但从不检查视线，因此隔墙伪造的
     * 攻击包会被拒绝。
     */
    public static boolean onMelee(ServerPlayerEntity attacker, RealityAnchorEntity anchor) {
        if (attacker.isSpectator() || anchor.isRemoved() || anchor.getWorld() != attacker.getWorld()
                || !SeekerDamageRules.hasLineOfSight(attacker.getWorld(), attacker.getEyePos(),
                anchor.getBoundingBox(), attacker)) {
            return false;
        }
        return anchor.destroy();
    }

    /**
     * A client-targeted gun shot from {@code shooter}'s eyes along the server rotation, {@code range} blocks far. It
     * ends where it enters {@code hit}, the entity the weapon resolved or the client claimed (a player, a device, a
     * puppet), so an anchor behind what was shot stays.
     * 由客户端选靶的一枪：自 {@code shooter} 的眼睛沿服务端朝向射出 {@code range} 格。它在进入 {@code hit}（武器结算出的或
     * 客户端声明的实体：玩家、设备、皮套）处结束，因此被击中目标身后的锚不受影响。
     */
    public static boolean gunShot(ServerPlayerEntity shooter, @Nullable Entity hit, double range) {
        return gunShot(shooter, hit, range, GUN_TARGET_CAP);
    }

    /**
     * {@link #gunShot(ServerPlayerEntity, Entity, double)} with the target cap the receiver really used for this shot
     * ({@code SparkTraitsApi.getServerGunTargetRange}: widen-only), so a widened shot is followed that far.
     * 带上接收器对这一枪实际使用的目标上限（{@code SparkTraitsApi.getServerGunTargetRange}：只放宽）的
     * {@link #gunShot(ServerPlayerEntity, Entity, double)}，被放宽的一枪会按放宽后的距离跟随。
     */
    public static boolean gunShot(ServerPlayerEntity shooter, @Nullable Entity hit, double range, double targetCap) {
        Vec3d eye = shooter.getEyePos();
        Vec3d look = shooter.getRotationVec(1.0F);
        double hitDistance = Double.POSITIVE_INFINITY;
        if (hit != null && hit != shooter && !hit.isSpectator() && !(hit instanceof RealityAnchorEntity)) {
            // Where the server's look ray enters the target; when it misses the server-side box (the client aimed at
            // its own, slightly older picture), the nearest point of that box.
            // 服务端视线进入目标的位置；视线没有碰到服务端包围盒时（客户端瞄的是自己稍旧的画面），取该包围盒上最近的点。
            Box box = hit.getBoundingBox();
            double nearest = Math.sqrt(SeekerDamageRules.squaredDistanceToBox(eye, box));
            hitDistance = nearest <= 0.0D ? 0.0D
                    : box.raycast(eye, eye.add(look.multiply(gunReach(range, targetCap, Double.POSITIVE_INFINITY))))
                    .map(eye::distanceTo).orElse(nearest);
        }
        double reach = gunReach(range, targetCap, hitDistance);
        return reach > 0.0D
                && destroyAlongRay(shooter.getServerWorld(), shooter, eye, eye.add(look.multiply(reach)));
    }

    /**
     * Pure rule: how far a client-targeted shot is followed. {@code range} is the gun's client pick range; a target
     * cap widened above {@link #GUN_TARGET_CAP} replaces it when larger; the shot never goes past {@code hitDistance},
     * the distance to what it hit (infinite for a miss). 0 for a value that is not a usable distance.
     * 纯规则：由客户端选靶的一枪被跟随多远。{@code range} 是该枪的客户端选靶距离；目标上限被放宽到 {@link #GUN_TARGET_CAP}
     * 以上且更大时取代它；这一枪绝不会越过 {@code hitDistance}（到被击中目标的距离，未命中为无穷大）。数值不是可用距离时为 0。
     */
    static double gunReach(double range, double targetCap, double hitDistance) {
        double reach = Double.isFinite(targetCap) && targetCap > GUN_TARGET_CAP ? Math.max(range, targetCap) : range;
        if (!Double.isNaN(hitDistance)) {
            reach = Math.min(reach, Math.max(0.0D, hitDistance));
        }
        return Double.isFinite(reach) && reach > 0.0D ? reach : 0.0D;
    }

    /**
     * Removes the nearest anchor on the segment {@code from..to} in front of the first colliding block, if any.
     * {@code from} is normally the shooter's eye and {@code to} where that weapon's bullet stops.
     * 移除 {@code from..to} 线段上、第一个阻挡方块之前最近的锚；{@code from} 通常是射手眼睛，{@code to} 是该武器子弹停下的位置。
     */
    public static boolean destroyAlongRay(ServerWorld world, @Nullable Entity shooter, Vec3d from, Vec3d to) {
        if (!(from.squaredDistanceTo(to) > 1.0E-8D)) {
            return false;
        }
        BlockHitResult block = world.raycast(shooter == null
                ? new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,
                ShapeContext.absent())
                : new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,
                shooter));
        Vec3d end = block.getType() == HitResult.Type.MISS ? to : block.getPos();

        RealityAnchorEntity nearest = null;
        double nearestDistance = from.squaredDistanceTo(end);
        Box bounds = new Box(from, end).expand(1.0D);
        for (RealityAnchorEntity anchor
                : world.getEntitiesByClass(RealityAnchorEntity.class, bounds, entity -> !entity.isRemoved())) {
            Box box = anchor.getBoundingBox().expand(HIT_MARGIN);
            double distance = box.contains(from) ? 0.0D
                    : box.raycast(from, end).map(from::squaredDistanceTo).orElse(Double.POSITIVE_INFINITY);
            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearest = anchor;
            }
        }
        return nearest != null && nearest.destroy();
    }

    /**
     * A blast of {@code radius} blocks around {@code centre}: an area effect, so every anchor in the sphere with a
     * clear COLLIDER segment from the centre to any sample point of its box breaks. This is the rule the mod's other
     * placed devices follow for the same grenade; an anchor behind a wall survives.
     * 以 {@code centre} 为中心、半径 {@code radius} 格的爆炸：属于范围效果，球内且从爆心到其包围盒任一采样点有畅通 COLLIDER
     * 线段的锚都会被炸掉。本模组其他放置类装置对同一颗手雷遵循同样的规则；墙后的锚不受影响。
     */
    public static int onBlast(ServerWorld world, Vec3d centre, double radius, @Nullable Entity context) {
        if (!(radius > 0.0D) || !Double.isFinite(radius)) {
            return 0;
        }
        List<RealityAnchorEntity> caught = world.getEntitiesByClass(RealityAnchorEntity.class,
                new Box(centre, centre).expand(radius + 1.0D),
                anchor -> !anchor.isRemoved()
                        && SeekerDamageRules.squaredDistanceToBox(centre, anchor.getBoundingBox()) <= radius * radius
                        && SeekerDamageRules.hasLineOfSight(world, centre, anchor.getBoundingBox(), context));
        int removed = 0;
        for (RealityAnchorEntity anchor : caught) {
            if (anchor.destroy()) {
                removed++;
            }
        }
        return removed;
    }
}
