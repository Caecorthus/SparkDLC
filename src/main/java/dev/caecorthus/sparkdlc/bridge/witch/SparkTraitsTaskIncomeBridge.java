package dev.caecorthus.sparkdlc.bridge.witch;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Cross-module seam for role-owned task income that must not stack with SparkTraits' own Impostor task pay. Calls only
 * the public facade {@code SparkTraitsApi.hasActiveTrait(PlayerEntity, Identifier)} for {@code sparktraits:impostor},
 * never its {@code impl} or {@code component} packages. SparkTraits is fused into SparkDLC, so the call is direct:
 * TRUE/FALSE from the facade, null ("unknown") only for a null player; callers then pay nothing (fail closed). Server
 * use only (task completion). Read by the Discoverer, the Accountant, BEAR and Tono Hanna.
 * 角色自有任务收入的跨模块接缝，该收入不得与 SparkTraits 自行支付的内鬼任务收入叠加。只调用公共门面
 * {@code SparkTraitsApi.hasActiveTrait} 查询 {@code sparktraits:impostor}，从不触及 {@code impl} 或 {@code component} 包。
 * SparkTraits 已融合进 SparkDLC，因此为直接调用：门面给出 TRUE/FALSE，仅在玩家为空时为 null（“未知”），调用方随即不发放
 * （失败即关闭）。仅服务端（任务完成）使用。探索者、会计、BEAR 与远野汉娜读取。
 */
public final class SparkTraitsTaskIncomeBridge {
    private static final String MOD_ID = "sparktraits";
    private static final Identifier IMPOSTOR = Identifier.of(MOD_ID, "impostor");

    private SparkTraitsTaskIncomeBridge() {
    }

    /**
     * TRUE/FALSE from the facade; null when no player is given.
     * 门面给出 TRUE/FALSE；未提供玩家时为 null。
     */
    public static @Nullable Boolean isImpostor(@Nullable PlayerEntity player) {
        if (player == null) {
            return null;
        }
        return SparkTraitsApi.hasActiveTrait(player, IMPOSTOR);
    }
}
