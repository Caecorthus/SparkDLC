package dev.caecorthus.sparkdlc;

import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import dev.caecorthus.sparkdlc.component.SparkFactionAntiFastRoundComponent;
import dev.caecorthus.sparkdlc.component.SparkFactionMapResetComponent;
import dev.caecorthus.sparkdlc.component.SparkFactionRoundEndComponent;
import dev.caecorthus.sparkdlc.component.SparkFactionThirdFactionComponent;

public final class SparkFactionComponents implements ScoreboardComponentInitializer {
    @Override
    public void registerScoreboardComponentFactories(@NotNull ScoreboardComponentFactoryRegistry registry) {
        registry.registerScoreboardComponent(SparkFactionRoundEndComponent.KEY, SparkFactionRoundEndComponent::new);
        registry.registerScoreboardComponent(
                SparkFactionAntiFastRoundComponent.KEY,
                SparkFactionAntiFastRoundComponent::new
        );
        registry.registerScoreboardComponent(SparkFactionMapResetComponent.KEY, SparkFactionMapResetComponent::new);
        registry.registerScoreboardComponent(
                SparkFactionThirdFactionComponent.KEY,
                SparkFactionThirdFactionComponent::new
        );
    }
}
