package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.collision.PlayerCollisionGraceWorldComponent;
import dev.caecorthus.sparkdlc.component.PerfumerPlayerComponent;
import dev.caecorthus.sparkdlc.component.AntiFastRoundComponent;
import dev.caecorthus.sparkdlc.component.LobbyMapResetComponent;
import dev.caecorthus.sparkdlc.component.FactionRoundEndComponent;
import dev.caecorthus.sparkdlc.component.ThirdFactionComponent;
import dev.caecorthus.sparkdlc.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.component.WitchWorldComponent;
import dev.caecorthus.sparkdlc.component.WraithPlayerComponent;
import dev.caecorthus.sparkdlc.component.WraithRoundComponent;
import dev.caecorthus.sparkdlc.economy.KillerTeamEconomyWorldComponent;
import dev.caecorthus.sparkdlc.entity.PartygoerEntity;
import dev.caecorthus.sparkdlc.item.rope.RopeHoldComponent;
import dev.caecorthus.sparkdlc.role.civilian.apprentice.ApprenticePlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.athlete.AthleteComponent;
import dev.caecorthus.sparkdlc.role.civilian.blind.BlindComponent;
import dev.caecorthus.sparkdlc.role.civilian.bodyguard.BodyguardGearComponent;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.ControlExpertStatusComponent;
import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerBodySnapshotComponent;
import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterSniffPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.detective.DetectiveCasePlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.detective.DetectiveCaseWorldComponent;
import dev.caecorthus.sparkdlc.role.civilian.emma.EmmaPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.emma.EmmaRoundComponent;
import dev.caecorthus.sparkdlc.role.civilian.engineer.EngineerStunnedPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.fisher.spirit.FisherSpiritComponent;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.hanna.HannaComponent;
import dev.caecorthus.sparkdlc.role.civilian.judge.JudgeWorldComponent;
import dev.caecorthus.sparkdlc.role.civilian.junker.JunkerLootWorldComponent;
import dev.caecorthus.sparkdlc.role.civilian.junker.JunkerPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.medium.MediumPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.medium.MediumSeanceComponent;
import dev.caecorthus.sparkdlc.role.civilian.noa.NoaComponent;
import dev.caecorthus.sparkdlc.role.civilian.noisemaker.NoisemakerGlowTargetComponent;
import dev.caecorthus.sparkdlc.role.civilian.noisemaker.NoisemakerGlowUserComponent;
import dev.caecorthus.sparkdlc.role.civilian.orthopedist.OrthopedistPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.perfumer.PerfumerScentComponent;
import dev.caecorthus.sparkdlc.role.civilian.planner.PlannerComponent;
import dev.caecorthus.sparkdlc.role.civilian.professor.ProfessorSerumTargetComponent;
import dev.caecorthus.sparkdlc.role.civilian.professor.ProfessorSerumUserComponent;
import dev.caecorthus.sparkdlc.role.civilian.prophet.ProphetPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.recruit.RecruitPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.reporter.ReporterCommunicationComponent;
import dev.caecorthus.sparkdlc.role.civilian.saint.flash.HolyFlashComponent;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerStatusComponent;
import dev.caecorthus.sparkdlc.role.civilian.spiritualist.SpiritPossessionPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.timekeeper.TimekeeperWatchComponent;
import dev.caecorthus.sparkdlc.role.civilian.toxicologist.ToxicologistAntidoteComponent;
import dev.caecorthus.sparkdlc.role.civilian.undercover.RemovedDoorsWorldComponent;
import dev.caecorthus.sparkdlc.role.civilian.undercover.UndercoverPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.usec.UsecPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.veteran.VeteranKnifeComponent;
import dev.caecorthus.sparkdlc.role.civilian.waiter.WaiterTaskRevealComponent;
import dev.caecorthus.sparkdlc.role.jiahao.corruptcop.CorruptCopAbilityComponent;
import dev.caecorthus.sparkdlc.role.jiahao.sphinxhao.SphinxHaoPlayerComponent;
import dev.caecorthus.sparkdlc.role.jiahao.sphinxhao.SphinxHaoWorldComponent;
import dev.caecorthus.sparkdlc.role.killer.assassin.AssassinMisfireComponent;
import dev.caecorthus.sparkdlc.role.killer.bellringer.BellEchoPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.blackraven.BlackRavenMarkPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.blackraven.BlackRavenPerceptionPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.blackraven.crow.BlackRavenCrowPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.blackraven.disguise.BlackRavenDisguiseComponent;
import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorForcedShotComponent;
import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.hiro.returning.HiroReturnComponent;
import dev.caecorthus.sparkdlc.role.killer.hiro.witchification.HiroWitchificationComponent;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.kidnapper.KidnapperControlComponent;
import dev.caecorthus.sparkdlc.role.killer.magician.MagicianPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.morphling.MorphBodyDisguiseWorldComponent;
import dev.caecorthus.sparkdlc.role.killer.morphling.MorphMarkPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartyAnimalSkillComponent;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartyBalloonMarkComponent;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerCorpseComponent;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerOwnerComponent;
import dev.caecorthus.sparkdlc.role.killer.phantom.PhantomBackpackTargetComponent;
import dev.caecorthus.sparkdlc.role.killer.phantom.PhantomBackpackUserComponent;
import dev.caecorthus.sparkdlc.role.killer.saboteur.SaboteurPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterCowardiceComponent;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterFearComponent;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterOtherworldComponent;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterSecondSkillComponent;
import dev.caecorthus.sparkdlc.role.killer.timestealer.TimeGiftPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.timestealer.TimeStealerPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.timestealer.TimeTheftPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.wither.MiniWitherPlayerComponent;
import dev.caecorthus.sparkdlc.role.neutral.criminalreasoner.CriminalReasonerComponent;
import dev.caecorthus.sparkdlc.role.neutral.ferryman.FerrymanFeignComponent;
import dev.caecorthus.sparkdlc.role.neutral.ferryman.FerrymanInstinctVeilComponent;
import dev.caecorthus.sparkdlc.role.neutral.ferryman.FerrymanMomentWorldComponent;
import dev.caecorthus.sparkdlc.role.neutral.ferryman.FerrymanPlayerComponent;
import dev.caecorthus.sparkdlc.role.neutral.fiend.FiendFeignComponent;
import dev.caecorthus.sparkdlc.role.neutral.fiend.FiendMomentWorldComponent;
import dev.caecorthus.sparkdlc.role.neutral.koko.KokoComponent;
import dev.caecorthus.sparkdlc.role.neutral.pathogen.PathogenStrainComponent;
import dev.caecorthus.sparkdlc.role.neutral.pathogen.VirusCarrierComponent;
import dev.caecorthus.sparkdlc.role.neutral.shadowjester.ShadowJesterShowdownWorldComponent;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadDazeComponent;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadPlayerComponent;
import dev.caecorthus.sparkdlc.role.neutral.vulture.SkateboardRideComponent;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSuperCursePlayerComponent;
import dev.caecorthus.sparkdlc.role.witch.abysslistener.zone.AbyssZoneExposureComponent;
import dev.caecorthus.sparkdlc.role.witch.accomplice.variant.AccompliceVariantRoundComponent;
import dev.caecorthus.sparkdlc.role.witch.bewitched.BewitchedPlayerComponent;
import dev.caecorthus.sparkdlc.role.witch.curser.CurserPlayerComponent;
import dev.caecorthus.sparkdlc.role.witch.grandwitch.GrandWitchRuntimeComponent;
import dev.caecorthus.sparkdlc.role.witch.grandwitch.factor.WitchFactorWorldComponent;
import dev.caecorthus.sparkdlc.role.witch.riftwalker.gate.RiftGateRegistryComponent;
import dev.caecorthus.sparkdlc.role.witch.riftwalker.session.RiftSessionComponent;
import dev.caecorthus.sparkdlc.sabotage.SabotageWorldComponent;
import dev.caecorthus.sparkdlc.tablet.TabletWorldComponent;
import dev.caecorthus.sparkdlc.trait.component.PreparefullComponent;
import dev.caecorthus.sparkdlc.trait.component.TraitPlayerComponent;
import dev.caecorthus.sparkdlc.trait.component.TraitWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

