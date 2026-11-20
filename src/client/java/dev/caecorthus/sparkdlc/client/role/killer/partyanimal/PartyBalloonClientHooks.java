package dev.caecorthus.sparkdlc.client.role.killer.partyanimal;

import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartyBalloonMarkComponent;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartyAnimalRules;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartyBalloonRules;
import dev.doctor4t.wathe.api.event.GetInstinctHighlight;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.Nullable;

/**
 * Party Balloon outline: a living Party Animal always sees balloon-marked players in yellow, through walls and without
 * the instinct key, like NoellesRoles' helium pink. The mark only syncs to Party Animals, so nobody else can answer here.
 * Priority {@link PartyBalloonRules#HIGHLIGHT_PRIORITY} beats the helium pink (priority 0) on a player who is both.
 * The blackout and Undercover arbiters wrap {@code WatheClient.getInstinctHighlight} from outside, so they still apply.
 * 派对气球描边：存活的派对狂无需本能键即可穿墙常驻看到被塞气球的玩家（黄色），与 NoellesRoles 的氦气粉色相同方式。
 * 标记只同步给派对狂，其他人在这里不会作答。优先级 {@link PartyBalloonRules#HIGHLIGHT_PRIORITY} 高于氦气粉色（优先级 0），
 * 两者同时适用时显示黄色。熄灯与卧底裁决从外层包装 {@code WatheClient.getInstinctHighlight}，因此仍然生效。
 */
public final class PartyBalloonClientHooks {
    private static boolean registered;
    // Boxed once: the trait instinct hook asks per visible player each frame. / 只装箱一次：词条本能钩子每帧为每个可见玩家查询。
    private static final Integer MARK_HIGHLIGHT_COLOR = PartyBalloonRules.HIGHLIGHT_COLOR;

    private PartyBalloonClientHooks() {
    }

    /**
     * The balloon outline colour when {@code target} carries a balloon, or {@code null} when it does not. The mark only
     * syncs to Party Animals; the trait feature's Conscience branch answers before Wathe's event and asks this so the
     * yellow outline survives there.
     * 目标带着气球时返回气球描边颜色；未带气球时返回 null。该标记只同步给派对狂；词条功能的善良分支先于 Wathe 事件作答，
     * 因此在那里查询本方法以保留黄色描边。
     */
    public static @Nullable Integer highlightColor(PlayerEntity target) {
        return PartyBalloonMarkComponent.isMarked(target) ? MARK_HIGHLIGHT_COLOR : null;
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        GetInstinctHighlight.EVENT.register(PartyBalloonClientHooks::highlight);
    }

    private static @Nullable GetInstinctHighlight.HighlightResult highlight(Entity entity) {
        // Same target filter as NoellesRoles' listener: spectators and invisible players get no answer.
        // 与 NoellesRoles 监听器相同的目标过滤：旁观者与隐身玩家不作答。
        if (!(entity instanceof PlayerEntity target) || target.isSpectator() || target.isInvisible()) {
            return null;
        }
        PlayerEntity viewer = MinecraftClient.getInstance().player;
        if (viewer == null || viewer == target || !PartyBalloonMarkComponent.isMarked(target)) {
            return null;
        }
        GameWorldComponent game = GameWorldComponent.KEY.get(viewer.getWorld());
        boolean viewerIsLivingPartyAnimal = WatheClient.isPlayerPlayingAndAlive()
                && PartyAnimalRules.isPartyAnimal(game.getRole(viewer));
        // NoellesRoles: a Survival Master behind an obstacle is immune to killer instinct outlines.
        // NoellesRoles：被障碍物遮挡的生存大师免疫杀手本能描边。
        boolean hiddenSurvivalMaster = game.isRole(target, Noellesroles.SURVIVAL_MASTER) && !viewer.canSee(target);
        if (!PartyBalloonRules.showsHighlight(viewerIsLivingPartyAnimal, GameFunctions.isPlayerPlayingAndAlive(target),
                true, hiddenSurvivalMaster)) {
            return null;
        }
        return GetInstinctHighlight.HighlightResult.always(
                PartyBalloonRules.HIGHLIGHT_COLOR, PartyBalloonRules.HIGHLIGHT_PRIORITY);
    }
}
