package dev.caecorthus.sparkdlc.mixin.trait;

import dev.caecorthus.sparkdlc.legacy.LegacyDataNames;
import dev.caecorthus.sparkdlc.trait.component.TraitPlayerComponent;
import dev.caecorthus.sparkdlc.trait.killer.conscience.ConsciencePoisonerService;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerStaminaComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.entity.PoisonGasCloudEntity;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Replaces only Conscience Poisoner gas clouds with blue-poison gas behavior.
 *  只把善良毒师创建的毒气云替换为蓝毒气逻辑。 */
@Mixin(PoisonGasCloudEntity.class)
public abstract class PoisonGasCloudEntityMixin extends Entity {
    /**
     * Renamed from its former-mod name on 2026-11-20 (owner: "就数据可以改"); data saved under the old name is still
     * read (current name first) and written back under this one (legacy.LegacyDataNames).
     * 2026-11-20 起不再使用旧模组名（所有者：“就数据可以改”）；旧名字下保存的数据仍可读取（先读当前名字），并以本名字写回
     * （legacy.LegacyDataNames）。
     */
    @Unique
    private static final String CONSCIENCE_GAS_KEY = "sparkdlcConscienceGas";
    @Unique
    private static final int MAX_GAS_BLOCKS = 500;
    @Unique
    private static final int MAX_LIFETIME = 600;
    @Unique
    private static final int SPREAD_INTERVAL = 8;
    @Unique
    private static final int EXPOSURE_THRESHOLD = 100;
    @Unique
    private static final double MAX_SPREAD_RADIUS_SQUARED = 400.0;
    @Unique
    private static final DustParticleEffect BLUE_GAS_PARTICLE = new DustParticleEffect(
            new Vector3f(0.0f, 0.75f, 1.0f),
            1.5f
    );

    @Shadow(remap = false)
    @Final
    private Set<BlockPos> gasBlocks;
    @Shadow(remap = false)
    private Set<BlockPos> frontier;
    @Shadow(remap = false)
    @Final
    private Map<UUID, Integer> exposureTicks;
    @Shadow(remap = false)
    @Final
    private Set<UUID> playersInGas;
    @Shadow(remap = false)
    private UUID ownerUuid;
    @Shadow(remap = false)
    private int age;

    @Unique
    private boolean sparkdlc$conscienceGas;
    // Particle sampling list; the blue cloud only grows, so it is rebuilt only after a spread added blocks.
    // 粒子采样列表；蓝色毒气只会扩散增长，因此只在扩散新增方块后才重建。
    @Unique
    private List<BlockPos> sparkdlc$particleBlocks = List.of();

    protected PoisonGasCloudEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow(remap = false)
    private void clearAllGasExhaustion() {
    }

    @Shadow(remap = false)
    private boolean doesShapeBlockExit(VoxelShape shape, Direction direction) {
        return false;
    }

    @Shadow(remap = false)
    private boolean isBlockTooSolid(VoxelShape shape) {
        return false;
    }

    @Shadow(remap = false)
    private boolean doesShapeBlockEntry(VoxelShape shape, Direction moveDirection) {
        return false;
    }

