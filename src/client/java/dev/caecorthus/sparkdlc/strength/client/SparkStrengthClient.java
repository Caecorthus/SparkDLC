package dev.caecorthus.sparkdlc.strength.client;

import dev.caecorthus.sparkdlc.strength.client.item.CapsuleClient;
import dev.caecorthus.sparkdlc.strength.client.item.M67Client;
import dev.caecorthus.sparkdlc.strength.client.role.coroner.CoronerClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.corruptcop.CorruptCopClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.corruptcop.CorruptCopMusicController;
import dev.caecorthus.sparkdlc.strength.client.role.demonhunter.DemonHunterSniffClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.detective.CriminologistClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.economy.RoleEconomyClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.economy.KillerTeamEconomyClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.engineer.EngineerClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.morphling.MorphlingClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.professor.ProfessorSerumClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.veteran.VeteranClientHooks;
import dev.caecorthus.sparkdlc.strength.client.screen.criminologist.CriminologistScreen;
import dev.caecorthus.sparkdlc.strength.client.screen.tablet.TabletClientState;
import dev.caecorthus.sparkdlc.strength.client.screen.tablet.TabletScreen;
import dev.caecorthus.sparkdlc.strength.client.tablet.TabletClientHighlights;
import dev.caecorthus.sparkdlc.strength.network.criminologist.OpenCriminologistScreenS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.economy.SyncKillerTeamEconomyS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.OpenTabletScreenS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.SyncTabletSnapshotS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.veteran.SyncVeteranBlackoutS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class SparkStrengthClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CapsuleClient.register();
        M67Client.initialize();
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
        ClientTickEvents.END_CLIENT_TICK.register(CorruptCopMusicController::tick);
        ClientTickEvents.END_CLIENT_TICK.register(client ->
                KillerTeamEconomyClientHooks.tick(client.world));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            VeteranClientHooks.resetBlackoutState();
            KillerTeamEconomyClientHooks.reset();
        });
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
        ClientPlayNetworking.registerGlobalReceiver(SyncKillerTeamEconomyS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        KillerTeamEconomyClientHooks.applySnapshot(
                                context.client().world,
                                payload.visible(),
                                payload.balance()
                        )));
    }
}
