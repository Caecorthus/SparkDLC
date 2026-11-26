package dev.caecorthus.sparkdlc.gametest;

import dev.caecorthus.sparkdlc.health.BodyPart;
import dev.caecorthus.sparkdlc.health.hitbox.BodyHitboxRules;
import dev.caecorthus.sparkdlc.health.hitbox.BodyHitboxes;
import dev.caecorthus.sparkdlc.health.hitbox.BodyPartHit;
import dev.caecorthus.sparkdlc.health.hitbox.BodyPose;
import dev.caecorthus.sparkdlc.health.hitbox.BodyStance;
import dev.caecorthus.sparkdlc.health.hitbox.BodyYawRules;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.List;

/**
 * The body-part hitboxes: the seven boxes of every stance, the part a ray names on a standing, turned and lying
 * body, the body yaw recomputed from movement, and the same answers read from a real player entity. The expected
 * numbers come from the vanilla player model (32 px drawn 1.875 blocks tall) and the Minecraft yaw convention
 * (yaw 0 faces +Z, yaw 90 faces -X, a player facing +Z has its left hand towards +X), not from the code under test.
 * 部位命中箱：每种姿势的七个箱体，射线在站立、转身与躺卧的身体上给出的部位，依据位移重新推算的身体偏航角，以及从真实玩家
 * 实体读出的相同结果。期望数值来自原版玩家模型（32 像素绘制为 1.875 格高）与 Minecraft 的偏航角约定（偏航角 0 朝 +Z，
 * 90 朝 -X，面朝 +Z 的玩家左手在 +X 一侧），而非被测代码。
 */
public final class BodyHitboxGameTest implements FabricGameTest {
    /** One pixel of the player model at scale 1. / 1 倍缩放下玩家模型的一个像素。 */
    private static final double PX = 1.875 / 32.0;
    private static final double EPS = 1.0e-9;
    /**
     * For values that went through a non-cardinal rotation or the nearest-point search.
     * 用于经过非正交旋转或最近点搜索的数值。
     */
    private static final double NEAR = 1.0e-6;
    /** How far in front of and behind the body a test ray starts and ends. / 测试射线在身体前后起止的距离。 */
    private static final double REACH = 3.0;

    private record Ray(Vec3d start, Vec3d end) {
        Ray reversed() {
            return new Ray(end, start);
        }
    }

    /**
     * A point on the body to aim at: sideways towards the player's left, and up from the soles.
     * 身体上的瞄准点：朝玩家左侧的横向偏移，以及距脚底的高度。
     */
    private record Aim(String name, double left, double up, BodyPart part) {
    }

    private static final List<Aim> STANDING_AIMS = List.of(
            new Aim("head", 0.0, 28 * PX, BodyPart.HEAD),
            new Aim("chest", 0.0, 20 * PX, BodyPart.CHEST),
            new Aim("abdomen", 0.0, 14.5 * PX, BodyPart.ABDOMEN),
            new Aim("left arm", 6 * PX, 18 * PX, BodyPart.LEFT_ARM),
            new Aim("right arm", -6 * PX, 18 * PX, BodyPart.RIGHT_ARM),
            new Aim("left leg", 2 * PX, 6 * PX, BodyPart.LEFT_LEG),
            new Aim("right leg", -2 * PX, 6 * PX, BodyPart.RIGHT_LEG));

