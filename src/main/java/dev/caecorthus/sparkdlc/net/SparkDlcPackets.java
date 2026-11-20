package dev.caecorthus.sparkdlc.net;

import dev.caecorthus.sparkdlc.economy.SyncKillerTeamEconomyS2CPacket;
import dev.caecorthus.sparkdlc.item.m67.M67Packets;
import dev.caecorthus.sparkdlc.role.civilian.accountant.AccountantPassbookService;
import dev.caecorthus.sparkdlc.role.civilian.bear.net.BearNetworking;
import dev.caecorthus.sparkdlc.role.civilian.bear.smoke.net.SmokeNetworking;
import dev.caecorthus.sparkdlc.role.civilian.blind.kit.BlindAttuneService;
import dev.caecorthus.sparkdlc.role.civilian.blind.net.BlindPulseS2CPayload;
import dev.caecorthus.sparkdlc.role.civilian.blind.net.UseBlindAttuneC2SPayload;
import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerMorphC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerService;
import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterSniffC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterSniffService;
import dev.caecorthus.sparkdlc.role.civilian.detective.DetectivePackets;
import dev.caecorthus.sparkdlc.role.civilian.emma.EmmaSkillService;
import dev.caecorthus.sparkdlc.role.civilian.fisher.swordfish.SwordfishStabC2SPayload;
import dev.caecorthus.sparkdlc.role.civilian.fisher.swordfish.SwordfishStabService;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.UseGuardianAngelSkillC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.judge.JudgePackets;
import dev.caecorthus.sparkdlc.role.civilian.junker.JunkerService;
import dev.caecorthus.sparkdlc.role.civilian.knight.duel.KnightDuelNetworking;
import dev.caecorthus.sparkdlc.role.civilian.medium.MediumPackets;
import dev.caecorthus.sparkdlc.role.civilian.noisemaker.NoisemakerGlowC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.noisemaker.NoisemakerGlowService;
import dev.caecorthus.sparkdlc.role.civilian.orthopedist.OrthopedistSkillService;
import dev.caecorthus.sparkdlc.role.civilian.orthopedist.UseOrthopedistSkillC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.professor.ProfessorRemoteFeedC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.professor.ProfessorSerumService;
import dev.caecorthus.sparkdlc.role.civilian.prophet.ProphetPackets;
import dev.caecorthus.sparkdlc.role.civilian.reporter.ReporterCommunicationC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.reporter.ReporterCommunicationService;
import dev.caecorthus.sparkdlc.role.civilian.seeker.net.SeekerNetworking;
import dev.caecorthus.sparkdlc.role.civilian.sherry.net.SherryPackets;
import dev.caecorthus.sparkdlc.role.civilian.spiritualist.SpiritPossessionPackets;
import dev.caecorthus.sparkdlc.role.civilian.tarotreader.TarotDivinationPackets;
import dev.caecorthus.sparkdlc.role.civilian.timekeeper.TimekeeperWatchModeC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.timekeeper.TimekeeperWatchService;
import dev.caecorthus.sparkdlc.role.civilian.undercover.UndercoverMaskOffC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.undercover.UndercoverService;
import dev.caecorthus.sparkdlc.role.civilian.usec.net.UsecNetworking;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.UseVendettaKnifeC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaKnifeService;
import dev.caecorthus.sparkdlc.role.killer.bandit.BanditRevolverActionC2SPayload;
import dev.caecorthus.sparkdlc.role.killer.bandit.BanditRevolverShootC2SPayload;
import dev.caecorthus.sparkdlc.role.killer.blackraven.BlackRavenPackets;
import dev.caecorthus.sparkdlc.role.killer.bomber.TimedBombMarkersPayload;
import dev.caecorthus.sparkdlc.role.killer.bomber.drone.DronePackets;
import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorForcedShotS2CPacket;
import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorSkillService;
import dev.caecorthus.sparkdlc.role.killer.dominator.UseDominatorSkillC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.hiro.returning.HiroReturnService;
import dev.caecorthus.sparkdlc.role.killer.hiro.returning.UseHiroSkillC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.hunter.net.HunterNetworking;
import dev.caecorthus.sparkdlc.role.killer.kidnapper.KidnapperThrowService;
import dev.caecorthus.sparkdlc.role.killer.magician.MagicianPackets;
import dev.caecorthus.sparkdlc.role.killer.natsume.NatsumePackets;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerPackets;
import dev.caecorthus.sparkdlc.role.killer.phantom.PhantomBackpackInvisibilityC2SPacket;
import dev.caecorthus.sparkdlc.role.killer.phantom.PhantomBackpackService;
import dev.caecorthus.sparkdlc.role.killer.saboteur.SaboteurNetworking;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterPackets;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.FocusedFootstepsRequestService;
import dev.caecorthus.sparkdlc.role.neutral.criminalreasoner.CriminalReasonerPackets;
import dev.caecorthus.sparkdlc.role.neutral.murderouswitch.deathray.MurderousWitchDeathRayService;
import dev.caecorthus.sparkdlc.role.neutral.shadowjester.SyncShadowJesterShowdownMusicS2CPacket;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadFireC2SPacket;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadService;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSuperCurseC2SPacket;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSuperCurseService;
import dev.caecorthus.sparkdlc.role.witch.curser.CurserFeatureService;
import dev.caecorthus.sparkdlc.role.witch.curser.UseCurserAbilityC2SPacket;
import dev.caecorthus.sparkdlc.role.witch.potiongunner.launcher.PotionLauncherFireService;
import dev.caecorthus.sparkdlc.role.witch.riftwalker.net.RiftwalkerNetworking;
import dev.caecorthus.sparkdlc.sabotage.net.SabotagePackets;
import dev.caecorthus.sparkdlc.tablet.net.TabletPackets;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * The one place where SparkDLC registers its play payload types and their server receivers. {@link #register()} is a
 * list of features: a feature with several payloads has a registrar of its own in its package ({@code *Packets} or
 * {@code *Networking}), a feature with one or two is registered here, under its name. Payload ids and codecs belong
 * to the payload classes and are not decided here.
 *
 * <p><b>Order.</b> Nothing below depends on the order of its lines: Fabric keeps payload types and global receivers
 * in maps keyed by the payload id, and a second registration of an id fails at start-up instead of winning. What
 * does depend on order is an event listener, and none is registered from here: a service that registers listeners
 * together with its payloads (the cross sword, the Savior's three services, the Hunter's aim claims) is called from
 * its step in {@link dev.caecorthus.sparkdlc.SparkDLC}. Until stage 3 this was three hubs, one per former mod.</p>
 *
 * <p>The version handshake has its own class ({@link dev.caecorthus.sparkdlc.net.version.SparkDlcVersionHandshake}),
 * and client receivers are registered by {@code client.net.SparkDlcClientPackets} and the client feature modules.</p>
 *
 * SparkDLC 注册 play 数据包类型及其服务端接收器的唯一位置。{@link #register()} 是一份功能清单：数据包较多的功能在自己的包里有
 * 注册类（{@code *Packets} 或 {@code *Networking}），只有一两个数据包的功能在此按功能名注册。数据包 id 与编解码器属于各数据包类，
 * 不在此决定。
 *
 * <p><b>顺序。</b>下面的内容都不依赖行的先后：Fabric 以数据包 id 为键把数据包类型和全局接收器保存在映射里，同一 id 的第二次注册会
 * 在启动时失败，而不是覆盖前者。真正依赖顺序的是事件监听器，而这里不注册任何监听器：连同数据包一起注册监听器的服务（十字剑、
 * 救世主的三个服务、猎人的瞄准声明）由 {@link dev.caecorthus.sparkdlc.SparkDLC} 中对应的步骤调用。第三阶段之前这里是三个注册中心，
 * 每个旧模组一个。</p>
 *
 * <p>版本握手有自己的类（{@link dev.caecorthus.sparkdlc.net.version.SparkDlcVersionHandshake}）；客户端接收器由
 * {@code client.net.SparkDlcClientPackets} 与各客户端功能模块注册。</p>
 */
public final class SparkDlcPackets {
    private static boolean registered;

    private SparkDlcPackets() {
    }

    /** Registers every payload type and server receiver of the mod, once. / 注册本模组的全部数据包类型与服务端接收器，仅一次。 */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        registerItemsAndSystems();
        registerCivilianRoles();
        registerKillerRoles();
        registerNeutralRoles();
        registerWitchFactionRoles();
        registerSpecialRoles();
    }

    /** Items and systems that belong to no single role. / 不属于任何单个职业的物品与系统。 */
    private static void registerItemsAndSystems() {
        M67Packets.initialize();
        TabletPackets.register();
        SabotagePackets.register();
        // Killer team economy: the shared balance, sent to the players who may see it. / 杀手团队经济：共享余额，只发给可见的玩家。
        PayloadTypeRegistry.playS2C().register(SyncKillerTeamEconomyS2CPacket.ID, SyncKillerTeamEconomyS2CPacket.CODEC);
    }

    /** Civilian roles. / 平民职业。 */
    private static void registerCivilianRoles() {
        DetectivePackets.register();
        JudgePackets.register();
        KnightDuelNetworking.registerServer();
        MediumPackets.register();
        ProphetPackets.register();
        SeekerNetworking.register();
        SherryPackets.register();
        SpiritPossessionPackets.initialize();
        TarotDivinationPackets.register();
        // Noisemaker. / 大嗓门。
        PayloadTypeRegistry.playC2S().register(NoisemakerGlowC2SPacket.ID, NoisemakerGlowC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(NoisemakerGlowC2SPacket.ID, (payload, context) ->
                NoisemakerGlowService.tryUseBackpackGlow(context.player(), payload.targetPlayer())
        );
        // Coroner. / 验尸官。
        PayloadTypeRegistry.playC2S().register(CoronerMorphC2SPacket.ID, CoronerMorphC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CoronerMorphC2SPacket.ID, (payload, context) ->
                CoronerService.selectDisguise(context.player(), payload.targetPlayer())
        );
        // Professor. / 教授。
        PayloadTypeRegistry.playC2S().register(ProfessorRemoteFeedC2SPacket.ID, ProfessorRemoteFeedC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ProfessorRemoteFeedC2SPacket.ID, (payload, context) ->
                ProfessorSerumService.tryRemoteFeed(context.player(), payload.targetPlayer(), payload.serumType())
        );
        // Reporter. / 记者。
        PayloadTypeRegistry.playC2S().register(ReporterCommunicationC2SPacket.ID, ReporterCommunicationC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ReporterCommunicationC2SPacket.ID, (payload, context) ->
                ReporterCommunicationService.handle(payload, context.player())
        );
        // Demon Hunter. / 猎魔人。
        PayloadTypeRegistry.playC2S().register(DemonHunterSniffC2SPacket.ID, DemonHunterSniffC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DemonHunterSniffC2SPacket.ID,
                (payload, context) -> DemonHunterSniffService.trySniff(context.player()));
        // Undercover. / 卧底。
        PayloadTypeRegistry.playC2S().register(UndercoverMaskOffC2SPacket.ID, UndercoverMaskOffC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UndercoverMaskOffC2SPacket.ID,
                (payload, context) -> UndercoverService.takeMaskOff(context.player()));
        // Timekeeper. / 计时员。
        PayloadTypeRegistry.playC2S().register(TimekeeperWatchModeC2SPacket.ID, TimekeeperWatchModeC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TimekeeperWatchModeC2SPacket.ID,
                (payload, context) -> TimekeeperWatchService.switchMode(context.player(), payload.modeOrdinal()));
        // 樱羽艾玛 (Emma). / 樱羽艾玛。
        PayloadTypeRegistry.playC2S().register(EmmaFactorC2SPacket.ID, EmmaFactorC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EmmaFactorC2SPacket.ID,
                (payload, context) -> EmmaSkillService.use(context.player(), payload.targetId()));
        // Orthopedist. / 骨科大夫。
        PayloadTypeRegistry.playC2S().register(
                UseOrthopedistSkillC2SPacket.ID,
                UseOrthopedistSkillC2SPacket.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(UseOrthopedistSkillC2SPacket.ID,
                (payload, context) -> OrthopedistSkillService.use(context.player()));
        // Guardian Angel. / 守护天使。
        PayloadTypeRegistry.playC2S().register(
                UseGuardianAngelSkillC2SPacket.ID,
                UseGuardianAngelSkillC2SPacket.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(UseGuardianAngelSkillC2SPacket.ID,
                (payload, context) -> GuardianAngelFeatureService.use(context.player()));
        // Vendetta. / 仇杀客。
        PayloadTypeRegistry.playC2S().register(
                UseVendettaKnifeC2SPacket.ID,
                UseVendettaKnifeC2SPacket.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(UseVendettaKnifeC2SPacket.ID,
                (payload, context) -> VendettaKnifeService.use(
                        context.player(), payload.targetEntityId()));
        // Fisher: the Swordfish stab. / 钓鱼佬：剑鱼刺击。
        PayloadTypeRegistry.playC2S().register(
                SwordfishStabC2SPayload.ID,
                SwordfishStabC2SPayload.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(SwordfishStabC2SPayload.ID,
                (payload, context) -> SwordfishStabService.use(context.player(), payload.targetEntityId()));
        // Blind: the Attune request (stun, Seeker and Fear guarded) and the owner-only perception pulse.
        // 盲人：凝神请求（受眩晕、搜寻者与恐惧拦截）与只发给本人的感知脉冲。
        PayloadTypeRegistry.playC2S().register(
                UseBlindAttuneC2SPayload.ID,
                UseBlindAttuneC2SPayload.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(UseBlindAttuneC2SPayload.ID,
                (payload, context) -> BlindAttuneService.tryUse(context.player()));
        PayloadTypeRegistry.playS2C().register(
                BlindPulseS2CPayload.ID,
                BlindPulseS2CPayload.CODEC
        );
        // Junker. / 拾荒者。
        PayloadTypeRegistry.playC2S().register(JunkerSearchC2SPacket.ID, JunkerSearchC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(JunkerSearchC2SPacket.ID,
                (payload, context) -> JunkerService.handleSearchRequest(context.player(), payload.bodyUuid()));
        // Accountant passbook left-click withdraw: a held-item action (not a role skill), on the stun, Seeker and Rift
        // lists like the potion launcher fire. / 会计存折左键取钱：手持物品操作（不是职业技能），与炮筒开火一样列入眩晕、
        // 搜寻者与裂隙名单。
        PayloadTypeRegistry.playC2S().register(AccountantWithdrawC2SPacket.ID, AccountantWithdrawC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AccountantWithdrawC2SPacket.ID,
                (payload, context) -> AccountantPassbookService.handleWithdrawRequest(context.player()));
        // BEAR smoke S2C payload types, role-owned like USEC; the receivers are client-side.
        // BEAR 烟雾 S2C 数据包类型，与 USEC 一样在自有类中注册；接收器在客户端。
        SmokeNetworking.register();
        // BEAR payload types (work packages append there), before USEC, which stays last.
        // BEAR 数据包类型（各工作包在其中追加），位于 USEC 之前，USEC 保持最后。
        BearNetworking.register();
        // USEC payload types register in their own role-owned class, like the Seeker; the owning services register the
        // receivers from SparkWitchEvents, which runs after this. / USEC 数据包类型与搜寻者一样在自有类中注册；
        // 接收器由各自的服务在之后运行的 SparkWitchEvents 中注册。
        UsecNetworking.register();
    }

    /** Killer roles. / 杀手职业。 */
    private static void registerKillerRoles() {
        BlackRavenPackets.register();
        DronePackets.initialize();
        // Hunter shotgun aim claim, also role-owned. / 猎人猎枪瞄准声明，同样在自有类中注册。
        HunterNetworking.register();
        MagicianPackets.register();
        NatsumePackets.register();
        PartygoerPackets.register();
        SaboteurNetworking.register();
        SpecterPackets.register();
        // Phantom. / 幻影。
        PayloadTypeRegistry.playC2S().register(PhantomBackpackInvisibilityC2SPacket.ID, PhantomBackpackInvisibilityC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PhantomBackpackInvisibilityC2SPacket.ID, (payload, context) ->
                PhantomBackpackService.tryUseBackpackInvisibility(context.player(), payload.targetPlayer())
        );
        // Serial Killer. / 连环杀手。
        PayloadTypeRegistry.playC2S().register(SerialPistolShootC2SPayload.ID, SerialPistolShootC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SerialPistolShootC2SPayload.ID,
                SerialPistolShootC2SPayload::receive);
        // Bandit. / 强盗。
        PayloadTypeRegistry.playC2S().register(BanditRevolverShootC2SPayload.ID, BanditRevolverShootC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BanditRevolverShootC2SPayload.ID,
                BanditRevolverShootC2SPayload::receive);
        PayloadTypeRegistry.playC2S().register(BanditRevolverActionC2SPayload.ID, BanditRevolverActionC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BanditRevolverActionC2SPayload.ID,
                BanditRevolverActionC2SPayload::receive);
        // Planted timed-bomb traps, sent to trap viewers only (TimedBombMarkerSync). / 已埋设的定时炸弹陷阱，只发给陷阱观察者。
        PayloadTypeRegistry.playS2C().register(TimedBombMarkersPayload.ID, TimedBombMarkersPayload.CODEC);
        // Dominator. / 支配者。
        PayloadTypeRegistry.playC2S().register(
                UseDominatorSkillC2SPacket.ID,
                UseDominatorSkillC2SPacket.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(UseDominatorSkillC2SPacket.ID,
                (payload, context) -> DominatorSkillService.use(context.player()));
        PayloadTypeRegistry.playS2C().register(
                DominatorForcedShotS2CPacket.ID,
                DominatorForcedShotS2CPacket.CODEC
        );
        // Kidnapper. / 绑架者。
        PayloadTypeRegistry.playC2S().register(
                ThrowKidnapperBodyC2SPacket.ID,
                ThrowKidnapperBodyC2SPacket.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(ThrowKidnapperBodyC2SPacket.ID,
                (payload, context) -> KidnapperThrowService.throwCarriedBody(context.player()));
        // 二阶堂希罗 (Nikaido Hiro). / 二阶堂希罗。
        PayloadTypeRegistry.playC2S().register(UseHiroSkillC2SPacket.ID, UseHiroSkillC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UseHiroSkillC2SPacket.ID,
                (payload, context) -> HiroReturnService.use(context.player()));
        // Witch Maiden: Focused Footsteps. / 巫女：聚焦步伐。
        PayloadTypeRegistry.playC2S().register(UseWitchSkillC2SPacket.ID, UseWitchSkillC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UseWitchSkillC2SPacket.ID,
                (payload, context) -> FocusedFootstepsRequestService.use(
                        context.player(), payload.targetUuid()));
        PayloadTypeRegistry.playS2C().register(
                FocusedFootstepsUseResultS2CPacket.ID,
                FocusedFootstepsUseResultS2CPacket.CODEC
        );
    }

    /** Neutral roles. / 中立职业。 */
    private static void registerNeutralRoles() {
        CriminalReasonerPackets.register();
        // Vulture. / 秃鹫。
        PayloadTypeRegistry.playC2S().register(VultureSuperCurseC2SPacket.ID, VultureSuperCurseC2SPacket.CODEC);
        // Runs on the server thread; the service re-checks round, role, liveness, skill locks and cooldown.
        // 在服务端主线程执行；服务层重新校验回合、职业、存活、技能封锁与冷却。
        ServerPlayNetworking.registerGlobalReceiver(VultureSuperCurseC2SPacket.ID,
                (payload, context) -> VultureSuperCurseService.tryCurse(context.player()));
        // Taotie. / 饕餮。
        PayloadTypeRegistry.playC2S().register(TaotieHeadFireC2SPacket.ID, TaotieHeadFireC2SPacket.CODEC);
        // Empty request: the service re-validates role, round, liveness, locks, cooldown and the stomach.
        // 空请求：服务端重新校验身份、对局、存活、各类锁定、冷却与体内玩家。
        ServerPlayNetworking.registerGlobalReceiver(TaotieHeadFireC2SPacket.ID,
                (payload, context) -> TaotieHeadService.tryFire(context.player()));
        // Shadow Jester. / 影子小丑。
        PayloadTypeRegistry.playS2C().register(
                SyncShadowJesterShowdownMusicS2CPacket.ID,
                SyncShadowJesterShowdownMusicS2CPacket.CODEC
        );
        // Murderous Witch. / 杀意魔女。
        PayloadTypeRegistry.playC2S().register(FireDeathRayC2SPacket.ID, FireDeathRayC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FireDeathRayC2SPacket.ID,
                (payload, context) -> MurderousWitchDeathRayService.fire(context.player(), payload));
    }

    /** Roles of the Witch faction. / 魔女阵营职业。 */
    private static void registerWitchFactionRoles() {
        // Riftwalker payloads register in their own role-owned class, like the Seeker. / 隙行者数据包与搜寻者一样在自有类中注册。
        RiftwalkerNetworking.register();
        // Curser. / 诅咒者。
        PayloadTypeRegistry.playC2S().register(UseCurserAbilityC2SPacket.ID, UseCurserAbilityC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UseCurserAbilityC2SPacket.ID,
                (payload, context) -> CurserFeatureService.use(context.player()));
        // Potion Gunner. / 药炮手。
        PayloadTypeRegistry.playC2S().register(FirePotionLauncherC2SPacket.ID, FirePotionLauncherC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FirePotionLauncherC2SPacket.ID,
                (payload, context) -> PotionLauncherFireService.fire(context.player(), payload));
    }

    /** Special roles. / 特殊职业。 */
    private static void registerSpecialRoles() {
        // Wraith: the role announcement. / 冤魂：职业公告。
        PayloadTypeRegistry.playS2C().register(
                WraithRoleAnnouncementS2CPacket.ID,
                WraithRoleAnnouncementS2CPacket.CODEC
        );
    }
}
