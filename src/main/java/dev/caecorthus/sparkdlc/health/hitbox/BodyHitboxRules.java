package dev.caecorthus.sparkdlc.health.hitbox;

import dev.caecorthus.sparkdlc.health.BodyPart;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Optional;

/**
 * Pure geometry of the body-part hitboxes: no entity and no world, usable from both sides and from plain tests.
 *
 * <p>The seven boxes follow what a viewer sees, not the vanilla collision box: they have the proportions of the
 * vanilla player model (head 8x8x8, torso 8x12x4 split 6:4 into the chest above and the abdomen below, arms and legs
 * 4x12x4, in model pixels) at the size it is drawn, and they turn with the body. The arms therefore reach past the
 * 0.6-wide collision box, as they do on screen. They are boxes in the body's own {@link Frame}: x towards the
 * player's LEFT, y along the body from the soles to the crown, z towards the FRONT. A ray is moved into that frame
 * and tested there, so a turned or lying body costs nothing more than an upright one.</p>
 *
 * <p>These boxes never decide WHETHER a player is hit; that stays with each weapon's own targeting on the vanilla
 * box. They only answer WHICH part a hit landed on, so {@link #resolve} always names a part: the one the ray enters
 * first, or, when the ray passes beside every box (a weapon's aim margin, a corner of the collision box the model
 * does not fill), the one it passes closest to.</p>
 *
 * 部位命中箱的纯几何：不涉及实体与世界，两端与普通测试均可使用。
 *
 * <p>七个箱体依据观察者所见，而非原版碰撞箱：采用原版玩家模型的比例（头 8x8x8，躯干 8x12x4 并按 6:4 分为上方的胸部与下方的
 * 腹部，手臂与腿 4x12x4，单位为模型像素），按绘制尺寸取值，并随身体转动。因此手臂会像画面上那样伸出 0.6 宽的碰撞箱。
 * 箱体定义在身体自身的 {@link Frame} 中：x 指向玩家的左侧，y 沿身体从脚底指向头顶，z 指向正面。射线被变换到该坐标系中
 * 再检测，因此转身或躺下的身体与直立时开销相同。</p>
 *
 * <p>这些箱体从不决定玩家是否被命中；那仍由各武器自身针对原版碰撞箱的瞄准逻辑决定。它们只回答命中落在哪个部位，因此
 * {@link #resolve} 总会给出一个部位：射线最先进入的那个；若射线从所有箱体旁边掠过（武器的瞄准余量、模型未填满的碰撞箱
 * 角落），则给出离射线最近的那个。</p>
 */
public final class BodyHitboxRules {
    /**
     * Blocks per pixel of the player model drawn at scale 1: vanilla draws the 32-pixel model 1.875 blocks tall.
     * 以 1 倍缩放绘制的玩家模型每像素对应的方块数：原版把 32 像素的模型画成 1.875 格高。
     */
    public static final double MODEL_PIXEL = 0.9375 / 16.0;
    /** Blocks per pixel of the animal body of {@link BodyStance#ALL_FOURS}. / {@link BodyStance#ALL_FOURS} 动物身体每像素对应的方块数。 */
    public static final double ANIMAL_PIXEL = 1.0 / 16.0;

    // Model pixels. / 模型像素。
    private static final double HEAD_SIZE = 8.0;
    private static final double TORSO_HEIGHT = 12.0;
    /** Chest to abdomen is 6:4: the abdomen is the lower four tenths of the torso. / 胸部与腹部之比为 6:4：腹部是躯干下方的十分之四。 */
    private static final double ABDOMEN_SHARE = 4.0 / 10.0;
    private static final double LEG_LENGTH = 12.0;
    private static final double LIMB_WIDTH = 4.0;
    private static final double TORSO_HALF_WIDTH = 4.0;
    private static final double BODY_HALF_DEPTH = 2.0;
    /** The sneaking model lowers its head by this much on top of {@link #CROUCH_SINK}. / 潜行模型在 {@link #CROUCH_SINK} 之外再把头下移的量。 */
    private static final double CROUCH_DROP = 4.2;
    /**
     * Vanilla draws a sneaking player this many blocks (times its scale) lower as a whole; with {@link #CROUCH_DROP}
     * the crown lands on the 1.5-block sneaking box. / 原版把潜行玩家整体下移这么多格（乘以其缩放）；再加上
     * {@link #CROUCH_DROP}，头顶恰好落在 1.5 格高的潜行碰撞箱上。
     */
    private static final double CROUCH_SINK = 2.0 / 16.0;
    // The sneaking torso leans forward from the shoulders, which stay put: the hips and legs end up behind them.
    // 潜行时躯干自肩部前倾，肩部位置不变：髋部与双腿因而位于肩部后方。
    private static final double CROUCH_LEG_SETBACK = 4.0;
    private static final double CROUCH_ABDOMEN_SETBACK = 4.0;
    private static final double CROUCH_CHEST_SETBACK = 1.5;
    /** The riding model splays its legs: each foot ends this far outside the hip. / 骑乘模型双腿外张：脚尖比髋部向外偏出的量。 */
    private static final double SEATED_LEG_SPLAY = 4.0;