    /**
     * Every stance has seven non-empty boxes in {@link BodyPart} order with the left parts on positive x, mirrored by
     * the right ones. Standing at scale 1 they are the player model: crown 1.875, neck 1.40625, hip 0.703125, soles
     * 0, the abdomen the lower four tenths (4.8 px) of the torso, arms out to 0.46875. Scale 0.5 halves every coordinate (the animal
     * body ignores scale), and a child keeps the total height with a head one third of it.
     * 每种姿势都有七个非空箱体，按 {@link BodyPart} 顺序排列，左侧部位位于 x 正向，并与右侧部位镜像对称。1 倍缩放站立时即为
     * 玩家模型：头顶 1.875，颈部 1.40625，髋部 0.703125，脚底 0，腹部为躯干下方的十分之四（4.8 像素），手臂外沿到 0.46875。0.5 倍缩放使
     * 所有坐标减半（动物身体忽略缩放）；幼体总高度不变，头占其三分之一。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void theBoxesOfEveryStanceFollowThePlayerModel(TestContext context) {
        for (BodyStance stance : BodyStance.values()) {
            List<BodyHitboxRules.PartBox> boxes = BodyHitboxRules.boxes(stance, false, 1.0);
            check(boxes.size() == BodyPart.values().length, stance + " has " + boxes.size() + " boxes");
            for (BodyPart part : BodyPart.values()) {
                BodyHitboxRules.PartBox box = boxes.get(part.ordinal());
                check(box.part() == part, stance + ": box " + part.ordinal() + " is " + box.part());
                check(box.box().getLengthX() > 0 && box.box().getLengthY() > 0 && box.box().getLengthZ() > 0,
                        stance + ": " + part.id() + " is empty: " + box.box());
            }
            checkMirrored(stance, box(boxes, BodyPart.LEFT_ARM), box(boxes, BodyPart.RIGHT_ARM));
            checkMirrored(stance, box(boxes, BodyPart.LEFT_LEG), box(boxes, BodyPart.RIGHT_LEG));
        }

        List<BodyHitboxRules.PartBox> standing = BodyHitboxRules.boxes(BodyStance.STANDING, false, 1.0);
        Box head = box(standing, BodyPart.HEAD);
        Box chest = box(standing, BodyPart.CHEST);
        Box abdomen = box(standing, BodyPart.ABDOMEN);
        Box leftArm = box(standing, BodyPart.LEFT_ARM);
        Box rightArm = box(standing, BodyPart.RIGHT_ARM);
        Box leftLeg = box(standing, BodyPart.LEFT_LEG);
        Box rightLeg = box(standing, BodyPart.RIGHT_LEG);
        checkNear(head.maxY, 1.875, "standing crown");
        checkNear(head.minY, 1.40625, "standing head bottom");
        checkNear(head.minX, -4 * PX, "standing head right face");
        checkNear(head.maxX, 4 * PX, "standing head left face");
        checkNear(head.minZ, -4 * PX, "standing head back face");
        checkNear(head.maxZ, 4 * PX, "standing head front face");
        checkNear(chest.maxY, 1.40625, "standing chest top (neck)");
        checkNear(chest.minY, 16.8 * PX, "standing chest bottom (4.8 px above the hip)");
        checkNear(abdomen.maxY, 16.8 * PX, "standing abdomen top");
        checkNear(chest.getLengthY() * 4.0, abdomen.getLengthY() * 6.0, "chest to abdomen is 6:4");
        checkNear(abdomen.minY, 0.703125, "standing abdomen bottom (hip)");
        check(chest.minY >= abdomen.maxY - EPS, "the chest sits above the abdomen: " + chest + " / " + abdomen);
        for (Box torso : List.of(chest, abdomen)) {
            checkNear(torso.minX, -4 * PX, "standing torso right side");
            checkNear(torso.maxX, 4 * PX, "standing torso left side");
        }
        checkNear(leftArm.minX, 4 * PX, "standing left arm inner side");
        checkNear(leftArm.maxX, 0.46875, "standing left arm outer side");
        checkNear(rightArm.minX, -0.46875, "standing right arm outer side");
        checkNear(leftArm.maxY, 1.40625, "standing shoulder");
        checkNear(leftArm.minY, 0.703125, "standing hand");
        checkNear(leftLeg.minY, 0.0, "standing left sole");
        checkNear(rightLeg.minY, 0.0, "standing right sole");
        checkNear(leftLeg.maxY, 0.703125, "standing left leg top (hip)");
        checkNear(leftLeg.minX, 0.0, "standing left leg inner side");
        checkNear(leftLeg.maxX, 4 * PX, "standing left leg outer side");

        for (BodyStance stance : BodyStance.values()) {
            List<BodyHitboxRules.PartBox> full = BodyHitboxRules.boxes(stance, false, 1.0);
            List<BodyHitboxRules.PartBox> half = BodyHitboxRules.boxes(stance, false, 0.5);
            double factor = stance == BodyStance.ALL_FOURS ? 1.0 : 0.5;
            for (BodyPart part : BodyPart.values()) {
                checkSameBox(box(half, part), scaled(box(full, part), factor),
                        stance + " " + part.id() + " at scale 0.5");
            }
        }

        List<BodyHitboxRules.PartBox> child = BodyHitboxRules.boxes(BodyStance.STANDING, true, 1.0);
        Box childHead = box(child, BodyPart.HEAD);
        checkNear(childHead.maxY, 1.875, "child crown");
        checkNear(childHead.getLengthY(), 1.875 / 3.0, "child head height");
        checkNear(box(child, BodyPart.LEFT_LEG).minY, 0.0, "child sole");
        checkNear(box(child, BodyPart.CHEST).maxY, childHead.minY, "child neck");
        context.complete();
    }

    /**
     * Crouching: the whole body sinks 2/16 block and the head a further 4.2 px, so the crown lands on the 1.5-block
     * sneaking box and the hip at 0.578125; the head keeps its 8 px, the legs still start at 0, and legs and abdomen
     * sit 4 px, the chest 1.5 px, behind the shoulders. Seated: the trunk is the standing one, the legs reach 12 px
     * forward, from 4 px below to 2 px above the hip; in that overlap a shot from behind is the abdomen's, one from
     * the front a leg's. All fours: head in front, chest ahead of the abdomen, hind legs behind the front legs.
     * 下蹲：整个身体下沉 2/16 格，头部再下移 4.2 像素，因此头顶落在 1.5 格高的潜行碰撞箱上，髋部位于 0.578125；头保持 8
     * 像素，腿仍从 0 起，腿与腹部位于肩部后方 4 像素，胸部在后方 1.5 像素。坐姿：躯干与站立时相同，双腿向前伸出 12 像素，
     * 上下范围为髋部以下 4 像素到以上 2 像素；在该重叠高度，从背后射来的属于腹部，从正面射来的属于腿。四足：头在前，胸在
     * 腹之前，后腿在前腿之后。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void crouchingSeatedAndAllFoursRearrangeTheBoxes(TestContext context) {
        List<BodyHitboxRules.PartBox> standing = BodyHitboxRules.boxes(BodyStance.STANDING, false, 1.0);

        List<BodyHitboxRules.PartBox> crouching = BodyHitboxRules.boxes(BodyStance.CROUCHING, false, 1.0);
        Box head = box(crouching, BodyPart.HEAD);
        check(Math.abs(head.maxY - 1.5) < 0.01, "the crouching crown lands on the 1.5 sneaking box: " + head.maxY);
        checkNear(head.maxY, (32 - 4.2) * PX - 0.125, "crouching crown");
        check(head.maxY < box(standing, BodyPart.HEAD).maxY, "crouching lowers the crown: " + head.maxY);
        checkNear(head.getLengthX(), 8 * PX, "crouching head width");
        checkNear(head.getLengthY(), 8 * PX, "crouching head height");
        checkNear(head.getLengthZ(), 8 * PX, "crouching head depth");
        checkNear(head.getCenter().z, 0.0, "crouching head centre z");
        checkNear(box(crouching, BodyPart.ABDOMEN).minY, 0.578125, "crouching hip");
        checkNear(box(crouching, BodyPart.CHEST).maxY, head.minY, "crouching neck");
        checkNear(box(crouching, BodyPart.CHEST).getCenter().z, -1.5 * PX, "crouching chest centre z");
        checkNear(box(crouching, BodyPart.ABDOMEN).getCenter().z, -4 * PX, "crouching abdomen centre z");
        for (BodyPart leg : List.of(BodyPart.LEFT_LEG, BodyPart.RIGHT_LEG)) {
            Box box = box(crouching, leg);
            checkNear(box.minY, 0.0, "crouching " + leg.id() + " sole");
            checkNear(box.maxY, 0.578125, "crouching " + leg.id() + " top (hip)");
            checkNear(box.getCenter().z, -4 * PX, "crouching " + leg.id() + " centre z");
            check(box.getCenter().z < head.getCenter().z, "crouching " + leg.id() + " sits behind the shoulders");
        }

        List<BodyHitboxRules.PartBox> seated = BodyHitboxRules.boxes(BodyStance.SEATED, false, 1.0);
        for (BodyPart trunk : List.of(BodyPart.HEAD, BodyPart.CHEST, BodyPart.ABDOMEN, BodyPart.LEFT_ARM,
                BodyPart.RIGHT_ARM)) {
            checkSameBox(box(seated, trunk), box(standing, trunk), "seated " + trunk.id());
        }
        double hip = box(seated, BodyPart.ABDOMEN).minY;
        checkNear(hip, 12 * PX, "seated hip");
        for (BodyPart leg : List.of(BodyPart.LEFT_LEG, BodyPart.RIGHT_LEG)) {
            Box box = box(seated, leg);
            checkNear(box.maxZ, 12 * PX, "seated " + leg.id() + " reach");
            check(box.maxZ > box(seated, BodyPart.CHEST).maxZ && box.maxZ > box(seated, BodyPart.ABDOMEN).maxZ,
                    "seated " + leg.id() + " reaches forward of the trunk: " + box);
            checkNear(box.maxY, hip + 2 * PX, "seated " + leg.id() + " top");
            checkNear(box.minY, hip - 4 * PX, "seated " + leg.id() + " bottom");
        }
        Vec3d feet = new Vec3d(4.5, 80.0, 9.25);
        BodyPose seatedPose = new BodyPose(BodyStance.SEATED, false, feet, 0.0F, 0.0F, 1.0);
        for (Aim aim : List.of(new Aim("left thigh", 2 * PX, 13 * PX, BodyPart.LEFT_LEG),
                new Aim("right thigh", -2 * PX, 13 * PX, BodyPart.RIGHT_LEG))) {
            Ray front = frontRay(feet, 0, aim.left(), aim.up());
            BodyHitboxRules.Hit fromFront = resolve(seatedPose, front);
            checkDirect(fromFront, aim.part(), "seated, from the front at the " + aim.name());
            checkNear(fromFront.distanceSquared(), square(REACH - 12 * PX), NEAR, "seated front entry distance");
            checkDirect(resolve(seatedPose, front.reversed()), BodyPart.ABDOMEN,
                    "seated, from behind at the " + aim.name());
        }

        List<BodyHitboxRules.PartBox> allFours = BodyHitboxRules.boxes(BodyStance.ALL_FOURS, false, 1.0);
        Box pigHead = box(allFours, BodyPart.HEAD);
        check(pigHead.getCenter().z > 0.0, "the head of a body on all fours is in front: " + pigHead);
        check(pigHead.maxZ >= box(allFours, BodyPart.CHEST).maxZ - EPS, "nothing of the trunk is ahead of the head");
        check(box(allFours, BodyPart.CHEST).minZ >= box(allFours, BodyPart.ABDOMEN).maxZ - EPS,
                "on all fours the chest is the front half of the trunk");
        check(box(allFours, BodyPart.LEFT_LEG).maxZ <= box(allFours, BodyPart.LEFT_ARM).minZ + EPS
                        && box(allFours, BodyPart.RIGHT_LEG).maxZ <= box(allFours, BodyPart.RIGHT_ARM).minZ + EPS,
                "on all fours the hind legs (the legs) are behind the front legs (the arms)");
        for (BodyPart part : BodyPart.values()) {
            checkSameBox(box(BodyHitboxRules.boxes(BodyStance.ALL_FOURS, true, 1.0), part), box(allFours, part),
                    "all fours " + part.id() + " as a child");
        }
        context.complete();
    }

    /**
     * A horizontal ray enters the part it is aimed at, from the front and from behind alike, and enters it on the
     * model's surface (the head 4 px, the chest 2 px from the body axis). The player's left is where the Minecraft
     * convention puts it (towards +Z when facing -X), so the same world rays hit the other arm and leg once the body
     * has turned by 180 degrees. A ray that starts inside the head reports distance 0 at its own start.
     * 水平射线无论来自正面还是背后，都进入其瞄准的部位，且进入点位于模型表面（头部距身体轴线 4 像素，胸部 2 像素）。玩家的
     * 左侧符合 Minecraft 约定（面朝 -X 时在 +Z 一侧），因此身体转过 180 度后，同样的世界射线会击中另一侧的手臂和腿。起点在
     * 头部内的射线在其起点处报告距离 0。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void aRayEntersThePartItIsAimedAt(TestContext context) {
        Vec3d feet = new Vec3d(10.5, 64.0, -3.25);
        int yaw = 90;
        BodyPose facingWest = standing(feet, yaw);
        BodyPose facingEast = standing(feet, yaw + 180);
        for (Aim aim : STANDING_AIMS) {
            Ray front = frontRay(feet, yaw, aim.left(), aim.up());
            checkDirect(resolve(facingWest, front), aim.part(), "from the front at the " + aim.name());
            checkDirect(resolve(facingWest, front.reversed()), aim.part(), "from behind at the " + aim.name());
            checkDirect(resolve(facingEast, front), opposite(aim.part()),
                    "the same ray at the " + aim.name() + " after the body turned by 180");
            checkDirect(resolve(facingEast, front.reversed()), opposite(aim.part()),
                    "the same reversed ray at the " + aim.name() + " after the body turned by 180");
        }

        BodyHitboxRules.Hit head = resolve(facingWest, frontRay(feet, yaw, 0.0, 28 * PX));
        checkNear(head.distanceSquared(), square(REACH - 4 * PX), "distance to the face");
        checkSamePoint(head.point(), around(feet, yaw, 0.0, 28 * PX, 4 * PX), EPS, "entry into the face");
        BodyHitboxRules.Hit back = resolve(facingWest, frontRay(feet, yaw, 0.0, 20 * PX).reversed());
        checkNear(back.distanceSquared(), square(REACH - 2 * PX), "distance to the back");
        checkSamePoint(back.point(), around(feet, yaw, 0.0, 20 * PX, -2 * PX), EPS, "entry into the back");

        Vec3d inside = around(feet, yaw, 1 * PX, 29 * PX, -1 * PX);
        BodyHitboxRules.Hit fromInside = BodyHitboxRules.resolve(facingWest, inside, around(feet, yaw, 0.0, 0.0, 5.0));
        checkDirect(fromInside, BodyPart.HEAD, "a ray starting inside the head");
        check(fromInside.distanceSquared() == 0.0, "a ray starting inside the head is at distance "
                + fromInside.distanceSquared());
        checkSamePoint(fromInside.point(), inside, EPS, "a ray starting inside the head enters at its start");

        // Yaw 45 faces half-way between +Z and -X; the left hand is half-way between +X and +Z.
        // 偏航角 45 朝向 +Z 与 -X 的正中；左手位于 +X 与 +Z 的正中。
        double diagonal = Math.sqrt(0.5);
        Vec3d front45 = new Vec3d(-diagonal, 0.0, diagonal);
        Vec3d left45 = new Vec3d(diagonal, 0.0, diagonal);
        BodyPose facingSouthWest = standing(feet, 45);
        Vec3d face = feet.add(0.0, 28 * PX, 0.0);
        BodyHitboxRules.Hit diagonalHead = BodyHitboxRules.resolve(facingSouthWest,
                face.add(front45.multiply(REACH)), face.subtract(front45.multiply(REACH)));
        checkDirect(diagonalHead, BodyPart.HEAD, "yaw 45, from the front at the head");
        checkNear(diagonalHead.distanceSquared(), square(REACH - 4 * PX), NEAR, "yaw 45, distance to the face");
        Vec3d leftElbow = feet.add(0.0, 18 * PX, 0.0).add(left45.multiply(6 * PX));
        checkDirect(BodyHitboxRules.resolve(facingSouthWest, leftElbow.add(front45.multiply(REACH)),
                leftElbow.subtract(front45.multiply(REACH))), BodyPart.LEFT_ARM, "yaw 45, at the left arm");
        Vec3d rightElbow = feet.add(0.0, 18 * PX, 0.0).subtract(left45.multiply(6 * PX));
        checkDirect(BodyHitboxRules.resolve(facingSouthWest, rightElbow.add(front45.multiply(REACH)),
                rightElbow.subtract(front45.multiply(REACH))), BodyPart.RIGHT_ARM, "yaw 45, at the right arm");
        context.complete();
    }

    /**
     * A ray that enters no box is never a miss: it is not {@code direct}, names the part it passes closest to,
     * reports the squared gap and a point on the ray. Four centimetres above the crown that is the head, five beside
     * an arm that arm; a zero-length ray (a point attack) in front of a foot names that leg.
     * 未进入任何箱体的射线也不会落空：它不是 {@code direct}，给出离它最近的部位，并报告间隙的平方以及射线上的一个点。头顶上方
     * 4 厘米处是头部，手臂外侧 5 厘米处是该手臂；位于某只脚前方的零长度射线（点状攻击）给出那条腿。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void aRayBesideEveryBoxNamesTheNearestPart(TestContext context) {
        Vec3d feet = new Vec3d(10.5, 64.0, -3.25);
        int yaw = 90;
        BodyPose pose = standing(feet, yaw);

        Ray above = frontRay(feet, yaw, 0.0, 32 * PX + 0.04);
        BodyHitboxRules.Hit overHead = resolve(pose, above);
        checkNearMiss(overHead, BodyPart.HEAD, 0.04, "a ray 0.04 above the crown");
        checkOnRay(overHead.point(), above, "the nearest point of the ray above the crown");
        double alongFront = overHead.point().subtract(feet).dotProduct(front(yaw));
        check(Math.abs(alongFront) <= 4 * PX + NEAR, "the nearest point above the crown is over the head, but it is "
                + alongFront + " in front of the body axis");

        for (Aim aim : List.of(new Aim("left arm", 8 * PX + 0.05, 18 * PX, BodyPart.LEFT_ARM),
                new Aim("right arm", -8 * PX - 0.05, 18 * PX, BodyPart.RIGHT_ARM))) {
            Ray beside = frontRay(feet, yaw, aim.left(), aim.up());
            BodyHitboxRules.Hit hit = resolve(pose, beside);
            checkNearMiss(hit, aim.part(), 0.05, "a ray 0.05 beside the " + aim.name());
            checkOnRay(hit.point(), beside, "the nearest point of the ray beside the " + aim.name());
            checkNearMiss(resolve(pose, beside.reversed()), aim.part(), 0.05,
                    "a ray from behind 0.05 beside the " + aim.name());
        }

        for (Aim aim : List.of(new Aim("left foot", 2 * PX, 0.05, BodyPart.LEFT_LEG),
                new Aim("right foot", -2 * PX, 0.05, BodyPart.RIGHT_LEG))) {
            Vec3d point = around(feet, yaw, aim.left(), aim.up(), 0.3);
            BodyHitboxRules.Hit hit = BodyHitboxRules.resolve(pose, point, point);
            checkNearMiss(hit, aim.part(), 0.3 - 2 * PX, "a point 0.3 in front of the " + aim.name());
            checkSamePoint(hit.point(), point, NEAR, "the nearest point of a zero-length ray");
        }
        Vec3d inTheShin = around(feet, yaw, 2 * PX, 0.2, 0.0);
        BodyHitboxRules.Hit shin = BodyHitboxRules.resolve(pose, inTheShin, inTheShin);
        checkDirect(shin, BodyPart.LEFT_LEG, "a zero-length ray inside the left leg");
        check(shin.distanceSquared() == 0.0, "a point inside the left leg is at distance " + shin.distanceSquared());
        context.complete();
    }

    /**
     * A prone body at yaw 0 lies face down along +Z: the head ahead of the position, the legs behind it, everything
     * within 0.6 blocks above the feet level, the left side still towards +X. A supine body lies along its yaw with
     * the head towards it (the bed's head, where the position is), its soles (1.62 * scale - 0.1) blocks behind the
     * position, and, being face up, its left arm on the other world side than a standing body of the same yaw.
     * 偏航角为 0 的俯卧身体面朝下沿 +Z 躺着：头在位置前方，腿在后方，全部位于脚底高度以上 0.6 格之内，左侧仍朝 +X。仰卧的
     * 身体沿其偏航角躺着，头朝该方向（床头，即位置所在处），脚底位于位置后方 (1.62 * 缩放 - 0.1) 格处；由于面朝上，其左臂
     * 与相同偏航角站立的身体相比位于世界的另一侧。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void lyingBodiesLieAlongTheirYaw(TestContext context) {
        Vec3d feet = new Vec3d(-7.5, 70.0, 22.25);

        BodyPose prone = new BodyPose(BodyStance.PRONE, false, feet, 0.0F, 0.0F, 1.0);
        Box proneHead = worldBox(prone, BodyPart.HEAD);
        check(proneHead.minZ > feet.z, "a prone head at yaw 0 is ahead (+Z) of the position: " + proneHead);
        for (BodyPart leg : List.of(BodyPart.LEFT_LEG, BodyPart.RIGHT_LEG)) {
            Box box = worldBox(prone, leg);
            check(box.maxZ < feet.z, "a prone " + leg.id() + " at yaw 0 is behind (-Z) the position: " + box);
        }
        for (BodyPart part : BodyPart.values()) {
            Box box = worldBox(prone, part);
            check(box.minY >= feet.y - EPS && box.maxY <= feet.y + 0.6 + EPS,
                    "a prone " + part.id() + " lies within 0.6 above the feet level: " + box);
        }
        check(worldBox(prone, BodyPart.LEFT_ARM).getCenter().x > feet.x
                        && worldBox(prone, BodyPart.RIGHT_ARM).getCenter().x < feet.x,
                "face down at yaw 0 the left arm stays towards +X");
        for (BodyPart part : BodyPart.values()) {
            checkDirect(resolve(prone, fromAbove(worldBox(prone, part).getCenter())), part,
                    "prone, from above at the " + part.id());
        }

        // Yaw 90: the bed's head is towards -X. / 偏航角 90：床头朝 -X。
        BodyPose supine = new BodyPose(BodyStance.SUPINE, false, feet, 90.0F, 0.0F, 1.0);
        Box head = worldBox(supine, BodyPart.HEAD);
        Box leftLeg = worldBox(supine, BodyPart.LEFT_LEG);
        double chestX = worldBox(supine, BodyPart.CHEST).getCenter().x;
        double abdomenX = worldBox(supine, BodyPart.ABDOMEN).getCenter().x;
        check(head.getCenter().x < chestX && chestX < abdomenX && abdomenX < leftLeg.getCenter().x,
                "a supine body at yaw 90 runs head, chest, abdomen, legs from -X to +X");
        checkNear(leftLeg.maxX - head.minX, 1.875, "length of a supine body along its yaw");
        checkNear(leftLeg.maxX, feet.x + 1.52, "supine soles");
        checkNear(worldBox(supine, BodyPart.RIGHT_LEG).maxX, feet.x + 1.52, "supine right sole");
        double lowest = Double.MAX_VALUE;
        double highest = -Double.MAX_VALUE;
        for (BodyPart part : BodyPart.values()) {
            lowest = Math.min(lowest, worldBox(supine, part).minY);
            highest = Math.max(highest, worldBox(supine, part).maxY);
        }
        check(highest - lowest <= 0.5, "a supine body lies flat, but it is " + (highest - lowest) + " high");
        Vec3d pillow = head.getCenter().subtract(feet);
        check(pillow.horizontalLength() < 0.5,
                "a supine head rests in the bed's head block, the position's: " + pillow);

        BodyPose upright = standing(feet, 90);
        check(worldBox(upright, BodyPart.LEFT_ARM).getCenter().z > feet.z
                        && worldBox(upright, BodyPart.RIGHT_ARM).getCenter().z < feet.z,
                "standing at yaw 90 the left arm is towards +Z");
        check(worldBox(supine, BodyPart.LEFT_ARM).getCenter().z < feet.z
                        && worldBox(supine, BodyPart.RIGHT_ARM).getCenter().z > feet.z,
                "supine at yaw 90 the left arm is towards -Z: " + worldBox(supine, BodyPart.LEFT_ARM));
        check(worldBox(supine, BodyPart.LEFT_LEG).getCenter().z < feet.z
                        && worldBox(supine, BodyPart.RIGHT_LEG).getCenter().z > feet.z,
                "supine at yaw 90 the left leg is towards -Z: " + worldBox(supine, BodyPart.LEFT_LEG));
        for (BodyPart part : BodyPart.values()) {
            checkDirect(resolve(supine, fromAbove(worldBox(supine, part).getCenter())), part,
                    "supine, from above at the " + part.id());
        }

        BodyPose smallSupine = new BodyPose(BodyStance.SUPINE, false, feet, 90.0F, 0.0F, 0.5);
        checkNear(worldBox(smallSupine, BodyPart.LEFT_LEG).maxX, feet.x + (1.62 * 0.5 - 0.1),
                "supine soles at scale 0.5");
        checkNear(worldBox(smallSupine, BodyPart.LEFT_LEG).maxX - worldBox(smallSupine, BodyPart.HEAD).minX,
                1.875 * 0.5, "length of a supine body at scale 0.5");
        context.complete();
    }

    /**
     * The body yaw a client would draw: a standing body is dragged to within 50 degrees of the head and no farther,
     * a walking body converges on the movement direction, a body walking backwards keeps facing where the player
     * looks, and an arm swing pulls the body to the look direction even while it moves sideways.
     * 客户端会绘制的身体偏航角：站立的身体被拉到距头部 50 度以内且不会更多，行走的身体收敛到移动方向，后退的身体保持朝向
     * 玩家的视线方向，挥臂会把身体拉向视线方向，即使它正在横向移动。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void theBodyYawFollowsHeadMovementAndSwing(TestContext context) {
        float dragged = BodyYawRules.next(0.0F, 90.0F, 0.0, 0.0, false);
        check(apart(dragged, 90.0F) <= 50.001F, "a head 90 from a standing body leaves the body at " + dragged);
        check(dragged >= 0.0F && dragged <= 90.0F, "the body turns towards the head, not past it: " + dragged);
        float still = BodyYawRules.next(0.0F, 30.0F, 0.0, 0.0, false);
        check(still == 0.0F, "a standing body with the head 30 away does not turn: " + still);

        // Looking and walking west (-X, yaw 90). / 面朝西（-X，偏航角 90）并向西行走。
        float west = walk(50.0F, 90.0F, -0.2, 0.0, false);
        check(apart(west, 90.0F) < 0.5F, "walking west for 40 ticks leaves the body at " + west);
        // Looking south (yaw 0), walking towards yaw -30, i.e. +Z with some +X.
        // 面朝南（偏航角 0），朝偏航角 -30 的方向行走，即 +Z 并带一些 +X。
        float strafing = walk(0.0F, 0.0F, 0.2 * 0.5, 0.2 * Math.sqrt(0.75), false);
        check(apart(strafing, -30.0F) < 0.5F, "walking towards yaw -30 for 40 ticks leaves the body at " + strafing);

        // Looking south, walking north. / 面朝南，向北行走。
        float backwards = walk(30.0F, 0.0F, 0.0, -0.2, false);
        check(apart(backwards, 0.0F) < 0.5F, "walking backwards for 40 ticks leaves the body at " + backwards);
        float backwardsWest = walk(90.0F, 90.0F, 0.2, 0.0, false);
        check(apart(backwardsWest, 90.0F) < 0.5F, "walking backwards while looking west leaves the body at "
                + backwardsWest);

        float swung = BodyYawRules.next(0.0F, 40.0F, 0.0, 0.0, true);
        check(swung > 0.0F && swung <= 40.0F, "one tick of swinging turns the body towards the look yaw: " + swung);
        check(BodyYawRules.next(0.0F, 40.0F, 0.0, 0.0, false) == 0.0F, "without a swing the body stays");
        float swinging = walk(0.0F, 40.0F, 0.0, 0.0, true);
        check(apart(swinging, 40.0F) < 0.5F, "swinging for 40 ticks leaves the body at " + swinging);
        // Looking south, walking east (+X, yaw -90). / 面朝南，向东行走（+X，偏航角 -90）。
        float sideways = walk(0.0F, 0.0F, 0.2, 0.0, false);
        check(apart(sideways, 0.0F) > 30.0F, "walking sideways turns the body away from the look yaw: " + sideways);
        check(apart(sideways, 0.0F) <= 50.001F, "the body never leaves the head by more than 50: " + sideways);
        float sidewaysSwinging = walk(0.0F, 0.0F, 0.2, 0.0, true);
        check(apart(sidewaysSwinging, 0.0F) < 0.5F, "swinging while walking sideways keeps the body at the look yaw: "
                + sidewaysSwinging);
        context.complete();
    }

    /**
     * A real player entity is read into the same poses: standing at scale 1 and not a child, crouching after
     * {@link EntityPose#CROUCHING}, prone and level out of water after {@link EntityPose#SWIMMING} whatever its
     * pitch. A mock player has no pose history, so its current pose alone answers a ray at head height: the head,
     * entered on the face.
     * 真实的玩家实体被读取为同样的姿态：站立时缩放为 1 且不是幼体，设为 {@link EntityPose#CROUCHING} 后为下蹲，设为
     * {@link EntityPose#SWIMMING} 后在水外无论俯仰角如何都是水平俯卧。模拟玩家没有姿态历史，因此仅凭其当前姿态回答头部高度的
     * 射线：头部，进入点在脸上。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void aPlayerEntityIsReadIntoItsPoseAndHit(TestContext context) {
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        Vec3d feet = Vec3d.ofBottomCenter(context.getAbsolutePos(new BlockPos(1, 2, 1)));
        int yaw = 90;
        player.setPosition(feet);
        player.setYaw(yaw);
        player.bodyYaw = yaw;
        player.setPitch(40.0F);

        BodyPose standing = BodyHitboxes.poseOf(player, player.bodyYaw);
        check(standing.stance() == BodyStance.STANDING, "a fresh mock player is " + standing.stance());
        check(standing.scale() == 1.0, "a fresh mock player has scale " + standing.scale());
        check(!standing.child(), "a fresh mock player is not a child");
        check(standing.bodyYaw() == 90.0F, "the pose takes the given body yaw: " + standing.bodyYaw());
        checkSamePoint(standing.position(), feet, EPS, "the pose stands at the player's position");

        player.setPose(EntityPose.CROUCHING);
        BodyPose crouching = BodyHitboxes.poseOf(player, player.bodyYaw);
        check(crouching.stance() == BodyStance.CROUCHING, "a crouching mock player is " + crouching.stance());
        player.setPose(EntityPose.SWIMMING);
        BodyPose crawling = BodyHitboxes.poseOf(player, player.bodyYaw);
        check(crawling.stance() == BodyStance.PRONE, "a swimming-pose mock player is " + crawling.stance());
        check(crawling.pitch() == 0.0F, "a crawling body out of water is level, but its pitch is " + crawling.pitch());
        check(crawling.bodyYaw() == 90.0F, "a crawling body keeps the given yaw: " + crawling.bodyYaw());
        player.setPose(EntityPose.STANDING);

        Ray ray = frontRay(feet, yaw, 0.0, 28 * PX);
        BodyPartHit hit = BodyHitboxes.resolve(null, player, ray.start(), ray.end());
        check(hit.part() == BodyPart.HEAD && hit.direct(), "a ray at the mock player's head height: " + hit);
        checkOnRay(hit.point(), ray, "the entry into the mock player's head");
        checkSamePoint(hit.point(), around(feet, yaw, 0.0, 28 * PX, 4 * PX), EPS,
                "the entry into the mock player's face");
        BodyPartHit fromBehind = BodyHitboxes.resolve(null, player, ray.end(), ray.start());
        check(fromBehind.part() == BodyPart.HEAD && fromBehind.direct(), "a ray from behind at head height: "
                + fromBehind);
        Ray overHead = frontRay(feet, yaw, 0.0, 32 * PX + 0.04);
        BodyPartHit near = BodyHitboxes.resolve(null, player, overHead.start(), overHead.end());
        check(near.part() == BodyPart.HEAD && !near.direct(), "a ray just above the mock player's head: " + near);
        context.complete();
    }

    /**
     * The horizontal direction a Minecraft yaw faces, for the four cardinal yaws: 0 south (+Z), 90 west (-X),
     * 180 north (-Z), 270 east (+X). / Minecraft 偏航角在四个正方向上的水平朝向：0 南（+Z），90 西（-X），180 北（-Z），
     * 270 东（+X）。
     */
    private static Vec3d front(int yaw) {
        return switch (Math.floorMod(yaw, 360)) {
            case 0 -> new Vec3d(0.0, 0.0, 1.0);
            case 90 -> new Vec3d(-1.0, 0.0, 0.0);
            case 180 -> new Vec3d(0.0, 0.0, -1.0);
            case 270 -> new Vec3d(1.0, 0.0, 0.0);
            default -> throw new GameTestException("not a cardinal yaw: " + yaw);
        };
    }

