package dev.caecorthus.sparkdlc.block;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * How one shining neon pillar column blinks: how long it stays lit, how long it stays dark, or random phases. The
 * compact constructor clamps both durations, so every instance, whether typed in the screen, decoded from a packet
 * or read from a saved block entity, is within bounds; a hand-made packet can never ask for a zero-tick phase.
 * <p>
 * 一根闪烁霓虹灯柱的闪烁方式：亮多久、灭多久，或随机时长。紧凑构造器会把两个时长夹到范围内，因此无论来自界面输入、
 * 数据包解码还是已保存的方块实体，每个实例都在界限之内；伪造的数据包也无法要求 0 tick 的相位。
 */
public record NeonBlinkSettings(int onTicks, int offTicks, boolean random) {
    public static final int TICKS_PER_SECOND = 20;
    /** Bounds of the two durations, in seconds as the screen shows them. / 两个时长的上下限，与界面一致以秒计。 */
    public static final int MIN_SECONDS = 1;
    public static final int MAX_SECONDS = 600;
    public static final int MIN_TICKS = MIN_SECONDS * TICKS_PER_SECOND;
    public static final int MAX_TICKS = MAX_SECONDS * TICKS_PER_SECOND;
    /**
     * Random mode ignores the two durations and draws every phase from this range, so a column never settles into a
     * rhythm. / 随机模式忽略两个时长，每段相位都从此区间重新取值，灯柱不会稳定成固定节奏。
     */
    public static final int RANDOM_MIN_SECONDS = 3;
    public static final int RANDOM_MAX_SECONDS = 8;
    /** What the screen shows for a column that has never blinked. / 从未闪烁过的灯柱在界面上显示的初值。 */
    public static final NeonBlinkSettings DEFAULT = new NeonBlinkSettings(MIN_TICKS, MIN_TICKS, false);

    public static final PacketCodec<ByteBuf, NeonBlinkSettings> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, NeonBlinkSettings::onTicks,
            PacketCodecs.VAR_INT, NeonBlinkSettings::offTicks,
            PacketCodecs.BOOL, NeonBlinkSettings::random,
            NeonBlinkSettings::new
    );

    public NeonBlinkSettings {
        onTicks = MathHelper.clamp(onTicks, MIN_TICKS, MAX_TICKS);
        offTicks = MathHelper.clamp(offTicks, MIN_TICKS, MAX_TICKS);
    }

    /** Length of a phase that has just begun; {@code lit} is the state the column now shows. / 刚开始的相位的长度；{@code lit} 为灯柱当前的亮灭。 */
    public int phaseTicks(boolean lit, Random rng) {
        if (random) {
            int min = RANDOM_MIN_SECONDS * TICKS_PER_SECOND;
            return min + rng.nextInt(RANDOM_MAX_SECONDS * TICKS_PER_SECOND - min + 1);
        }
        return lit ? onTicks : offTicks;
    }
}
