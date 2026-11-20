package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.role.killer.blackraven.crow.BlackRavenFeatherRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The one creative tab of SparkDLC and what fills it: the role kit section first (every item registered before
 * {@link #registerRoleKitSection()} ran, in registration order), then the section of the mod's own roles (a fixed
 * order, then every other item of the mod). Operator-only items stay in vanilla's Operator Utilities tab.
 * SparkDLC 唯一的创造模式物品栏及其内容：先是职业套件部分（{@link #registerRoleKitSection()} 运行之前注册的全部物品，
 * 按注册顺序），再是本模组自有职业部分（固定顺序，随后是本模组的其他物品）。管理员专用物品仍只在原版「管理员用品」物品栏。
 */
public final class SparkDlcItemGroups {
    private SparkDlcItemGroups() {
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The tab / 物品栏
    // ---------------------------------------------------------------------------------------------------------------

    // Stable creative tab id; other mods may target it via ItemGroupEvents.
    // 稳定的创造物品栏标签页 ID，其他模组可通过 ItemGroupEvents 引用。
    public static final RegistryKey<ItemGroup> ITEMS_GROUP =
            RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(SparkDLC.MOD_ID, "items"));
    private static boolean registered;

    /** Called once, after every feature has registered its items. / 在所有功能注册完物品之后调用一次。 */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        Registry.register(
                Registries.ITEM_GROUP,
                ITEMS_GROUP,
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemGroup.sparkdlc.items"))
                        .icon(() -> new ItemStack(SparkDlcItems.m67()))
                        .entries((displayContext, entries) -> {
                            SparkDlcItemGroups.addRoleKitEntries(entries);
                            SparkDlcItemGroups.addRoleEntries(entries);
                        })
                        .build()
        );
        registered = true;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Role kit section (first) and its operator-only item / 职业套件部分（在前）及其管理员专用物品
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * The role kit items, in registration order. Captured when the role kits finish registering,
     * before the mod's own roles register anything: every item shares one namespace, so the namespace alone does not
     * say which section an item belongs to.
     * 职业套件的物品，按注册顺序。在职业套件注册完成时、本模组自有职业注册任何物品之前记录：所有物品共用一个命名空间，
     * 仅凭命名空间无法判断物品属于哪个部分。
     */
    private static final Set<Item> ROLE_KIT_ITEMS = new LinkedHashSet<>();
    private static boolean roleKitSectionRegistered;

    public static synchronized void registerRoleKitSection() {
        if (roleKitSectionRegistered) {
            return;
        }
        for (Item item : Registries.ITEM) {
            if (SparkDLC.MOD_ID.equals(Registries.ITEM.getId(item).getNamespace())) {
                ROLE_KIT_ITEMS.add(item);
            }
        }
        // The admin-only configurator lives in the vanilla operator tab. / 仅限管理员的线路配置器放在原版管理员物品栏。
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
            if (entries.shouldShowOpRestrictedItems()) {
                entries.add(SparkDlcItems.circuitConfigurator());
            }
        });
        roleKitSectionRegistered = true;
    }

    /** Whether {@code item} is a role kit item. / 该物品是否为职业套件物品。 */
    public static boolean isRoleKitItem(Item item) {
        return ROLE_KIT_ITEMS.contains(item);
    }

    /**
     * The role kit section of the tab: every role kit item, in registration order, so new items
     * join automatically.
     * 物品栏的职业套件部分：按注册顺序列出职业套件的全部物品，新增物品无需再手动加入。
     */
    static void addRoleKitEntries(ItemGroup.Entries entries) {
        for (Item item : ROLE_KIT_ITEMS) {
            if (item != SparkDlcItems.circuitConfigurator()) {
                entries.add(item);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Section of the mod's own roles (second) and its operator-only items / 本模组自有职业部分（在后）及其管理员专用物品
    // ---------------------------------------------------------------------------------------------------------------

    private static boolean roleSectionRegistered;

    public static synchronized void registerRoleSection() {
        if (roleSectionRegistered) {
            return;
        }
        // Vanilla shows this tab only to operators with "Operator Items Tab" on (Wathe's barrier blocks live here too).
        // 原版只向开启「显示管理员用品」的管理员显示该物品栏（Wathe 的屏障方块也在这里）。
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR)
                .register(entries -> operatorOnlyItems().forEach(entries::add));
        roleSectionRegistered = true;
    }

    /** The section of the mod's own roles. / 物品栏的本模组自有职业部分。 */
    static void addRoleEntries(ItemGroup.Entries entries) {
        // Default stacks only: the creative search index builds their tooltips off-thread with a null player.
        // 只列出默认物品堆：创造模式搜索索引会在后台线程以空玩家构建其提示文本。
        List<Item> factionOrder = List.of(
                // Witch / 魔女
                SparkDlcItems.ceremonialSword(),
                SparkDlcItems.shriekGun(),
                SparkDlcItems.deepDarkSporeFlask(),
                // Potion Gunner (witch) / 药炮手（魔女）
                SparkDlcItems.potionLauncher(),
                SparkDlcItems.gwDkShell(),
                SparkDlcItems.gwAcShell(),
                SparkDlcItems.gwMrShell(),
                SparkDlcItems.trShell(),
                SparkDlcItems.riftGate(),
                // Neutral / 中立
                SparkDlcItems.firePoker(),
                // 泽渡可可's lightstick (neutral) / 泽渡可可的应援棒（中立）
                SparkDlcItems.kokoLightstick(),
                // 狮身豪's hand gun and fist ammo (Team Jiahao) / 狮身豪的手枪与拳头子弹（嘉豪阵营）
                SparkDlcItems.sphinxHaoHandGun(),
                SparkDlcItems.sphinxHaoFistAmmo(),
                // The Sniper's M1903A4 and .30-06 rounds (Team Jiahao) / 狙击手的 M1903A4 与 .30-06 子弹（嘉豪阵营）
                SparkDlcItems.m1903a4(),
                SparkDlcItems.ammo3006(),
                // Civilian / 平民
                SparkDlcItems.perfumeEssence(),
                SparkDlcItems.cologne(),
                SparkDlcItems.tarotCard(),
                SparkDlcItems.prophetNecrology(),
                SparkDlcItems.vendettaKnife(),
                SparkDlcItems.disruptor(),
                SparkDlcItems.taser(),
                SparkDlcItems.shockDevice(),
                SparkDlcItems.holyFlash(),
                SparkDlcItems.seekerCar(),
                SparkDlcItems.seekerCamera(),
                SparkDlcItems.fishingRod(),
                SparkDlcItems.fishBait(),
                SparkDlcItems.salmon(),
                SparkDlcItems.cod(),
                SparkDlcItems.clownfish(),
                SparkDlcItems.goldfish(),
                SparkDlcItems.keyFish(),
                SparkDlcItems.swordfish(),
                SparkDlcItems.glimmerfish(),
                SparkDlcItems.whiteCane(),
                SparkDlcItems.comTac(),
                SparkDlcItems.usecRifle(),
                SparkDlcItems.usecMagazine(),
                SparkDlcItems.usecFmjRound(),
                SparkDlcItems.usecApRound(),
                SparkDlcItems.usecSuppressor(),
                // BEAR (civilian) / BEAR（平民）
                dev.caecorthus.sparkdlc.role.civilian.bear.BearItems.M32A1,
                dev.caecorthus.sparkdlc.role.civilian.bear.BearItems.SMOKE_ROUND,
                dev.caecorthus.sparkdlc.role.civilian.bear.BearItems.RYS_T,
                dev.caecorthus.sparkdlc.role.civilian.bear.BearItems.EARPLUGS,
                // NoellesRoles Attendant's Spare Key (civilian) / NoellesRoles 乘务员的备用钥匙（平民）
                SparkDlcItems.spareKey(),
                SparkDlcItems.bigDogPortrait(),
                // Accountant's passbook (civilian) / 会计的存折（平民）
                SparkDlcItems.accountantPassbook(),
                // Tono Hanna's parasol and handkerchief (civilian) / 远野汉娜的洋伞与手帕（平民）
                SparkDlcItems.hannaUmbrella(),
                SparkDlcItems.hannaHandkerchief(),
                // 橘雪莉's detective badge and note (civilian) / 橘雪莉的侦探徽章与纸条（平民）
                SparkDlcItems.sherryBadge(),
                SparkDlcItems.sherryNote(),
                // 规划大师's task slip (civilian) / 规划大师的任务单（平民）
                SparkDlcItems.plannerTaskSlip(),
                // Defense Vial (any holder, PR #240) / 防御药剂（任何持有者，PR #240）
                SparkDlcItems.defenseVial(),
                // Savior (civilian police) / 救世主（平民警察）
                SparkDlcItems.saviorPistol(),
                SparkDlcItems.saviorLightsaber(),
                // Black Raven (civilian police) / 黑羽鸦（平民警察）
                SparkDlcItems.featherBlade(),
                SparkDlcItems.blackRavenLedger(),
                SparkDlcItems.blackRavenMask(),
                // Killer / 杀手
                SparkDlcItems.ninjaKnife(),
                SparkDlcItems.ninjaShuriken(),
                SparkDlcItems.ninjaGrapplingHook(),
                SparkDlcItems.poisonApple(),
                SparkDlcItems.tofanaElixir(),
                SparkDlcItems.witherGohei(),
                SparkDlcItems.hunterTrap(),
                SparkDlcItems.doubleBarrelShotgun(),
                SparkDlcItems.doubleBarrelShell(),
                SparkDlcItems.doubleBarrelSlug(),
                SparkDlcItems.knockoutDrug(),
                SparkDlcItems.tollBell(),
                SparkDlcItems.timeStealerClock(),
                SparkDlcItems.timeStealerGiftWatch(),
                SparkDlcItems.timeStamp(),
                // Specter (killer) / 幽魂（杀手）
                SparkDlcItems.ghostTrigger(),
                SparkDlcItems.peachwoodSword(),
                // 二阶堂希罗 (killer) / 二阶堂希罗（杀手）
                SparkDlcItems.rope()
        );
        factionOrder.forEach(entries::add);
        // Any other item of the mod (ours, and not a role kit item) follows in registration order,
        // so new items join automatically.
        // Operator-only items are the exception: they live in vanilla's Operator Utilities tab only;
        // hidden items stay out of creative entirely.
        // 本模组的其他物品（属于本模组且不是职业套件物品）按注册顺序追加在后，新增物品无需再手动加入。
        // 管理员专用物品例外：只放在原版「管理员用品」物品栏；隐藏物品则完全不出现在创造模式物品栏。
        List<Item> operatorOnly = operatorOnlyItems();
        List<Item> creativeHidden = creativeHiddenItems();
        for (Item item : Registries.ITEM) {
            if (SparkDLC.MOD_ID.equals(Registries.ITEM.getId(item).getNamespace())
                    && !SparkDlcItemGroups.isRoleKitItem(item)
                    && !factionOrder.contains(item) && !operatorOnly.contains(item)
                    && !creativeHidden.contains(item)) {
                // The display-only Homing Feather is never obtainable. / 仅用于显示的归羽永不可获得。
                if (!BlackRavenFeatherRegistry.isHomingFeather(item)) {
                    entries.add(item);
                }
            }
        }
    }

    /** Items of the mod's own roles kept out of the tab and listed under Operator Utilities. / 管理员专用物品。 */
    private static List<Item> operatorOnlyItems() {
        return List.of(SparkDlcItems.riftGateRemover());
    }

    /**
     * Items hidden from the creative tab (owner decision 2026-11-02): the Cross Sword is a duel-only Knight weapon,
     * still granted by the Knight loadout and obtainable with commands, but no longer browsable in creative.
     * 不显示在创造模式物品栏的物品（所有者 2026-11-02 决定）：十字剑是仅用于决斗的骑士武器，仍由骑士开局发放并可用命令
     * 获取，但不再在创造模式中浏览。
     */
    private static List<Item> creativeHiddenItems() {
        return List.of(SparkDlcItems.crossSword());
    }
}