    /** A player facing south has its left hand towards east, and so on round. / 面朝南的玩家左手朝东，依此类推。 */
    private static Vec3d left(int yaw) {
        return front(yaw - 90);
    }

    /**
     * The world point that far to the left of, above and in front of a body standing at {@code feet}.
     * 位于站在 {@code feet} 处的身体左方、上方、前方相应距离的世界坐标点。
     */
    private static Vec3d around(Vec3d feet, int yaw, double left, double up, double forward) {
        return feet.add(left(yaw).multiply(left)).add(0.0, up, 0.0).add(front(yaw).multiply(forward));
    }

    /** A horizontal ray through the body from in front of it to behind it. / 从身体前方穿到后方的水平射线。 */
    private static Ray frontRay(Vec3d feet, int yaw, double left, double up) {
        return new Ray(around(feet, yaw, left, up, REACH), around(feet, yaw, left, up, -REACH));
    }

    private static Ray fromAbove(Vec3d point) {
        return new Ray(point.add(0.0, REACH, 0.0), point.subtract(0.0, REACH, 0.0));
    }

    private static BodyPose standing(Vec3d feet, float yaw) {
        return new BodyPose(BodyStance.STANDING, false, feet, yaw, 0.0F, 1.0);
    }

    private static BodyHitboxRules.Hit resolve(BodyPose pose, Ray ray) {
        return BodyHitboxRules.resolve(pose, ray.start(), ray.end());
    }

