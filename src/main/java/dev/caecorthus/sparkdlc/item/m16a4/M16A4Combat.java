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
 * The M16A4's share of the rifle combat seam. The gate is the shared rifle gate (main hand, the shooter's held states,
 * the trait block, at least one round; the rifle has no item cooldown), and an accepted round runs the shared rifle
 * hitscan with this gun's replay id, without the sniper rifle's white trace or its kill confirmation sound.
 * M16A4 在步枪战斗接缝中的部分。门槛即共用的步枪门槛（主手、射手受控状态、词条拦截、至少一发子弹；本枪没有物品冷却），
 * 被接受的子弹执行共用的步枪即时射线，使用本枪的回放 id，但不画狙击步枪的白色弹道、也不播狙击步枪的击杀确认音。
 */
public final class M16A4Combat {
    private M16A4Combat() {
    }

    static M1903A4Rules.Decision decide(PlayerEntity user, ItemStack gun, Hand hand) {
        return M1903A4Combat.decideForOpenGun(user, gun, hand, M16A4Item.getAmmo(gun) > 0);
    }

    /**
     * The round plays its sound and flash for any allowed shooter, but its hitscan only runs for a MATCH shooter: no
     * player, puppet or device is touched by a presentation shot (no ACTIVE round, or a lobby holder during one).
     * 任何被允许的射手都会看到枪声与火光，但只有 MATCH 射手的子弹进行即时射线结算：表现性射击（没有 ACTIVE 对局，或对局进行
     * 中的大厅持有者）不会触碰任何玩家、皮套或设备。
     */
    static void fire(ServerPlayerEntity shooter, ItemStack gun) {
        if (OffMatchUse.mode(shooter,
                GameFunctions.isPlayerPlayingAndAlive(shooter) && !shooter.isSpectator()) != OffMatchUse.Mode.MATCH) {
            return;
        }
        M1903A4Combat.fireRifle(shooter, gun, SparkDlcItems.M16A4_ID, false, null, M16A4Rules.RANGE);
    }
}
