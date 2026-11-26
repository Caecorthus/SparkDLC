package dev.caecorthus.sparkdlc.gametest.hudsnapshot;

import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

/**
 * What is behind the HUD in the pictures of {@code ./gradlew runHudSnapshot}: {@code -PhudSnapshotBackdrop=mid|sky|dark},
 * {@code mid} by default. The walls are plain concrete lit by the noon sky, so one run shows the HUD on a flat
 * mid-tone, and two more runs show how it holds up against a bright sky and a dark cabin.
 * {@code ./gradlew runHudSnapshot} 的图片里 HUD 背后是什么：{@code -PhudSnapshotBackdrop=mid|sky|dark}，默认为 {@code mid}。
 * 墙是被正午天空照亮的纯色混凝土，因此一次运行展示 HUD 在平整中间调上的样子，另外两次运行展示它在明亮天空与昏暗车厢前的表现。
 */
enum HudSnapshotBackdrop {
    /** A light gray wall: about RGB 100, 100, 92 on screen. / 淡灰色墙：屏幕上约为 RGB 100, 100, 92。 */
    MID(Blocks.LIGHT_GRAY_CONCRETE),
    /** No wall: the daytime sky of the void world. / 没有墙：虚空世界的白天天空。 */
    SKY(null),
    /** A gray wall: about RGB 43, 46, 49 on screen. / 灰色墙：屏幕上约为 RGB 43, 46, 49。 */
    DARK(Blocks.GRAY_CONCRETE);

    static final String PROPERTY = "sparkdlc.hudSnapshot.backdrop";

    private final Block wall;

    HudSnapshotBackdrop(Block wall) {
        this.wall = wall;
    }

    /** The block of the wall, or null for none. / 墙所用的方块；没有墙则为 null。 */
    Block wall() {
        return wall;
    }

    String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The backdrop with this id; blank is {@link #MID}, an unknown id is an error. / 该 id 对应的背景；留空为 {@link #MID}，未知 id 是错误。 */
    static HudSnapshotBackdrop select(String id) {
        if (id == null || id.isBlank()) {
            return MID;
        }
        for (HudSnapshotBackdrop backdrop : values()) {
            if (backdrop.id().equals(id.trim().toLowerCase(Locale.ROOT))) {
                return backdrop;
            }
        }
        throw new IllegalArgumentException("unknown backdrop '" + id + "'; known backdrops: mid, sky, dark");
    }
}
