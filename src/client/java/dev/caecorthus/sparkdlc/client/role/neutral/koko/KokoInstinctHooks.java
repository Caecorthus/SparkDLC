package dev.caecorthus.sparkdlc.client.role.neutral.koko;

import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesTaotieSeekerBridge;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.caecorthus.sparkdlc.role.neutral.koko.KokoComponent;
import dev.caecorthus.sparkdlc.role.neutral.koko.KokoRules;
import dev.doctor4t.wathe.api.event.GetInstinctHighlight;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Client seam: Koko's single Wathe {@link GetInstinctHighlight} listener. While she is live, holding the instinct key
 * outlines every other living player in her colour through walls (owner 2026-11-13: the instinct key is required, so
 * the answer is a keybind result, never {@code always}). Only her own client answers, from the owner-synced
 * {@link KokoComponent}. It is an ordinary keyed instinct: the keyed-instinct blockers (Fear, the Control Expert
 * Disruptor, Natsume's hypnosis, the SparkAssist toggle) and every {@code skip()} (100) or higher answer (a floating
 * Hanna, a hidden Survival Master, Last Stand) still hide it. How others see HER is not here: that is the vanilla
 * Glowing effect the server puts on her.
 * 客户端接缝：可可唯一的 Wathe {@link GetInstinctHighlight} 监听器。直播期间按住本能键，其他所有存活玩家会以她的身份色隔墙
 * 描边（所有者 2026-11-13：必须按本能键，因此返回的是按键型结果，从不是 {@code always}）。只有她自己的客户端作答，数据来自
 * 只同步给本人的 {@link KokoComponent}。它是普通的按键本能：按键本能的屏蔽（恐惧、控场专家干扰器、夏目的催眠、SparkAssist
 * 开关）以及任何 {@code skip()}（100）及更高优先级的答复（浮游中的汉娜、被遮挡的生存大师、背水一战）仍会隐藏它。别人如何
 * 看到她不在这里：那是服务端给她挂上的原版发光效果。
 */
public final class KokoInstinctHooks {
    private static boolean registered;

    private KokoInstinctHooks() {
    }

    static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        GetInstinctHighlight.EVENT.register(KokoInstinctHooks::highlight);
    }

    /**
     * Wathe listener; {@code null} means "no opinion". The component read comes first: it holds no stream on every
     * client but a live Koko's, so this per-entity, per-frame path leaves at once for everyone else.
     * Wathe 监听器；{@code null} 表示不作答。先读组件：除了正在直播的可可，所有客户端的组件里都没有直播，这条逐实体、逐帧的
     * 路径对其他人立即返回。
     */
    private static @Nullable GetInstinctHighlight.HighlightResult highlight(Entity target) {
        if (!(target instanceof PlayerEntity targetPlayer)) {
            return null;
        }
        ClientPlayerEntity viewer = MinecraftClient.getInstance().player;
        if (viewer == null || targetPlayer == viewer || !KokoComponent.KEY.get(viewer).isLive()) {
            return null;
        }
        if (!SparkDlcServerConnection.isConfirmedServer() || !isLivingKoko(viewer)
                || !isShownTo(viewer, targetPlayer)) {
            return null;
        }
        return GetInstinctHighlight.HighlightResult.withKeybind(KokoRules.OUTLINE_COLOR, KokoRules.OUTLINE_PRIORITY);
    }

    private static boolean isLivingKoko(ClientPlayerEntity viewer) {
        return KokoRules.isKoko(GameWorldComponent.KEY.get(viewer.getWorld()).getRole(viewer))
                && GameFunctions.isPlayerPlayingAndAlive(viewer)
                && !NoellesTaotieSeekerBridge.isSwallowed(viewer);
    }

    /**
     * The target gates of the other role outlines: a living participant who is not an alive spectator (Rift occupant)
     * or creative, not invisible or invisible to the viewer, not swallowed by a Taotie and not hidden from the
     * viewer's instinct by SparkTraits. A failing target gets no answer, so Wathe's own rules decide it.
     * 与其他职业描边相同的目标门槛：存活的参与者，不是存活旁观者（裂隙门内）或创造模式，未隐身且对观察者不隐身，未被饕餮
     * 吞下，未被 SparkTraits 从观察者本能中隐藏。不满足的目标不作答，由 Wathe 自身规则判定。
     */
    private static boolean isShownTo(ClientPlayerEntity viewer, PlayerEntity target) {
        return GameFunctions.isPlayerPlayingAndAlive(target)
                && !GameFunctions.isPlayerSpectatingOrCreative(target)
                && !target.isInvisible()
                && !target.isInvisibleTo(viewer)
                && !NoellesTaotieSeekerBridge.isSwallowed(target)
                && !isInstinctHidden(viewer, target);
    }

    /**
     * Cross-module seam (SparkTraits, fused into SparkDLC, so a direct call): her own read of the public
     * {@code SparkTraitsApi.isInstinctHidden(viewer, target)} facade, like the Seeker, Dominator, Savior and 橘雪莉
     * outlines.
     * 跨模块接缝（SparkTraits 已融合进 SparkDLC，因此为直接调用）：她自有的一份公共
     * {@code SparkTraitsApi.isInstinctHidden(viewer, target)} 门面读取，与搜寻者、支配者、救世主、橘雪莉的描边相同。
     */
    private static boolean isInstinctHidden(PlayerEntity viewer, PlayerEntity target) {
        return SparkTraitsApi.isInstinctHidden(viewer, target);
    }
}
