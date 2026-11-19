package dev.caecorthus.sparkdlc.event;

import dev.caecorthus.sparkdlc.role.civilian.detective.DetectiveCasePlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.noisemaker.NoisemakerGlowTargetComponent;
import dev.caecorthus.sparkdlc.role.civilian.noisemaker.NoisemakerGlowUserComponent;
import dev.caecorthus.sparkdlc.role.civilian.professor.ProfessorSerumTargetComponent;
import dev.caecorthus.sparkdlc.role.civilian.professor.ProfessorSerumUserComponent;
import dev.caecorthus.sparkdlc.role.civilian.reporter.ReporterCommunicationComponent;
import dev.caecorthus.sparkdlc.role.civilian.timekeeper.TimekeeperWatchComponent;
import dev.caecorthus.sparkdlc.role.killer.assassin.AssassinGuessService;
import dev.caecorthus.sparkdlc.role.civilian.bodyguard.BodyguardShieldService;
import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerService;
import dev.caecorthus.sparkdlc.role.jiahao.corruptcop.CorruptCopAbilityService;
import dev.caecorthus.sparkdlc.role.civilian.demonhunter.DemonHunterSniffService;
import dev.caecorthus.sparkdlc.role.civilian.engineer.EngineerCaptureDeviceService;
import dev.caecorthus.sparkdlc.role.killer.morphling.MorphlingService;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartyBalloonService;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerSkillService;
import dev.caecorthus.sparkdlc.role.killer.phantom.PhantomBackpackService;
import dev.caecorthus.sparkdlc.role.civilian.toxicologist.ToxicologistAntidoteService;
import dev.caecorthus.sparkdlc.role.civilian.toxicologist.ToxicologistBluePassiveService;
import dev.caecorthus.sparkdlc.role.civilian.undercover.UndercoverService;
import dev.caecorthus.sparkdlc.role.civilian.veteran.VeteranKnifeService;
import dev.caecorthus.sparkdlc.role.neutral.vulture.VultureSuperCurseService;
import dev.caecorthus.sparkdlc.role.civilian.waiter.WaiterTaskRevealService;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * The one per-player round reset: every SparkStrength per-player state that must not outlive a round.
 * 唯一的单人回合重置：所有不能跨局保留的 SparkStrength 单人状态。
 *
 * <p>Runs on Wathe's ResetPlayer, which fires after the player's inventory and effects are cleared, never on death:
 * in baseInitialize at round start (before any RoleAssigned, so role setup is never wiped); in resetPlayer, which
 * finalizeGame runs for every player of the game world, spectators included, before it clears the role map and fires
 * ON_FINISH_FINALIZE; and in resetPlayer from serverTick while no game runs (non-creative players inside the play area,
 * spectators without the bypass permission). SparkWitch also calls resetPlayer for a disconnected Vendetta-bound killer.
 * No step branches on the role map or the game status, so it does the same at round start, round end and in the
 * lobby.</p>
 * <p>在 Wathe 的 ResetPlayer 上执行；该事件在清空玩家物品栏与效果之后触发，死亡时不会触发：开局 baseInitialize
 * （早于任何 RoleAssigned，因此不会抹掉身份分配的设置）；finalizeGame 在清空身份表并触发 ON_FINISH_FINALIZE 之前，
 * 对游戏世界的每名玩家（含旁观者）调用的 resetPlayer；以及非对局期间 serverTick 调用的 resetPlayer（游戏区域内的非创造模式玩家、
 * 没有绕过权限的旁观者）。SparkWitch 也会对掉线的复仇绑定杀手调用 resetPlayer。没有任何一步依据身份表或对局状态分支，
 * 因此开局、终局与大厅中的效果完全相同。</p>
 */
public final class PlayerRoundReset {
    private PlayerRoundReset() {
    }

    public static void reset(ServerPlayerEntity player) {
        // The steps are independent but for one rule: Undercover puts the saved armour back before Coroner sweeps its
        // temporary grants from every slot, so nothing put back can outlive the sweep.
        // 各步骤互不依赖，只有一条顺序约束：卧底先把保存的护甲穿回去，验尸官再从所有槽位清走临时装备，
        // 这样穿回去的东西不会躲过清理。
        NoisemakerGlowUserComponent.KEY.get(player).reset();
        NoisemakerGlowTargetComponent.KEY.get(player).reset();
        PhantomBackpackService.clearPlayer(player);
        ProfessorSerumUserComponent.KEY.get(player).reset();
        ProfessorSerumTargetComponent.KEY.get(player).reset();
        ReporterCommunicationComponent.KEY.get(player).reset();
        WaiterTaskRevealService.reset(player);
        UndercoverService.reset(player);
        TimekeeperWatchComponent.KEY.get(player).reset();
        DetectiveCasePlayerComponent.KEY.get(player).clearAll();
        DemonHunterSniffService.clearPlayer(player);
        VultureSuperCurseService.clearPlayer(player);
        CorruptCopAbilityService.reset(player);
        CoronerService.clearPlayer(player);
        EngineerCaptureDeviceService.clearPlayer(player);
        MorphlingService.reset(player);
        ToxicologistAntidoteService.clearPlayer(player);
        ToxicologistBluePassiveService.clearPlayer(player);
        VeteranKnifeService.reset(player);
        BodyguardShieldService.resetRound(player);
        PartyBalloonService.clearPlayer(player);
        PartygoerSkillService.reset(player);
        AssassinGuessService.reset(player);
    }
}
