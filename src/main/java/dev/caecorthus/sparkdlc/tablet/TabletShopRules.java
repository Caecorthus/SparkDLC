package dev.caecorthus.sparkdlc.tablet;

import dev.caecorthus.sparkdlc.faction.api.PoliceRoles;
import dev.caecorthus.sparkdlc.role.jiahao.corruptcop.CorruptCopRules;
import dev.caecorthus.sparkdlc.role.civilian.veteran.VeteranRules;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Pure grant, police-network and highlight rules for the tablet item. The tablet is never sold: it is
 * granted free to every tablet-eligible player (the class name is historical).
 * 平板物品的纯发放、义警网络与高亮规则。平板从不出售，而是免费发放给每名符合条件的玩家（类名沿用旧称）。
 */
public final class TabletShopRules {
    public static final Identifier VIGILANTE_ID = Identifier.of("wathe", "vigilante");
    /**
     * Mid-round grant reconciliation cadence while the round is ACTIVE. / 对局 ACTIVE 期间局中补发对账的间隔。
     */
    public static final int GRANT_RECONCILE_INTERVAL_TICKS = 20;
    public static final String POLICE_GRANTED_KEY = "message.sparkdlc.tablet.police_granted";
    public static final String KILLER_GRANTED_KEY = "message.sparkdlc.tablet.killer_granted";
    public static final String WITCH_GRANTED_KEY = "message.sparkdlc.tablet.witch_granted";
    public static final String IMPOSTOR_GRANTED_KEY = "message.sparkdlc.tablet.impostor_granted";
    public static final String UNDERCOVER_GRANTED_KEY = "message.sparkdlc.tablet.undercover_granted";
    public static final String ATTENDANT_GRANTED_KEY = "message.sparkdlc.tablet.attendant_granted";
    public static final String BOMBER_GRANTED_KEY = "message.sparkdlc.tablet.bomber_granted";
    public static final String PUBLIC_GRANTED_KEY = "message.sparkdlc.tablet.public_granted";
    /** Police-network suspect highlight; tablets never outline channel members. / 义警网络嫌疑人高亮色；平板从不描边频道成员。 */
    public static final int SUSPECT_HIGHLIGHT_COLOR = 0xFF8C00;

    private TabletShopRules() {
    }

    /**
     * Police-network role ({@code TabletChannelRules.Facts#policeRole}): the faction core's {@code PoliceRoles} minus
     * Veteran, with Corrupt Cop registered by {@code TabletShopService#register}.
     * 义警网络身份（Facts#policeRole）：阵营核心的 PoliceRoles 去掉老兵；黑警由 TabletShopService#register 注册。
     */
    public static boolean isPoliceNetworkRole(@Nullable Role role) {
        if (role == null || VeteranRules.isVeteran(role)) {
            return false;
        }
        // Idempotent: Corrupt Cop is registered before the first classification, whatever the initialization order.
        // 幂等：无论初始化顺序如何，黑警都会在首次分类前完成注册。
        registerCorruptCopPoliceRole();
        // The police registry decides, and it is read on every call so registrations made later remain visible.
        // Never infer police from gear/faction.
        // 由警职注册表决定；每次调用都读取注册表，确保稍后注册的身份立即可见。绝不通过装备或阵营推断警职。
        return PoliceRoles.contains(role.identifier());
    }

    /** Police tablet-network classification only: this does not change NoellesRoles allocation or faction.
     *  仅注册义警平板网络分类，不改变 NoellesRoles 的身份分配或阵营。 */
    static void registerCorruptCopPoliceRole() {
        PoliceRoles.register(CorruptCopRules.CORRUPT_COP_ID);
    }

    public static boolean isVigilante(@Nullable Role role) {
        return role != null && VIGILANTE_ID.equals(role.identifier());
    }

    /**
     * Tablet eligibility: a real round role that belongs to at least one tablet network
     * ({@code TabletChannelResolver.identityChannels} non-empty, which since the public channel is every round role),
     * or a real role whose tablet feature needs no channel:
     * the Attendant (door monitor) or the Bomber (drone directory; a Bomber with the Conscience trait has no channel).
     * Disguises never count: a Coroner's Attendant disguise is lent a temporary tablet instead, and a Coroner's Bomber
     * disguise gets no drones at all.
     * 平板资格：拥有有效局内身份，且属于至少一个平板网络（identityChannels 非空；有了公共频道后即所有局内身份），或真实身份的平板功能无需频道：
     * 乘务员（房门监控）或炸弹客（无人机目录；带善良天赋的炸弹客没有频道）。伪装不计入：验尸官的乘务员伪装改为借出临时平板，
     * 验尸官的炸弹客伪装没有无人机。
     *
     * @param hasRole            {@code TabletChannelRules.Facts#hasRole} / 拥有有效局内身份
     * @param hasIdentityChannel identity channel set is non-empty / 身份频道集非空
     * @param realAttendant      real round role is Attendant / 真实局内身份为乘务员
     * @param realBomber         real round role is Bomber / 真实局内身份为炸弹客
     */
    public static boolean isTabletEligible(
            boolean hasRole, boolean hasIdentityChannel, boolean realAttendant, boolean realBomber
    ) {
        return hasRole && (hasIdentityChannel || realAttendant || realBomber);
    }

    /**
     * Private chat lines sent with a granted tablet: the public-channel line, one for the faction networks (Undercover
     * keeps its own killer line), then the Attendant's door-monitor line, then the Bomber's drone line. Empty for an
     * ineligible player.
     * 发放平板时发送的私聊提示：先发公共频道提示，再发阵营频道提示（卧底保留其专属的杀手频道提示），然后是乘务员房门监控提示，
     * 最后是炸弹客无人机提示；无资格者为空。
     */
    public static List<String> grantMessageKeys(
            @Nullable Set<TabletChannel> channels,
            boolean undercover,
            boolean realAttendant,
            boolean realBomber
    ) {
        List<String> keys = new ArrayList<>(4);
        if (channels != null && channels.contains(TabletChannel.PUBLIC)) {
            keys.add(PUBLIC_GRANTED_KEY);
        }
        String channelKey = channelGrantMessageKey(channels, undercover);
        if (channelKey != null) {
            keys.add(channelKey);
        }
        if (realAttendant) {
            keys.add(ATTENDANT_GRANTED_KEY);
        }
        if (realBomber) {
            keys.add(BOMBER_GRANTED_KEY);
        }
        return List.copyOf(keys);
    }

    private static @Nullable String channelGrantMessageKey(@Nullable Set<TabletChannel> channels, boolean undercover) {
        if (channels == null || channels.isEmpty()) {
            return null;
        }
        if (channels.contains(TabletChannel.POLICE)) {
            return channels.contains(TabletChannel.KILLER) ? IMPOSTOR_GRANTED_KEY : POLICE_GRANTED_KEY;
        }
        if (channels.contains(TabletChannel.KILLER)) {
            return undercover ? UNDERCOVER_GRANTED_KEY : KILLER_GRANTED_KEY;
        }
        return channels.contains(TabletChannel.WITCH) ? WITCH_GRANTED_KEY : null;
    }
}
