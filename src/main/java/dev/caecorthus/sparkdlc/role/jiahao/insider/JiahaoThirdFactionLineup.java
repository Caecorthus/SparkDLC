package dev.caecorthus.sparkdlc.role.jiahao.insider;

import dev.caecorthus.sparkdlc.faction.api.ThirdFactionDrawContext;
import dev.caecorthus.sparkdlc.SparkDlcRoles;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Thin adapter for Team Jiahao's third-faction lineup (registered in {@code SparkDlcRoleRegistry} as a method
 * reference): reads the draw context and maps {@link JiahaoThirdFactionLineupRules} seats to roles. The Corrupt Cop is
 * found by its exact id among Wathe's registered roles; a list without it would just make Team Jiahao undrawable, which
 * cannot happen now that NoellesRoles is a required dependency. It never writes a role or any state; the third-faction
 * draw deals the returned roles. Its only RNG read is the second seat's 50/50 coin (Insider or Sphinx Hao, owner Q1),
 * flipped from the draw world's random source and only when a partner seat is actually dealt, so the draw's own RNG
 * sequence is otherwise unchanged. A forced (pre-round) Sniper, which the draw counts toward the faction cap, fills the
 * partner seat like a forced Insider or Sphinx Hao; the Sniper itself is never dealt by this draw.
 * 嘉豪阵营“第三阵营”阵容的薄适配层（在 {@code SparkDlcRoleRegistry} 中以方法引用注册）：读取抽取上下文，把
 * {@link JiahaoThirdFactionLineupRules} 的席位映射为职业。黑警按精确 id 在 Wathe 已注册职业中查找；列表中没有它时嘉豪阵
 * 营只是不可抽取，而 NoellesRoles 现为必需依赖，这种情况不会出现。从不写入职业或任何状态；由第三阵营抽取发放返回的职
 * 业。唯一的随机数读取是第二个席位的 50/50 硬币（内应或狮身豪，所有者 Q1），取自抽取世界的随机源，且只在确实发放同伴席
 * 位时抛出，因此抽取自身的随机序列在其他情况下不变。开局前强制指定的狙击手（抽取将其计入阵营上限）与强制的内应或狮身豪
 * 一样占用同伴席位；狙击手本身从不由本抽取发放。
 */
public final class JiahaoThirdFactionLineup {
    private JiahaoThirdFactionLineup() {
    }

    public static List<Role> rolesToDeal(ThirdFactionDrawContext context) {
        return rolesFor(
                context.presentMembers(),
                context.remainingSlots(),
                context.gameComponent()::isRoleEnabled,
                registeredCorruptCop(WatheRoles.ROLES),
                SparkDlcRoles.insider(),
                SparkDlcRoles.sphinxHao(),
                context.world().getRandom()::nextBoolean
        );
    }

    static List<Role> rolesFor(
            List<Role> presentMembers,
            int remainingSlots,
            Predicate<Role> enabled,
            @Nullable Role corruptCop,
            Role insider,
            Role sphinxHao,
            BooleanSupplier sphinxHaoCoin
    ) {
        int corruptCops = 0;
        int partners = 0;
        for (Role member : presentMembers) {
            if (InsiderParticipation.isCorruptCopRole(member)) {
                corruptCops++;
            } else if (InsiderParticipation.isPartnerRole(member) || InsiderParticipation.isSniperRole(member)) {
                // A forced Sniper fills the partner seat like a forced Insider or Sphinx Hao, so the team stays the
                // Corrupt Cop plus one of the three; it is never dealt here (no Sniper seat).
                // 强制指定的狙击手与强制的内应或狮身豪一样占用同伴席位，因此队伍仍为黑警加三者之一；这里从不发放狙击手（没有狙击手席位）。
                partners++;
            }
        }
        List<Role> roles = new ArrayList<>();
        for (JiahaoThirdFactionLineupRules.Seat seat : JiahaoThirdFactionLineupRules.seats(
                corruptCops,
                partners,
                corruptCop != null && enabled.test(corruptCop),
                enabled.test(insider),
                enabled.test(sphinxHao),
                sphinxHaoCoin,
                remainingSlots
        )) {
            roles.add(switch (seat) {
                case CORRUPT_COP -> corruptCop;
                case INSIDER -> insider;
                case SPHINX_HAO -> sphinxHao;
            });
        }
        return List.copyOf(roles);
    }

    /** NoellesRoles' Corrupt Cop by exact id; null only if it is not registered. / 按精确 id 查找黑警；仅在未注册时为 null。 */
    static @Nullable Role registeredCorruptCop(List<Role> registeredRoles) {
        for (Role role : registeredRoles) {
            if (InsiderParticipation.isCorruptCopRole(role)) {
                return role;
            }
        }
        return null;
    }
}
