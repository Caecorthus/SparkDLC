package dev.caecorthus.sparkdlc.client.gui.card;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lets SparkAssist decorate the owner info card (frame plates, faction sigil, bookmark tab, charm, rim foliage).
 * Resolved once by reflection against SparkAssist's public {@code GuidebookDecorApi}; after any failure every call
 * is a no-op and the card looks exactly as before. The single shared copy for SparkWitch and SparkTraits (each mod
 * used to ship its own); state is per instance, so each mod resolves, logs under its own logger name and disables
 * its own decoration only. The modules are fused in SparkDLC, so SparkAssist is always present and the lookup is no
 * longer gated on its mod id; it stays a lookup by name because factionapi is the base contract and must not import
 * a sibling module.
 * 由 SparkAssist 为信息卡绘制点缀（角板、阵营暗纹、折角书签、吊坠、贴边枝叶）。通过反射解析一次 SparkAssist 公开的
 * GuidebookDecorApi；任一次失败后，所有调用都是空操作，卡片与原来完全一样。SparkWitch 与 SparkTraits 共用的唯一实现
 * （原先两个模组各带一份）；状态在实例上，因此各模组各自解析、用自己的日志名记录，并且只禁用自己的点缀。各模块已融合进
 * SparkDLC，SparkAssist 始终存在，查找不再以其 mod id 为前提；但 factionapi 是基础契约、不得 import 兄弟模块，因此仍仅按
 * 名称访问该 API。
 */
public final class GuideDecorBridge implements InventoryInfoCard.Decor {
    private static final String API = "dev.caecorthus.sparkdlc.client.guidebook.ui.decor.GuidebookDecorApi";
    private final Logger logger;
    private boolean resolved;
    private MethodHandle chrome;
    private MethodHandle overlay;

    /**
     * @param loggerName the owning mod's logger name for the two warnings below, e.g. {@code "SparkTraits/GuideDecor"}.
     *                   所属模组用于下面两条警告的日志名，例如 {@code "SparkTraits/GuideDecor"}。
     */
    public GuideDecorBridge(String loggerName) {
        this.logger = LoggerFactory.getLogger(loggerName);
    }

    /** Frame ornaments and sigil: call right after the card panel, before its rows. 面板之后、行之前调用。 */
    @Override
    public void cardChrome(DrawContext context, int x, int y, int width, int height) {
        invoke(chrome(), context, x, y, width, height);
    }

    /** Tab, charm and foliage: call after the card rows. 行之后调用。 */
    @Override
    public void cardOverlay(DrawContext context, int x, int y, int width, int height) {
        invoke(overlay(), context, x, y, width, height);
    }

    private void invoke(MethodHandle handle, DrawContext context, int x, int y, int width, int height) {
        if (handle == null) {
            return;
        }
        try {
            handle.invokeExact(context, x, y, width, height);
        } catch (Throwable failure) {
            // Decoration must never take the inventory down with it: disable it for the session.
            // 点缀不能拖垮背包界面：本次会话内禁用。
            logger.warn("SparkAssist guide decoration failed; disabling it for this session", failure);
            chrome = null;
            overlay = null;
        }
    }

    private MethodHandle chrome() {
        resolve();
        return chrome;
    }

    private MethodHandle overlay() {
        resolve();
        return overlay;
    }

    private synchronized void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        try {
            Class<?> api = Class.forName(API, false, GuideDecorBridge.class.getClassLoader());
            MethodType type = MethodType.methodType(void.class, DrawContext.class, int.class, int.class, int.class,
                    int.class);
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            chrome = lookup.findStatic(api, "cardChrome", type);
            overlay = lookup.findStatic(api, "cardOverlay", type);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            logger.warn("SparkAssist is installed but its guide decoration API is unavailable", exception);
            chrome = null;
            overlay = null;
        }
    }
}
