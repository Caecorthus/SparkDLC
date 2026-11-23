package dev.caecorthus.sparkdlc.client.mixin.role.killer.assassin;

import dev.doctor4t.wathe.api.Role;
import org.agmas.noellesroles.client.screen.AssassinScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/**
 * Reads NoellesRoles' own guessable-role list, so whatever is mixed into it (this mod's mixins hide the Savior and keep
 * the Saboteur) also shapes the grouped guess screen.
 * 读取 NoellesRoles 自己的可猜身份列表，让对它的修改（本模组的 mixin 隐藏救世主、保留破坏者）同样作用于分组猜测界面。
 */
@Mixin(value = AssassinScreen.class, remap = false)
public interface AssassinScreenInvoker {
    @Invoker(value = "getAllGuessableRoles", remap = false)
    List<Role> sparkdlc$guessableRoles();
}
