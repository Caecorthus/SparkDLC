package dev.caecorthus.sparkdlc.client.round.modevote;

import dev.caecorthus.sparkdlc.round.modevote.ModeVotingComponent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

/**
 * Opens and closes the mode vote screen by watching the synced component.
 * <p>
 * 通过监听已同步的组件来开关模式投票界面。
 */
public final class ModeVotingClient {

    private ModeVotingClient() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ModeVotingClient::tick);
    }

    private static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }
        ModeVotingComponent component;
        try {
            component = ModeVotingComponent.KEY.get(client.world.getScoreboard());
        } catch (Throwable ignored) {
            return;
        }
        boolean active = component.isVotingActive();
        boolean showing = client.currentScreen instanceof ModeVotingScreen;
        if (active && !showing) {
            client.setScreen(new ModeVotingScreen(component));
        } else if (!active && showing) {
            client.setScreen(null);
        }
    }
}
