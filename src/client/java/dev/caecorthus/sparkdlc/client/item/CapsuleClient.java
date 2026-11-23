package dev.caecorthus.sparkdlc.client.item;

import dev.caecorthus.sparkdlc.SparkDlcEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

/**
 * Client render registration for the capsule entity.
 * 胶囊实体的客户端渲染注册。
 */
public final class CapsuleClient {
    private CapsuleClient() {
    }

    public static void register() {
        EntityRendererRegistry.register(SparkDlcEntities.capsule(), FlyingItemEntityRenderer::new);
    }
}