    // A Childish player is drawn as the vanilla baby biped scaled up to its box: head 4/3, body and limbs 8/9.
    // 幼稚玩家按原版幼体双足模型绘制并放大至其碰撞箱：头为 4/3，身体与四肢为 8/9。
    private static final double CHILD_HEAD_FACTOR = 4.0 / 3.0;
    private static final double CHILD_BODY_FACTOR = 8.0 / 9.0;

    // Vanilla draws a swimming or crawling model one block back along the body and 0.3 up; a sleeping one with its
    // soles (standing eye height - 0.1) behind the position. / 原版把游泳或爬行的模型沿身体后移一格并上移 0.3；睡觉的模型
    // 脚底位于位置后方（站立眼高 - 0.1）处。
    private static final double PRONE_BACK_SHIFT = 1.0;
    private static final double PRONE_LIFT = 0.3;
    private static final double STANDING_EYE_HEIGHT = 1.62;
    private static final double SUPINE_EYE_GAP = 0.1;

    private static final Vec3d UP = new Vec3d(0.0, 1.0, 0.0);
    private static final int NEAREST_SEARCH_STEPS = 48;

    private BodyHitboxRules() {
    }

    /** One part's box in the body frame. / 单个部位在身体坐标系中的箱体。 */
    public record PartBox(BodyPart part, Box box) {
    }

    /**
     * The body's own coordinate system in the world: {@code origin} is where the soles are, the three unit axes are
     * orthogonal, {@code left x axis = front}. / 身体自身坐标系在世界中的表示：{@code origin} 为脚底所在处，三条单位轴
     * 两两正交，且 {@code left x axis = front}。
     */
    public record Frame(Vec3d origin, Vec3d left, Vec3d axis, Vec3d front) {
        public Vec3d toLocal(Vec3d world) {
            Vec3d offset = world.subtract(origin);
            return new Vec3d(offset.dotProduct(left), offset.dotProduct(axis), offset.dotProduct(front));
        }

        public Vec3d toWorld(Vec3d local) {
            return toWorld(local.x, local.y, local.z);
        }

        public Vec3d toWorld(double x, double y, double z) {
            return new Vec3d(
                    origin.x + left.x * x + axis.x * y + front.x * z,
                    origin.y + left.y * x + axis.y * y + front.y * z,
                    origin.z + left.z * x + axis.z * y + front.z * z);
        }
    }

    /**
     * Where a ray struck a body. {@code direct}: the ray entered {@code part}'s box, {@code distanceSquared} is the
     * squared distance from the ray's start to the entry (0 when it starts inside) and {@code point} the entry.
     * Otherwise the ray entered no box, {@code part} is the nearest one, {@code distanceSquared} the squared gap
     * between ray and box and {@code point} the point of the ray nearest to it. {@code point} is in world space.
     * 射线击中身体的位置。{@code direct}：射线进入了 {@code part} 的箱体，{@code distanceSquared} 为射线起点到进入点的平方
     * 距离（起点在内为 0），{@code point} 为进入点。否则射线未进入任何箱体，{@code part} 为最近的部位，
     * {@code distanceSquared} 为射线与箱体间隙的平方，{@code point} 为射线上离它最近的点。{@code point} 为世界坐标。
     */
    public record Hit(BodyPart part, boolean direct, double distanceSquared, Vec3d point) {
        /**
         * Whether this hit should replace {@code other} as the answer for the same ray against another moment of the
         * same body: a direct hit beats a near miss, then the smaller distance wins; a tie keeps {@code other}.
         * 针对同一射线、同一身体的另一时刻，本次结果是否应取代 {@code other}：直接命中优先于擦边，其次距离更小者优先；
         * 相同则保留 {@code other}。
         */
        public boolean beats(Hit other) {
            if (direct != other.direct) {
                return direct;
            }
            return distanceSquared < other.distanceSquared;
        }
    }

