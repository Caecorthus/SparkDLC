package dev.caecorthus.sparkdlc.client.guidebook.ui.decor;

import dev.caecorthus.sparkdlc.client.gui.card.InventoryInfoCard;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The guidebook's decoration of one owner info card (frame plates, faction sigil, bookmark tab, charm, rim foliage):
 * {@link GuidebookDecorApi} behind the card's {@link InventoryInfoCard.Decor} hook. After any failure every call is
 * a no-op and the card looks exactly as it does undecorated. State is per instance: the role card and the trait card
 * each create their own, log under their own logger name and disable their own decoration only.
 * 指南为一张信息卡绘制的点缀（角板、阵营暗纹、折角书签、吊坠、贴边枝叶）：把 {@link GuidebookDecorApi} 接到卡片的
 * {@link InventoryInfoCard.Decor} 钩子上。任一次失败后，所有调用都是空操作，卡片与没有点缀时完全一样。状态在实例上：
 * 职业卡与词条卡各自创建实例、用自己的日志名记录，并且只禁用自己的点缀。
 */
public final class GuidebookCardDecor implements InventoryInfoCard.Decor {
    private final Logger logger;
    // Set once a decoration call failed; this instance draws nothing for the rest of the session.
    // 某次点缀调用失败后置位；本次会话余下时间里该实例不再绘制。
    private boolean disabled;

    /**
     * @param loggerName the owning card's logger name for the warning below, e.g. {@code "SparkDLC/GuideDecor"}.
     *                   所属卡片用于下面这条警告的日志名，例如 {@code "SparkDLC/GuideDecor"}。
     */
    public GuidebookCardDecor(String loggerName) {
        this.logger = LoggerFactory.getLogger(loggerName);
    }

    /** Frame ornaments and sigil: call right after the card panel, before its rows. 面板之后、行之前调用。 */
    @Override
    public void cardChrome(DrawContext context, int x, int y, int width, int height) {
        if (disabled) {
            return;
        }
        try {
            GuidebookDecorApi.cardChrome(context, x, y, width, height);
        } catch (RuntimeException failure) {
            disable(failure);
        }
    }

    /** Tab, charm and foliage: call after the card rows. 行之后调用。 */
    @Override
    public void cardOverlay(DrawContext context, int x, int y, int width, int height) {
        if (disabled) {
            return;
        }
        try {
            GuidebookDecorApi.cardOverlay(context, x, y, width, height);
        } catch (RuntimeException failure) {
            disable(failure);
        }
    }

    private void disable(Throwable failure) {
        // Decoration must never take the inventory down with it: disable it for the session.
        // 点缀不能拖垮背包界面：本次会话内禁用。
        logger.warn("Guidebook decoration failed; disabling it for this session", failure);
        disabled = true;
    }
}
