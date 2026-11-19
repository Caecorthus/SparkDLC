package dev.caecorthus.sparkdlc.strength.client;

import dev.caecorthus.sparkdlc.strength.client.item.CapsuleClient;
import dev.caecorthus.sparkdlc.strength.client.item.FlashlightModelClient;
import dev.caecorthus.sparkdlc.strength.client.item.M67Client;
import dev.caecorthus.sparkdlc.strength.client.instinct.InstinctViewer;
import dev.caecorthus.sparkdlc.strength.client.item.PerfumerItemsClient;
import dev.caecorthus.sparkdlc.strength.client.render.ClientFrame;
import dev.caecorthus.sparkdlc.strength.client.role.attendant.flashlight.FlashlightLights;
import dev.caecorthus.sparkdlc.strength.client.role.attendant.flashlight.FlashlightRenderer;
import dev.caecorthus.sparkdlc.strength.client.role.bandit.BanditRevolverClient;
import dev.caecorthus.sparkdlc.strength.client.role.bodyguard.BodyguardClient;
import dev.caecorthus.sparkdlc.strength.client.role.bomber.BomberDroneClient;
import dev.caecorthus.sparkdlc.strength.client.role.bomber.TimedBombMarkerClientStore;
import dev.caecorthus.sparkdlc.strength.client.role.coroner.CoronerClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.corruptcop.CorruptCopClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.corruptcop.CorruptCopMusicController;
import dev.caecorthus.sparkdlc.strength.client.role.demonhunter.DemonHunterSniffClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.economy.RoleEconomyClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.economy.KillerTeamEconomyClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.engineer.EngineerClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.jester.JesterMomentClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.morphling.MorphlingClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.partyanimal.PartyBalloonClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.partyanimal.PartygoerClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.partyanimal.PartygoerInstinctClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.pathogen.PathogenClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.perfumer.PerfumerClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.professor.ProfessorSerumClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.spiritualist.SpiritPossessionClient;
import dev.caecorthus.sparkdlc.strength.client.role.taotie.TaotieHeadClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.undercover.UndercoverClient;
import dev.caecorthus.sparkdlc.strength.client.role.vulture.SkateboardClient;
import dev.caecorthus.sparkdlc.strength.client.role.vulture.VultureSuperCurseClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.waiter.WaiterTaskRevealClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.timekeeper.TimekeeperWatchClientHooks;
import dev.caecorthus.sparkdlc.strength.client.role.shadowjester.ShadowJesterShowdownMusicController;
import dev.caecorthus.sparkdlc.strength.client.sabotage.BlackoutSightClient;
import dev.caecorthus.sparkdlc.strength.client.sabotage.SabotageClient;
import dev.caecorthus.sparkdlc.strength.client.screen.assassin.AssassinGuessSources;
import dev.caecorthus.sparkdlc.strength.client.screen.detective.DetectiveFolderScreen;
import dev.caecorthus.sparkdlc.strength.client.screen.detective.DetectiveInterrogationScreen;
import dev.caecorthus.sparkdlc.strength.client.screen.tablet.TabletClientState;
import dev.caecorthus.sparkdlc.strength.client.screen.tablet.TabletScreen;
import dev.caecorthus.sparkdlc.strength.client.tablet.TabletChatNotifier;
import dev.caecorthus.sparkdlc.strength.client.tablet.TabletClientHighlights;
import dev.caecorthus.sparkdlc.strength.client.tablet.TabletHeldHud;
import dev.caecorthus.sparkdlc.strength.client.world.LoadedEntities;
import dev.caecorthus.sparkdlc.strength.network.detective.DetectiveInterrogationResultS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.detective.OpenDetectiveFolderS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.detective.OpenDetectiveInterrogationS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.economy.SyncKillerTeamEconomyS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.sabotage.SyncBlackoutSightS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.OpenTabletScreenS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.tablet.SyncTabletSnapshotS2CPacket;
import dev.caecorthus.sparkdlc.strength.network.shadowjester.SyncShadowJesterShowdownMusicS2CPacket;
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
        // Clock for the per-frame caches of hot render hooks (instinct outlines, skins). / 热点渲染钩子逐帧缓存的时钟。
        ClientFrame.register();
        InstinctViewer.register();
        // Loaded drones, Partygoers and corpses, so per-frame picks can skip empty searches. / 已加载的无人机、派对客与尸体，
        // 让逐帧选取跳过必然落空的搜索。
        LoadedEntities.register();
        BanditRevolverClient.register();
        BodyguardClient.register();
        CapsuleClient.register();
        FlashlightModelClient.register();
        M67Client.initialize();
        PerfumerItemsClient.register();
        BomberDroneClient.initialize();
        // Planted timed-bomb traps arrive privately to trap viewers. / 已埋设的定时炸弹陷阱只私下发给陷阱观察者。
        TimedBombMarkerClientStore.register();
        CoronerClientHooks.register();
        CorruptCopClientHooks.register();
        DemonHunterSniffClientHooks.register();
        UndercoverClient.register();
        EngineerClientHooks.register();
        FlashlightLights.register();
        FlashlightRenderer.register();
        JesterMomentClientHooks.register();
        MorphlingClientHooks.register();
        PartyBalloonClientHooks.register();
        PathogenClientHooks.register();
        PerfumerClientHooks.register();
        ProfessorSerumClientHooks.register();
        RoleEconomyClientHooks.register();
        SkateboardClient.register();
        TaotieHeadClientHooks.register();
        // Spiritualist Wraith possession; the key and Wraith sight bind only with SparkWitch (reflection).
        // 灵界行者附身冤魂；按键与冤魂可见仅在加载 SparkWitch 时挂接（反射）。
        SpiritPossessionClient.register();
        TabletClientHighlights.register();
        TabletChatNotifier.register();
        TabletHeldHud.register();
        SabotageClient.register();
        // Binds only when SparkWitch is loaded (reflection, no compile dependency).
        // 仅在加载了 SparkWitch 时挂接按键（反射，无编译依赖）。
        VultureSuperCurseClientHooks.register();
        PartygoerClientHooks.register();
        PartygoerInstinctClientHooks.register();
        WaiterTaskRevealClientHooks.register();
        ClientTickEvents.END_CLIENT_TICK.register(CorruptCopMusicController::tick);
        ClientTickEvents.END_CLIENT_TICK.register(client ->
                KillerTeamEconomyClientHooks.tick(client.world));
        ClientTickEvents.END_CLIENT_TICK.register(ShadowJesterShowdownMusicController::tick);
        ClientTickEvents.END_CLIENT_TICK.register(TimekeeperWatchClientHooks::tick);
        // Assassin: the ability key opens the hit list during a cooldown too (NoellesRoles only opens it when ready).
        // 刺客：冷却期间按技能键也能打开刺杀名单（NoellesRoles 只在可猜测时打开）。
        ClientTickEvents.END_CLIENT_TICK.register(AssassinGuessSources::tickAbilityKey);
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

        // Fabric may fire DISCONNECT on the Netty thread (channelInactive), racing the client thread that still ticks and
        // applies queued packets, so the reset runs on the client thread. JOIN (client thread, before any play packet)
        // repeats it in case MinecraftClient.disconnect dropped the queued task.
        // Fabric 可能在 Netty 线程（channelInactive）上触发 DISCONNECT，与仍在 tick 并执行排队数据包的客户端线程竞争，因此重置在
        // 客户端线程上执行。JOIN（客户端线程、任何游戏数据包之前）再重置一次，以防排队的任务被 MinecraftClient.disconnect 丢弃。
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(SparkStrengthClient::resetSessionState));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetSessionState());
        // The server syncs the case component before this packet, so the screen reads fresh data on open.
        // 服务端在发送此包前已同步命案组件，界面打开时即可读取最新数据。
        ClientPlayNetworking.registerGlobalReceiver(OpenDetectiveFolderS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        context.client().setScreen(new DetectiveFolderScreen())));
        ClientPlayNetworking.registerGlobalReceiver(OpenDetectiveInterrogationS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        context.client().setScreen(new DetectiveInterrogationScreen())));
        ClientPlayNetworking.registerGlobalReceiver(DetectiveInterrogationResultS2CPacket.ID,
                (payload, context) -> context.client().execute(() -> DetectiveInterrogationScreen.acceptResult(payload)));
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
        ClientPlayNetworking.registerGlobalReceiver(SyncBlackoutSightS2CPacket.ID,
                (payload, context) -> context.client().execute(() -> BlackoutSightClient.apply(payload)));
        ClientPlayNetworking.registerGlobalReceiver(SyncKillerTeamEconomyS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        KillerTeamEconomyClientHooks.applySnapshot(
                                context.client().world,
                                payload.visible(),
                                payload.balance()
                        )));
        ClientPlayNetworking.registerGlobalReceiver(
                SyncShadowJesterShowdownMusicS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        ShadowJesterShowdownMusicController.setServerRequestedActive(payload.active()))
        );
    }

    /** Per-connection client state that must not outlive its server (client thread only). / 不得跨服务器保留的单连接客户端状态（仅客户端线程）。 */
    private static void resetSessionState() {
        BlackoutSightClient.reset();
        KillerTeamEconomyClientHooks.reset();
        ShadowJesterShowdownMusicController.reset();
        TimedBombMarkerClientStore.clear();
        // A stale snapshot would otherwise drive tablet outlines on the next server.
        // 否则过期快照会在下一个服务器上继续驱动平板描边。
        TabletClientState.reset();
        TabletHeldHud.reset();
    }
}
