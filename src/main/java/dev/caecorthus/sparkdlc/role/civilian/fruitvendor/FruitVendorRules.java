package dev.caecorthus.sparkdlc.role.civilian.fruitvendor;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Numbers for the Fruit Vendor (owner 2026-11-28). Passenger faction, colour BCFF7B.
 * 水果贩的数值（所有者 2026-11-28）。乘客阵营，身份色 BCFF7B。
 */
public final class FruitVendorRules {

    public static final Identifier ROLE_ID = SparkDLC.id("fruit_vendor");
    public static final int COLOR = 0xBCFF7B;
    public static final int OUTLINE_COLOR = 0xFFBCFF7B;

    // ---- Shop ----

    /** The random-fruit crate: one of the four vanilla fruits, 75 coins, one minute between buys. */
    /** 随机水果：四种原版水果之一，75 金币，两次购买之间隔一分钟。 */
    public static final String RANDOM_FRUIT_ENTRY = SparkDLC.MOD_ID + ":fruit_crate";
    public static final int RANDOM_FRUIT_PRICE = 75;
    public static final int RANDOM_FRUIT_COOLDOWN_TICKS = 60 * 20;

    public static final String BANANA_ENTRY = SparkDLC.MOD_ID + ":banana";
    public static final int BANANA_PRICE = 125;
    public static final String ORANGE_ENTRY = SparkDLC.MOD_ID + ":orange";
    public static final int ORANGE_PRICE = 75;
    public static final String BERRY_ENTRY = SparkDLC.MOD_ID + ":enlightenment_berry";
    public static final int BERRY_PRICE = 100;

    // ---- Edible effects ----

    /** Orange: Speed II for eight seconds. / 橙子：8 秒速度 II。 */
    public static final int ORANGE_SPEED_TICKS = 8 * 20;
    public static final int ORANGE_SPEED_AMPLIFIER = 1;
    /** Enlightenment Berry: Glowing for twelve seconds. / 启明浆果：自身发光 12 秒。 */
    public static final int BERRY_GLOW_TICKS = 12 * 20;

    // ---- Banana peel ----

    /** A peel slips the first player who steps on it for 1.0–8.5 s (rolls to the nearest 10 ticks). */
    /** 香蕉皮让第一个踩上去的玩家滑倒 1.0–8.5 秒（按 10 刻取整）。 */
    public static final int SLIP_MIN_TICKS = 20;
    public static final int SLIP_MAX_TICKS = 170;
    /** How hard the slip throws the player, and how fast it dies down. / 滑倒的初速度与衰减。 */
    public static final double SLIP_SPEED = 0.85D;
    public static final double SLIP_DAMPING = 0.82D;

    // ---- Economy ----

    public static final int TASK_MONEY_REWARD = 50;
    public static final int KILL_MONEY_REWARD = 100;

    private FruitVendorRules() {
    }

    public static boolean isFruitVendor(@Nullable Role role) {
        return role != null && ROLE_ID.equals(role.identifier());
    }
}
