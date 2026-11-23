package dev.caecorthus.sparkdlc.gametest;

import dev.caecorthus.sparkdlc.item.CapsuleItem;
import dev.caecorthus.sparkdlc.legacy.LegacyDataNames;
import dev.caecorthus.sparkdlc.legacy.LegacyIds;
import dev.caecorthus.sparkdlc.role.civilian.accountant.AccountantPassbookService;
import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerService;
import dev.caecorthus.sparkdlc.role.civilian.engineer.EngineerCaptureReport;
import dev.caecorthus.sparkdlc.role.civilian.fisher.FisherPufferfishService;
import dev.caecorthus.sparkdlc.role.civilian.junker.JunkerLoot;
import dev.caecorthus.sparkdlc.role.civilian.knight.duel.KnightDuelService;
import dev.caecorthus.sparkdlc.role.civilian.undercover.UndercoverService;
import dev.caecorthus.sparkdlc.role.killer.bomber.TimedBombTrapHolder;
import dev.caecorthus.sparkdlc.role.killer.morphling.MorphlingService;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.PoisonAppleDrinkMarker;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.PoisonApplePlateAccess;
import dev.caecorthus.sparkdlc.role.special.wraith.conversion.WraithBodyRoleAccess;
import dev.caecorthus.sparkdlc.trait.killer.conscience.ConsciencePoisonedPlate;
import dev.caecorthus.sparkdlc.trait.killer.conscience.ConscienceScorpionBed;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.index.WatheBlocks;
import dev.doctor4t.wathe.index.WatheEntities;
import dev.doctor4t.wathe.index.WatheItems;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.agmas.noellesroles.NoellesRolesEntities;
import org.agmas.noellesroles.entity.PoisonGasCloudEntity;
import org.agmas.noellesroles.voodoo.VoodooPlayerComponent;

/**
 * The private data names renamed on 2026-11-20 (owner: "就数据可以改", the {@code private_data_name} rows of
 * {@code legacy-ids.tsv}) on a running server: data planted under the OLD name (a stack decoded from a save or a
 * packet, a block entity or entity read from NBT, a component read from a player file, a scoreboard team and an entity
 * tag left in a world) is recognised by the real readers, and what is saved afterwards carries only the current name.
 * The old names are literals on purpose: they are what old saves hold.
 * 2026-11-20 改名的私有数据名（所有者：“就数据可以改”，{@code legacy-ids.tsv} 的 {@code private_data_name} 行）在运行中的服务器上：
 * 以“旧”名字植入的数据（从存档或数据包解码的物品堆、从 NBT 读取的方块实体或实体、从玩家文件读取的组件、世界里遗留的计分板队伍与
 * 实体标签）能被真实的读取方识别，之后保存的内容只带当前名字。这里有意把旧名字写成字面量：它们就是旧存档里的内容。
 */