    /** The frame of a pose. / 某个姿态的坐标系。 */
    public static Frame frame(BodyPose pose) {
        double yaw = Math.toRadians(pose.bodyYaw());
        Vec3d forward = new Vec3d(-Math.sin(yaw), 0.0, Math.cos(yaw));
        Vec3d left = new Vec3d(Math.cos(yaw), 0.0, Math.sin(yaw));
        double scale = scale(pose.scale());
        return switch (pose.stance()) {
            case STANDING, CROUCHING, SEATED, ALL_FOURS -> new Frame(pose.position(), left, UP, forward);
            case PRONE, GLIDING -> {
                // The body follows the look direction; pitch 0 is level, a positive pitch dips the head.
                // 身体沿视线方向；俯仰角为 0 时水平，为正时头部下沉。
                double pitch = Math.toRadians(pose.pitch());
                Vec3d axis = forward.multiply(Math.cos(pitch)).add(0.0, -Math.sin(pitch), 0.0);
                Vec3d back = forward.multiply(Math.sin(pitch)).add(0.0, Math.cos(pitch), 0.0);
                Vec3d origin = pose.stance() == BodyStance.PRONE
                        ? pose.position().add(axis.multiply(-PRONE_BACK_SHIFT * scale))
                                .add(back.multiply(PRONE_LIFT * scale))
                        : pose.position();
                yield new Frame(origin, left, axis, back.negate());
            }
            // Face up, so the player's left is on the other side than when standing with the same yaw.
            // 面朝上，因此玩家的左侧与以相同偏航角站立时相反。
            case SUPINE -> new Frame(
                    pose.position().subtract(forward.multiply(STANDING_EYE_HEIGHT * scale - SUPINE_EYE_GAP)),
                    left.negate(), forward, UP);
        };
    }

    /** The seven boxes of a pose, in its {@link #frame}, in {@link BodyPart} order. / 某个姿态在其 {@link #frame} 中的七个箱体，按 {@link BodyPart} 顺序。 */
    public static List<PartBox> boxes(BodyPose pose) {
        return boxes(pose.stance(), pose.child(), pose.scale());
    }

