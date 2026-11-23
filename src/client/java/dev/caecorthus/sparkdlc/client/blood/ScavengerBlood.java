package dev.caecorthus.sparkdlc.client.blood;

import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesHiddenBodiesBridge;
import dev.doctor4t.wathe.api.Faction;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.World;

/**
 * Reads NoellesRoles' hidden-body state for the Wathe Blood spray. NoellesRoles is a hard dependency and registers its
 * hidden-body component for every world, so the state is read directly through the compatibility bridge.
 * 为 Wathe Blood 的喷血读取 NoellesRoles 的隐藏尸体状态。NoellesRoles 是硬依赖，且为每个世界注册其隐藏尸体组件，因此经由
 * 兼容桥接直接读取。
 *
 * <p>A scavenger kill does not delete the corpse — the body entity still exists and still ticks, it is simply not shown.
 * That matters here because Wathe Blood 1.1.4 spawns its spray from the presence of a {@code PlayerBodyEntity}, so it
 * would happily paint blood next to an invisible body.
 * 清道夫击杀并不会删除尸体——尸体实体仍然存在、仍然 tick，只是不显示。这一点在这里很关键，因为 Wathe Blood 1.1.4 正是
 * 依据 {@code PlayerBodyEntity} 的出现来喷血的，它会给一具不可见的尸体旁边照喷不误。</p>
 */
public final class ScavengerBlood {
    private ScavengerBlood() {
    }

    /**
     * True when this body is hidden from the local player, meaning no blood should be sprayed beside it. NoellesRoles
     * still draws a hidden body for spectators and for killer and neutral roles, so for them the blood stays too —
     * a visible corpse with a clean floor would stand out from every other one.
     * 该尸体对本地玩家隐藏、不应在旁边喷血时返回 true。NoellesRoles 仍会为旁观者以及杀手、中立身份绘制被隐藏的尸体，因此
     * 对他们血迹同样保留——看得见的尸体配上干净的地板，反而会在所有尸体中显得突兀。
     */
    public static boolean isBodyHidden(World world, UUID owner) {
        // The world and the body's owner come from Wathe Blood's call. / 世界与尸体主人来自 Wathe Blood 的调用。
        if (world == null || owner == null) {
            return false;
        }
        try {
            return NoellesHiddenBodiesBridge.isHidden(world, owner) && !localPlayerSeesHiddenBodies(world);
        } catch (RuntimeException exception) {
            // A failing check never suppresses blood. / 检查失败时绝不抑制血迹。
            return false;
        }
    }

    /** The same exemptions NoellesRoles' {@code ScavengerBodyHideMixin} applies before hiding a body. 与 NoellesRoles 的 {@code ScavengerBodyHideMixin} 隐藏尸体前所用的豁免一致。 */
    private static boolean localPlayerSeesHiddenBodies(World world) {
        if (WatheClient.canSeeSpectatorInformation()) {
            return true;
        }
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return false;
        }
        Role role = GameWorldComponent.KEY.maybeGet(world).map(game -> game.getRole(player)).orElse(null);
        return role != null && (role.getFaction() == Faction.KILLER || role.getFaction() == Faction.NEUTRAL);
    }
}
