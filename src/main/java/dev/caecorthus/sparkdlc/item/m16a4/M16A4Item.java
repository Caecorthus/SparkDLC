package dev.caecorthus.sparkdlc.item.m16a4;

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
 * when a later pull starts with nothing left in it, and a reload spends what is left of it. One press therefore starts
 * one burst and never restarts itself while the button stays down. Firing is server-authoritative and main hand only;
 * the magazine and the burst window live in the stack's custom data, so a fresh rifle is always full, and every round
 * (the first of a pull included) waits for the shooter's own deadline ({@code M16A4Service}), whatever the client sends
 * and however many rifles it carries.
 * <p>
 * Left click reloads (the client sends the intent, see {@code M16A4ReloadInput}; the vanilla attack and block breaking
 * are swallowed for this item so the click never punches). A reload with rounds left is tactical and takes 2.2 s to a
 * full 31; an empty one takes 3 s and chambers 30. No round leaves while a reload runs.
 * M16A4：31 发弹匣、三发点射窗口的步枪。每次右键是一扣扳机，按住时以每秒 15 发最多打出
 * {@link M16A4Rules#BURST_SIZE} 发；提前松手不会补满窗口，只有下一次扣扳机时窗口已空才会重置，换弹会花掉窗口里剩下的部分。
 * 因此一次按下只开始一轮点射，按住时不会自己重新开始。开火为服务端权威且仅限主手；弹匣数与点射窗口存于物品的自定义数据，
 * 因此新枪总是满弹，并且无论客户端发送什么、携带多少把步枪，每一发（包括一次扣扳机的第一发）都要等到射手自己的截止时刻
 * （{@code M16A4Service}）。
 * <p>
 * 左键换弹（客户端发送意图，见 {@code M16A4ReloadInput}；本物品吞掉了原版攻击与方块破坏，这一下不会打人）。弹匣内还有
 * 子弹时为战术换弹，2.2 秒装满 31 发；空仓换弹 3 秒，装 30 发。换弹期间不出膛。
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

    public M16A4Item(Settings settings) {
        super(settings);
    }

    public static Settings createSettings() {
        return new Settings().maxCount(1);
    }

    /**
     * Right click: one trigger pull. Main hand only; a reload in progress, a refused gate or an empty magazine passes
     * nothing. The stack is put into the use state so the pull lasts while the button is down; the first round leaves
     * at once when the previous round's deadline has passed, else with the rest on {@link #usageTick}.
     * 右键：一次扣扳机。仅限主手；换弹中、门槛拒绝或空弹匣都不生效。物品进入使用状态，按住期间这次扣扳机一直有效；上一发的
     * 截止时刻已过时第一发立刻出膛，否则与其余各发一样由 {@link #usageTick} 接续。
     */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (hand != Hand.MAIN_HAND) {
            return TypedActionResult.pass(stack);
        }
        if (M16A4Service.isReloading(user)
                || M16A4Combat.decide(user, stack, hand) != M1903A4Rules.Decision.FIRE) {
            return TypedActionResult.fail(stack);
        }
        user.setCurrentHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity shooter) {
            M16A4Service.pull(shooter, stack);
            // A pull that starts with an empty window opens a new one; a carried-over window is spent first.
            // 窗口为空时这次扣扳机开新一轮；还有剩余就先花掉剩下的。
            if (getBurst(stack) <= 0) {
                setBurst(stack, M16A4Rules.BURST_SIZE);
            }
            fireWhenDue(shooter, stack);
        }
        return TypedActionResult.consume(stack);
    }

    /**
     * Cyclic fire: while the use state lasts, one more round whenever its deadline has passed, until the burst window
     * or the magazine runs out. The use state then stays (idle) until the button is released: ending it here would let
     * a held button start another burst on the very next tick. A reload likewise only pauses the pull. Vanilla keeps
     * the use state when the held button is carried to another stack of the same item, so only the stack the trigger
     * was pulled on fires here: a second rifle in the hotbar is no second burst.
     * 循环射击：使用状态期间，每当截止时刻已过就再出一发，直到点射窗口或弹匣耗尽。之后使用状态保持（空转）到松开按钮：在这里
     * 结束会让按住的按钮在下一刻就开始新一轮点射。换弹同样只是暂停这次扣扳机。按住按钮切换到同一物品的另一个物品堆时，原版会
     * 保留使用状态，因此这里只有被扣下扳机的那个物品堆开火：快捷栏里的第二把步枪不等于第二轮点射。
     */
    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient || !(user instanceof ServerPlayerEntity shooter)) {
            return;
        }
        if (!M16A4Service.holdsTrigger(shooter, stack) || M16A4Service.isReloading(shooter)
                || getAmmo(stack) <= 0 || getBurst(stack) <= 0) {
            return;
        }
        fireWhenDue(shooter, stack);
    }

    /** A bow release is not a shot for this rifle; only the burst timer fires. / 松手不算这一枪的开火；只有点射计时器开火。 */
    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
    }

    /**
     * The rifle keeps the bow item class for its long use state but reports no use action, so vanilla draws it in the
     * plain item branch (no bow draw, no bow sway): {@code M16A4AimPoseMixin} builds the fixed first-person aim frame
     * there and {@code M16A4ArmPoseMixin} supplies the third-person pose.
     * 步枪沿用弓类物品以获得长时间的使用状态，但不报告使用动作，原版按普通物品分支渲染（没有拉弓、没有弓的摆动）：
     * {@code M16A4AimPoseMixin} 在该分支构建固定的第一人称瞄准框架，第三人称姿势由 {@code M16A4ArmPoseMixin} 提供。
     */
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE;
    }

    /**
     * One round if the shooter's deadline has passed ({@link M16A4Rules#roundDue}), after a fresh gate: it spends the
     * round and the burst step in one custom-data write, sets the shooter's next deadline, plays the shot sound with
     * its muzzle flash, resolves the hit and tells the shooter's client to kick the view. A gate that refuses in the
     * middle of a pull (a stun, a seal on the rifle, a shooter who just died) spends the rest of the window instead,
     * so the held button fires nothing more once the gate opens again.
     * 射手的截止时刻已过（{@link M16A4Rules#roundDue}）时出一发，先重新过一次门槛：消耗子弹与点射计数（一次自定义数据
     * 写入）、设定射手的下一个截止时刻、播放枪声与枪口火光、结算命中，并通知射手客户端视角后坐。扣扳机途中门槛拒绝（眩晕、
     * 步枪被封印、射手刚刚死亡）时改为花掉窗口的剩余部分，因此门槛重新放行后按住的按钮也不会再出膛。
     */
    private static void fireWhenDue(ServerPlayerEntity shooter, ItemStack stack) {
        double now = shooter.getServerWorld().getTime();
        double deadline = M16A4Service.nextRoundAt(shooter);
        if (!M16A4Rules.roundDue(now, deadline)) {
            return;
        }
        if (M16A4Combat.decide(shooter, stack, Hand.MAIN_HAND) != M1903A4Rules.Decision.FIRE) {
            setBurst(stack, 0);
            return;
        }
        int ammo = getAmmo(stack);
        int burst = getBurst(stack);
        if (ammo <= 0 || burst <= 0) {
            return;
        }
        NbtCompound nbt = customData(stack);
        nbt.putInt(AMMO_KEY, ammo - 1);
        nbt.putInt(BURST_KEY, burst - 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        M16A4Service.setNextRoundAt(shooter, M16A4Rules.nextDeadline(now, deadline));
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
