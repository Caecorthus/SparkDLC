package dev.caecorthus.sparkdlc.client.round.dualselect;

import dev.caecorthus.sparkdlc.round.dualselect.DualRoleSelectionComponent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

/**
 * Opens and closes the opening-pick screen by watching the synced component.
 * <p>
 * 通过监听已同步的组件来开关「身份抉择」界面。
 */
public final class DualSelectClient {

    private DualSelectClient() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(DualSelectClient::tick);
    }

    private static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }
        DualRoleSelectionComponent component;
        try {
            component = DualRoleSelectionComponent.KEY.get(client.world.getScoreboard());
        } catch (Throwable ignored) {
            return;
        }
        boolean selecting = component.phase() == DualRoleSelectionComponent.Phase.SELECTING
                && !component.localOptions().isEmpty();
        boolean showing = client.currentScreen instanceof DualRoleSelectionScreen;
        if (selecting && !showing) {
            client.setScreen(new DualRoleSelectionScreen(component));
        } else if (!selecting && showing) {
            client.setScreen(null);
        }
    }
}
