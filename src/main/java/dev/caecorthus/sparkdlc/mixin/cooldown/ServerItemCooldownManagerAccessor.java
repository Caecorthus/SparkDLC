package dev.caecorthus.sparkdlc.mixin.cooldown;

import net.minecraft.server.network.ServerItemCooldownManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes only the owner of a server item-cooldown manager: a removed cooldown releases that player's own timers, and
 * the cooldown-length traits and the Knight's derringer rule ask whose cooldown is being written. One accessor for
 * every reader (it was one per former mod).
 * 只暴露服务端物品冷却管理器的所属玩家：移除冷却时同时释放该玩家的自有计时，改变冷却时长的特质与骑士的德林加规则也借此得知
 * 正在写入的是谁的冷却。所有读取方共用这一个访问器（此前每个旧 mod 各有一个）。
 */
@Mixin(ServerItemCooldownManager.class)
public interface ServerItemCooldownManagerAccessor {
    @Accessor("player")
    ServerPlayerEntity sparkdlc$getPlayer();
}
