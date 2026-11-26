package dev.caecorthus.sparkdlc.health.hitbox;

import dev.caecorthus.sparkdlc.health.BodyPart;
import net.minecraft.util.math.Vec3d;

/**
 * The answer of {@link BodyHitboxes#resolve}: the part a hit landed on. {@code direct} is false when the ray entered
 * no part box and {@code part} is the nearest one; {@code point} (world space) is then the point of the ray nearest
 * to it, otherwise where the ray entered the part.
 * {@link BodyHitboxes#resolve} 的结果：命中所落在的部位。射线未进入任何部位箱体、{@code part} 取最近部位时 {@code direct}
 * 为 false；此时 {@code point}（世界坐标）为射线上离它最近的点，否则为射线进入该部位的位置。
 */
public record BodyPartHit(BodyPart part, Vec3d point, boolean direct) {
}
