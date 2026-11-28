package dev.caecorthus.sparkdlc.health;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * The seven parts of a player's body-health pool, each with its own maximum. Pure data, shared by both sides.
 *
 * <p>The declaration order is a contract: {@link BodyHealthComponent} writes its sync packet in this order, so a
 * part is added at the end and none is reordered. {@link #id()} is what {@code /sparkdlc:healthMode set} takes and
 * what the client names its assets and lang keys after; do not rename one.</p>
 *
 * 玩家部位血量池的七个部位，各有自己的上限。纯数据，两端共用。
 *
 * <p>声明顺序属于契约：{@link BodyHealthComponent} 按此顺序写同步包，因此新部位只能追加在末尾，已有部位不得调换顺序。
 * {@link #id()} 是 {@code /sparkdlc:healthMode set} 接受的参数，也是客户端资源与语言键的命名依据；不要改名。</p>
 */
public enum BodyPart {
    HEAD(35),
    CHEST(85),
    ABDOMEN(70),
    LEFT_ARM(60),
    RIGHT_ARM(60),
    LEFT_LEG(65),
    RIGHT_LEG(65);

    private final int maxHealth;
    private final String id;

    BodyPart(int maxHealth) {
        this.maxHealth = maxHealth;
        this.id = name().toLowerCase(Locale.ROOT);
    }

    /** Full health of this part, and its default. / 该部位的满血量，也是默认值。 */
    public int maxHealth() {
        return maxHealth;
    }

    /** Stable snake_case name, e.g. {@code left_arm}. / 稳定的蛇形命名，例如 {@code left_arm}。 */
    public String id() {
        return id;
    }

    /**
     * Whether this is a vital part (head and chest). Only a label for now: nothing in the mod kills on it yet.
     * 是否为要害部位（头部与胸部）。目前只是标记：模组中尚无任何逻辑据此致死。
     */
    public boolean vital() {
        return this == HEAD || this == CHEST;
    }

    /** The part with this {@link #id()}, or null. / 具有该 {@link #id()} 的部位，没有则为 null。 */
    public static @Nullable BodyPart byId(String id) {
        for (BodyPart part : values()) {
            if (part.id.equals(id)) {
                return part;
            }
        }
        return null;
    }
}
