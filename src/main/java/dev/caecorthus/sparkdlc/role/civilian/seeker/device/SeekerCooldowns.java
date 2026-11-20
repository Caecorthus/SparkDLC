package dev.caecorthus.sparkdlc.role.civilian.seeker.device;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerCooldownReason;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerRules;
import dev.caecorthus.sparkdlc.util.ExactItemCooldowns;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.IntConsumer;

/**
 * Frozen contract: the single "max + exact" writer of the {@code seeker_car} item cooldown. A longer existing cooldown
 * is kept; the write is the exact one ({@code SparkTraitsApi.setExactItemCooldownRemaining}), which performs the
 * vanilla set itself (one write, one packet).
 * Only {@code SeekerStatusComponent#apply} calls {@link #writeFloorExact}; it is never written anywhere else.
 * 冻结契约：{@code seeker_car} 物品冷却“取大 + 精确”的唯一写入方。保留更长的既有冷却；
 * 写入使用精确写入（{@code SparkTraitsApi.setExactItemCooldownRemaining}），由它自行完成原版写入（只写一次、只发一个数据包）。
 * 只有 {@code SeekerStatusComponent#apply} 调用 {@link #writeFloorExact}，其他地方从不写入。
 */
public final class SeekerCooldowns {
    private SeekerCooldowns() {
    }

    /**
     * Server only. {@code reason} is informational: the component stores it for the HUD label; the item cooldown
     * itself is authoritative.
     * 仅服务端。{@code reason} 仅供参考：组件保存它用作 HUD 标签；物品冷却本身才是权威。
     *
     * @return true when a new value was written. / 写入了新值时返回 true。
     */
    public static boolean writeFloorExact(ServerPlayerEntity player, int ticks, SeekerCooldownReason reason) {
        if (player == null || ticks <= 0) {
            return false;
        }
        Item car = SparkDlcItems.seekerCar();
        // The exact write bypasses Fast Hands itself and performs the vanilla write.
        // 精确写入自行绕过快手并完成原版写入。
        return write(remainingTicks(player), ticks,
                merged -> SparkTraitsApi.setExactItemCooldownRemaining(player, car, merged));
    }

    /**
     * Pure write rule: nothing happens unless the merged value ({@link SeekerRules#mergeCooldownTicks}) is longer than
     * what already runs; then the exact writer runs once.
     * 纯写入规则：只有取大后的值长于当前剩余时间才写入；随后精确写入执行一次。
     */
    static boolean write(int existingTicks, int requestedTicks, IntConsumer exactWriter) {
        int existing = Math.max(0, existingTicks);
        int merged = SeekerRules.mergeCooldownTicks(existing, requestedTicks);
        if (merged <= existing) {
            return false;
        }
        exactWriter.accept(merged);
        return true;
    }

    /**
     * Remaining ticks on the car item cooldown; 0 when none. Side-neutral: the client reads its synced vanilla cooldown
     * (HUD, console), the server its authoritative one.
     * 小车物品冷却的剩余刻数；无冷却时为 0。两端通用：客户端读取已同步的原版冷却（HUD、控制台），服务端读取权威冷却。
     */
    public static int remainingTicks(PlayerEntity player) {
        return ExactItemCooldowns.remainingTicks(player, SparkDlcItems.seekerCar());
    }
}
