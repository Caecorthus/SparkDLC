package dev.caecorthus.sparkdlc.client.net;

import dev.caecorthus.sparkdlc.client.economy.KillerTeamEconomyClientHooks;
import dev.caecorthus.sparkdlc.client.role.civilian.detective.DetectiveFolderScreen;
import dev.caecorthus.sparkdlc.client.role.civilian.detective.DetectiveInterrogationScreen;
import dev.caecorthus.sparkdlc.client.role.civilian.prophet.ProphetNecrologyBookScreen;
import dev.caecorthus.sparkdlc.client.role.civilian.tarotreader.TarotDivinationClientState;
import dev.caecorthus.sparkdlc.client.role.civilian.tarotreader.TarotDivinationSelectorScreen;
import dev.caecorthus.sparkdlc.client.role.civilian.tarotreader.TarotReadingLog;
import dev.caecorthus.sparkdlc.client.role.killer.blackraven.BlackRavenLedgerScreen;
import dev.caecorthus.sparkdlc.client.role.neutral.shadowjester.ShadowJesterShowdownMusicController;
import dev.caecorthus.sparkdlc.client.sabotage.BlackoutSightClient;
import dev.caecorthus.sparkdlc.client.tablet.screen.TabletClientState;
import dev.caecorthus.sparkdlc.client.tablet.screen.TabletScreen;
import dev.caecorthus.sparkdlc.client.trait.killer.BluePoisonMarkerClientStore;
import dev.caecorthus.sparkdlc.economy.SyncKillerTeamEconomyS2CPacket;
import dev.caecorthus.sparkdlc.net.OpenBlackRavenLedgerS2CPacket;
import dev.caecorthus.sparkdlc.net.OpenProphetNecrologyS2CPacket;
import dev.caecorthus.sparkdlc.net.OpenTarotDivinationSelectorS2CPacket;
import dev.caecorthus.sparkdlc.net.TarotDivinationReadingS2CPacket;
import dev.caecorthus.sparkdlc.net.TarotDivinationSnapshotS2CPacket;
import dev.caecorthus.sparkdlc.net.WraithRoleAnnouncementS2CPacket;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.caecorthus.sparkdlc.role.civilian.detective.DetectiveInterrogationResultS2CPacket;
import dev.caecorthus.sparkdlc.role.civilian.detective.OpenDetectiveFolderS2CPacket;
import dev.caecorthus.sparkdlc.role.civilian.detective.OpenDetectiveInterrogationS2CPacket;
import dev.caecorthus.sparkdlc.role.neutral.shadowjester.SyncShadowJesterShowdownMusicS2CPacket;
import dev.caecorthus.sparkdlc.sabotage.net.SyncBlackoutSightS2CPacket;
import dev.caecorthus.sparkdlc.tablet.net.OpenTabletScreenS2CPacket;
import dev.caecorthus.sparkdlc.tablet.net.SyncTabletSnapshotS2CPacket;
import dev.caecorthus.sparkdlc.trait.net.BluePoisonMarkersPayload;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.client.gui.RoundTextRenderer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

/**
 * The play receivers of the client that belong to no feature module: the one client-side call for them, next to
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets} on the common side. Most client features register their
 * receivers in their own module (for example {@code SabotageClient}, {@code SmokeNetworkClient},
 * {@code MediumClientModule}); what is listed here are screens and small state updates whose feature has no client
 * module of its own. Until stage 3 these fifteen receivers were written out in three places of the client start-up,
 * one per former mod.
 *
 * <p>Receivers are kept per payload id, so the order of the lines below is not behaviour. The receiver bodies are
 * the ones the start-up had, unchanged: the thread each one works on and its confirmed-server gate included.</p>
 *
 * 不属于任何功能模块的客户端 play 接收器：它们在客户端的唯一注册入口，对应通用端的
 * {@link dev.caecorthus.sparkdlc.net.SparkDlcPackets}。大多数客户端功能在各自的模块中注册接收器（如 {@code SabotageClient}、
 * {@code SmokeNetworkClient}、{@code MediumClientModule}）；此处列出的是所属功能没有独立客户端模块的界面与小型状态更新。
 * 第三阶段之前，这十五个接收器分散写在客户端启动流程的三处，每个旧模组一处。
 *
 * <p>接收器按数据包 id 保存，因此下面各行的顺序不属于行为。接收器的函数体与原先启动流程中的完全相同，包括各自工作的线程和
 * “已确认服务端”判断。</p>
 */
public final class SparkDlcClientPackets {
    private static boolean registered;