public final class LegacyDataNamesGameTest implements FabricGameTest {
    /**
     * Every row of the table, read back from an item's custom data three ways (the save codec, the network codec,
     * {@code NbtComponent.of}): the old key is gone, the current key holds its value, other keys are untouched, and a
     * current key that is already present wins.
     * 表中每一行，经三种途径（存档编解码器、网络编解码器、{@code NbtComponent.of}）从物品自定义数据读回：旧键消失，当前键持有其值，
     * 其他键不变；当前键已存在时以它为准。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void everyOldTopLevelCustomDataKeyCarriesItsCurrentNameOnceLoaded(TestContext context) {
        RegistryWrapper.WrapperLookup registries = context.getWorld().getRegistryManager();
        Map<String, String> rows = LegacyIds.renamed(LegacyDataNames.KIND);
        check(rows.size() == 22, "expected the 22 private_data_name rows: " + rows.keySet());
        List<String> problems = new ArrayList<>();
        rows.forEach((old, current) -> {
            NbtCompound oldData = new NbtCompound();
            oldData.putInt(old, 7);
            oldData.putString("Unrelated", "kept");

            NbtCompound fromSave = customData(decodeStack(registries, Items.PAPER, oldData));
            NbtCompound fromPacket = viaPacket(oldData);
            NbtCompound fromOf = NbtComponent.of(oldData).copyNbt();
            for (NbtCompound loaded : List.of(fromSave, fromPacket, fromOf)) {
                if (loaded.contains(old) || loaded.getInt(current) != 7 || !"kept".equals(loaded.getString("Unrelated"))) {
                    problems.add(old + " -> " + current + ": " + loaded);
                }
            }
            if (!oldData.contains(old) || oldData.contains(current)) {
                problems.add(old + ": the caller's compound was changed: " + oldData);
            }
            String saved = decodeStack(registries, Items.PAPER, oldData).encode(registries).toString();
            if (saved.contains(old)) {
                problems.add(old + ": saved again under the old name: " + saved);
            }

            NbtCompound both = new NbtCompound();
            both.putInt(old, 1);
            both.putInt(current, 2);
            NbtCompound kept = NbtComponent.of(both).copyNbt();
            if (kept.contains(old) || kept.getInt(current) != 2) {
                problems.add(old + ": the current key did not win: " + kept);
            }
        });
        check(problems.isEmpty(), String.join("; ", problems));
        context.complete();
    }

    /**
     * The readers of every item marker answer for a stack saved under the old name, and the public markers write the
     * current one.
     * 每个物品标记的读取方都能识别以旧名字保存的物品堆，公开的标记方法写入当前名字。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void itemMarkersSavedUnderOldNamesAreRecognised(TestContext context) throws ReflectiveOperationException {
        RegistryWrapper.WrapperLookup registries = context.getWorld().getRegistryManager();
        UUID owner = UUID.randomUUID();

        NbtCompound coins = new NbtCompound();
        coins.putInt("sparkwitch:stored_coins", 12);
        check(AccountantPassbookService.storedCoins(decodeStack(registries, Items.PAPER, coins)) == 12,
                "Accountant passbook coins");
        check(JunkerLoot.isLoot(decodeStack(registries, Items.PAPER, flag("sparkwitch:ragpicker_loot"))), "Junker loot");
        check(UndercoverService.isBound(decodeStack(registries, Items.LEATHER_HELMET,
                flag("sparkstrength_undercover_outfit"))), "Undercover outfit");
        check(PoisonAppleDrinkMarker.isMarked(decodeStack(registries, Items.PAPER, flag("sparkwitch:poison_apple_drink"))),
                "Poison Apple drink");
        Method crowbarMarked = Class.forName("dev.caecorthus.sparkdlc.role.neutral.fiend.FiendMomentCrowbar")
                .getDeclaredMethod("isMarked", ItemStack.class);
        crowbarMarked.setAccessible(true);
        check((boolean) crowbarMarked.invoke(null, decodeStack(registries, WatheItems.CROWBAR,
                flag("sparkwitch:fiend_moment_crowbar"))), "Fiend Moment crowbar");

        NbtCompound capsule = new NbtCompound();
        NbtCompound contents = new NbtCompound();
        contents.put("ContainedItem", new ItemStack(Items.APPLE).encode(registries));
        contents.putString("ContainedName", "Apple");
        capsule.put("SparkStrengthCapsule", contents);
        check(CapsuleItem.hasContents(decodeStack(registries, Items.PAPER, capsule)), "Capsule contents");

        ItemStack report = decodeStack(registries, Items.PAPER, rootWithUuid("SparkStrengthEngineerCaptureReport", "Owner", owner));
        check(EngineerCaptureReport.isCaptureReport(report) && owner.equals(EngineerCaptureReport.ownerUuid(report)),
                "Engineer capture report");
        NbtCompound sample = rootWithUuid("SparkStrengthMorphReagent", "SampleUuid", owner);
        sample.getCompound("SparkStrengthMorphReagent").putString("SampleName", "someone");
        check(owner.equals(MorphlingService.sampleUuid(decodeStack(registries, Items.PAPER, sample)).orElse(null)),
                "Morphling reagent sample");
        check(CoronerService.isTemporaryGrant(decodeStack(registries, Items.PAPER,
                rootWithUuid("SparkStrengthCoronerTemporaryGrant", "Owner", owner))), "Coroner loaned gear");

        // Written under the current name. / 以当前名字写入。
        ItemStack marked = new ItemStack(Items.PAPER);
        JunkerLoot.mark(marked);
        PoisonAppleDrinkMarker.mark(marked);
        EngineerCaptureReport.mark(marked, owner);
        NbtCompound written = customData(marked);
        check(written.getBoolean("sparkdlc:ragpicker_loot") && written.getBoolean("sparkdlc:poison_apple_drink")
                && written.contains("SparkDlcEngineerCaptureReport") && written.getKeys().size() == 3,
                "markers written: " + written);
        context.complete();
    }

    /**
     * A Wathe platter and bed saved with the old keys of the Conscience Poisoner, the Poison Apple and the timed bomb:
     * the state arrives, and the next save and the chunk data carry only the current keys.
     * 以善良毒师、毒苹果与定时炸弹的旧键保存的 Wathe 餐盘与床：状态能够恢复，之后的保存与区块数据只带当前键。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void blockEntitiesSavedUnderOldKeysAreReadAndSavedUnderCurrentKeys(TestContext context) {
        RegistryWrapper.WrapperLookup registries = context.getWorld().getRegistryManager();
        UUID poisoner = UUID.randomUUID();
        UUID bomber = UUID.randomUUID();

        BlockPos platterPos = new BlockPos(1, 1, 1);
        context.setBlockState(platterPos, WatheBlocks.FOOD_PLATTER.getDefaultState());
        BlockEntity platter = Objects.requireNonNull(context.getBlockEntity(platterPos), "no platter block entity");
        NbtCompound clean = platter.createNbt(registries);
        NbtCompound old = withoutCurrentNames(clean);
        old.putString("sparktraitsConsciencePoisoner", poisoner.toString());
        old.putUuid("SparkStrengthTimedBombOwner", bomber);
        old.putBoolean("sparkwitch:poison_apple_armed", true);
        try {
            platter.read(old, registries);
            check(poisoner.toString().equals(((ConsciencePoisonedPlate) platter).sparkdlc$getConsciencePoisoner()),
                    "platter: Conscience Poisoner");
            check(bomber.equals(((TimedBombTrapHolder) platter).sparkdlc$getTimedBombTrapOwner()), "platter: timed bomb");
            check(((PoisonApplePlateAccess) platter).sparkdlc$isPoisonAppleArmed(), "platter: Poison Apple armed bit");
            NbtCompound saved = platter.createNbt(registries);
            check(poisoner.toString().equals(saved.getString("sparkdlcConsciencePoisoner"))
                    && bomber.equals(saved.getUuid("SparkDlcTimedBombOwner")), "platter saved: " + saved);
            checkNoOldKey(saved, "platter saved");
            NbtCompound chunkData = platter.toInitialChunkDataNbt(registries);
            check(chunkData.getBoolean("sparkdlc:poison_apple_armed"), "platter chunk data: " + chunkData);
            checkNoOldKey(chunkData, "platter chunk data");
        } finally {
            platter.read(clean, registries);
        }

        BlockPos bedPos = new BlockPos(3, 1, 1);
        context.setBlockState(bedPos, WatheBlocks.WHITE_TRIMMED_BED.getDefaultState());
        BlockEntity bed = Objects.requireNonNull(context.getBlockEntity(bedPos), "no bed block entity");
        NbtCompound cleanBed = bed.createNbt(registries);
        NbtCompound oldBed = withoutCurrentNames(cleanBed);
        oldBed.putBoolean("sparktraitsConscienceScorpion", true);
        oldBed.putUuid("sparktraitsConscienceScorpionPoisoner", poisoner);
        oldBed.putUuid("SparkStrengthTimedBombOwner", bomber);
        try {
            bed.read(oldBed, registries);
            ConscienceScorpionBed scorpion = (ConscienceScorpionBed) bed;
            check(scorpion.sparkdlc$hasConscienceScorpion() && poisoner.equals(scorpion.sparkdlc$getConscienceScorpionPoisoner()),
                    "bed: blue scorpion");
            check(bomber.equals(((TimedBombTrapHolder) bed).sparkdlc$getTimedBombTrapOwner()), "bed: timed bomb");
            NbtCompound saved = bed.createNbt(registries);
            check(saved.getBoolean("sparkdlcConscienceScorpion")
                    && poisoner.equals(saved.getUuid("sparkdlcConscienceScorpionPoisoner"))
                    && bomber.equals(saved.getUuid("SparkDlcTimedBombOwner")), "bed saved: " + saved);
            checkNoOldKey(saved, "bed saved");
        } finally {
            bed.read(cleanBed, registries);
        }
        context.complete();
    }

    /**
     * A Wathe body with its original role and a Conscience Poisoner gas cloud saved under the old keys, and the Judge's
     * hidden voodoo actor saved under its old key inside the NoellesRoles component of a player file.
     * 以旧键保存原始职业的 Wathe 尸体、善良毒师的毒气云，以及玩家文件里 NoellesRoles 组件中以旧键保存的法官隐藏巫毒施加者。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void entitiesAndComponentsSavedUnderOldKeysAreReadAndSavedUnderCurrentKeys(TestContext context) {
        ServerWorld world = context.getWorld();
        RegistryWrapper.WrapperLookup registries = world.getRegistryManager();

        PlayerBodyEntity body = Objects.requireNonNull(WatheEntities.PLAYER_BODY.create(world), "no body");
        NbtCompound bodyNbt = withoutCurrentNames(body.writeNbt(new NbtCompound()));
        bodyNbt.putString("SparkWitchDeathRole", "sparkdlc:hunter");
        body.readNbt(bodyNbt);
        check(Identifier.of("sparkdlc", "hunter").equals(((WraithBodyRoleAccess) body).sparkdlc$getDeathRole()),
                "body death role: " + ((WraithBodyRoleAccess) body).sparkdlc$getDeathRole());
        NbtCompound bodySaved = body.writeNbt(new NbtCompound());
        check("sparkdlc:hunter".equals(bodySaved.getString("SparkDlcDeathRole")), "body saved: " + bodySaved);
        checkNoOldKey(bodySaved, "body saved");
        body.discard();

        PoisonGasCloudEntity cloud = Objects.requireNonNull(NoellesRolesEntities.POISON_GAS_CLOUD_ENTITY.create(world),
                "no gas cloud");
        NbtCompound cloudNbt = withoutCurrentNames(cloud.writeNbt(new NbtCompound()));
        cloudNbt.putBoolean("sparktraitsConscienceGas", true);
        cloud.readNbt(cloudNbt);
        NbtCompound cloudSaved = cloud.writeNbt(new NbtCompound());
        check(cloudSaved.getBoolean("sparkdlcConscienceGas"), "gas cloud saved: " + cloudSaved);
        checkNoOldKey(cloudSaved, "gas cloud saved");
        cloud.discard();

        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        VoodooPlayerComponent voodoo = VoodooPlayerComponent.KEY.get(player);
        UUID actor = UUID.randomUUID();
        NbtCompound voodooNbt = new NbtCompound();
        voodoo.writeToNbt(voodooNbt, registries);
        voodooNbt = withoutCurrentNames(voodooNbt);
        voodooNbt.putUuid("sparkwitch:judge_voodoo_actor", actor);
        voodoo.readFromNbt(voodooNbt, registries);
        NbtCompound voodooSaved = new NbtCompound();
        voodoo.writeToNbt(voodooSaved, registries);
        check(voodooSaved.containsUuid("sparkdlc:judge_voodoo_actor")
                && actor.equals(voodooSaved.getUuid("sparkdlc:judge_voodoo_actor")), "voodoo saved: " + voodooSaved);
        checkNoOldKey(voodooSaved, "voodoo saved");
        voodoo.reset();
        context.complete();
    }

    /**
     * A world saved with the duel teams under their old names, one of them still holding a player: the real duel team
     * reset (server start, round start) removes both old teams; a Fisher pufferfish left with the old tag is swept by
     * the real round sweep, an untagged pufferfish is not.
     * 以旧名字保存了决斗队伍、其中一个仍有玩家的世界：真实的决斗队伍重置（服务端启动、回合开始）移除两个旧队伍；带旧标签遗留的渔夫
     * 河豚被真实的回合清扫清除，不带标签的河豚不受影响。
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void leftoverOldTeamsAndTagsAreCleanedUpAsBefore(TestContext context) throws ReflectiveOperationException {
        MinecraftServer server = context.getWorld().getServer();
        Scoreboard scoreboard = server.getScoreboard();
        Team oldKnight = scoreboard.addTeam("sparkwitch_duel_knight");
        scoreboard.addTeam("sparkwitch_duel_target");
        scoreboard.addScoreHolderToTeam("LegacyDuelist", oldKnight);
        Method resetDuelTeams = KnightDuelService.class.getDeclaredMethod("resetDuelTeams", MinecraftServer.class);
        resetDuelTeams.setAccessible(true);
        resetDuelTeams.invoke(null, server);
        check(scoreboard.getTeam("sparkwitch_duel_knight") == null && scoreboard.getTeam("sparkwitch_duel_target") == null,
                "the old duel teams survived the reset");
        check(scoreboard.getScoreHolderTeam("LegacyDuelist") == null, "a player is still on an old duel team");

        PufferfishEntity tagged = context.spawnEntity(EntityType.PUFFERFISH, new BlockPos(1, 2, 1));
        tagged.addCommandTag("sparkwitch_fisher_pufferfish");
        PufferfishEntity untagged = context.spawnEntity(EntityType.PUFFERFISH, new BlockPos(2, 2, 1));
        Method sweepAll = FisherPufferfishService.class.getDeclaredMethod("sweepAll", MinecraftServer.class);
        sweepAll.setAccessible(true);
        sweepAll.invoke(null, server);
        check(tagged.isRemoved(), "a pufferfish with the old Fisher tag survived the sweep");
        check(!untagged.isRemoved(), "the sweep removed an untagged pufferfish");
        untagged.discard();
        context.complete();
    }

    private static ItemStack decodeStack(RegistryWrapper.WrapperLookup registries, net.minecraft.item.Item item,
                                         NbtCompound customData) {
        NbtCompound components = new NbtCompound();
        components.put("minecraft:custom_data", customData.copy());
        NbtCompound stack = new NbtCompound();
        stack.putString("id", net.minecraft.registry.Registries.ITEM.getId(item).toString());
        stack.putInt("count", 1);
        stack.put("components", components);
        return ItemStack.fromNbt(registries, stack).orElseThrow(() -> new GameTestException("cannot decode " + stack));
    }

    private static NbtCompound viaPacket(NbtCompound customData) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        try {
            buf.writeNbt(customData);
            return NbtComponent.PACKET_CODEC.decode(buf).copyNbt();
        } finally {
            buf.release();
        }
    }

    private static NbtCompound customData(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
    }

    private static NbtCompound flag(String key) {
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean(key, true);
        return nbt;
    }

    private static NbtCompound rootWithUuid(String rootKey, String key, UUID value) {
        NbtCompound root = new NbtCompound();
        root.putUuid(key, value);
        NbtCompound data = new NbtCompound();
        data.put(rootKey, root);
        return data;
    }

    /**
     * What a build before 2026-11-20 saved: the same NBT without any current name (a writer may put a current key
     * even when it holds nothing, which a reader rightly prefers).
     * 2026-11-20 之前的版本保存的内容：同一份 NBT，但不含任何当前名字（写入方即使没有值也可能写出当前键，读取方理应优先读取它）。
     */
    private static NbtCompound withoutCurrentNames(NbtCompound nbt) {
        NbtCompound old = nbt.copy();
        LegacyIds.renamed(LegacyDataNames.KIND).values().forEach(old::remove);
        return old;
    }

    private static void checkNoOldKey(NbtCompound nbt, String what) {
        String text = nbt.toString();
        for (String old : LegacyIds.renamed(LegacyDataNames.KIND).keySet()) {
            check(!text.contains(old), what + " still holds " + old + ": " + text);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new GameTestException(message);
        }
    }
}
