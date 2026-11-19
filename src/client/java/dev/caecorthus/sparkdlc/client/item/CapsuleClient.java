package dev.caecorthus.sparkdlc.client.item;

import dev.caecorthus.sparkdlc.SparkStrengthEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

/**
 * Client render registration for SparkStrength items/entities.
 * SparkStrength 物品/实体的客户端渲染注册。
 */
public final class CapsuleClient {
    private CapsuleClient() {
    }

    public static void register() {
        EntityRendererRegistry.register(SparkStrengthEntities.capsule(), FlyingItemEntityRenderer::new);
    }
}
