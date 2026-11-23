package dev.caecorthus.sparkdlc.compat.noellesroles;

import net.minecraft.world.World;
import org.agmas.noellesroles.scavenger.HiddenBodiesWorldComponent;

import java.util.UUID;

/**
 * Isolates NoellesRoles' scavenger body-visibility storage behind the mod's compatibility boundary.
 * 将 NoellesRoles 清道夫的尸体可见性存储隔离在本模组的兼容边界之后。
 */
public final class NoellesHiddenBodiesBridge {
    private NoellesHiddenBodiesBridge() {
    }

    public static boolean isHidden(World world, UUID bodyOwnerUuid) {
        return HiddenBodiesWorldComponent.KEY.get(world).isHidden(bodyOwnerUuid);
    }
}
