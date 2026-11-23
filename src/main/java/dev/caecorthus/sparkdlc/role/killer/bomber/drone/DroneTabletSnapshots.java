package dev.caecorthus.sparkdlc.role.killer.bomber.drone;

import dev.caecorthus.sparkdlc.entity.DroneEntity;
import dev.caecorthus.sparkdlc.item.DroneItem;
import dev.caecorthus.sparkdlc.tablet.net.TabletSnapshot;
import dev.caecorthus.sparkdlc.tablet.TabletRules;
import dev.caecorthus.sparkdlc.util.ExactItemCooldowns;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * The tablet's drone page for one viewer who holds the drone feature: one row per drone of the viewer's own.
 * 拥有无人机功能的查看者看到的平板无人机页：查看者自己的每架无人机一行。
 */
public final class DroneTabletSnapshots {
    private DroneTabletSnapshots() {
    }

    /**
     * The viewer's own drones: carried drone items first (inventory order), then grenade drones queued to come back
     * (hotbar full or viewer out of survival; item rows with their cooldown), then its placed drones in this world
     * (entity id order). Rows never describe another player's drones, and owner identity never leaves the server.
     * 查看者自己的无人机：先列随身携带的无人机物品（按物品栏顺序），再列排队等待归还的投弹无人机（快捷栏已满或查看者暂不在生存模式；
     * 以物品行显示并带冷却），最后列其在本世界已放置的无人机（按实体 id）。绝不包含他人的无人机，主人身份也不会离开服务器。
     */
    public static List<TabletSnapshot.DroneRow> rows(ServerPlayerEntity viewer) {
        List<TabletSnapshot.DroneRow> rows = new ArrayList<>();
        PlayerInventory inventory = viewer.getInventory();
        ItemCooldownManager cooldowns = viewer.getItemCooldownManager();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!(stack.getItem() instanceof DroneItem item)) {
                continue;
            }
            rows.add(new TabletSnapshot.DroneRow(
                    -1,
                    item.kind().wire(),
                    TabletSnapshot.DroneRow.STATUS_ITEM,
                    DroneRules.percent(DroneItem.charge(stack)),
                    item.kind() == DroneKind.GRENADE && DroneItem.hasPayload(stack),
                    TabletRules.secondsCeil(ExactItemCooldowns.remainingTicks(cooldowns, item)),
                    -1
            ));
        }
        UUID viewerUuid = viewer.getUuid();
        for (DroneService.PendingReturnStatus pending : DroneService.pendingReturns(viewer.getServerWorld(), viewerUuid)) {
            rows.add(new TabletSnapshot.DroneRow(
                    -1,
                    DroneKind.GRENADE.wire(),
                    TabletSnapshot.DroneRow.STATUS_ITEM,
                    DroneRules.percent(pending.charge()),
                    false,
                    TabletRules.secondsCeil(pending.cooldownTicks()),
                    -1
            ));
        }
        DroneService.dronesOf(viewer.getServerWorld(), viewerUuid).stream()
                // Defensive owner re-check: the registry contract already scopes to this owner.
                // 防御性复核主人：登记表契约本已限定为该主人。
                .filter(drone -> !drone.isRemoved() && viewerUuid.equals(drone.ownerUuid()))
                .sorted(Comparator.comparingInt(DroneEntity::getId))
                .forEach(drone -> rows.add(new TabletSnapshot.DroneRow(
                        drone.getId(),
                        drone.kind().wire(),
                        viewerUuid.equals(drone.pilotUuid()) ? TabletSnapshot.DroneRow.STATUS_PILOTING : statusWire(drone.state()),
                        DroneRules.percent(drone.charge()),
                        drone.hasPayload(),
                        0,
                        MathHelper.floor(viewer.distanceTo(drone))
                )));
        return rows;
    }

    private static int statusWire(DroneState state) {
        return switch (state) {
            case GROUNDED -> TabletSnapshot.DroneRow.STATUS_GROUNDED;
            case HOVERING -> TabletSnapshot.DroneRow.STATUS_HOVERING;
            case FLYING -> TabletSnapshot.DroneRow.STATUS_FLYING;
            case FALLING -> TabletSnapshot.DroneRow.STATUS_FALLING;
        };
    }
}
