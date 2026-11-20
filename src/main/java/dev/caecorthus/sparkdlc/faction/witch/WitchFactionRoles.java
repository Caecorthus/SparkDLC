package dev.caecorthus.sparkdlc.faction.witch;

import dev.caecorthus.sparkdlc.SparkWitchFactions;
import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.doctor4t.wathe.api.Role;
import org.jetbrains.annotations.Nullable;

/**
 * Role-only membership of the witch faction, for callers that have no player at hand (tablet networks, blackout
 * teams). / 仅由身份决定的魔女阵营归属，供手头没有玩家的调用方使用（平板网络、熄灯队伍）。
 */
public final class WitchFactionRoles {
    private WitchFactionRoles() {
    }

    /** The witch faction is declared through the faction contract, so its base faction id identifies every witch
     *  role (Grand Witch, Accomplice, Curser, ...). Role-only, so client and server agree; false for a null role.
     *  魔女阵营经阵营契约声明，因此基础阵营 id 即可识别所有魔女身份（大魔女、共犯、诅咒者……）。只看身份，客户端与服务端结果
     *  一致；身份为 null 时返回 false。 */
    public static boolean isWitchFactionRole(@Nullable Role role) {
        return role != null && SparkWitchFactions.WITCH.equals(SparkFactionApi.resolveBaseFaction(role));
    }
}