/**
 * The one Cardinal Components entrypoint of the mod ({@code cardinal-components} in {@code fabric.mod.json}). It
 * registers every component factory; the ids are declared once, under {@code custom.cardinal-components} in
 * {@code fabric.mod.json}, and the game test {@code SparkDlcStartupTest} compares that list with what is registered
 * here.
 *
 * <p>Order is kept from the four entrypoint classes this replaces: Cardinal Components asked each of them in turn
 * for its entity factories, then for its world factories, then for its scoreboard factories, so every method below
 * lists the former blocks in that same order (role kits, traits, the mod's own roles). A new component is appended
 * to the block of its feature.</p>
 *
 * <p>Player components all use NEVER_COPY: a new round, a respawn or a player reset must not inherit old state, so
 * that a cooldown, a glow countdown or a Detective folder case of the previous round cannot leak into the next.</p>
 *
 * 本模组唯一的 Cardinal Components 入口（{@code fabric.mod.json} 的 {@code cardinal-components}）。它注册全部组件工厂；
 * 组件 id 只在 {@code fabric.mod.json} 的 {@code custom.cardinal-components} 下声明一次，game test
 * {@code SparkDlcStartupTest} 会把该列表与此处实际注册的内容进行比对。
 *
 * <p>顺序沿用它所取代的四个入口类：Cardinal Components 依次向它们索取实体工厂、世界工厂、计分板工厂，因此下面每个方法都按
 * 同样的顺序列出原先的各段（职业套件、词条、本模组自有职业）。新增组件追加到所属功能的那一段。</p>
 *
 * <p>玩家组件都使用 NEVER_COPY：新一轮、死亡重生或玩家重置时不继承旧状态，
 * 防止上一局的大嗓门冷却、发光倒计时或侦探文件夹案件残留到下一局。</p>
 */
