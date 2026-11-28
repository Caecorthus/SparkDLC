package dev.caecorthus.sparkdlc.role.civilian.seeker.device;

import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerState;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerStatusComponent;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerTargeting;
import dev.caecorthus.sparkdlc.tablet.net.TabletSnapshot;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * The cameras of the round as every police-channel viewer sees them (owner spec 2026-11-28: every player who can
 * connect to the police channel can use the monitors). Server only, read-only: a camera still belongs to the Seeker who
 * placed it, lives in that Seeker's {@code sparkdlc:seeker_status} and disappears with its owner; this class only
 * answers "which cameras exist right now" for somebody else. Who may watch is {@code SeekerTargeting.canWatchCameras};
 * nothing here checks the viewer.
 * 义警频道的每名观看者所看到的本局摄像头（所有者规格 2026-11-28：能连接义警频道的每名玩家都可以使用监控）。仅服务端、只读：
 * 摄像头仍属于放置它的搜寻者，登记在该搜寻者的 {@code sparkdlc:seeker_status} 中，并随其拥有者一起消失；本类只为其他人回答
 * “此刻有哪些摄像头”。谁可以观看由 {@code SeekerTargeting.canWatchCameras} 决定；这里不检查观看者。
 */
public final class SeekerSharedCameras {
    private SeekerSharedCameras() {
    }

    /**
     * One live camera of the round.
     * 本局的一台存活摄像头。
     *
     * @param entityId    the camera entity / 摄像头实体 id
     * @param label       its owner's per-match label ("CAM 01") / 其拥有者的本局编号
     * @param owner       the Seeker who placed it / 放置它的搜寻者
     * @param ownerNumber 1-based number of that Seeker among the Seekers who own a camera, by UUID, the same for every
     *                    viewer; it tells two Seekers' cameras apart when the name may not be shown /
     *                    该搜寻者在拥有摄像头的搜寻者中按 UUID 排序的序号（从 1 开始），对所有观看者相同；
     *                    名字不可显示时用它区分两名搜寻者的摄像头
     */
    public record Entry(int entityId, int label, ServerPlayerEntity owner, int ownerNumber) {
    }

    /**
     * The camera with that id if {@code viewer} could watch it now: a live camera entity in the viewer's world that
     * its owner of record still lists (the same reference the camera's own self-check needs). Null otherwise.
     * 该 id 的摄像头，前提是 {@code viewer} 此刻可以观看它：位于观看者所在世界的存活摄像头实体，且其记录中的拥有者仍登记着它
     * （与摄像头自检所要求的引用相同）。否则为 null。
     */
    @Nullable
    public static SeekerCameraEntity find(@Nullable ServerPlayerEntity viewer, int entityId) {
        if (viewer == null || entityId < 0) {
            return null;
        }
        Entity entity = viewer.getServerWorld().getEntityById(entityId);
        if (!(entity instanceof SeekerCameraEntity camera) || camera.isRemoved() || !camera.isAlive()) {
            return null;
        }
        ServerPlayerEntity owner = SeekerDeviceService.findOwner(camera);
        return owner != null && SeekerTargeting.isOwnerOfRecord(owner)
                && SeekerStatusComponent.KEY.get(owner).hasCamera(entityId) ? camera : null;
    }

