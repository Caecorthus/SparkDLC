package dev.caecorthus.sparkdlc.health;

/**
 * Pure arithmetic of the body-health pool: no Minecraft state, usable from both sides and from plain unit tests.
 * 部位血量池的纯算术：不涉及 Minecraft 状态，两端与普通单元测试均可使用。
 */
public final class BodyHealthRules {
    /** Sum of every part's maximum (440). / 所有部位上限之和（440）。 */
    public static final int TOTAL_MAX = totalMax();

    private BodyHealthRules() {
    }

    /** {@code value} limited to {@code 0..part.maxHealth()}. / 把 {@code value} 限制在 {@code 0..part.maxHealth()}。 */
    public static int clamp(BodyPart part, int value) {
        return Math.clamp(value, 0, part.maxHealth());
    }

    /** Whether {@code value} is a health this part can hold as it is. / {@code value} 是否无需限幅即为该部位的合法血量。 */
    public static boolean isInRange(BodyPart part, int value) {
        return value >= 0 && value <= part.maxHealth();
    }

    private static int totalMax() {
        int total = 0;
        for (BodyPart part : BodyPart.values()) {
            total += part.maxHealth();
        }
        return total;
    }
}
