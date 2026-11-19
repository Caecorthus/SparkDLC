package dev.caecorthus.sparkdlc.client.hud.charge;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Asks SparkTraits how the local player's traits change a charge. Each call reads only state SparkTraits syncs to the
 * owning client, and is the same call the mod's own release checks go through, so the indicator stays in step with
 * what the server will accept.
 * 询问 SparkTraits 本地玩家的词条如何改变蓄力。每个调用只读取 SparkTraits 同步给所属客户端的状态，且与该模组自身松手判定
 * 走的是同一个调用，因此指示与服务端将会接受的结果保持一致。
 *
 * <p>The modules are fused in SparkDLC, so these are direct calls to {@link SparkTraitsApi} (they used to be looked
 * up by name, and a missing method left the untalented value in place).
 * 各模块已融合进 SparkDLC，因此这里直接调用 {@link SparkTraitsApi}（原先按名称查找，方法缺失时沿用无词条的数值）。</p>
 */
public final class SparkTraitsCharge {
    private SparkTraitsCharge() {
    }

    /** Charge of a throwable after Herculean Strength, or {@code baseTicks}. 计入「力大无穷」后的投掷物蓄力；否则为 {@code baseTicks}。 */
    public static int throwCharge(PlayerEntity player, int baseTicks) {
        return positive(SparkTraitsApi.getThrowChargeTicks(player, baseTicks), baseTicks);
    }

    /** Windup of Wathe's knife after Seasoned or Close Quarters, or {@code baseTicks}. 计入「老练」或「狭路相逢」后的小刀前摇；否则为 {@code baseTicks}。 */
    public static int knifeWindup(PlayerEntity player, ItemStack knife, int baseTicks) {
        return positive(SparkTraitsApi.getKnifeWindupTicks(player, knife, baseTicks), baseTicks);
    }

    /** Whether a forced-melee lock is discarding this weapon's releases right now. 强制近战锁定此刻是否正在作废该武器的松手。 */
    public static boolean meleeLocked(PlayerEntity player, ItemStack weapon) {
        return SparkTraitsApi.getForcedMeleeCooldownTicks(player, weapon) > 0;
    }

    private static int positive(int ticks, int fallback) {
        return ticks > 0 ? ticks : fallback;
    }
}
