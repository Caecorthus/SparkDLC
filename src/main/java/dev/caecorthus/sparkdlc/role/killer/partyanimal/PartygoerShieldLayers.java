package dev.caecorthus.sparkdlc.role.killer.partyanimal;

import dev.caecorthus.sparkdlc.role.civilian.bodyguard.BodyguardVestService;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelEffects;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import org.agmas.noellesroles.ModEffects;
import org.agmas.noellesroles.professor.IronManPlayerComponent;

/**
 * Counts the shield layers a player has right now, each worth 5 more seconds of the Partygoer's meal: Wathe psycho
 * armour, NR Iron Man, NR whiskey (one per level), the Guardian Angel shield and the Bodyguard vest. Each
 * 5 s bite then spends one through its own normal break, by a non-forced kill attempt.
 * 统计玩家当前的盾层数，每层让派对客多吃 5 秒：Wathe 疯魔护甲、NR 铁人、NR 威士忌（每级一层）、守护天使护盾、保镖防弹衣。
 * 之后每 5 秒的一口通过一次非强制击杀，让一层盾按其原本方式碎掉。
 */
public final class PartygoerShieldLayers {
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
        if (player.hasStatusEffect(GuardianAngelEffects.guardianShield())) {
            layers++;
        }
        if (BodyguardVestService.isWorn(player)) {
            layers++;
        }
        return layers;
    }
}
