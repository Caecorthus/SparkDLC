package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.role.SparkDlcRoleRegistry;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.util.Identifier;

import java.util.List;

public final class SparkDlcRoles {
    public static final Identifier EMMA_ID = SparkDlcRoleRegistry.EMMA_ID;
    public static final Identifier GRAND_WITCH_ID = SparkDlcRoleRegistry.GRAND_WITCH_ID;
    public static final Identifier ACCOMPLICE_ID = SparkDlcRoleRegistry.ACCOMPLICE_ID;
    public static final Identifier APPRENTICE_WITCH_ID = SparkDlcRoleRegistry.APPRENTICE_WITCH_ID;
    public static final Identifier MURDEROUS_WITCH_ID = SparkDlcRoleRegistry.MURDEROUS_WITCH_ID;
    public static final Identifier PIG_GOD_ID = SparkDlcRoleRegistry.PIG_GOD_ID;
    public static final Identifier PROPHET_ID = SparkDlcRoleRegistry.PROPHET_ID;
    public static final Identifier SAINT_ID = SparkDlcRoleRegistry.SAINT_ID;
    public static final Identifier PERFUMER_ID = SparkDlcRoleRegistry.PERFUMER_ID;
    public static final Identifier NINJA_ID = SparkDlcRoleRegistry.NINJA_ID;
    public static final Identifier BLACK_RAVEN_ID = SparkDlcRoleRegistry.BLACK_RAVEN_ID;
    public static final Identifier HUNTER_ID = SparkDlcRoleRegistry.HUNTER_ID;
    public static final Identifier DOMINATOR_ID = SparkDlcRoleRegistry.DOMINATOR_ID;
    public static final Identifier ORTHOPEDIST_ID = SparkDlcRoleRegistry.ORTHOPEDIST_ID;
    public static final Identifier KIDNAPPER_ID = SparkDlcRoleRegistry.KIDNAPPER_ID;
    public static final Identifier MAGICIAN_ID = SparkDlcRoleRegistry.MAGICIAN_ID;
    public static final Identifier TAROT_READER_ID = SparkDlcRoleRegistry.TAROT_READER_ID;
    public static final Identifier WRAITH_ID = SparkDlcRoleRegistry.WRAITH_ID;
    public static final Identifier WIND_SPIRIT_ID = SparkDlcRoleRegistry.WIND_SPIRIT_ID;
    public static final Identifier GUARDIAN_ANGEL_ID = SparkDlcRoleRegistry.GUARDIAN_ANGEL_ID;
    public static final Identifier VENDETTA_ID = SparkDlcRoleRegistry.VENDETTA_ID;
    public static final Identifier SABOTEUR_ID = SparkDlcRoleRegistry.SABOTEUR_ID;
    public static final Identifier WITCH_MAIDEN_ID = SparkDlcRoleRegistry.WITCH_MAIDEN_ID;
    public static final Identifier CURSER_ID = SparkDlcRoleRegistry.CURSER_ID;
    public static final Identifier BELL_RINGER_ID = SparkDlcRoleRegistry.BELL_RINGER_ID;
    public static final Identifier TIME_STEALER_ID = SparkDlcRoleRegistry.TIME_STEALER_ID;
    public static final Identifier JUDGE_ID = SparkDlcRoleRegistry.JUDGE_ID;
    public static final Identifier CONTROL_EXPERT_ID = SparkDlcRoleRegistry.CONTROL_EXPERT_ID;
    public static final Identifier SEEKER_ID = SparkDlcRoleRegistry.SEEKER_ID;
    public static final Identifier FISHER_ID = SparkDlcRoleRegistry.FISHER_ID;
    public static final Identifier BLIND_ID = SparkDlcRoleRegistry.BLIND_ID;
    public static final Identifier FIEND_ID = SparkDlcRoleRegistry.FIEND_ID;
    public static final Identifier STOWAWAY_ID = SparkDlcRoleRegistry.STOWAWAY_ID;
    public static final Identifier SCHRODINGERS_CAT_ID = SparkDlcRoleRegistry.SCHRODINGERS_CAT_ID;
    public static final Identifier INSIDER_ID = SparkDlcRoleRegistry.INSIDER_ID;
    public static final Identifier SNIPER_ID = SparkDlcRoleRegistry.SNIPER_ID;
    public static final Identifier ABYSS_LISTENER_ID = SparkDlcRoleRegistry.ABYSS_LISTENER_ID;
    public static final Identifier POTION_GUNNER_ID = SparkDlcRoleRegistry.POTION_GUNNER_ID;
    public static final Identifier RIFTWALKER_ID = SparkDlcRoleRegistry.RIFTWALKER_ID;
    public static final Identifier BEWITCHED_ID = SparkDlcRoleRegistry.BEWITCHED_ID;
    public static final Identifier USEC_ID = SparkDlcRoleRegistry.USEC_ID;
    public static final Identifier KNIGHT_ID = SparkDlcRoleRegistry.KNIGHT_ID;
    public static final Identifier FERRYMAN_ID = SparkDlcRoleRegistry.FERRYMAN_ID;
    public static final Identifier LOST_SOUL_ID = SparkDlcRoleRegistry.LOST_SOUL_ID;