public final class SparkDlcComponents
        implements EntityComponentInitializer, WorldComponentInitializer, ScoreboardComponentInitializer {
    @Override
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
        // Kits of the Wathe and NoellesRoles roles. / Wathe 与 NoellesRoles 职业的套件。
        registry.beginRegistration(PlayerEntity.class, CorruptCopAbilityComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(CorruptCopAbilityComponent::new);
        registry.beginRegistration(PlayerEntity.class, UndercoverPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(UndercoverPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, NoisemakerGlowUserComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(NoisemakerGlowUserComponent::new);
        registry.beginRegistration(PlayerEntity.class, NoisemakerGlowTargetComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(NoisemakerGlowTargetComponent::new);
        registry.beginRegistration(PlayerEntity.class, PhantomBackpackUserComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(PhantomBackpackUserComponent::new);
        registry.beginRegistration(PlayerEntity.class, PhantomBackpackTargetComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(PhantomBackpackTargetComponent::new);
        registry.beginRegistration(PlayerEntity.class, ProfessorSerumUserComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(ProfessorSerumUserComponent::new);
        registry.beginRegistration(PlayerEntity.class, ProfessorSerumTargetComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(ProfessorSerumTargetComponent::new);
        // Replaces the retired sparkdlc:criminologist_player; CCA skips saved NBT for unregistered ids
        // (logs a warning, then drops it on the next save), so old worlds load without migration.
        // 取代已移除的 criminologist_player；CCA 会跳过未注册 ID 的旧存档 NBT（仅记录警告，下次保存时丢弃），旧世界无需迁移。
        registry.beginRegistration(PlayerEntity.class, DetectiveCasePlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(DetectiveCasePlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, CoronerPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(CoronerPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, VeteranKnifeComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(VeteranKnifeComponent::new);
        registry.beginRegistration(PlayerEntity.class, EngineerStunnedPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(EngineerStunnedPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, DemonHunterSniffPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(DemonHunterSniffPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, MorphMarkPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(MorphMarkPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, ToxicologistAntidoteComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(ToxicologistAntidoteComponent::new);
        registry.beginRegistration(PlayerEntity.class, PerfumerScentComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(PerfumerScentComponent::new);
        registry.beginRegistration(PlayerEntity.class, BodyguardGearComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(BodyguardGearComponent::new);
        registry.beginRegistration(PlayerEntity.class, SkateboardRideComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(SkateboardRideComponent::new);
        registry.beginRegistration(PlayerEntity.class, VultureSuperCursePlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(VultureSuperCursePlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, PartyAnimalSkillComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(PartyAnimalSkillComponent::new);
        registry.beginRegistration(PlayerEntity.class, PathogenStrainComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(PathogenStrainComponent::new);
        registry.beginRegistration(PlayerEntity.class, VirusCarrierComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(VirusCarrierComponent::new);
        registry.beginRegistration(PlayerEntity.class, PartyBalloonMarkComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(PartyBalloonMarkComponent::new);
        registry.beginRegistration(PlayerEntity.class, ReporterCommunicationComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(ReporterCommunicationComponent::new);
        registry.beginRegistration(PlayerEntity.class, WaiterTaskRevealComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(WaiterTaskRevealComponent::new);
        registry.beginRegistration(PlayerEntity.class, TimekeeperWatchComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(TimekeeperWatchComponent::new);
        registry.beginRegistration(PlayerEntity.class, SpiritPossessionPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(SpiritPossessionPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, TaotieHeadPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(TaotieHeadPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, TaotieHeadDazeComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(TaotieHeadDazeComponent::new);
        registry.beginRegistration(PlayerEntity.class, AssassinMisfireComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(AssassinMisfireComponent::new);
        registry.registerFor(PlayerBodyEntity.class, CoronerBodySnapshotComponent.KEY, CoronerBodySnapshotComponent::new);
        registry.registerFor(PlayerBodyEntity.class, PartygoerCorpseComponent.KEY, PartygoerCorpseComponent::new);
        registry.registerFor(PartygoerEntity.class, PartygoerOwnerComponent.KEY, PartygoerOwnerComponent::new);
        // Traits. / 词条。
        registry.beginRegistration(PlayerEntity.class, TraitPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(TraitPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, PreparefullComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(PreparefullComponent::new);
        // The mod's own roles and factions. / 本模组自有的职业与阵营。
        registry.beginRegistration(PlayerEntity.class, WitchPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(WitchPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, EmmaPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(EmmaPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, GrandWitchRuntimeComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(GrandWitchRuntimeComponent::new);
        registry.beginRegistration(PlayerEntity.class, PerfumerPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(PerfumerPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, ProphetPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(ProphetPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, HunterPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(HunterPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, KidnapperControlComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(KidnapperControlComponent::new);
        registry.beginRegistration(PlayerEntity.class, OrthopedistPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(OrthopedistPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, GuardianAngelPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(GuardianAngelPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, VendettaPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(VendettaPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, BlackRavenMarkPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(BlackRavenMarkPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, BlackRavenPerceptionPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(BlackRavenPerceptionPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, BlackRavenDisguiseComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(BlackRavenDisguiseComponent::new);
        registry.beginRegistration(PlayerEntity.class, WraithPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(WraithPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, SaboteurPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(SaboteurPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, CurserPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(CurserPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, MagicianPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(MagicianPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, BellEchoPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(BellEchoPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, TimeStealerPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(TimeStealerPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, TimeTheftPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(TimeTheftPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, TimeGiftPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(TimeGiftPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, ControlExpertStatusComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(ControlExpertStatusComponent::new);
        // Rope strangle hold (2026-11-11): which side of a strangle the owner is on, owner-synced and transient; kept
        // beside the stun status because it drives the same client input lock.
        // 绳索勒杀定身（2026-11-11）：拥有者处于勒杀的哪一方，只同步给本人且不保存；与眩晕状态相邻，因为它驱动同一套客户端输入锁。
        registry.beginRegistration(PlayerEntity.class, RopeHoldComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(RopeHoldComponent::new);
        registry.beginRegistration(PlayerEntity.class, HolyFlashComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(HolyFlashComponent::new);
        registry.beginRegistration(PlayerEntity.class, SeekerStatusComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(SeekerStatusComponent::new);
        registry.beginRegistration(PlayerEntity.class, FisherSpiritComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(FisherSpiritComponent::new);
        registry.beginRegistration(PlayerEntity.class, BlindComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(BlindComponent::new);
        registry.beginRegistration(PlayerEntity.class, AbyssZoneExposureComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(AbyssZoneExposureComponent::new);
        registry.beginRegistration(PlayerEntity.class, RiftSessionComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(RiftSessionComponent::new);
        // Bewitched promotion task count, synced to its owner only (C3). / 魔化使晋升任务计数，只同步给本人（C3）。
        registry.beginRegistration(PlayerEntity.class, BewitchedPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(BewitchedPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, ApprenticePlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(ApprenticePlayerComponent::new);
        // USEC scope state for the lens glint, synced to everyone; appended last.
        // USEC 开镜状态（镜头反光用），同步给所有人；追加在最后。
        registry.beginRegistration(PlayerEntity.class, UsecPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(UsecPlayerComponent::new);
        // Fiend feign-death cycle, synced to its owner only; appended last.
        // 魔人假死周期，只同步给本人；追加在最后。
        registry.beginRegistration(PlayerEntity.class, FiendFeignComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(FiendFeignComponent::new);
        // Witch Maiden Mini Wither: the active flag for everyone, tasks and cooldown for its owner; appended last.
        // 巫女小凋灵：在场标记同步给所有人，任务与冷却只同步给本人；追加在最后。
        registry.beginRegistration(PlayerEntity.class, MiniWitherPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(MiniWitherPlayerComponent::new);
        // Black Raven Raven Form: the active bit for everyone, the rest for its owner; appended last.
        // 黑羽鸦化鸦：在场标记同步给所有人，其余只同步给本人；追加在最后。
        registry.beginRegistration(PlayerEntity.class, BlackRavenCrowPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(BlackRavenCrowPlayerComponent::new);
        // Recruit first-task progress and knife grant, synced to its owner only; appended last.
        // 新兵首个任务进度与发刀状态，只同步给本人；追加在最后。
        registry.beginRegistration(PlayerEntity.class, RecruitPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(RecruitPlayerComponent::new);
        // Athlete dash ready tick, synced to its owner only; appended last.
        // 运动员冲刺就绪刻，只同步给本人；追加在最后。
        registry.beginRegistration(PlayerEntity.class, AthleteComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(AthleteComponent::new);
        // 城崎诺亚's paint ready tick (owner-synced) and this round's painted one-offs; appended last.
        // 城崎诺亚的作画就绪刻（只同步给本人）与本局已画出的一次性结果；追加在最后。
        registry.beginRegistration(PlayerEntity.class, NoaComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(NoaComponent::new);
        // Ragpicker search progress and ready tick, synced to its owner only; appended last.
        // 拾荒者搜刮进度与就绪刻，只同步给本人；追加在最后。
        registry.beginRegistration(PlayerEntity.class, JunkerPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(JunkerPlayerComponent::new);
        // Dominator uses and mark target, synced to its owner only; appended last.
        // 支配者剩余次数与标记目标，只同步给本人；追加在最后。
        registry.beginRegistration(PlayerEntity.class, DominatorPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(DominatorPlayerComponent::new);
        // Forced-shot sequence. Only the "active" flag is saved/copied: a loaded or respawn-copied sequence never resumes,
        // it purges leftover marked guns on its next tick and keeps no stash.
        // 强制开枪序列。只保存/复制“进行中”标记：读档或重生复制后的序列绝不继续，下一刻清除残留的标记左轮且不保留原物品。
        registry.beginRegistration(PlayerEntity.class, DominatorForcedShotComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY)
                .end(DominatorForcedShotComponent::new);
        // 夏目安安 (PR #194): hypnosis window and cooldown ready tick (server), their shown seconds synced to the owner
        // only; appended last. / 夏目安安（PR #194）：催眠窗口与冷却就绪刻（服务端），显示秒数只同步给本人；追加在最后。
        registry.beginRegistration(PlayerEntity.class,
                        dev.caecorthus.sparkdlc.role.killer.natsume.NatsumeComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(dev.caecorthus.sparkdlc.role.killer.natsume.NatsumeComponent::new);
        // Headset slot storage (BEAR earplugs, Blind ComTac VIII): saved, synced to its owner only; appended last.
        // 耳机槽存储（BEAR 耳塞、盲人 ComTac VIII）：随存档保存，只同步给本人；追加在最后。
        registry.beginRegistration(PlayerEntity.class, dev.caecorthus.sparkdlc.headset.HeadsetComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(dev.caecorthus.sparkdlc.headset.HeadsetComponent::new);
        // Specter sanity-outline colours, fear levels, second-skill state and cowardice; appended last.
        // 幽魂理智描边颜色、恐惧层数、二技能状态与怯懦；追加在最后。
        registry.beginRegistration(PlayerEntity.class, SpecterPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(SpecterPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, SpecterFearComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(SpecterFearComponent::new);
        registry.beginRegistration(PlayerEntity.class, SpecterSecondSkillComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(SpecterSecondSkillComponent::new);
        registry.beginRegistration(PlayerEntity.class, SpecterCowardiceComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(SpecterCowardiceComponent::new);
        // Savior G-skill cooldown and domain timer (PR #200), synced to its owner only; appended after the
        // Specter's components.
        // 救世主 G 技能冷却与领域计时（PR #200），只同步给本人；追加在幽魂组件之后。
        registry.beginRegistration(PlayerEntity.class, SaviorPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(SaviorPlayerComponent::new);
        // Medium cooldowns and 招魂 bindings, synced to the owner only; appended after the Savior's.
        // 通灵师冷却与招魂绑定，只同步给本人；追加在救世主组件之后。
        registry.beginRegistration(PlayerEntity.class, MediumPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(MediumPlayerComponent::new);
        // Charon (PR #202): pardon window (server only), both cooldowns and the marks (owner only); appended after the
        // Medium's component. / 卡戎（PR #202）：宽恕窗口（仅服务端）、两个冷却与标记（只同步给本人）；追加在通灵师组件之后。
        registry.beginRegistration(PlayerEntity.class, FerrymanPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(FerrymanPlayerComponent::new);
        // Charon gaze feign: the marked attacker's 10 s fake corpse, server only; appended last.
        // 卡戎「瞥视」假死：被标记者 10 秒的假尸体，仅服务端；追加在最后。
        registry.beginRegistration(PlayerEntity.class, FerrymanFeignComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(FerrymanFeignComponent::new);
        // Charon instinct veil: broadcast to everyone because instinct is drawn per client; appended last.
        // 卡戎的本能帷幕：本能按客户端绘制，因此同步给所有人；追加在最后。
        registry.beginRegistration(PlayerEntity.class, FerrymanInstinctVeilComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(FerrymanInstinctVeilComponent::new);
        // Tono Hanna (PR #212): float, penalty and handkerchief timers. Everyone receives only the float's remaining
        // ticks (instinct is drawn per client); the owner also gets the ready and penalty ticks. Appended last, after
        // the Charon's components.
        // 远野汉娜（PR #212）：浮游、惩罚与手帕计时。所有人只收到浮游的剩余刻数（本能按客户端绘制）；本人额外收到就绪刻与
        // 惩罚刻。追加在最后，位于卡戎的组件之后。
        registry.beginRegistration(PlayerEntity.class, HannaComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(HannaComponent::new);
        // 橘雪莉 (PR #206): skill cooldowns, her Mighty Force window, badge holders and reveal trackers, synced to the
        // owner only; appended last, after Tono Hanna's.
        // 橘雪莉（PR #206）：技能冷却、巨力窗口、徽章持有者与揭示追踪，只同步给本人；追加在最后，位于远野汉娜的组件之后。
        registry.beginRegistration(PlayerEntity.class,
                        dev.caecorthus.sparkdlc.role.civilian.sherry.SherryComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(dev.caecorthus.sparkdlc.role.civilian.sherry.SherryComponent::new);
        // 二阶堂希罗 (2026-11-11): 死亡回归 cooldown, window and record point, then 魔女化 progress; both synced to
        // the owner only; appended last, after 橘雪莉's component.
        // 二阶堂希罗（2026-11-11）：死亡回归的冷却、窗口与记录点，以及魔女化进度；均只同步给本人；追加在最后，位于橘雪莉的
        // 组件之后。
        registry.beginRegistration(PlayerEntity.class, HiroReturnComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(HiroReturnComponent::new);
        registry.beginRegistration(PlayerEntity.class, HiroWitchificationComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(HiroWitchificationComponent::new);
        // 泽渡可可 (PR #223): traffic, the stream and shared-cooldown ticks, the lightstick window and the bodies she
        // already streamed; synced to the owner only; appended last, after 二阶堂希罗's components.
        // 泽渡可可（PR #223）：流量、直播与共用冷却的刻、应援棒窗口以及已直播过的尸体；只同步给本人；追加在最后，位于
        // 二阶堂希罗的组件之后。
        registry.beginRegistration(PlayerEntity.class, KokoComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(KokoComponent::new);
        // 犯罪推理学家 (PR #228): the server-only answer key, solved victims, win progress, invisibility charges and the
        // cooldown readout; synced to the owner only (never the answer key); appended after 泽渡可可's component.
        // 犯罪推理学家（PR #228）：仅服务端的答案、已解出的死者、胜利进度、隐身充能与冷却读数；只同步给本人（从不同步答案）；
        // 追加在泽渡可可的组件之后。
        registry.beginRegistration(PlayerEntity.class, CriminalReasonerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(CriminalReasonerComponent::new);
        // 规划大师 (PR #219): the planner's recommendation ready tick and next-task seconds, and on any player the
        // recommendation marker; synced to the owner only; appended last, after 犯罪推理学家's component.
        // 规划大师（PR #219）：规划大师的推荐就绪刻与下一任务秒数，以及任何玩家身上的推荐标记；只同步给本人；追加在最后，
        // 位于犯罪推理学家的组件之后。
        registry.beginRegistration(PlayerEntity.class, PlannerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(PlannerComponent::new);
        // 狮身豪 Sphinx Hao (2026-11-17): hao and both skills' ready ticks; synced to the owner only; appended last, after
        // 规划大师's component.
        // 狮身豪（2026-11-17）：豪度值与两个技能的就绪刻；只同步给本人；追加在最后，位于规划大师的组件之后。
        registry.beginRegistration(PlayerEntity.class, SphinxHaoPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(SphinxHaoPlayerComponent::new);
    }

    @Override
    public void registerWorldComponentFactories(@NotNull WorldComponentFactoryRegistry registry) {
        // Kits of the Wathe and NoellesRoles roles. / Wathe 与 NoellesRoles 职业的套件。
        registry.register(KillerTeamEconomyWorldComponent.KEY, KillerTeamEconomyWorldComponent::new);
        // Replaces the retired sparkdlc:criminologist_world; its saved NBT is likewise ignored by CCA.
        // 取代已移除的 criminologist_world；其旧存档 NBT 同样会被 CCA 忽略。
        registry.register(DetectiveCaseWorldComponent.KEY, DetectiveCaseWorldComponent::new);
        registry.register(PlayerCollisionGraceWorldComponent.KEY, PlayerCollisionGraceWorldComponent::new);
        registry.register(TabletWorldComponent.KEY, TabletWorldComponent::new);
        registry.register(SabotageWorldComponent.KEY, SabotageWorldComponent::new);
        registry.register(RemovedDoorsWorldComponent.KEY, RemovedDoorsWorldComponent::new);
        registry.register(MorphBodyDisguiseWorldComponent.KEY, MorphBodyDisguiseWorldComponent::new);
        registry.register(
                ShadowJesterShowdownWorldComponent.KEY,
                ShadowJesterShowdownWorldComponent::new
        );
        // Traits. / 词条。
        registry.register(TraitWorldComponent.KEY, TraitWorldComponent::new);
        // The mod's own roles and factions. / 本模组自有的职业与阵营。
        registry.register(JudgeWorldComponent.KEY, JudgeWorldComponent::new);
        registry.register(WitchWorldComponent.KEY, WitchWorldComponent::new);
        registry.register(WitchFactorWorldComponent.KEY, WitchFactorWorldComponent::new);
        registry.register(EmmaRoundComponent.KEY, EmmaRoundComponent::new);
        registry.register(WraithRoundComponent.KEY, WraithRoundComponent::new);
        registry.register(FiendMomentWorldComponent.KEY, FiendMomentWorldComponent::new);
        registry.register(AccompliceVariantRoundComponent.KEY, AccompliceVariantRoundComponent::new);
        registry.register(RiftGateRegistryComponent.KEY, RiftGateRegistryComponent::new);
        registry.register(dev.caecorthus.sparkdlc.role.civilian.knight.duel.KnightDuelWorldComponent.KEY,
                dev.caecorthus.sparkdlc.role.civilian.knight.duel.KnightDuelWorldComponent::new);
        registry.register(JunkerLootWorldComponent.KEY, JunkerLootWorldComponent::new);
        registry.register(SpecterOtherworldComponent.KEY, SpecterOtherworldComponent::new);
        registry.register(MediumSeanceComponent.KEY, MediumSeanceComponent::new);
        // Charon (PR #202) Soul Moment, appended last, after the Medium's seance.
        // 卡戎（PR #202）亡魂时刻，追加在最后，位于通灵师通灵组件之后。
        registry.register(FerrymanMomentWorldComponent.KEY, FerrymanMomentWorldComponent::new);
        // 狮身豪 Sphinx Hao (2026-11-17) infections and 豪气冲天 window, appended last, after the Charon's Soul Moment.
        // 狮身豪（2026-11-17）的感染与豪气冲天窗口，追加在最后，位于卡戎亡魂时刻之后。
        registry.register(SphinxHaoWorldComponent.KEY, SphinxHaoWorldComponent::new);
    }

    @Override
    public void registerScoreboardComponentFactories(@NotNull ScoreboardComponentFactoryRegistry registry) {
        // Round services: round end, anti-fast-round, lobby map reset, third-faction draw.
        // 对局服务：回合结束、防速通、大厅地图重置、第三阵营抽取。
        registry.registerScoreboardComponent(FactionRoundEndComponent.KEY, FactionRoundEndComponent::new);
        registry.registerScoreboardComponent(
                AntiFastRoundComponent.KEY,
                AntiFastRoundComponent::new
        );
        registry.registerScoreboardComponent(LobbyMapResetComponent.KEY, LobbyMapResetComponent::new);
        registry.registerScoreboardComponent(
                ThirdFactionComponent.KEY,
                ThirdFactionComponent::new
        );
    }
}
