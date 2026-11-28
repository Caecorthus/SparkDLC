package dev.caecorthus.sparkdlc.entity;

import dev.caecorthus.sparkdlc.SparkDlcEntities;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Placed reality stabilization anchor: a static decoration that is never an obstacle and only disappears when a
 * player left-clicks it or a shot/grenade reaches it (owner 2026-11-26, destruction sources added 2026-11-27). No
 * gravity. It borrows the beacon's sound set (activation on placement, ambient pulse every 80 ticks, deactivation on
 * destruction), burns like a torch through a level-15 light block at its position, gives nearby players (5 blocks)
 * night vision, and denies the Alt instinct look within 15 blocks (client side).
 * 放置后的现实稳定锚：静止、从不阻挡任何东西的装饰实体，只有被玩家左键或枪械/手雷命中才会消失（所有者 2026-11-26，
 * 2026-11-27 增加摧毁途径）。不受重力。沿用信标音效（放下激活、每 80 刻环境音、被摧毁熄灭），通过所在位置的 15 级
 * 光源方块像火把一样照亮，给 5 格内的玩家夜视，并在 15 格内禁止按 Alt 使用本能透视（客户端判断）。
 *
 * <p>Every destruction goes through {@link #destroy()} from {@link RealityAnchorHits}; {@link #damage} never removes
 * it, so fire, lava, explosions, stray projectiles and creative punches outside that seam do nothing. The light block
 * is the anchor's own: it is placed only into air, remembered in NBT, and taken away again whenever the anchor is
 * gone for good (destroyed, discarded, {@code /kill}, moved), never when its chunk merely unloads. An anchor placed
 * during a match belongs to that match and leaves with it.
 * <p>所有摧毁都由 {@link RealityAnchorHits} 经 {@link #destroy()} 执行；{@link #damage} 从不移除它，因此火、岩浆、爆炸、
 * 流弹以及该接缝之外的创造模式攻击都无效。光源方块归锚自己所有：只放进空气，位置记在 NBT 里，锚彻底消失（被摧毁、
 * 被丢弃、{@code /kill}、被移动）时收回，区块仅仅卸载时不动。对局中放下的锚属于那一局，随那一局一起消失。
 */
public final class RealityAnchorEntity extends Entity {
    /** Beacon ambient interval, the same 80 ticks an active beacon uses. / 信标环境音间隔，与激活信标相同的 80 刻。 */
    private static final int AMBIENT_INTERVAL_TICKS = 80;
    /** Light, night-vision and match check interval. / 光源、夜视与对局检查的间隔。 */
    private static final int AURA_INTERVAL_TICKS = 20;
    /**
     * Placed and removed without neighbour or shape updates: a light block appearing next to a map maker's floating
     * sand, unsupported torch or carpet must never make it fall or pop.
     * 放置与移除都不触发邻居更新和形状更新：光源方块出现在地图作者悬空的沙子、无支撑的火把或地毯旁边时，绝不能让它们掉落或
     * 脱落。
     */
    private static final int LIGHT_UPDATE_FLAGS = Block.NOTIFY_LISTENERS | Block.FORCE_STATE;
    private static final String LIGHT_POS_KEY = "LightPos";
    private static final String MATCH_KEY = "Match";

    /** Where this anchor put its own light block; null while it has none. / 本锚自己放下光源方块的位置；没有时为 null。 */
    private @Nullable BlockPos lightPos;
    /** Wathe match the anchor was placed in; null for one placed outside a match. / 放置时所在的 Wathe 对局；对局外放置为 null。 */
    private @Nullable UUID matchId;
    private boolean ticked;

    public RealityAnchorEntity(EntityType<? extends RealityAnchorEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    /**
     * Wathe's replay match id while a match runs (from {@code initializeGame} to {@code finalizeGame}), else null.
     * Server only: the record manager is server state.
     * 对局进行期间（从 {@code initializeGame} 到 {@code finalizeGame}）Wathe 的回放对局 id，否则为 null。仅服务端：记录管理器
     * 属于服务端状态。
     */
    public static @Nullable UUID currentMatchId() {
        if (!GameRecordManager.hasActiveMatch()) {
            return null;
        }
        GameRecordManager.MatchRecord match = GameRecordManager.getCurrentMatch();
        return match == null ? null : match.getMatchId();
    }

    /** Called once at placement. / 放置时调用一次。 */
    public void bindToMatch(@Nullable UUID matchId) {
        this.matchId = matchId;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(getWorld() instanceof ServerWorld world) || isRemoved()) {
            return;
        }
        boolean first = !ticked;
        ticked = true;
        if (first || age % AURA_INTERVAL_TICKS == 0) {
            if (outlivedItsMatch()) {
                // Round end is not a destruction: no sound. / 对局结束不算摧毁：不播放音效。
                discard();
                return;
            }
            updateLight(world);
            applyNightVision(world);
        }
        if (age % AMBIENT_INTERVAL_TICKS == 0) {
            world.playSound(null, getBlockPos(), SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    /**
     * An anchor of a match that is over (or of one the server no longer knows after a restart) removes itself. This
     * also catches an anchor whose chunk was not loaded when {@link #discardMatchAnchors} swept the world.
     * 所属对局已结束（或服务器重启后已不认识该对局）的锚会自行移除。这也覆盖了 {@link #discardMatchAnchors} 清扫世界时区块未
     * 加载的锚。
     */
    private boolean outlivedItsMatch() {
        return matchId != null && !matchId.equals(currentMatchId());
    }

    /**
     * Round-end sweep, called at Wathe's finalize: every loaded anchor placed during a match leaves silently, with
     * its light block. Anchors placed outside a match (map decoration) stay.
     * 回合结束清理，在 Wathe 收尾时调用：所有已加载、在对局中放下的锚连同光源方块一起静默消失。对局外放下的锚（地图装饰）
     * 保留。
     */
    public static void discardMatchAnchors(ServerWorld world) {
        for (RealityAnchorEntity anchor
                : world.getEntitiesByType(SparkDlcEntities.realityAnchor(), EntityPredicates.VALID_ENTITY)) {
            if (anchor.matchId != null) {
                anchor.discard();
            }
        }
    }

    /**
     * Keeps a level-15 light block at the anchor's block position, or in the block above it when the anchor stands in
     * a carpet, slab or other low block. Only air is ever replaced, and only a block this anchor placed itself is
     * ever taken away, so a map's own blocks (its own light blocks included) are never touched.
     * 维持锚所在方块位置的 15 级光源；锚立在地毯、台阶等矮方块里时改放在其上方一格。只会替换空气，也只会收回本锚自己放下的
     * 方块，因此地图自身的方块（包括地图自己的光源方块）绝不会被改动。
     */
    private void updateLight(ServerWorld world) {
        BlockPos feet = getBlockPos();
        BlockPos above = feet.up();
        if (lightPos != null && (!(lightPos.equals(feet) || lightPos.equals(above))
                || !isOwnLight(world.getBlockState(lightPos)))) {
            // Moved (teleport), or something else took the spot (a map reset, a placed block): let go of the old one.
            // 被移动（传送），或该位置已被别的东西占用（地图重置、放置的方块）：放弃旧的光源。
            releaseLight(world);
        }
        if (lightPos == null) {
            for (BlockPos pos : new BlockPos[]{feet, above}) {
                if (world.getBlockState(pos).isAir() && world.setBlockState(pos, ownLight(), LIGHT_UPDATE_FLAGS)) {
                    lightPos = pos.toImmutable();
                    break;
                }
            }
        }
    }

    private void releaseLight(World world) {
        if (lightPos != null && isOwnLight(world.getBlockState(lightPos))) {
            world.setBlockState(lightPos, Blocks.AIR.getDefaultState(), LIGHT_UPDATE_FLAGS);
        }
        lightPos = null;
    }

    private static BlockState ownLight() {
        return Blocks.LIGHT.getDefaultState().with(LightBlock.LEVEL_15, 15).with(LightBlock.WATERLOGGED, false);
    }

    private static boolean isOwnLight(BlockState state) {
        return state.equals(ownLight());
    }

    /** Night vision for players within {@link RealityAnchorAuraService#NIGHT_VISION_RADIUS}. / 给半径内的玩家夜视。 */
    private void applyNightVision(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.squaredDistanceTo(this)
                    <= RealityAnchorAuraService.NIGHT_VISION_RADIUS * RealityAnchorAuraService.NIGHT_VISION_RADIUS) {
                RealityAnchorAuraService.grant(player);
            }
        }
    }

    /**
     * The one way a hit removes the anchor: beacon deactivation sound, then the removal (which takes the light block
     * along). Returns false when it was already gone.
     * 命中移除锚的唯一途径：播放信标熄灭音，然后移除（光源方块随之收回）。已经不存在时返回 false。
     */
    public boolean destroy() {
        if (getWorld().isClient || isRemoved()) {
            return false;
        }
        getWorld().playSound(null, getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS,
                1.0F, 1.0F);
        discard();
        return true;
    }

    /**
     * The light block leaves with the anchor whenever the anchor is gone for good (destroyed, discarded, killed, sent
     * to another dimension). A chunk unload keeps both: the block stays in the world and the anchor comes back with
     * the position in its NBT.
     * 锚彻底消失（被摧毁、被丢弃、被击杀、被送往其他维度）时光源方块随之收回。区块卸载时两者都保留：方块留在世界里，锚带着
     * NBT 中的位置回来。
     */
    @Override
    public void remove(RemovalReason reason) {
        if (!getWorld().isClient && reason != RemovalReason.UNLOADED_TO_CHUNK
                && reason != RemovalReason.UNLOADED_WITH_PLAYER) {
            releaseLight(getWorld());
        }
        super.remove(reason);
    }

    /** A copy made for another dimension must not claim a position of the world it left. / 跨维度的副本不能认领原世界的位置。 */
    @Override
    public void copyFrom(Entity original) {
        super.copyFrom(original);
        lightPos = null;
    }

    // ---- Hit geometry: crosshair-targetable, never an obstacle (Seeker device set) / 可被准星选中，从不阻挡（搜寻者设备同款） ----

    /** Crosshair-targetable so a left-click reaches {@link RealityAnchorHits}. / 可被准星选中，左键才能到达摧毁接缝。 */
    @Override
    public boolean canHit() {
        return !isRemoved();
    }

    /** Thrown grenades, shock devices and shurikens fly through it. / 投出的手雷、电击装置与手里剑会穿过它。 */
    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.IGNORE;
    }

    @Override
    public boolean isImmuneToExplosion(Explosion explosion) {
        return true;
    }

    /** Every break goes through {@link #destroy()}. / 所有摧毁都经 {@link #destroy()} 处理。 */
    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected MoveEffect getMoveEffect() {
        return MoveEffect.NONE;
    }

    @Override
    public boolean canUsePortals(boolean allowVehicles) {
        return false;
    }

    @Override
    public boolean doesRenderOnFire() {
        return false;
    }

    // ---- Persistence / 持久化 ----

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        lightPos = NbtHelper.toBlockPos(nbt, LIGHT_POS_KEY).orElse(null);
        matchId = nbt.containsUuid(MATCH_KEY) ? nbt.getUuid(MATCH_KEY) : null;
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (lightPos != null) {
            nbt.put(LIGHT_POS_KEY, NbtHelper.fromBlockPos(lightPos));
        }
        if (matchId != null) {
            nbt.putUuid(MATCH_KEY, matchId);
        }
    }
}
