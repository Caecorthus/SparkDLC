package dev.caecorthus.sparkdlc.role.civilian.controlexpert;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * The Control Expert's own stun counter and nothing else: whether a Taser or Shock Device stun is still counting down
 * on this player. It is the Seeker's only read of Control Expert state (device gates, remote sessions, the Taotie
 * release, the console's availability prediction). A missing component would read as "not stunned", but the component
 * is registered for every player, so that fallback is unreachable since the fusion.
 * 仅读取控场专家自身的眩晕计时：该玩家身上的电击枪或电击装置眩晕是否仍在倒计时。它是搜寻者对控场专家状态的唯一读取点
 * （设备门槛、遥控会话、饕餮释放、控制台的可用性预测）。组件缺失时会视为“未眩晕”，但该组件为每名玩家注册，
 * 因此这一回退在融合后不可达。
 *
 * <p>Deliberately not {@link ControlExpertStun#isStunned}: that one is the shared input lock, so it also answers true
 * for the Black Raven's landing hold and the Rope strangle hold, and it drops a stunned player who stopped
 * participating. This read sees neither hold and does not ask about participation. It used to be filed as
 * {@code compat.noellesroles.SeekerControlExpertBridge}, a seam between two mods; both sides are one mod now.
 * 刻意不等同于 {@link ControlExpertStun#isStunned}：后者是共用的输入锁，因此对黑羽鸦的落地定身和绳索勒杀定身同样回答
 * true，并且会排除已不再参与对局的被眩晕者。本读取既看不到这两种定身，也不询问是否参与对局。它原先归档为
 * {@code compat.noellesroles.SeekerControlExpertBridge}（两个模组之间的接缝）；如今两端同属一个模组。</p>
 */
public final class ControlExpertStunCounter {
    private ControlExpertStunCounter() {
    }

    public static boolean isCounting(@Nullable PlayerEntity player) {
        if (player == null) {
            return false;
        }
        return ControlExpertStatusComponent.KEY.maybeGet(player)
                .map(ControlExpertStatusComponent::isStunned)
                .orElse(false);
    }
}