    private static BodyPart opposite(BodyPart part) {
        return switch (part) {
            case LEFT_ARM -> BodyPart.RIGHT_ARM;
            case RIGHT_ARM -> BodyPart.LEFT_ARM;
            case LEFT_LEG -> BodyPart.RIGHT_LEG;
            case RIGHT_LEG -> BodyPart.LEFT_LEG;
            default -> part;
        };
    }

    private static Box box(List<BodyHitboxRules.PartBox> boxes, BodyPart part) {
        for (BodyHitboxRules.PartBox box : boxes) {
            if (box.part() == part) {
                return box.box();
            }
        }
        throw new GameTestException("no box for " + part.id());
    }

    /**
     * A part's box in the world. Only for a pose whose frame is axis-aligned (a cardinal yaw, pitch 0): two opposite
     * corners then span the whole box. / 某部位在世界中的箱体。仅适用于坐标系与世界轴对齐的姿态（正方向偏航角、俯仰角 0）：
     * 此时两个对角即可张成整个箱体。
     */
    private static Box worldBox(BodyPose pose, BodyPart part) {
        BodyHitboxRules.Frame frame = BodyHitboxRules.frame(pose);
        Box local = box(BodyHitboxRules.boxes(pose), part);
        return new Box(frame.toWorld(local.minX, local.minY, local.minZ),
                frame.toWorld(local.maxX, local.maxY, local.maxZ));
    }

