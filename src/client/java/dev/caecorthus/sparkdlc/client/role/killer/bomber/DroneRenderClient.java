package dev.caecorthus.sparkdlc.client.role.killer.bomber;

import dev.caecorthus.sparkdlc.SparkDlcEntities;
import dev.caecorthus.sparkdlc.client.render.entity.BombDroneModel;
import dev.caecorthus.sparkdlc.client.render.entity.DroneEntityRenderer;
import dev.caecorthus.sparkdlc.client.render.entity.GrenadeDroneModel;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/** Entity renderers + model layers for both drones. / 两种无人机的实体渲染器与模型层。 */
public final class DroneRenderClient {
    private DroneRenderClient() {
    }

    public static void register() {
        EntityModelLayerRegistry.registerModelLayer(GrenadeDroneModel.LAYER, GrenadeDroneModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(BombDroneModel.LAYER, BombDroneModel::getTexturedModelData);
        EntityRendererRegistry.register(SparkDlcEntities.grenadeDrone(), DroneEntityRenderer::grenade);
        EntityRendererRegistry.register(SparkDlcEntities.bombDrone(), DroneEntityRenderer::bomb);
    }
}
