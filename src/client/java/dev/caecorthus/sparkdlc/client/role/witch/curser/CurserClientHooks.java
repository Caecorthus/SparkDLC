package dev.caecorthus.sparkdlc.client.role.witch.curser;

import dev.caecorthus.sparkdlc.SparkWitchRoles;
import dev.caecorthus.sparkdlc.client.render.LocalViewerSnapshot;
import dev.caecorthus.sparkdlc.client.render.WraithClientState;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.caecorthus.sparkdlc.role.witch.curser.CurserPlayerComponent;
import dev.caecorthus.sparkdlc.role.witch.curser.CurserRules;
import dev.caecorthus.sparkdlc.role.witch.curser.UseCurserAbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/** Client sender for the shared ability key; authority stays in CurserFeatureService. / 共用技能键的客户端发送端；权威仍在 CurserFeatureService。 */
public final class CurserClientHooks {
    private CurserClientHooks() {
    }

    public static boolean canUse(ClientPlayerEntity player) {
        if (player == null) {
            return false;
        }
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
        var role = game.getRole(player);
        return CurserRules.canSend(
                SparkDlcServerConnection.isConfirmedServer(),
                game.isRunning(),
                WraithClientState.isActive(player),
                WraithClientState.isPromoted(player),
                role != null && SparkWitchRoles.CURSER_ID.equals(role.identifier()),
                CurserPlayerComponent.KEY.get(player).getCooldownTicks(),
                ClientPlayNetworking.canSend(UseCurserAbilityC2SPacket.ID)
        );
    }

    public static void use() {
        if (ClientPlayNetworking.canSend(UseCurserAbilityC2SPacket.ID)) {
            ClientPlayNetworking.send(new UseCurserAbilityC2SPacket());
        }
    }

    /**
     * Viewer-only and asked by both instinct gates for every rendered entity, so it is memoized per world pass
     * ({@link LocalViewerSnapshot}). / 只与观察者有关，且两个本能入口会对每个渲染实体询问，因此按世界 pass 缓存。
     */
    public static boolean canUseInstinct() {
        return LocalViewerSnapshot.localFact(LocalViewerSnapshot.Fact.CURSER_CAN_USE_INSTINCT,
                CurserClientHooks::evaluateCanUseInstinct);
    }

    private static boolean evaluateCanUseInstinct() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (!SparkDlcServerConnection.isConfirmedServer() || player == null) {
            return false;
        }
        CurserPlayerComponent curser = CurserPlayerComponent.KEY.get(player);
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
        var role = game.getRole(player);
        return CurserRules.canUseInstinct(
                game.isRunning(),
                WraithClientState.isActive(player),
                WraithClientState.isPromoted(player),
                role != null && SparkWitchRoles.CURSER_ID.equals(role.identifier()),
                curser.isConfused()
        );
    }

    /** Memoized per world pass like {@link #canUseInstinct}. / 与 {@link #canUseInstinct} 一样按世界 pass 缓存。 */
    public static boolean isLocallyConfused() {
        return LocalViewerSnapshot.localFact(LocalViewerSnapshot.Fact.CURSER_LOCALLY_CONFUSED,
                CurserClientHooks::evaluateLocallyConfused);
    }

    private static boolean evaluateLocallyConfused() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return SparkDlcServerConnection.isConfirmedServer()
                && player != null
                && CurserPlayerComponent.KEY.get(player).isConfused();
    }
}
