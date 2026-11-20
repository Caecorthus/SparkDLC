package dev.caecorthus.sparkdlc.role.killer.partyanimal;

import dev.caecorthus.sparkdlc.role.civilian.bodyguard.BodyguardVestService;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.ModEffects;
import org.agmas.noellesroles.professor.IronManPlayerComponent;

import java.util.Optional;

/**
 * Counts the shield layers a player has right now, each worth 5 more seconds of the Partygoer's meal: Wathe psycho
 * armour, NR Iron Man, NR whiskey (one per level), the Guardian Angel shield and the Bodyguard vest. Each
 * 5 s bite then spends one through its own normal break, by a non-forced kill attempt.
 * 统计玩家当前的盾层数，每层让派对客多吃 5 秒：Wathe 疯魔护甲、NR 铁人、NR 威士忌（每级一层）、守护天使护盾、保镖防弹衣。
 * 之后每 5 秒的一口通过一次非强制击杀，让一层盾按其原本方式碎掉。
 */
public final class PartygoerShieldLayers {
    /** Guardian Angel shield effect, looked up by id because it used to be another jar's; always found now.
     * 守护天使护盾效果，因曾属于另一个 jar 而按 id 查找；现在必定能找到。 */
    private static final Identifier GUARDIAN_SHIELD_ID = Identifier.of("sparkdlc", "guardian_shield");

    private PartygoerShieldLayers() {
    }

    public static int count(ServerPlayerEntity player) {
        int layers = 0;
        PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(player);
        if (psycho.getPsychoTicks() > 0) {
            layers += Math.max(0, psycho.getArmour());
        }
        if (IronManPlayerComponent.KEY.get(player).hasBuff()) {
            layers++;
        }
        StatusEffectInstance whiskey = player.getStatusEffect(ModEffects.WHISKEY_SHIELD);
        if (whiskey != null) {
            layers += whiskey.getAmplifier() + 1;
        }
        Optional<RegistryEntry.Reference<StatusEffect>> guardian = Registries.STATUS_EFFECT.getEntry(GUARDIAN_SHIELD_ID);
        if (guardian.isPresent() && player.hasStatusEffect(guardian.get())) {
            layers++;
        }
        if (BodyguardVestService.isWorn(player)) {
            layers++;
        }
        return layers;
    }
}
