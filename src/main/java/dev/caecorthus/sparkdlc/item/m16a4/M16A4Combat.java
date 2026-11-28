package dev.caecorthus.sparkdlc.item.m16a4;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.item.m1903a4.M1903A4Combat;
import dev.caecorthus.sparkdlc.item.m1903a4.M1903A4Rules;
import dev.caecorthus.sparkdlc.util.OffMatchUse;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

/**
 * The M16A4's share of the rifle combat seam. The gate is the M1903A4's with the presentation use let through
 * ({@link M1903A4Combat#decideForOpenGun}): main hand, the shooter's held states (Control Expert stun, Engineer stun,
 * Taotie daze, Seeker remote lock, Kidnapper control, active Wraith, Dominator sequence), the trait block, the rifle's
 * item cooldown and at least one round. The rifle writes no cooldown of its own, so that fact is only ever an outside
 * lock (Knight duel seal, a forced cooldown). An accepted round runs the shared rifle hitscan with this gun's replay
 * id, without the sniper rifle's white trace or its kill confirmation sound.
 * M16A4 在步枪战斗接缝中的部分。门槛即 M1903A4 的门槛，但放行表现性使用（{@link M1903A4Combat#decideForOpenGun}）：主手、
 * 射手受控状态（控场专家眩晕、工程师定身、饕餮眩晕、搜寻者远程锁定、绑匪操控、激活的冤魂、支配者序列）、词条拦截、步枪的
 * 物品冷却与至少一发子弹。本枪自己从不写入冷却，因此该事实只可能来自外部锁定（骑士决斗封印、强制冷却）。被接受的子弹执行
 * 共用的步枪即时射线，使用本枪的回放 id，但不画狙击步枪的白色弹道、也不播狙击步枪的击杀确认音。
 */
public final class M16A4Combat {
    private M16A4Combat() {
    }

    static M1903A4Rules.Decision decide(PlayerEntity user, ItemStack gun, Hand hand) {
        return gate(user, gun, hand, M16A4Item.getAmmo(gun) > 0);
    }

    /**
     * Whether the holder may start a reload now: the fire gate without the magazine, so whatever stops the trigger
     * (not the main-hand rifle, a spectator, a dead participant, a held state, a trait block, a seal on the rifle)
     * stops the left click too.
     * 持有者此刻能否开始换弹：即不看弹匣的开火门槛，因此凡是能拦住扳机的（不是主手步枪、旁观、已死亡的参与者、受控状态、
     * 词条拦截、步枪被封印）同样拦住左键。
     */
    static boolean mayReload(ServerPlayerEntity holder, ItemStack gun) {
        return gate(holder, gun, Hand.MAIN_HAND, true) == M1903A4Rules.Decision.FIRE;
    }

    private static M1903A4Rules.Decision gate(PlayerEntity user, ItemStack gun, Hand hand, boolean loaded) {
        boolean mainHand = hand == Hand.MAIN_HAND && user.getMainHandStack() == gun
                && gun.getItem() instanceof M16A4Item;
        return M1903A4Combat.decideForOpenGun(user, gun, mainHand,
                user.getItemCooldownManager().isCoolingDown(gun.getItem()), loaded);
    }

    /**
     * The round plays its sound and flash for any allowed shooter, but its hitscan only runs for a MATCH shooter: no
     * player, puppet or device is touched by a presentation shot (no ACTIVE round, or a lobby holder during one).
     * A match round is always recorded; the replay only prints the ones that hit a player ({@link M16A4Replay}).
     * 任何被允许的射手都会看到枪声与火光，但只有 MATCH 射手的子弹进行即时射线结算：表现性射击（没有 ACTIVE 对局，或对局进行
     * 中的大厅持有者）不会触碰任何玩家、皮套或设备。对局中的每一发都会记录；回放只显示打中玩家的那些（{@link M16A4Replay}）。
     */
    static void fire(ServerPlayerEntity shooter, ItemStack gun) {
        if (OffMatchUse.mode(shooter,
                GameFunctions.isPlayerPlayingAndAlive(shooter) && !shooter.isSpectator()) != OffMatchUse.Mode.MATCH) {
            return;
        }
        M1903A4Combat.fireRifle(shooter, gun, SparkDlcItems.M16A4_ID, M16A4Rules.RANGE, false, null);
    }
}
