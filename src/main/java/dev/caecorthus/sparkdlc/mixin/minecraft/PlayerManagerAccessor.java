package dev.caecorthus.sparkdlc.mixin.minecraft;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Persists a disconnected player entity after a deferred change to its saved state: the Vendetta's deferred Wathe
 * escape or round reset, and the cleanup the trait 最后的逃脱 Last Escape confirms after the disconnect. One invoker
 * for both (each used to declare its own).
 * 在延迟修改离线玩家实体的存档状态后把结果写回：复仇者的延迟逃脱或回合重置，以及特质“最后的逃脱”在断线后确认的清理。
 * 两者共用这一个调用口（此前各自声明一个）。
 */
@Mixin(PlayerManager.class)
public interface PlayerManagerAccessor {
    @Invoker("savePlayerData")
    void sparkdlc$savePlayerData(ServerPlayerEntity player);
}
