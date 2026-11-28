package dev.caecorthus.sparkdlc.role.civilian.seeker.hit;

import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.tablet.TabletItemLookup;
import dev.caecorthus.sparkdlc.tablet.TabletStateService;
import dev.caecorthus.sparkdlc.record.AchievementRecords;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerDeviceKind;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerRules;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerStatusComponent;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerTargeting;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

/**
 * Frozen contract: breaker mark (10 s, owner-only, newest replaces oldest) when the owner has a tablet anywhere.
 * Called only by {@code SeekerDeviceService.breakDevice} for attributable sources; car and camera breaks both mark
 * (Q8), while Taotie swallows, recalls and battery depletion never reach here. Server only. Conditions (plan §3.14):
 * a breaker other than the owner; the owner is the owner of record and alive in the round; the owner holds the
 * tablet anywhere in the inventory (the spec says "tablet"; the former car-item console fallback, since removed,
 * never counted); {@code SparkFactionApi.canAffectPlayer} lets the owner's {@code MARK_ACTION_ID} affect the
 * breaker. The mark lives in the owner-only {@code sparkdlc:seeker_status} component (decay is WP-02's tick,
 * rendering WP-05's), so no other client ever learns it; the owner is told only that a mark was applied, never the
 * breaker's name.
 * <p>Owner spec 2026-11-28: the same call also puts the breaker on the tablet's suspect list
 * ({@code TabletStateService.markSuspect}; a new suspect is announced to the police channel by name, with the kind of
 * device). That needs only a breaker other than the owner: not the owner's tablet, a living owner or the faction
 * check of the outline, and it is no outline, so the two stay independent. Recalls, battery depletion, a void fall and
 * a Taotie swallow never reach this class, so they never make a suspect.
 * <p>冻结契约：拥有者背包任意位置持有平板时标记损坏者（10 秒、仅拥有者可见、新标记替换旧标记）。仅由
 * {@code SeekerDeviceService.breakDevice} 在可归属来源下调用；小车与摄像头被打坏都会标记（Q8），饕餮吞车、回收与电量耗尽
 * 永远不会走到这里。仅服务端。条件（计划 §3.14）：损坏者不是拥有者；拥有者是记录中的拥有者且在本局存活；拥有者背包任意位置
 * 持有平板（规格写的是“平板电脑”；已移除的小车物品兜底从来不算）；{@code SparkFactionApi.canAffectPlayer} 允许拥有者的
 * {@code MARK_ACTION_ID} 影响损坏者。标记保存在仅同步给拥有者的组件中（衰减由 WP-02 负责、渲染由 WP-05 负责），
 * 因此其他客户端无从得知；拥有者只会被告知“已标记”，不会得知损坏者的名字。
 * <p>所有者规格 2026-11-28：同一次调用还会把损坏者列入平板的嫌疑人名单（{@code TabletStateService.markSuspect}；新嫌疑人会连同
 * 设备种类点名通知义警频道）。这只要求损坏者不是拥有者：不要求拥有者持有平板、拥有者存活，也不经过描边所用的阵营判定；它也不是
 * 描边，两者互相独立。回收、电量耗尽、掉出列车与饕餮吞车永远不会走到本类，因此不会产生嫌疑人。
 */
public final class SeekerMarkService {
    static final String APPLIED_MESSAGE_KEY = "message.sparkdlc.seeker.mark.applied";
    static final String SUSPECT_CAR_NOTICE_KEY = "message.sparkdlc.seeker.suspect.car";
    static final String SUSPECT_CAMERA_NOTICE_KEY = "message.sparkdlc.seeker.suspect.camera";

    private SeekerMarkService() {
    }

    public static void onDeviceBroken(ServerPlayerEntity owner, @Nullable ServerPlayerEntity breaker,
                                      SeekerDeviceKind kind) {
        if (owner == null || kind == null || owner.getWorld().isClient()) {
            return;
        }
        // Suspect first and on its own gate: it needs neither the owner's tablet nor a living owner.
        // 先处理嫌疑人，且使用独立的门槛：既不要求拥有者持有平板，也不要求拥有者存活。
        if (shouldMarkSuspect(breaker != null, breaker != null && SeekerDamageRules.isSamePlayer(owner, breaker))) {
            // The seam adds the breaker only when new, tells the police channel (action bar) and resyncs the tablets.
            // 该接口仅在损坏者尚未在名单上时加入，通知义警频道（动作栏）并重新同步平板。
            TabletStateService.markSuspect(breaker,
                    Text.translatable(suspectNoticeKey(kind), breaker.getName().getString()));
        }
        GameWorldComponent game = GameWorldComponent.KEY.get(owner.getWorld());
        boolean marks = shouldMark(
                breaker != null,
                breaker != null && SeekerDamageRules.isSamePlayer(owner, breaker),
                () -> SeekerTargeting.isOwnerOfRecord(owner),
                () -> GameFunctions.isPlayerPlayingAndAlive(owner),
                () -> TabletItemLookup.hasTabletAnywhere(owner),
                () -> SparkFactionApi.canAffectPlayer(owner, breaker, SeekerRules.MARK_ACTION_ID, game));
        if (!marks) {
            return;
        }
        SeekerStatusComponent component = SeekerStatusComponent.KEY.get(owner);
        // Newest replaces oldest: mark() overwrites the target and restarts the 10 s window. / 新标记覆盖旧标记并重新计时。
        component.apply(component.state().mark(breaker.getUuid(), SeekerRules.MARK_TICKS));
        // Chat, not the action bar, so the device-broken action-bar notice that follows does not hide it.
        // 使用聊天栏而非动作栏，避免随后的设备损坏提示将其覆盖。
        owner.sendMessage(Text.translatable(APPLIED_MESSAGE_KEY), false);
        // Achievement record W15: the breaker is marked now. / 成就记录 W15：损坏者此刻已被标记。
        AchievementRecords.seekerMark(owner, breaker, SeekerRules.MARK_TICKS);
    }

    /**
     * Pure suspect gate (owner spec 2026-11-28): every attributable break by a known breaker other than the owner. The
     * caller only reaches this class for attributable sources.
     * 纯嫌疑人判定（所有者规格 2026-11-28）：由拥有者以外的已知损坏者造成的每一次可归属损坏。调用方只在可归属来源下才会走到本类。
     */
    static boolean shouldMarkSuspect(boolean breakerPresent, boolean breakerIsOwner) {
        return breakerPresent && !breakerIsOwner;
    }

    /** Police-channel notice naming the breaker (its one argument) and the kind of device. / 点名损坏者（唯一参数）与设备种类的义警频道通知。 */
    static String suspectNoticeKey(SeekerDeviceKind kind) {
        return kind == SeekerDeviceKind.CAR ? SUSPECT_CAR_NOTICE_KEY : SUSPECT_CAMERA_NOTICE_KEY;
    }

    /** Pure gate in evaluation order. / 按求值顺序排列的纯判定。 */
    static boolean shouldMark(boolean breakerPresent, boolean breakerIsOwner, BooleanSupplier ownerOfRecord,
                              BooleanSupplier ownerAlive, BooleanSupplier ownerHasTablet,
                              BooleanSupplier factionAllows) {
        return breakerPresent
                && !breakerIsOwner
                && ownerOfRecord.getAsBoolean()
                && ownerAlive.getAsBoolean()
                && ownerHasTablet.getAsBoolean()
                && factionAllows.getAsBoolean();
    }
}
