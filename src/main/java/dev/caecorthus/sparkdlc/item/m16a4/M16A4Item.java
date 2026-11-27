package dev.caecorthus.sparkdlc.item.m16a4;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.SparkDlcSounds;
import dev.caecorthus.sparkdlc.item.m1903a4.M1903A4Rules;
import dev.doctor4t.wathe.index.WatheParticles;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * The M16A4: a 31-round rifle with a three-round burst window. Every right click is one trigger pull that fires up to
 * {@link M16A4Rules#BURST_SIZE} rounds at fifteen rounds a second; the window is not refilled by an early release, only
 * when a later pull starts with nothing left in it, and a reload refills it. One press therefore starts one burst and
 * never restarts itself while the button stays down. Firing is server-authoritative and main hand only; the burst, the
 * magazine and the next-shot tick live in the stack's custom data, so a fresh rifle is always full.
 * <p>
 * Left click reloads (the client sends the intent; {@code MinecraftClient#doAttack} and the block-breaking loop are
 * cancelled for this item so the click never punches). A reload with rounds left is tactical and takes 2.2 s to a full
 * 31; an empty one takes 3 s and chambers 30. Reloading cancels firing.
 * M16A4：31 发弹匣、三发点射窗口的步枪。每次右键是一扣扳机，按住时以每秒 15 发最多打出
 * {@link M16A4Rules#BURST_SIZE} 发；提前松手不会补满窗口，只有下一次扣扳机时窗口已空才会重置，换弹也会重置。因此一次按下
 * 只开始一轮点射，按住时不会自己重新开始。开火为服务端权威且仅限主手；点射数、弹匣数与下一发时刻存于物品的自定义数据，
 * 因此新枪总是满弹。
 * <p>
 * 左键换弹（客户端发送意图；本物品取消了 {@code MinecraftClient#doAttack} 与方块破坏循环，这一下不会打人）。弹匣内还有
 * 子弹时为战术换弹，2.2 秒装满 31 发；空仓换弹 3 秒，装 30 发。换弹会中断开火。
 */
public class M16A4Item extends BowItem {
    /** How far along the look vector the flash sits. / 枪口火光沿视线的距离。 */
    private static final double MUZZLE_DISTANCE = 1.05D;
    /** How far below eye level the flash sits, matching the levelled first-person pose. / 火光低于眼睛的高度，与平举的第一人称姿势一致。 */
    private static final double MUZZLE_DROP = 0.12D;
    /** How far to the right of the aim the flash sits; the rifle's muzzle is right of the crosshair. / 火光在瞄准线右侧的距离；枪口在准星右侧。 */
    private static final double MUZZLE_SIDE = 0.25D;

    private static final String AMMO_KEY = "Ammo";
    private static final String BURST_KEY = "Burst";
    private static final String NEXT_SHOT_KEY = "NextShotAt";

    public M16A4Item(Settings settings) {
        super(settings);
    }

    public static Settings createSettings() {
        return new Settings().maxCount(1);
    }

    /**
     * Right click: one trigger pull. Main hand only; a reload in progress, a refused gate or an empty magazine passes
     * nothing. The stack is put into the bow's use state so the third-person aim pose and the first-person aim hold last
     * while the burst runs; the first round leaves immediately and the rest follow on {@link #usageTick}.
     * 右键：一次扣扳机。仅限主手；换弹中、门槛拒绝或空弹匣都不生效。物品进入弓的使用状态，使第三人称瞄准姿势与第一人称
     * 瞄准握持在点射期间保持；第一发立刻出膛，其余由 {@link #usageTick} 接续。
     */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (hand != Hand.MAIN_HAND) {
            return TypedActionResult.pass(stack);
        }
        if (M16A4Service.isReloading(user)) {
            return TypedActionResult.fail(stack);
        }
        M1903A4Rules.Decision decision = M16A4Combat.decide(user, stack, hand);
        if (decision != M1903A4Rules.Decision.FIRE) {
            // Diagnostic: a refused trigger pull says why in the log. / 诊断：被拒绝的扣扳机在日志中留下原因。
            dev.caecorthus.sparkdlc.SparkDLC.LOGGER.info("[m16a4] refused {} client={} ammo={}",
                    decision, world.isClient, getAmmo(stack));
            return TypedActionResult.fail(stack);
        }
        user.setCurrentHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity shooter) {
            // A pull that starts with an empty window opens a new one; a carried-over window is spent first.
            // 窗口为空时这次扣扳机开新一轮；还有剩余就先花掉剩下的。
            if (getBurst(stack) <= 0) {
                setBurst(stack, M16A4Rules.BURST_SIZE);
            }
            fireOne(shooter, stack);
        }
        return TypedActionResult.consume(stack);
    }

    /**
     * Cyclic fire: every {@link M16A4Rules#TICKS_PER_SHOT} ticks while the use state lasts, one more round, until the
     * burst window or the magazine runs out. The use state then stays (idle) until the button is released, so a held
     * button cannot start another burst on its own.
     * 循环射击：使用状态期间每 {@link M16A4Rules#TICKS_PER_SHOT} 刻再出一发，直到点射窗口或弹匣耗尽。之后使用状态保持
     * （空转）到松开按钮，按住的按钮无法自行开始下一轮点射。
     */
    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient || !(user instanceof ServerPlayerEntity shooter)) {
            return;
        }
        // The use state stays until the button is released, even with the window or the magazine spent: ending it here
        // would let a held button start a new burst on the very next tick. A reload also only pauses firing; it does
        // not end the pull.
        // 即使点射窗口或弹匣已耗尽，使用状态也保留到松开按钮：在这里结束会让按住的按钮在下一刻就开始新一轮点射。换弹同样只是
        // 暂停射击，不结束这次扣扳机。
        if (M16A4Service.isReloading(shooter) || getAmmo(stack) <= 0 || getBurst(stack) <= 0) {
            return;
        }
        if (world.getTime() >= getNextShotAt(stack)) {
            fireOne(shooter, stack);
        }    }

    /** A bow release is not a shot for this rifle; only the burst timer fires. / 松手不算这一枪的开火；只有点射计时器开火。 */
    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
    }

    /**
     * The rifle keeps the bow item class for its use state but must not take the bow's rendering path: a partial draw
     * would fold a charge-dependent bow transform into the fixed aim frame, and the third-person pose is supplied by
     * {@code M16A4ArmPoseMixin} anyway. With {@code NONE} vanilla renders it in the plain item branch, where
     * {@code M16A4AimPoseMixin} builds the sniper rifle's fully-charged frame every tick.
     * 步枪沿用弓类物品以获得使用状态，但不能走弓的渲染路径：半拉会往固定的瞄准框架里折叠随蓄力变化的弓变换，而第三人称姿势
     * 已由 {@code M16A4ArmPoseMixin} 提供。返回 {@code NONE} 后原版按普通物品分支渲染，由 {@code M16A4AimPoseMixin}
     * 每帧构建狙击步枪满蓄力的框架。
     */
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE;
    }

    /**
     * One round, after a fresh gate: it spends the round and the burst step, writes the next-shot tick, plays the shot
     * sound with its muzzle flash, resolves the hit and tells the shooter's client to kick the view. A refused gate, an
     * emptied magazine or a spent window ends the use state instead.
     * 一发子弹，先重新过一次门槛：消耗子弹与点射计数、写入下一发时刻、播放枪声与枪口火光、结算命中，并通知射手客户端视角后坐。
     * 门槛拒绝、弹匣打空或窗口用尽时改为结束使用状态。
     */
    private static void fireOne(ServerPlayerEntity shooter, ItemStack stack) {
        if (M16A4Combat.decide(shooter, stack, Hand.MAIN_HAND) != M1903A4Rules.Decision.FIRE) {
            shooter.stopUsingItem();
            return;
        }        int ammo = getAmmo(stack);
        int burst = getBurst(stack);
        if (ammo <= 0 || burst <= 0) {
            shooter.stopUsingItem();
            return;
        }
        setAmmo(stack, ammo - 1);
        setBurst(stack, burst - 1);
        // Fractional tick schedule: 20/15 ticks per round keeps the average at fifteen rounds a second even though a
        // server tick can only fire once. A slipped tick is absorbed by taking the later of now and the old deadline.
        // 小数刻调度：每发 20/15 刻，使平均射速维持每秒 15 发（服务器每刻最多开一次火）。跳过的刻用“当前时刻与旧截止时刻取
        // 较晚者”吸收，不会累积漂移。
        double now = shooter.getServerWorld().getTime();
        setNextShotAt(stack, Math.max(now, getNextShotAt(stack)) + M16A4Rules.TICKS_PER_SHOT);
        dev.caecorthus.sparkdlc.SparkDLC.LOGGER.info("[m16a4] shot ammo={} burst={}", ammo - 1, burst - 1);
        Vec3d muzzle = muzzle(shooter);
        shooter.getServerWorld().spawnParticles(
                WatheParticles.GUNSHOT, muzzle.x, muzzle.y, muzzle.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        shooter.getServerWorld().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                SparkDlcSounds.M16A4_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        M16A4Combat.fire(shooter, stack);
        M16A4Service.notifyShot(shooter);
    }

    /** Muzzle flash point: along the aim, below eye level and right of the crosshair. / 枪口火光位置：沿瞄准方向、低于眼睛并在准星右侧。 */
    public static Vec3d muzzle(ServerPlayerEntity shooter) {
        Vec3d look = shooter.getRotationVec(1.0F);
        // Right of the look direction, level: (-z, 0, x) normalised in the horizontal plane.
        // 视线右侧、保持水平：水平面内的 (-z, 0, x) 归一化。
        Vec3d right = new Vec3d(-look.z, 0.0D, look.x);
        right = right.lengthSquared() > 1.0E-6D ? right.normalize() : new Vec3d(1.0D, 0.0D, 0.0D);
        return new Vec3d(
                shooter.getX() + look.x * MUZZLE_DISTANCE + right.x * MUZZLE_SIDE,
                shooter.getEyeY() - MUZZLE_DROP + look.y * MUZZLE_DISTANCE,
                shooter.getZ() + look.z * MUZZLE_DISTANCE + right.z * MUZZLE_SIDE
        );
    }

    /** Fabric seam: shooting only rewrites CUSTOM_DATA, so the held rifle never replays the equip animation. / 只改自定义数据，不重播换手动画。 */
    @Override
    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack,
                                                  ItemStack newStack) {
        return false;
    }

    public static int getAmmo(ItemStack stack) {
        NbtCompound nbt = customData(stack);
        return nbt.contains(AMMO_KEY)
                ? Math.max(0, Math.min(nbt.getInt(AMMO_KEY), M16A4Rules.MAGAZINE_SIZE))
                : M16A4Rules.MAGAZINE_SIZE;
    }

    public static void setAmmo(ItemStack stack, int ammo) {
        NbtCompound nbt = customData(stack);
        nbt.putInt(AMMO_KEY, Math.max(0, Math.min(ammo, M16A4Rules.MAGAZINE_SIZE)));
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public static int getBurst(ItemStack stack) {
        NbtCompound nbt = customData(stack);
        return nbt.contains(BURST_KEY)
                ? Math.max(0, Math.min(nbt.getInt(BURST_KEY), M16A4Rules.BURST_SIZE))
                : M16A4Rules.BURST_SIZE;
    }

    public static void setBurst(ItemStack stack, int burst) {
        NbtCompound nbt = customData(stack);
        nbt.putInt(BURST_KEY, Math.max(0, Math.min(burst, M16A4Rules.BURST_SIZE)));
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    private static double getNextShotAt(ItemStack stack) {
        NbtCompound nbt = customData(stack);
        return nbt.contains(NEXT_SHOT_KEY) ? nbt.getDouble(NEXT_SHOT_KEY) : 0.0D;
    }

    private static void setNextShotAt(ItemStack stack, double tick) {
        NbtCompound nbt = customData(stack);
        nbt.putDouble(NEXT_SHOT_KEY, tick);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    private static NbtCompound customData(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        return customData == null ? new NbtCompound() : customData.copyNbt();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.sparkdlc.m16a4.tooltip.ammo", getAmmo(stack), M16A4Rules.MAGAZINE_SIZE)
                .formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.sparkdlc.m16a4.tooltip.burst", getBurst(stack), M16A4Rules.BURST_SIZE)
                .formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.sparkdlc.m16a4.tooltip.use").formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
