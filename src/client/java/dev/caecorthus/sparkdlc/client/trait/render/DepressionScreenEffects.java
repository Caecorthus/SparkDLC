package dev.caecorthus.sparkdlc.client.trait.render;

import com.google.gson.JsonSyntaxException;
import dev.caecorthus.sparkdlc.SparkTraits;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.trait.component.TraitPlayerComponent;
import dev.caecorthus.sparkdlc.trait.civilian.depression.DepressionRules;
import dev.caecorthus.sparkdlc.trait.civilian.CivilianTraits;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.network.ClientPlayerEntity;
import dev.caecorthus.sparkdlc.client.trait.killer.LastEscapeWitchBridge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.IOException;

/**
 * Client-only StarRailExpress-style grayscale post effect for the Depression trait.
 * 抑郁天赋的 StarRailExpress 风格客户端灰阶后处理；理智越低，视角越灰白。
 */
public final class DepressionScreenEffects {
    private static final Identifier GRAYSCALE_SHADER = SparkTraits.id("shaders/post/depression_insanity.json");
    private static final float DESATURATE_FACTOR = 0.69f;
    private static final float SPREAD_FACTOR = 1.43f;
    private static final ScreenEffect NO_EFFECT = new ScreenEffect(0.0f, 0.0f, 1.0f);
    /**
     * Shared neutral answer of {@link #getLastEscapeComposition}: callers only read it and never write into it.
     * {@link #getLastEscapeComposition} 的共享中性结果：调用方只读不写。
     */
    private static final float[] NO_ESCAPE_COMPOSITION = {0.0f, 0.0f, 1.0f};
    /** An unused processor is kept this long before its framebuffers are freed. 处理器闲置超过该时长后释放其帧缓冲。 */
    private static final long IDLE_CLOSE_MS = 5_000L;
    private static boolean loadFailed;
    private static Framebuffer processorTarget;
    private static PostEffectProcessor processor;
    private static int processorWidth = -1;
    private static int processorHeight = -1;
    private static long lastUsedMs;

    private DepressionScreenEffects() {
    }

