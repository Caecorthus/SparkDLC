package dev.caecorthus.sparkdlc.client.skill;

import net.minecraft.client.MinecraftClient;

/** Role-owned handler for the generic secondary ability key. / 通用第二技能键的角色自有处理器。 */
public interface SecondaryAbilityHandler {
    void onPressed(MinecraftClient client);

    default void tick(MinecraftClient client) {
    }

    default void reset() {
    }
}
