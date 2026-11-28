package dev.caecorthus.sparkdlc.client.hud;

import dev.caecorthus.sparkdlc.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.saint.SaintPlayerState;
import dev.caecorthus.sparkdlc.role.civilian.saint.SaintRules;
import dev.caecorthus.sparkdlc.util.TickTime;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

/**
 * Builds the owner-only Hellfire lines: for the Saint, the seconds her killer would burn right now (her passive, from
 * her own synced coins); for a player burning with it, the countdown (the Karma mirror).
 * 构造仅本人可见的业火文本：对圣徒显示凶手此刻将承受的秒数（她的被动，取自她自己同步的金币）；对正在承受业火的玩家显示
 * 倒计时（业障镜像）。
 */
public final class SaintHudRenderer {
    private SaintHudRenderer() {
    }

    public static List<Text> getHudLines(ClientPlayerEntity player) {
        WitchPlayerComponent component = WitchPlayerComponent.KEY.get(player);
        SaintPlayerState state = component.getSaintState();
        boolean saint = SaintRules.isSaint(GameWorldComponent.KEY.get(player.getWorld()).getRole(player));
        // Almost every player is neither a Saint nor burning; the HUD mixin asks each frame, so answer
        // without allocating. / 几乎所有玩家既不是圣徒也没有承受业火；HUD mixin 每帧都会询问，因此不分配对象直接返回。
        if (!saint && state.karmaCooldownTicks() <= 0) {
            return List.of();
        }
        List<Text> lines = new ArrayList<>(2);
        if (saint) {
            lines.add(Text.translatable(
                    "hud.sparkdlc.saint.hellfire.passive",
                    SaintRules.hellfireSeconds(PlayerShopComponent.KEY.get(player).getBalance())
            ));
        }

        if (state.karmaCooldownTicks() > 0) {
            lines.add(Text.translatable(
                    "hud.sparkdlc.saint.hellfire.burning",
                    TickTime.ceilSecondsAtLeastOne(state.karmaCooldownTicks())
            ));
        }
        return List.copyOf(lines);
    }
}
