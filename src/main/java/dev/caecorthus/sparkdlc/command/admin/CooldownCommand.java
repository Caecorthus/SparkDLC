package dev.caecorthus.sparkdlc.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import dev.caecorthus.sparkdlc.cooldown.api.AdminCooldownClear;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;

public final class CooldownCommand {
    private CooldownCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sparkdlc:clearCooldown")
                .requires(AdminToolPermissions.admin())
                .executes(context -> clearCooldown(
                        context.getSource(),
                        List.of(context.getSource().getPlayerOrThrow())
                ))
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .executes(context -> clearCooldown(
                                context.getSource(),
                                EntityArgumentType.getPlayers(context, "targets")
                        ))));
    }

    private static int clearCooldown(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        int changed = 0;
        for (ServerPlayerEntity target : targets) {
            ItemStack stack = target.getMainHandStack();
            if (stack.isEmpty()) {
                continue;
            }
            // The features that keep a floor, a lock or a timer of their own behind this cooldown are told through
            // AdminCooldownClear, in the order their three former mixins into this call nested: "was it cooling" is
            // read first, then the listeners that must run before the remove (the forced melee floor is lifted, or
            // the remove would be cancelled), the remove itself, then the listeners after it (item locks released
            // when the clear took effect; the clear recorded against Saint Karma when the item was cooling before
            // and is not any more). A server player's cooldown manager is its own ServerItemCooldownManager, so the
            // listeners get the target itself.
            // 在该冷却背后另有下限、锁或计时的功能经由 AdminCooldownClear 得到通知，顺序与它们原先针对此调用的三个 mixin 的嵌套
            // 顺序一致：先读取“是否处于冷却”，再运行必须先于移除的监听器（解除强制近战下限，否则移除会被取消），然后是移除本身，
            // 最后是移除之后的监听器（清除生效时释放物品锁；物品此前处于冷却而此时已不再冷却时，针对圣徒业障记录本次清除）。
            // 服务端玩家的冷却管理器就是它自己的 ServerItemCooldownManager，因此监听器直接拿到目标玩家。
            ItemCooldownManager manager = target.getItemCooldownManager();
            Item item = stack.getItem();
            boolean wasCooling = manager.isCoolingDown(item);
            AdminCooldownClear.beforeRemove(target, item);
            manager.remove(item);
            AdminCooldownClear.afterRemove(target, item, wasCooling);
            changed++;
        }
        int finalChanged = changed;
        source.sendFeedback(() -> Text.literal("Cleared current item cooldown for " + finalChanged + " player(s)."), true);
        return changed;
    }
}
