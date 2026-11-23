package dev.caecorthus.sparkdlc;

import com.mojang.serialization.Codec;
import dev.caecorthus.sparkdlc.SparkDlcBlocks;
import dev.caecorthus.sparkdlc.item.AromaOrbItem;
import dev.caecorthus.sparkdlc.item.BanditAmmoItem;
import dev.caecorthus.sparkdlc.item.BanditAttachmentItem;
import dev.caecorthus.sparkdlc.item.BanditRevolverItem;
import dev.caecorthus.sparkdlc.item.BlueBelladonnaItem;
import dev.caecorthus.sparkdlc.item.BlueVitriolItem;
import dev.caecorthus.sparkdlc.item.BodyguardVestItem;
import dev.caecorthus.sparkdlc.item.CapsuleItem;
import dev.caecorthus.sparkdlc.item.CaptureDeviceItem;
import dev.caecorthus.sparkdlc.item.CaseFolderItem;
import dev.caecorthus.sparkdlc.item.CircuitConfiguratorItem;
import dev.caecorthus.sparkdlc.item.CoolingOilItem;
import dev.caecorthus.sparkdlc.item.CoronerBodyBagItem;
import dev.caecorthus.sparkdlc.item.CqcmMaskItem;
import dev.caecorthus.sparkdlc.item.DemocracyShieldItem;
import dev.caecorthus.sparkdlc.item.DroneItem;
import dev.caecorthus.sparkdlc.item.EmberSugarItem;
import dev.caecorthus.sparkdlc.item.FlashlightItem;
import dev.caecorthus.sparkdlc.item.GhostflameBittersItem;
import dev.caecorthus.sparkdlc.item.ImpostorLockpickItem;
import dev.caecorthus.sparkdlc.item.M67Item;
import dev.caecorthus.sparkdlc.item.MagnifierItem;
import dev.caecorthus.sparkdlc.item.MorphDeviceItem;
import dev.caecorthus.sparkdlc.item.MorphReagentItem;
import dev.caecorthus.sparkdlc.item.PartyBalloonItem;
import dev.caecorthus.sparkdlc.item.PowerRestorationItem;
import dev.caecorthus.sparkdlc.item.ProfessorSerumItem;
import dev.caecorthus.sparkdlc.item.SerialPistolItem;
import dev.caecorthus.sparkdlc.item.SkateboardItem;
import dev.caecorthus.sparkdlc.item.TVirusItem;
import dev.caecorthus.sparkdlc.item.TabletItem;
import dev.caecorthus.sparkdlc.item.TacticalTomahawkItem;
import dev.caecorthus.sparkdlc.item.TimekeeperWatchItem;
import dev.caecorthus.sparkdlc.item.VirusItem;
import dev.caecorthus.sparkdlc.item.ZephyrPerfumeItem;
import dev.caecorthus.sparkdlc.item.ceremonialsword.CeremonialSwordItem;
import dev.caecorthus.sparkdlc.item.defense.DefenseVialItem;
import dev.caecorthus.sparkdlc.item.defense.DefenseVialRules;
import dev.caecorthus.sparkdlc.item.m1903a4.Ammo3006Item;
import dev.caecorthus.sparkdlc.item.m1903a4.M1903A4Item;
import dev.caecorthus.sparkdlc.item.ninja.NinjaGrapplingHookItem;
import dev.caecorthus.sparkdlc.item.ninja.NinjaKnifeItem;
import dev.caecorthus.sparkdlc.item.ninja.NinjaShurikenItem;
import dev.caecorthus.sparkdlc.item.rope.RopeItem;
import dev.caecorthus.sparkdlc.item.rope.RopeRules;
import dev.caecorthus.sparkdlc.item.sparekey.SpareKeyItem;
import dev.caecorthus.sparkdlc.item.sparekey.SpareKeyRules;
import dev.caecorthus.sparkdlc.role.civilian.accountant.AccountantPassbookItem;
import dev.caecorthus.sparkdlc.role.civilian.accountant.AccountantRules;
import dev.caecorthus.sparkdlc.role.civilian.blind.BlindRules;
import dev.caecorthus.sparkdlc.role.civilian.blind.item.ComTacItem;
import dev.caecorthus.sparkdlc.role.civilian.blind.item.WhiteCaneItem;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.ControlExpertRules;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.DisruptorItem;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.ShockDeviceItem;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.TaserItem;
import dev.caecorthus.sparkdlc.role.civilian.fisher.FisherRules;
import dev.caecorthus.sparkdlc.role.civilian.fisher.item.FishBaitItem;
import dev.caecorthus.sparkdlc.role.civilian.fisher.item.FisherFishItem;
import dev.caecorthus.sparkdlc.role.civilian.fisher.item.FisherFishKind;
import dev.caecorthus.sparkdlc.role.civilian.fisher.item.FishingRodItem;
import dev.caecorthus.sparkdlc.role.civilian.fisher.item.KeyFishItem;
import dev.caecorthus.sparkdlc.role.civilian.fisher.swordfish.SwordfishItem;
import dev.caecorthus.sparkdlc.role.civilian.hanna.HannaHandkerchiefItem;
import dev.caecorthus.sparkdlc.role.civilian.hanna.HannaRules;
import dev.caecorthus.sparkdlc.role.civilian.hanna.HannaUmbrellaItem;
import dev.caecorthus.sparkdlc.role.civilian.knight.CrossSwordItem;
import dev.caecorthus.sparkdlc.role.civilian.noa.NoaRules;
import dev.caecorthus.sparkdlc.role.civilian.perfumer.CologneItem;
import dev.caecorthus.sparkdlc.role.civilian.perfumer.PerfumeEssenceItem;
import dev.caecorthus.sparkdlc.role.civilian.perfumer.PerfumerKitRules;
import dev.caecorthus.sparkdlc.role.civilian.planner.PlannerRules;
import dev.caecorthus.sparkdlc.role.civilian.planner.PlannerTaskSlipItem;
import dev.caecorthus.sparkdlc.role.civilian.professor.ProfessorSerumType;
import dev.caecorthus.sparkdlc.role.civilian.prophet.ProphetNecrologyItem;
import dev.caecorthus.sparkdlc.role.civilian.prophet.ProphetNecrologyRules;
import dev.caecorthus.sparkdlc.role.civilian.saint.flash.HolyFlashItem;
import dev.caecorthus.sparkdlc.role.civilian.saint.flash.HolyFlashRules;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorLightsaberItem;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorPistolItem;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorRules;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerRules;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerCameraItem;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerCarItem;
import dev.caecorthus.sparkdlc.role.civilian.toxicologist.ToxicologistBlueRules;
import dev.caecorthus.sparkdlc.role.civilian.undercover.UndercoverRules;
import dev.caecorthus.sparkdlc.role.civilian.usec.UsecAmmoItem;
import dev.caecorthus.sparkdlc.role.civilian.usec.UsecAmmoType;
import dev.caecorthus.sparkdlc.role.civilian.usec.UsecMagazineItem;
import dev.caecorthus.sparkdlc.role.civilian.usec.UsecRifleItem;
import dev.caecorthus.sparkdlc.role.civilian.usec.UsecRules;
import dev.caecorthus.sparkdlc.role.civilian.usec.UsecSuppressorItem;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaInteractionService;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaKnifeItem;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaKnifeLoadoutService;
import dev.caecorthus.sparkdlc.role.jiahao.sphinxhao.SphinxHaoRules;
import dev.caecorthus.sparkdlc.role.jiahao.sphinxhao.gun.SphinxHaoFistAmmoItem;
import dev.caecorthus.sparkdlc.role.jiahao.sphinxhao.gun.SphinxHaoHandGunItem;
import dev.caecorthus.sparkdlc.role.killer.bandit.BanditAmmo;
import dev.caecorthus.sparkdlc.role.killer.bandit.BanditAttachment;
import dev.caecorthus.sparkdlc.role.killer.bandit.BanditRevolverRules;
import dev.caecorthus.sparkdlc.role.killer.bandit.BanditRevolverState;
import dev.caecorthus.sparkdlc.role.killer.bellringer.TollBellItem;
import dev.caecorthus.sparkdlc.role.killer.blackraven.BlackRavenLedgerItem;
import dev.caecorthus.sparkdlc.role.killer.blackraven.FeatherBladeItem;
import dev.caecorthus.sparkdlc.role.killer.blackraven.disguise.BlackRavenDisguiseRules;
import dev.caecorthus.sparkdlc.role.killer.blackraven.disguise.BlackRavenMaskItem;
import dev.caecorthus.sparkdlc.role.killer.bomber.drone.DroneKind;
import dev.caecorthus.sparkdlc.role.killer.hunter.DoubleBarrelShellItem;
import dev.caecorthus.sparkdlc.role.killer.hunter.DoubleBarrelShotgunItem;
import dev.caecorthus.sparkdlc.role.killer.hunter.DoubleBarrelSlugItem;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterTrapItem;
import dev.caecorthus.sparkdlc.role.killer.kidnapper.KnockoutDrugItem;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartyBalloonRules;
import dev.caecorthus.sparkdlc.role.killer.specter.GhostTriggerItem;
import dev.caecorthus.sparkdlc.role.killer.specter.PeachwoodSwordItem;
import dev.caecorthus.sparkdlc.role.killer.timestealer.TimeGiftWatchItem;
import dev.caecorthus.sparkdlc.role.killer.timestealer.TimeStampItem;
import dev.caecorthus.sparkdlc.role.killer.timestealer.TimeStealerClockItem;
import dev.caecorthus.sparkdlc.role.killer.timestealer.TimeStealerRules;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.PoisonAppleItem;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.wither.MiniWitherRules;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.wither.WitherGoheiItem;
import dev.caecorthus.sparkdlc.role.neutral.koko.KokoLightstickItem;
import dev.caecorthus.sparkdlc.role.neutral.koko.KokoRules;
import dev.caecorthus.sparkdlc.role.witch.abysslistener.AbyssListenerRules;
import dev.caecorthus.sparkdlc.role.witch.abysslistener.gun.ShriekGunItem;
import dev.caecorthus.sparkdlc.role.witch.abysslistener.zone.DeepDarkSporeFlaskItem;
import dev.caecorthus.sparkdlc.role.witch.potiongunner.PotionGunnerRules;
import dev.caecorthus.sparkdlc.role.witch.potiongunner.PotionShellType;
import dev.caecorthus.sparkdlc.role.witch.potiongunner.launcher.PotionLauncherItem;
import dev.caecorthus.sparkdlc.role.witch.potiongunner.shell.PotionShellItem;
import dev.caecorthus.sparkdlc.role.witch.riftwalker.RiftwalkerRules;
import dev.caecorthus.sparkdlc.role.witch.riftwalker.gate.RiftGateItem;
import dev.caecorthus.sparkdlc.role.witch.riftwalker.gate.RiftGateRemoverItem;
import dev.doctor4t.wathe.api.event.AllowPlayerPunching;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.agmas.noellesroles.item.IngredientItem;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Every item the mod registers from one place (BEAR and Black Raven feather items keep their role classes). The two
 * parts register at different moments of {@code SparkDLC#onInitialize}, each through its own method, and that order
 * is the raw-id order of the item registry and the order of the creative tab: add a new item at the end of the
 * registration method of its part.
 * 本模组集中注册的全部物品（BEAR 与黑羽鸦羽毛物品仍在各自的职业类中）。两个部分在 {@code SparkDLC#onInitialize} 的不同时刻、
 * 各自通过自己的方法注册；该顺序就是物品注册表的 raw id 顺序，也是创造模式物品栏的顺序：新增物品请追加到所属部分注册方法的末尾。
 */
public final class SparkDlcItems {
    private SparkDlcItems() {
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Role kit items (kits of the Wathe and NoellesRoles roles) / 职业套件物品（Wathe 与 NoellesRoles 职业的套件）
    // ---------------------------------------------------------------------------------------------------------------

    public static final Identifier CAPSULE_ID = SparkDLC.id("capsule");
    public static final Identifier FLASHLIGHT_ID = SparkDLC.id("flashlight");
    public static final Identifier TABLET_ID = SparkDLC.id("tablet");
    public static final Identifier INVISIBILITY_SERUM_ID = SparkDLC.id("invisibility_serum");
    public static final Identifier DOORPASSING_POTION_ID = SparkDLC.id("doorpassing_potion");
    public static final Identifier SEDATIVE_ID = SparkDLC.id("sedative");
    public static final Identifier TRUTH_SERUM_ID = SparkDLC.id("truth_serum");
    public static final Identifier CAPTURE_DEVICE_ID = SparkDLC.id("capture_device");
    public static final Identifier POWER_RESTORATION_ID = SparkDLC.id("power_restoration");
    public static final Identifier CIRCUIT_CONFIGURATOR_ID = SparkDLC.id("circuit_configurator");
    public static final Identifier MORPH_REAGENT_ID = SparkDLC.id("morph_reagent");
    public static final Identifier MORPH_DEVICE_ID = SparkDLC.id("morph_device");
    public static final Identifier CORONER_BODY_BAG_ID = SparkDLC.id("coroner_body_bag");
    public static final Identifier M67_ID = SparkDLC.id("m67");
    public static final Identifier MAGNIFIER_ID = SparkDLC.id("magnifier");
    public static final Identifier CASE_FOLDER_ID = SparkDLC.id("case_folder");
    public static final Identifier COOLING_OIL_ID = SparkDLC.id("cooling_oil");
    public static final Identifier AROMA_ORB_ID = SparkDLC.id("aroma_orb");
    public static final Identifier ZEPHYR_PERFUME_ID = SparkDLC.id("zephyr_perfume");
    public static final Identifier BLUE_VITRIOL_ID = SparkDLC.id("blue_vitriol");
    public static final Identifier BLUE_BELLADONNA_ID = SparkDLC.id("blue_belladonna");
    public static final Identifier GRENADE_DRONE_ID = SparkDLC.id("grenade_drone");
    public static final Identifier BOMB_DRONE_ID = SparkDLC.id("bomb_drone");
    public static final Identifier SKATEBOARD_ID = SparkDLC.id("skateboard");
    public static final Identifier GHOSTFLAME_BITTERS_ID = SparkDLC.id("ghostflame_bitters");
    public static final Identifier EMBER_SUGAR_ID = SparkDLC.id("ember_sugar");
    public static final Identifier VIRUS_ID = SparkDLC.id("virus");
    public static final Identifier T_VIRUS_ID = SparkDLC.id("t_virus");
    private static Item m67;
    public static final Identifier DYING_WATCH_ID = SparkDLC.id("dying_watch");
    public static final Identifier SERIAL_PISTOL_ID = SparkDLC.id("serial_pistol");
    public static final Identifier SERIAL_LEFT_PISTOL_ID = SparkDLC.id("serial_left_pistol");
    public static final Identifier DEMOCRACY_SHIELD_ID = SparkDLC.id("democracy_shield");
    public static final Identifier BODYGUARD_VEST_ID = SparkDLC.id("bodyguard_vest");
    public static final Identifier CQCM_MASK_ID = SparkDLC.id("cqcm_mask");
    public static final Identifier TACTICAL_TOMAHAWK_ID = SparkDLC.id("tactical_tomahawk");
    public static final Identifier PARTY_BALLOON_ID = SparkDLC.id("party_balloon");
    public static final Identifier IMPOSTOR_LOCKPICK_ID = SparkDLC.id("impostor_lockpick");
    public static final Identifier BANDIT_REVOLVER_ID = SparkDLC.id("bandit_revolver");
    private static Item capsule;
    private static Item flashlight;
    private static Item tablet;
    private static Item invisibilitySerum;
    private static Item doorpassingPotion;
    private static Item sedative;
    private static Item truthSerum;
    private static Item captureDevice;
    private static Item powerRestoration;
    private static Item circuitConfigurator;
    private static Item morphReagent;
    private static Item morphDevice;
    private static Item coronerBodyBag;
    private static Item magnifier;
    private static Item caseFolder;
    private static Item coolingOil;
    private static Item aromaOrb;
    private static Item zephyrPerfume;
    private static Item blueVitriol;
    private static Item blueBelladonna;
    private static Item grenadeDrone;
    private static Item bombDrone;
    private static Item skateboard;
    private static Item ghostflameBitters;
    private static Item emberSugar;
    private static Item virus;
    private static Item tVirus;
    private static Item dyingWatch;
    private static Item serialPistol;
    private static Item serialLeftPistol;
    private static Item democracyShield;
    private static Item bodyguardVest;
    private static Item cqcmMask;
    private static Item tacticalTomahawk;
    private static Item partyBalloon;
    private static Item impostorLockpick;
    private static Item banditRevolver;
    /** Bandit revolver parts ({@code bandit_<key>}) and rounds ({@code <key>_round}). / 强盗左轮配件与子弹。 */
    private static final Map<BanditAttachment, Item> BANDIT_ATTACHMENTS = new EnumMap<>(BanditAttachment.class);
    private static final Map<BanditAmmo, Item> BANDIT_AMMO = new EnumMap<>(BanditAmmo.class);
    private static boolean roleKitItemsRegistered;

    public static synchronized void registerRoleKitItems() {
        if (roleKitItemsRegistered) {
            return;
        }
        capsule = Registry.register(
                Registries.ITEM,
                CAPSULE_ID,
                new CapsuleItem(new Item.Settings().maxCount(1))
        );
        flashlight = Registry.register(
                Registries.ITEM,
                FLASHLIGHT_ID,
                new FlashlightItem(new Item.Settings().maxCount(1))
        );
        tablet = Registry.register(
                Registries.ITEM,
                TABLET_ID,
                new TabletItem(new Item.Settings().maxCount(1))
        );
        invisibilitySerum = Registry.register(
                Registries.ITEM,
                INVISIBILITY_SERUM_ID,
                new ProfessorSerumItem(new Item.Settings().maxCount(1), ProfessorSerumType.INVISIBILITY)
        );
        doorpassingPotion = Registry.register(
                Registries.ITEM,
                DOORPASSING_POTION_ID,
                new ProfessorSerumItem(new Item.Settings().maxCount(1), ProfessorSerumType.DOORPASSING)
        );
        sedative = Registry.register(
                Registries.ITEM,
                SEDATIVE_ID,
                new ProfessorSerumItem(new Item.Settings().maxCount(1), ProfessorSerumType.SEDATIVE)
        );
        truthSerum = Registry.register(
                Registries.ITEM,
                TRUTH_SERUM_ID,
                new ProfessorSerumItem(new Item.Settings().maxCount(1), ProfessorSerumType.TRUTH)
        );
        captureDevice = Registry.register(
                Registries.ITEM,
                CAPTURE_DEVICE_ID,
                new CaptureDeviceItem(new Item.Settings().maxCount(1))
        );
        powerRestoration = Registry.register(
                Registries.ITEM,
                POWER_RESTORATION_ID,
                new PowerRestorationItem(new Item.Settings().maxCount(1))
        );
        circuitConfigurator = Registry.register(
                Registries.ITEM,
                CIRCUIT_CONFIGURATOR_ID,
                new CircuitConfiguratorItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC))
        );
        morphReagent = Registry.register(
                Registries.ITEM,
                MORPH_REAGENT_ID,
                new MorphReagentItem(new Item.Settings().maxCount(1))
        );
        morphDevice = Registry.register(
                Registries.ITEM,
                MORPH_DEVICE_ID,
                new MorphDeviceItem(new Item.Settings().maxCount(1))
        );
        coronerBodyBag = Registry.register(
                Registries.ITEM,
                CORONER_BODY_BAG_ID,
                new CoronerBodyBagItem(new Item.Settings().maxCount(1))
        );
        m67 = Registry.register(
                Registries.ITEM,
                M67_ID,
                new M67Item(new Item.Settings().maxCount(1))
        );
        magnifier = Registry.register(
                Registries.ITEM,
                MAGNIFIER_ID,
                new MagnifierItem(new Item.Settings().maxCount(1))
        );
        caseFolder = Registry.register(
                Registries.ITEM,
                CASE_FOLDER_ID,
                new CaseFolderItem(new Item.Settings().maxCount(1))
        );
        coolingOil = Registry.register(
                Registries.ITEM,
                COOLING_OIL_ID,
                new CoolingOilItem(new Item.Settings().maxCount(PerfumerKitRules.THROWABLE_MAX_STACK))
        );
        aromaOrb = Registry.register(
                Registries.ITEM,
                AROMA_ORB_ID,
                new AromaOrbItem(new Item.Settings().maxCount(PerfumerKitRules.THROWABLE_MAX_STACK))
        );
        zephyrPerfume = Registry.register(
                Registries.ITEM,
                ZEPHYR_PERFUME_ID,
                new ZephyrPerfumeItem(new Item.Settings().maxCount(1))
        );
        blueVitriol = Registry.register(
                Registries.ITEM,
                BLUE_VITRIOL_ID,
                new BlueVitriolItem(new Item.Settings().maxCount(ToxicologistBlueRules.MAX_STACK))
        );
        blueBelladonna = Registry.register(
                Registries.ITEM,
                BLUE_BELLADONNA_ID,
                new BlueBelladonnaItem(new Item.Settings()
                        .maxCount(ToxicologistBlueRules.MAX_STACK)
                        .food(BlueBelladonnaItem.FOOD))
        );
        grenadeDrone = Registry.register(
                Registries.ITEM,
                GRENADE_DRONE_ID,
                new DroneItem(DroneKind.GRENADE, new Item.Settings().maxCount(1))
        );
        bombDrone = Registry.register(
                Registries.ITEM,
                BOMB_DRONE_ID,
                new DroneItem(DroneKind.BOMB, new Item.Settings().maxCount(1))
        );
        skateboard = Registry.register(
                Registries.ITEM,
                SKATEBOARD_ID,
                new SkateboardItem(new Item.Settings().maxCount(1))
        );
        ghostflameBitters = registerIngredient(
                GHOSTFLAME_BITTERS_ID,
                new GhostflameBittersItem(new Item.Settings().maxCount(1))
        );
        emberSugar = registerIngredient(
                EMBER_SUGAR_ID,
                new EmberSugarItem(new Item.Settings().maxCount(1))
        );
        virus = Registry.register(
                Registries.ITEM,
                VIRUS_ID,
                new VirusItem(new Item.Settings().maxCount(16))
        );
        tVirus = Registry.register(
                Registries.ITEM,
                T_VIRUS_ID,
                new TVirusItem(new Item.Settings().maxCount(1))
        );
        // 计时员濒毁怀表：只保留两种刷新冷却模式，状态由数据组件保存。
        dyingWatch = Registry.register(
                Registries.ITEM,
                DYING_WATCH_ID,
                new TimekeeperWatchItem(new Item.Settings()
                        .maxCount(1)
                        .component(SparkDlcDataComponents.TIMEKEEPER_WATCH_MODE, 0))
        );
        serialPistol = Registry.register(
                Registries.ITEM,
                SERIAL_PISTOL_ID,
                new SerialPistolItem(new Item.Settings().maxCount(1))
        );
        serialLeftPistol = Registry.register(
                Registries.ITEM,
                SERIAL_LEFT_PISTOL_ID,
                new SerialPistolItem(new Item.Settings().maxCount(1))
        );
        democracyShield = Registry.register(
                Registries.ITEM,
                DEMOCRACY_SHIELD_ID,
                new DemocracyShieldItem(new Item.Settings().maxCount(1))
        );
        // Zero defence: the vest only matters to Wathe kills (BodyguardVestService), and no vanilla armour bar shows.
        // 护甲值为 0：防弹衣只对 Wathe 击杀生效（BodyguardVestService），也不会显示原版护甲条。
        RegistryEntry<ArmorMaterial> vestMaterial = Registry.registerReference(
                Registries.ARMOR_MATERIAL,
                BODYGUARD_VEST_ID,
                new ArmorMaterial(
                        Map.of(ArmorItem.Type.CHESTPLATE, 0),
                        0,
                        SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,
                        () -> Ingredient.EMPTY,
                        List.of(new ArmorMaterial.Layer(BODYGUARD_VEST_ID)),
                        0.0F,
                        0.0F
                )
        );
        bodyguardVest = Registry.register(
                Registries.ITEM,
                BODYGUARD_VEST_ID,
                new BodyguardVestItem(vestMaterial, new Item.Settings().maxCount(1))
        );
        // Same idea as the vest: zero defence, the vanilla armour layer draws the mask on the face. It does resist
        // knockback like a netherite helmet. / 与防弹衣同理：护甲值为 0，由原版盔甲层把面具画在脸上；击退抗性同下界合金头盔。
        RegistryEntry<ArmorMaterial> maskMaterial = Registry.registerReference(
                Registries.ARMOR_MATERIAL,
                CQCM_MASK_ID,
                new ArmorMaterial(
                        Map.of(ArmorItem.Type.HELMET, 0),
                        0,
                        SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                        () -> Ingredient.EMPTY,
                        List.of(new ArmorMaterial.Layer(CQCM_MASK_ID)),
                        0.0F,
                        UndercoverRules.KNOCKBACK_RESISTANCE_PER_PIECE
                )
        );
        cqcmMask = Registry.register(
                Registries.ITEM,
                CQCM_MASK_ID,
                new CqcmMaskItem(maskMaterial, new Item.Settings().maxCount(1))
        );
        tacticalTomahawk = Registry.register(
                Registries.ITEM,
                TACTICAL_TOMAHAWK_ID,
                new TacticalTomahawkItem(new Item.Settings().maxCount(1))
        );
        partyBalloon = Registry.register(
                Registries.ITEM,
                PARTY_BALLOON_ID,
                new PartyBalloonItem(new Item.Settings().maxCount(PartyBalloonRules.MAX_STACK))
        );
        impostorLockpick = Registry.register(
                Registries.ITEM,
                IMPOSTOR_LOCKPICK_ID,
                new ImpostorLockpickItem(new Item.Settings().maxCount(1))
        );
        banditRevolver = Registry.register(
                Registries.ITEM,
                BANDIT_REVOLVER_ID,
                new BanditRevolverItem(new Item.Settings().maxCount(1)
                        .component(SparkDlcDataComponents.banditRevolver(), BanditRevolverState.EMPTY))
        );
        for (BanditAttachment attachment : BanditAttachment.values()) {
            BANDIT_ATTACHMENTS.put(attachment, Registry.register(
                    Registries.ITEM,
                    SparkDLC.id("bandit_" + attachment.key()),
                    new BanditAttachmentItem(new Item.Settings().maxCount(1), attachment)
            ));
        }
        for (BanditAmmo ammo : BanditAmmo.values()) {
            BANDIT_AMMO.put(ammo, Registry.register(
                    Registries.ITEM,
                    SparkDLC.id(ammo.key() + "_round"),
                    new BanditAmmoItem(new Item.Settings().maxCount(BanditRevolverRules.AMMO_MAX_STACK), ammo)
            ));
        }
        roleKitItemsRegistered = true;
    }

    public static Item m67() {
        if (m67 == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return m67;
    }

    public static Item capsule() {
        if (capsule == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return capsule;
    }

    public static Item flashlight() {
        if (flashlight == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return flashlight;
    }

    public static Item tablet() {
        if (tablet == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return tablet;
    }

    public static Item invisibilitySerum() {
        if (invisibilitySerum == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return invisibilitySerum;
    }

    public static Item doorpassingPotion() {
        if (doorpassingPotion == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return doorpassingPotion;
    }

    public static Item sedative() {
        if (sedative == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return sedative;
    }

    public static Item truthSerum() {
        if (truthSerum == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return truthSerum;
    }

    public static Item captureDevice() {
        if (captureDevice == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return captureDevice;
    }

    public static Item powerRestoration() {
        if (powerRestoration == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return powerRestoration;
    }

    public static Item circuitConfigurator() {
        if (circuitConfigurator == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return circuitConfigurator;
    }

    public static Item morphReagent() {
        if (morphReagent == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return morphReagent;
    }

    public static Item morphDevice() {
        if (morphDevice == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return morphDevice;
    }

    public static Item coronerBodyBag() {
        if (coronerBodyBag == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return coronerBodyBag;
    }

    public static Item magnifier() {
        if (magnifier == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return magnifier;
    }

    public static Item caseFolder() {
        if (caseFolder == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return caseFolder;
    }

    public static Item coolingOil() {
        if (coolingOil == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return coolingOil;
    }

    public static Item aromaOrb() {
        if (aromaOrb == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return aromaOrb;
    }

    public static Item zephyrPerfume() {
        if (zephyrPerfume == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return zephyrPerfume;
    }

    public static Item blueVitriol() {
        if (blueVitriol == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return blueVitriol;
    }

    public static Item blueBelladonna() {
        if (blueBelladonna == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return blueBelladonna;
    }

    public static Item grenadeDrone() {
        if (grenadeDrone == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return grenadeDrone;
    }

    public static Item bombDrone() {
        if (bombDrone == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return bombDrone;
    }

    public static Item democracyShield() {
        if (democracyShield == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return democracyShield;
    }

    public static Item cqcmMask() {
        if (cqcmMask == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return cqcmMask;
    }

    public static Item tacticalTomahawk() {
        if (tacticalTomahawk == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return tacticalTomahawk;
    }

    public static Item partyBalloon() {
        if (partyBalloon == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return partyBalloon;
    }

    public static Item banditRevolver() {
        if (banditRevolver == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return banditRevolver;
    }

    public static Item banditAttachment(BanditAttachment attachment) {
        Item item = BANDIT_ATTACHMENTS.get(attachment);
        if (item == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return item;
    }

    public static Item banditAmmo(BanditAmmo ammo) {
        Item item = BANDIT_AMMO.get(ammo);
        if (item == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return item;
    }

    public static Item impostorLockpick() {
        if (impostorLockpick == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return impostorLockpick;
    }

    public static Item bodyguardVest() {
        if (bodyguardVest == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return bodyguardVest;
    }

    public static Item skateboard() {
        if (skateboard == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return skateboard;
    }

    public static Item ghostflameBitters() {
        if (ghostflameBitters == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return ghostflameBitters;
    }

    public static Item emberSugar() {
        if (emberSugar == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return emberSugar;
    }

    /**
     * Bartender spices also join NoellesRoles' ingredient registry, which a base spirit uses to resolve the ids in
     * its NBT (effects, naming, tooltip).
     * 酒保调料同时登记到 NoellesRoles 的调剂注册表；基酒靠它解析 NBT 里的 id（效果、命名、提示）。
     */
    private static Item registerIngredient(Identifier id, IngredientItem ingredient) {
        Item item = Registry.register(Registries.ITEM, id, ingredient);
        IngredientItem.register(ingredient);
        return item;
    }

    public static Item drone(DroneKind kind) {
        return kind == DroneKind.BOMB ? bombDrone() : grenadeDrone();
    }

    public static Item virus() {
        if (virus == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return virus;
    }

    public static Item tVirus() {
        if (tVirus == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return tVirus;
    }

    public static Item dyingWatch() {
        if (dyingWatch == null) {
            throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        }
        return dyingWatch;
    }

    public static Item serialPistol() {
        if (serialPistol == null) throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        return serialPistol;
    }

    public static Item serialLeftPistol() {
        if (serialLeftPistol == null) throw new IllegalStateException("SparkDLC role kit items are not registered yet");
        return serialLeftPistol;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Items of the mod's own roles / 本模组自有职业的物品
    // ---------------------------------------------------------------------------------------------------------------

    public static final Identifier CEREMONIAL_SWORD_ID = SparkDLC.id("ceremonial_sword");
    public static final Identifier CROSS_SWORD_ID = SparkDLC.id("cross_sword");
    public static final Identifier FIRE_POKER_ID = SparkDLC.id("fire_poker");
    public static final Identifier PERFUME_ESSENCE_ID = SparkDLC.id("perfume_essence");
    public static final Identifier COLOGNE_ID = SparkDLC.id("cologne");
    public static final Identifier TAROT_CARD_ID = SparkDLC.id("tarot_card");
    public static final Identifier NINJA_KNIFE_ID = SparkDLC.id("ninja_knife");
    public static final Identifier VENDETTA_KNIFE_ID = SparkDLC.id("vendetta_knife");
    public static final Identifier NINJA_SHURIKEN_ID = SparkDLC.id("ninja_shuriken");
    public static final Identifier NINJA_GRAPPLING_HOOK_ID = SparkDLC.id("ninja_grappling_hook");
    public static final Identifier FEATHER_BLADE_ID = SparkDLC.id("feather_blade");
    public static final Identifier BLACK_RAVEN_LEDGER_ID = SparkDLC.id("black_raven_ledger");
    public static final Identifier BLACK_RAVEN_MASK_ID = BlackRavenDisguiseRules.MASK_ITEM_ID;
    public static final Identifier PROPHET_NECROLOGY_ID = ProphetNecrologyRules.ITEM_ID;
    public static final Identifier HUNTER_TRAP_ID = HunterTrapItem.ID;
    public static final Identifier DOUBLE_BARREL_SHOTGUN_ID = DoubleBarrelShotgunItem.ID;
    public static final Identifier DOUBLE_BARREL_SHELL_ID = DoubleBarrelShellItem.ID;
    public static final Identifier DOUBLE_BARREL_SLUG_ID = DoubleBarrelSlugItem.ID;
    public static final Identifier POISON_APPLE_ID = SparkDLC.id("poison_apple");
    public static final Identifier TOFANA_ELIXIR_ID = SparkDLC.id("tofana_elixir");
    public static final Identifier TOLL_BELL_ID = SparkDLC.id("toll_bell");
    public static final Identifier TIME_STEALER_CLOCK_ID = TimeStealerRules.CLOCK_ID;
    public static final Identifier TIME_STEALER_GIFT_WATCH_ID = TimeStealerRules.GIFT_WATCH_ID;
    public static final Identifier TIME_STAMP_ID = TimeStealerRules.STAMP_ID;
    public static final Identifier KNOCKOUT_DRUG_ID = SparkDLC.id("knockout_drug");
    public static final Identifier DISRUPTOR_ID = ControlExpertRules.DISRUPTOR_ID;
    public static final Identifier TASER_ID = ControlExpertRules.TASER_ID;
    public static final Identifier SHOCK_DEVICE_ID = ControlExpertRules.SHOCK_DEVICE_ID;
    public static final Identifier HOLY_FLASH_ID = HolyFlashRules.ITEM_ID;
    public static final Identifier SEEKER_CAR_ID = SeekerRules.CAR_ITEM_ID;
    public static final Identifier SEEKER_CAMERA_ID = SeekerRules.CAMERA_ITEM_ID;
    public static final Identifier FISHING_ROD_ID = FisherRules.FISHING_ROD_ID;
    public static final Identifier FISH_BAIT_ID = FisherRules.BAIT_ID;
    public static final Identifier SALMON_ID = FisherRules.SALMON_ID;
    public static final Identifier COD_ID = FisherRules.COD_ID;
    public static final Identifier CLOWNFISH_ID = FisherRules.CLOWNFISH_ID;
    public static final Identifier GOLDFISH_ID = FisherRules.GOLDFISH_ID;
    public static final Identifier KEY_FISH_ID = FisherRules.KEY_FISH_ID;
    public static final Identifier SPARE_KEY_ID = SpareKeyRules.ITEM_ID;
    public static final Identifier SWORDFISH_ID = FisherRules.SWORDFISH_ID;
    public static final Identifier GLIMMERFISH_ID = FisherRules.GLIMMERFISH_ID;
    public static final Identifier WHITE_CANE_ID = BlindRules.WHITE_CANE_ID;
    public static final Identifier COMTAC_ID = BlindRules.COMTAC_ID;
    public static final Identifier SHRIEK_GUN_ID = AbyssListenerRules.GUN_ITEM_ID;
    public static final Identifier DEEP_DARK_SPORE_FLASK_ID = AbyssListenerRules.FLASK_ITEM_ID;
    public static final Identifier POTION_LAUNCHER_ID = PotionGunnerRules.LAUNCHER_ID;
    public static final Identifier RIFT_GATE_ID = RiftwalkerRules.GATE_ITEM_ID;
    public static final Identifier RIFT_GATE_REMOVER_ID = RiftwalkerRules.GATE_REMOVER_ITEM_ID;
    public static final Identifier USEC_RIFLE_ID = UsecRules.RIFLE_ITEM_ID;
    public static final Identifier USEC_MAGAZINE_ID = UsecRules.MAGAZINE_ITEM_ID;
    public static final Identifier USEC_FMJ_ROUND_ID = UsecRules.FMJ_ITEM_ID;
    public static final Identifier USEC_AP_ROUND_ID = UsecRules.AP_ITEM_ID;
    public static final Identifier USEC_SUPPRESSOR_ID = UsecRules.SUPPRESSOR_ITEM_ID;
    public static final Identifier WITHER_GOHEI_ID = MiniWitherRules.GOHEI_ID;
    public static final Identifier BIG_DOG_PORTRAIT_ID = NoaRules.DOG_PORTRAIT_ITEM_ID;
    public static final Identifier ACCOUNTANT_PASSBOOK_ID = AccountantRules.PASSBOOK_ID;
    public static final Identifier SHERRY_BADGE_ID =
            dev.caecorthus.sparkdlc.role.civilian.sherry.SherryRules.BADGE_ITEM_ID;
    public static final Identifier SHERRY_NOTE_ID =
            dev.caecorthus.sparkdlc.role.civilian.sherry.SherryRules.NOTE_ITEM_ID;
    public static final Identifier PEACHWOOD_SWORD_ID = SparkDLC.id("peachwood_sword");
    public static final Identifier GHOST_TRIGGER_ID = SparkDLC.id("ghost_trigger");
    public static final Identifier SAVIOR_PISTOL_ID = SaviorRules.PISTOL_ID;
    public static final Identifier SAVIOR_LIGHTSABER_ID = SaviorRules.LIGHTSABER_ID;
    public static final Identifier HANNA_UMBRELLA_ID = HannaRules.UMBRELLA_ID;
    public static final Identifier HANNA_HANDKERCHIEF_ID = HannaRules.HANDKERCHIEF_ID;
    public static final Identifier ROPE_ID = RopeRules.ITEM_ID;
    public static final Identifier KOKO_LIGHTSTICK_ID = KokoRules.LIGHTSTICK_ID;
    public static final Identifier PLANNER_TASK_SLIP_ID = PlannerRules.TASK_SLIP_ID;
    public static final Identifier SPHINX_HAO_HAND_GUN_ID = SphinxHaoRules.HAND_GUN_ID;
    public static final Identifier SPHINX_HAO_FIST_AMMO_ID = SphinxHaoRules.FIST_AMMO_ID;
    public static final Identifier DEFENSE_VIAL_ID = DefenseVialRules.ITEM_ID;
    public static final Identifier M1903A4_ID = SparkDLC.id("m1903a4");
    public static final Identifier AMMO_3006_ID = SparkDLC.id("ammo_3006");
    private static Item ceremonialSword;
    private static Item crossSword;
    private static Item firePoker;
    private static Item perfumeEssence;
    private static Item cologne;
    private static Item tarotCard;
    private static Item ninjaKnife;
    private static Item vendettaKnife;
    private static Item ninjaShuriken;
    private static Item ninjaGrapplingHook;
    private static Item featherBlade;
    private static Item blackRavenLedger;
    private static Item blackRavenMask;
    private static Item prophetNecrology;
    private static Item hunterTrap;
    private static Item doubleBarrelShotgun;
    private static Item doubleBarrelShell;
    private static Item doubleBarrelSlug;
    private static Item poisonApple;
    private static Item tofanaElixir;
    private static Item tollBell;
    private static Item timeStealerClock;
    private static Item timeStealerGiftWatch;
    private static Item timeStamp;
    private static Item knockoutDrug;
    private static Item disruptor;
    private static Item taser;
    private static Item shockDevice;
    private static Item holyFlash;
    private static Item seekerCar;
    private static Item seekerCamera;
    private static Item fishingRod;
    private static Item fishBait;
    private static Item salmon;
    private static Item cod;
    private static Item clownfish;
    private static Item goldfish;
    private static Item keyFish;
    private static Item spareKey;
    private static Item swordfish;
    private static Item glimmerfish;
    private static Item whiteCane;
    private static Item comTac;
    private static Item shriekGun;
    private static Item deepDarkSporeFlask;
    private static Item potionLauncher;
    private static Item gwDkShell;
    private static Item gwAcShell;
    private static Item gwMrShell;
    private static Item trShell;
    private static Item riftGate;
    private static Item riftGateRemover;
    private static Item usecRifle;
    private static Item usecMagazine;
    private static Item usecFmjRound;
    private static Item usecApRound;
    private static Item usecSuppressor;
    private static Item witherGohei;
    private static Item peachwoodSword;
    private static Item sherryBadge;
    private static Item sherryNote;
    private static Item ghostTrigger;
    private static Item bigDogPortrait;
    private static Item accountantPassbook;
    private static Item saviorPistol;
    private static Item saviorLightsaber;
    private static Item hannaUmbrella;
    private static Item hannaHandkerchief;
    private static Item rope;
    private static Item kokoLightstick;
    private static Item plannerTaskSlip;
    private static Item sphinxHaoHandGun;
    private static Item sphinxHaoFistAmmo;
    private static Item defenseVial;
    private static Item m1903a4;
    private static Item ammo3006;

    private static boolean roleItemsRegistered;

    public static synchronized void registerRoleItems() {
        if (roleItemsRegistered) {
            return;
        }
        ceremonialSword = Registry.register(
                Registries.ITEM,
                CEREMONIAL_SWORD_ID,
                new CeremonialSwordItem(CeremonialSwordItem.createSettings())
        );
        firePoker = Registry.register(
                Registries.ITEM,
                FIRE_POKER_ID,
                new Item(new Item.Settings().maxCount(1))
        );
        perfumeEssence = Registry.register(
                Registries.ITEM,
                PERFUME_ESSENCE_ID,
                new PerfumeEssenceItem(new Item.Settings().maxCount(1))
        );
        cologne = Registry.register(
                Registries.ITEM,
                COLOGNE_ID,
                new CologneItem(new Item.Settings().maxCount(1))
        );
        tarotCard = Registry.register(
                Registries.ITEM,
                TAROT_CARD_ID,
                new Item(new Item.Settings().maxCount(1))
        );
        ninjaKnife = Registry.register(
                Registries.ITEM,
                NINJA_KNIFE_ID,
                new NinjaKnifeItem(new Item.Settings().maxCount(1))
        );
        vendettaKnife = Registry.register(
                Registries.ITEM,
                VENDETTA_KNIFE_ID,
                new VendettaKnifeItem(new Item.Settings().maxCount(1))
        );
        ninjaShuriken = Registry.register(
                Registries.ITEM,
                NINJA_SHURIKEN_ID,
                new NinjaShurikenItem(new Item.Settings().maxCount(1))
        );
        ninjaGrapplingHook = Registry.register(
                Registries.ITEM,
                NINJA_GRAPPLING_HOOK_ID,
                new NinjaGrapplingHookItem(new Item.Settings().maxCount(1))
        );
        featherBlade = Registry.register(
                Registries.ITEM,
                FEATHER_BLADE_ID,
                new FeatherBladeItem(new Item.Settings().maxCount(1))
        );
        blackRavenLedger = Registry.register(
                Registries.ITEM,
                BLACK_RAVEN_LEDGER_ID,
                new BlackRavenLedgerItem(new Item.Settings().maxCount(1))
        );
        blackRavenMask = Registry.register(
                Registries.ITEM,
                BLACK_RAVEN_MASK_ID,
                new BlackRavenMaskItem(new Item.Settings().maxCount(1))
        );
        prophetNecrology = Registry.register(
                Registries.ITEM,
                PROPHET_NECROLOGY_ID,
                new ProphetNecrologyItem(new Item.Settings().maxCount(1))
        );
        hunterTrap = Registry.register(
                Registries.ITEM,
                HUNTER_TRAP_ID,
                new HunterTrapItem(HunterTrapItem.createSettings())
        );
        doubleBarrelShotgun = Registry.register(
                Registries.ITEM,
                DOUBLE_BARREL_SHOTGUN_ID,
                new DoubleBarrelShotgunItem(DoubleBarrelShotgunItem.createSettings())
        );
        doubleBarrelShell = Registry.register(
                Registries.ITEM,
                DOUBLE_BARREL_SHELL_ID,
                new DoubleBarrelShellItem(DoubleBarrelShellItem.createSettings())
        );
        doubleBarrelSlug = Registry.register(
                Registries.ITEM,
                DOUBLE_BARREL_SLUG_ID,
                new DoubleBarrelSlugItem(DoubleBarrelSlugItem.createSettings())
        );
        poisonApple = Registry.register(
                Registries.ITEM,
                POISON_APPLE_ID,
                new PoisonAppleItem(new Item.Settings().maxCount(1))
        );
        tofanaElixir = Registry.register(
                Registries.ITEM,
                TOFANA_ELIXIR_ID,
                new Item(new Item.Settings().maxCount(1))
        );
        tollBell = Registry.register(
                Registries.ITEM,
                TOLL_BELL_ID,
                new TollBellItem(TollBellItem.createSettings())
        );
        timeStealerClock = Registry.register(
                Registries.ITEM,
                TIME_STEALER_CLOCK_ID,
                new TimeStealerClockItem(TimeStealerClockItem.createSettings())
        );
        timeStealerGiftWatch = Registry.register(
                Registries.ITEM,
                TIME_STEALER_GIFT_WATCH_ID,
                new TimeGiftWatchItem(TimeGiftWatchItem.createSettings())
        );
        timeStamp = Registry.register(
                Registries.ITEM,
                TIME_STAMP_ID,
                new TimeStampItem(TimeStampItem.createSettings())
        );
        knockoutDrug = Registry.register(
                Registries.ITEM,
                KNOCKOUT_DRUG_ID,
                new KnockoutDrugItem(new Item.Settings().maxCount(4))
        );
        disruptor = Registry.register(
                Registries.ITEM,
                DISRUPTOR_ID,
                new DisruptorItem(DisruptorItem.createSettings())
        );
        taser = Registry.register(
                Registries.ITEM,
                TASER_ID,
                new TaserItem(TaserItem.createSettings())
        );
        shockDevice = Registry.register(
                Registries.ITEM,
                SHOCK_DEVICE_ID,
                new ShockDeviceItem(ShockDeviceItem.createSettings())
        );
        holyFlash = Registry.register(
                Registries.ITEM,
                HOLY_FLASH_ID,
                new HolyFlashItem(HolyFlashItem.createSettings())
        );
        seekerCar = Registry.register(
                Registries.ITEM,
                SEEKER_CAR_ID,
                new SeekerCarItem(SeekerCarItem.createSettings())
        );
        seekerCamera = Registry.register(
                Registries.ITEM,
                SEEKER_CAMERA_ID,
                new SeekerCameraItem(SeekerCameraItem.createSettings())
        );
        fishingRod = Registry.register(
                Registries.ITEM,
                FISHING_ROD_ID,
                new FishingRodItem(FishingRodItem.createSettings())
        );
        fishBait = Registry.register(
                Registries.ITEM,
                FISH_BAIT_ID,
                new FishBaitItem(FishBaitItem.createSettings())
        );
        salmon = Registry.register(
                Registries.ITEM,
                SALMON_ID,
                new FisherFishItem(FisherFishItem.createSettings(), FisherFishKind.SALMON)
        );
        cod = Registry.register(
                Registries.ITEM,
                COD_ID,
                new FisherFishItem(FisherFishItem.createSettings(), FisherFishKind.COD)
        );
        clownfish = Registry.register(
                Registries.ITEM,
                CLOWNFISH_ID,
                new FisherFishItem(FisherFishItem.createSettings(), FisherFishKind.CLOWNFISH)
        );
        goldfish = Registry.register(
                Registries.ITEM,
                GOLDFISH_ID,
                new FisherFishItem(FisherFishItem.createSettings(), FisherFishKind.GOLDFISH)
        );
        keyFish = Registry.register(
                Registries.ITEM,
                KEY_FISH_ID,
                new KeyFishItem(KeyFishItem.createSettings())
        );
        // Spare Key: the NoellesRoles Attendant's round-start key, usable by any holder; 7 points, train door 3, key-locked
        // small door 1, 5 s cooldown after each opening.
        // 备用钥匙：NoellesRoles 乘务员的开局钥匙，任何持有者都能使用；共 7 点，列车门 3 点、上锁小门 1 点，每次开门后冷却 5 秒。
        spareKey = Registry.register(
                Registries.ITEM,
                SPARE_KEY_ID,
                new SpareKeyItem(SpareKeyItem.createSettings())
        );
        swordfish = Registry.register(
                Registries.ITEM,
                SWORDFISH_ID,
                new SwordfishItem(SwordfishItem.createSettings())
        );
        glimmerfish = Registry.register(
                Registries.ITEM,
                GLIMMERFISH_ID,
                new FisherFishItem(FisherFishItem.createSettings(), FisherFishKind.GLIMMERFISH)
        );
        whiteCane = Registry.register(
                Registries.ITEM,
                WHITE_CANE_ID,
                new WhiteCaneItem(WhiteCaneItem.createSettings())
        );
        comTac = Registry.register(
                Registries.ITEM,
                COMTAC_ID,
                new ComTacItem(ComTacItem.createSettings())
        );
        shriekGun = Registry.register(
                Registries.ITEM,
                SHRIEK_GUN_ID,
                new ShriekGunItem(ShriekGunItem.createSettings())
        );
        deepDarkSporeFlask = Registry.register(
                Registries.ITEM,
                DEEP_DARK_SPORE_FLASK_ID,
                new DeepDarkSporeFlaskItem(DeepDarkSporeFlaskItem.createSettings())
        );
        potionLauncher = Registry.register(
                Registries.ITEM,
                POTION_LAUNCHER_ID,
                new PotionLauncherItem(PotionLauncherItem.createSettings())
        );
        gwDkShell = Registry.register(
                Registries.ITEM,
                PotionShellType.DK.itemId(),
                new PotionShellItem(PotionShellType.DK, PotionShellItem.createSettings())
        );
        gwAcShell = Registry.register(
                Registries.ITEM,
                PotionShellType.AC.itemId(),
                new PotionShellItem(PotionShellType.AC, PotionShellItem.createSettings())
        );
        gwMrShell = Registry.register(
                Registries.ITEM,
                PotionShellType.MR.itemId(),
                new PotionShellItem(PotionShellType.MR, PotionShellItem.createSettings())
        );
        trShell = Registry.register(
                Registries.ITEM,
                PotionShellType.TR.itemId(),
                new PotionShellItem(PotionShellType.TR, PotionShellItem.createSettings())
        );
        riftGate = Registry.register(
                Registries.ITEM,
                RIFT_GATE_ID,
                new RiftGateItem(RiftGateItem.createSettings())
        );
        riftGateRemover = Registry.register(
                Registries.ITEM,
                RIFT_GATE_REMOVER_ID,
                new RiftGateRemoverItem(RiftGateRemoverItem.createSettings())
        );
        // USEC (police variant): the rifle, its magazine, the two .338 rounds and the suppressor; appended last.
        // USEC（警察变体）：狙击步枪、弹匣、两种 .338 子弹与消音器；追加在最后。
        usecRifle = Registry.register(
                Registries.ITEM,
                USEC_RIFLE_ID,
                new UsecRifleItem(UsecRifleItem.createSettings())
        );
        usecMagazine = Registry.register(
                Registries.ITEM,
                USEC_MAGAZINE_ID,
                new UsecMagazineItem(UsecMagazineItem.createSettings())
        );
        usecFmjRound = Registry.register(
                Registries.ITEM,
                USEC_FMJ_ROUND_ID,
                new UsecAmmoItem(UsecAmmoType.FMJ, UsecAmmoItem.createSettings())
        );
        usecApRound = Registry.register(
                Registries.ITEM,
                USEC_AP_ROUND_ID,
                new UsecAmmoItem(UsecAmmoType.AP, UsecAmmoItem.createSettings())
        );
        usecSuppressor = Registry.register(
                Registries.ITEM,
                USEC_SUPPRESSOR_ID,
                new UsecSuppressorItem(UsecSuppressorItem.createSettings())
        );
        // Witch Maiden Wither Gohei (Mini Wither unlock); appended last. / 巫女凋灵御币（小凋灵解锁）；追加在最后。
        witherGohei = Registry.register(
                Registries.ITEM,
                WITHER_GOHEI_ID,
                new WitherGoheiItem(WitherGoheiItem.createSettings())
        );
        // Knight's cross sword: Wathe knife logic and sounds, rendered as a scaled vanilla-style sword.
        // 骑士的十字剑：沿用 Wathe 小刀的逻辑与音效，按放大后的原版剑样式渲染。
        crossSword = Registry.register(
                Registries.ITEM,
                CROSS_SWORD_ID,
                new CrossSwordItem(new Item.Settings().maxCount(1))
        );
        // Noa's Big Dog Portrait: a plain carried token with no use action. NoaKillWatch finds it by registry id
        // (NoaRules.DOG_PORTRAIT_ITEM_ID); appended last.
        // 诺亚的大狗画像：无使用动作的普通携带物品。NoaKillWatch 按注册 ID（NoaRules.DOG_PORTRAIT_ITEM_ID）识别；追加在最后。
        bigDogPortrait = Registry.register(
                Registries.ITEM,
                BIG_DOG_PORTRAIT_ID,
                new Item(new Item.Settings().maxCount(1))
        );
        // Accountant passbook (PR #195): stored coins live in the stack's custom data, so maxCount(1) is mandatory; a
        // stack would share one stored value and splitting it would duplicate coins. Appended last.
        // 会计的存折（PR #195）：存款保存在物品的自定义数据中，因此必须 maxCount(1)；堆叠会共享同一存款值，拆分即复制金币。
        // 追加在最后。
        accountantPassbook = Registry.register(
                Registries.ITEM,
                ACCOUNTANT_PASSBOOK_ID,
                new AccountantPassbookItem(new Item.Settings().maxCount(1))
        );
        // Specter (PR #207) items, appended last so earlier registration order stays unchanged.
        // 幽魂（PR #207）物品，追加在最后以保持既有注册顺序不变。
        // Specter counterplay: the peachwood sword handed to low-sanity players while the Otherworld is open.
        // 幽魂的反制：里世界期间发给低理智玩家的桃木剑。
        peachwoodSword = Registry.register(
                Registries.ITEM,
                PEACHWOOD_SWORD_ID,
                new PeachwoodSwordItem(PeachwoodSwordItem.createSettings())
        );
        // Specter shop: an invisible placed trigger spent by the first player who steps on it.
        // 幽魂商店：放在地上隐形的触发器，第一个踩到的玩家将其消耗。
        ghostTrigger = Registry.register(
                Registries.ITEM,
                GHOST_TRIGGER_ID,
                new GhostTriggerItem(GhostTriggerItem.createSettings())
        );
        // Savior (police): a self-contained revolver copy and a bat-stat lightsaber.
        // 救世主（警类）：自包含的左轮复制品与球棒属性的光剑。
        saviorPistol = Registry.register(
                Registries.ITEM,
                SAVIOR_PISTOL_ID,
                new SaviorPistolItem(SaviorPistolItem.createSettings())
        );
        saviorLightsaber = Registry.register(
                Registries.ITEM,
                SAVIOR_LIGHTSABER_ID,
                new SaviorLightsaberItem(SaviorLightsaberItem.createSettings())
        );
        // Tono Hanna's shop items (PR #212), usable by any holder: the parasol (25 durability, 1 per second held) and
        // the handkerchief (2 uses). Uses are vanilla durability, so maxCount(1). Appended last.
        // 远野汉娜的商店物品（PR #212），谁拿到都能用：洋伞（25 点耐久，手持每秒 1 点）与手帕（2 次）。次数即原版耐久，
        // 因此 maxCount(1)。追加在最后。
        hannaUmbrella = Registry.register(
                Registries.ITEM,
                HANNA_UMBRELLA_ID,
                new HannaUmbrellaItem(HannaUmbrellaItem.createSettings())
        );
        hannaHandkerchief = Registry.register(
                Registries.ITEM,
                HANNA_HANDKERCHIEF_ID,
                new HannaHandkerchiefItem(HannaHandkerchiefItem.createSettings())
        );
        // 橘雪莉 (PR #206) items, appended last so earlier registration order stays unchanged. The badge is a plain
        // token handed over by SherryBadgeService (no use action of its own); the note names a dead badge holder in its
        // custom data, so maxCount(1) keeps two notes from merging into one name.
        // 橘雪莉（PR #206）物品，追加在最后以保持既有注册顺序不变。徽章是普通携带物，由 SherryBadgeService 负责赠送（自身没有
        // 使用动作）；纸条在自定义数据中写着死去的徽章持有者名字，因此用 maxCount(1) 防止两张纸条合并成同一个名字。
        sherryBadge = Registry.register(
                Registries.ITEM,
                SHERRY_BADGE_ID,
                new dev.caecorthus.sparkdlc.role.civilian.sherry.SherryBadgeItem(new Item.Settings().maxCount(1))
        );
        sherryNote = Registry.register(
                Registries.ITEM,
                SHERRY_NOTE_ID,
                new dev.caecorthus.sparkdlc.role.civilian.sherry.SherryNoteItem(new Item.Settings().maxCount(1))
        );
        // 二阶堂希罗's Rope (2026-11-11), appended last, after 橘雪莉's items, so earlier registration order
        // stays unchanged. / 二阶堂希罗的绳索（2026-11-11），追加在最后（橘雪莉的物品之后），以保持既有注册顺序不变。
        rope = Registry.register(
                Registries.ITEM,
                ROPE_ID,
                new RopeItem(RopeItem.createSettings())
        );
        // 泽渡可可's lightstick (PR #223), appended last, after 二阶堂希罗's Rope, so earlier registration order stays
        // unchanged. / 泽渡可可的应援棒（PR #223），追加在最后（二阶堂希罗的绳索之后），以保持既有注册顺序不变。
        kokoLightstick = Registry.register(
                Registries.ITEM,
                KOKO_LIGHTSTICK_ID,
                new KokoLightstickItem(KokoLightstickItem.createSettings())
        );
        // 规划大师's task slip (PR #219), appended last, after 泽渡可可's lightstick, so earlier registration order stays
        // unchanged. Any holder may use it; maxCount(1). / 规划大师的任务单（PR #219），追加在最后（泽渡可可的应援棒之后），
        // 以保持既有注册顺序不变。谁拿到都能用；maxCount(1)。
        plannerTaskSlip = Registry.register(
                Registries.ITEM,
                PLANNER_TASK_SLIP_ID,
                new PlannerTaskSlipItem(PlannerTaskSlipItem.createSettings())
        );
        // 狮身豪's spring-fist hand gun (one fist, loaded flag in a data component, so maxCount(1)) and its fist ammo
        // (stacks of 16), appended last, after 规划大师's task slip, so earlier registration order stays unchanged.
        // 狮身豪的弹簧拳头手枪（装一发拳头，装填标记在数据组件中，因此 maxCount(1)）与拳头子弹（16 个一组），追加在最后
        // （规划大师的任务单之后），以保持既有注册顺序不变。
        sphinxHaoHandGun = Registry.register(
                Registries.ITEM,
                SPHINX_HAO_HAND_GUN_ID,
                new SphinxHaoHandGunItem(SphinxHaoHandGunItem.createSettings())
        );
        sphinxHaoFistAmmo = Registry.register(
                Registries.ITEM,
                SPHINX_HAO_FIST_AMMO_ID,
                new SphinxHaoFistAmmoItem(SphinxHaoFistAmmoItem.createSettings())
        );
        // Defense Vial (PR #240, any holder), appended last so earlier registration order stays unchanged.
        // 防御药剂（PR #240，任何持有者可用），追加在最后，以保持既有注册顺序不变。
        defenseVial = Registry.register(
                Registries.ITEM,
                DEFENSE_VIAL_ID,
                new DefenseVialItem(DefenseVialItem.createSettings())
        );
        // Map-maker beds (PR #239): blocks plus their BlockItems, appended after every existing item so earlier
        // registration order stays unchanged.
        // 地图作者用的床（PR #239）：方块及其 BlockItem，追加在所有既有物品之后，以保持既有注册顺序不变。
        SparkDlcBlocks.registerBeds();
        // The Sniper's M1903A4 (five-round stack in custom data, so maxCount(1)) and its .30-06 rounds (stacks of 64),
        // appended last, after the PR #239 beds, so earlier registration order stays unchanged.
        // 狙击手的 M1903A4（五发弹栈存于自定义数据，因此 maxCount(1)）与 .30-06 子弹（64 个一组），追加在最后
        // （PR #239 的床之后），以保持既有注册顺序不变。
        m1903a4 = Registry.register(
                Registries.ITEM,
                M1903A4_ID,
                new M1903A4Item(M1903A4Item.createSettings())
        );
        ammo3006 = Registry.register(
                Registries.ITEM,
                AMMO_3006_ID,
                new Ammo3006Item(Ammo3006Item.createSettings())
        );
        // Map-maker yellowed wallpaper (PR #1): blocks plus their BlockItems, appended last, after the M1903A4 and its
        // rounds, so earlier registration order stays unchanged.
        // 地图作者用的泛黄墙纸（PR #1）：方块及其 BlockItem，追加在最后（M1903A4 及其子弹之后），以保持既有注册顺序不变。
        SparkDlcBlocks.registerWallpaper();
        registerMeleeSuppression();
        VendettaKnifeLoadoutService.register();
        roleItemsRegistered = true;
    }

    public static Item ceremonialSword() {
        if (ceremonialSword == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return ceremonialSword;
    }

    public static Item crossSword() {
        if (crossSword == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return crossSword;
    }

    public static Item peachwoodSword() {
        if (peachwoodSword == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return peachwoodSword;
    }

    public static Item ghostTrigger() {
        if (ghostTrigger == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return ghostTrigger;
    }

    public static Item saviorPistol() {
        if (saviorPistol == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return saviorPistol;
    }

    public static Item saviorLightsaber() {
        if (saviorLightsaber == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return saviorLightsaber;
    }

    public static Item rope() {
        if (rope == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return rope;
    }

    public static Item sphinxHaoHandGun() {
        if (sphinxHaoHandGun == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return sphinxHaoHandGun;
    }

    public static Item sphinxHaoFistAmmo() {
        if (sphinxHaoFistAmmo == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return sphinxHaoFistAmmo;
    }

    public static Item kokoLightstick() {
        if (kokoLightstick == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return kokoLightstick;
    }

    public static Item defenseVial() {
        if (defenseVial == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return defenseVial;
    }

    public static Item plannerTaskSlip() {
        if (plannerTaskSlip == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return plannerTaskSlip;
    }

    public static Item firePoker() {
        if (firePoker == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return firePoker;
    }

    public static Item perfumeEssence() {
        if (perfumeEssence == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return perfumeEssence;
    }

    public static Item cologne() {
        if (cologne == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return cologne;
    }

    public static Item tarotCard() {
        if (tarotCard == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return tarotCard;
    }

    public static Item ninjaKnife() {
        if (ninjaKnife == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return ninjaKnife;
    }

    public static Item vendettaKnife() {
        if (vendettaKnife == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return vendettaKnife;
    }

    public static Item ninjaShuriken() {
        if (ninjaShuriken == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return ninjaShuriken;
    }

    public static Item ninjaGrapplingHook() {
        if (ninjaGrapplingHook == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return ninjaGrapplingHook;
    }

    public static Item featherBlade() {
        if (featherBlade == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return featherBlade;
    }

    public static Item blackRavenLedger() {
        if (blackRavenLedger == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return blackRavenLedger;
    }

    public static Item blackRavenMask() {
        if (blackRavenMask == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return blackRavenMask;
    }

    public static Item prophetNecrology() {
        if (prophetNecrology == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return prophetNecrology;
    }

    public static Item hunterTrap() {
        if (hunterTrap == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return hunterTrap;
    }

    public static Item doubleBarrelShotgun() {
        if (doubleBarrelShotgun == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return doubleBarrelShotgun;
    }

    public static Item doubleBarrelShell() {
        if (doubleBarrelShell == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return doubleBarrelShell;
    }

    public static Item doubleBarrelSlug() {
        if (doubleBarrelSlug == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return doubleBarrelSlug;
    }

    public static Item poisonApple() {
        if (poisonApple == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return poisonApple;
    }

    public static Item tofanaElixir() {
        if (tofanaElixir == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return tofanaElixir;
    }

    public static Item tollBell() {
        if (tollBell == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return tollBell;
    }

    public static Item timeStealerClock() {
        if (timeStealerClock == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return timeStealerClock;
    }

    public static Item timeStealerGiftWatch() {
        if (timeStealerGiftWatch == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return timeStealerGiftWatch;
    }

    public static Item timeStamp() {
        if (timeStamp == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return timeStamp;
    }

    public static Item knockoutDrug() {
        if (knockoutDrug == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return knockoutDrug;
    }

    public static Item disruptor() {
        if (disruptor == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return disruptor;
    }

    public static Item taser() {
        if (taser == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return taser;
    }

    public static Item shockDevice() {
        if (shockDevice == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return shockDevice;
    }

    public static Item holyFlash() {
        if (holyFlash == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return holyFlash;
    }

    public static Item seekerCar() {
        if (seekerCar == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return seekerCar;
    }

    public static Item seekerCamera() {
        if (seekerCamera == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return seekerCamera;
    }

    private static void registerMeleeSuppression() {
        // Kunai uses Wathe's player-only punching path so its left click keeps the native shove contract.
        // 苦无通过 Wathe 的仅玩家攻击路径处理左键，从而沿用原生击退规则。
        AllowPlayerPunching.EVENT.register((attacker, victim) ->
                attacker.getMainHandStack().isOf(ninjaKnife)
        );
        // Cross Sword keeps the knife's left-click punching contract. / 十字剑沿用与小刀一致的主手攻击规则。
        AllowPlayerPunching.EVENT.register((attacker, victim) ->
                attacker.getMainHandStack().isOf(crossSword)
        );
        AllowPlayerPunching.EVENT.register((attacker, victim) ->
                attacker.getMainHandStack().isOf(vendettaKnife)
                        && (attacker == victim
                        ? VendettaInteractionService.isActiveVendetta(attacker)
                        : VendettaInteractionService.isExactPair(attacker, victim))
        );
        // Shuriken kills through its explicit server path and must never fall back to vanilla melee damage.
        // 手里剑只通过明确的服务端路径击杀，不能回退为原版近战伤害。
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            Item heldItem = player.getStackInHand(hand).getItem();
            return heldItem == ninjaShuriken
                    ? ActionResult.FAIL
                    : ActionResult.PASS;
        });
    }

    public static Item fishingRod() {
        if (fishingRod == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return fishingRod;
    }

    public static Item fishBait() {
        if (fishBait == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return fishBait;
    }

    public static Item salmon() {
        if (salmon == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return salmon;
    }

    public static Item cod() {
        if (cod == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return cod;
    }

    public static Item clownfish() {
        if (clownfish == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return clownfish;
    }

    public static Item goldfish() {
        if (goldfish == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return goldfish;
    }

    public static Item keyFish() {
        if (keyFish == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return keyFish;
    }

    public static Item spareKey() {
        if (spareKey == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return spareKey;
    }

    public static Item swordfish() {
        if (swordfish == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return swordfish;
    }

    public static Item glimmerfish() {
        if (glimmerfish == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return glimmerfish;
    }

    public static Item whiteCane() {
        if (whiteCane == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return whiteCane;
    }

    public static Item comTac() {
        if (comTac == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return comTac;
    }

    public static Item shriekGun() {
        if (shriekGun == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return shriekGun;
    }

    public static Item deepDarkSporeFlask() {
        if (deepDarkSporeFlask == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return deepDarkSporeFlask;
    }

    public static Item potionLauncher() {
        if (potionLauncher == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return potionLauncher;
    }

    public static Item gwDkShell() {
        if (gwDkShell == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return gwDkShell;
    }

    public static Item gwAcShell() {
        if (gwAcShell == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return gwAcShell;
    }

    public static Item gwMrShell() {
        if (gwMrShell == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return gwMrShell;
    }

    public static Item trShell() {
        if (trShell == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return trShell;
    }

    public static Item potionShell(PotionShellType type) {
        return switch (type) {
            case DK -> gwDkShell();
            case AC -> gwAcShell();
            case MR -> gwMrShell();
            case TR -> trShell();
        };
    }

    public static Item riftGate() {
        if (riftGate == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return riftGate;
    }

    public static Item riftGateRemover() {
        if (riftGateRemover == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return riftGateRemover;
    }

    public static Item usecRifle() {
        if (usecRifle == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return usecRifle;
    }

    public static Item usecMagazine() {
        if (usecMagazine == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return usecMagazine;
    }

    public static Item usecFmjRound() {
        if (usecFmjRound == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return usecFmjRound;
    }

    public static Item usecApRound() {
        if (usecApRound == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return usecApRound;
    }

    public static Item usecAmmo(UsecAmmoType type) {
        return switch (type) {
            case FMJ -> usecFmjRound();
            case AP -> usecApRound();
        };
    }

    public static Item usecSuppressor() {
        if (usecSuppressor == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return usecSuppressor;
    }

    public static Item witherGohei() {
        if (witherGohei == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return witherGohei;
    }

    public static Item bigDogPortrait() {
        if (bigDogPortrait == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return bigDogPortrait;
    }

    public static Item accountantPassbook() {
        if (accountantPassbook == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return accountantPassbook;
    }

    public static Item hannaUmbrella() {
        if (hannaUmbrella == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return hannaUmbrella;
    }

    public static Item hannaHandkerchief() {
        if (hannaHandkerchief == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return hannaHandkerchief;
    }

    public static Item sherryBadge() {
        if (sherryBadge == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return sherryBadge;
    }

    public static Item sherryNote() {
        if (sherryNote == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return sherryNote;
    }

    public static Item m1903a4() {
        if (m1903a4 == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return m1903a4;
    }

    public static Item ammo3006() {
        if (ammo3006 == null) {
            throw new IllegalStateException("SparkDLC role items are not registered yet");
        }
        return ammo3006;
    }
}
