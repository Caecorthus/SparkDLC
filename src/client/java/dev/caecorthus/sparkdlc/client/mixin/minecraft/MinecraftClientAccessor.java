package dev.caecorthus.sparkdlc.client.mixin.minecraft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * The mod's one accessor on {@link MinecraftClient} (the scope and the spirit possession camera each had their own).
 * 本 mod 在 {@link MinecraftClient} 上唯一的访问器（瞄准镜与灵魂附身镜头此前各有一个）。
 */
@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    /**
     * Writes {@code MinecraftClient.cameraEntity} without {@code setCameraEntity}: vanilla's setter also calls
     * {@code GameRenderer.onCameraEntitySet}, which drops the post-processing shader, and NoellesRoles only loads its
     * spirit grayscale when its projection starts, so a possession swap through the setter would leave the spirit view
     * in colour.
     * 不经 setCameraEntity 直接写入 MinecraftClient.cameraEntity：原版的 setter 还会调用 GameRenderer.onCameraEntitySet 丢弃
     * 后处理着色器，而 NoellesRoles 只在出窍开始时加载灵界灰度，经 setter 切换附身镜头会让灵魂画面恢复彩色。
     */
    @Accessor("cameraEntity")
    void sparkdlc$setCameraEntityField(Entity entity);

    /**
     * Lets the PiP lens pass point {@code MinecraftClient#getFramebuffer()} at its lens target for the length of one
     * {@code WorldRenderer#render} call, because vanilla re-binds {@code client.getFramebuffer()} inside that method
     * (after the entity-outline clear and post pass) and {@code RenderPhase} targets bind it to end their draws. Only
     * {@code ScopePictureInPicture} calls it, always restoring the main framebuffer in a {@code finally} on the render
     * thread before anything else reads it.
     * 让画中画镜内渲染在一次 {@code WorldRenderer#render} 调用期间把 {@code MinecraftClient#getFramebuffer()} 指向镜内目标：
     * 原版在该方法内（实体描边清除与后处理之后）会重新绑定 {@code client.getFramebuffer()}，{@code RenderPhase} 目标结束绘制时
     * 也会绑定它。仅 {@code ScopePictureInPicture} 调用，并总在渲染线程的 {@code finally} 中、任何其他代码读取之前恢复主帧缓冲。
     */
    @Mutable
    @Accessor("framebuffer")
    void sparkdlc$setScopeFramebuffer(Framebuffer framebuffer);
}
