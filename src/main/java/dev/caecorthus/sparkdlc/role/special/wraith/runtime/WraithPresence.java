package dev.caecorthus.sparkdlc.role.special.wraith.runtime;

import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaInteractionService;

import dev.caecorthus.sparkdlc.role.special.wraith.WraithStateService;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.agmas.noellesroles.ModEffects;
import org.agmas.noellesroles.silencer.SilencedPlayerComponent;

/**
 * Owns the player properties and status effects that make an active Wraith present in the world.
 * 负责让激活冤魂存在于世界中的玩家属性与状态效果。
 */
public final class WraithPresence {
    private WraithPresence() {
    }

    static void apply(ServerPlayerEntity player, boolean restricted) {
        player.setInvulnerable(!VendettaInteractionService.isActiveVendetta(player));
        SilencedPlayerComponent.KEY.get(player).reset();
        refresh(player, StatusEffects.INVISIBILITY, 0);
        refresh(player, ModEffects.NO_COLLISION, 0);
        if (restricted) {
            refresh(player, StatusEffects.SLOWNESS, 1);
            refresh(player, StatusEffects.BLINDNESS, 0);
        }
    }

    static void removeRestrictedEffects(ServerPlayerEntity player) {
        player.removeStatusEffect(StatusEffects.SLOWNESS);
        player.removeStatusEffect(StatusEffects.BLINDNESS);
    }

    static void clear(ServerPlayerEntity player) {
        player.removeStatusEffect(StatusEffects.INVISIBILITY);
        removeRestrictedEffects(player);
        player.removeStatusEffect(ModEffects.NO_COLLISION);
        player.setInvulnerable(false);
    }

    /**
     * Identifies effects that external cleanup must preserve for the current Wraith phase.
     * 标识外部清理在当前冤魂阶段必须保留的效果。
     */
    public static boolean shouldPreserve(PlayerEntity player, RegistryEntry<StatusEffect> effect) {
        if (!WraithStateService.isActive(player)) {
            return false;
        }
        if (effect.matches(StatusEffects.INVISIBILITY) || ModEffects.NO_COLLISION.matches(effect)) {
            return true;
        }
        return WraithStateService.isRestricted(player)
                && (effect.matches(StatusEffects.SLOWNESS) || effect.matches(StatusEffects.BLINDNESS));
    }

    private static void refresh(ServerPlayerEntity player, RegistryEntry<StatusEffect> effect, int amplifier) {
        StatusEffectInstance current = player.getStatusEffect(effect);
        if (current == null || !current.isInfinite() || current.getAmplifier() != amplifier) {
            player.addStatusEffect(new StatusEffectInstance(
                    effect, StatusEffectInstance.INFINITE, amplifier, false, false, false));
        }
    }
}
