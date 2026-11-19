package dev.caecorthus.sparkdlc.bridge.witch;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * SparkTraits seam for USEC. Calls only the public facade
 * {@code SparkTraitsApi.getMarksmanRangeMultiplier(PlayerEntity)}, {@code hasActiveTrait(PlayerEntity, Identifier)}
 * and the server-only {@code isHeavyArtilleryGunShot(ServerPlayerEntity, ServerPlayerEntity)}. SparkTraits is fused
 * into SparkDLC, so the calls are direct; a null player still means base rifle range, the base bolt time and no Heavy
 * Artillery layer. Both sides may call the first two.
 * USEC 的 SparkTraits 接缝。仅调用公共门面 {@code getMarksmanRangeMultiplier}、{@code hasActiveTrait} 与仅服务端的
 * {@code isHeavyArtilleryGunShot}。SparkTraits 已融合进 SparkDLC，因此均为直接调用；玩家为空时，狙击枪仍使用基础射程、
 * 基础拉栓时间且没有重炮手额外层数。前两者两端均可调用。
 */
public final class SparkTraitsUsecBridge {
    private static final Identifier FAST_RELOAD = Identifier.of("sparktraits", "fast_reload");

    private SparkTraitsUsecBridge() {
    }

    /**
     * SparkTraits Marksman range multiplier for the rifle, clamped to [1.0, 1.3]; a null player is 1.0.
     * Shared with the Control Expert through {@link SparkTraitsMarksmanBridge}.
     * 狙击枪使用的 SparkTraits 精确枪手射程倍率，限制在 [1.0, 1.3]；玩家为空时为 1.0。经
     * {@link SparkTraitsMarksmanBridge} 与控场专家共用。
     */
    public static double marksmanRangeMultiplier(@Nullable PlayerEntity player) {
        return SparkTraitsMarksmanBridge.marksmanRangeMultiplier(player);
    }

    /**
     * Whether SparkTraits Fast Reload (快速装填) is active; false for a null player.
     * SparkTraits 快速装填是否生效；玩家为空时为 false。
     */
    public static boolean hasFastReload(@Nullable PlayerEntity player) {
        return player != null && SparkTraitsApi.hasActiveTrait(player, FAST_RELOAD);
    }

    /**
     * Server only (O3): whether SparkTraits counts this {@code wathe:gun_shot} kill of {@code victim} by
     * {@code shooter} as a Heavy Artillery shot (active trait on a gun-police role, within 5 blocks feet to feet), which
     * gives the AXMC one more shield layer. Ask it before the kill, because a death clears traits. A null shooter or
     * victim is false.
     * 仅服务端（O3）：SparkTraits 是否把 {@code shooter} 以 {@code wathe:gun_shot} 击杀 {@code victim} 视为重炮手射击（持枪警职
     * 持有生效词条、脚对脚 5 格内），这会让 AXMC 多击穿一层护盾。须在击杀之前询问，因为死亡会清除词条。射手或受害者为空时
     * 为 false。
     */
    public static boolean isHeavyArtilleryGunShot(@Nullable ServerPlayerEntity shooter,
                                                  @Nullable ServerPlayerEntity victim) {
        return shooter != null && victim != null && SparkTraitsApi.isHeavyArtilleryGunShot(shooter, victim);
    }
}
