package dev.caecorthus.sparkdlc.witch.registry;

import dev.caecorthus.sparkdlc.witch.SparkWitch;
import dev.caecorthus.sparkdlc.witch.block.props.ShiningNeonBlock;
import dev.caecorthus.sparkdlc.witch.net.props.NeonSettingsC2SPayload;
import dev.caecorthus.sparkdlc.witch.net.props.NeonSettingsS2CPayload;
import dev.caecorthus.sparkdlc.witch.prop.NeonSettings;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

/**
 * Registers the blinking neon pillar and the settings channel it needs. The block only flips a {@code lit} state, so
 * no custom block entity has to be ticked.
 * <p>
 * 注册闪烁霓虹灯柱，以及它需要的设置通道。方块本身只翻转 {@code lit} 状态，因此不需要额外的方块实体。
 */
public final class NeonBlocks {
    public static final Identifier SHINING_NEON_ID = SparkWitch.id("shining_neon");

    private static ShiningNeonBlock shiningNeon;
    private static boolean registered;

    private NeonBlocks() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        // Light follows the lit state, like Wathe's own neon pillar: an unlit lamp really goes dark.
        // 亮度跟随 lit 状态，与 Wathe 原版霓虹灯柱一致：灭掉的灯真的不发光。
        shiningNeon = new ShiningNeonBlock(AbstractBlock.Settings.copy(Blocks.SEA_LANTERN)
                .luminance(state -> state.get(Properties.LIT) ? 15 : 0));
        Registry.register(Registries.BLOCK, SHINING_NEON_ID, shiningNeon);
        Registry.register(Registries.ITEM, SHINING_NEON_ID, new BlockItem(shiningNeon, new Item.Settings()));

        PayloadTypeRegistry.playC2S().register(NeonSettingsC2SPayload.ID, NeonSettingsC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NeonSettingsS2CPayload.ID, NeonSettingsS2CPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(NeonSettingsC2SPayload.ID, (payload, context) ->
                context.server().execute(() -> NeonSettings.apply(context.player(), payload.pos(),
                        payload.onTicks(), payload.offTicks(), payload.enabled(), payload.random())));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getOverworld() != null) {
                NeonSettings.tick(server.getOverworld());
            }
        });
        ServerWorldEvents.UNLOAD.register((server, world) -> NeonSettings.clear());
    }

    public static ShiningNeonBlock shiningNeon() {
        return shiningNeon;
    }
}