    private SparkDlcClientPackets() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        registerDetectiveScreens();
        registerTablet();
        registerBlackoutSight();
        registerKillerTeamEconomy();
        registerShadowJesterShowdownMusic();
        registerBluePoisonMarkers();
        registerTarotDivination();
        registerBlackRavenLedger();
        registerProphetNecrology();
        registerWraithRoleAnnouncement();
    }

    /** Detective: the case folder, the interrogation screen and its result. / 侦探：案卷、审讯界面及其结果。 */
    private static void registerDetectiveScreens() {
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
    }

    /** Tablet: the screen and the snapshot it shows. / 平板电脑：界面及其显示的快照。 */
    private static void registerTablet() {
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
    }

    /** Sabotage: who still sees during a blackout. / 破坏：停电期间谁仍能看见。 */
    private static void registerBlackoutSight() {
        ClientPlayNetworking.registerGlobalReceiver(SyncBlackoutSightS2CPacket.ID,
                (payload, context) -> context.client().execute(() -> BlackoutSightClient.apply(payload)));
    }

    /** Killer team economy: the shared balance. / 杀手团队经济：共享余额。 */
    private static void registerKillerTeamEconomy() {
        ClientPlayNetworking.registerGlobalReceiver(SyncKillerTeamEconomyS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        KillerTeamEconomyClientHooks.applySnapshot(
                                context.client().world,
                                payload.visible(),
                                payload.balance()
                        )));
    }

    /** Shadow Jester: the showdown music switch. / 影子小丑：对决配乐开关。 */
    private static void registerShadowJesterShowdownMusic() {
        ClientPlayNetworking.registerGlobalReceiver(
                SyncShadowJesterShowdownMusicS2CPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        ShadowJesterShowdownMusicController.setServerRequestedActive(payload.active()))
        );
    }

    /** Conscience trait (善良): the private blue-poison markers. / “善良”词条：私有蓝毒标记。 */
    private static void registerBluePoisonMarkers() {
        // Private blue-poison markers: the server sends this only to viewers that may see blue poison. Play payload
        // handlers run on the client thread, which owns the store.
        // 私有蓝毒标记：服务端只发给可以看到蓝毒的观察者。play 数据包处理器在客户端线程执行，存储也归该线程所有。
        ClientPlayNetworking.registerGlobalReceiver(BluePoisonMarkersPayload.ID,
                (payload, context) -> BluePoisonMarkerClientStore.accept(context.client().world, payload));
    }

    /** Tarot Reader: the faction count snapshot, the selector and the reading. / 塔罗牌师：阵营人数快照、选择界面与占卜结果。 */
    private static void registerTarotDivination() {
        ClientPlayNetworking.registerGlobalReceiver(TarotDivinationSnapshotS2CPacket.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (!SparkDlcServerConnection.isConfirmedServer()) {
                        return;
                    }
                    TarotDivinationClientState.snapshotState().overwrite(
                            payload.civilianCount(),
                            payload.killerCount(),
                            payload.neutralCount(),
                            payload.witchCount(),
                            payload.jiahaoCount()
                    );
                }));
        ClientPlayNetworking.registerGlobalReceiver(OpenTarotDivinationSelectorS2CPacket.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (!SparkDlcServerConnection.isConfirmedServer()) {
                        return;
                    }
                    context.client().setScreen(new TarotDivinationSelectorScreen(
                            payload.mode(),
                            payload.playerIds(),
                            payload.playerNames()
                    ));
                }));
        // Purchaser-only result; stored so the slip and the selector's stamps repeat what the server already said.
        // 仅购买者可见的结果；保存下来，供结果条与选择界面的印记重复展示服务端已给出的结论。
        ClientPlayNetworking.registerGlobalReceiver(TarotDivinationReadingS2CPacket.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (!SparkDlcServerConnection.isConfirmedServer()) {
                        return;
                    }
                    TarotDivinationClientState.readingLog().record(new TarotReadingLog.Reading(
                            payload.mode(),
                            payload.target(),
                            payload.displayName(),
                            payload.positive(),
                            Util.getMeasuringTimeMs()
                    ));
                }));
    }

    /** Black Raven: the ledger screen. / 黑羽鸦：账本界面。 */
    private static void registerBlackRavenLedger() {
        ClientPlayNetworking.registerGlobalReceiver(OpenBlackRavenLedgerS2CPacket.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (SparkDlcServerConnection.isConfirmedServer()) {
                        BlackRavenLedgerScreen.open(context.client());
                    }
                }));
    }

    /**
     * The Necrology opens only on the server's empty authorization; its pages come from the owner-synced component.
     * 亡者名录只在收到服务端的空授权包后打开；书页内容来自仅同步给所有者的组件。
     */
    private static void registerProphetNecrology() {
        ClientPlayNetworking.registerGlobalReceiver(OpenProphetNecrologyS2CPacket.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (SparkDlcServerConnection.isConfirmedServer()) {
                        ProphetNecrologyBookScreen.open(context.client());
                    }
                }));
    }

    /** Wraith: the role announcement of a player who becomes one. / 冤魂：玩家成为冤魂时的职业公告。 */
    private static void registerWraithRoleAnnouncement() {
        ClientPlayNetworking.registerGlobalReceiver(WraithRoleAnnouncementS2CPacket.ID, (payload, context) ->
                context.client().execute(() -> {
                    var roleId = Identifier.tryParse(payload.roleId());
                    RoundTextRenderer.startWelcome(
                            RoleAnnouncementTexts.getForRole(roleId),
                            payload.killers(),
                            payload.targets()
                    );
                }));
    }
}
