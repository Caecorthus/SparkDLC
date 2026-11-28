package dev.caecorthus.sparkdlc.client.render.entity;

import dev.caecorthus.sparkdlc.entity.RealityAnchorEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

/**
 * Draws the placed reality stabilization anchor with {@link BlockbenchRotatedModel}: the Blockbench export carries
 * per-element rotations vanilla cannot load, so it is parsed and drawn directly. The anchor is centred on the entity,
 * scaled to half a block and sits with its bottom on the entity origin.
 * 用 {@link BlockbenchRotatedModel} 绘制已放置的现实稳定锚：这份 Blockbench 导出带原版无法加载的逐元素旋转，因此直接解析并绘制。
 * 锚以实体为中心、整体半个方块大、底面落在实体原点。
 */
public final class RealityAnchorEntityRenderer extends EntityRenderer<RealityAnchorEntity> {
    /** Model span along X is 29 px; scaling by this makes it half a block. / 模型 X 跨度 29 像素，乘此系数后为半格。 */
    private static final float SCALE = 0.2759F;
    /** Baked-model bounds in blocks: centre X, bottom Y. / 模型包围盒（方块）：X 中心、Y 底。 */
    private static final float CENTER_X = 0.75F;
    private static final float MIN_Y = -0.4518F;

    public RealityAnchorEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(RealityAnchorEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public void render(RealityAnchorEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.scale(SCALE, SCALE, SCALE);
        matrices.translate(-CENTER_X, -MIN_Y, 0.0D);
        BlockbenchRotatedModel.render(matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
