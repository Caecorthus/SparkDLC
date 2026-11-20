package dev.caecorthus.sparkdlc.client.role.civilian.undercover;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.client.role.civilian.bear.smoke.SmokeClientState;
import dev.caecorthus.sparkdlc.client.role.civilian.bear.smoke.occlusion.SmokeHeatMask;
import dev.caecorthus.sparkdlc.client.role.civilian.bear.smoke.occlusion.SmokeHeatMath;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * 卧底热成像中的 BEAR 烟雾步骤（BEAR S4“热烟挡住热成像”，所有者 Q6：烟显示为一团亮白并遮住其后的人体）。
 * The BEAR smoke step of the Undercover thermal heat image (owner S4 "hot smoke blocks thermal", owner Q6: smoke
 * shows as a bright white blob and hides the bodies behind it).
 *
 * <p>调用烟雾功能的热图入口 {@link SmokeHeatMask}（{@code maskHeatTarget}、{@code drawHeatHaze}、{@code smokeRect}），以
 * {@link SmokeClientState#isEmpty()} 判断是否有烟，热雾热值取 {@link SmokeHeatMath#DEFAULT_HAZE}。顺序遵循
 * {@link SmokeHeatMask} 的说明：画完人体之后、柔光合成之前，先遮罩，失败时隐藏烟雾矩形，再画热雾。这里的 try/catch 是渲染
 * 故障隔离：有烟时任何一步失败都按失败即遮挡处理，把可能有烟的区域（未知时为整个热图）交给调用方清掉，宁可多藏不可透烟。
 * 只记录一次日志，绝不抛出。
 * Calls the smoke feature's heat-image entries on {@link SmokeHeatMask} ({@code maskHeatTarget}, {@code drawHeatHaze},
 * {@code smokeRect}), asks {@link SmokeClientState#isEmpty()} whether there is smoke at all, and takes the haze heat
 * from {@link SmokeHeatMath#DEFAULT_HAZE}. Call order follows {@link SmokeHeatMask}: after the bodies, before the
 * bloom composite, mask first, hide the smoke rectangle when the mask fails, then the haze. The try/catch blocks are
 * render fault isolation: with smoke known, every failure fails closed, the area smoke may touch (the whole target
 * when unknown) goes to the caller to clear, so heat never shows through smoke. Logged once, never throws. Render
 * thread only.</p>
 */
public final class UndercoverThermalSmoke {
    /**
     * A throwing smoke query or rectangle makes every later frame hide the whole heat target while thermal is on; a
     * throwing mask makes every later frame hide the smoke rectangle instead (fail closed, permanent like the smoke
     * mask's own GL failure). / 烟雾查询或矩形抛出异常后，之后每帧热成像都隐藏整个热图；遮罩抛出异常后，之后每帧改为隐藏烟雾矩形
     * （失败即遮挡，与烟雾遮罩自身的 GL 失败一样永久）。
     */
    private static boolean smokeUnknown;
    private static boolean maskBroken;
    private static boolean hazeBroken;
    private static boolean logged;

    private UndercoverThermalSmoke() {
    }

    /**
     * Applies BEAR smoke to a heat target that already holds its bodies (and their depth): {@code colorTexture} and
     * {@code depthTexture} are its GL texture ids, {@code width} x {@code height} its size, covering exactly this world
     * render's view. {@code hide} receives {@code {x0, y0, x1, y1}} (exclusive ends, GL origin bottom-left, clamped
     * to the target) whenever the heat there must be cleared because the mask could not run; it is called at most once,
     * before the haze. Call inside the same {@code WorldRenderer#render} pass, on the render thread.
     * 把 BEAR 烟雾作用到已画好人体（及其深度）的热图上：{@code colorTexture} 与 {@code depthTexture} 为其 GL 纹理 id，
     * {@code width} x {@code height} 为其尺寸，恰好覆盖本次世界渲染的视野。遮罩无法执行时，{@code hide} 收到需清空热值的
     * {@code {x0, y0, x1, y1}}（不含终点，GL 原点在左下，已裁剪到热图内）；最多调用一次，且在热雾之前。须在同一次
     * {@code WorldRenderer#render} 中、于渲染线程调用。
     */
    public static void applyToHeat(int colorTexture, int depthTexture, int width, int height, Consumer<int[]> hide) {
        if (hide == null || width <= 0 || height <= 0) {
            return;
        }
        int[] whole = {0, 0, width, height};
        if (smokeUnknown) {
            hide.accept(whole);
            return;
        }
        try {
            if (SmokeClientState.isEmpty()) {
                return;
            }
        } catch (RuntimeException e) {
            smokeUnknown = true;
            warnOnce("SparkWitch BEAR smoke query failed; the thermal view now hides all heat while it is on.", e);
            hide.accept(whole);
            return;
        }
        if (!mask(colorTexture, depthTexture, width, height)) {
            int[] rect = hiddenRect(width, height);
            if (rect != null) {
                hide.accept(rect);
            }
        }
        haze(colorTexture, depthTexture, width, height);
    }

    /** True when the heat behind smoke is right (masked, or no smoke touches this pass). / 烟后的热值正确时为 true。 */
    private static boolean mask(int colorTexture, int depthTexture, int width, int height) {
        if (maskBroken) {
            return false;
        }
        try {
            return SmokeHeatMask.maskHeatTarget(colorTexture, depthTexture, width, height);
        } catch (RuntimeException e) {
            maskBroken = true;
            warnOnce("SparkWitch BEAR smoke heat mask failed; the thermal view hides heat inside smoke instead.", e);
            return false;
        }
    }

    /**
     * The area to clear after a failed mask: the smoke rectangle clamped to the target, null when smoke cannot touch
     * it, the whole target when that is unknown or the answer is malformed.
     * 遮罩失败后需清空的区域：烟雾矩形（裁剪到热图内）；烟不可能触及时为 null；无法得知或答案异常时为整个热图。
     */
    private static @Nullable int[] hiddenRect(int width, int height) {
        int[] whole = {0, 0, width, height};
        if (smokeUnknown) {
            return whole;
        }
        int[] rect;
        try {
            rect = SmokeHeatMask.smokeRect(width, height);
        } catch (RuntimeException e) {
            smokeUnknown = true;
            warnOnce("SparkWitch BEAR smoke rectangle failed; the thermal view now hides all heat while it is on.", e);
            return whole;
        }
        if (rect == null) {
            return null;
        }
        if (rect.length != 4) {
            return whole;
        }
        int x0 = Math.max(0, rect[0]);
        int y0 = Math.max(0, rect[1]);
        int x1 = Math.min(width, rect[2]);
        int y1 = Math.min(height, rect[3]);
        return x1 > x0 && y1 > y0 ? new int[]{x0, y0, x1, y1} : null;
    }

    /** Cosmetic: a failure only switches the haze off. / 仅外观：失败只会关掉热雾。 */
    private static void haze(int colorTexture, int depthTexture, int width, int height) {
        if (hazeBroken) {
            return;
        }
        try {
            SmokeHeatMask.drawHeatHaze(colorTexture, depthTexture, width, height,
                    SmokeHeatMath.DEFAULT_HAZE);
        } catch (RuntimeException e) {
            hazeBroken = true;
            warnOnce("SparkWitch BEAR smoke heat haze failed; the thermal view shows no smoke haze.", e);
        }
    }

    private static void warnOnce(String message, Throwable error) {
        if (logged) {
            return;
        }
        logged = true;
        SparkDLC.LOGGER.warn(message, error);
    }
}
