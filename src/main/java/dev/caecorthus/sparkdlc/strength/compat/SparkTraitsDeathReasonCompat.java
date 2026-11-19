package dev.caecorthus.sparkdlc.strength.compat;

import dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi;
import net.minecraft.util.Identifier;

/**
 * Bridge to SparkTraits' terminal death reasons: a death with a registered reason is final, so traits such as
 * Last Stand and Depression never step in. The modules are fused in SparkDLC, so this calls
 * {@link SparkTraitsApi#registerTerminalDeathReason} directly.
 * SparkTraits 终结死因的桥接：以已注册死因死亡即为最终死亡，背水一战、抑郁等词条不会介入。各模块已融合进 SparkDLC，
 * 因此这里直接调用 {@link SparkTraitsApi#registerTerminalDeathReason}。
 */
public final class SparkTraitsDeathReasonCompat {
    private SparkTraitsDeathReasonCompat() {
    }

    /** Pass the caller's own constant; SparkTraits keeps it as is. / 传入调用方自己的常量；SparkTraits 原样保存。 */
    public static void registerTerminal(Identifier reason) {
        if (reason == null) {
            return;
        }
        SparkTraitsApi.registerTerminalDeathReason(reason);
    }
}
