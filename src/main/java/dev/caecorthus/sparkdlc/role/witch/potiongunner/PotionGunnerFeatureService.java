package dev.caecorthus.sparkdlc.role.witch.potiongunner;

import dev.caecorthus.sparkdlc.SparkDlcRoles;
import dev.caecorthus.sparkdlc.role.witch.accomplice.variant.AccompliceVariantHooks;
import dev.caecorthus.sparkdlc.role.witch.accomplice.variant.AccompliceVariants;
import dev.caecorthus.sparkdlc.role.witch.potiongunner.shell.effect.PotionShellBurn;
import dev.doctor4t.wathe.api.event.RoleAssigned;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Potion Gunner registration entry point (called once from RoleEvents, after roles are registered). It joins
 * the special-accomplice pool, so a Bewitched promotion may roll it at most once per round; the launcher is granted
 * from {@code RoleAssigned} and again, idempotently, once the promotion commits.
 * 药炮手注册入口（由 RoleEvents 在职业注册后调用一次）。它加入特殊共犯池，魔化使晋升每局最多抽到一次；炮筒在
 * {@code RoleAssigned} 时发放，并在晋升提交后再幂等地确保一次。
 */
public final class PotionGunnerFeatureService {
    private static boolean registered;

    private PotionGunnerFeatureService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        PotionGunnerShopService.register();
        PotionGunnerLifecycle.register();
        PotionShellBurn.register();
        AccompliceVariants.register(SparkDlcRoles.potionGunner(), new AccompliceVariantHooks() {
            @Override
            public void afterPromotionCommitted(ServerPlayerEntity player) {
                PotionGunnerLoadoutService.ensureLauncher(player);
            }
        });
        RoleAssigned.EVENT.register((player, role) -> {
            if (player instanceof ServerPlayerEntity serverPlayer && PotionGunnerRules.isPotionGunner(role)) {
                PotionGunnerLoadoutService.ensureLauncher(serverPlayer);
            }
        });
    }
}