    private static Box scaled(Box box, double factor) {
        return new Box(box.minX * factor, box.minY * factor, box.minZ * factor, box.maxX * factor, box.maxY * factor,
                box.maxZ * factor);
    }

    /** Forty ticks of the same movement, look and swing. / 以相同的位移、视线与挥臂状态推进四十刻。 */
    private static float walk(float bodyYaw, float yaw, double dx, double dz, boolean swinging) {
        float body = bodyYaw;
        for (int tick = 0; tick < 40; tick++) {
            body = BodyYawRules.next(body, yaw, dx, dz, swinging);
        }
        return body;
    }

    private static float apart(float yaw, float other) {
        return Math.abs(MathHelper.wrapDegrees(yaw - other));
    }

    private static double square(double value) {
        return value * value;
    }

    private static void checkMirrored(BodyStance stance, Box left, Box right) {
        check(left.getCenter().x > 0.0 && right.getCenter().x < 0.0,
                stance + ": the left part is on positive x: " + left + " / " + right);
        check(Math.abs(left.minX + right.maxX) <= EPS && Math.abs(left.maxX + right.minX) <= EPS
                        && Math.abs(left.minY - right.minY) <= EPS && Math.abs(left.maxY - right.maxY) <= EPS
                        && Math.abs(left.minZ - right.minZ) <= EPS && Math.abs(left.maxZ - right.maxZ) <= EPS,
                stance + ": left and right mirror each other: " + left + " / " + right);
    }

