package dev.caecorthus.sparkdlc.client.role.civilian.seeker.render;

import dev.caecorthus.sparkdlc.SparkWitchItems;
import dev.caecorthus.sparkdlc.client.role.civilian.seeker.SeekerClientState;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerRules;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerSessionMode;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerCarEntity;
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
 * Client only: renders the baked toy-car model (front at model north) at the entity's interpolated yaw, resting on
 * its support. The local owner does not see its own car model while possessing it.
 * 仅客户端：按实体插值后的偏航渲染烘焙的玩具小车模型（模型北侧为车头），贴合支撑面。本地拥有者在附身小车时不渲染自己的车模。
 */
public class SeekerCarEntityRenderer extends EntityRenderer<SeekerCarEntity> {
    /** ItemRenderer centres the 16-px model; lift it so model y=0 sits on the entity feet. / 使模型底部贴合实体脚底。 */
    private static final double MODEL_Y_OFFSET = 0.5;
    private final ItemRenderer itemRenderer;
    private final ItemStack stack;

    public SeekerCarEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
        stack = new ItemStack(SparkWitchItems.seekerCar());
        shadowRadius = 0.2F;
        shadowOpacity = 0.6F;
    }

    @Override
    public void render(SeekerCarEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        if (!isPossessedLocally(entity)) {
            matrices.push();
            matrices.translate(0.0, MODEL_Y_OFFSET, 0.0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - yaw));
            itemRenderer.renderItem(stack, ModelTransformationMode.FIXED, false, matrices, vertexConsumers, light,
                    OverlayTexture.DEFAULT_UV, SeekerModels.model(SeekerRules.CAR_PLACED_MODEL_ID));
            matrices.pop();
        }
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private static boolean isPossessedLocally(SeekerCarEntity entity) {
        return SeekerClientState.sessionMode() == SeekerSessionMode.CAR
                && SeekerClientState.carEntityId() == entity.getId();
    }

    @Override
    public Identifier getTexture(SeekerCarEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }
}
