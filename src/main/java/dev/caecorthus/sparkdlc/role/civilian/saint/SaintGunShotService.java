package dev.caecorthus.sparkdlc.role.civilian.saint;

import dev.caecorthus.sparkdlc.role.killer.dominator.DominatorForcedShotScope;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterPlayerComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * The Saint may carry guns but never kills with them (owner 2026-11-28). Her shot runs through Wathe's whole kill
 * path like anyone's, so every veto and every protection of the victim still decides first; only a death that would
 * really commit is replaced: the victim stays alive, is rooted for 3 s and glows for 10 s
 * ({@code mixin/role/civilian/saint/GameFunctionsSaintGunShotMixin}). The gun does not matter, only the bullet's death
 * reason ({@link SaintRules#isGunDeathReason}) and the raw role of the player the kill is credited to. Her shot never
 * punishes her either ({@code SaintGunShotReceiverMixin}). The one exception is the shot the Dominator forces: it is
 * the Dominator's kill and stays lethal.
 * 圣徒可以持枪，但永远无法用枪杀人（所有者 2026-11-28）。她的射击与其他人一样走完 Wathe 的整条击杀路径，因此每个否决与受害者
 * 的每项保护仍然先行裁定；只有真正会落定的死亡被替换：受害者存活，被禁锢 3 秒并发光 10 秒
 * （{@code mixin/role/civilian/saint/GameFunctionsSaintGunShotMixin}）。判定与具体枪械无关，只看子弹的死因
 * （{@link SaintRules#isGunDeathReason}）与击杀归属者的真实职业。她的射击也从不让她自己受罚
 * （{@code SaintGunShotReceiverMixin}）。唯一的例外是支配者强迫开出的那一枪：那是支配者的击杀，仍然致命。
 */
public final class SaintGunShotService {
    private SaintGunShotService() {
    }

    /** Raw role, like every other Saint rule: a Black Raven acting as the Saint keeps lethal guns. / 真实职业，与圣徒的其他规则一致：扮演圣徒的黑羽鸦用枪仍然致命。 */
    public static boolean isSaint(@Nullable PlayerEntity player) {
        return player != null
                && SaintRules.isSaint(GameWorldComponent.KEY.get(player.getWorld()).getRole(player));
    }

    /**
     * Whether this kill is a Saint's gunshot, which never commits. Wathe's {@code force} flag is not consulted: no gun
     * kill is forced, and a kill replayed later (the Charon's expired pardon) is still her bullet.
     * 本次击杀是否为圣徒的枪击（永不落定）。不参考 Wathe 的 {@code force} 标记：没有强制的枪杀，而稍后重放的击杀
     * （卡戎宽恕到期）仍然是她的子弹。
     */
    public static boolean isNonLethalShot(
            ServerPlayerEntity victim,
            @Nullable ServerPlayerEntity killer,
            Identifier deathReason
    ) {
        return killer != null
                && !killer.getUuid().equals(victim.getUuid())
                && SaintRules.isGunDeathReason(deathReason)
                && !DominatorForcedShotScope.isFiring(killer)
                && isSaint(killer);
    }

    /**
     * What the hit does instead of killing: the Hunter trap's root without a fracture layer (a longer root already
     * running is kept; Bone Setting is not consumed) and vanilla Glowing, visible to everyone.
     * 命中后取代死亡的效果：猎人捕兽夹的禁锢但不附带骨折层（已有更长的禁锢时保留更长的；不消耗正骨），以及所有人可见的
     * 原版发光。
     */
    public static void applyHit(ServerPlayerEntity victim, ServerPlayerEntity shooter) {
        HunterPlayerComponent.KEY.get(victim).applyRoot(SaintRules.GUN_SHOT_ROOT_TICKS);
        victim.addStatusEffect(new StatusEffectInstance(
                StatusEffects.GLOWING,
                SaintRules.GUN_SHOT_GLOW_TICKS,
                0,
                false,
                false,
                true
        ));
        // Neither line names a role or a player. / 两行提示都不点名职业或玩家。
        victim.sendMessage(Text.translatable("message.sparkdlc.saint.gun_shot.victim"), true);
        shooter.sendMessage(Text.translatable("message.sparkdlc.saint.gun_shot.shooter"), true);
    }
}
