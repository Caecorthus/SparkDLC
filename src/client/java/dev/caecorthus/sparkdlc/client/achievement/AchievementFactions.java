package dev.caecorthus.sparkdlc.client.achievement;

import dev.caecorthus.sparkdlc.achievement.Factions;
import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.component.FactionRoundEndComponent;
import dev.doctor4t.wathe.api.Faction;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Identifier;

/**
 * The faction ids achievements are counted under, read from the faction contract so witch and other custom factions
 * are named correctly. When the contract has no faction for a player or role, falls back to Wathe's own faction of
 * the role, named by {@link Factions#fromWatheName} ({@code wathe:civilian}, and {@code sparkdlc:witch} /
 * {@code sparkdlc:jiahao} for the native custom constants appended to Wathe's enum).
 * 成就统计所用的阵营 id，取自阵营契约，使魔女等自定义阵营得到正确的 id。契约对某个玩家或身份没有阵营时，回退到 Wathe 给该
 * 身份的阵营，由 {@link Factions#fromWatheName} 命名（{@code wathe:civilian} 等；追加到 Wathe 枚举的原生自定义常量为
 * {@code sparkdlc:witch} / {@code sparkdlc:jiahao}）。
 */
final class AchievementFactions {
    private AchievementFactions() {
    }

    /** The player's current faction id, or null without a role. 玩家当前的阵营 id，没有角色时为 null。 */
    static String faction(PlayerEntity player, GameWorldComponent game, Role role) {
        if (role == null) {
            return null;
        }
        String id = idOrNull(SparkFactionApi.resolveEffectiveFaction(player, game));
        return id != null ? id : watheFaction(role);
    }

    /**
     * The base faction of a role id (no player context, so no trait flips), or null when this client does not know
     * the role. The faction contract names custom factions such as {@code sparkdlc:witch}; a role it has no faction for
     * uses Wathe's base faction.
     * 身份 id 的基础阵营（没有玩家上下文，因此不含词条翻转）；本客户端不认识该身份时为 null。
     * 阵营契约能给出 {@code sparkdlc:witch} 等自定义阵营；它没有给出阵营的身份使用 Wathe 的基础阵营。
     */
    static String baseFaction(String roleId) {
        Identifier id = roleId == null ? null : Identifier.tryParse(roleId);
        Role role = id == null ? null : WatheRoles.getRole(id);
        if (role == null) {
            return null;
        }
        String factionId = idOrNull(SparkFactionApi.resolveBaseFaction(role));
        return factionId != null ? factionId : watheFaction(role);
    }

    /**
     * The custom winning faction {@code FactionRoundEndComponent} recorded for the last round, or null when the round
     * ended with a plain Wathe win.
     * {@code FactionRoundEndComponent} 记录的上一局自定义获胜阵营；普通 Wathe 胜利时为 null。
     */
    static String customWinningFaction(Scoreboard scoreboard) {
        FactionRoundEndComponent component = FactionRoundEndComponent.KEY.getNullable(scoreboard);
        return component == null ? null : idOrNull(component.getWinningFaction());
    }

    /**
     * Wathe's own faction of {@code role}: built-ins as {@code wathe:*}, the native constants the faction core appends
     * by their declared ids (see {@link Factions#fromWatheName}).
     * Wathe 给 {@code role} 的阵营：内置阵营为 {@code wathe:*}，阵营核心追加的原生常量取其声明的 id。
     */
    private static String watheFaction(Role role) {
        return Factions.fromWatheName(Faction.fromRole(role).name());
    }

    private static String idOrNull(Identifier faction) {
        return faction == null ? null : faction.toString();
    }
}