    /**
     * Whether {@link #list} would hold at least one camera, without building it: the tablet grants its camera section
     * only then ({@code TabletChannelResolver.features}).
     * {@link #list} 是否至少包含一台摄像头（不构建列表）：平板只在此时授予摄像头分区（{@code TabletChannelResolver.features}）。
     */
    public static boolean hasAny(@Nullable ServerPlayerEntity viewer) {
        MinecraftServer server = viewer == null ? null : viewer.getServer();
        if (server == null) {
            return false;
        }
        for (ServerPlayerEntity owner : server.getPlayerManager().getPlayerList()) {
            SeekerStatusComponent status = SeekerStatusComponent.KEY.get(owner);
            if (status.cameraCount() == 0 || !SeekerTargeting.isOwnerOfRecord(owner)) {
                continue;
            }
            for (SeekerState.Camera camera : status.cameras()) {
                SeekerCameraEntity entity = SeekerDeviceService.findCamera(owner, camera.entityId());
                if (entity != null && entity.isAlive() && entity.getWorld() == viewer.getWorld()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Every camera {@code viewer} could watch now, in viewing order: the viewer's own cameras first, then the other
     * Seekers' by owner UUID, each owner's cameras by label. The untargeted open, the A/D cycle and the tablet's list
     * all use this one order.
     * {@code viewer} 此刻可以观看的全部摄像头，按观看顺序排列：先是观看者自己的摄像头，再按拥有者 UUID 排列其他搜寻者的，
     * 每名拥有者的摄像头按编号排列。未指定目标的打开、A/D 切换与平板列表都使用这一个顺序。
     */
    public static List<Entry> list(@Nullable ServerPlayerEntity viewer) {
        MinecraftServer server = viewer == null ? null : viewer.getServer();
        if (server == null) {
            return List.of();
        }
        List<ServerPlayerEntity> owners = new ArrayList<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (SeekerStatusComponent.KEY.get(player).cameraCount() > 0 && SeekerTargeting.isOwnerOfRecord(player)) {
                owners.add(player);
            }
        }
        if (owners.isEmpty()) {
            return List.of();
        }
        owners.sort(Comparator.comparing(Entity::getUuid));
        List<Entry> entries = new ArrayList<>();
        int own = owners.indexOf(viewer);
        if (own >= 0) {
            collect(viewer, viewer, own + 1, entries);
        }
        for (int index = 0; index < owners.size(); index++) {
            if (index != own) {
                collect(viewer, owners.get(index), index + 1, entries);
            }
        }
        return entries;
    }

    /**
     * {@link #list} as the label-ordered list the pure {@code SeekerCameraRules} work on: the label of each element is
     * its 1-based viewing position, because two Seekers' own labels collide.
     * 把 {@link #list} 转成纯规则 {@code SeekerCameraRules} 所用的按编号排序的列表：每个元素的编号是其从 1 开始的观看位置，
     * 因为两名搜寻者各自的编号会重复。
     */
    public static List<SeekerState.Camera> viewingOrder(@Nullable ServerPlayerEntity viewer) {
        List<Entry> entries = list(viewer);
        List<SeekerState.Camera> order = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            order.add(new SeekerState.Camera(entry.entityId(), order.size() + 1));
        }
        return order;
    }

    /**
     * The tablet's camera rows for one viewer holding the camera section, in {@link #list} order. A row never carries
     * the owner's UUID; it carries the owner's name only when {@code revealsOwner} allows it (the police channel's own
     * identity rule, so a viewer who has not unlocked the police member list learns no name here either).
     * 持有摄像头分区的某名观看者的平板摄像头行，按 {@link #list} 的顺序。行中从不携带拥有者的 UUID；只有 {@code revealsOwner}
     * 允许时才携带拥有者的名字（即义警频道自身的身份规则，因此尚未解锁义警成员列表的观看者在这里同样得不到名字）。
     */
    public static List<TabletSnapshot.CameraRow> tabletRows(ServerPlayerEntity viewer, Predicate<UUID> revealsOwner) {
        List<Entry> entries = list(viewer);
        if (entries.isEmpty()) {
            return List.of();
        }
        List<TabletSnapshot.CameraRow> rows = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            ServerPlayerEntity owner = entry.owner();
            rows.add(new TabletSnapshot.CameraRow(entry.entityId(), entry.label(), entry.ownerNumber(),
                    revealsOwner.test(owner.getUuid()) ? owner.getName().getString() : ""));
        }
        return rows;
    }

    private static void collect(ServerPlayerEntity viewer, ServerPlayerEntity owner, int ownerNumber,
                                List<Entry> entries) {
        for (SeekerState.Camera camera : SeekerStatusComponent.KEY.get(owner).cameras()) {
            SeekerCameraEntity entity = SeekerDeviceService.findCamera(owner, camera.entityId());
            if (entity != null && entity.isAlive() && entity.getWorld() == viewer.getWorld()) {
                entries.add(new Entry(camera.entityId(), camera.label(), owner, ownerNumber));
            }
        }
    }
}
