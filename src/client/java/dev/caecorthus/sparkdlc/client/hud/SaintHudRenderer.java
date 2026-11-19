package dev.caecorthus.sparkdlc.client.hud;

import dev.caecorthus.sparkdlc.client.SparkWitchClient;
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
 * Builds the owner-only Saint Hellfire or Karma countdown line.
 * 构造仅本人可见的圣徒业火或业障倒计时文本。
 */
public final class SaintHudRenderer {
    private SaintHudRenderer() {
    }

    public static List<Text> getHudLines(ClientPlayerEntity player) {
        WitchPlayerComponent component = WitchPlayerComponent.KEY.get(player);
        SaintPlayerState state = component.getSaintState();
        boolean saint = SaintRules.isSaint(GameWorldComponent.KEY.get(player.getWorld()).getRole(player));
        // Almost every player is neither a Saint nor carrying Karma; the HUD mixin asks each frame, so answer
        // without allocating. / 几乎所有玩家既不是圣徒也没有业障；HUD mixin 每帧都会询问，因此不分配对象直接返回。
        if (!saint && state.karmaCooldownTicks() <= 0) {
            return List.of();
        }
        List<Text> lines = new ArrayList<>(2);
        if (saint) {
            int balance = PlayerShopComponent.KEY.get(player).getBalance();
            Text hellfireLine = hellfireLine(state, balance);
            lines.add(hellfireLine);
        }

        if (state.karmaCooldownTicks() > 0) {
            lines.add(Text.translatable(
                    "hud.sparkdlc.saint.karma.active",
                    TickTime.ceilSecondsAtLeastOne(state.karmaCooldownTicks())
            ));
        }
        return List.copyOf(lines);
    }

    private static Text hellfireLine(SaintPlayerState state, int balance) {
        if (state.isHellfireActive()) {
            return Text.translatable(
                    "hud.sparkdlc.saint.hellfire.active",
                    TickTime.ceilSecondsAtLeastOne(state.hellfireActiveTicks())
            );
        }
        if (state.hellfireCooldownTicks() > 0) {
            return Text.translatable(
                    "hud.sparkdlc.saint.hellfire.cooldown",
                    TickTime.ceilSecondsAtLeastOne(state.hellfireCooldownTicks())
            );
        }
        if (!SaintRules.hasHellfireCoinRequirement(balance)) {
            return Text.translatable("hud.sparkdlc.saint.hellfire.not_enough_money");
        }
        return Text.translatable(
                "hud.sparkdlc.saint.hellfire.ready",
                SparkWitchClient.abilityKeyText()
        );
    }
}
