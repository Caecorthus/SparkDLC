package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.role.killer.assassin.AssassinMisfireComponent;
import dev.caecorthus.sparkdlc.role.civilian.bodyguard.BodyguardGearComponent;
import dev.caecorthus.sparkdlc.role.neutral.pathogen.PathogenStrainComponent;
import dev.caecorthus.sparkdlc.role.neutral.pathogen.VirusCarrierComponent;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartyAnimalSkillComponent;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartyBalloonMarkComponent;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerCorpseComponent;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerOwnerComponent;
import dev.caecorthus.sparkdlc.entity.PartygoerEntity;
import dev.caecorthus.sparkdlc.collision.PlayerCollisionGraceWorldComponent;
import dev.caecorthus.sparkdlc.role.jiahao.corruptcop.CorruptCopAbilityComponent;
import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerBodySnapshotComponent;
import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.detective.DetectiveCasePlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.detective.DetectiveCaseWorldComponent;
import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterSniffPlayerComponent;
import dev.caecorthus.sparkdlc.economy.KillerTeamEconomyWorldComponent;
import dev.caecorthus.sparkdlc.role.civilian.engineer.EngineerStunnedPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.morphling.MorphBodyDisguiseWorldComponent;
import dev.caecorthus.sparkdlc.role.killer.morphling.MorphMarkPlayerComponent;
import dev.caecorthus.sparkdlc.role.killer.phantom.PhantomBackpackTargetComponent;
import dev.caecorthus.sparkdlc.role.killer.phantom.PhantomBackpackUserComponent;
import dev.caecorthus.sparkdlc.role.civilian.perfumer.kit.PerfumerScentComponent;
import dev.caecorthus.sparkdlc.role.civilian.professor.ProfessorSerumTargetComponent;
import dev.caecorthus.sparkdlc.role.civilian.professor.ProfessorSerumUserComponent;
import dev.caecorthus.sparkdlc.role.civilian.reporter.ReporterCommunicationComponent;
import dev.caecorthus.sparkdlc.sabotage.SabotageWorldComponent;
import dev.caecorthus.sparkdlc.tablet.TabletWorldComponent;
import dev.caecorthus.sparkdlc.role.civilian.noisemaker.NoisemakerGlowTargetComponent;
import dev.caecorthus.sparkdlc.role.civilian.noisemaker.NoisemakerGlowUserComponent;
import dev.caecorthus.sparkdlc.role.civilian.toxicologist.ToxicologistAntidoteComponent;
import dev.caecorthus.sparkdlc.role.civilian.undercover.RemovedDoorsWorldComponent;
import dev.caecorthus.sparkdlc.role.civilian.undercover.UndercoverPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.veteran.VeteranKnifeComponent;
import dev.caecorthus.sparkdlc.role.neutral.vulture.SkateboardRideComponent;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSuperCursePlayerComponent;
import dev.caecorthus.sparkdlc.role.neutral.shadowjester.ShadowJesterShowdownWorldComponent;
import dev.caecorthus.sparkdlc.role.civilian.waiter.WaiterTaskRevealComponent;
import dev.caecorthus.sparkdlc.role.civilian.timekeeper.TimekeeperWatchComponent;
import dev.caecorthus.sparkdlc.role.civilian.spiritualist.SpiritPossessionPlayerComponent;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadDazeComponent;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadPlayerComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

/**
 * SparkStrength 的 CCA 组件注册入口。
 *
 * <p>玩家组件都使用 NEVER_COPY：新一轮、死亡重生或玩家重置时不继承旧状态，
 * 防止上一局的大嗓门冷却、发光倒计时或侦探文件夹案件残留到下一局。</p>
 */
public class SparkStrengthComponents implements EntityComponentInitializer, WorldComponentInitializer {
    @Override
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
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
    }

    @Override
    public void registerWorldComponentFactories(@NotNull WorldComponentFactoryRegistry registry) {
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
    }
}
