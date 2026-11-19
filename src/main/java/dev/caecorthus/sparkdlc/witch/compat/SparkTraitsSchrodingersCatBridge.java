package dev.caecorthus.sparkdlc.witch.compat;

import dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * SparkTraits seam for 薛定谔之猫. Calls only the public facade
 * {@code SparkTraitsApi.replaceTraitsIneligibleForCurrentRole(ServerPlayerEntity, BiConsumer)}; never
 * {@code sparktraits.impl}. Called after the new role, kit and balance are written: the provider removes every trait
 * the new role could not have rolled, redraws as many and syncs. SparkTraits is fused into SparkDLC, so the call is
 * direct; a null player changes nothing. Only the visible names reach the visitor.
 * 薛定谔之猫的 SparkTraits 接缝。仅调用公共门面
 * {@code SparkTraitsApi.replaceTraitsIneligibleForCurrentRole}，从不触及 {@code sparktraits.impl}。在新身份、装备与余额
 * 写入之后调用：由提供方移除新身份开局无法抽到的全部词条、补抽同等数量并同步。SparkTraits 已融合进 SparkDLC，因此为直接
 * 调用；玩家为空时不做任何改动。只有可见的词条名会传给回调。
 */
public final class SparkTraitsSchrodingersCatBridge {
    private SparkTraitsSchrodingersCatBridge() {
    }

    public static void replaceIneligibleTraits(@Nullable ServerPlayerEntity player,
                                               BiConsumer<List<Text>, List<Text>> visitor) {
        if (player == null) {
            return;
        }
        SparkTraitsApi.replaceTraitsIneligibleForCurrentRole(player, visitor);
    }
}
