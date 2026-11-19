package dev.caecorthus.sparkdlc.client.hooks;

import dev.caecorthus.sparkdlc.client.role.witch.curser.CurserInstinctClientRules;
import dev.caecorthus.sparkdlc.client.role.jiahao.insider.InsiderHighlightRules;
import dev.caecorthus.sparkdlc.client.render.WraithClientState;
import dev.caecorthus.sparkdlc.role.witch.WitchFactionRules;
import dev.caecorthus.sparkdlc.role.neutral.murderouswitch.rules.MurderousWitchRules;
import dev.caecorthus.sparkdlc.net.version.SparkWitchServerConnection;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Grants only the visual instinct lightmap bridge for Grand Witch faction members, the Murderous Witch and the
 * Insider (owner-approved A3).
 * 只给大魔女阵营成员、杀意魔女与内应（所有者批准的 A3）接入本能亮度过渡，不授予 wathe 原生杀手能力。
 */
public final class WitchInstinctClientHooks {
    private WitchInstinctClientHooks() {
    }

    public static boolean usesKillerStyleInstinctLight() {
        if (!SparkWitchServerConnection.isConfirmedServer()) {
            return false;
        }
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return false;
        }

        boolean instinctEnabled = WatheClient.isInstinctEnabled();
        Role role = GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
        if (CurserInstinctClientRules.shouldUseWitchInstinctLight(
                role == null ? null : role.identifier(),
                SparkWitchServerConnection.isConfirmedServer(),
                WraithClientState.isActive(player),
                WraithClientState.isPromoted(player),
                GameFunctions.isPlayerSpectatingOrCreative(player),
                instinctEnabled
        )) {
            return true;
        }
        if (!instinctEnabled || !GameFunctions.isPlayerPlayingAndAlive(player)) {
            return false;
        }

        return WitchFactionRules.usesKillerStyleInstinctLight(role)
                || MurderousWitchRules.usesKillerStyleInstinctLight(role)
                || InsiderHighlightRules.usesKillerStyleInstinctLight(role);
    }
}
