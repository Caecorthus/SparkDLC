package dev.caecorthus.sparkdlc.bridge.witch;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Cross-module seam: the Abyss Listener's own SparkTraits reads, through the public {@code SparkTraitsApi} facade only
 * ({@code isLastStandPending(PlayerEntity)} and {@code hasActiveTrait(PlayerEntity, Identifier)} for Conscience and
 * Impostor); never {@code sparktraits.impl} or {@code sparktraits.component}. SparkTraits is fused into SparkDLC, so
 * the calls are direct. A null player: not pending, and trait queries read as unknown ({@code null}) so the caller
 * fails closed.
 * 跨模块接缝：聆渊者自有的 SparkTraits 读取，仅经由公共门面 {@code SparkTraitsApi}（{@code isLastStandPending} 与
 * 针对良知、内鬼的 {@code hasActiveTrait}），从不触及 {@code impl} 或 {@code component} 包。SparkTraits 已融合进
 * SparkDLC，因此均为直接调用。玩家为空时：非待定，词条查询视为未知（{@code null}），由调用方失败关闭。
 */
public final class SparkTraitsAbyssListenerBridge {
    private static final String MOD_ID = "sparkdlc";
    static final Identifier CONSCIENCE = Identifier.of(MOD_ID, "conscience");
    static final Identifier IMPOSTOR = Identifier.of(MOD_ID, "impostor");

    private SparkTraitsAbyssListenerBridge() {
    }

    /**
     * Whether SparkTraits Last Stand currently holds this player's pending death transition; a null player reads as
     * false. The query is shared with the Seeker through {@link SparkTraitsLastStandBridge#isLastStandPending}.
     * SparkTraits 背水一战当前是否持有该玩家的待决死亡转换；玩家为空时为 false。查询经
     * {@link SparkTraitsLastStandBridge#isLastStandPending} 与搜寻者共用。
     */
    public static boolean isLastStandPending(@Nullable PlayerEntity player) {
        return SparkTraitsLastStandBridge.isLastStandPending(player);
    }

    /** Conscience trait: null (unknown) for a null player. / 良知词条：玩家为空时为 null（未知）。 */
    public static @Nullable Boolean isConscienceActive(@Nullable PlayerEntity player) {
        return hasActiveTrait(player, CONSCIENCE);
    }

    /** Impostor trait: null (unknown) for a null player. / 内鬼词条：玩家为空时为 null（未知）。 */
    public static @Nullable Boolean isImpostorActive(@Nullable PlayerEntity player) {
        return hasActiveTrait(player, IMPOSTOR);
    }

    private static @Nullable Boolean hasActiveTrait(@Nullable PlayerEntity player, Identifier traitId) {
        return player == null ? null : Boolean.valueOf(SparkTraitsApi.hasActiveTrait(player, traitId));
    }
}
