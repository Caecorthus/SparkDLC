package dev.caecorthus.sparkdlc.witch.impl;

import dev.caecorthus.sparkdlc.witch.SparkWitchRoles;
import dev.caecorthus.sparkdlc.witch.api.WitchSkillDefinition;
import dev.caecorthus.sparkdlc.witch.api.WitchSkillRegistry;
import dev.caecorthus.sparkdlc.witch.api.WitchSkillSelectionContext;
import dev.caecorthus.sparkdlc.witch.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.witch.component.WitchWorldComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Optional;
import java.util.random.RandomGenerator;

public final class WitchSkillAssignmentService {
    private WitchSkillAssignmentService() {
    }

    public static void assignForRole(ServerPlayerEntity player, Role role) {
        WitchPlayerComponent component = WitchPlayerComponent.KEY.get(player);
        if (!SparkWitchRoles.isSparkWitchRole(role)) {
            component.clear();
            return;
        }

        ServerWorld world = player.getServerWorld();
        GameWorldComponent gameComponent = GameWorldComponent.KEY.get(world);
        WitchWorldComponent worldComponent = WitchWorldComponent.KEY.get(world);
        WitchSkillSelectionContext context = new WitchSkillSelectionContext(world, gameComponent, player, role);
        RandomGenerator random = new java.util.Random(world.getRandom().nextLong());
        Optional<WitchSkillDefinition> selected = WitchSkillSelector.selectFrom(
                WitchSkillRegistry.values().stream()
                        .filter(skill -> worldComponent.isSkillEnabled(skill.id()))
                        .toList(),
                context,
                random
        );
        component.setActiveSkill(selected.map(WitchSkillDefinition::id).orElse(null));
        component.setCooldownTicks(0);
    }
}
