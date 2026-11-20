package dev.caecorthus.sparkdlc.round.end;

import com.mojang.authlib.GameProfile;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Builds Wathe round-end rows the way Wathe's own {@code setRoundEndData} does, with the winner rule as a parameter.
 * Two writers replace Wathe's rows: the custom faction win (from custom faction round-end state) and, when there is
 * none, the traits' effective-team win; each used to carry its own copy of this loop. Part of the core's contract.
 * 按 Wathe 自身 {@code setRoundEndData} 的方式构造 Wathe 结算行，胜负规则作为参数。有两个写入方替换 Wathe 的结算行：自定义阵营
 * 胜利（根据自定义阵营结算状态）以及在没有自定义胜利时的词条有效阵营胜利；二者原先各带一份这段循环。属于核心契约。
 */
public final class RoundEndRows {
    private RoundEndRows() {
    }

    /**
     * The rows as a new list (the custom faction win adds them to Wathe's list in one step).
     * 以新列表返回结算行（自定义阵营胜利一次性加入 Wathe 的列表）。
     */
    public static List<GameRoundEndComponent.RoundEndData> rows(
            ServerWorld serverWorld,
            GameWorldComponent game,
            BiPredicate<UUID, Role> didWin
    ) {
        List<GameRoundEndComponent.RoundEndData> rows = new ArrayList<>();
        appendRows(rows, serverWorld, game, didWin);
        return rows;
    }

    /**
     * Appends the rows to {@code into} one by one (the traits' effective win writes straight into Wathe's list).
     * 将结算行逐行追加到 {@code into}（词条有效胜利直接写入 Wathe 的列表）。
     */
    public static void appendRows(
            List<GameRoundEndComponent.RoundEndData> into,
            ServerWorld serverWorld,
            GameWorldComponent game,
            BiPredicate<UUID, Role> didWin
    ) {
        appendRows(
                into,
                game.getRoles(),
                game.getGameProfiles(),
                game::isPlayerDead,
                uuid -> serverWorld.getPlayerByUuid(uuid) != null,
                didWin
        );
    }

    public static @Nullable Identifier gameModeId(GameWorldComponent game) {
        GameMode gameMode = game.getGameMode();
        return gameMode != null ? gameMode.identifier : null;
    }

    /**
     * One row per player with a profile and a role, in role-map order; per row the death, then the presence, then the
     * winner rule is asked, as Wathe does.
     * 每个有档案且有身份的玩家一行，按身份表顺序；每行依次询问死亡、在线、胜负规则，与 Wathe 相同。
     */
    static void appendRows(
            List<GameRoundEndComponent.RoundEndData> into,
            Map<UUID, Role> roles,
            Map<UUID, GameProfile> profiles,
            Predicate<UUID> isDead,
            Predicate<UUID> isOnline,
            BiPredicate<UUID, Role> didWin
    ) {
        for (Map.Entry<UUID, Role> entry : roles.entrySet()) {
            UUID uuid = entry.getKey();
            Role role = entry.getValue();
            GameProfile profile = profiles.get(uuid);
            if (profile == null || role == WatheRoles.NO_ROLE) {
                continue;
            }

            into.add(new GameRoundEndComponent.RoundEndData(
                    profile,
                    role.identifier(),
                    endStatus(isDead.test(uuid), isOnline.test(uuid)),
                    didWin.test(uuid, role)
            ));
        }
    }

    static GameRoundEndComponent.PlayerEndStatus endStatus(boolean wasDead, boolean isOnline) {
        if (wasDead) {
            return isOnline
                    ? GameRoundEndComponent.PlayerEndStatus.DEAD
                    : GameRoundEndComponent.PlayerEndStatus.LEFT_DEAD;
        }
        return isOnline
                ? GameRoundEndComponent.PlayerEndStatus.ALIVE
                : GameRoundEndComponent.PlayerEndStatus.LEFT;
    }
}
