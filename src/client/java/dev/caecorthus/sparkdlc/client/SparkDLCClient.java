package dev.caecorthus.sparkdlc.client;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.SparkDlcParticles;
import dev.caecorthus.sparkdlc.SparkDlcEntities;
import dev.caecorthus.sparkdlc.SparkDlcSounds;
import dev.caecorthus.sparkdlc.achievement.AchievementGalaxy;
import dev.caecorthus.sparkdlc.client.achievement.AchievementAdvancements;
import dev.caecorthus.sparkdlc.client.achievement.AchievementClientState;
import dev.caecorthus.sparkdlc.client.config.SparkDlcConfigManager;
import dev.caecorthus.sparkdlc.client.creative.NoellesCreativeEntries;
import dev.caecorthus.sparkdlc.client.economy.KillerTeamEconomyClientHooks;
import dev.caecorthus.sparkdlc.client.economy.RoleEconomyClientHooks;
import dev.caecorthus.sparkdlc.client.guidebook.GuidebookClientState;
import dev.caecorthus.sparkdlc.client.guidebook.GuidebookInventoryButton;
import dev.caecorthus.sparkdlc.client.guidebook.GuidebookResources;
import dev.caecorthus.sparkdlc.client.hooks.DeathRayClientHooks;
import dev.caecorthus.sparkdlc.client.hooks.GrandWitchFearClientHooks;
import dev.caecorthus.sparkdlc.client.hooks.HunterTrapClientHooks;
import dev.caecorthus.sparkdlc.client.hooks.KidnapperThrowClientHooks;
import dev.caecorthus.sparkdlc.client.hooks.OrthopedistClientHooks;
import dev.caecorthus.sparkdlc.client.hooks.ProphetCorpseHighlightClientHooks;
import dev.caecorthus.sparkdlc.client.hooks.AbilityKeyBridge;
import dev.caecorthus.sparkdlc.client.hooks.WitchCohortClientHooks;
import dev.caecorthus.sparkdlc.client.hooks.WitchInstinctSuppressionClientHooks;
import dev.caecorthus.sparkdlc.client.hooks.WitchPoisonVisionClientHooks;
import dev.caecorthus.sparkdlc.client.hud.charge.ChargeIndicatorHud;
import dev.caecorthus.sparkdlc.client.input.InstinctKeyController;
import dev.caecorthus.sparkdlc.client.instinct.InstinctViewer;
import dev.caecorthus.sparkdlc.client.item.CapsuleClient;
import dev.caecorthus.sparkdlc.client.item.FlashlightModelClient;
import dev.caecorthus.sparkdlc.client.item.M67Client;
import dev.caecorthus.sparkdlc.client.item.PerfumerItemsClient;
import dev.caecorthus.sparkdlc.client.legacy.LegacyClientNames;
import dev.caecorthus.sparkdlc.client.net.SparkDlcClientPackets;
import dev.caecorthus.sparkdlc.client.net.version.SparkDlcClientVersionHandshake;
import dev.caecorthus.sparkdlc.client.ponder.PonderSupport;
import dev.caecorthus.sparkdlc.client.record.MatchRecordClient;
import dev.caecorthus.sparkdlc.client.render.ClientFrame;
import dev.caecorthus.sparkdlc.client.render.LocalViewerSnapshot;
import dev.caecorthus.sparkdlc.client.render.WraithClientState;
import dev.caecorthus.sparkdlc.client.render.entity.HunterTrapEntityRenderer;
import dev.caecorthus.sparkdlc.client.render.entity.NinjaGrapplingHookEntityRenderer;
import dev.caecorthus.sparkdlc.client.replay.ReplayClient;
import dev.caecorthus.sparkdlc.client.role.civilian.apprentice.ApprenticeClientModule;
import dev.caecorthus.sparkdlc.client.role.civilian.athlete.AthleteClient;
import dev.caecorthus.sparkdlc.client.role.civilian.athlete.AthleteDashClient;
import dev.caecorthus.sparkdlc.client.role.civilian.attendant.flashlight.FlashlightLights;
import dev.caecorthus.sparkdlc.client.role.civilian.attendant.flashlight.FlashlightRenderer;
import dev.caecorthus.sparkdlc.client.role.civilian.blind.BlindClient;
import dev.caecorthus.sparkdlc.client.role.civilian.blind.kit.BlindKitClientWiring;
import dev.caecorthus.sparkdlc.client.role.civilian.bodyguard.BodyguardClient;
import dev.caecorthus.sparkdlc.client.role.civilian.controlexpert.ControlExpertStatusHud;
import dev.caecorthus.sparkdlc.client.role.civilian.controlexpert.ControlExpertStunClient;
import dev.caecorthus.sparkdlc.client.role.civilian.coroner.CoronerClientHooks;
import dev.caecorthus.sparkdlc.client.role.civilian.demonhunter.DemonHunterSniffClientHooks;
import dev.caecorthus.sparkdlc.client.role.civilian.emma.EmmaClientModule;
import dev.caecorthus.sparkdlc.client.role.civilian.engineer.EngineerClientHooks;
import dev.caecorthus.sparkdlc.client.role.civilian.fisher.FisherClient;
import dev.caecorthus.sparkdlc.client.role.civilian.hanna.HannaClient;
import dev.caecorthus.sparkdlc.client.role.civilian.judge.JudgeClientModule;
import dev.caecorthus.sparkdlc.client.role.civilian.junker.JunkerClientModule;
import dev.caecorthus.sparkdlc.client.role.civilian.knight.KnightDuelClient;
import dev.caecorthus.sparkdlc.client.role.civilian.knight.KnightSwordClient;
import dev.caecorthus.sparkdlc.client.role.civilian.medium.MediumClientModule;
import dev.caecorthus.sparkdlc.client.role.civilian.medium.MediumVoteClient;
import dev.caecorthus.sparkdlc.client.role.civilian.noa.NoaClient;
import dev.caecorthus.sparkdlc.client.role.civilian.noa.NoaPaintClient;
import dev.caecorthus.sparkdlc.client.role.civilian.perfumer.kit.PerfumerClientHooks;
import dev.caecorthus.sparkdlc.client.role.civilian.planner.PlannerClient;
import dev.caecorthus.sparkdlc.client.role.civilian.professor.ProfessorSerumClientHooks;
import dev.caecorthus.sparkdlc.client.role.civilian.prophet.ProphetClientModule;
import dev.caecorthus.sparkdlc.client.role.civilian.recruit.RecruitClientPresentation;
import dev.caecorthus.sparkdlc.client.role.civilian.savior.SaviorClient;
import dev.caecorthus.sparkdlc.client.role.civilian.savior.SaviorDomainClient;
import dev.caecorthus.sparkdlc.client.role.civilian.seeker.SeekerClientModule;
import dev.caecorthus.sparkdlc.client.role.civilian.sherry.SherryClient;
import dev.caecorthus.sparkdlc.client.role.civilian.spiritualist.SpiritPossessionClient;
import dev.caecorthus.sparkdlc.client.role.civilian.tarotreader.TarotDivinationClientState;
import dev.caecorthus.sparkdlc.client.role.civilian.timekeeper.TimekeeperWatchClientHooks;
import dev.caecorthus.sparkdlc.client.role.civilian.undercover.UndercoverClient;
import dev.caecorthus.sparkdlc.client.role.civilian.usec.UsecClientModule;
import dev.caecorthus.sparkdlc.client.role.civilian.vendetta.VendettaKnifeModelLoadingPlugin;
import dev.caecorthus.sparkdlc.client.role.civilian.waiter.WaiterTaskRevealClientHooks;
import dev.caecorthus.sparkdlc.client.role.jiahao.corruptcop.CorruptCopClientHooks;
import dev.caecorthus.sparkdlc.client.role.jiahao.corruptcop.CorruptCopMusicController;
import dev.caecorthus.sparkdlc.client.role.jiahao.insider.InsiderClient;
import dev.caecorthus.sparkdlc.client.role.jiahao.sphinxhao.SphinxHaoClient;
import dev.caecorthus.sparkdlc.client.role.killer.assassin.AssassinGuessSources;
import dev.caecorthus.sparkdlc.client.role.killer.bandit.BanditRevolverClient;
import dev.caecorthus.sparkdlc.client.role.killer.bellringer.BellRingerClient;
import dev.caecorthus.sparkdlc.client.role.killer.blackraven.BlackRavenClientModule;
import dev.caecorthus.sparkdlc.client.role.killer.blackraven.crow.BlackRavenCrowClient;
import dev.caecorthus.sparkdlc.client.role.killer.blackraven.crow.BlackRavenCrowWorldClient;
import dev.caecorthus.sparkdlc.client.role.killer.bomber.BomberDroneClient;
import dev.caecorthus.sparkdlc.client.role.killer.bomber.TimedBombMarkerClientStore;
import dev.caecorthus.sparkdlc.client.role.killer.hiro.HiroClientModule;
import dev.caecorthus.sparkdlc.client.role.killer.hunter.DoubleBarrelMuzzleClient;
import dev.caecorthus.sparkdlc.client.role.killer.hunter.HunterTrapDismantleHud;
import dev.caecorthus.sparkdlc.client.role.killer.magician.MagicianKeyRepeatGuard;
import dev.caecorthus.sparkdlc.client.role.killer.magician.MagicianPlaybackEntityRenderer;
import dev.caecorthus.sparkdlc.client.role.killer.magician.MagicianPuppetStandIn;
import dev.caecorthus.sparkdlc.client.role.killer.morphling.MorphlingClientHooks;
import dev.caecorthus.sparkdlc.client.role.killer.ninja.NinjaGrappleClientPull;
import dev.caecorthus.sparkdlc.client.role.killer.partyanimal.PartyBalloonClientHooks;
import dev.caecorthus.sparkdlc.client.role.killer.partyanimal.PartygoerClientHooks;
import dev.caecorthus.sparkdlc.client.role.killer.partyanimal.PartygoerInstinctClientHooks;
import dev.caecorthus.sparkdlc.client.role.killer.saboteur.SaboteurClientAbilityRules;
import dev.caecorthus.sparkdlc.client.role.killer.specter.SpecterClientModule;
import dev.caecorthus.sparkdlc.client.role.killer.timestealer.TimeStealerClient;
import dev.caecorthus.sparkdlc.client.role.killer.witchmaiden.MiniWitherClient;
import dev.caecorthus.sparkdlc.client.role.killer.witchmaiden.WitchMaidenClientModule;
import dev.caecorthus.sparkdlc.client.role.neutral.criminalreasoner.CriminalReasonerClientModule;
import dev.caecorthus.sparkdlc.client.role.neutral.ferryman.FerrymanClient;
import dev.caecorthus.sparkdlc.client.role.neutral.fiend.FiendDashClient;
import dev.caecorthus.sparkdlc.client.role.neutral.jester.JesterMomentClientHooks;
import dev.caecorthus.sparkdlc.client.role.neutral.koko.KokoClient;
import dev.caecorthus.sparkdlc.client.role.neutral.pathogen.PathogenClientHooks;
import dev.caecorthus.sparkdlc.client.role.neutral.shadowjester.ShadowJesterShowdownMusicController;
import dev.caecorthus.sparkdlc.client.role.neutral.stowaway.StowawaySensesClient;
import dev.caecorthus.sparkdlc.client.role.neutral.taotie.TaotieHeadClientHooks;
import dev.caecorthus.sparkdlc.client.role.neutral.vulture.SkateboardClient;
import dev.caecorthus.sparkdlc.client.role.neutral.vulture.VultureSuperCurseClientHooks;
import dev.caecorthus.sparkdlc.client.role.witch.abysslistener.AbyssListenerClient;
import dev.caecorthus.sparkdlc.client.role.witch.bewitched.BewitchedClientPresentation;
import dev.caecorthus.sparkdlc.client.role.witch.curser.CurserClientHooks;
import dev.caecorthus.sparkdlc.client.role.witch.grandwitch.GrandWitchClientModule;
import dev.caecorthus.sparkdlc.client.role.witch.potiongunner.PotionGunnerClient;
import dev.caecorthus.sparkdlc.client.role.witch.potiongunner.PotionShellEntityRenderer;
import dev.caecorthus.sparkdlc.client.role.witch.riftwalker.RiftwalkerClient;
import dev.caecorthus.sparkdlc.client.round.mapreset.MapResetHud;
import dev.caecorthus.sparkdlc.client.sabotage.BlackoutSightClient;
import dev.caecorthus.sparkdlc.client.sabotage.SabotageClient;
import dev.caecorthus.sparkdlc.client.skill.SecondaryAbilityController;
import dev.caecorthus.sparkdlc.client.tablet.TabletChatNotifier;
import dev.caecorthus.sparkdlc.client.tablet.TabletClientHighlights;
import dev.caecorthus.sparkdlc.client.tablet.TabletHeldHud;
import dev.caecorthus.sparkdlc.client.tablet.screen.TabletClientState;
import dev.caecorthus.sparkdlc.client.trait.audio.DepressionRageLoopController;
import dev.caecorthus.sparkdlc.client.trait.audio.FinalMomentMusicController;
import dev.caecorthus.sparkdlc.client.trait.hud.DepressionHud;
import dev.caecorthus.sparkdlc.client.trait.instinct.InstinctViewerContext;
import dev.caecorthus.sparkdlc.client.trait.killer.BluePoisonMarkerClientStore;
import dev.caecorthus.sparkdlc.client.trait.killer.NewKillerTraitsClient;
import dev.caecorthus.sparkdlc.client.world.LoadedEntities;
import dev.caecorthus.sparkdlc.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.component.WitchWorldComponent;
import dev.caecorthus.sparkdlc.faction.api.FactionInstinctPolicy;
import dev.caecorthus.sparkdlc.faction.vision.FactionCohortRules;
import dev.caecorthus.sparkdlc.faction.vision.FactionInstinctRules;
import dev.caecorthus.sparkdlc.item.TimekeeperWatchItem;
import dev.caecorthus.sparkdlc.net.UseWitchSkillC2SPacket;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.caecorthus.sparkdlc.role.civilian.athlete.AthleteRules;
import dev.caecorthus.sparkdlc.role.civilian.blind.BlindRules;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.ControlExpertEntities;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelRules;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.UseGuardianAngelSkillC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.hanna.HannaRules;
import dev.caecorthus.sparkdlc.role.civilian.judge.JudgeRules;
import dev.caecorthus.sparkdlc.role.civilian.junker.JunkerRules;
import dev.caecorthus.sparkdlc.role.civilian.medium.MediumRules;
import dev.caecorthus.sparkdlc.role.civilian.medium.UseMediumSkillC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.noa.NoaRules;
import dev.caecorthus.sparkdlc.role.civilian.orthopedist.UseOrthopedistSkillC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.planner.PlannerRules;
import dev.caecorthus.sparkdlc.role.civilian.saint.SaintRules;
import dev.caecorthus.sparkdlc.role.civilian.saint.flash.HolyFlashEntities;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorRules;
import dev.caecorthus.sparkdlc.role.civilian.savior.UseSaviorSkillC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.sherry.SherryRules;
import dev.caecorthus.sparkdlc.role.civilian.timekeeper.TimekeeperConstants;
import dev.caecorthus.sparkdlc.role.civilian.timekeeper.TimekeeperWatchComponent;
import dev.caecorthus.sparkdlc.role.civilian.timekeeper.TimekeeperWatchMode;
import dev.caecorthus.sparkdlc.role.jiahao.sphinxhao.SphinxHaoRules;
import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorRules;
import dev.caecorthus.sparkdlc.role.killer.dominator.UseDominatorSkillC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.hiro.HiroRules;
import dev.caecorthus.sparkdlc.role.killer.hiro.returning.HiroReturnComponent;
import dev.caecorthus.sparkdlc.role.killer.hiro.returning.UseHiroSkillC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterEntities;
import dev.caecorthus.sparkdlc.role.killer.magician.UseMagicianAbilityC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.saboteur.SaboteurRole;
import dev.caecorthus.sparkdlc.role.killer.saboteur.UseSaboteurSkillC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterRules;
import dev.caecorthus.sparkdlc.role.killer.specter.UseSpecterSkillC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.WitchMaidenRules;
import dev.caecorthus.sparkdlc.role.neutral.criminalreasoner.CriminalReasonerRules;
import dev.caecorthus.sparkdlc.role.neutral.fiend.FiendParticipation;
import dev.caecorthus.sparkdlc.role.neutral.koko.KokoRules;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithParticipationRules;
import dev.caecorthus.sparkdlc.role.witch.curser.UseCurserAbilityC2SPacket;
import dev.caecorthus.sparkdlc.role.witch.potiongunner.shell.PotionGunnerEntities;
import dev.caecorthus.sparkdlc.trait.civilian.laststand.LastStandFinalMomentService;
import dev.caecorthus.sparkdlc.trait.component.TraitWorldComponent;
import dev.doctor4t.ratatouille.client.util.ambience.AmbienceUtil;
import dev.doctor4t.ratatouille.client.util.ambience.BackgroundAmbience;
import dev.doctor4t.wathe.api.event.AllowPlayerChat;
import dev.doctor4t.wathe.api.event.CanSeePoison;
import dev.doctor4t.wathe.api.event.GetInstinctHighlight;
import dev.doctor4t.wathe.api.event.ShouldAllowSuppressedKey;
import dev.doctor4t.wathe.api.event.ShouldShowCohort;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.particle.PoisonParticle;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint of SparkDLC, and the whole client start-up of the mod: {@link #onInitializeClient} is the list of
 * steps, each step is a method below that names what it registers.
 *
 * <p><b>The order of the steps and of the calls inside them is behaviour, not layout</b>, for the reasons given on
 * {@link dev.caecorthus.sparkdlc.SparkDLC}. On the client the order-sensitive registrations are the instinct
 * highlight, cohort, chat and suppressed-key listeners, the HUD layers (drawn in registration order), the session
 * resets on JOIN and DISCONNECT, the end-of-tick listeners and the Role Skill 2 key, which keeps the first handler
 * registered for a role: the role kits register theirs before the mod's own roles do. The sequence is the one the
 * mod had as five separate mods started in mod-id order (settings, guidebook and achievements first, then faction
 * and replay, the kits of the Wathe and NoellesRoles roles, traits, and the mod's own roles), and
 * {@code ./gradlew runClientAudit -PstartupOrderDump=<dir>} writes it down so that two builds can be compared.</p>
 *
 * SparkDLC 的客户端入口，也是本模组客户端的全部启动流程：{@link #onInitializeClient} 是步骤清单，每个步骤是下方的一个方法，
 * 方法名说明它注册的内容。
 *
 * <p><b>步骤之间以及步骤内部调用的顺序属于行为，而不是排版</b>，原因见 {@link dev.caecorthus.sparkdlc.SparkDLC}。
 * 客户端上对顺序敏感的注册有：本能高亮、同伙、聊天与被压制按键的监听器，HUD 图层（按注册顺序绘制），JOIN 与 DISCONNECT 时的
 * 会话重置，tick 结束监听器，以及“职业技能 2”按键（每个职业只保留最先注册的处理器：职业套件先于本模组自有职业注册）。
 * 该顺序沿用本模组还是五个独立 mod 时按 mod id 启动的顺序（先是设置、指南与成就，然后是阵营与回放、Wathe 与 NoellesRoles
 * 职业的套件、词条，最后是本模组自有职业），{@code ./gradlew runClientAudit -PstartupOrderDump=<目录>} 会把它写下来，
 * 以便比较两个构建。</p>
 */
public final class SparkDLCClient implements ClientModInitializer {
    /** Logger name kept from before the fusion so existing log filters still match. / 保留融合前的日志名，已有的日志过滤规则仍然有效。 */
    private static final Logger LOGGER = LoggerFactory.getLogger("SparkDLC/Achievements");

    private static SparkDlcConfigManager configManager;
    /** The world Wathe's muzzle map was last cleared for. 上次为之清空 Wathe 枪口表的世界。 */
    private static ClientWorld muzzleWorld;

    @Override
    public void onInitializeClient() {
        // Files and key bindings saved under the old names, before any module reads the new ones.
        // 以旧名字保存的文件与按键绑定，要在任何模块读取新名字之前带过来。
        LegacyClientNames.carryOver();
        startSettingsGuidebookAndAchievements();
        startFactionAndReplayClient();
        // The play receivers that belong to no client feature module, for the whole mod. Receivers are kept per
        // payload id, so this line has no order to keep. / 不属于任何客户端功能模块的 play 接收器，覆盖全模组。接收器按数据包 id
        // 保存，因此这一行没有需要保持的顺序。
        SparkDlcClientPackets.register();
        registerRoleKitClient();
        registerRoleKitTicksAndSessionResets();
        registerTraitClient();
        registerOwnRoleClient();
        registerWraithChatAndJumpRules();
        registerOwnRoleSessionResets();
        registerOwnRoleWorldHooks();
        registerSharedAbilityKey();
        registerWitchCohortAndPoisonVision();
        registerLateRoleHubs();
    }

    /**
     * The settings file, the achievement galaxy layout, Ponder, the charge HUD, the guidebook, the creative entries
     * and their tick and session listeners. The settings are loaded first: every later step may read them.
     * 设置文件、成就银河排布、Ponder、蓄力 HUD、指南、创造模式条目，以及它们的 tick 与会话监听器。设置最先加载：之后的每个步骤都可能读取它。
     */
    private static void startSettingsGuidebookAndAchievements() {
        configManager = SparkDlcConfigManager.load();
        // Lay the achievement galaxy out now, off the render thread. 现在就在渲染线程之外排布成就银河。
        Thread galaxy = new Thread(SparkDLCClient::layOutGalaxy, "SparkDLC galaxy layout");
        galaxy.setDaemon(true);
        galaxy.start();
        PonderSupport.init();
        ChargeIndicatorHud.register();
        GuidebookResources.register();
        NoellesCreativeEntries.register();
        ClientTickEvents.END_CLIENT_TICK.register(GuidebookClientState::tick);
        GuidebookInventoryButton.register();
        ClientTickEvents.END_CLIENT_TICK.register(AchievementClientState::tick);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // A proxy's server switch fires no DISCONNECT, only a new JOIN, so a latched instinct toggle is dropped
            // here too. JOIN runs on the client thread as the game-join packet finishes, before the new world is
            // ticked or drawn, so nothing of the new session has read the key yet.
            // 代理切换服务器不会触发 DISCONNECT，只会再次触发 JOIN，因此这里也清除锁定的本能切换。JOIN 在客户端线程上、
            // 进入游戏数据包处理完毕时触发，此时新世界尚未 tick 或绘制，新会话还没有读取过该按键。
            InstinctKeyController.reset();
            // A server with no advancements of its own never sends the update that would restore the page.
            // 自身没有任何进度的服务器不会发送更新包，因此加入时主动补上成就页。
            AchievementAdvancements.restore(handler.getAdvancementHandler());
        });
        // Fabric fires DISCONNECT from the Netty thread when the channel closes, while these resets touch state the
        // client thread reads (the guide session, the card texture, the round tracker); so they run there, in order.
        // 通道关闭时 Fabric 在 Netty 线程上触发 DISCONNECT，而这些重置涉及客户端线程读取的状态（指南会话、信息卡纹理、
        // 对局跟踪器），因此按原顺序放到客户端线程上执行。
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> {
            InstinctKeyController.reset();
            GuidebookClientState.disconnect();
            AchievementClientState.disconnect();
        }));
        ClientTickEvents.END_CLIENT_TICK.register(SparkDLCClient::forgetMuzzles);
    }

    /**
     * The version handshake of the whole mod, replay screen, match record, map-reset HUD, and the faction instinct
     * highlight and cohort listeners. 全模组的版本握手、回放界面、对局记录、地图重置 HUD，以及阵营本能高亮与同伙监听器。
     */
    private static void startFactionAndReplayClient() {
        SparkDLC.LOGGER.info("Initializing SparkDLC faction and replay client hooks.");
        // The one version handshake: the login-query answer and the play confirmation receiver. Receivers are kept
        // per channel, so their place in the start-up is free; the session edges that drop the confirmation stay in
        // the trait and own-role session listeners below, where the two former flags had them.
        // 唯一的版本握手：登录查询的应答与 play 确认接收器。接收器按频道保存，因此它在启动流程中的位置没有约束；
        // 丢弃确认的会话节点仍留在下方词条与自有职业的会话监听器中，即原先两个标记各自所在的位置。
        SparkDlcClientVersionHandshake.registerClient();
        // Replay screen: snapshot receiver, keybind, and the map-voting button. 回放界面：快照接收、按键与投票界面按钮。
        ReplayClient.register();
        // Match record: store-only receiver behind SparkMatchRecordClient. 对局记录：仅保存，经 SparkMatchRecordClient 读取。
        MatchRecordClient.register();
        // Lobby map reset: progress line at the top of the HUD. 大厅地图重置：HUD 顶部的进度行。
        MapResetHud.register();

        // Bridge through Wathe's event so other add-ons can still win by priority.
        // 通过 wathe 事件桥接，保留下游模组按优先级覆盖的空间。
        GetInstinctHighlight.EVENT.register(target -> {
            PlayerEntity viewer = MinecraftClient.getInstance().player;
            if (viewer == null) {
                return null;
            }

            GameWorldComponent gameComponent = GameWorldComponent.KEY.get(viewer.getWorld());
            if (!FactionInstinctRules.shouldUseCustomHighlight(
                    GameFunctions.isPlayerPlayingAndAlive(viewer),
                    GameFunctions.isPlayerSpectatingOrCreative(viewer)
            )) {
                return null;
            }
            // Wathe fires this several times per visible entity per frame, so stay allocation-free and stateless.
            // wathe 每帧对每个可见实体多次触发此事件，因此保持无分配、无状态。
            FactionInstinctPolicy.InstinctResult policyResult =
                    FactionInstinctRules.policyResultOrNull(viewer, target, gameComponent);
            if (policyResult != null) {
                return toWatheHighlight(policyResult);
            }
            return fallbackInstinctHighlight(viewer, target, gameComponent);
        });

        // Cohort display is visual only; it must not put custom factions into native killer teams.
        // 同伙显示只影响视觉提示，不能把自定义阵营塞进原生杀手队伍。
        ShouldShowCohort.EVENT.register((viewer, target) -> {
            GameWorldComponent gameComponent = GameWorldComponent.KEY.get(viewer.getWorld());
            if (FactionCohortRules.sharesCohort(viewer, target, gameComponent)) {
                return ShouldShowCohort.CohortResult.show();
            }
            return null;
        });
    }

    /**
     * Client hubs of the kits for the Wathe and NoellesRoles roles, of sabotage and of the tablet. The Role Skill 2
     * handlers registered here come before those of the mod's own roles, and the first one per role wins.
     * Wathe 与 NoellesRoles 职业套件、破坏与平板电脑的客户端注册中心。这里注册的“职业技能 2”处理器先于本模组自有职业的处理器，
     * 每个职业以最先注册的为准。
     */
    private static void registerRoleKitClient() {
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
        // Spiritualist Wraith possession; the key and Wraith sight bind by direct calls to the Role Skill 2 key registry and
        // WraithViewerGates. / 灵界行者附身冤魂；按键与冤魂可见通过直接调用“职业技能 2”按键注册表与 WraithViewerGates 挂接。
        SpiritPossessionClient.register();
        TabletClientHighlights.register();
        TabletChatNotifier.register();
        TabletHeldHud.register();
        SabotageClient.register();
        // Binds to the Role Skill 2 key by a direct call to its registry.
        // 直接调用按键注册表挂接到“职业技能 2”按键。
        VultureSuperCurseClientHooks.register();
        PartygoerClientHooks.register();
        PartygoerInstinctClientHooks.register();
        WaiterTaskRevealClientHooks.register();
    }

    /**
     * End-of-tick listeners, the Dying Watch tooltip and the session resets of those kits.
     * 这些套件的 tick 结束监听器、濒死怀表提示与会话重置。
     */
    private static void registerRoleKitTicksAndSessionResets() {
        ClientTickEvents.END_CLIENT_TICK.register(CorruptCopMusicController::tick);
        ClientTickEvents.END_CLIENT_TICK.register(client ->
                KillerTeamEconomyClientHooks.tick(client.world));
        ClientTickEvents.END_CLIENT_TICK.register(ShadowJesterShowdownMusicController::tick);
        ClientTickEvents.END_CLIENT_TICK.register(TimekeeperWatchClientHooks::tick);
        // Assassin: the ability key opens the hit list during a cooldown too (NoellesRoles only opens it when ready).
        // 刺客：冷却期间按技能键也能打开刺杀名单（NoellesRoles 只在可猜测时打开）。
        ClientTickEvents.END_CLIENT_TICK.register(AssassinGuessSources::tickAbilityKey);
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!stack.isOf(SparkDlcItems.dyingWatch())) {
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
                    ? Text.translatable("item.sparkdlc.dying_watch.cooldown", (cooldownTicks + 19) / 20)
                    : Text.translatable("item.sparkdlc.dying_watch.cooldown.ready");
            var style = net.minecraft.text.Style.EMPTY.withColor(0xADD8E6).withItalic(false);
            int insertAt = Math.min(1, lines.size());
            lines.add(insertAt, Text.translatable("item.sparkdlc.dying_watch.cost", cost).setStyle(style));
            lines.add(insertAt + 1, cooldown.copy().setStyle(style));
        });

        // Fabric may fire DISCONNECT on the Netty thread (channelInactive), racing the client thread that still ticks and
        // applies queued packets, so the reset runs on the client thread. JOIN (client thread, before any play packet)
        // repeats it in case MinecraftClient.disconnect dropped the queued task.
        // Fabric 可能在 Netty 线程（channelInactive）上触发 DISCONNECT，与仍在 tick 并执行排队数据包的客户端线程竞争，因此重置在
        // 客户端线程上执行。JOIN（客户端线程、任何游戏数据包之前）再重置一次，以防排队的任务被 MinecraftClient.disconnect 丢弃。
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(SparkDLCClient::resetRoleKitSessionState));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetRoleKitSessionState());
    }

    /**
     * Owner-inventory adapter, killer-trait client, blue-poison particles, session resets (with the
     * confirmed-server flag's login and JOIN edges), the Final Moment highlight and the trait tick.
     * 所有者背包适配器、杀手词条客户端、蓝毒粒子、会话重置（含已确认服务端标记的登录与 JOIN 节点）、终局时刻高亮与词条 tick。
     */
    private static void registerTraitClient() {
        dev.caecorthus.sparkdlc.client.trait.gui.OwnerInventoryClientAdapter.install();
        NewKillerTraitsClient.initialize();
        ParticleFactoryRegistry.getInstance().register(SparkDlcParticles.BLUE_POISON, PoisonParticle.Factory::new);
        // A new connection starts unconfirmed, even if the previous one never reached its play session. This and the
        // play DISCONNECT listener below are the flag's only resets on those two edges.
        // 新连接从未确认状态开始，即使上一个连接没有进入 play 会话。此处与下方的 play DISCONNECT 监听器是这两个节点上
        // 对该标记仅有的重置。
        ClientLoginConnectionEvents.INIT.register((handler, client) -> SparkDlcServerConnection.reset());
        /*
         * JOIN also starts a play session after a proxy backend switch, which fires no DISCONNECT: session-scoped
         * state must not survive it. JOIN runs on the client thread. The forced-melee cooldown snapshot is cleared by
         * NewKillerTraitsClient's own JOIN listener.
         * 代理切换后端同样会触发 JOIN 开始新的 play 会话，但不会触发 DISCONNECT：会话级状态不能跨过它保留。
         * JOIN 在客户端线程执行。强制近战冷却快照由 NewKillerTraitsClient 自己的 JOIN 监听清理。
         */
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            BluePoisonMarkerClientStore.clear();
            FinalMomentMusicController.reset(client);
            SparkDlcServerConnection.beginPlaySession();
            dev.caecorthus.sparkdlc.client.trait.gui.OwnerInventoryClientAdapter.resetNativeCard();
            InstinctViewerContext.clear();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            // The volatile flag is safe to drop from the network thread; features gated on it go dark at once.
            // volatile 标记可以在网络线程上直接清除；依赖它的功能随即停用。
            SparkDlcServerConnection.reset();
            runSessionCleanup(client, handler, () -> {
                BluePoisonMarkerClientStore.clear();
                FinalMomentMusicController.reset(client);
                dev.caecorthus.sparkdlc.client.trait.gui.OwnerInventoryClientAdapter.resetNativeCard();
                InstinctViewerContext.clear();
            });
        });
        registerFinalMomentHighlight();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            DepressionRageLoopController.tick(client);
            FinalMomentMusicController.tick(client);
            dev.caecorthus.sparkdlc.client.trait.jester.JesterMomentClientHooks.tick(client);
            if (SparkDlcServerConnection.isConfirmedServer()) {
                DepressionHud.tick();
            }
        });
    }

    /**
     * Client hubs of the mod's own roles: the Role Skill 2 key binding, entity renderers and the role-owned HUD
     * rows. 本模组自有职业的客户端注册中心：“职业技能 2”按键绑定、实体渲染器与各职业自有的 HUD 行。
     */
    private static void registerOwnRoleClient() {
        SparkDLC.LOGGER.info("Initializing SparkDLC role client hooks.");
        // Per-world-pass memo of local-viewer facts read by the instinct, outline and Blind hooks.
        // 本能、描边与盲人钩子读取的本地观察者事实，按世界渲染 pass 缓存。
        LocalViewerSnapshot.register();
        SecondaryAbilityController.registerKeyBinding();
        BlackRavenClientModule.register();
        GrandWitchClientModule.register();
        ApprenticeClientModule.register();
        SpecterClientModule.register();
        // 二阶堂希罗 (2026-11-11): role-owned HUD hub. / 二阶堂希罗（2026-11-11）：职业自有 HUD 注册中心。
        HiroClientModule.register();
        WitchMaidenClientModule.register();
        MiniWitherClient.register();
        BlackRavenCrowClient.register();
        BellRingerClient.init();
        TimeStealerClient.init();
        FerrymanClient.register();
        VendettaKnifeModelLoadingPlugin.register();
        SecondaryAbilityController.reset();
        registerEntityRenderers();
        JudgeClientModule.register();
        JunkerClientModule.register();
        KnightSwordClient.register();
        KnightDuelClient.register();
        dev.caecorthus.sparkdlc.client.role.killer.dominator.DominatorClientModule.register();
        // 夏目安安 (PR #194): the target-menu receiver (instinct is gated by NatsumeInstinctGateMixin).
        // 夏目安安（PR #194）：目标菜单接收器（本能由 NatsumeInstinctGateMixin 屏蔽）。
        dev.caecorthus.sparkdlc.client.role.killer.natsume.NatsumeClientModule.register();
        SaviorClient.register();
        ProphetClientModule.register();
        ControlExpertStunClient.register();
        ControlExpertStatusHud.register();
        InsiderClient.init();
        PotionGunnerClient.init();
        SeekerClientModule.register();
        FisherClient.register();
        dev.caecorthus.sparkdlc.client.role.neutral.fiend.FiendClient.init();
        StowawaySensesClient.register();
        BlindClient.register();
        dev.caecorthus.sparkdlc.client.role.civilian.saint.HolyFlashAudioClient.register();
        AbyssListenerClient.init();
        RiftwalkerClient.init();
        // Bewitched promotion progress line (role-owned HUD). / 魔化使晋升进度行（职业自有 HUD）。
        BewitchedClientPresentation.register();
        // Recruit first-task knife progress line (role-owned HUD). / 新兵首个任务发刀进度行（职业自有 HUD）。
        RecruitClientPresentation.register();
        // Athlete Dash line (role-owned HUD); the dash itself rides the shared ability key below.
        // 运动员冲刺行（职业自有 HUD）；冲刺本身走下方的共享技能键。
        AthleteClient.init();
        // Noa painting line (role-owned HUD); the painting itself rides the shared ability key below.
        // 诺亚作画行（职业自有 HUD）；作画本身走下方的共享技能键。
        NoaClient.init();
        // Hanna float line (role-owned HUD); the float itself rides the shared ability key below.
        // 汉娜浮游行（职业自有 HUD）；浮游本身走下方的共享技能键。
        HannaClient.init();
        // Koko traffic and stream rows plus her live instinct outline (role-owned); going live rides the shared
        // ability key below. / 可可的流量行、直播行与直播期间的本能描边（职业自有）；开播走下方的共享技能键。
        KokoClient.init();
        // 狮身豪 (Team Jiahao): HUD rows, its keyed infected outline, 《Fade》 for every client and its N key (role-owned);
        // 嘉豪病毒 rides the shared ability key below. / 狮身豪（嘉豪阵营）：HUD 行、按键型感染者描边、所有客户端的《Fade》与
        // N 键（职业自有）；嘉豪病毒走下方的共享技能键。
        SphinxHaoClient.init();
        // 犯罪推理学家 (PR #228): reasoning menu, invisibility key, instinct hide and HUD rows (role-owned); the menu
        // request rides the shared ability key below. / 犯罪推理学家（PR #228）：推理菜单、隐身键、本能屏蔽与 HUD 行（职业自有）；
        // 菜单请求走下方的共享技能键。
        CriminalReasonerClientModule.register();
        // BEAR smoke (spike): the S2C receivers that write SmokeClientState, and the disconnect cleanup.
        // BEAR 烟雾（原型）：写入 SmokeClientState 的 S2C 接收器与断线清理。
        dev.caecorthus.sparkdlc.client.role.civilian.bear.smoke.SmokeNetworkClient.register();
        // BEAR volumetric smoke (client presentation only; reads SmokeClientState). / BEAR 体积烟雾（纯客户端展示；只读 SmokeClientState）。
        dev.caecorthus.sparkdlc.client.role.civilian.bear.smoke.render.SmokeRenderWiring.register();
        // BEAR smoke under an Iris shader pack: liveness, billboard fallback, per-entity outline veto (no-op without a pack).
        // Iris 光影包下的 BEAR 烟雾：存活判定、公告板兜底、逐实体描边否决（未用光影包时不做任何事）。
        dev.caecorthus.sparkdlc.client.role.civilian.bear.smoke.shaderpack.SmokeShaderPackWiring.register();
    }

    /** What an active Wraith may type and whether it may jump. / 活跃冤魂能否打字、能否跳跃。 */
    private static void registerWraithChatAndJumpRules() {
        AllowPlayerChat.EVENT.register(player -> {
            if (!SparkDlcServerConnection.isConfirmedServer()) {
                return false;
            }
            var role = GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
            return WraithParticipationRules.mayUseTextChat(
                    WraithClientState.isActive(player),
                    GuardianAngelRules.isGuardianAngel(role),
                    player.isCreative()
            );
        });
        ShouldAllowSuppressedKey.EVENT.register(keyBinding -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!SparkDlcServerConnection.isConfirmedServer()
                    || client.player == null
                    || keyBinding != client.options.jumpKey) {
                return false;
            }
            // Wathe 会先询问该事件再压制跳跃键；活跃冤魂必须无视地图禁跳和跳跃体力检查。
            boolean activeWraith = WraithClientState.isActive(client.player);
            return activeWraith && WraithParticipationRules.mayJump(true, false);
        });
    }

    /** Session state of the mod's own roles, dropped on every connection edge. / 本模组自有职业的会话状态，在每个连接节点清除。 */
    private static void registerOwnRoleSessionResets() {
        // Reset on every connection lifecycle edge so failed login attempts cannot leak confirmed state. The
        // confirmed-server flag itself is dropped on login INIT and play DISCONNECT by the trait session listeners
        // above, which run earlier on the same edge, and no listener in between can confirm a server; so only login
        // DISCONNECT, which has no such listener, drops it here.
        // 在每个连接生命周期节点清理状态，避免失败的登录尝试残留已确认标记。已确认标记本身在登录 INIT 与 play DISCONNECT 上
        // 由上方词条会话监听器清除，它们在同一节点上更早执行，且其间没有任何监听器能确认服务端；因此只有没有这类监听器的
        // 登录 DISCONNECT 在这里清除它。
        ClientLoginConnectionEvents.INIT.register((handler, client) -> resetConnectionClientState());
        ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> resetConnectionState());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetConnectionClientState());
        // A proxy backend switch goes play -> configuration -> play on one connection: no DISCONNECT and no login INIT
        // fire, only another JOIN, so the previous backend's session state is dropped here too. Play-phase state of the
        // new session only arrives after JOIN, so nothing of it can be wiped. The confirmed-server flag is NOT reset
        // here: on a direct connection it was set by this same connection's login query and must survive its own JOIN.
        // Its JOIN edge is SparkDlcServerConnection.beginPlaySession() in the trait session listener above, which keeps
        // exactly that confirmation and drops one left by a previous backend.
        // 代理切换后端时，同一连接经历 play -> configuration -> play：既不触发 DISCONNECT 也不触发登录 INIT，只会再次 JOIN，
        // 因此在这里同样丢弃上一个后端的会话状态。新会话的 play 阶段状态只会在 JOIN 之后到达，所以不会被误清。已确认服务端
        // 标记不在此重置：直连时它由同一连接的登录查询设置，必须在自己的 JOIN 之后保留。它的 JOIN 节点是上方词条会话监听器中的
        // SparkDlcServerConnection.beginPlaySession()：恰好保留这种确认，并丢弃上一个后端留下的确认。
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetOwnRoleSessionState());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> releaseClosedWorldPlayers());
    }

    /**
     * Instinct suppression, Hunter traps, the double-barrel muzzle, the Ninja grapple pull, Orthopedist and Prophet
     * hooks and the ceremonial-sword music. 本能压制、猎人陷阱、双管猎枪枪口、忍者钩爪牵引、正骨师与预言家钩子，以及仪式剑配乐。
     */
    private static void registerOwnRoleWorldHooks() {
        WitchInstinctSuppressionClientHooks.register();
        HunterTrapClientHooks.register();
        HunterTrapDismantleHud.register();
        DoubleBarrelMuzzleClient.register();
        NinjaGrappleClientPull.register();
        OrthopedistClientHooks.register();
        ProphetCorpseHighlightClientHooks.register();
        registerGrandWitchCeremonialSwordBgm();
    }

    /**
     * The end-of-tick listener of the mod's own roles. It also owns the shared ability key: one press goes to the
     * first branch that claims it, so the order of the branches is the priority between roles.
     * 本模组自有职业的 tick 结束监听器。它同时负责共享技能键：一次按下交给第一个认领它的分支，因此分支顺序就是职业之间的优先级。
     */
    private static void registerSharedAbilityKey() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            TarotDivinationClientState.tick(client);
            JudgeClientModule.tick(client);
            ProphetClientModule.tick(client);
            SecondaryAbilityController.tick(client);
            if (!SparkDlcServerConnection.isConfirmedServer()) {
                AbilityKeyBridge.reset();
                DeathRayClientHooks.reset();
                KidnapperThrowClientHooks.reset();
                return;
            }
            KidnapperThrowClientHooks.tick(client);
            GrandWitchFearClientHooks.tick();
            DeathRayClientHooks.tick(client);
            dev.caecorthus.sparkdlc.client.gui.OwnerInventoryPresenter.tick(client);
            MagicianKeyRepeatGuard.observe(AbilityKeyBridge.isHeld());
            if (client.player != null
                    && client.getNetworkHandler() != null
                    && AbilityKeyBridge.wasPressed()) {
                var role = GameWorldComponent.KEY.get(client.player.getWorld()).getRole(client.player);
                boolean exactSaboteurRole = role != null
                        && SaboteurRole.ID.equals(role.identifier());
                if (MediumVoteClient.ownsAbilityKey(client)) {
                    // Ahead of every role branch: during a live Medium vote window (a 通灵 question or 幽灵的加护) a
                    // dead round member's press opens that window's vote screen, whatever role they held (the dead
                    // have no other use for the key). The server re-checks every vote.
                    // 位于所有职业分支之前：通灵师投票窗口（通灵提问或幽灵的加护）进行期间，本局死者的按下打开该窗口的
                    // 投票界面，无论其原职业为何（死者的技能键别无他用）。每一票都由服务端重新校验。
                    MediumVoteClient.onAbilityKey(client);
                } else if (KnightDuelClient.ownsAbilityKey(client.player, role)) {
                    // Checked first: either duelist's press does nothing (any role, owner 2026-10-30; the server also
                    // drops their G-skill payloads), and a Knight's press opens the duel target panel.
                    // 最先判定：任一决斗者的按下无效（任何职业，所有者 2026-10-30；服务端也会丢弃其 G 键技能包），
                    // 骑士的按下打开决斗目标面板。
                    KnightDuelClient.onAbilityKey(client);
                } else if (role != null && role.identifier().equals(dev.caecorthus.sparkdlc.SparkDlcRoles.MAGICIAN_ID)) {
                    // Real role only; the server re-checks the role and every skill lock before advancing the stage.
                    // Key-repeat presses while the key stays held are dropped (one stage step per physical press).
                    // 仅限真实职业；服务端会再次检查职业与所有技能锁后才推进阶段。按住不放时的重复按下被丢弃（每次物理按键只推进一步）。
                    if (MagicianKeyRepeatGuard.accept() && ClientPlayNetworking.canSend(UseMagicianAbilityC2SPacket.ID)) {
                        ClientPlayNetworking.send(new UseMagicianAbilityC2SPacket(UseMagicianAbilityC2SPacket.ADVANCE));
                    }
                } else if (JudgeRules.isJudge(role)) {
                    JudgeClientModule.requestSelection(client);
                } else if (CriminalReasonerRules.isReasoner(role)) {
                    // Real role only; the module drops a dead, spectating or swallowed Reasoner's press. The key opens
                    // his own reasoning menu. / 仅限真实职业；死亡、旁观或被吞下的推理学家的按键由模块丢弃。该键打开推理菜单。
                    CriminalReasonerClientModule.requestSelection(client);
                } else if (JunkerRules.isRagpicker(role)) {
                    // Real role only; a dead or swallowed Ragpicker's press is dropped here, not sent as a witch skill.
                    // 仅限真实职业；死亡或被吞下的拾荒者的按键在此被丢弃，不会作为魔女技能发送。
                    JunkerClientModule.onAbilityKey(client);
                } else if (ProphetClientModule.ownsAbilityKey(client.player, role)) {
                    // Prophecy opens its own session instead of sending the generic skill packet.
                    // 预言打开自有会话，而不是发送通用技能包。
                    ProphetClientModule.requestProphecy(client);
                } else if (EmmaClientModule.isEmma(client.player)) {
                    EmmaClientModule.use(client.player);
                } else if (exactSaboteurRole) {
                    if (SaboteurClientAbilityRules.shouldSend(
                            true,
                            true,
                            WraithClientState.isPromoted(client.player),
                            ClientPlayNetworking.canSend(UseSaboteurSkillC2SPacket.ID)
                    )) {
                        ClientPlayNetworking.send(new UseSaboteurSkillC2SPacket());
                    }
                } else if (DominatorRules.isDominator(role)) {
                    // Real role only. Sends nothing while the press cannot do anything: dead, in spectator mode or
                    // swallowed (the shared side-neutral caster gate), no uses left, or on cooldown. The server re-checks
                    // all of this plus its own gates, so this only saves a doomed request.
                    // 仅限真实职业。按键无效时不发送：死亡、旁观模式或被吞下（两端共用的施放者门槛）、次数为 0 或冷却中。
                    // 服务端会再次检查这些以及其自身门槛，此处只是省掉注定被拒的请求。
                    DominatorPlayerComponent dominator = DominatorPlayerComponent.KEY.get(client.player);
                    if (DominatorRules.isActiveCaster(client.player)
                            && dominator.getRemainingUses() > 0
                            && !dominator.isOnCooldown()
                            && ClientPlayNetworking.canSend(UseDominatorSkillC2SPacket.ID)) {
                        ClientPlayNetworking.send(new UseDominatorSkillC2SPacket());
                    }
                } else if (SpecterRules.isSpecter(role)) {
                    // The server owns the full validation; the synced cooldown only trims obviously dead presses.
                    // 完整校验在服务端；本地同步的冷却只用来过滤明显无效的按键。
                    if (!SpecterPlayerComponent.KEY.get(client.player).isOnCooldown()
                            && ClientPlayNetworking.canSend(UseSpecterSkillC2SPacket.ID)) {
                        ClientPlayNetworking.send(new UseSpecterSkillC2SPacket());
                    }
                } else if (HiroRules.isHiro(role)) {
                    // 二阶堂希罗 死亡回归, real role only: an empty request. The server owns the full validation (mana,
                    // window, gates); the synced cooldown only trims obviously dead presses, as for the Specter.
                    // 二阶堂希罗的死亡回归，仅限真实职业：空请求。完整校验（魔力、窗口、门槛）在服务端；与幽魂一样，本地同步的冷却
                    // 只用来过滤明显无效的按键。
                    if (!HiroReturnComponent.KEY.get(client.player).isOnCooldown()
                            && ClientPlayNetworking.canSend(UseHiroSkillC2SPacket.ID)) {
                        ClientPlayNetworking.send(new UseHiroSkillC2SPacket());
                    }
                } else if (role != null && role.identifier().equals(dev.caecorthus.sparkdlc.SparkDlcRoles.CURSER_ID)) {
                    if (CurserClientHooks.canUse(client.player)) {
                        CurserClientHooks.use();
                    }
                } else if (GuardianAngelRules.isGuardianAngel(role)) {
                    ClientPlayNetworking.send(new UseGuardianAngelSkillC2SPacket());
                } else if (GameWorldComponent.KEY.get(client.player.getWorld())
                        .isRole(client.player, dev.caecorthus.sparkdlc.SparkDlcRoles.orthopedist())) {
                    // Widened by the Black Raven acting overlay; getRole stays raw. / 黑羽鸦扮演覆盖层会放宽此判定；getRole 仍为真实身份。
                    ClientPlayNetworking.send(new UseOrthopedistSkillC2SPacket());
                } else if (SaviorRules.isSavior(role)) {
                    // Client-side guard is presentation only; the server re-checks role, round, cooldown and the active domain.
                    // 客户端门槛仅用于表现；服务端会重新校验职业、回合、冷却与领域是否已展开。
                    SaviorPlayerComponent savior = SaviorPlayerComponent.KEY.get(client.player);
                    if (savior.getCooldownTicks() <= 0
                            && savior.getDomainTicks() <= 0
                            && ClientPlayNetworking.canSend(UseSaviorSkillC2SPacket.ID)) {
                        ClientPlayNetworking.send(new UseSaviorSkillC2SPacket());
                    }
                } else if (BlindRules.isBlind(role)) {
                    // Real role only (the Blind is never a disguise target). / 仅真实职业（盲人不可被伪装）。
                    BlindKitClientWiring.onAbilityKey(client);
                } else if (FiendParticipation.isMomentFiend(client.player)) {
                    // Real role plus the synced moment owner; Dash exists only during the moment. The server decides use.
                    // 真实职业加同步的时刻拥有者；疾驰只在时刻中存在，是否生效由服务端决定。
                    FiendDashClient.onAbilityKey(client);
                } else if (FerrymanClient.isLocalFerryman(client)) {
                    // Real role only; the press asks the server to pardon a held lethal hit (1.5 s window).
                    // 仅真实职业；按下即请求服务端宽恕一次被扣住的致命击杀（1.5 秒窗口）。
                    FerrymanClient.onAbilityKey(client);
                } else if (AthleteRules.isAthlete(role)) {
                    // Real role only (the Athlete is never a disguise target); the server decides use.
                    // 仅真实职业（运动员不可被伪装）；是否生效由服务端决定。
                    AthleteDashClient.onAbilityKey(client);
                } else if (SphinxHaoRules.isSphinxHao(role)) {
                    // 嘉豪病毒, real role only (getRole stays raw): an empty request; the synced cooldown only trims
                    // obviously refused presses (with a local hint), the server decides use.
                    // 嘉豪病毒，仅真实职业（getRole 始终是真实身份）：空请求；同步的冷却只过滤明显被拒的按键（并给出本地提示），
                    // 是否生效由服务端决定。
                    SphinxHaoClient.onAbilityKey(client);
                } else if (role != null
                        && dev.caecorthus.sparkdlc.role.killer.natsume.NatsumeRules.ROLE_ID
                                .equals(role.identifier())) {
                    // 夏目安安 (PR #194, fork order: right before Noa): the key asks the server for the target menu.
                    // 夏目安安（PR #194，fork 中的顺序：紧挨诺亚之前）：按键向服务端请求目标菜单。
                    dev.caecorthus.sparkdlc.client.role.killer.natsume.NatsumeClientModule.onAbilityKey(client);
                } else if (NoaRules.isNoa(role)) {
                    // Real role only (getRole stays raw, so a Black Raven acting as Noa keeps its own key); the server
                    // decides use. / 仅真实职业（getRole 始终是真实身份，扮演诺亚的黑羽鸦保留自己的按键）；是否生效由服务端决定。
                    NoaPaintClient.onAbilityKey(client);
                } else if (PlannerRules.isPlanner(role)) {
                    // 规划大师 recommendation, real role only (getRole stays raw, so a Black Raven acting as the planner
                    // keeps its own key): sends the aimed player's UUID, or her own; the server decides use.
                    // 规划大师的推荐，仅真实职业（getRole 始终是真实身份，扮演规划大师的黑羽鸦保留自己的按键）：发送准星下
                    // 玩家的 UUID，没有目标时为她自己的；是否生效由服务端决定。
                    PlannerClient.onAbilityKey(client);
                } else if (MediumRules.isMedium(role)) {
                    // 通灵 (the shared ability key): empty request, the server owns every check and opens the box.
                    // 幽灵的加护 is on the Role Skill 2 key (MediumClientModule) and 招魂 in the inventory drawer.
                    // 通灵（共享技能键）：空请求，全部校验在服务端，并由服务端打开提问框。幽灵的加护在“职业技能 2”键上
                    // （MediumClientModule），招魂在背包抽屉中。
                    if (ClientPlayNetworking.canSend(UseMediumSkillC2SPacket.ID)) {
                        ClientPlayNetworking.send(new UseMediumSkillC2SPacket());
                    }
                } else if (KokoRules.isKoko(role)) {
                    // Go live (the shared ability key), real role only (getRole stays raw): empty request, the server
                    // decides use. / 开播（共享技能键），仅真实职业（getRole 始终是真实身份）：空请求，是否生效由服务端决定。
                    KokoClient.onAbilityKey(client);
                } else if (HannaRules.isHanna(role)) {
                    // Real role only (getRole stays raw, so a Black Raven acting as Hanna keeps its own key); the server
                    // decides use. / 仅真实职业（getRole 始终是真实身份，扮演汉娜的黑羽鸦保留自己的按键）；是否生效由服务端决定。
                    HannaClient.onAbilityKey(client);
                } else if (SherryRules.isSherry(role)) {
                    // 推演 (the shared ability key), real role only: empty request, the server checks the corpse,
                    // cooldown and coins and answers with the candidate panel.
                    // 推演（共享技能键），仅限真实职业：空请求，服务端检查尸体、冷却与金币后以候选面板应答。
                    SherryClient.onAbilityKey(client);
                } else if (!WitchMaidenRules.isWitchMaiden(role)
                        && (WitchPlayerComponent.KEY.get(client.player).hasSkill()
                        || SaintRules.isSaint(role))) {
                    ClientPlayNetworking.send(new UseWitchSkillC2SPacket());
                }
            }
        });
    }

    /** Grand Witch cohort hiding and hidden-poison vision. / 大魔女同伙隐藏与隐藏毒药可见性。 */
    private static void registerWitchCohortAndPoisonVision() {
        ShouldShowCohort.EVENT.register((viewer, target) -> {
            if (!SparkDlcServerConnection.isConfirmedServer()) {
                return null;
            }
            if (WitchCohortClientHooks.isGrandWitchCohortPair(viewer, target)) {
                return ShouldShowCohort.CohortResult.hide(110);
            }
            return null;
        });
        CanSeePoison.EVENT.register(WitchPoisonVisionClientHooks::canSeeHiddenPoison);
    }

    /** The hubs that are appended last on purpose: BEAR, Medium, Sherry, then USEC. / 有意追加在最后的注册中心：BEAR、通灵师、雪莉，然后是 USEC。 */
    private static void registerLateRoleHubs() {
        // BEAR client hub (work packages append there), before USEC, which stays last.
        // BEAR 客户端注册中心（各工作包在其中追加），位于 USEC 之前，USEC 保持最后。
        dev.caecorthus.sparkdlc.client.role.civilian.bear.BearClientModule.register();
        // Medium client hub (Role Skill 2 key = 幽灵的加护), after BEAR and before USEC, which stays last.
        // 通灵师客户端注册中心（职业技能 2 键 = 幽灵的加护），位于 BEAR 之后、USEC 之前，USEC 保持最后。
        MediumClientModule.register();
        // 橘雪莉 client hub (PR #206: panel, Role Skill 2 key, private outlines, corner HUD), after the Medium and
        // before USEC, which stays last.
        // 橘雪莉客户端注册中心（PR #206：面板、职业技能 2 键、私有描边、右下角 HUD），位于通灵师之后、USEC 之前，USEC 保持最后。
        SherryClient.register();
        // USEC client hub, appended last: scope, HUD, attachment screen, impact cracks.
        // USEC 客户端注册中心，追加在最后：开镜、HUD、配件界面、命中裂痕。
        UsecClientModule.register();
    }

    /** The settings of the mod, loaded in the first start-up step. / 本模组的设置，在启动的第一个步骤中加载。 */
    public static SparkDlcConfigManager configManager() {
        return configManager;
    }

    public static Text abilityKeyText() {
        return AbilityKeyBridge.keyText();
    }

    private static void layOutGalaxy() {
        try {
            AchievementGalaxy.warmUp();
        } catch (LinkageError exception) {
            // Opening the page then fails too, as it would have without this thread. 之后打开成就页同样会失败，与没有这个线程时一样。
            LOGGER.error("Could not lay out the achievement galaxy", exception);
        }
    }

    /**
     * Wathe keeps the muzzle of every gun-holding player it draws in a static map and forgets one only when that player
     * fires, so a world left behind would stay referenced through its players. The map is cleared on the client thread
     * whenever the client's world changes: a disconnect, a server switch (a proxy's reconfiguration fires no disconnect)
     * or a new dimension, whose players are all new entities.
     * Wathe 把所绘制的每个持枪玩家的枪口位置记在静态表中，只有该玩家开枪时才移除，因此离开的世界会通过其玩家一直被引用。
     * 客户端世界一旦改变就在客户端线程上清空该表：断开连接、切换服务器（代理的重新配置不会触发断开事件）或进入新维度时，
     * 其中的玩家都会是新实体。
     */
    private static void forgetMuzzles(MinecraftClient client) {
        if (client.world != muzzleWorld) {
            muzzleWorld = client.world;
            if (WatheClient.particleMap != null) {
                WatheClient.particleMap.clear();
            }
        }
    }

    private static GetInstinctHighlight.HighlightResult toWatheHighlight(
            FactionInstinctPolicy.InstinctResult result
    ) {
        if (result.skip()) {
            return GetInstinctHighlight.HighlightResult.skip();
        }
        if (result.requiresKeybind()) {
            return GetInstinctHighlight.HighlightResult.withKeybind(result.color(), result.priority());
        }
        return GetInstinctHighlight.HighlightResult.always(result.color(), result.priority());
    }

    private static GetInstinctHighlight.HighlightResult fallbackInstinctHighlight(
            PlayerEntity viewer,
            net.minecraft.entity.Entity target,
            GameWorldComponent gameComponent
    ) {
        if (!(target instanceof PlayerEntity targetPlayer)) {
            return null;
        }
        if (!FactionInstinctRules.hasCustomInstinctViewer(viewer, gameComponent)) {
            return null;
        }
        if (GameFunctions.isPlayerSpectatingOrCreative(targetPlayer)) {
            return GetInstinctHighlight.HighlightResult.skip();
        }
        int color = FactionInstinctRules.displayColor(targetPlayer, gameComponent);
        if (color == -1) {
            return null;
        }
        return GetInstinctHighlight.HighlightResult.withKeybind(color);
    }

    /** Per-connection client state that must not outlive its server (client thread only). / 不得跨服务器保留的单连接客户端状态（仅客户端线程）。 */
    private static void resetRoleKitSessionState() {
        BlackoutSightClient.reset();
        KillerTeamEconomyClientHooks.reset();
        ShadowJesterShowdownMusicController.reset();
        TimedBombMarkerClientStore.clear();
        // A stale snapshot would otherwise drive tablet outlines on the next server.
        // 否则过期快照会在下一个服务器上继续驱动平板描边。
        TabletClientState.reset();
        TabletHeldHud.reset();
    }

    /**
     * Runs a DISCONNECT cleanup on the client thread. Fabric can fire DISCONNECT from the Netty thread
     * ({@code channelInactive}), where closing render resources throws and plain client caches are raced; listeners
     * keep their registration order because the client task queue is FIFO (and runs inline when already on the
     * client thread). A cleanup that only runs after a newer play session has joined is dropped, since that session's
     * JOIN listeners already reset the same state.
     * 在客户端线程上执行 DISCONNECT 清理。Fabric 可能在 Netty 线程（{@code channelInactive}）触发 DISCONNECT，
     * 在那里关闭渲染资源会抛异常，普通客户端缓存也会产生竞态；客户端任务队列先进先出（已在客户端线程时直接执行），
     * 因此各监听保持注册顺序。若清理执行时更新的 play 会话已经加入，则跳过，因为该会话的 JOIN 监听已重置同样的状态。
     */
    public static void runSessionCleanup(MinecraftClient client, ClientPlayNetworkHandler disconnected, Runnable cleanup) {
        client.execute(() -> {
            if (shouldRunSessionCleanup(client.getNetworkHandler(), disconnected)) {
                cleanup.run();
            }
        });
    }

    /** Pure rule: clean up unless a different, newer play handler is already current. 纯规则：除非已有更新的 play 处理器，否则执行清理。 */
    public static boolean shouldRunSessionCleanup(Object currentHandler, Object disconnectedHandler) {
        return currentHandler == null || currentHandler == disconnectedHandler;
    }

    private static void registerFinalMomentHighlight() {
        GetInstinctHighlight.EVENT.register(target -> {
            if (!SparkDlcServerConnection.isConfirmedServer()) {
                return null;
            }
            PlayerEntity viewer = MinecraftClient.getInstance().player;
            if (viewer == null || !(target instanceof PlayerEntity targetPlayer)) {
                return null;
            }
            TraitWorldComponent traitWorld = TraitWorldComponent.KEY.get(viewer.getWorld());
            if (!traitWorld.isFinalMomentActive()) {
                return null;
            }
            GameWorldComponent game = GameWorldComponent.KEY.get(viewer.getWorld());
            if (!game.hasAnyRole(targetPlayer) || !GameFunctions.isPlayerPlayingAndAlive(targetPlayer)) {
                return null;
            }

            // Final Moment reveals every living player by faction color until the round ends.
            // 终局时刻会按阵营颜色高亮所有存活玩家，直到本局结束。
            return GetInstinctHighlight.HighlightResult.always(
                    LastStandFinalMomentService.finalMomentHighlightColorForViewer(
                            viewer,
                            targetPlayer,
                            game,
                            traitWorld.isFinalMomentLooseEnd(targetPlayer.getUuid()),
                            CoronerClientHooks.resolveKillerDisguiseInstinctColor(targetPlayer)
                    ),
                    GetInstinctHighlight.HighlightResult.PRIORITY_HIGH + 1
            );
        });
    }

    private static void registerEntityRenderers() {
        // The projectile renders its synced shuriken item stack, never the upstream generic knife texture.
        // 投射物始终渲染同步的手里剑物品模型，不复用上游通用飞刀贴图。
        EntityRendererRegistry.register(
                SparkDlcEntities.ninjaShuriken(),
                context -> new FlyingItemEntityRenderer<>(context, 1.0F, true)
        );
        // The grappling hook draws its 3D head model and the chain to the owner's hand.
        // 钩爪绘制 3D 钩头模型与连到主人手上的锁链。
        NinjaGrapplingHookEntityRenderer.registerHeadModel();
        EntityRendererRegistry.register(SparkDlcEntities.ninjaGrapplingHook(), NinjaGrapplingHookEntityRenderer::new);
        EntityRendererRegistry.register(HunterEntities.hunterTrap(), HunterTrapEntityRenderer::new);
        // The thrown Shock Device renders its synced item stack like vanilla thrown items.
        // 投出的电击装置与原版投掷物一样渲染其同步的物品。
        EntityRendererRegistry.register(ControlExpertEntities.shockDevice(), FlyingItemEntityRenderer::new);
        // The thrown Holy Flash renders its synced item stack. / 投出的圣光弹渲染其同步的物品。
        EntityRendererRegistry.register(HolyFlashEntities.holyFlash(), FlyingItemEntityRenderer::new);
        // The flying shell draws its 3D model by stack type. / 飞行中的炮弹按物品种类绘制 3D 模型。
        EntityRendererRegistry.register(PotionGunnerEntities.potionShell(), PotionShellEntityRenderer::new);
        SeekerClientModule.registerEntityRenderers();
        // BEAR smoke round (WP0 stub renderer). / BEAR 烟雾榴弹（WP0 桩渲染器）。
        dev.caecorthus.sparkdlc.client.role.civilian.bear.BearClientModule.registerEntityRenderers();
        EntityRendererRegistry.register(SparkDlcEntities.magicianPlayback(), MagicianPlaybackEntityRenderer::new);
        // Raven Form world content: Homing Feather marker and black feather particle. / 化鸦世界内容：归羽标记与黑羽粒子。
        BlackRavenCrowWorldClient.register();
        // 狮身豪's "手"枪: loaded-model predicate, fist model and the spring-fist renderer (lattice + fist).
        // 狮身豪的“手”枪：装填模型谓词、拳头模型与弹簧拳头渲染器（剪刀架 + 拳头）。
        dev.caecorthus.sparkdlc.client.role.jiahao.sphinxhao.SphinxHaoGunClient.register();
    }

    private static void registerGrandWitchCeremonialSwordBgm() {
        AmbienceUtil.registerBackgroundAmbience(new BackgroundAmbience(
                SparkDlcSounds.GRAND_WITCH_CEREMONIAL_SWORD_BGM,
                player -> SparkDlcServerConnection.isConfirmedServer()
                        && WitchWorldComponent.KEY.get(player.getWorld()).hasGrandWitchCeremonialSwordBgm(),
                20
        ));
    }

    /**
     * Connection edges (login INIT, login DISCONNECT, play DISCONNECT; on login INIT and play DISCONNECT only the
     * client-thread part runs, because the trait session listeners already dropped the flag earlier on that edge).
     * These fire on the server-connector or Netty thread. The confirmed flag is volatile and is the point of the early
     * reset, so it is cleared at once; everything else is client-thread state (two of the resets close a screen, which
     * grabs the cursor through GLFW), so it is handed to the client thread. Tasks run in order, so the reset still
     * precedes every play packet of a new session.
     * 连接生命周期节点（登录 INIT、登录 DISCONNECT、play DISCONNECT；在登录 INIT 与 play DISCONNECT 上只执行客户端线程部分，
     * 因为词条会话监听器已在同一节点上更早清除了标记）。这些事件在连接线程或 Netty 线程触发。已确认标记是 volatile 的，
     * 且提前重置正是为了它，因此立即清除；其余都是客户端线程状态（其中两个重置会关闭界面，进而通过 GLFW 抓取光标），因此
     * 交给客户端线程执行。任务按顺序执行，所以重置仍先于新会话的任何 play 数据包。
     */
    private static void resetConnectionState() {
        SparkDlcServerConnection.reset();
        resetConnectionClientState();
    }

    /**
     * The client-thread part of {@link #resetConnectionState}, for the edges whose flag reset already ran.
     * {@link #resetConnectionState} 的客户端线程部分，用于标记已被清除的节点。
     */
    private static void resetConnectionClientState() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.isOnThread()) {
            client.execute(SparkDLCClient::resetClientThreadState);
            return;
        }
        resetClientThreadState();
    }

    private static void resetClientThreadState() {
        resetOwnRoleSessionState();
        releaseClosedWorldPlayers();
    }

    /** Client thread: per-session state of the shared modules; never the confirmed flag. / 客户端线程：共享模块的逐会话状态；绝不包含已确认标记。 */
    private static void resetOwnRoleSessionState() {
        dev.caecorthus.sparkdlc.client.gui.OwnerInventoryPresenter.reset();
        AbilityKeyBridge.reset();
        SecondaryAbilityController.reset();
        KidnapperThrowClientHooks.reset();
        DoubleBarrelMuzzleClient.reset();
        WitchMaidenClientModule.clear();
        TarotDivinationClientState.clear();
        CriminalReasonerClientModule.clear();
        JudgeClientModule.clear();
        ProphetClientModule.clear();
        SeekerClientModule.reset();
        StowawaySensesClient.reset();
        BlindClient.reset();
        SaviorDomainClient.reset();
    }

    /**
     * Connection edges (login, disconnect, join): drops client-side player references that would otherwise keep a
     * closed ClientWorld alive, namely the Magician stand-in copies, the Savior client caches (lightsaber hums and
     * held-stack state, pistol muzzle positions; {@code SaviorClient.releaseClosedWorld}) and Wathe's muzzle-position map
     * ({@code WatheClient.particleMap}, keyed by player and never cleared by Wathe). Wathe only reads and writes that
     * map on the client thread (held-item render, muzzle packet task) and never iterates it, so all of them are cleared
     * there; calls from another thread (login events run on the network thread) are deferred to it.
     * 连接生命周期节点（登录、断开、加入）：丢弃否则会让已关闭 ClientWorld 一直存活的客户端玩家引用，即魔术师替身副本、
     * 救世主客户端缓存（光剑嗡鸣与手持物品状态、手枪枪口位置；{@code SaviorClient.releaseClosedWorld}）与 Wathe 的枪口位置表
     * （{@code WatheClient.particleMap}，以玩家为键且 Wathe 从不清空）。Wathe 只在客户端线程（手持物品渲染、枪口数据包任务）
     * 读写该表且从不遍历它，因此它们都在该线程清理；来自其他线程的调用（登录事件在网络线程触发）会推迟到该线程。
     */
    private static void releaseClosedWorldPlayers() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.isOnThread()) {
            client.execute(SparkDLCClient::releaseClosedWorldPlayers);
            return;
        }
        MagicianPuppetStandIn.reset();
        SaviorClient.releaseClosedWorld();
        // forgetMuzzles clears the same map at the end of the tick the world changed; neither covers the other (a
        // dimension change fires no connection edge, and a JOIN's muzzle packets can run before that tick ends), so
        // both stay.
        // forgetMuzzles 在世界改变的那一 tick 结束时清空同一张表；两者互不覆盖（切换维度不触发连接节点，而 JOIN 之后的枪口
        // 数据包可能在该 tick 结束前执行），因此都保留。
        var muzzles = WatheClient.particleMap;
        if (muzzles != null) {
            muzzles.clear();
        }
    }
}
