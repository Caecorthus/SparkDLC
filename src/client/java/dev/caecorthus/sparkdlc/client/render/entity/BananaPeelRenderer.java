package dev.caecorthus.sparkdlc.client.render.entity;

import dev.caecorthus.sparkdlc.entity.BananaPeelEntity;
import dev.caecorthus.sparkdlc.item.fruit.FruitItems;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * Draws the peel as a flat item on the floor — the same trick Wathe's note entity uses, so no bespoke model is needed.
 * 把果皮当作平放的物品画出来——和 Wathe 便签实体同样的手法，因此不需要专门做模型。
 */
public class BananaPeelRenderer extends EntityRenderer<BananaPeelEntity> {

    private final ItemRenderer itemRenderer;

    public BananaPeelRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(BananaPeelEntity peel, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0.0D, 0.02D, 0.0D);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(peel.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
        matrices.scale(0.5F, 0.5F, 0.5F);
        this.itemRenderer.renderItem(new ItemStack(FruitItems.bananaPeel()), ModelTransformationMode.FIXED, light,
                OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, peel.getWorld(), peel.getId());
        matrices.pop();
        super.render(peel, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(BananaPeelEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }
}
