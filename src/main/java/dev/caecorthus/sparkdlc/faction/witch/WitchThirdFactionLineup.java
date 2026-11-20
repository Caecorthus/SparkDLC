package dev.caecorthus.sparkdlc.faction.witch;

import dev.caecorthus.sparkdlc.faction.api.ThirdFactionDrawContext;
import dev.caecorthus.sparkdlc.SparkDlcRoles;
import dev.doctor4t.wathe.api.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Thin faction-core adapter for the witch faction's third-faction lineup (registered in
 * {@code SparkDlcRoleRegistry} as a method reference): reads the draw context and maps
 * {@link WitchThirdFactionLineupRules} seats to roles. Pure: it never writes a role, the RNG or any state; the core
 * deals the returned roles. The Grand Witch's appearance condition is ignored on purpose (it is always false): the
 * draw's own player minimum governs.
 * 魔女阵营“第三阵营”阵容的阵营核心薄适配层（在 {@code SparkDlcRoleRegistry} 中以方法引用注册）：读取抽取上下文，
 * 把 {@link WitchThirdFactionLineupRules} 的席位映射为职业。纯函数：从不写入职业、随机数或任何状态；由阵营核心
 * 发放返回的职业。刻意不读取大魔女的出现条件（恒为 false）：由抽取自身的最低人数决定。
 */
public final class WitchThirdFactionLineup {
    private WitchThirdFactionLineup() {
    }

    public static List<Role> rolesToDeal(ThirdFactionDrawContext context) {
        return rolesFor(
                context.presentMembers(),
                context.remainingSlots(),
                context.gameComponent()::isRoleEnabled,
                SparkDlcRoles.grandWitch(),
                SparkDlcRoles.bewitched()
        );
    }

    static List<Role> rolesFor(
            List<Role> presentMembers,
            int remainingSlots,
            Predicate<Role> enabled,
            Role grandWitch,
            Role bewitched
    ) {
        boolean grandWitchPresent = false;
        for (Role member : presentMembers) {
            if (member != null && grandWitch.identifier().equals(member.identifier())) {
                grandWitchPresent = true;
                break;
            }
        }
        List<Role> roles = new ArrayList<>();
        for (WitchThirdFactionLineupRules.Seat seat : WitchThirdFactionLineupRules.seats(
                grandWitchPresent,
                enabled.test(grandWitch),
                enabled.test(bewitched),
                remainingSlots
        )) {
            roles.add(seat == WitchThirdFactionLineupRules.Seat.GRAND_WITCH ? grandWitch : bewitched);
        }
        return List.copyOf(roles);
    }
}
