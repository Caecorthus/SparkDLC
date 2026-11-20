package dev.caecorthus.sparkdlc.gametest;

import dev.caecorthus.sparkdlc.component.AntiFastRoundComponent;
import dev.caecorthus.sparkdlc.component.FactionRoundEndComponent;
import dev.caecorthus.sparkdlc.component.ThirdFactionComponent;
import dev.caecorthus.sparkdlc.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.component.WitchWorldComponent;
import dev.caecorthus.sparkdlc.component.WraithPlayerComponent;
import dev.caecorthus.sparkdlc.component.WraithRoundComponent;
import dev.caecorthus.sparkdlc.faction.thirdfaction.ThirdFactionDrawSettings;
import dev.caecorthus.sparkdlc.legacy.LegacyIds;
import dev.caecorthus.sparkdlc.legacy.LegacySavedData;
import dev.caecorthus.sparkdlc.role.civilian.detective.DetectiveCaseWorldComponent;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterPlayerComponent;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithSettings;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithState;
import dev.caecorthus.sparkdlc.trait.component.TraitPlayerComponent;
import dev.caecorthus.sparkdlc.trait.component.TraitWorldComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.RoleHistoryComponent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

/**
 * Saved data written by the five separate mods loads into the running server: each test takes what the live
 * container saves right now, turns it back into the OLD save format (old component keys, old ids inside the values),
 * lets the REAL container / entity read it (so the hooks of {@code mixin.legacy} do the work, nothing is called by
 * hand), reads the result through the public accessors of the components, and saves again to see that only current
 * names are written. The old names are literals on purpose: they are what old saves hold.
 * 五个独立模组写下的存档数据能够载入运行中的服务器：每个测试取现行容器此刻保存的内容，把它还原成“旧”存档格式（旧组件键、
 * 值内部的旧 id），交给“真实”的容器 / 实体读取（因此起作用的是 {@code mixin.legacy} 的钩子，没有任何手工调用），通过组件的
 * 公开访问器读取结果，再保存一次以确认只写出当前名字。这里有意把旧名字写成字面量：它们就是旧存档里的内容。
 */
public final class LegacySavedDataGameTest implements FabricGameTest {
    private static final String COMPONENTS = LegacySavedData.COMPONENTS_KEY;

