package dev.caecorthus.sparkdlc.event;

import dev.caecorthus.sparkdlc.role.civilian.accountant.AccountantFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.athlete.AthleteFeatureService;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.wither.MiniWitherFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.usec.UsecFeatureService;
import dev.caecorthus.sparkdlc.role.witch.potiongunner.PotionGunnerFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.emma.EmmaGunService;
import dev.caecorthus.sparkdlc.role.civilian.emma.EmmaLifecycle;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.component.PerfumerPlayerComponent;
import dev.caecorthus.sparkdlc.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.component.WitchWorldComponent;
import dev.caecorthus.sparkdlc.economy.WitchEconomyService;
import dev.caecorthus.sparkdlc.item.defense.DefenseVialRuntime;
import dev.caecorthus.sparkdlc.item.ceremonialsword.CeremonialSwordCombatService;
import dev.caecorthus.sparkdlc.item.ceremonialsword.CeremonialSwordDashService;
import dev.caecorthus.sparkdlc.item.firepoker.FirePokerCombatService;
import dev.caecorthus.sparkdlc.item.firepoker.FirePokerFallAttributionService;
import dev.caecorthus.sparkdlc.item.sparekey.SpareKeyAttendantKit;
import dev.caecorthus.sparkdlc.item.sparekey.SpareKeyDoors;
import dev.caecorthus.sparkdlc.item.tofana.TofanaProtectionService;
import dev.caecorthus.sparkdlc.role.civilian.apprentice.ApprenticeFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.apprentice.abilities.mightyforce.MightyForceCombatService;
import dev.caecorthus.sparkdlc.role.civilian.blind.BlindFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.ControlExpertFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.discoverer.DiscovererNightVisionService;
import dev.caecorthus.sparkdlc.role.civilian.discoverer.DiscovererService;
import dev.caecorthus.sparkdlc.role.civilian.discoverer.DiscovererShopService;
import dev.caecorthus.sparkdlc.role.civilian.fisher.FisherFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.knight.KnightEconomyService;
import dev.caecorthus.sparkdlc.role.civilian.knight.KnightFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.knight.KnightKillService;
import dev.caecorthus.sparkdlc.role.civilian.knight.duel.KnightDuelService;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorBlackoutService;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorDomainService;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorLoadoutService;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorObservationService;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorSanityService;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.judge.JudgeRuntime;
import dev.caecorthus.sparkdlc.role.civilian.junker.JunkerService;
import dev.caecorthus.sparkdlc.role.civilian.orthopedist.OrthopedistEconomyService;
import dev.caecorthus.sparkdlc.role.civilian.orthopedist.OrthopedistShopService;
import dev.caecorthus.sparkdlc.role.civilian.orthopedist.OrthopedistSkillService;
import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorForcedShotLifecycle;
import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorForcedShotService;
import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorPsychoGazeService;
import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorSkillService;
import dev.caecorthus.sparkdlc.item.rope.RopeStrangleService;
import dev.caecorthus.sparkdlc.role.killer.hiro.returning.HiroReturnService;
import dev.caecorthus.sparkdlc.role.killer.hiro.shop.HiroShopService;
import dev.caecorthus.sparkdlc.role.killer.hiro.witchification.HiroWitchificationService;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterCowardiceComponent;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterFearComponent;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterFeatureService;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterOtherworldComponent;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterOtherworldService;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterPeachwoodService;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterReplayService;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterRules;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterSecondSkillComponent;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterShopService;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterSkillService;
import dev.caecorthus.sparkdlc.role.neutral.ferryman.FerrymanFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelRules;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaDisconnectService;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaInteractionService;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaReplayService;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaTerminalService;
import dev.caecorthus.sparkdlc.role.civilian.perfumer.PerfumerEconomyService;
import dev.caecorthus.sparkdlc.role.civilian.perfumer.PerfumerFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.perfumer.PerfumerRuntime;
import dev.caecorthus.sparkdlc.role.civilian.recruit.RecruitRuntime;
import dev.caecorthus.sparkdlc.role.civilian.hanna.HannaFeatureService;
import dev.caecorthus.sparkdlc.role.neutral.koko.KokoFeatureService;
import dev.caecorthus.sparkdlc.role.jiahao.sphinxhao.net.SphinxHaoPayloads;
import dev.caecorthus.sparkdlc.role.neutral.criminalreasoner.CriminalReasonerRuntime;
import dev.caecorthus.sparkdlc.role.civilian.noa.NoaFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.perfumer.PerfumerShopService;
import dev.caecorthus.sparkdlc.role.witch.grandwitch.GrandWitchActiveSkillService;
import dev.caecorthus.sparkdlc.role.witch.grandwitch.GrandWitchFeatureService;
import dev.caecorthus.sparkdlc.role.witch.WitchFactionFeatureService;
import dev.caecorthus.sparkdlc.role.witch.abysslistener.AbyssListenerFeatureService;
import dev.caecorthus.sparkdlc.role.witch.bewitched.BewitchedFeatureService;
import dev.caecorthus.sparkdlc.role.witch.bewitched.BewitchedPromotionService;
import dev.caecorthus.sparkdlc.skill.mana.WitchManaService;
import dev.caecorthus.sparkdlc.role.neutral.murderouswitch.feature.MurderousWitchFeatureService;
import dev.caecorthus.sparkdlc.role.neutral.fiend.FiendFeatureService;
import dev.caecorthus.sparkdlc.role.jiahao.insider.InsiderEconomyService;
import dev.caecorthus.sparkdlc.role.jiahao.insider.InsiderEquipmentService;
import dev.caecorthus.sparkdlc.role.jiahao.sniper.SniperFeatureService;
import dev.caecorthus.sparkdlc.role.neutral.stowaway.StowawayKillRewardService;
import dev.caecorthus.sparkdlc.role.neutral.stowaway.StowawayLoadoutService;
import dev.caecorthus.sparkdlc.role.neutral.stowaway.StowawaySettlement;
import dev.caecorthus.sparkdlc.role.neutral.cat.SchrodingersCatService;
import dev.caecorthus.sparkdlc.role.witch.riftwalker.RiftwalkerFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.piggod.PigGodEconomyService;
import dev.caecorthus.sparkdlc.role.civilian.piggod.PigGodChaseRuntime;
import dev.caecorthus.sparkdlc.role.civilian.piggod.PigGodFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.prophet.ProphetDeathLedger;
import dev.caecorthus.sparkdlc.role.civilian.prophet.ProphetProphecyService;
import dev.caecorthus.sparkdlc.role.civilian.prophet.ProphetEconomyService;
import dev.caecorthus.sparkdlc.role.civilian.prophet.ProphetRuntime;
import dev.caecorthus.sparkdlc.role.civilian.saint.SaintEconomyService;
import dev.caecorthus.sparkdlc.role.civilian.saint.SaintFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.tarotreader.TarotReaderFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.windspirit.WindSpiritFeatureService;
import dev.caecorthus.sparkdlc.role.killer.bellringer.BellRingerFeatureService;
import dev.caecorthus.sparkdlc.role.killer.blackraven.BlackRavenFeatureService;
import dev.caecorthus.sparkdlc.role.killer.blackraven.crow.BlackRavenCrowFeatureService;
import dev.caecorthus.sparkdlc.role.killer.blackraven.crow.BlackRavenCrowWorldFeatureService;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterFeatureService;
import dev.caecorthus.sparkdlc.role.killer.kidnapper.KidnapperControlGuards;
import dev.caecorthus.sparkdlc.role.killer.kidnapper.KidnapperDragLifecycle;
import dev.caecorthus.sparkdlc.role.killer.kidnapper.KidnapperKnockoutService;
import dev.caecorthus.sparkdlc.role.killer.ninja.NinjaFeatureService;
import dev.caecorthus.sparkdlc.role.killer.saboteur.SaboteurFeatureService;
import dev.caecorthus.sparkdlc.role.killer.timestealer.TimeStealerFeatureService;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.FocusedFootstepsRuntime;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.PoisonApplePlateService;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.WitchMaidenShopService;
import dev.caecorthus.sparkdlc.role.killer.magician.MagicianPlaybackManager;
import dev.caecorthus.sparkdlc.role.killer.magician.MagicianPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.magician.MagicianReplayEvents;
import dev.caecorthus.sparkdlc.role.killer.magician.MagicianPuppetAttackHandlers;
import dev.caecorthus.sparkdlc.role.killer.magician.MagicianDecoyBodies;
import dev.caecorthus.sparkdlc.role.killer.magician.MagicianRoster;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithStateService;
import dev.caecorthus.sparkdlc.role.special.wraith.conversion.WraithConversion;
import dev.caecorthus.sparkdlc.role.special.wraith.runtime.WraithLifecycle;
import dev.caecorthus.sparkdlc.skill.WitchSkillAssignmentService;
import dev.doctor4t.wathe.api.event.GameEvents;
import dev.doctor4t.wathe.api.event.KillPlayer;
import dev.doctor4t.wathe.api.event.ResetPlayer;
import dev.doctor4t.wathe.api.event.RoleAssigned;
import dev.doctor4t.wathe.api.event.TaskComplete;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class RoleEvents {
    private static boolean registered;

    private RoleEvents() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;

        JudgeRuntime.register();
        CeremonialSwordCombatService.register();
        CeremonialSwordDashService.register();
        GrandWitchFeatureService.register();
        EmmaLifecycle.register();
        EmmaGunService.register();
        MightyForceCombatService.register();
        FirePokerCombatService.register();
        TofanaProtectionService.register();
        WitchFactionFeatureService.register();
        MurderousWitchFeatureService.register();
        PigGodFeatureService.register();
        PigGodEconomyService.register();
        ProphetRuntime.register();
        ProphetDeathLedger.register();
        ProphetProphecyService.register();
        ProphetEconomyService.register();
        SaintFeatureService.register();
        PerfumerShopService.register();
        PerfumerFeatureService.register();
        PerfumerRuntime.register();
        PerfumerEconomyService.register();
        RecruitRuntime.register();
        AthleteFeatureService.register();
        NoaFeatureService.register();
        NinjaFeatureService.register();
        HunterFeatureService.register();
        OrthopedistSkillService.registerReplayFormatter();
        // The Orthopedist's economy and Exoskeleton shop (owner 2026-11-28): money visibility, then the shop entry.
        // 骨科大夫的经济与外骨骼商店（所有者 2026-11-28）：先是金币可见性，然后是商店条目。
        OrthopedistEconomyService.register();
        OrthopedistShopService.register();
        GuardianAngelFeatureService.register();
        VendettaInteractionService.register();
        VendettaDisconnectService.register();
        VendettaReplayService.register();
        VendettaTerminalService.register();
        KidnapperKnockoutService.register();
        KidnapperDragLifecycle.register();
        // Knockout Drug attack / use lock, server-enforced (both sides). / 迷药的攻击与使用锁，由服务端强制（两端）。
        KidnapperControlGuards.register();
        TarotReaderFeatureService.register();
        BlackRavenFeatureService.register();
        BlackRavenCrowFeatureService.register();
        BlackRavenCrowWorldFeatureService.register();
        BellRingerFeatureService.register();
        TimeStealerFeatureService.register();
        WindSpiritFeatureService.register();
        SaboteurFeatureService.register();
        FocusedFootstepsRuntime.register();
        WitchMaidenShopService.register();
        MiniWitherFeatureService.register();
        WraithLifecycle.register();
        ControlExpertFeatureService.register();
        KnightEconomyService.register();
        KnightFeatureService.register();
        KnightKillService.register();
        KnightDuelService.register();
        DominatorPsychoGazeService.register();
        DominatorForcedShotLifecycle.register();
        SpecterFeatureService.register();
        SpecterSkillService.register();
        SpecterPeachwoodService.register();
        SpecterOtherworldService.register();
        SpecterShopService.register();
        SpecterReplayService.register();
        // The Charon (PR #202): one hub line right after the Specter's services, following role registration order
        // (Specter, Charon, Insider) and leaving the Seeker, Angler, Fiend, Insider run below contiguous.
        // 卡戎（PR #202）：紧随幽魂各服务之后的一行注册中心，与职业注册顺序（幽魂、卡戎、内应）一致，并保持下方搜寻者、钓鱼佬、
        // 魔人、内应这一段连续。
        FerrymanFeatureService.register();
        // 二阶堂希罗 (2026-11-11), right after the Charon's hub line (role registration order): shop, 死亡回归, 魔女化 and the Rope strangle; each service registers its own
        // listeners (role assignment, reset), so this hub needs no further lines for the role.
        // 二阶堂希罗（2026-11-11），紧随卡戎的注册行（与职业注册顺序一致）：商店、死亡回归、魔女化与绳索勒杀；各服务自行注册监听（职业分配、重置），本注册中心无需为该职业
        // 再添加其他行。
        HiroShopService.register();
        HiroReturnService.register();
        HiroWitchificationService.register();
        RopeStrangleService.register();
        // 夏目安安 (PR #194): runtime listeners, then its replay formatter (the fork kept it in a shared
        // SparkWitchReplayFormatters class that main does not have). 夏目安安（PR #194）：运行期监听器，然后是回放格式化器
        // （fork 中位于 main 没有的共享 SparkWitchReplayFormatters 类）。
        dev.caecorthus.sparkdlc.role.killer.natsume.NatsumeService.register();
        dev.caecorthus.sparkdlc.role.killer.natsume.NatsumeReplay.register();
        // Hypnosis item and skill lock (both sides). / 催眠的道具与技能锁（两端）。
        dev.caecorthus.sparkdlc.role.killer.natsume.NatsumeHypnosisGuards.register();
        SaviorLoadoutService.register();
        SaviorSanityService.register();
        SaviorObservationService.register();
        SaviorBlackoutService.register();
        SeekerFeatureService.register();
        FisherFeatureService.register();
        FiendFeatureService.register();
        InsiderEquipmentService.register();
        InsiderEconomyService.register();
        BlindFeatureService.register();
        AbyssListenerFeatureService.register();
        PotionGunnerFeatureService.register();
        RiftwalkerFeatureService.register();
        BewitchedFeatureService.register();
        ApprenticeFeatureService.register();
        StowawayLoadoutService.register();
        StowawayKillRewardService.register();
        StowawaySettlement.register();
        SchrodingersCatService.register();
        JunkerService.register();
        // BEAR smoke (spike): round-edge clears, expiry and player resyncs. / BEAR 烟雾（原型）：回合节点清除、过期与玩家重新同步。
        dev.caecorthus.sparkdlc.role.civilian.bear.smoke.SmokeCloudLifecycle.register();
        MagicianPlaybackManager.init();
        MagicianReplayEvents.register();
        MagicianPuppetAttackHandlers.register();
        RoleAssigned.EVENT.register((player, role) -> {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                DominatorForcedShotService.onRoleAssigned(serverPlayer);
                dev.caecorthus.sparkdlc.role.civilian.medium.MediumPlayerComponent.KEY.get(serverPlayer)
                        .assignForRole(role);
                dev.caecorthus.sparkdlc.role.civilian.sherry.SherryComponent.KEY.get(serverPlayer)
                        .assignForRole(role);
                SpecterPlayerComponent.KEY.get(serverPlayer).assignForRole(role);
                SpecterFearComponent.KEY.get(serverPlayer).clear();
                SpecterSecondSkillComponent.KEY.get(serverPlayer).assignForRole(role);
                SpecterCowardiceComponent.KEY.get(serverPlayer).clear();
                if (SpecterRules.isSpecter(role)) {
                    // Cowardice syncs only to its owner and Specters: a new Specter needs every current flag now.
                    // 怯懦只同步给本人与幽魂：新的幽魂需要立即收到当前所有标记。
                    SpecterCowardiceComponent.resyncTo(serverPlayer);
                }
                PerfumerPlayerComponent.KEY.get(serverPlayer).clear();
                if (role != null && dev.caecorthus.sparkdlc.SparkDlcRoles.MAGICIAN_ID.equals(role.identifier())) {
                    // One sync with the final state instead of one per write. / 只用最终状态同步一次，而不是每次写入各同步一次。
                    MagicianPlayerComponent magician = MagicianPlayerComponent.KEY.get(serverPlayer);
                    magician.syncOnceAfter(() -> {
                        magician.reset();
                        magician.assignInitialCooldown();
                        // Mid-round assignments get the roster now; round start re-captures it once every role is dealt.
                        // 回合中途分配时立即记录名单；回合开始时会在全部身份发放后重新记录。
                        MagicianRoster.capture(serverPlayer);
                    });
                } else {
                    // A Magician who is re-roled mid-round (admin force-role, a death that converts into a Wraith,
                    // any other conversion) must not keep a live puppet replaying shots in their name, nor a running
                    // recording, cooldown or roster: the playback tick only watches the owner's life, never the role.
                    // Everyone who holds no Magician state is skipped, so other roles see no reset and no packet.
                    // 回合中途被更换职业的魔术师（管理员强制指定、死亡后转化为冤魂或其他转化）不得保留仍以其名义重放开枪的
                    // 活皮套，也不得保留进行中的录制、冷却或名单：播放刻只检查主人是否存活，从不检查职业。不持有任何魔术师
                    // 状态的玩家会被跳过，因此其他职业既不会被重置也不会收到数据包。
                    MagicianPlayerComponent magician = MagicianPlayerComponent.KEY.get(serverPlayer);
                    if (magician.holdsMagicianState()) {
                        // A dead owner's live puppet is first ended like the playback tick ends it (replay line
                        // included); the reset then clears the rest, and both go out as one sync.
                        // 已死亡主人的活皮套先按播放刻的方式结束（包含回放记录）；随后的重置清空其余状态，两者合并为一次同步。
                        magician.syncOnceAfter(() -> {
                            MagicianPlaybackManager.finishPlaybackOfDeadOwner(serverPlayer);
                            magician.reset();
                        });
                    }
                }
                ProphetRuntime.assignForRole(serverPlayer, role);
                ProphetEconomyService.assignForRole(serverPlayer, role);
                WitchSkillAssignmentService.assignForRole(serverPlayer, role);
                WitchManaService.assignForRole(serverPlayer, role);
                WitchFactionFeatureService.assignForRole(serverPlayer, role);
                MurderousWitchFeatureService.assignForRole(serverPlayer, role);
                PigGodEconomyService.assignForRole(serverPlayer, role);
                SaintFeatureService.assignForRole(serverPlayer, role);
                PerfumerEconomyService.assignForRole(serverPlayer, role);
                NinjaFeatureService.assignForRole(serverPlayer, role);
                OrthopedistSkillService.assignForRole(serverPlayer, role);
                OrthopedistEconomyService.assignForRole(serverPlayer, role);
                DominatorSkillService.assignForRole(serverPlayer, role);
                SaviorDomainService.assignForRole(serverPlayer, role);
                if (!GuardianAngelRules.isGuardianAngel(role)) {
                    GuardianAngelFeatureService.detachPlayer(serverPlayer);
                }
            }
        });
        TaskComplete.EVENT.register(WitchManaService::onTaskComplete);
        DiscovererService.register();
        // Night Vision key (PR #12): its payload and its cooldown lifecycle, right after the passives.
        // 夜视键（PR #12）：数据包与冷却生命周期，紧随被动之后。
        DiscovererNightVisionService.register();
        DiscovererShopService.register();
        AccountantFeatureService.register();
        // Tono Hanna (PR #212): after the shared RoleAssigned listener above, whose WitchManaService.assignForRole
        // grants her opening mana; her own listener only resets the float component.
        // 远野汉娜（PR #212）：排在上方共享 RoleAssigned 监听器之后，其开局魔力由其中的 WitchManaService.assignForRole
        // 发放；她自己的监听器只重置浮游组件。
        HannaFeatureService.register();
        SpareKeyDoors.register();
        SpareKeyAttendantKit.register();
        // 泽渡可可 (neutral, PR #223): its hub fixes the order of her services. It sits after the shared RoleAssigned
        // listener above, whose WitchManaService.assignForRole turns her mana on; her own listener only resets her
        // component.
        // 泽渡可可（中立，PR #223）：各服务的顺序由其注册中心固定。它排在上方共享 RoleAssigned 监听器之后，其魔力由其中的
        // WitchManaService.assignForRole 开启；她自己的监听器只重置组件。
        KokoFeatureService.register();
        // 犯罪推理学家 (neutral, PR #228): right after 泽渡可可; its win phase is ordered after hers.
        // 犯罪推理学家（中立，PR #228）：紧随泽渡可可；其胜负阶段排在她之后。
        CriminalReasonerRuntime.register();
        // 狮身豪 (Team Jiahao): both C2S payload types, once (idempotent); the server receivers live in its skill services.
        // 狮身豪（嘉豪阵营）：两个 C2S 数据包类型，只注册一次（幂等）；服务端接收器位于其技能服务中。
        SphinxHaoPayloads.registerTypes();
        // Defense Vial (PR #240): its replay lines and the round-edge reset of poured trays.
        // 防御药剂（PR #240）：回放文本，以及回合两端对已倒药托盘的清理。
        DefenseVialRuntime.register();
        TaskComplete.EVENT.register((player, taskType) -> GrandWitchActiveSkillService.onTaskComplete(player));
        TaskComplete.EVENT.register((player, taskType) -> WitchEconomyService.onTaskComplete(player));
        // Bewitched promotion progress (C3): counts and enqueues only; the role changes at END_SERVER_TICK.
        // 魔化使晋升进度（C3）：只计数与入队；身份在 END_SERVER_TICK 变更。
        TaskComplete.EVENT.register((player, taskType) -> BewitchedPromotionService.onTaskComplete(player));
        TaskComplete.EVENT.register((player, taskType) -> PigGodEconomyService.onTaskComplete(player));
        TaskComplete.EVENT.register((player, taskType) -> ProphetEconomyService.onTaskComplete(player));
        TaskComplete.EVENT.register((player, taskType) -> SaintEconomyService.onTaskComplete(player));
        TaskComplete.EVENT.register((player, taskType) -> PerfumerEconomyService.onTaskComplete(player));
        TaskComplete.EVENT.register((player, taskType) -> OrthopedistEconomyService.onTaskComplete(player));
        KillPlayer.AFTER.register(WraithConversion::afterKill);
        KillPlayer.AFTER.register(WitchManaService::afterKill);
        KillPlayer.AFTER.register(WitchEconomyService::afterKill);
        // A death the Last Stand trait intercepted is not a death: no use for the killer, and a revived Dominator
        // keeps uses and mark (Time Stealer / Seeker precedent). / 被背水一战词条拦截的死亡不算死亡：击杀者不加次数，
        // 复活的支配者保留次数与标记（窃时者、搜寻者先例）。
        KillPlayer.AFTER.register((victim, killer, deathReason) -> {
            if (!SparkTraitsApi.isLastStandDeathIntercepted(victim)) {
                DominatorSkillService.afterKill(victim, killer, deathReason);
            }
        });
        KillPlayer.AFTER.register((victim, killer, deathReason) -> {
            WitchFactionFeatureService.clearPlayerRuntime(victim);
            FirePokerFallAttributionService.clearPlayer(victim);
            PigGodChaseRuntime.clear(victim, WitchPlayerComponent.KEY.get(victim));
            PerfumerPlayerComponent.KEY.get(victim).stopCologne();
            if (!SparkTraitsApi.isLastStandDeathIntercepted(victim)) {
                // A Last Stand survivor keeps fear, the Otherworld state and cowardice (Dominator / Time Stealer /
                // Seeker precedent). / 背水一战存活者保留恐惧、里世界状态与怯懦（支配者、窃时者、搜寻者先例）。
                SpecterFearComponent.KEY.get(victim).clear();
                SpecterSecondSkillComponent.KEY.get(victim).clear();
                SpecterCowardiceComponent.KEY.get(victim).clear();
                // A Last Stand survivor keeps the Medium cooldowns and 招魂 bindings (same precedent).
                // 背水一战存活者保留通灵师冷却与招魂绑定（同一先例）。
                dev.caecorthus.sparkdlc.role.civilian.medium.MediumPlayerComponent.KEY.get(victim).clear();
                // A dead 橘雪莉 loses her cooldowns, badge holders and reveals (same Last Stand precedent).
                // 死亡的橘雪莉失去冷却、徽章持有者与揭示（同一背水一战先例）。
                dev.caecorthus.sparkdlc.role.civilian.sherry.SherryComponent.KEY.get(victim).clear();
            }
            if (SparkTraitsApi.isLastStandDeathIntercepted(victim)) {
                // A Last Stand survivor keeps the Exoskeleton layers (they last until death); the Orthopedist cooldown
                // and Bone Setting are cleared as they always were.
                // 背水一战存活者保留外骨骼层数（它们持续到死亡）；骨科大夫的冷却与正骨照旧清除。
                OrthopedistSkillService.clearPlayerKeepingExoskeleton(victim);
            } else {
                OrthopedistSkillService.clearPlayer(victim);
            }
            if (!SparkTraitsApi.isLastStandDeathIntercepted(victim)) {
                DominatorSkillService.clearPlayer(victim);
            }
        });
        ResetPlayer.EVENT.register(player -> {
            WitchFactionFeatureService.clearPlayerRuntime(player);
            FirePokerFallAttributionService.clearPlayer(player);
            WitchPlayerComponent.KEY.get(player).clear();
            MagicianPlayerComponent.KEY.get(player).reset();
            PerfumerPlayerComponent.KEY.get(player).clear();
            if (player instanceof ServerPlayerEntity serverPlayer) {
                // ResetPlayer fires after Wathe cleared the inventory: the stash is discarded, never re-inserted.
                // ResetPlayer 在 Wathe 清空物品栏之后触发：原物品被丢弃，绝不重新放入。
                DominatorForcedShotService.discard(serverPlayer);
                SpecterFearComponent.KEY.get(serverPlayer).clear();
                SpecterSecondSkillComponent.KEY.get(serverPlayer).clear();
                SpecterCowardiceComponent.KEY.get(serverPlayer).clear();
                dev.caecorthus.sparkdlc.role.civilian.medium.MediumPlayerComponent.KEY.get(serverPlayer).clear();
                dev.caecorthus.sparkdlc.role.civilian.sherry.SherryComponent.KEY.get(serverPlayer).clear();
                OrthopedistSkillService.clearPlayer(serverPlayer);
                DominatorSkillService.clearPlayer(serverPlayer);
                GuardianAngelFeatureService.clearRoundPlayer(serverPlayer);
                if (WraithStateService.isActive(serverPlayer)) {
                    // The traits observe active Wraith state while dispatching terminal trait removals.
                    // 天赋功能分发终局天赋移除事件时必须仍能观察到激活的冤魂状态。
                    SparkTraitsApi.clearWraithTraits(serverPlayer, true);
                }
                WraithLifecycle.clearPlayer(serverPlayer);
            }
        });
        GameEvents.ON_FINISH_INITIALIZE.register((world, gameComponent) -> {
            if (world instanceof ServerWorld serverWorld) {
                WraithConversion.beginRound(serverWorld, gameComponent.getAllPlayers().size());
                MagicianRoster.captureAll(serverWorld);
                SpecterOtherworldComponent.KEY.get(serverWorld).clear();
                // A window from the previous round never survives into a new one.
                // 上一局的投票窗口绝不带入新一局。
                dev.caecorthus.sparkdlc.role.civilian.medium.MediumSeanceComponent.KEY.get(serverWorld).clear();
            }
        });
        GameEvents.ON_FINISH_FINALIZE.register((world, gameComponent) -> {
            if (world instanceof ServerWorld serverWorld) {
                // Round end clears only the own roles' runtime state; role maps and other mods remain owned by wathe.
                // 回合结束只清理本模组职业的运行态，身份表和其他模组状态仍由 wathe 自己管理。
                WitchWorldComponent.KEY.get(serverWorld).clearRoundState();
                FirePokerFallAttributionService.clearAll();
                SpecterOtherworldComponent.KEY.get(serverWorld).clear();
                dev.caecorthus.sparkdlc.role.civilian.medium.MediumSeanceComponent.KEY.get(serverWorld).clear();
                WraithLifecycle.clearRoundState(serverWorld);
                PoisonApplePlateService.clearLoadedPlates();
                MagicianPlaybackManager.clearAll();
                MagicianDecoyBodies.clear();
                for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                    WitchFactionFeatureService.clearPlayerRuntime(player);
                    WitchPlayerComponent.KEY.get(player).clear();
                    MagicianPlayerComponent.KEY.get(player).reset();
                    PerfumerPlayerComponent.KEY.get(player).clear();
                    SpecterFearComponent.KEY.get(player).clear();
                    SpecterSecondSkillComponent.KEY.get(player).clear();
                    SpecterCowardiceComponent.KEY.get(player).clear();
                    dev.caecorthus.sparkdlc.role.civilian.medium.MediumPlayerComponent.KEY.get(player).clear();
                    dev.caecorthus.sparkdlc.role.civilian.sherry.SherryComponent.KEY.get(player).clear();
                    DominatorForcedShotService.discard(player);
                    OrthopedistSkillService.clearPlayer(player);
                    DominatorSkillService.clearPlayer(player);
                    GuardianAngelFeatureService.clearRoundPlayer(player);
                }
            }
        });
        // Headset slot round edges (BEAR earplugs, Blind ComTac). / 耳机槽回合节点（BEAR 耳塞、盲人 ComTac）。
        dev.caecorthus.sparkdlc.headset.HeadsetLifecycle.register();
        // BEAR (civilian): its hub fixes the order of the BEAR services; registered before USEC, which stays last.
        // BEAR（平民）：BEAR 各服务的顺序由其注册中心固定；注册在 USEC 之前，USEC 保持最后。
        dev.caecorthus.sparkdlc.role.civilian.bear.BearFeatureService.register();
        // Medium (civilian): its hub fixes the order of the Medium services; registered after BEAR and before USEC,
        // which stays last. / 通灵师（平民）：通灵师各服务的顺序由其注册中心固定；注册在 BEAR 之后、USEC 之前，USEC 保持最后。
        dev.caecorthus.sparkdlc.role.civilian.medium.MediumFeatureService.register();
        // 橘雪莉 (civilian, PR #206): its hub fixes the order of her services; registered after the Medium and before
        // USEC, which stays last. / 橘雪莉（平民，PR #206）：各服务的顺序由其注册中心固定；注册在通灵师之后、USEC 之前，
        // USEC 保持最后。
        dev.caecorthus.sparkdlc.role.civilian.sherry.SherryFeatureService.register();
        // 规划大师 (civilian, PR #219): its hub fixes the order of its services; registered after 橘雪莉 and before USEC,
        // which stays last. / 规划大师（平民，PR #219）：各服务的顺序由其注册中心固定；注册在橘雪莉之后、USEC 之前，USEC
        // 保持最后。
        dev.caecorthus.sparkdlc.role.civilian.planner.PlannerFeatureService.register();
        // 狮身豪 Sphinx Hao (Team Jiahao): its hub fixes the order of its server services (kit, doors, shop, skills);
        // registered after 规划大师 and before USEC, which stays last. / 狮身豪（嘉豪阵营）：各服务端服务（开局物品、门、
        // 商店、技能）的顺序由其注册中心固定；注册在规划大师之后、USEC 之前，USEC 保持最后。
        dev.caecorthus.sparkdlc.role.jiahao.sphinxhao.SphinxHaoFeatureService.register();
        // The Sniper (Team Jiahao): its hub fixes the order of its server services (kit, money and shop, the M1903A4's
        // sounds and replay lines, the gun-punishment exemption); registered after 狮身豪 and before USEC, which stays
        // last. / 狙击手（嘉豪阵营）：各服务端服务（开局物品、金钱与商店、M1903A4 音效与回放文本、开枪惩罚豁免）的顺序由其
        // 注册中心固定；注册在狮身豪之后、USEC 之前，USEC 保持最后。
        SniperFeatureService.register();
        // USEC (police variant): appended last so every existing listener keeps its order; the hub fixes the order of
        // the USEC services. / USEC（警察变体）：追加在最后，使所有既有监听器保持顺序；USEC 各服务的顺序由注册中心固定。
        UsecFeatureService.register();
    }
}