    /**
     * The seven boxes of a stance in the body frame, in {@link BodyPart} declaration order. A crouching body sinks as
     * a whole and lowers its head further: the head keeps its size, the torso and arms shorten, the legs lose what
     * sinks below the soles, and hips and legs move back under the leaning torso. A seated body keeps its trunk and
     * stretches its legs forward; their tops overlap the abdomen's height, where the earlier entry decides.
     * {@link BodyStance#ALL_FOURS} is drawn outside the entity scale and ignores {@code child} and {@code scale}.
     * 某种姿势在身体坐标系中的七个箱体，按 {@link BodyPart} 声明顺序。下蹲的身体整体下沉，头部再额外下移：头保持尺寸，
     * 躯干与手臂变短，腿失去沉到脚底以下的部分，髋部与双腿后移到前倾的躯干下方。坐姿保持躯干不变，双腿向前伸出；腿的
     * 上沿与腹部的高度有重叠，重叠处由先进入者决定。{@link BodyStance#ALL_FOURS} 的绘制不受实体缩放影响，忽略
     * {@code child} 与 {@code scale}。
     */
    public static List<PartBox> boxes(BodyStance stance, boolean child, double scale) {
        if (stance == BodyStance.ALL_FOURS) {
            return allFours();
        }
        double pixel = MODEL_PIXEL * scale(scale);
        double head = pixel * (child ? CHILD_HEAD_FACTOR : 1.0);
        double body = pixel * (child ? CHILD_BODY_FACTOR : 1.0);

        boolean crouching = stance == BodyStance.CROUCHING;
        double sink = crouching ? CROUCH_SINK * scale(scale) : 0.0;
        double hip = LEG_LENGTH * body - sink;
        double neck = (LEG_LENGTH + TORSO_HEIGHT - (crouching ? CROUCH_DROP : 0.0)) * body - sink;
        double waist = hip + (neck - hip) * ABDOMEN_SHARE;
        double crown = neck + HEAD_SIZE * head;
        double halfHead = HEAD_SIZE / 2.0 * head;
        double halfTorso = TORSO_HALF_WIDTH * body;
        double halfDepth = BODY_HALF_DEPTH * body;
        double limb = LIMB_WIDTH * body;

        Box leftLeg;
        Box rightLeg;
        if (stance == BodyStance.SEATED) {
            // Thighs level with the hips, feet forward and slightly apart. / 大腿与髋部齐平，双脚向前并略微分开。
            double reach = LEG_LENGTH * body;
            double outer = (LIMB_WIDTH + SEATED_LEG_SPLAY) * body;
            leftLeg = new Box(0.0, hip - limb, -halfDepth, outer, hip + limb / 2.0, reach);
            rightLeg = new Box(-outer, hip - limb, -halfDepth, 0.0, hip + limb / 2.0, reach);
        } else {
            double back = crouching ? CROUCH_LEG_SETBACK * body : 0.0;
            leftLeg = new Box(0.0, 0.0, -halfDepth - back, limb, hip, halfDepth - back);
            rightLeg = new Box(-limb, 0.0, -halfDepth - back, 0.0, hip, halfDepth - back);
        }
        double chestBack = crouching ? CROUCH_CHEST_SETBACK * body : 0.0;
        double abdomenBack = crouching ? CROUCH_ABDOMEN_SETBACK * body : 0.0;
        return List.of(
                new PartBox(BodyPart.HEAD, new Box(-halfHead, neck, -halfHead, halfHead, crown, halfHead)),
                new PartBox(BodyPart.CHEST, new Box(-halfTorso, waist, -halfDepth - chestBack, halfTorso, neck,
                        halfDepth - chestBack)),
                new PartBox(BodyPart.ABDOMEN, new Box(-halfTorso, hip, -halfDepth - abdomenBack, halfTorso, waist,
                        halfDepth - abdomenBack)),
                new PartBox(BodyPart.LEFT_ARM, new Box(halfTorso, hip, -halfDepth, halfTorso + limb, neck, halfDepth)),
                new PartBox(BodyPart.RIGHT_ARM, new Box(-halfTorso - limb, hip, -halfDepth, -halfTorso, neck, halfDepth)),
                new PartBox(BodyPart.LEFT_LEG, leftLeg),
                new PartBox(BodyPart.RIGHT_LEG, rightLeg));
    }

    /**
     * The vanilla pig model with the player's head where the Pig trait draws it: trunk 10x8x16, legs 4x6x4, the
     * 8x8x8 head above the front of the trunk. / 原版猪模型，玩家头颅位于猪词条绘制它的位置：躯干 10x8x16，腿 4x6x4，
     * 8x8x8 的头位于躯干前部上方。
     */
    private static List<PartBox> allFours() {
        double p = ANIMAL_PIXEL;
        return List.of(
                new PartBox(BodyPart.HEAD, new Box(-4 * p, 12 * p, 2 * p, 4 * p, 20 * p, 10 * p)),
                new PartBox(BodyPart.CHEST, new Box(-5 * p, 6 * p, 0.0, 5 * p, 14 * p, 8 * p)),
                new PartBox(BodyPart.ABDOMEN, new Box(-5 * p, 6 * p, -8 * p, 5 * p, 14 * p, 0.0)),
                new PartBox(BodyPart.LEFT_ARM, new Box(1 * p, 0.0, 3 * p, 5 * p, 6 * p, 7 * p)),
                new PartBox(BodyPart.RIGHT_ARM, new Box(-5 * p, 0.0, 3 * p, -1 * p, 6 * p, 7 * p)),
                new PartBox(BodyPart.LEFT_LEG, new Box(1 * p, 0.0, -9 * p, 5 * p, 6 * p, -5 * p)),
                new PartBox(BodyPart.RIGHT_LEG, new Box(-5 * p, 0.0, -9 * p, -1 * p, 6 * p, -5 * p)));
    }

