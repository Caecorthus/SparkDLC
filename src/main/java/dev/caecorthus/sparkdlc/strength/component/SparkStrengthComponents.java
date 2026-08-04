package dev.caecorthus.sparkdlc.strength.component;

import dev.caecorthus.sparkdlc.strength.component.corruptcop.CorruptCopAbilityComponent;
import dev.caecorthus.sparkdlc.strength.component.detective.CriminologistPlayerComponent;
import dev.caecorthus.sparkdlc.strength.component.detective.CriminologistWorldComponent;
import dev.caecorthus.sparkdlc.strength.component.demonhunter.DemonHunterSniffPlayerComponent;
import dev.caecorthus.sparkdlc.strength.component.engineer.EngineerStunnedPlayerComponent;
import dev.caecorthus.sparkdlc.strength.component.morphling.MorphBodyDisguiseWorldComponent;
import dev.caecorthus.sparkdlc.strength.component.morphling.MorphMarkPlayerComponent;
import dev.caecorthus.sparkdlc.strength.component.professor.ProfessorSerumTargetComponent;
import dev.caecorthus.sparkdlc.strength.component.professor.ProfessorSerumUserComponent;
import dev.caecorthus.sparkdlc.strength.component.tablet.TabletWorldComponent;
import dev.caecorthus.sparkdlc.strength.component.noisemaker.NoisemakerGlowTargetComponent;
import dev.caecorthus.sparkdlc.strength.component.noisemaker.NoisemakerGlowUserComponent;
import dev.caecorthus.sparkdlc.strength.component.toxicologist.ToxicologistAntidoteComponent;
import dev.caecorthus.sparkdlc.strength.component.veteran.VeteranKnifeComponent;
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
 * 防止上一局的大嗓门冷却、发光倒计时或犯罪学家追踪状态残留到下一局。</p>
 */
public class SparkStrengthComponents implements EntityComponentInitializer, WorldComponentInitializer {
    @Override
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(PlayerEntity.class, CorruptCopAbilityComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(CorruptCopAbilityComponent::new);
        registry.beginRegistration(PlayerEntity.class, NoisemakerGlowUserComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(NoisemakerGlowUserComponent::new);
        registry.beginRegistration(PlayerEntity.class, NoisemakerGlowTargetComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(NoisemakerGlowTargetComponent::new);
        registry.beginRegistration(PlayerEntity.class, ProfessorSerumUserComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(ProfessorSerumUserComponent::new);
        registry.beginRegistration(PlayerEntity.class, ProfessorSerumTargetComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(ProfessorSerumTargetComponent::new);
        registry.beginRegistration(PlayerEntity.class, CriminologistPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(CriminologistPlayerComponent::new);
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
    }

    @Override
    public void registerWorldComponentFactories(@NotNull WorldComponentFactoryRegistry registry) {
        registry.register(CriminologistWorldComponent.KEY, CriminologistWorldComponent::new);
        registry.register(TabletWorldComponent.KEY, TabletWorldComponent::new);
        registry.register(MorphBodyDisguiseWorldComponent.KEY, MorphBodyDisguiseWorldComponent::new);
    }
}
