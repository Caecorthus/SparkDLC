package dev.caecorthus.sparkdlc.role.civilian.apprentice.abilities.murdersense;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.skill.api.WitchSkillUseContext;
import dev.caecorthus.sparkdlc.skill.api.WitchSkillUseResult;
import dev.caecorthus.sparkdlc.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.record.AchievementRecords;
import dev.caecorthus.sparkdlc.role.civilian.apprentice.abilities.ApprenticeAbilitySupport;
import dev.doctor4t.wathe.game.GameConstants;
import java.util.Set;
import net.minecraft.util.Identifier;

public final class MurderSenseAbility {
    public static final Identifier ID = SparkDLC.id("murder_sense");
    public static final int MANA_COST = 40;
    public static final int DURATION_TICKS = GameConstants.getInTicks(0, 15);
    public static final int COOLDOWN_TICKS = GameConstants.getInTicks(0, 40);
    public static final double RANGE_BLOCKS = 20.0;
    // A graduated Apprentice senses farther (owner D7). / 出师后感知范围更远（所有者 D7）。
    public static final double GRADUATED_RANGE_BLOCKS = 30.0;
    public static final int COLOR = 0xFF3030;

    public static final Set<Identifier> DANGEROUS_ITEM_IDS = Set.of(
            Identifier.of("wathe", "revolver"),
            Identifier.of("wathe", "derringer"),
            Identifier.of("noellesroles", "demon_hunter_pistol"),
            Identifier.of("wathe", "knife"),
            Identifier.of("wathe", "bat"),
            Identifier.of("wathe", "grenade"),
            Identifier.of("wathe", "poison_vial"),
            Identifier.of("wathe", "scorpion"),
            Identifier.of("noellesroles", "poison_needle"),
            Identifier.of("noellesroles", "poison_gas_bomb"),
            Identifier.of("noellesroles", "throwing_axe"),
            SparkDLC.id("ceremonial_sword"),
            SparkDLC.id("fire_poker"),
            // USEC AXMC (outside wathe:guns, so it needs its own entry). / USEC 的 AXMC（不在 wathe:guns 中，需单独列出）。
            SparkDLC.id("usec_rifle"),
            // Sphinx Hao "手"枪 (outside wathe:guns, so it needs its own entry). / 狮身豪“手”枪（不在 wathe:guns 中，需单独列出）。
            SparkDLC.id("sphinx_hao_hand_gun"),
            // Sniper M1903A4 (outside wathe:guns, so it needs its own entry). / 狙击手 M1903A4（不在 wathe:guns 中，需单独列出）。
            SparkDLC.id("m1903a4"),
            // M16A4 burst rifle (outside wathe:guns, so it needs its own entry). / M16A4 点射步枪（不在 wathe:guns 中，需单独列出）。
            SparkDLC.id("m16a4")
    );

    private MurderSenseAbility() {
    }

    public static double rangeBlocks(boolean graduated) {
        return graduated ? GRADUATED_RANGE_BLOCKS : RANGE_BLOCKS;
    }

    public static WitchSkillUseResult use(WitchSkillUseContext context) {
        return ApprenticeAbilitySupport.use(
                context,
                MANA_COST,
                COOLDOWN_TICKS,
                "message.sparkdlc.skill.murder_sense.activated",
                () -> {
                    WitchPlayerComponent.KEY.get(context.player()).beginMurderSense(DURATION_TICKS);
                    // Achievement record W5, at the actual activation (role and mana already passed).
                    // 成就记录 W5，在真正启用时写入（职业与魔力检查均已通过）。
                    AchievementRecords.murderSense(context.player(), DURATION_TICKS);
                }
        );
    }
}
