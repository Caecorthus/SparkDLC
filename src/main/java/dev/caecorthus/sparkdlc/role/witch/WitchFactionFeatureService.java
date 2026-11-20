package dev.caecorthus.sparkdlc.role.witch;

import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import dev.caecorthus.sparkdlc.role.witch.accomplice.shop.AccompliceShopService;
import dev.caecorthus.sparkdlc.role.witch.grandwitch.GrandWitchActiveSkillService;
import dev.caecorthus.sparkdlc.role.witch.grandwitch.GrandWitchShopReplayService;
import dev.caecorthus.sparkdlc.role.witch.grandwitch.GrandWitchShopService;
import dev.doctor4t.wathe.api.Role;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Facade for Witch faction registration and role-assignment side effects.
 * 魔女阵营注册和身份分配副作用的门面，具体策略由独立 policy 承担。
 */
public final class WitchFactionFeatureService {
    private static boolean registered;

    private WitchFactionFeatureService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        SparkFactionApi.registerEconomyPolicy(WitchFactionEconomyPolicy::economyDecision);
        SparkFactionApi.registerInstinctPolicy((viewer, target, gameComponent) ->
                WitchInstinctPolicy.instinctHighlight(viewer, target, gameComponent,
                        SparkDlcServerConnection.isConfirmedServer()));
        WitchFactionProtectionPolicy.register();
        // Delay this global Wathe replacement until server startup, after all mod initializers.
        // 将这个 Wathe 全局替换延迟到服务端启动阶段，确保所有模组初始化器已经完成。
        ServerLifecycleEvents.SERVER_STARTING.register(server -> GrandWitchShopReplayService.register());
        GrandWitchShopService.register();
        AccompliceShopService.register();
    }

    public static void assignForRole(ServerPlayerEntity player, Role role) {
        WitchFactionEconomyPolicy.assignStartingLoadout(player, role);
        if (!WitchFactionRules.isGrandWitch(role)) {
            GrandWitchActiveSkillService.clearCeremonialSword(player, false);
        }
    }

    public static void clearPlayerRuntime(ServerPlayerEntity player) {
        GrandWitchActiveSkillService.clearCeremonialSword(player, false);
    }
}