    public static final Identifier RECRUIT_ID = SparkDlcRoleRegistry.RECRUIT_ID;
    public static final Identifier ATHLETE_ID = SparkDlcRoleRegistry.ATHLETE_ID;
    public static final Identifier DISCOVERER_ID = SparkDlcRoleRegistry.DISCOVERER_ID;
    public static final Identifier NOA_ID = SparkDlcRoleRegistry.NOA_ID;
    public static final Identifier RAGPICKER_ID = SparkDlcRoleRegistry.RAGPICKER_ID;
    public static final Identifier ACCOUNTANT_ID = SparkDlcRoleRegistry.ACCOUNTANT_ID;
    public static final Identifier BEAR_ID = SparkDlcRoleRegistry.BEAR_ID;
    public static final Identifier MEDIUM_ID = SparkDlcRoleRegistry.MEDIUM_ID;
    public static final Identifier HANNA_ID = SparkDlcRoleRegistry.HANNA_ID;
    public static final Identifier SHERRY_ID = SparkDlcRoleRegistry.SHERRY_ID;
    public static final Identifier NIKAIDO_HIRO_ID = SparkDlcRoleRegistry.NIKAIDO_HIRO_ID;
    public static final Identifier KOKO_ID = SparkDlcRoleRegistry.KOKO_ID;
    public static final Identifier CRIMINAL_REASONER_ID = SparkDlcRoleRegistry.CRIMINAL_REASONER_ID;
    public static final Identifier PLANNER_MASTER_ID = SparkDlcRoleRegistry.PLANNER_MASTER_ID;
    public static final Identifier SPHINX_HAO_ID = SparkDlcRoleRegistry.SPHINX_HAO_ID;

    private SparkDlcRoles() {
    }

    public static synchronized void register() {
        SparkDlcRoleRegistry.register();
    }

    public static synchronized void refreshAssassinGuessRoleOrder() {
        SparkDlcRoleRegistry.refreshAssassinGuessRoleOrder();
    }

    public static Role emma() {
        return SparkDlcRoleRegistry.emma();
    }

    public static Role grandWitch() {
        return SparkDlcRoleRegistry.grandWitch();
    }

    public static Role accomplice() {
        return SparkDlcRoleRegistry.accomplice();
    }

    public static Role potionGunner() {
        return SparkDlcRoleRegistry.potionGunner();
    }

    public static Role apprenticeWitch() {
        return SparkDlcRoleRegistry.apprenticeWitch();
    }

    public static Role murderousWitch() {
        return SparkDlcRoleRegistry.murderousWitch();
    }

    public static Role pigGod() {
        return SparkDlcRoleRegistry.pigGod();
    }

    public static Role prophet() {
        return SparkDlcRoleRegistry.prophet();
    }

    public static Role saint() {
        return SparkDlcRoleRegistry.saint();
    }

    public static Role perfumer() {
        return SparkDlcRoleRegistry.perfumer();
    }

    public static Role ninja() {
        return SparkDlcRoleRegistry.ninja();
    }

    public static Role blackRaven() {
        return SparkDlcRoleRegistry.blackRaven();
    }

    public static Role hunter() {
        return SparkDlcRoleRegistry.hunter();
    }

    public static Role orthopedist() {
        return SparkDlcRoleRegistry.orthopedist();
    }

    public static Role kidnapper() {
        return SparkDlcRoleRegistry.kidnapper();
    }

    public static Role magician() { return SparkDlcRoleRegistry.magician(); }

