package dev.caecorthus.sparkdlc.tablet;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Role-granted tablet sections that are not chat networks. Wire bits are a stable packet contract; never use ordinal().
 * 由身份授予、不属于聊天网络的平板分区。wire 位是稳定的网络包契约，禁止使用 ordinal()。
 *
 * <p>Features are independent of the viewed {@link TabletChannel}: a holder with no channel ("no signal") still sees
 * its features, and features never affect channel membership, links or the police electorate. {@link #CAMERAS} is
 * granted by police-network membership (identity, not the viewed channel), so an Impostor viewing the killer channel
 * keeps it.
 * 功能与当前查看的频道相互独立：没有频道（无信号）的持有者仍能看到其功能；功能不影响频道成员资格、互认或义警选民。
 * {@link #CAMERAS} 按义警网络成员资格授予（按身份而非当前查看的频道），因此正在查看杀手频道的内鬼仍保留它。</p>
 */
public enum TabletFeature {
    /** Attendant room-door monitor. / 乘务员房门监控。 */
    DOOR_LOG("door_log", 1),
    /** Bomber drone control (real Bomber only). / 炸弹客无人机操控（仅真实炸弹客）。 */
    DRONE("drone", 2),
    /** Killer-team sabotage page (see {@code SabotageAccess}). / 杀手阵营破坏页（见 SabotageAccess）。 */
    SABOTAGE("sabotage", 3),
    /**
     * The Seekers' wall cameras, for every living member of the police network while a camera stands (owner spec
     * 2026-11-28).
     * 搜寻者的摄像头监控，在有摄像头存在时面向义警网络的每名存活成员（所有者规格 2026-11-28）。
     */
    CAMERAS("cameras", 4);

    private final String id;
    private final int wire;

    TabletFeature(String id, int wire) {
        this.id = id;
        this.wire = wire;
    }

    public String id() {
        return id;
    }

    public int wire() {
        return wire;
    }

    public static int mask(Collection<TabletFeature> features) {
        int mask = 0;
        for (TabletFeature feature : features) {
            mask |= 1 << feature.wire;
        }
        return mask;
    }

    public static EnumSet<TabletFeature> fromMask(int mask) {
        EnumSet<TabletFeature> features = EnumSet.noneOf(TabletFeature.class);
        for (TabletFeature feature : values()) {
            if ((mask & (1 << feature.wire)) != 0) {
                features.add(feature);
            }
        }
        return features;
    }
}