    /**
     * World settings: disabled traits (with a retired one), disabled skills, forced-role locks, Wraith settings and
     * round quota (saved under the oldest {@code sparktraits:} key), tablet and detective limits, and Wathe's
     * disabled roles.
     * 世界设置：禁用的天赋（含一个已退役的）、禁用的技能、强制职业锁定、冤魂设置与本局名额（保存在最早的
     * {@code sparktraits:} 键下）、平板与侦探上限，以及 Wathe 的禁用职业。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void worldSettingsSavedUnderOldNamesAreLoadedAndSavedUnderCurrentNames(TestContext context) {
        ServerWorld world = context.getWorld();
        ComponentContainer container = ((ComponentProvider) world).getComponentContainer();
        RegistryWrapper.WrapperLookup registries = world.getRegistryManager();
        NbtCompound before = container.toTag(new NbtCompound(), registries).getCompound(COMPONENTS).copy();
        UUID wraithLock = UUID.randomUUID();
        UUID accompliceLock = UUID.randomUUID();
        UUID pendingLock = UUID.randomUUID();
        UUID consumed = UUID.randomUUID();

        NbtCompound traitWorld = saved(before, "sparkdlc:trait_world");
        traitWorld.put("DisabledTraits", strings("sparktraits:anxiety", "sparktraits:wraith", "sparktraits:cautious"));
        traitWorld.putFloat("TraitSlotRollChance", 0.25F);
        NbtCompound witchWorld = saved(before, "sparkdlc:witch_world");
        witchWorld.put("DisabledSkills", strings("sparkwitch:bell_echo", "sparkwitch:clairvoyance"));
        witchWorld.put("ForcedWraithPromotions", compounds(lock(wraithLock, "sparktraits:wind_spirit")));
        witchWorld.put("ForcedAccompliceRoles", compounds(lock(accompliceLock, "sparkwitch:prophet")));
        witchWorld.put("PendingForcedAccompliceRoles", compounds(lock(pendingLock, "sparkwitch:hunter")));
        witchWorld.putInt("WraithChance", 40);
        witchWorld.putInt("WraithMinimum", 12);
        witchWorld.putInt("WraithDividend", 4);
        NbtCompound tabletWorld = saved(before, "sparkdlc:tablet_world");
        tabletWorld.putInt("EmergencyMeetingChances", 7);
        tabletWorld.putInt("MeetingDurationTicks", 1234);
        tabletWorld.putInt("ChatIntervalTicks", 55);
        NbtCompound detectiveWorld = saved(before, "sparkdlc:detective_case_world");
        detectiveWorld.putInt("DetectiveSuspectLimit", 2);
        NbtCompound wraithRound = new NbtCompound();
        wraithRound.putInt("StartingPlayerCount", 15);
        wraithRound.put("ConsumedPlayers", strings(consumed.toString()));
        NbtCompound game = saved(before, "wathe:game");
        game.put("DisabledRoles", strings("sparkwitch:prophet", "sparktraits:curser", "sparkwitch:curser",
                "noellesroles:not_a_registered_role"));
        NbtCompound old = new NbtCompound();
        old.put("sparktraits:world", traitWorld);
        old.put("sparkwitch:world", witchWorld);
        old.put("sparkstrength:tablet_world", tabletWorld);
        old.put("sparkstrength:detective_case_world", detectiveWorld);
        old.put("sparktraits:wraith_round", wraithRound);
        old.put("wathe:game", game);
        List<String> touched = List.of("sparkdlc:trait_world", "sparkdlc:witch_world", "sparkdlc:tablet_world",
                "sparkdlc:detective_case_world", "sparkdlc:wraith_round", "wathe:game");

        try {
            container.fromTag(root(old), registries);

            TraitWorldComponent traits = TraitWorldComponent.KEY.get(world);
            check(!traits.isTraitEnabled(id("anxiety")) && !traits.isTraitEnabled(id("cautious")),
                    "the disabled traits are enabled again");
            check(traits.isTraitEnabled(id("charisma")), "a trait that was not disabled is disabled");
            // The retired trait must not come back as the live role id. / 退役天赋不得以现行职业 id 的身份回来。
            check(traits.isTraitEnabled(id("wraith")), "sparktraits:wraith was resurrected as a disabled trait");
            check(traits.getTraitSlotRollChance() == 0.25F, "trait slot roll chance: " + traits.getTraitSlotRollChance());
            WitchWorldComponent witch = WitchWorldComponent.KEY.get(world);
            check(!witch.isSkillEnabled(id("bell_echo")) && !witch.isSkillEnabled(id("clairvoyance")),
                    "the disabled skills are enabled again");
            check(witch.isSkillEnabled(id("ceremonial_sword")), "a skill that was not disabled is disabled");
            check(id("wind_spirit").equals(witch.getForcedWraithPromotion(wraithLock)),
                    "forced wraith promotion: " + witch.getForcedWraithPromotion(wraithLock));
            check(Map.of(accompliceLock, id("prophet")).equals(witch.getForcedAccompliceRoles()),
                    "forced accomplice roles: " + witch.getForcedAccompliceRoles());
            check(Map.of(pendingLock, id("hunter")).equals(witch.getPendingForcedAccompliceRoles()),
                    "pending forced accomplice roles: " + witch.getPendingForcedAccompliceRoles());
            check(new WraithSettings(40, 12, 4).equals(witch.getWraithSettings()),
                    "wraith settings: " + witch.getWraithSettings());
            check(DetectiveCaseWorldComponent.KEY.get(world).getSuspectLimit() == 2, "detective suspect limit");
            WraithRoundComponent round = WraithRoundComponent.KEY.get(world);
            check(round.getStartingPlayerCount() == 15 && Set.of(consumed).equals(round.getConsumedPlayers()),
                    "wraith round quota: " + round.getStartingPlayerCount() + " " + round.getConsumedPlayers());
            GameWorldComponent wathe = GameWorldComponent.KEY.get(world);
            check(!wathe.isRoleEnabled(role("prophet")) && !wathe.isRoleEnabled(role("curser")),
                    "the roles Wathe had disabled are enabled again: " + wathe.getDisabledRoles());
            check(wathe.isRoleEnabled(role("hunter")), "a role that was not disabled is disabled");
            check(Set.of(id("prophet"), id("curser"), Identifier.of("noellesroles", "not_a_registered_role"))
                    .equals(wathe.getDisabledRoles()), "Wathe's disabled roles: " + wathe.getDisabledRoles());

            NbtCompound after = container.toTag(new NbtCompound(), registries).getCompound(COMPONENTS);
            checkNoOldKey(after);
            check(strings("sparkdlc:anxiety", "sparkdlc:cautious").equals(
                            after.getCompound("sparkdlc:trait_world").getList("DisabledTraits", NbtElement.STRING_TYPE)),
                    "saved disabled traits: " + after.getCompound("sparkdlc:trait_world"));
            NbtCompound savedWitch = after.getCompound("sparkdlc:witch_world");
            check(Set.of("sparkdlc:bell_echo", "sparkdlc:clairvoyance").equals(stringSet(savedWitch, "DisabledSkills")),
                    "saved disabled skills: " + savedWitch);
            check(compounds(lock(wraithLock, "sparkdlc:wind_spirit")).equals(
                    savedWitch.getList("ForcedWraithPromotions", NbtElement.COMPOUND_TYPE)), "saved wraith locks");
            NbtCompound savedTablet = after.getCompound("sparkdlc:tablet_world");
            check(savedTablet.getInt("EmergencyMeetingChances") == 7 && savedTablet.getInt("MeetingDurationTicks") == 1234
                    && savedTablet.getInt("ChatIntervalTicks") == 55, "saved tablet settings: " + savedTablet);
            check(after.getCompound("sparkdlc:wraith_round").getInt("StartingPlayerCount") == 15,
                    "saved wraith round: " + after.getCompound("sparkdlc:wraith_round"));
            check(Set.of("sparkdlc:prophet", "sparkdlc:curser", "noellesroles:not_a_registered_role")
                            .equals(stringSet(after.getCompound("wathe:game"), "DisabledRoles")),
                    "saved Wathe disabled roles: " + after.getCompound("wathe:game").get("DisabledRoles"));
        } finally {
            container.fromTag(root(restore(before, touched)), registries);
        }
        context.complete();
    }

    /**
     * Scoreboard scope: anti-fast-round and third-faction settings, last round's winning faction, and Wathe's role
     * history and round-end rows.
     * 计分板作用域：防速通与第三阵营设置、上一局的获胜阵营，以及 Wathe 的职业历史与结算行。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void scoreboardDataSavedUnderOldNamesIsLoadedAndSavedUnderCurrentNames(TestContext context) {
        ServerWorld world = context.getWorld();
        Scoreboard scoreboard = world.getServer().getScoreboard();
        ComponentContainer container = ((ComponentProvider) scoreboard).getComponentContainer();
        RegistryWrapper.WrapperLookup registries = world.getRegistryManager();
        NbtCompound before = container.toTag(new NbtCompound(), registries).getCompound(COMPONENTS).copy();
        UUID player = UUID.randomUUID();

        NbtCompound antiFastRound = new NbtCompound();
        antiFastRound.putBoolean("Enabled", true);
        antiFastRound.putInt("Seconds", 77);
        NbtCompound thirdFaction = new NbtCompound();
        thirdFaction.putInt("Dividend", 9);
        thirdFaction.putInt("MinPlayers", 11);
        NbtCompound roundEnd = new NbtCompound();
        roundEnd.putString("WinningFaction", "sparkwitch:murderous_witch");
        NbtCompound historyPlayer = new NbtCompound();
        historyPlayer.putUuid("Player", player);
        historyPlayer.put("Entries", compounds(historyEntry("sparkwitch:prophet"), historyEntry("sparktraits:vendetta"),
                historyEntry("noellesroles:bartender")));
        NbtCompound history = new NbtCompound();
        history.put("History", compounds(historyPlayer));
        NbtCompound row = new NbtCompound();
        row.putUuid("uuid", player);
        row.putString("name", "sparkwitch:prophet");
        row.putString("role", "sparkwitch:prophet");
        row.putString("endStatus", "ALIVE");
        row.putBoolean("isWinner", true);
        NbtCompound watheRoundEnd = saved(before, "wathe:roundend");
        watheRoundEnd.put("players", compounds(row));
        if (!watheRoundEnd.contains("winstatus")) {
            watheRoundEnd.putInt("winstatus", 0);
        }
        NbtCompound old = new NbtCompound();
        old.put("sparkfactionapi:anti_fast_round", antiFastRound);
        old.put("sparkfactionapi:third_faction_draw", thirdFaction);
        old.put("sparkfactionapi:round_end", roundEnd);
        old.put("wathe:role_history", history);
        old.put("wathe:roundend", watheRoundEnd);
        List<String> touched = List.of("sparkdlc:anti_fast_round", "sparkdlc:third_faction_draw",
                "sparkdlc:round_end", "wathe:role_history", "wathe:roundend");

        try {
            container.fromTag(root(old), registries);

            AntiFastRoundComponent anti = AntiFastRoundComponent.KEY.get(scoreboard);
            check(anti.isEnabled() && anti.getSeconds() == 77,
                    "anti fast round: " + anti.isEnabled() + " " + anti.getSeconds());
            ThirdFactionDrawSettings third = ThirdFactionComponent.KEY.get(scoreboard).settings();
            check(third.dividend() == 9 && third.minPlayers() == 11, "third faction settings: " + third);
            check(id("murderous_witch").equals(FactionRoundEndComponent.KEY.get(scoreboard).getWinningFaction()),
                    "winning faction: " + FactionRoundEndComponent.KEY.get(scoreboard).getWinningFaction());
            check(Set.of("sparkdlc:prophet", "sparkdlc:vendetta", "noellesroles:bartender")
                            .equals(RoleHistoryComponent.KEY.get(scoreboard).recentRoleIds(player)),
                    "role history: " + RoleHistoryComponent.KEY.get(scoreboard).recentRoleIds(player));
            List<GameRoundEndComponent.RoundEndData> rows = GameRoundEndComponent.KEY.get(scoreboard).getPlayers();
            check(rows.size() == 1 && id("prophet").equals(rows.get(0).role())
                    && "sparkwitch:prophet".equals(rows.get(0).player().getName()), "round end rows: " + rows);

            NbtCompound after = container.toTag(new NbtCompound(), registries).getCompound(COMPONENTS);
            checkNoOldKey(after);
            NbtCompound savedAnti = after.getCompound("sparkdlc:anti_fast_round");
            check(savedAnti.getBoolean("Enabled") && savedAnti.getInt("Seconds") == 77, "saved: " + savedAnti);
            check("sparkdlc:murderous_witch".equals(after.getCompound("sparkdlc:round_end").getString("WinningFaction")),
                    "saved winning faction: " + after.getCompound("sparkdlc:round_end"));
            check("sparkdlc:prophet".equals(after.getCompound("wathe:roundend")
                    .getList("players", NbtElement.COMPOUND_TYPE).getCompound(0).getString("role")), "saved round end");
            NbtList savedEntries = after.getCompound("wathe:role_history").getList("History", NbtElement.COMPOUND_TYPE)
                    .getCompound(0).getList("Entries", NbtElement.COMPOUND_TYPE);
            check("sparkdlc:prophet".equals(savedEntries.getCompound(0).getString("RoleId"))
                            && "sparkdlc:vendetta".equals(savedEntries.getCompound(1).getString("RoleId")),
                    "saved role history: " + savedEntries);
        } finally {
            container.fromTag(root(restore(before, touched)), registries);
        }
        context.complete();
    }

    /**
     * A player file: every component key the player saves is put back under its old key, with pending traits, a
     * forced skill, Wraith state under the oldest key, and the Hunter's persistent fracture modifier under its old
     * id. Read by {@code Entity#readNbt}, as a player file is.
     * 玩家文件：玩家保存的每个组件键都放回旧键下，并带有待发放天赋、强制技能、最早键下的冤魂状态，以及旧 id 下的猎人持久骨折
     * 修饰符。与读取玩家文件一样经由 {@code Entity#readNbt} 读取。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void playerDataSavedUnderOldNamesIsLoadedAndSavedUnderCurrentNames(TestContext context) {
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        NbtCompound nbt = player.writeNbt(new NbtCompound());
        Map<String, String> oldKeys = new LinkedHashMap<>();
        LegacyIds.renamed("component").forEach((oldKey, currentKey) -> oldKeys.put(currentKey, oldKey));
        NbtCompound current = nbt.getCompound(COMPONENTS);
        NbtCompound old = new NbtCompound();
        for (String key : current.getKeys()) {
            old.put(oldKeys.getOrDefault(key, key), current.get(key).copy());
        }
        NbtCompound traits = new NbtCompound();
        traits.put("PendingTraits", strings("sparktraits:bloodthirsty", "sparktraits:wraith"));
        traits.putString("SerialKillerMurdererRole", "sparktraits:wraith");
        old.put("sparktraits:traits", traits);
        NbtCompound witch = old.getCompound("sparkwitch:player");
        witch.putString("ForcedSkill", "sparkwitch:bell_echo");
        old.put("sparkwitch:player", witch);
        NbtCompound wraith = new NbtCompound();
        wraith.putBoolean("WraithActive", true);
        wraith.putBoolean("WraithRestricted", true);
        wraith.putInt("WraithCompletedTasks", 2);
        wraith.putString("WraithAlignment", "GOOD");
        old.remove("sparkwitch:wraith_player");
        old.put("sparktraits:wraith_player", wraith);
        nbt.put(COMPONENTS, old);
        for (String key : old.getKeys()) {
            check(!key.startsWith("sparkdlc:"), "the old player file still has a current key: " + key);
        }
        NbtCompound fracture = new NbtCompound();
        fracture.putString("id", "sparkwitch:fracture_speed");
        fracture.putDouble("amount", -0.3);
        fracture.putString("operation", "add_multiplied_total");
        NbtCompound speedNbt = new NbtCompound();
        speedNbt.putString("id", "minecraft:generic.movement_speed");
        speedNbt.putDouble("base", 0.1);
        speedNbt.put("modifiers", compounds(fracture));
        NbtList attributes = new NbtList();
        for (NbtElement attribute : nbt.getList("attributes", NbtElement.COMPOUND_TYPE)) {
            if (!"minecraft:generic.movement_speed".equals(((NbtCompound) attribute).getString("id"))) {
                attributes.add(attribute.copy());
            }
        }
        attributes.add(speedNbt);
        nbt.put("attributes", attributes);

        player.readNbt(nbt);

        TraitPlayerComponent playerTraits = TraitPlayerComponent.KEY.get(player);
        check(List.of(id("bloodthirsty")).equals(playerTraits.getPendingTraitIds()),
                "pending traits: " + playerTraits.getPendingTraitIds());
        check(id("wraith").equals(playerTraits.getSerialKillerMurdererRole()),
                "serial killer murderer role: " + playerTraits.getSerialKillerMurdererRole());
        check(id("bell_echo").equals(WitchPlayerComponent.KEY.get(player).getForcedSkillId()),
                "forced skill: " + WitchPlayerComponent.KEY.get(player).getForcedSkillId());
        WraithPlayerComponent wraithState = WraithPlayerComponent.KEY.get(player);
        check(wraithState.isActive() && wraithState.isRestricted() && wraithState.getCompletedTasks() == 2
                && wraithState.getAlignment() == WraithState.Alignment.GOOD, "wraith state did not arrive");
        EntityAttributeInstance speed = Objects.requireNonNull(
                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED));
        check(speed.hasModifier(id("fracture_speed")), "the fracture modifier was lost");
        check(!speed.hasModifier(Identifier.of("sparkwitch", "fracture_speed")),
                "the fracture modifier kept its old id: the mod can never remove it");

        NbtCompound after = player.writeNbt(new NbtCompound());
        checkNoOldKey(after.getCompound(COMPONENTS));
        check(strings("sparkdlc:bloodthirsty").equals(after.getCompound(COMPONENTS).getCompound("sparkdlc:traits")
                .getList("PendingTraits", NbtElement.STRING_TYPE)), "saved pending traits");
        check("sparkdlc:bell_echo".equals(after.getCompound(COMPONENTS).getCompound("sparkdlc:player")
                .getString("ForcedSkill")), "saved forced skill");
        check(after.getCompound(COMPONENTS).getCompound("sparkdlc:wraith_player").getInt("WraithCompletedTasks") == 2,
                "saved wraith state");
        check(after.get("attributes").toString().contains("sparkdlc:fracture_speed")
                && !after.get("attributes").toString().contains("sparkwitch:"), "saved attributes: " + after.get("attributes"));

        // What the Hunter does when the injury is over: it removes the modifier by its current id.
        // 伤势结束时猎人的处理：按当前 id 移除修饰符。
        HunterPlayerComponent.KEY.get(player).reset();
        check(!speed.hasModifier(id("fracture_speed")), "the fracture modifier survived the Hunter's own cleanup");
        check(!player.writeNbt(new NbtCompound()).get("attributes").toString().contains("fracture_speed"),
                "the fracture modifier is still saved after the cleanup");
        context.complete();
    }

    /**
     * A value rule that names a component nobody registers would silently migrate nothing; an old key whose current
     * key is not registered would move data to where CCA drops it.
     * 值规则若指向无人注册的组件，就会悄悄地什么都不迁移；旧键对应的当前键若未注册，数据会被移到 CCA 丢弃它的地方。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void everyMigrationTargetIsARegisteredComponent(TestContext context) {
        Set<String> missing = new TreeSet<>();
        for (String component : LegacySavedData.componentsWithValueRules()) {
            if (ComponentRegistry.get(Identifier.of(component)) == null) {
                missing.add("value rule " + component);
            }
        }
        for (String kind : List.of("component", "component_fallback")) {
            LegacyIds.renamed(kind).forEach((oldKey, currentKey) -> {
                if (ComponentRegistry.get(Identifier.of(currentKey)) == null) {
                    missing.add(kind + " " + oldKey + " -> " + currentKey);
                }
                if (ComponentRegistry.get(Identifier.of(oldKey)) != null) {
                    missing.add(kind + " " + oldKey + " is still a registered component");
                }
            });
        }
        check(LegacyIds.renamed("component").size() + LegacyIds.renamed("component_fallback").size() == 124,
                "expected the 122 + 2 old component keys of the table");
        check(missing.isEmpty(), missing.toString());
        context.complete();
    }

    private static NbtCompound saved(NbtCompound components, String key) {
        return components.getCompound(key).copy();
    }

    /** The saved state of the touched components as it was; a component that had saved nothing reads an empty tag. */
    private static NbtCompound restore(NbtCompound before, List<String> touched) {
        NbtCompound restore = new NbtCompound();
        for (String key : touched) {
            restore.put(key, saved(before, key));
        }
        return restore;
    }

