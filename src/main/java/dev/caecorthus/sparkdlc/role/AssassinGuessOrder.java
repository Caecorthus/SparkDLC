package dev.caecorthus.sparkdlc.role;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;

import java.util.List;

final class AssassinGuessOrder {
    private AssassinGuessOrder() {
    }

    static void appendToTail(List<Role> roles) {
        // Keep the roles registered here at the end of the Assassin guess panels (NoellesRoles' and the trait page).
        // 让此处注册的职业稳定显示在刺客猜身份面板（NoellesRoles 的面板与词条页）末尾。
        WatheRoles.ROLES.removeIf(role -> containsIdentity(roles, role));
        WatheRoles.ROLES.addAll(roles);
    }

    private static boolean containsIdentity(List<Role> roles, Role candidate) {
        for (Role role : roles) {
            if (role == candidate) {
                return true;
            }
        }
        return false;
    }
}
