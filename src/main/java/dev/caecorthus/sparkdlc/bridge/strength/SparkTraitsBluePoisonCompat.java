package dev.caecorthus.sparkdlc.bridge.strength;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * Bridge to SparkTraits' public blue-poison facade, used by the Toxicologist buff. The modules are fused in SparkDLC,
 * so every method is a direct call to {@link SparkTraitsApi}; Blue Vitriol, Blue Belladonna and the blue passive are
 * always available.
 * 毒理学家增强使用的 SparkTraits 蓝毒公开门面桥接。各模块已融合进 SparkDLC，因此每个方法都直接调用
 * {@link SparkTraitsApi}；蓝矾、蓝颠茄和蓝毒被动始终可用。
 */
public final class SparkTraitsBluePoisonCompat {
    private SparkTraitsBluePoisonCompat() {
    }

    /**
     * Always true now that the modules are fused in SparkDLC: every blue-poison facade method the Toxicologist buff
     * needs is a compiled call. Kept for its callers.
     * Shop listings never depend on this (client/server entry order must match); purchases and uses check it server-side.
     * 各模块已融合进 SparkDLC，毒理学家增强所需的全部蓝毒门面方法都是编译期调用，因此恒为 true；为调用方保留。
     * 商店列表不依赖它（客户端与服务端条目顺序必须一致），只在服务端购买和使用时检查。
     */
    public static boolean isBluePoisonApiAvailable() {
        return true;
    }

    /** Server-side: swaps a plate's native Wathe poison for blue poison owned by {@code poisoner}.
     *  服务端：把餐盘上的 Wathe 原生毒换成归属 {@code poisoner} 的蓝毒。 */
    public static boolean convertPlatePoisonToBlue(World world, BlockPos pos, UUID poisoner) {
        return world != null && pos != null && poisoner != null
                && SparkTraitsApi.convertPlatePoisonToBlue(world, pos, poisoner);
    }

    /** Server-side: swaps a bed's native scorpion for a blue scorpion owned by {@code poisoner}.
     *  服务端：把床上的原生蝎子换成归属 {@code poisoner} 的蓝毒蝎子。 */
    public static boolean convertBedPoisonToBlue(World world, BlockPos pos, UUID poisoner) {
        return world != null && pos != null && poisoner != null
                && SparkTraitsApi.convertBedPoisonToBlue(world, pos, poisoner);
    }

    /** Swaps a food/drink stack's native poison marker for a blue marker owned by {@code poisoner}.
     *  把食物/饮品上的原生毒标记换成归属 {@code poisoner} 的蓝毒标记。 */
    public static boolean convertStackPoisonToBlue(ItemStack stack, UUID poisoner) {
        return stack != null && !stack.isEmpty() && poisoner != null
                && SparkTraitsApi.convertStackPoisonToBlue(stack, poisoner);
    }

    /** Stamps a blue-poison marker owned by {@code poisoner}; false only for an empty stack or a null argument.
     *  写入归属 {@code poisoner} 的蓝毒标记；仅当物品为空或参数为 null 时返回 false。 */
    public static boolean markStackBluePoison(ItemStack stack, UUID poisoner) {
        if (stack == null || stack.isEmpty() || poisoner == null) {
            return false;
        }
        SparkTraitsApi.markStackBluePoison(stack, poisoner);
        return true;
    }

    public static @Nullable UUID getStackBluePoisoner(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return SparkTraitsApi.getStackBluePoisoner(stack);
    }

    /** Server-side: springs a blue food trap with SparkTraits' alignment rules (sanity drain for civilians, lethal otherwise).
     *  服务端：按 SparkTraits 阵营规则触发蓝毒食物陷阱（好人扣理智，其余致死蓝毒）。 */
    public static void applyBlueTrap(ServerPlayerEntity target, UUID poisoner) {
        if (target != null && poisoner != null) {
            SparkTraitsApi.applyBlueTrap(target, poisoner);
        }
    }

    /** Server-side: extends the blue sanity-drain window to at least {@code ticks}.
     *  服务端：把蓝毒扣理智窗口延长到至少 {@code ticks}。 */
    public static void applyBlueSanityDrain(ServerPlayerEntity target, int ticks) {
        if (target != null && ticks > 0) {
            SparkTraitsApi.applyBlueSanityDrain(target, ticks);
        }
    }

    /** Server-authoritative remaining blue sanity-drain ticks; 0 on clients.
     *  服务端权威的蓝毒扣理智剩余 tick；客户端为 0。 */
    public static int getBlueSanityDrainTicks(PlayerEntity player) {
        if (player == null) {
            return 0;
        }
        return Math.max(0, SparkTraitsApi.getBlueSanityDrainTicks(player));
    }

    /** Registers a server predicate that skips SparkTraits' blue sanity drain; false only for a null predicate.
     *  注册服务端谓词以跳过 SparkTraits 的蓝毒扣理智；仅当谓词为 null 时返回 false。 */
    public static boolean registerBlueSanityDrainExemption(Predicate<ServerPlayerEntity> exemption) {
        if (exemption == null) {
            return false;
        }
        SparkTraitsApi.registerBlueSanityDrainExemption(exemption);
        return true;
    }
}
