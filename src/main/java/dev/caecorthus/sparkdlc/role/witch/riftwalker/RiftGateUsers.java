package dev.caecorthus.sparkdlc.role.witch.riftwalker;

import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.SparkDlcFactions;
import dev.caecorthus.sparkdlc.SparkDlcRoles;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Side-neutral helper (the server's entry check and R1's client crosshair use it) that feeds {@link RiftGateUser#classify}
 * from live/synced state: the RAW Wathe role
 * ({@code GameWorldComponent#getRole}, never the Black Raven acting role) and the effective faction
 * (an unknown or null faction counts as NOT witch faction). Classification only: liveness, participation, stun and
 * other entry gates belong to the caller.
 * 两端通用的辅助（服务端进门检查与 R1 客户端准星都使用）：以实时/同步状态为 {@link RiftGateUser#classify} 提供输入——原始 Wathe 职业（{@code GameWorldComponent#getRole}，
 * 从不使用黑羽鸦伪装职业）与有效阵营（未知或 null 视为非魔女阵营）。只负责分类：存活、参赛、眩晕等
 * 进门门槛由调用方负责。
 */
public final class RiftGateUsers {
    private RiftGateUsers() {
    }

    public static RiftGateUser classify(PlayerEntity player) {
        if (player == null) {
            return RiftGateUser.NONE;
        }
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
        Role role = game.getRole(player);
        if (role == null) {
            return RiftGateUser.NONE;
        }
        return RiftGateUser.classify(
                RiftwalkerRules.isRiftwalker(role),
                isWitchFaction(player, game),
                role == SparkDlcRoles.murderousWitch(),
                role == SparkDlcRoles.apprenticeWitch());
    }

    /**
     * Effective faction is exactly {@code sparkdlc:witch} (C6: the Sabbath pulls only these; the Apprentice and
     * Murderous Witches are not same faction). Unknown → false.
     * 有效阵营恰为 {@code sparkdlc:witch}（C6：魔女集会只召集这些人；预备魔女与杀意魔女不算同阵营）。未知 → false。
     */
    public static boolean isWitchFaction(PlayerEntity player) {
        return player != null && isWitchFaction(player, GameWorldComponent.KEY.get(player.getWorld()));
    }

    private static boolean isWitchFaction(PlayerEntity player, GameWorldComponent game) {
        Identifier faction = SparkFactionApi.resolveEffectiveFaction(player, game);
        return SparkDlcFactions.WITCH.equals(faction);
    }
}