    @Inject(method = "setOwnerUuid", at = @At("TAIL"), remap = false)
    private void sparkdlc$markConscienceGas(UUID uuid, CallbackInfo ci) {
        if (uuid == null || !(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        ServerPlayerEntity owner = serverWorld.getServer().getPlayerManager().getPlayer(uuid);
        sparkdlc$conscienceGas = ConsciencePoisonerService.isConsciencePoisoner(
                owner,
                GameWorldComponent.KEY.get(serverWorld)
        );
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void sparkdlc$tickConscienceGas(CallbackInfo ci) {
        if (!sparkdlc$conscienceGas) {
            return;
        }
        sparkdlc$tickBlueGas();
        ci.cancel();
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void sparkdlc$writeConscienceGas(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean(CONSCIENCE_GAS_KEY, sparkdlc$conscienceGas);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void sparkdlc$readConscienceGas(NbtCompound nbt, CallbackInfo ci) {
        sparkdlc$conscienceGas = nbt.getBoolean(LegacyDataNames.keyIn(nbt, CONSCIENCE_GAS_KEY));
    }

    @Unique
    private void sparkdlc$tickBlueGas() {
        super.tick();
        age++;
        if (age > MAX_LIFETIME) {
            clearAllGasExhaustion();
            this.discard();
            return;
        }
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        sparkdlc$spreadGas(serverWorld);
        sparkdlc$applyBlueGas(serverWorld);
        sparkdlc$spawnBlueGasParticles(serverWorld);
    }

    @Unique
    private void sparkdlc$spreadGas(ServerWorld serverWorld) {
        if (age == 1) {
            BlockPos startPos = this.getBlockPos();
            gasBlocks.add(startPos);
            frontier.add(startPos);
        }
        if (age % SPREAD_INTERVAL != 0 || frontier.isEmpty() || gasBlocks.size() >= MAX_GAS_BLOCKS) {
            return;
        }
        Set<BlockPos> newFrontier = new HashSet<>();
        for (BlockPos pos : frontier) {
            if (gasBlocks.size() >= MAX_GAS_BLOCKS) {
                break;
            }
            boolean stillEdge = false;
            for (Direction direction : Direction.values()) {
                if (gasBlocks.size() >= MAX_GAS_BLOCKS) {
                    break;
                }
                BlockPos neighbor = pos.offset(direction);
                if (gasBlocks.contains(neighbor)
                        || neighbor.getSquaredDistance(this.getBlockPos()) > MAX_SPREAD_RADIUS_SQUARED) {
                    continue;
                }
                VoxelShape fromShape = serverWorld.getBlockState(pos).getCollisionShape(serverWorld, pos);
                VoxelShape toShape = serverWorld.getBlockState(neighbor).getCollisionShape(serverWorld, neighbor);
                if (doesShapeBlockExit(fromShape, direction)
                        || isBlockTooSolid(toShape)
                        || doesShapeBlockEntry(toShape, direction)) {
                    stillEdge = true;
                    continue;
                }
                gasBlocks.add(neighbor);
                newFrontier.add(neighbor);
            }
            if (stillEdge) {
                newFrontier.add(pos);
            }
        }
        frontier = newFrontier;
    }

    @Unique
    private void sparkdlc$applyBlueGas(ServerWorld serverWorld) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(serverWorld);
        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
                continue;
            }
            boolean effectiveCivilian = ConsciencePoisonerService.shouldBlueGasIgnorePlayer(
                    gameWorld.getRole(player),
                    TraitPlayerComponent.KEY.get(player).getActiveTraitIds()
            );
            boolean inGas = sparkdlc$isPlayerInGas(player);
            if (effectiveCivilian) {
                exposureTicks.put(player.getUuid(), 0);
                sparkdlc$clearGasExhaustion(player);
                // Effective civilians lose sanity only while standing in the gas; Poisoners are exempt as in NoellesRoles gas.
                // 有效好人只在待在毒气里时扣理智；与 NoellesRoles 原版毒气一致，毒师本人豁免。
                if (inGas && !gameWorld.isRole(player, Noellesroles.POISONER)) {
                    ConsciencePoisonerService.applyBlueSanityDrain(player, 1);
                }
                continue;
            }
            if (inGas) {
                int ticks = exposureTicks.getOrDefault(player.getUuid(), 0) + 1;
                exposureTicks.put(player.getUuid(), ticks);
                sparkdlc$applyGasExhaustion(player);
                if (ticks >= EXPOSURE_THRESHOLD) {
                    ConsciencePoisonerService.applyBluePoison(
                            player,
                            ownerUuid,
                            ConsciencePoisonerService.BLUE_GAS_POISON_TICKS
                    );
                    exposureTicks.put(player.getUuid(), 0);
                }
            } else {
                exposureTicks.put(player.getUuid(), 0);
                sparkdlc$clearGasExhaustion(player);
            }
        }
    }

    @Unique
    private boolean sparkdlc$isPlayerInGas(ServerPlayerEntity player) {
        Box box = player.getBoundingBox();
        BlockPos.Mutable cell = new BlockPos.Mutable();
        for (int x = MathHelper.floor(box.minX); x <= MathHelper.floor(box.maxX); x++) {
            for (int y = MathHelper.floor(box.minY); y <= MathHelper.floor(box.maxY); y++) {
                for (int z = MathHelper.floor(box.minZ); z <= MathHelper.floor(box.maxZ); z++) {
                    if (gasBlocks.contains(cell.set(x, y, z))) {
                        return true;
                    }
                }
            }
        }
        // Mirrors NoellesRoles' isInGas (1.7.6): a sleeping player's box lies inside the bed block, which gas cannot
        // enter, so the open cells around the bed (above and the four horizontal neighbours) count as well.
        // 与 NoellesRoles 的 isInGas（1.7.6）保持一致：睡眠玩家的碰撞箱位于毒气无法进入的床方块内部，
        // 因此床周围的开放方块（正上方与四个水平相邻方块）也算作暴露。
        if (player.isSleeping()) {
            BlockPos bedPos = player.getBlockPos();
            if (gasBlocks.contains(bedPos.up())) {
                return true;
            }
            for (Direction direction : Direction.Type.HORIZONTAL) {
                if (gasBlocks.contains(bedPos.offset(direction))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    private void sparkdlc$applyGasExhaustion(ServerPlayerEntity player) {
        PlayerStaminaComponent stamina = PlayerStaminaComponent.KEY.get(player);
        if (!stamina.isInfiniteStamina()) {
            stamina.setSprintingTicks(0);
            stamina.setExhausted(true);
            playersInGas.add(player.getUuid());
        }
    }

    @Unique
    private void sparkdlc$clearGasExhaustion(ServerPlayerEntity player) {
        if (!playersInGas.remove(player.getUuid())) {
            return;
        }
        PlayerStaminaComponent stamina = PlayerStaminaComponent.KEY.get(player);
        if (!stamina.isInfiniteStamina()) {
            stamina.setExhausted(false);
            stamina.setSprintingTicks(76.0f);
        }
    }

    @Unique
    private void sparkdlc$spawnBlueGasParticles(ServerWorld serverWorld) {
        if (gasBlocks.isEmpty()) {
            return;
        }
        if (sparkdlc$particleBlocks.size() != gasBlocks.size()) {
            sparkdlc$particleBlocks = new ArrayList<>(gasBlocks);
        }
        List<BlockPos> blockList = sparkdlc$particleBlocks;
        int particleCount = 4 + serverWorld.random.nextInt(3);
        for (int i = 0; i < particleCount && !blockList.isEmpty(); i++) {
            BlockPos pos = blockList.get(serverWorld.random.nextInt(blockList.size()));
            serverWorld.spawnParticles(
                    BLUE_GAS_PARTICLE,
                    pos.getX() + 0.5 + serverWorld.random.nextDouble() * 0.3,
                    pos.getY() + 0.5 + serverWorld.random.nextDouble() * 0.3,
                    pos.getZ() + 0.5 + serverWorld.random.nextDouble() * 0.3,
                    1,
                    0,
                    0,
                    0,
                    0
            );
        }
    }
}
