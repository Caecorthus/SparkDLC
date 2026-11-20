package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.command.SparkDlcCommands;
import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesCollisionCompatibility;
import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesHiddenEquipment;
import dev.caecorthus.sparkdlc.event.SparkStrengthEvents;
import dev.caecorthus.sparkdlc.event.SparkWitchEvents;
import dev.caecorthus.sparkdlc.faction.FactionCompatibilityEvents;
import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.legacy.LegacyFiles;
import dev.caecorthus.sparkdlc.legacy.LegacyRegistryAliases;
import dev.caecorthus.sparkdlc.net.SparkStrengthPackets;
import dev.caecorthus.sparkdlc.net.SparkTraitsPackets;
import dev.caecorthus.sparkdlc.net.SparkWitchPackets;
import dev.caecorthus.sparkdlc.net.version.ServerVersionHandshake;
import dev.caecorthus.sparkdlc.net.version.SparkTraitsVersionHandshake;
import dev.caecorthus.sparkdlc.net.version.SparkWitchVersionHandshake;
import dev.caecorthus.sparkdlc.record.MatchRecordBroadcaster;
import dev.caecorthus.sparkdlc.record.PsychoRecorder;
import dev.caecorthus.sparkdlc.replay.SparkReplayBootstrap;
import dev.caecorthus.sparkdlc.replay.role.SparkStrengthReplayFormatters;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.ControlExpertEntities;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelEffects;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelRules;
import dev.caecorthus.sparkdlc.role.civilian.orthopedist.OrthopedistEffects;
import dev.caecorthus.sparkdlc.role.civilian.saint.flash.HolyFlashEntities;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorParticles;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerSounds;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerEntities;
import dev.caecorthus.sparkdlc.role.killer.blackraven.crow.BlackRavenCrowEntities;
import dev.caecorthus.sparkdlc.role.killer.blackraven.crow.BlackRavenFeatherRegistry;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterEffects;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterEntities;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterEntities;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterParticles;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.FocusedFootstepsEffects;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.wither.MiniWitherEntities;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithParticipationRules;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithStateService;
import dev.caecorthus.sparkdlc.role.witch.abysslistener.zone.AbyssListenerEntities;
import dev.caecorthus.sparkdlc.role.witch.curser.CurserFeatureService;
import dev.caecorthus.sparkdlc.role.witch.potiongunner.shell.PotionGunnerEntities;
import dev.caecorthus.sparkdlc.role.witch.riftwalker.gate.RiftGateEntities;
import dev.caecorthus.sparkdlc.round.antifastround.AntiFastRoundBootstrap;
import dev.caecorthus.sparkdlc.round.mapreset.LobbyMapResetBootstrap;
import dev.caecorthus.sparkdlc.skill.SparkWitchBuiltInSkills;
import dev.caecorthus.sparkdlc.team.CaecorthusTeamSync;
import dev.caecorthus.sparkdlc.trait.civilian.jester.JesterTraitService;
import dev.caecorthus.sparkdlc.trait.civilian.laststand.LastStandFinalMomentService;
import dev.caecorthus.sparkdlc.trait.civilian.laststand.LastStandService;
import dev.caecorthus.sparkdlc.trait.lifecycle.TraitGameHooks;
import dev.caecorthus.sparkdlc.trait.registry.SparkTraitsBuiltInTraits;
import dev.caecorthus.sparkdlc.trait.replay.SparkTraitsReplayEvents;
import dev.caecorthus.sparkdlc.trait.replay.SparkTraitsReplayTooltips;
import dev.caecorthus.sparkdlc.util.hitscan.PlayerHitboxHistory;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entrypoint of SparkDLC, and the whole common start-up of the mod: {@link #onInitialize} is the list of
 * steps, each step is a method below that names what it registers.
 *
 * <p><b>The order of the steps and of the calls inside them is behaviour, not layout.</b> Listeners on one event run
 * in registration order and many of them are first-wins or edit a shared list (kill, win-condition, shop, door,
 * blackout, chat and login-query events); the faction policy lists (effective-faction resolvers, player-affect
 * rules, collision exemptions) are evaluated in registration order; a creative tab lists items in the order of its
 * entries. The sequence is the one the mod had as five separate mods started in mod-id order (faction and round
 * services, then the kits of the Wathe and NoellesRoles roles, then traits, then the mod's own roles), and
 * {@code ./gradlew runGameTest -PstartupOrderDump=<dir>} writes it down so that two builds can be compared. Add a
 * new registration at the end of the step it belongs to; move an existing one only with that comparison in hand.</p>
 *
 * SparkDLC 的通用入口，也是本模组通用端的全部启动流程：{@link #onInitialize} 是步骤清单，每个步骤是下方的一个方法，
 * 方法名说明它注册的内容。
 *
 * <p><b>步骤之间以及步骤内部调用的顺序属于行为，而不是排版。</b>同一事件的监听器按注册顺序执行，其中许多是先到先得或会修改
 * 共享列表（击杀、胜利条件、商店、门、停电、聊天与登录查询事件）；阵营策略列表（生效阵营解析器、玩家影响规则、碰撞豁免）
 * 按注册顺序求值；创造模式物品栏按条目顺序列出物品。该顺序沿用本模组还是五个独立 mod 时按 mod id 启动的顺序（阵营与对局服务，
 * 然后是 Wathe 与 NoellesRoles 职业的套件，然后是词条，最后是本模组自有职业），
 * {@code ./gradlew runGameTest -PstartupOrderDump=<目录>} 会把它写下来，以便比较两个构建。新增注册请追加到所属步骤的末尾；
 * 只有拿着这份比较结果才可以移动已有的注册。</p>
 */
public final class SparkDLC implements ModInitializer {
    public static final String MOD_ID = "sparkdlc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** An id of this mod: {@code sparkdlc:<path>}. / 本模组的 id：{@code sparkdlc:<path>}。 */
    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Files written under the old names, before any module reads the new ones (the team-list cache is read by
        // the faction and round services on both sides; the client-only files follow in SparkDLCClient).
        // 以旧名字写出的文件，要在任何模块读取新名字之前带过来（团队名单缓存在两端都由阵营与对局服务在启动时读取；
        // 仅客户端的文件在 SparkDLCClient 中处理）。
        LegacyFiles.carryOverCommon(FabricLoader.getInstance().getConfigDir());
        startFactionAndRoundServices();
        registerRoleKitContent();
        registerRoleKitGameplay();
        registerTraits();
        registerOwnRoleContent();
        registerOwnRolesAndSkills();
        registerOwnRoleGameplay();
        // Every command of the mod, and the old command names, in one place.
        // 本模组的全部命令以及旧命令名，统一在一处注册。
        SparkDlcCommands.register();
        // One creative tab for the whole mod, once every module has registered its items.
        // 所有模块注册完物品后，注册全模组唯一的创造模式物品栏。
        SparkDlcItemGroups.register();
        // Old registry ids of the five former mods, once every new id is registered and before the registries freeze.
        // 五个旧模组的旧注册表 id：在所有新 id 注册完成之后、注册表冻结之前设为别名。
        LegacyRegistryAliases.register();
    }

    /**
     * Version handshake, the faction registry with the native factions and the faction win service, NoellesRoles
     * collision glue, compatibility events, replay, match record, anti-fast-round, lobby map reset and team sync.
     * 版本握手、阵营注册表（含原生阵营与阵营胜利服务）、NoellesRoles 碰撞兼容、兼容事件、回放、对局记录、防速通、
     * 大厅地图重置与团队名单同步。
     */
    private static void startFactionAndRoundServices() {
        ServerVersionHandshake.registerServer();
        SparkFactionApi.bootstrap();
        NoellesCollisionCompatibility.register();
        FactionCompatibilityEvents.register();
        SparkReplayBootstrap.register();
        MatchRecordBroadcaster.register();
        PsychoRecorder.register();
        AntiFastRoundBootstrap.register();
        LobbyMapResetBootstrap.register();
        CaecorthusTeamSync.start();
    }

    /**
     * Sounds, entities, data components, blocks and items of the kits for the Wathe and NoellesRoles roles.
     * Wathe 与 NoellesRoles 职业套件的声音、实体、数据组件、方块与物品。
     */
    private static void registerRoleKitContent() {
        SparkStrengthSounds.initialize();
        SparkStrengthEntities.register();
        SparkStrengthDataComponents.register();
        SparkStrengthBlocks.register();
        SparkStrengthItems.register();
        SparkStrengthItemGroups.register();
    }

    /**
     * Packets, event listeners and replay formatters of those kits, of sabotage, the tablet and the killer team
     * economy. 这些套件以及破坏、平板电脑与杀手团队经济的数据包、事件监听器和回放格式化器。
     */
    private static void registerRoleKitGameplay() {
        SparkStrengthPackets.registerServer();
        SparkStrengthEvents.register();
        SparkStrengthReplayFormatters.register();
    }

    /**
     * Version handshake, packets, data component, particles and sounds of the trait system, the built-in traits and
     * their services and replay texts. 词条系统的版本握手、数据包、数据组件、粒子与声音，内置词条及其服务和回放文本。
     */
    private static void registerTraits() {
        SparkTraitsVersionHandshake.registerServer();
        SparkTraitsPackets.register();
        SparkTraitsDataComponentTypes.init();
        SparkTraitsParticles.register();
        SparkTraitsSounds.initialize();
        SparkTraitsBuiltInTraits.register();
        LastStandService.register();
        LastStandFinalMomentService.register();
        JesterTraitService.register();
        TraitGameHooks.register();
        SparkTraitsReplayTooltips.register();
        SparkTraitsReplayEvents.registerFormatters();
    }

    /**
     * Sounds, particles, status effects, entities, data components and items of the mod's own roles, and the items
     * NoellesRoles hides in other players' hands. Items come after the data components they carry and before the
     * hidden-equipment registrations that name them.
     * 本模组自有职业的声音、粒子、状态效果、实体、数据组件与物品，以及交给 NoellesRoles 在他人手中隐藏的物品。
     * 物品位于其携带的数据组件之后、点名它们的隐藏装备注册之前。
     */
    private static void registerOwnRoleContent() {
        SparkWitchSounds.register();
        SaviorParticles.register();
        SeekerSounds.register();
        HunterEffects.register();
        OrthopedistEffects.register();
        GuardianAngelEffects.register();
        FocusedFootstepsEffects.register();
        HunterEntities.register();
        SpecterEntities.register();
        SpecterParticles.register();
        ControlExpertEntities.register();
        AbyssListenerEntities.register();
        RiftGateEntities.register();
        SeekerEntities.register();
        HolyFlashEntities.register();
        PotionGunnerEntities.register();
        MiniWitherEntities.register();
        BlackRavenCrowEntities.register();
        BlackRavenFeatherRegistry.register();
        // BEAR smoke round entity type. / BEAR 烟雾榴弹实体类型。
        dev.caecorthus.sparkdlc.role.civilian.bear.launcher.BearEntities.register();
        SparkWitchDataComponents.register();
        SparkWitchItems.register();
        // BEAR items (launcher, smoke round, Rys-T, earplugs), after the data components they carry and before the tab.
        // BEAR 物品（发射器、烟雾榴弹、Rys-T、耳塞），位于其携带的数据组件之后、物品栏之前。
        dev.caecorthus.sparkdlc.role.civilian.bear.BearItems.register();
        SparkWitchItemGroups.register();
        SparkWitchEntities.register();
        // NoellesRoles remains the packet-filter owner; FactionAPI only extends its hidden-item predicate.
        // NoellesRoles 仍负责装备包过滤，FactionAPI 这里只扩展其隐藏物品判定。
        NoellesHiddenEquipment.register(SparkWitchItems.perfumeEssence());
        NoellesHiddenEquipment.register(SparkWitchItems.cologne());
        NoellesHiddenEquipment.register(SparkWitchItems.blackRavenLedger());
        NoellesHiddenEquipment.register(SparkWitchItems.timeStealerClock());
        NoellesHiddenEquipment.register(SparkWitchItems.timeStealerGiftWatch());
        NoellesHiddenEquipment.register(SparkWitchItems.timeStamp());
        NoellesHiddenEquipment.register(SparkWitchItems.blackRavenMask());
        NoellesHiddenEquipment.register(SparkWitchItems.prophetNecrology());
        NoellesHiddenEquipment.register(SparkWitchItems.tollBell());
        NoellesHiddenEquipment.register(SparkWitchItems.seekerCar());
        NoellesHiddenEquipment.register(SparkWitchItems.seekerCamera());
        // Angler: the rod, bait and every non-weapon fish; the Key Fish and Swordfish stay visible on purpose.
        // 钓鱼佬：鱼竿、鱼饵与所有非武器类的鱼；钥匙鱼与剑鱼刻意保持可见。
        NoellesHiddenEquipment.register(SparkWitchItems.fishingRod());
        NoellesHiddenEquipment.register(SparkWitchItems.fishBait());
        NoellesHiddenEquipment.register(SparkWitchItems.salmon());
        NoellesHiddenEquipment.register(SparkWitchItems.cod());
        NoellesHiddenEquipment.register(SparkWitchItems.clownfish());
        NoellesHiddenEquipment.register(SparkWitchItems.goldfish());
        NoellesHiddenEquipment.register(SparkWitchItems.glimmerfish());
        // Blind: the cane is hidden in round (D9); a held ComTac would expose the role the same way.
        // 盲人：盲杖局内隐藏（D9）；手持的 ComTac 同样会暴露身份。
        NoellesHiddenEquipment.register(SparkWitchItems.whiteCane());
        NoellesHiddenEquipment.register(SparkWitchItems.comTac());
        // Abyss Listener: the bound Shriek Gun is hidden in hand (owner default N11); the flask stays visible.
        // 聆渊者：绑定的啸音铳手持时对他人隐藏（所有者默认 N11）；孢瓶保持可见。
        NoellesHiddenEquipment.register(SparkWitchItems.shriekGun());
        // Riftwalker: the Rift Gate stack is hidden in hand, like the Seeker devices.
        // 隙行者：裂隙门手持时对他人隐藏，与搜寻者设备相同。
        NoellesHiddenEquipment.register(SparkWitchItems.riftGate());
    }

    /**
     * The Witch, Murderous Witch and Jiahao factions with every role of the mod, then the built-in skills.
     * 魔女、杀意魔女与嘉豪阵营及本模组的全部职业，然后是内置技能。
     */
    private static void registerOwnRolesAndSkills() {
        SparkWitchRoles.register();
        SparkWitchBuiltInSkills.register();
    }

    /**
     * Packets, hitbox history, the version handshake, the event listeners of the mod's own roles, forced cooldowns,
     * the Wraith text-chat gates and the Assassin guess order refreshed once the server has started.
     * 本模组自有职业的数据包、碰撞箱历史、版本握手、事件监听器、强制冷却、冤魂文字聊天限制，以及服务器启动后刷新的刺客猜测顺序。
     */
    private static void registerOwnRoleGameplay() {
        SparkWitchPackets.register();
        PlayerHitboxHistory.register();
        CurserFeatureService.register();
        SparkWitchVersionHandshake.registerServer();
        SparkWitchEvents.register();
        dev.caecorthus.sparkdlc.cooldown.provider.SparkWitchForcedCooldowns.register();
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(
                (message, sender, params) -> mayUseTextChat(sender)
        );
        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, params) -> {
            ServerPlayerEntity sender = source.getPlayer();
            return sender == null || mayUseTextChat(sender);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> SparkWitchRoles.refreshAssassinGuessRoleOrder());
    }

    private static boolean mayUseTextChat(ServerPlayerEntity sender) {
        if (!WraithStateService.isActive(sender)) {
            return true;
        }
        var role = GameWorldComponent.KEY.get(sender.getWorld()).getRole(sender);
        return WraithParticipationRules.mayUseTextChat(
                true,
                GuardianAngelRules.isGuardianAngel(role),
                sender.isCreative()
        );
    }
}