    private static void checkSameBox(Box actual, Box expected, String what) {
        check(Math.abs(actual.minX - expected.minX) <= EPS && Math.abs(actual.minY - expected.minY) <= EPS
                        && Math.abs(actual.minZ - expected.minZ) <= EPS && Math.abs(actual.maxX - expected.maxX) <= EPS
                        && Math.abs(actual.maxY - expected.maxY) <= EPS && Math.abs(actual.maxZ - expected.maxZ) <= EPS,
                what + ": expected " + expected + " but was " + actual);
    }

    private static void checkDirect(BodyHitboxRules.Hit hit, BodyPart part, String what) {
        check(hit.direct() && hit.part() == part, what + ": expected a direct hit on " + part.id() + " but was " + hit);
    }

    private static void checkNearMiss(BodyHitboxRules.Hit hit, BodyPart part, double gap, String what) {
        check(!hit.direct() && hit.part() == part && Math.abs(hit.distanceSquared() - gap * gap) <= NEAR,
                what + ": expected a near miss of " + part.id() + " with squared gap " + gap * gap + " but was " + hit);
    }

    private static void checkOnRay(Vec3d point, Ray ray, String what) {
        double detour = ray.start().distanceTo(point) + point.distanceTo(ray.end()) - ray.start().distanceTo(ray.end());
        check(Math.abs(detour) <= NEAR, what + " is not on the ray: " + point + " for " + ray);
    }

    private static void checkSamePoint(Vec3d actual, Vec3d expected, double tolerance, String what) {
        check(actual.distanceTo(expected) <= tolerance, what + ": expected " + expected + " but was " + actual);
    }

    private static void checkNear(double actual, double expected, String what) {
        checkNear(actual, expected, EPS, what);
    }

    private static void checkNear(double actual, double expected, double tolerance, String what) {
        check(Math.abs(actual - expected) <= tolerance, what + ": expected " + expected + " but was " + actual);
    }

    private static void check(boolean condition, String what) {
        if (!condition) {
            throw new GameTestException(what);
        }
    }
}