    public static Role tarotReader() {
        return SparkDlcRoleRegistry.tarotReader();
    }

    public static Role wraith() {
        return SparkDlcRoleRegistry.wraith();
    }

    public static Role windSpirit() {
        return SparkDlcRoleRegistry.windSpirit();
    }

    public static Role guardianAngel() {
        return SparkDlcRoleRegistry.guardianAngel();
    }

    public static Role vendetta() {
        return SparkDlcRoleRegistry.vendetta();
    }

    public static Role saboteur() {
        return SparkDlcRoleRegistry.saboteur();
    }

    public static Role witchMaiden() {
        return SparkDlcRoleRegistry.witchMaiden();
    }

    public static Role curser() {
        return SparkDlcRoleRegistry.curser();
    }

    public static Role bellRinger() {
        return SparkDlcRoleRegistry.bellRinger();
    }

    public static Role timeStealer() {
        return SparkDlcRoleRegistry.timeStealer();
    }

    public static Role judge() {
        return SparkDlcRoleRegistry.judge();
    }

    public static Role controlExpert() {
        return SparkDlcRoleRegistry.controlExpert();
    }

    public static Role seeker() {
        return SparkDlcRoleRegistry.seeker();
    }

    public static Role fisher() {
        return SparkDlcRoleRegistry.fisher();
    }

    public static Role blind() {
        return SparkDlcRoleRegistry.blind();
    }

    public static Role fiend() {
        return SparkDlcRoleRegistry.fiend();
    }

    public static Role stowaway() {
        return SparkDlcRoleRegistry.stowaway();
    }

    public static Role schrodingersCat() {
        return SparkDlcRoleRegistry.schrodingersCat();
    }

    public static Role insider() {
        return SparkDlcRoleRegistry.insider();
    }

    public static Role sniper() {
        return SparkDlcRoleRegistry.sniper();
    }

    public static Role abyssListener() {
        return SparkDlcRoleRegistry.abyssListener();
    }

    public static Role riftwalker() {
        return SparkDlcRoleRegistry.riftwalker();
    }

    public static Role bewitched() {
        return SparkDlcRoleRegistry.bewitched();
    }

    public static Role usec() {
        return SparkDlcRoleRegistry.usec();
    }

    public static Role knight() {
        return SparkDlcRoleRegistry.knight();
    }

    public static Role ferryman() {
        return SparkDlcRoleRegistry.ferryman();
    }

    public static Role lostSoul() {
        return SparkDlcRoleRegistry.lostSoul();
    }

    public static Role savior() {
        return SparkDlcRoleRegistry.savior();
    }

    public static Role recruit() {
        return SparkDlcRoleRegistry.recruit();
    }

    public static Role specter() {
        return SparkDlcRoleRegistry.specter();
    }

    public static Role athlete() {
        return SparkDlcRoleRegistry.athlete();
    }

    public static Role discoverer() {
        return SparkDlcRoleRegistry.discoverer();
    }

    public static Role noa() {
        return SparkDlcRoleRegistry.noa();
    }

    public static Role ragpicker() {
        return SparkDlcRoleRegistry.ragpicker();
    }

    public static Role dominator() {
        return SparkDlcRoleRegistry.dominator();
    }

    public static Role accountant() {
        return SparkDlcRoleRegistry.accountant();
    }

    public static Role bear() {
        return SparkDlcRoleRegistry.bear();
    }

    public static Role medium() {
        return SparkDlcRoleRegistry.medium();
    }

    public static Role hanna() {
        return SparkDlcRoleRegistry.hanna();
    }

    public static Role sherry() {
        return SparkDlcRoleRegistry.sherry();
    }

    public static Role nikaidoHiro() {
        return SparkDlcRoleRegistry.nikaidoHiro();
    }

    public static Role koko() {
        return SparkDlcRoleRegistry.koko();
    }

    public static Role criminalReasoner() {
        return SparkDlcRoleRegistry.criminalReasoner();
    }

    public static Role plannerMaster() {
        return SparkDlcRoleRegistry.plannerMaster();
    }

    public static Role sphinxHao() {
        return SparkDlcRoleRegistry.sphinxHao();
    }

    public static List<Role> assassinGuessRoles() {
        return SparkDlcRoleRegistry.assassinGuessRoles();
    }

    public static boolean isSkillRole(Role role) {
        return SparkDlcRoleRegistry.isSkillRole(role);
    }
}
