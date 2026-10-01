package dev.caecorthus.sparkdlc.witch.compat.cooldown;

import dev.caecorthus.sparkdlc.factionapi.api.cooldown.RoleSkillCooldownStore;
import dev.caecorthus.sparkdlc.witch.SparkWitch;
import dev.caecorthus.sparkdlc.witch.api.WitchSkillDefinition;
import dev.caecorthus.sparkdlc.witch.api.WitchSkillRegistry;
import dev.caecorthus.sparkdlc.witch.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.witch.roles.killer.kidnapper.KidnapperCarryState;
import dev.caecorthus.sparkdlc.witch.roles.killer.kidnapper.KidnapperRules;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.OptionalInt;

/**
 * The shared {@link WitchPlayerComponent} skill cooldown, used by every role that holds a registered witch skill
 * (Saint's Hellfire lives in its own store). Remaining time includes a deferred cooldown that waits for an active
 * window; raises and extensions also floor that deferred cooldown so a penalty is not lost at the window end.
 * 共享的 WitchPlayerComponent 技能冷却，适用于所有持有已注册魔女技能的职业（圣徒业火另有存储）。剩余时间包含等待主动
 * 窗口结束的延后冷却；抬高与延长也会同时抬高该延后冷却，使惩罚不会在窗口结束时丢失。
 */
final class WitchSkillCooldownStore implements RoleSkillCooldownStore {
    static final Identifier ID = SparkWitch.id("witch_skill");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public boolean appliesTo(ServerPlayerEntity player) {
        return WitchPlayerComponent.KEY.get(player).hasSkill();
    }

    @Override
    public int remainingTicks(ServerPlayerEntity player) {
        WitchPlayerComponent component = WitchPlayerComponent.KEY.get(player);
        return ForcedCooldownMath.witchSkillRemaining(
                component.getCooldownTicks(),
                component.getDeferredCooldownTicks(),
                component.getActiveSkillWindowTicks()
        );
    }

    @Override
    public OptionalInt nominalTicks(ServerPlayerEntity player) {
        Identifier skillId = WitchPlayerComponent.KEY.get(player).getActiveSkillId();
        WitchSkillDefinition skill = skillId == null ? null : WitchSkillRegistry.get(skillId);
        return skill == null ? OptionalInt.empty() : OptionalInt.of(skill.cooldownTicks());
    }

    /**
     * Exact writes cannot be expressed while a deferred cooldown is pending, so this floors both counters; the
     * registry never asks for less than the current remaining time.
     * 延后冷却待启动时无法表达精确写入，因此这里对两个计数都取下限；注册表不会请求低于当前剩余值的数。
     */
    @Override
    public void setRemainingTicks(ServerPlayerEntity player, int ticks) {
        raiseTo(player, ticks);
    }

    @Override
    public boolean mayForce(ServerPlayerEntity player) {
        WitchPlayerComponent component = WitchPlayerComponent.KEY.get(player);
        return ForcedCooldownMath.mayForceWitchSkill(
                KidnapperRules.DRAG_BODY_SKILL_ID.equals(component.getActiveSkillId()),
                KidnapperCarryState.findCarriedBody(player) != null
        );
    }

    @Override
    public boolean raiseTo(ServerPlayerEntity player, int ticks) {
        if (ticks <= 0) {
            return false;
        }
        ForcedCooldownMath.WitchSkillFloors floors = ForcedCooldownMath.raiseWitchSkill(ticks);
        return WitchPlayerComponent.KEY.get(player)
                .raiseForcedCooldownFloors(floors.cooldownTicks(), floors.deferredCooldownTicks());
    }

    @Override
    public boolean extendBy(ServerPlayerEntity player, int ticks) {
        if (ticks <= 0) {
            return false;
        }
        WitchPlayerComponent component = WitchPlayerComponent.KEY.get(player);
        ForcedCooldownMath.WitchSkillFloors floors = ForcedCooldownMath.extendWitchSkill(
                component.getCooldownTicks(),
                component.getDeferredCooldownTicks(),
                ticks
        );
        return component.raiseForcedCooldownFloors(floors.cooldownTicks(), floors.deferredCooldownTicks());
    }
}