    /** {@link #resolve(Frame, List, Vec3d, Vec3d)} on the frame and boxes of {@code pose}. / 对 {@code pose} 的坐标系与箱体执行 {@link #resolve(Frame, List, Vec3d, Vec3d)}。 */
    public static Hit resolve(BodyPose pose, Vec3d start, Vec3d end) {
        return resolve(frame(pose), boxes(pose), start, end);
    }

    /**
     * The part the world-space ray {@code start → end} strikes on a body. Never null and never a miss: see the class
     * comment. Among boxes entered at the same distance, and among equally near boxes, the earlier part in
     * {@code boxes} wins.
     * 世界坐标射线 {@code start → end} 击中身体的部位。永不为 null，也永不落空：见类注释。进入距离相同的箱体之间、以及
     * 同样近的箱体之间，{@code boxes} 中靠前的部位优先。
     */
    public static Hit resolve(Frame frame, List<PartBox> boxes, Vec3d start, Vec3d end) {
        Vec3d from = frame.toLocal(start);
        Vec3d to = frame.toLocal(end);

        BodyPart entered = null;
        double entrySquared = 0.0;
        Vec3d entryPoint = from;
        for (PartBox part : boxes) {
            double squared;
            Vec3d point;
            if (part.box().contains(from)) {
                squared = 0.0;
                point = from;
            } else {
                Optional<Vec3d> entry = part.box().raycast(from, to);
                if (entry.isEmpty()) {
                    continue;
                }
                point = entry.get();
                squared = from.squaredDistanceTo(point);
            }
            if (entered == null || squared < entrySquared) {
                entered = part.part();
                entrySquared = squared;
                entryPoint = point;
            }
        }
        if (entered != null) {
            return new Hit(entered, true, entrySquared, frame.toWorld(entryPoint));
        }

        BodyPart nearest = null;
        double gapSquared = 0.0;
        Vec3d nearestPoint = from;
        for (PartBox part : boxes) {
            double along = nearestAlong(from, to, part.box());
            Vec3d point = from.lerp(to, along);
            double squared = gapSquared(point, part.box());
            if (nearest == null || squared < gapSquared) {
                nearest = part.part();
                gapSquared = squared;
                nearestPoint = point;
            }
        }
        if (nearest == null) {
            throw new IllegalArgumentException("a body has no part boxes");
        }
        return new Hit(nearest, false, gapSquared, frame.toWorld(nearestPoint));
    }

    /**
     * Squared distance from a point to a box, 0 inside it. / 点到箱体的平方距离，在箱体内为 0。
     */
    static double gapSquared(Vec3d point, Box box) {
        double x = Math.max(Math.max(box.minX - point.x, point.x - box.maxX), 0.0);
        double y = Math.max(Math.max(box.minY - point.y, point.y - box.maxY), 0.0);
        double z = Math.max(Math.max(box.minZ - point.z, point.z - box.maxZ), 0.0);
        return x * x + y * y + z * z;
    }

    /**
     * The fraction {@code 0..1} of the segment at which it is nearest to the box. The distance from a point moving
     * along a line to a convex box is convex in the fraction, so a ternary search finds it.
     * 线段上离箱体最近处所对应的比例 {@code 0..1}。沿直线移动的点到凸箱体的距离是该比例的凸函数，因此可用三分法求得。
     */
    private static double nearestAlong(Vec3d from, Vec3d to, Box box) {
        double low = 0.0;
        double high = 1.0;
        for (int step = 0; step < NEAREST_SEARCH_STEPS; step++) {
            double third = (high - low) / 3.0;
            double a = low + third;
            double b = high - third;
            if (gapSquared(from.lerp(to, a), box) <= gapSquared(from.lerp(to, b), box)) {
                high = b;
            } else {
                low = a;
            }
        }
        return (low + high) / 2.0;
    }

    private static double scale(double scale) {
        return Double.isFinite(scale) && scale > 0.0 ? scale : 1.0;
    }
}
