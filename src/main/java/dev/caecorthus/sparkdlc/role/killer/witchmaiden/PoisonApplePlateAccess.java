package dev.caecorthus.sparkdlc.role.killer.witchmaiden;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/**
 * Stable role-owned bridge implemented only by the Wathe platter block entity mixin.
 * 仅由 Wathe 餐盘方块实体 mixin 实现的巫女角色私有桥接。
 */
public interface PoisonApplePlateAccess {
    boolean sparkdlc$isPoisonAppleArmed();

    boolean sparkdlc$armPoisonApple(UUID placerUuid, UUID matchUuid);

    @Nullable UUID sparkdlc$recordSuccessfulTake(UUID matchUuid);

    /**
     * The player who armed this platter's Poison Apple (server state only), or {@code null} when none is armed.
     * 布置此餐盘毒苹果的玩家（仅服务端状态）；未布置时为 {@code null}。
     */
    @Nullable UUID sparkdlc$poisonApplePlacer();

    void sparkdlc$clearPoisonApple();

    void sparkdlc$clearIfMatchChanged(@Nullable UUID matchUuid);
}
