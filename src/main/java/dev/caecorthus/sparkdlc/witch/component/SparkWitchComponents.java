package dev.caecorthus.sparkdlc.witch.component;

import dev.caecorthus.sparkdlc.witch.roles.civilian.blind.BlindComponent;
import dev.caecorthus.sparkdlc.witch.roles.civilian.controlexpert.ControlExpertStatusComponent;
import dev.caecorthus.sparkdlc.witch.roles.civilian.saint.flash.HolyFlashComponent;
import dev.caecorthus.sparkdlc.witch.roles.civilian.fisher.spirit.FisherSpiritComponent;
import dev.caecorthus.sparkdlc.witch.roles.civilian.seeker.SeekerStatusComponent;
import dev.caecorthus.sparkdlc.witch.roles.civilian.judge.JudgeWorldComponent;
import dev.caecorthus.sparkdlc.witch.roles.civilian.emma.EmmaPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.civilian.emma.EmmaRoundComponent;
import dev.caecorthus.sparkdlc.witch.roles.civilian.orthopedist.OrthopedistPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.civilian.guardianangel.GuardianAngelPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.civilian.prophet.ProphetPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.civilian.vendetta.VendettaPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.killer.bellringer.BellEchoPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.killer.hunter.HunterPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.killer.kidnapper.KidnapperControlComponent;
import dev.caecorthus.sparkdlc.witch.roles.killer.blackraven.BlackRavenMarkPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.killer.blackraven.BlackRavenPerceptionPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.killer.blackraven.disguise.BlackRavenDisguiseComponent;
import dev.caecorthus.sparkdlc.witch.roles.killer.saboteur.SaboteurPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.killer.timestealer.TimeStealerPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.killer.timestealer.TimeTheftPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.neutral.fiend.FiendMomentWorldComponent;
import dev.caecorthus.sparkdlc.witch.roles.witch.accomplice.variant.AccompliceVariantRoundComponent;
import dev.caecorthus.sparkdlc.witch.roles.witch.curser.CurserPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.witch.grandwitch.GrandWitchRuntimeComponent;
import dev.caecorthus.sparkdlc.witch.roles.witch.grandwitch.factor.WitchFactorWorldComponent;
import dev.caecorthus.sparkdlc.witch.roles.witch.grandwitch.recruitment.GrandWitchRecruitmentRoundComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

public final class SparkWitchComponents implements EntityComponentInitializer, WorldComponentInitializer {
    @Override
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
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
        registry.beginRegistration(PlayerEntity.class, LegacyWraithPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(LegacyWraithPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, BellEchoPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(BellEchoPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, TimeStealerPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(TimeStealerPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, TimeTheftPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(TimeTheftPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, ControlExpertStatusComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(ControlExpertStatusComponent::new);
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
    }

    @Override
    public void registerWorldComponentFactories(@NotNull WorldComponentFactoryRegistry registry) {
        registry.register(JudgeWorldComponent.KEY, JudgeWorldComponent::new);
        registry.register(WitchWorldComponent.KEY, WitchWorldComponent::new);
        registry.register(WitchFactorWorldComponent.KEY, WitchFactorWorldComponent::new);
        registry.register(EmmaRoundComponent.KEY, EmmaRoundComponent::new);
        registry.register(GrandWitchRecruitmentRoundComponent.KEY, GrandWitchRecruitmentRoundComponent::new);
        registry.register(WraithRoundComponent.KEY, WraithRoundComponent::new);
        registry.register(LegacyWraithRoundComponent.KEY, LegacyWraithRoundComponent::new);
        registry.register(FiendMomentWorldComponent.KEY, FiendMomentWorldComponent::new);
        registry.register(AccompliceVariantRoundComponent.KEY, AccompliceVariantRoundComponent::new);
    }
}