    private static NbtCompound root(NbtCompound components) {
        NbtCompound root = new NbtCompound();
        root.put(COMPONENTS, components);
        return root;
    }

    private static void checkNoOldKey(NbtCompound components) {
        for (String key : components.getKeys()) {
            check(!LegacyIds.isLegacyNamespace(key.substring(0, Math.max(0, key.indexOf(':')))),
                    "a component is still saved under an old key: " + key);
        }
    }

    private static Identifier id(String path) {
        return Identifier.of(LegacyIds.NAMESPACE, path);
    }

    private static Role role(String path) {
        Role role = WatheRoles.getRole(id(path));
        check(role != null, "no role " + id(path));
        return role;
    }

    private static NbtCompound lock(UUID player, String role) {
        NbtCompound entry = new NbtCompound();
        entry.putString("Player", player.toString());
        entry.putString("Role", role);
        return entry;
    }

    private static NbtCompound historyEntry(String roleId) {
        NbtCompound entry = new NbtCompound();
        entry.putDouble("Ks", 0.0);
        entry.putDouble("Vs", 0.0);
        entry.putDouble("Ns", 0.0);
        entry.putString("Category", "CIVILIAN");
        entry.putString("RoleId", roleId);
        return entry;
    }

    private static Set<String> stringSet(NbtCompound tag, String key) {
        Set<String> values = new TreeSet<>();
        for (NbtElement element : tag.getList(key, NbtElement.STRING_TYPE)) {
            values.add(element.asString());
        }
        return values;
    }

    private static NbtList strings(String... values) {
        NbtList list = new NbtList();
        for (String value : values) {
            list.add(NbtString.of(value));
        }
        return list;
    }

    private static NbtList compounds(NbtCompound... values) {
        NbtList list = new NbtList();
        for (NbtCompound value : values) {
            list.add(value);
        }
        return list;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new GameTestException(message);
        }
    }
}
