package dev.caecorthus.sparkdlc.strength.client;

import dev.caecorthus.sparkdlc.strength.client.item.CapsuleClient;
import dev.caecorthus.sparkdlc.strength.client.role.coroner.CoronerClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.corruptcop.CorruptCopClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.corruptcop.CorruptCopMusicController;
import dev.caecorthus.sparkdlc.strength.client.role.demonhunter.DemonHunterSniffClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.detective.CriminologistClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.economy.RoleEconomyClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.engineer.EngineerClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.morphling.MorphlingClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.professor.ProfessorSerumClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.veteran.VeteranClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.waiter.WaiterTaskRevealClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.timekeeper.TimekeeperWatchClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.shadowjester.ShadowJesterShowdownMusicController;
import dev.caecorthus.sparkdlc.strength.client.screen.criminologist.CriminologistScreen;
import dev.caecorthus.sparkdlc.strength.client.screen.tablet.TabletClientState;
import dev.caecorthus.sparkdlc.strength.client.screen.tablet.TabletScreen;
import dev.caecorthus.sparkdlc.strength.client.tablet.TabletClientHighlights;
import dev.caecorthus.sparkdlc.strength.network.criminologist.OpenCriminologistScreenS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.OpenTabletScreenS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.SyncTabletSnapshotS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.shadowjester.SyncShadowJesterShowdownMusicS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.veteran.SyncVeteranBlackoutS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import dev.caecorthus.sparkdlc.strength.SparkStrengthItems;
import dev.caecorthus.sparkdlc.strength.component.timekeeper.TimekeeperWatchComponent;
import dev.caecorthus.sparkdlc.strength.item.TimekeeperWatchItem;
import dev.caecorthus.sparkdlc.strength.role.timekeeper.TimekeeperConstants;
import dev.caecorthus.sparkdlc.strength.role.timekeeper.TimekeeperWatchMode;

public final class SparkStrengthClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CapsuleClient.register();
        CoronerClientHooks.register();
        CorruptCopClientHooks.register();
        CriminologistClientHooks.register();
        DemonHunterSniffClientHooks.register();
        EngineerClientHooks.register();
        MorphlingClientHooks.register();
        ProfessorSerumClientHooks.register();
        RoleEconomyClientHooks.register();
        TabletClientHighlights.register();
        VeteranClientHooks.register();
        WaiterTaskRevealClientHooks.register();
        ClientTickEvents.END_CLIENT_TICK.register(CorruptCopMusicController::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ShadowJesterShowdownMusicController::tick);
        ClientTickEvents.END_CLIENT_TICK.register(TimekeeperWatchClientHooks::tick);
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!stack.isOf(SparkStrengthItems.dyingWatch())) {
                return;
            }

            TimekeeperWatchMode mode = TimekeeperWatchItem.getMode(stack);
            int cost = mode == TimekeeperWatchMode.ITEM_REFRESH
                    ? TimekeeperConstants.ITEM_REFRESH_COST_COINS
                    : TimekeeperConstants.ABILITY_REFRESH_COST_COINS;
            int cooldownTicks = 0;
            if (MinecraftClient.getInstance().player != null) {
                var watch = TimekeeperWatchComponent.KEY.get(MinecraftClient.getInstance().player);
                cooldownTicks = mode == TimekeeperWatchMode.ITEM_REFRESH
                        ? watch.getItemRefreshCooldownTicks()
                        : watch.getAbilityRefreshCooldownTicks();
            }
            Text cooldown = cooldownTicks > 0
                    ? Text.translatable("item.sparkstrength.dying_watch.cooldown", (cooldownTicks + 19) / 20)
                    : Text.translatable("item.sparkstrength.dying_watch.cooldown.ready");
            var style = net.minecraft.text.Style.EMPTY.withColor(0xADD8E6).withItalic(false);
            int insertAt = Math.min(1, lines.size());
            lines.add(insertAt, Text.translatable("item.sparkstrength.dying_watch.cost", cost).setStyle(style));
            lines.add(insertAt + 1, cooldown.copy().setStyle(style));
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                VeteranClientHooks.resetBlackoutState());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                ShadowJesterShowdownMusicController.reset());
        ClientPlayNetworking.registerGlobalReceiver(OpenCriminologistScreenS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        context.client().setScreen(new CriminologistScreen(payload.victimUuid()))));
        ClientPlayNetworking.registerGlobalReceiver(OpenTabletScreenS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        context.client().setScreen(new TabletScreen())));
        ClientPlayNetworking.registerGlobalReceiver(SyncTabletSnapshotS2CPacket.ID,
                (payload, context) -> context.client().execute(() -> {
                    TabletClientState.apply(payload.snapshot());
                    if (context.client().currentScreen instanceof TabletScreen tabletScreen) {
                        tabletScreen.handleSnapshotUpdate();
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(SyncVeteranBlackoutS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        VeteranClientHooks.setBlackoutActive(payload.active())));
        ClientPlayNetworking.registerGlobalReceiver(
                SyncShadowJesterShowdownMusicS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        ShadowJesterShowdownMusicController.setServerRequestedActive(payload.active()))
        );
    }
}
