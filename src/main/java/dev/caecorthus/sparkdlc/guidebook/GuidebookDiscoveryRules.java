package dev.caecorthus.sparkdlc.guidebook;

import java.util.List;
import java.util.Map;

/**
 * Defines which optional registry entries belong in the GuideBook index.
 * 定义可选注册表中的哪些条目属于指南书目录。
 */
public final class GuidebookDiscoveryRules {
    // Grand Witch: the main-key Witch Factor plus the Ceremonial Sword unlocked after 2 tasks. Only Witch Factor is a
    // registry skill; the permanent Ceremonial Sword appears only through its authored page.
    // 大魔女：主技能键的魔女因子，以及完成 2 个任务后解锁的仪礼剑。只有魔女因子在技能注册表中；
    // 常驻仪礼剑只会通过人工编写的页面出现。
    // The owner also files every skill page under its directory group (GuidebookNavigation.groupsFor), so authored
    // pages that are never discovered, like ceremonial_sword, must stay mapped here; an owner without a skills
    // group leaves its skill with no directory row.
    // 所属身份同时决定技能页所在的目录分组（GuidebookNavigation.groupsFor），因此从不被自动发现的人工页面（如仪礼剑）
    // 也必须保留在此映射中；所属身份没有技能分组时，该技能在目录中不会出现。
    // Apprentice Witch: five random registry skills plus Purify, the fixed second skill unlocked by graduating; Purify
    // is not in the skill registry either, so it too appears only through its authored page.
    // 预备魔女：五个随机的注册表技能，加上出师后固定解锁的第二技能净化；净化同样不在技能注册表中，只通过人工编写的页面出现。
    private static final Map<String, String> WITCH_SKILL_OWNER_ROLE_IDS = Map.ofEntries(
            Map.entry("sparkdlc:witch_factor", "sparkdlc:grand_witch"),
            Map.entry("sparkdlc:ceremonial_sword", "sparkdlc:grand_witch"),
            Map.entry("sparkdlc:emma_factor", "sparkdlc:emma"),
            Map.entry("sparkdlc:death_ray", "sparkdlc:murderous_witch"),
            Map.entry("sparkdlc:mighty_force", "sparkdlc:apprentice_witch"),
            Map.entry("sparkdlc:swift_step", "sparkdlc:apprentice_witch"),
            Map.entry("sparkdlc:murder_sense", "sparkdlc:apprentice_witch"),
            Map.entry("sparkdlc:healing", "sparkdlc:apprentice_witch"),
            Map.entry("sparkdlc:clairvoyance", "sparkdlc:apprentice_witch"),
            Map.entry("sparkdlc:purify", "sparkdlc:apprentice_witch")
    );

    private GuidebookDiscoveryRules() {
    }

    public static boolean includes(GuidebookTab tab, String id) {
        return tab != GuidebookTab.SKILL || WITCH_SKILL_OWNER_ROLE_IDS.containsKey(id);
    }

    public static List<String> ownerRoleIds(String skillId) {
        String roleId = WITCH_SKILL_OWNER_ROLE_IDS.get(skillId);
        return roleId == null ? List.of() : List.of(roleId);
    }
}
