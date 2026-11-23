package dev.caecorthus.sparkdlc.command;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Wathe's forced roles (the {@code /wathe:forcerole} lock) as the lock validations of the trait and the witch-skill
 * commands read them. The trait and the skill validation each kept a copy of both lookups.
 * 词条命令与魔女技能命令的锁定校验所读取的 Wathe 强制身份（{@code /wathe:forcerole} 锁定）。词条校验与技能校验原先各留一份
 * 这两个查找。
 */
public final class WatheForcedRoles {
    private WatheForcedRoles() {
    }

    /**
     * The role {@code /wathe:forcerole} would lock for this name: the same scan as Wathe's {@code ForceRoleCommand},
     * so null exactly where Wathe rejects the name (unknown or special role).
     * {@code /wathe:forcerole} 对此名称会锁定的身份：与 Wathe 的 {@code ForceRoleCommand} 扫描相同，因此恰在 Wathe 拒绝该名称
     * （未知或特殊身份）时返回 null。
     */
    public static Role findForceableRole(String roleName) {
        for (Role role : WatheRoles.ROLES) {
            if (!WatheRoles.SPECIAL_ROLES.contains(role) && role.identifier().getPath().equals(roleName)) {
                return role;
            }
        }
        return null;
    }

    /** The role Wathe has locked for the player's next round, or null. / Wathe 为该玩家下局锁定的身份，没有则为 null。 */
    public static Role forcedRoleFor(ServerCommandSource source, ServerPlayerEntity player) {
        ScoreboardRoleSelectorComponent selector =
                ScoreboardRoleSelectorComponent.KEY.get(source.getServer().getScoreboard());
        return selector.getForcedRoleForPlayer(player.getUuid());
    }
}