    public static void render(ClientPlayerEntity player, float delta) {
        if (!SparkDlcServerConnection.isConfirmedServer()) {
            closeProcessor();
            return;
        }
        // Delegation performs the pass now, so mixin ordering cannot cause a second pass. SparkWitch's compositor is
        // called directly (the modules are fused in SparkDLC); it reads the parameters back through
        // getLastEscapeComposition.
        // 委托在此刻执行后处理，因此 mixin 顺序不会造成第二次后处理。各模块已融合进 SparkDLC，这里直接调用 SparkWitch
        // 合成器；它通过 getLastEscapeComposition 取回参数。
        if (SparkTraitsApi.hasLastEscapeGrayscale(player) && LastEscapeWitchBridge.tryRender(player, delta)) {
            releaseIfIdle(player);
            return;
        }
        ScreenEffect effect = screenEffect(player);
        if (!effect.isVisible()) {
            releaseIfIdle(player);
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        PostEffectProcessor activeProcessor = ensureProcessor(client);
        if (activeProcessor == null) {
            return;
        }
        lastUsedMs = Util.getMeasuringTimeMs();

        activeProcessor.setUniforms("DesaturateFactor", effect.desaturateFactor());
        activeProcessor.setUniforms("SpreadFactor", effect.spreadFactor());
        activeProcessor.setUniforms("Brightness", effect.brightness());
        try {
            activeProcessor.render(delta);
        } finally {
            client.getFramebuffer().beginWrite(false);
        }
    }

    /*
     * An invisible frame only skips the pass: mood crossing the threshold or a psycho toggle would otherwise rebuild the
     * processor (JSON parse, shader link, framebuffers) on the next visible frame. It is closed once unused for a few
     * seconds, or right away when the round is over.
     * 不可见的帧只跳过本次后处理：否则心情跨过阈值或疯魔切换后，下一可见帧就要重建处理器（解析 JSON、链接着色器、创建帧缓冲）。
     * 闲置数秒后关闭；对局结束时立即关闭。
     */
    private static void releaseIfIdle(PlayerEntity player) {
        if (processor != null && (Util.getMeasuringTimeMs() - lastUsedMs > IDLE_CLOSE_MS
                || !GameWorldComponent.KEY.get(player.getWorld()).isRunning())) {
            closeProcessor();
        }
    }

    private static ScreenEffect screenEffect(ClientPlayerEntity player) {
        if (SparkTraitsApi.hasLastEscapeGrayscale(player)) {
            return escapeComposition(player);
        }
        // SparkWitch supplies the exact Wraith grayscale, so Depression must not stack another processor.
        // SparkWitch 提供精确的冤魂灰阶，因此抑郁效果不得再叠加后处理器。
        if (SparkTraitsApi.isWraithActive(player)) {
            return NO_EFFECT;
        }
        return depressionEffect(player);
    }

    /**
     * Witch compositor contract, called directly by SparkWitch's {@code BlackRavenPerceptionScreenEffects} (the
     * modules are fused in SparkDLC): client visual parameters [desaturation, spread, brightness], including stronger
     * Depression. Without an escape (null player, unconfirmed server, no escape grayscale) or with invalid parameters
     * it returns one shared neutral array [0, 0, 1], so the per-frame query allocates nothing while no escape is
     * active; callers must treat every result as read-only. This query must never delegate to another compositor or
     * render anything.
     * Witch 合成器契约，由 SparkWitch 的 {@code BlackRavenPerceptionScreenEffects} 直接调用（各模块已融合进 SparkDLC）：
     * 客户端返回含更强抑郁效果的[灰阶、扩散、亮度]。无脱险（玩家为空、服务端未确认、无脱险灰阶）或参数无效时返回同一个
     * 共享中性数组 [0, 0, 1]，因此没有脱险时每帧查询不分配内存；调用方必须把结果视为只读。此查询不调用其他合成器，
     * 也不执行渲染。
     */
    public static float[] getLastEscapeComposition(PlayerEntity player) {
        if (player == null || !SparkDlcServerConnection.isConfirmedServer()
                || !SparkTraitsApi.hasLastEscapeGrayscale(player)) {
            return NO_ESCAPE_COMPOSITION;
        }
        ScreenEffect effect = escapeComposition(player);
        float desaturation = effect.desaturateFactor();
        float spread = effect.spreadFactor();
        float brightness = effect.brightness();
        // Never hand malformed parameters to the compositor: they read as "no escape", so Traits' fallback draws.
        // 不向合成器交出无效参数：它们视为“无脱险”，由 Traits 回退绘制。
        if (!LastEscapeVisionRules.isValid(desaturation, spread, brightness)) {
            return NO_ESCAPE_COMPOSITION;
        }
        return new float[] {desaturation, spread, brightness};
    }

    private static ScreenEffect escapeComposition(PlayerEntity player) {
        ScreenEffect depression = depressionEffect(player);
        // A weaker Depression effect must not darken/spread the fixed 50% escape vision.
        // A stronger effect retains its original spread and brightness instead of stacking two passes.
        float[] selected = LastEscapeVisionRules.compose(
                depression.desaturateFactor(), depression.spreadFactor(), depression.brightness());
        return new ScreenEffect(selected[0], selected[1], selected[2]);
    }

    private static ScreenEffect depressionEffect(PlayerEntity player) {
        TraitPlayerComponent traits = TraitPlayerComponent.KEY.get(player);
        if (player.isCreative() || player.isSpectator()) {
            return NO_EFFECT;
        }
        float mood = PlayerMoodComponent.KEY.get(player).getMood();
        float strength = DepressionRules.depressionScreenEffectStrength(
                traits.hasActiveTrait(CivilianTraits.DEPRESSION),
                traits.isDepressionPsychoActive(),
                mood
        );
        return new ScreenEffect(strength * DESATURATE_FACTOR, strength * SPREAD_FACTOR, 1.2f);
    }

    private static PostEffectProcessor ensureProcessor(MinecraftClient client) {
        Framebuffer framebuffer = client.getFramebuffer();
        if (loadFailed || framebuffer.textureWidth <= 0 || framebuffer.textureHeight <= 0) {
            return null;
        }
        if (processor != null && processorTarget != framebuffer) {
            closeProcessor();
        }
        if (processor != null) {
            if (processorWidth != framebuffer.textureWidth || processorHeight != framebuffer.textureHeight) {
                // A window resize only resizes the pass targets, as vanilla does for its own post effects.
                // 窗口尺寸变化时只调整后处理目标的大小，与原版处理自身后处理效果的方式一致。
                processor.setupDimensions(framebuffer.textureWidth, framebuffer.textureHeight);
                processorWidth = framebuffer.textureWidth;
                processorHeight = framebuffer.textureHeight;
            }
            return processor;
        }

        try {
            processor = new PostEffectProcessor(
                    client.getTextureManager(),
                    client.getResourceManager(),
                    framebuffer,
                    GRAYSCALE_SHADER
            );
            processor.setupDimensions(framebuffer.textureWidth, framebuffer.textureHeight);
            processorWidth = framebuffer.textureWidth;
            processorHeight = framebuffer.textureHeight;
            processorTarget = framebuffer;
            return processor;
        } catch (IOException | JsonSyntaxException exception) {
            SparkTraits.LOGGER.warn("Unable to load Depression/Last Escape grayscale shader", exception);
            closeProcessor();
            loadFailed = true;
            return null;
        }
    }

    /** Called on the render thread on resource reload, disconnect and client shutdown. */
    public static void close() {
        closeProcessor();
        loadFailed = false;
    }

    private static void closeProcessor() {
        if (processor != null) {
            processor.close();
            processor = null;
        }
        processorWidth = -1;
        processorHeight = -1;
        processorTarget = null;
    }

    private record ScreenEffect(float desaturateFactor, float spreadFactor, float brightness) {
        private boolean isVisible() {
            return desaturateFactor > 0.001f || spreadFactor > 0.001f;
        }
    }
}
