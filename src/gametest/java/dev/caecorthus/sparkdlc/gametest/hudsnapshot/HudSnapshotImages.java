package dev.caecorthus.sparkdlc.gametest.hudsnapshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;

/**
 * Pictures of {@code ./gradlew runHudSnapshot}: the whole window as the game last drew it, and the top-left corner
 * cut out and enlarged without smoothing, so single GUI pixels stay countable. Render thread only.
 * {@code ./gradlew runHudSnapshot} 的图片：游戏最近一次绘制的整个窗口，以及裁出并无平滑放大的左上角，使单个 GUI 像素仍可逐个数清。
 * 仅限渲染线程。
 */
final class HudSnapshotImages {
    /**
     * The corner that is cut out, in GUI pixels: wide enough for Wathe's longest task row in Simplified Chinese
     * (it ends near x 180 beside the figure).
     * 裁出的角落，单位为 GUI 像素：宽度足以容纳 Wathe 最长的简体中文任务行（在示意图旁边时它结束于 x 180 附近）。
     */
    static final int CORNER_GUI_WIDTH = 190, CORNER_GUI_HEIGHT = 140;
    /** Nearest-neighbour enlargement of the corner. / 角落的最近邻放大倍数。 */
    static final int CORNER_ZOOM = 2;

    private HudSnapshotImages() {
    }

    /**
     * Saves {@code <name>.png} (full window) and {@code <name>-topleft.png} into {@code directory}. The main
     * framebuffer still holds the frame of the previous render pass, HUD included, when a client tick runs.
     * 把 {@code <name>.png}（整个窗口）与 {@code <name>-topleft.png} 保存到 {@code directory}。客户端 tick 运行时，主帧缓冲仍保存着
     * 上一次渲染的画面（包含 HUD）。
     */
    static void save(MinecraftClient client, Path directory, String name) throws IOException {
        Files.createDirectories(directory);
        int scale = (int) Math.round(client.getWindow().getScaleFactor());
        try (NativeImage full = ScreenshotRecorder.takeScreenshot(client.getFramebuffer())) {
            full.writeTo(directory.resolve(name + ".png"));
            int width = Math.min(full.getWidth(), CORNER_GUI_WIDTH * scale);
            int height = Math.min(full.getHeight(), CORNER_GUI_HEIGHT * scale);
            try (NativeImage corner = new NativeImage(width * CORNER_ZOOM, height * CORNER_ZOOM, false)) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int color = full.getColor(x, y);
                        for (int dy = 0; dy < CORNER_ZOOM; dy++) {
                            for (int dx = 0; dx < CORNER_ZOOM; dx++) {
                                corner.setColor(x * CORNER_ZOOM + dx, y * CORNER_ZOOM + dy, color);
                            }
                        }
                    }
                }
                corner.writeTo(directory.resolve(name + "-topleft.png"));
            }
        }
    }
}
