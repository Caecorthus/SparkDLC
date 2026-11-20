package dev.caecorthus.sparkdlc.client.role.civilian.engineer;

import dev.caecorthus.sparkdlc.SparkDlcEntities;
import dev.caecorthus.sparkdlc.client.render.entity.CaptureDeviceEntityRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * 工程师增强的客户端注册入口。
 */
public final class EngineerClientHooks {
    private EngineerClientHooks() {
    }

    public static void register() {
        EntityRendererRegistry.register(SparkDlcEntities.captureDevice(), CaptureDeviceEntityRenderer::new);
    }
}
