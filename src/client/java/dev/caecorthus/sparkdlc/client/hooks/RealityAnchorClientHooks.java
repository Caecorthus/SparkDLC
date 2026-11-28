package dev.caecorthus.sparkdlc.client.hooks;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.client.render.entity.BlockbenchRotatedModel;
import dev.caecorthus.sparkdlc.entity.RealityAnchorEntity;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerRules;
import dev.doctor4t.wathe.api.event.GetInstinctHighlight;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
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
    private static final Identifier MODEL_RELOAD_LISTENER_ID = SparkDLC.id("reality_anchor_model");
    /** Client tick the cached answer below belongs to. / 下方缓存结果所属的客户端刻。 */
    private static int checkedAtTick = Integer.MIN_VALUE;
    private static boolean blocksInstinct;
    private static boolean registered;

    private RealityAnchorClientHooks() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        GetInstinctHighlight.EVENT.register(RealityAnchorClientHooks::anchorHighlight);
        // The placed model is parsed and drawn by BlockbenchRotatedModel, not by the model loader; it is re-read with
        // every resource reload so a pack change or F3+T never leaves stale geometry behind.
        // 放置模型由 BlockbenchRotatedModel 自行解析绘制，不经过模型加载器；每次资源重载都会重新读取，更换资源包或按 F3+T
        // 都不会留下过期的几何数据。
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return MODEL_RELOAD_LISTENER_ID;
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        BlockbenchRotatedModel.reload(manager);
                    }
                });
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

    /**
     * Whether an anchor is close enough to deny the local player's instinct look. Wathe asks its instinct gates for
     * every outlined entity of every frame, so the entity search runs once per client tick and the answer is reused.
     * A spectating viewer (a dead player included) is never gated, like under the mod's other instinct gates.
     * 附近的锚是否禁用本地玩家的本能。Wathe 每帧都会为每个被描边的实体询问本能入口，因此实体搜索每个客户端刻只做一次，
     * 结果重复使用。旁观视角（包括已死亡的玩家）永不受拦截，与本模组其他本能拦截一致。
     */
    public static boolean blocksLocalInstinct() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || player.isSpectator()) {
            return false;
        }
        int tick = client.inGameHud.getTicks();
        if (tick != checkedAtTick) {
            checkedAtTick = tick;
            blocksInstinct = anchorInRange(player);
        }
        return blocksInstinct;
    }

    private static boolean anchorInRange(ClientPlayerEntity player) {
        Box search = player.getBoundingBox().expand(INSTINCT_BLOCK_RADIUS);
        for (Entity entity : player.getWorld().getOtherEntities(player, search,
                e -> e instanceof RealityAnchorEntity && !e.isRemoved())) {
            if (entity.squaredDistanceTo(player) <= INSTINCT_BLOCK_RADIUS * INSTINCT_BLOCK_RADIUS) {
                return true;
            }
        }
        return false;
    }
}
