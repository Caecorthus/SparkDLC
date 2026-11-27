package dev.caecorthus.sparkdlc.client.hooks;

import dev.caecorthus.sparkdlc.entity.RealityAnchorEntity;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerRules;
import dev.doctor4t.wathe.api.event.GetInstinctHighlight;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side reality anchor rules: within 15 blocks of a placed anchor the local player's Alt instinct look is denied
 * (by wrapping Wathe's two keyed instinct gates like the Control Expert stun gate does), and holding the instinct key
 * outlines the anchor itself through walls in the Seeker's role colour.
 * 现实稳定锚的客户端规则：距已放置的锚 15 格以内时本地玩家的 Alt 本能透视被拒绝（做法与控场专家眩晕拦截相同，包装 Wathe 的
 * 两个按键本能入口）；按住本能键时，锚本身会以搜寻者职业色透墙描边。
 */
public final class RealityAnchorClientHooks {
    /** Instinct denial radius. / 本能禁用半径。 */
    private static final double INSTINCT_BLOCK_RADIUS = 15.0D;
    /** Anchor outline priority: above the default entity outlines, below skip(). / 锚描边优先级。 */
    private static final int OUTLINE_PRIORITY = GetInstinctHighlight.HighlightResult.PRIORITY_DEFAULT + 40;
    private static boolean registered;

    private RealityAnchorClientHooks() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        GetInstinctHighlight.EVENT.register(RealityAnchorClientHooks::anchorHighlight);
        // The placed model is parsed and drawn by BlockbenchRotatedModel, so it is not registered with the model loader.
        // 放置模型由 BlockbenchRotatedModel 自行解析绘制，不经过模型加载器。
    }

    /**
     * The anchor shown through walls while the instinct key is held (owner 2026-11-27), painted in the Seeker's role
     * colour. Keyed, so it never shows up as an always-on outline.
     * 按住本能键时透墙显示锚（所有者 2026-11-27），用搜寻者职业色。使用按键门控，因此不会变成常驻描边。
     */
    @Nullable
    private static GetInstinctHighlight.HighlightResult anchorHighlight(Entity target) {
        if (!(target instanceof RealityAnchorEntity)) {
            return null;
        }
        return GetInstinctHighlight.HighlightResult.withKeybind(SeekerRules.COLOR, OUTLINE_PRIORITY);
    }

    /** Whether an anchor is close enough to deny the local player's instinct look. / 附近的锚是否禁用本地玩家的本能。 */
    public static boolean blocksLocalInstinct() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return false;
        }
        Box search = player.getBoundingBox().expand(INSTINCT_BLOCK_RADIUS);
        for (Entity entity : player.getWorld().getOtherEntities(player, search, e -> e instanceof RealityAnchorEntity)) {
            if (entity.squaredDistanceTo(player) <= INSTINCT_BLOCK_RADIUS * INSTINCT_BLOCK_RADIUS) {
                return true;
            }
        }
        return false;
    }
}
